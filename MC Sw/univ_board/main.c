#include "bus.h"
#include "serial.h"
#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>     /* in ‰lteren avr-libc Versionen <avr/delay.h> */
#include <avr/eeprom.h>

//#define _DEBUG_LED
#define _MINIMAL

#define UNBOUNCING_VAL 255

#define READ	0
#define WRITE	1
#define CONFIG	2

#define INPUT			0
#define INPUT_PULLUP	1
#define OUTPUT			2
#define PWM				3

#ifdef EN_UART
volatile char connected=0;


void ToHost_Transmit_Byte( unsigned char data )
{
	//if(connected)
	softuart_putchar(data);
}
#else

#ifdef CONFIG_ROLLO
#define PIN_NUMBER_START	4
#define PINS_NUMBER 6
#else
#define PIN_NUMBER_START	0
#define PINS_NUMBER 10
#endif
#define CONFIG_SIZE (1+PINS_NUMBER/2)
volatile uint8_t ee_configuration[CONFIG_SIZE+1] EEMEM = {};
volatile uint8_t configuration[CONFIG_SIZE+1];

volatile uint8_t conf_output_portB = 0;
volatile uint8_t conf_output_portC = 0;
volatile uint8_t conf_pwm[PINS_NUMBER] = {};
volatile uint8_t val_pwm[PINS_NUMBER] = {};
	
#define JB_ID (configuration[0])	

#endif

void init();
void periodic();


int main( void )
{
  	jbus_init();
	init();

  	// Global Interrupts aktivieren
	sei();

	for(;;) {
		periodic();
	}
}

#ifndef EN_UART
void load_config() {
	uint8_t i, data;
	
	eeprom_read_block (configuration, ee_configuration, sizeof(configuration));
	
	conf_output_portB = conf_output_portC = 0;
	
#ifdef CONFIG_ROLLO
	DDRC  |= 0x0f;
	PORTC &= ~(0x0f);
	DDRB  &= ~(0x0f<<2); //input
	PORTB |= (0x0f<<2); // pullup
#endif	
	
	for (i=PIN_NUMBER_START; i<PINS_NUMBER; i++)
	{
		data = (configuration[1+i/2]>>(i&1?4:0))&0x0F;
		
		switch(data&0x03) {
			case INPUT:
				if(i<6) {
					DDRC  &= ~(1<<i); //input
					PORTC &= ~(1<<i); //no pullup
				} else {
					DDRB  &= ~(1<<(i-4)); //input
					PORTB &= ~(1<<(i-4)); //no pullup
				}				
				break;
			case INPUT_PULLUP:
				if(i<6) {
					DDRC  &= ~(1<<i); //input
					PORTC |= (1<<i); // pullup
				} else {
					DDRB  &= ~(1<<(i-4)); //input
					PORTB |= (1<<(i-4)); // pullup
				}
				break;
			case OUTPUT:
				if(i<6) {
					conf_output_portC |= (1<<i);
					DDRC  |= (1<<i); //output
					if(data&0x04)
					PORTC |= (1<<i); //1
					else
					PORTC &= ~(1<<i); //0
				} else {
					conf_output_portB |= (1<<(i-4));
					DDRB  |= (1<<(i-4)); //output
					if(data&0x04)
					PORTB |= (1<<(i-4)); //1
					else
					PORTB &= ~(1<<(i-4)); //0
				}
				break;
			case PWM:
				if(i<6) {
					DDRC  |= (1<<i); //output
				} else {
					DDRB  |= (1<<(i-4)); //output
				}
				conf_pwm[i] = (data&0x0C)<<6;
				break;
		}
	}
}
#endif

void init() {
#ifdef EN_UART
	softuart_init( );
#else
#ifdef CONFIG_ROLLO
	TCCR1B = (1<<CS11);		//timer1 (16-bit) with pre-scaler of 8
	TIMSK1 = TOIE1;			//enable overflow interrupt
#endif
	ADCSRA = (1<<ADEN)|(1<<ADPS2);            // enable ADC, prescaler 16
	load_config();
#endif
#ifdef _DEBUG_LED
	PORTB |= (1<<PB4);
#endif
}

extern volatile u8 data[16];

