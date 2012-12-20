// softuart.c
// AVR-port of the generic software uart written in C
//
// Generic code from
// Colin Gittins, Software Engineer, Halliburton Energy Services
// (available from the iar.com web-site -> application notes)
//
// Adapted to AVR using avr-gcc and avr-libc
// by Martin Thomas, Kaiserslautern, Germany
// <eversmith@heizung-thomas.de> 
// http://www.siwawi.arubi.uni-kl.de/avr_projects
//
// AVR-port Version 0.3  4/2007
//
// ---------------------------------------------------------------------
//
// Remarks from Colin Gittins:
//
// Generic software uart written in C, requiring a timer set to 3 times
// the baud rate, and two software read/write pins for the receive and
// transmit functions.
//
// * Received characters are buffered
// * putchar(), getchar(), kbhit() and flush_input_buffer() are available
// * There is a facility for background processing while waiting for input
// The baud rate can be configured by changing the BAUD_RATE macro as
// follows:
//
// #define BAUD_RATE  19200.0
//
// The function init_uart() must be called before any comms can take place
//
// Interface routines required:
// 1. get_rx_pin_status()
//    Returns 0 or 1 dependent on whether the receive pin is high or low.
// 2. set_tx_pin_high()
//    Sets the transmit pin to the high state.
// 3. set_tx_pin_low()
//    Sets the transmit pin to the low state.
// 4. idle()
//    Background functions to execute while waiting for input.
// 5. timer_set( BAUD_RATE )
//    Sets the timer to 3 times the baud rate.
// 6. set_timer_interrupt( timer_isr )
//    Enables the timer interrupt.
//
// Functions provided:
// 1. void flush_input_buffer( void )
//    Clears the contents of the input buffer.
// 2. char kbhit( void )
//    Tests whether an input character has been received.
// 3. char getchar( void )
//    Reads a character from the input buffer, waiting if necessary.
// 4. void turn_rx_on( void )
//    Turns on the receive function.
// 5. void turn_rx_off( void )
//    Turns off the receive function.
// 6. void putchar( char )
//    Writes a character to the serial port.
//
// ---------------------------------------------------------------------

/* 
Remarks by Martin Thomas (avr-gcc):
V0.1:
- stdio.h not used
- AVR-Timer in CTC-Mode ("manual" reload may not be accurate enough)
  Timer1 used here (Timer0 CTC not available i.e. on ATmega8)
- Global Interrupt Flag has to be enabled (see Demo-Application)
- Interface timer_set and set_timer_interrupt not used here
- internal_tx_buffer was defined as unsigned char - thas could not
  work since more than 8 bits needed, changed to unsigned short
- some variables moved from "global scope" into ISR function-scope
- GPIO initialisation included
- Added functions for string-output inspired by P. Fleury's AVR UART-lib.
V0.2:
- adjust num of RX-bits
- adapted to avr-libc ISR-macro (replaces SIGNAL)
- disable interrupts during timer-init
- used unsigned char (uint8_t) where apropriate
- removed "magic" char checking (0xc2)
- added softuart_can_transmit()
- Makefile based on template from WinAVR 1/2007
- reformated
- extended demo-application to show various methods to 
  send a string from flash and RAM
- demonstrate usage of avr-libc's stdio in demo-applcation
- tested with ATmega644 @ 3,6864MHz system-clock using
  avr-gcc 4.1.1/avr-libc 1.4.5 (WinAVR 1/2007)
V0.3
- better configuration options in softuart.h.
  ->should be easier to adapt to different AVRs
- tested with ATtiny85 @ 1MHz (int R/C) with 2400 bps
- AVR-Studio Project-File
*/

#ifdef EN_UART

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>

#include "softuart.h"

#define SU_TRUE 1
#define SU_FALSE 0

// startbit and stopbit parsed internaly (see ISR)
#define RX_NUM_OF_BITS (8)
volatile static char              inbuf[SOFTUART_IN_BUF_SIZE];
volatile static char              rsbuf[16];
volatile static unsigned char	  rsp1 = 0, rsp2;

volatile static unsigned char    qin  = 0;
/*volatile*/ static unsigned char qout = 0;
volatile static unsigned char    flag_rx_off;
volatile static unsigned char    flag_rx_ready;

// 1 Startbit, 8 Databits, 1 Stopbit = 10 Bits/Frame
#define TX_NUM_OF_BITS (10)
volatile static unsigned char  flag_tx_ready;
volatile static unsigned char  timer_tx_ctr;
volatile static unsigned char  bits_left_in_tx;
volatile static unsigned short internal_tx_buffer; /* ! mt: was type uchar - this was wrong */
volatile static unsigned char flag_rx_waiting_for_stop_bit = SU_FALSE;

#define set_tx_pin_high()      ( SOFTUART_TXPORT |=  ( 1<<SOFTUART_TXBIT ) )
#define set_tx_pin_low()       ( SOFTUART_TXPORT &= ~( 1<<SOFTUART_TXBIT ) )
#define get_rx_pin_status()    ( SOFTUART_RXPIN  & ( 1<<SOFTUART_RXBIT ) )
// #define get_rx_pin_status() ( ( SOFTUART_RXPIN & ( 1<<SOFTUART_RXBIT ) ) ? 1 : 0 )