#ifdef CONFIG_ROLLO
inline void motor_Up(uint8_t m) {
	PORTC = 1<<(2*m) | (PORTC&0xf0);
}
inline void motor_Down(uint8_t m) {
	PORTC = 2<<(2*m) | (PORTC&0xf0);
}
inline uint8_t motor_isUp(uint8_t m) {
	return PORTC & (1<<(2*m));
}
inline uint8_t motor_isDown(uint8_t m) {
	return PORTC & (2<<(2*m));
}
inline void motor_Stop(uint8_t m) {
	PORTC &= 0xf0;
}
inline uint8_t is_Up(uint8_t m) {
	return PORTB&(1<<(2*m));
}
inline uint8_t is_Down(uint8_t m) {
	return PORTB&(2<<(2*m));
}
static volatile uint16_t counter_rollo;
static uint16_t time_to_reach[2] = {};
inline void rollo_start(uint16_t start) {
	counter_rollo = start;
}
inline uint16_t rollo_counter() {
	return counter_rollo;
}
ISR(TIMER1_OVF_vect)
{
	if(counter_rollo)
		--counter_rollo;
}
inline void rollo_set(uint8_t val) {
	uint8_t m = val&0x80;
	if( (val&0x3f)==0x3f )
		rollo_start(0xffff);
	else
		rollo_start( (val&0x3f)*(uint32_t)time_to_reach[m]/64 );
	if(val&0x40)
		motor_Up(m);
	else
		motor_Down(m);
}
#endif

void periodic() {
#ifdef EN_UART

	unsigned char cdata;
	static u8 buffer[16];
	static u8 len=0, off=0, i;
	//int temp=0;

	/*//_delay_ms(30000);
	data='A';
	while(1) {
		softuart_putchar(data);
		data=softuart_getchar();
		_delay_ms(500);}*/

	
	/*while( !softuart_kbhit() ) {

		temp++;
		if(temp>10000) {
#ifdef _DEBUG_LED
			PORTB |= (1<<PB4);
#endif
			connected=0;
			off=len=0;
		}
		_delay_ms(1);
	}*
	
	while(1)
		if(softuart_kbhit()) {
			ToHost_Transmit_Byte(softuart_getchar()+1);
		}	*/		

	cdata=softuart_getchar();
	
	/*//ToHost_Transmit_Byte(cdata);

	if(!connected) {
		if(cdata==RECV_CON) {
			connected = 1;
#ifdef _DEBUG_LED
#error
			PORTB &= ~(1<<PB4);
#endif
		}
	}
	else*/ {
		if(len) {
			buffer[(off>>3)]=cdata;
			off+=8;
			if(off>=len) {
				while(!jbus_can_send()) ;
				cli();
				for(i=0; i<(off>>3); i++)
					data[i] = buffer[i];
				jbus_send(len);
				sei();
				
				off=len=0;
			}
		}
		else {
			if(cdata==0xfd)
				ToHost_Transmit_Byte(jbus_error());
			//else if(cdata==0xfc)
			//	;//setTimeJB(softuart_getchar());
			else if(!(cdata&SEND_MASK))
				len=cdata;
		}

	}
#else
	static uint16_t old_port = 0;//((PINC&0x3F)|((PINB<<4)&0xC0))|(((PINB>>4)&0x03)<<8);
	static uint8_t cnt[PINS_NUMBER] = {};
		
	uint8_t i=0, send=0;
	uint16_t port = ((PINC&0x3F)|((PINB<<4)&0xC0))|(((PINB>>4)&0x03)<<8);
	
#ifdef CONFIG_ROLLO
	for(i=0; i<2; i++) {
		if(time_to_reach[i]==0) {
			motor_Up(i);
			if(is_Up(i)) {
				time_to_reach[i]=1;
				rollo_start(0xffff); //start timer now
			}				
		}
		else if(time_to_reach[i]==1) {
			motor_Down(i);
			if(is_Down(i)) {
				motor_Stop(i);
				time_to_reach[i] = 0xffff-rollo_counter();
			}
		}
		else {
			if(
			(is_Down(i) && motor_isDown(i)) || 
			(is_Up(i) && motor_isUp(i)) ||
			!rollo_counter() )
				motor_Stop(i);
		}
	}
#endif
	
	for(i=PIN_NUMBER_START; i<PINS_NUMBER; i++) {
		if( ((configuration[1+i/2]>>(i&1?4:0))&0x03)==PWM ) {
			if(val_pwm[i]>=conf_pwm[i]) {
				if(i<6)
					PORTC &= ~(1<<i); //0
				else
					PORTB &= ~(1<<(i-4)); //0
			}
			else {
				if(i<6)
					PORTC |= (1<<i); //1
				else
					DDRB  |= (1<<(i-4)); //output
			}
			
			val_pwm[i]++;
		}
		else if( ((configuration[1+i/2]>>(i&1?4:0))&0x03)==INPUT || ((configuration[1+i/2]>>(i&1?4:0))&0x03)==INPUT_PULLUP ) {
			
			if( (old_port^port)&(1<<i) ) {
				cnt[i] = UNBOUNCING_VAL;
			}
			
			if(cnt[i]==1)
				send = 1;
			if(cnt[i]>0) 
				--cnt[i];
		}			
	}
	
	if(send!=0) {
		
		while(!jbus_can_send()); //wait till can send
		
		cli();
		data[0] = JB_ID|(READ<<6);
		*(uint16_t*)(data+1) = port;
		jbus_send(8+10);
		sei();
	}
	
	old_port = port;
	
	/*static uint8_t t=0;
	++t;
	data[0]=t;
	jbus_send(8);
	_delay_ms(30);*/
#endif
}

//called on successful send
void jbus_on_done(volatile u8 *data, volatile u8 len) {
	#ifdef EN_UART
	ToHost_Transmit_Byte(SEND_OK);
	#else
	#endif
}

//called on unsucessful send
void jbus_on_failure(volatile u8 *data, volatile u8 len) {
	#ifdef EN_UART
	ToHost_Transmit_Byte(RECV_FAILED);
	#else
	#endif
}

//called on unsucessful send
void jbus_on_failure_send(volatile u8 *data, volatile u8 len) {
	#ifdef EN_UART
	ToHost_Transmit_Byte(SEND_FAILED);
	#else
	#endif
}

//called on finished receive
void jbus_on_receive(volatile u8 *data, volatile u8 len) {	
#ifdef EN_UART
	ToHost_Transmit_Byte(SEND_DONE|len);
	for(u8 i=0; i<(len+7)>>3; i++)
		ToHost_Transmit_Byte(data[i]);
#else
	uint16_t ad;
	uint8_t i;
	if( len>7 && JB_ID==(data[0]&0x3F)) {
		switch( data[0]&0xC0 ) {
			case READ<<6:	//read IOs
				if(len==0+8) { //read inputs
					data[1] = (PINC&0x3F)|((PINB<<4)&0xC0);
					data[2] = (PINB>>4)&0x03;
					jbus_sendI(8+10);
				}
				else if(len==3+8) {//read analog
					// Kanal waehlen, ohne andere Bits zu beeinfluﬂen
					ADMUX = ((1<<REFS0)|(data[1]&0x07));
					//external ref.
					ADCSRA |= (1<<ADSC);            // eine Wandlung "single conversion"
					while (ADCSRA & (1<<ADSC) )     // auf Abschluss der Konvertierung warten
					;
					ad=ADCW;
					data[1] = (data[1]&0x07)|(ad<<3);
					data[2] = (ad>>5)&0x1F;
					jbus_sendI(8+3+10);
				}
				break;
				
			case WRITE<<6:	//write IOs
				if(len==8+10) {	//simple output
					PORTC = (PORTC&(~conf_output_portC))|(conf_output_portC&(data[1]));
#ifdef CONFIG_ROLLO
					rollo_set( (data[1]&0xcf)|( (data[2]&0x03)<<4 ) );
#else
					PORTB = (PORTB&(~conf_output_portB))|(conf_output_portB&( (data[1]>>4)|(data[2]<<4) ));
#endif
				}
				else if(len==8+11) {//PWM
					i = data[2]&0x07;
					if(i>9) break;
					conf_pwm[i] = data[1];
				}
				break;
				
			case CONFIG<<6:	//read/write firmware
				if(len==8+8*CONFIG_SIZE) {
					eeprom_write_block ( (data+1), ee_configuration, CONFIG_SIZE);
					load_config();
				}
				else if(len==8) {
					for(i=1; i<sizeof(configuration); i++)
						data[i] = configuration[i];
					jbus_sendI(8*sizeof(configuration));
				}				
				break;
/*
			case 0x40:	//Relais 1-5
			if(len==8+5)
			PORTA= ((data[1]&0x1F)<<2)|(PORTA&0x83);
			data[1]= (PORTA&0x7C)>>2;
			jbus_sendI(8+5);
			break;

			case 0x80:	//Leinwand-Ziel
			if(len==8+8)
			LeinwandAim = data[1];
			data[1]= LeinwandAim;
			jbus_sendI(8+8);
			break;

			case 0xC0:	//Temperatur auslesen

			// Kanal waehlen, ohne andere Bits zu beeinfluﬂen
			ADMUX = 1;
			ADCSRA |= (1<<ADSC);            // eine Wandlung "single conversion"
			while (ADCSRA & (1<<ADSC) )     // auf Abschluss der Konvertierung warten
			;
			ad=ADCW;
			data[1] = ad&0xff;;
			data[2] = (ad>>8)&0x03;
			jbus_sendI(data, 8+10);
			break;*/
		}
	}
	else if(len==6 && JB_ID==(data[0]&0x3F))
		jbus_sendI(6);
#endif
}