ISR(SOFTUART_T_COMP_LABEL)
{
	static unsigned char rx_mask;
	
	static char timer_rx_ctr;
	static char bits_left_in_rx;
	static unsigned char internal_rx_buffer;
	
	char start_bit, flag_in;
	char tmp;
	
	// Transmitter Section
	if ( flag_tx_ready ) {
		tmp = timer_tx_ctr;
		if ( --tmp <= 0 ) { // if ( --timer_tx_ctr <= 0 )
			if ( internal_tx_buffer & 0x01 ) {
				set_tx_pin_high();
			}
			else {
				set_tx_pin_low();
			}
			internal_tx_buffer >>= 1;
			tmp = 3; // timer_tx_ctr = 3;
			if ( --bits_left_in_tx <= 0 ) {
				flag_tx_ready = SU_FALSE;

				if(rsp2!=rsp1) {
					softuart_putchar(rsbuf[rsp2&15]);
					++rsp2;
				}
			}
		}
		timer_tx_ctr = tmp;
	}

	// Receiver Section
	if ( flag_rx_off == SU_FALSE ) {
		if ( flag_rx_waiting_for_stop_bit ) {
			if ( --timer_rx_ctr <= 0 ) {
				flag_rx_waiting_for_stop_bit = SU_FALSE;
				flag_rx_ready = SU_FALSE;
				if(0==get_rx_pin_status())
					return;
				inbuf[qin] = internal_rx_buffer;
				if ( ++qin >= SOFTUART_IN_BUF_SIZE ) {
					// overflow - rst inbuf-index
					qin = 0;
				}
			}
		}
		else {  // rx_test_busy
			if ( flag_rx_ready == SU_FALSE ) {
				start_bit = get_rx_pin_status();
				// test for start bit
				if ( start_bit == 0 ) {
					flag_rx_ready      = SU_TRUE;
					internal_rx_buffer = 0;
					timer_rx_ctr       = 4;
					bits_left_in_rx    = RX_NUM_OF_BITS;
					rx_mask            = 1;
				}
			}
			else {  // rx_busy
				if ( --timer_rx_ctr <= 0 ) {
					// rcv
					timer_rx_ctr = 3;
					flag_in = get_rx_pin_status();
					if ( flag_in ) {
						internal_rx_buffer |= rx_mask;
					}
					rx_mask <<= 1;
					if ( --bits_left_in_rx <= 0 ) {
						flag_rx_waiting_for_stop_bit = SU_TRUE;
					}
				}
			}
		}
	}
}

static void avr_io_init(void)
{
	// RX-Pin as input
	SOFTUART_RXDDR &= ~( 1 << SOFTUART_RXBIT );
	// TX-Pin as output
	SOFTUART_TXDDR |=  ( 1 << SOFTUART_TXBIT );
}

static void avr_timer_init(void)
{
	unsigned char sreg_tmp;
	
	sreg_tmp = SREG;
	cli();
	
	SOFTUART_T_COMP_REG = SOFTUART_TIMERTOP;     /* set top */

	SOFTUART_T_CONTR_REGA = SOFTUART_CTC_MASKA | SOFTUART_PRESC_MASKA;
	SOFTUART_T_CONTR_REGB = SOFTUART_CTC_MASKB | SOFTUART_PRESC_MASKB;

	SOFTUART_T_INTCTL_REG |= SOFTUART_CMPINT_EN_MASK;

	SOFTUART_T_CNT_REG = 0; /* reset counter */
	
	SREG = sreg_tmp;
}

void softuart_init( void )
{
	flag_tx_ready = SU_FALSE;
	flag_rx_ready = SU_FALSE;
	flag_rx_off   = SU_FALSE;
	
	set_tx_pin_high(); /* mt: set to high to avoid garbage on init */
	avr_io_init();

	// timer_set( BAUD_RATE );
	// set_timer_interrupt( timer_isr );
	avr_timer_init(); // replaces the two calls above
}
/*
static void idle(void)
{
	// timeout handling goes here 
	// - but there is a "softuart_kbhit" in this code...
	// add watchdog-reset here if needed
}

void softuart_turn_rx_on( void )
{
	flag_rx_off = SU_FALSE;
}

void softuart_turn_rx_off( void )
{
	flag_rx_off = SU_TRUE;
}*/

char softuart_getchar( void )
{
	char ch;

	while ( qout == qin ) {
		//idle();
	}
	ch = inbuf[qout];
	if ( ++qout >= SOFTUART_IN_BUF_SIZE ) {
		qout = 0;
	}
	
	return( ch );
}

unsigned char softuart_kbhit( void )
{
	return( qin != qout );
}
/*
void softuart_flush_input_buffer( void )
{
	qin  = 0;
	qout = 0;
}

	
unsigned char softuart_can_transmit( void ) 
{
	return ( flag_tx_ready );
}
*/
void softuart_putchar( const char ch )
{
	if ( flag_tx_ready ) {
		rsbuf[rsp1&15]=ch;
		rsp1++;
		return;
		; // wait for transmitter ready
		  // add watchdog-reset here if needed;
	}

	// invoke_UART_transmit
	timer_tx_ctr       = 3;
	bits_left_in_tx    = TX_NUM_OF_BITS;
	internal_tx_buffer = ( ch<<1 ) | 0x200;
	flag_tx_ready      = SU_TRUE;
}


void softuart_enable() {
	SOFTUART_T_INTCTL_REG |= SOFTUART_CMPINT_EN_MASK;
}

void softuart_disable(){
	SOFTUART_T_INTCTL_REG &= ~SOFTUART_CMPINT_EN_MASK;
	flag_rx_waiting_for_stop_bit = flag_tx_ready = flag_rx_ready = SU_FALSE;
}

#endif