#include "bus.h"
#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>     /* in �lteren avr-libc Versionen <avr/delay.h> */ 
#include "serial.h"

//#define _PDEBUG

#define TIME_0 126
/*volatile unsigned char TIME_0=126;

void setTimeJB(unsigned char t)
{
	TIME_0=t;
}*/

#define ACTIVE_TIMER() {TIFR&=~RESET_TIMER;TIMSK |= (1<<TOIE);softuart_disable();}
#define INACTIVE_TIMER() {TIMSK &= ~(1<<TOIE1);softuart_enable();}


#define TIMER_INTERRUPT TIM1_OVF_vect

#ifdef __AVR_ATtiny48__

//pins
#define PIN	(DDD2)
#define PDDR DDRD
#define PPIN (PIND&(1<<PIN))
#define PPORT PORTD
#define PIN_VEC PCINT2_vect
#define PIN_MSK PCMSK2

//timer
#define TCCR1 TCCR0A
#define GIMSK PCICR
#define EN_INT (1<<PCIE2)
#define TIFR TIFR0
#define TOIE TOIE0
#define TIMSK TIMSK0
#define RESET_TIMER ((1<<OCF0B)|(1<<OCF0A)|(1<<TOV0))
#define PRESCALER (1<<CS01)|(1<<CS00)	//64
#define TCNT TCNT0

#define TIM1_OVF_vect TIMER0_OVF_vect

#elif defined(_CENTRAL)
a
#define PIN	(DDA0)
#define PDDR DDRA
#define PPIN (PINA&(1<<PIN))
#define PPORT PORTA
#define PIN_VEC PCINT0_vect
#define PIN_MSK PCMSK0

#elif defined (__AVR_ATmega169PA__)
b
#define PIN	(DDB1)
#define PDDR DDRB
#define PPIN (PINB&(1<<PIN))
#define PPORT PORTB
#define PIN_VEC PCINT1_vect
#define PIN_MSK PCMSK1
#define TCCR0A TCCR0A
#define GIMSK EIMSK

#undef TIMER_INTERRUPT
#define TIMER_INTERRUPT TIMER1_OVF_vect

#else

#define PIN	(DDB3)
#define PDDR DDRB
#define PPIN (PINB&(1<<PIN))
#define PPORT PORTB
#define PIN_VEC PCINT0_vect
#define PIN_MSK PCMSK
#define EN_INT PCIE1
#define TOIE TOIE1
#define RESET_TIMER ((1<<OCF1B)|(1<<OCF1A)|(1<<TOV1))
#define PRESCALER (1<<CS11)|(1<<CS12)
#define TCNT TCNT0

#endif

/*#ifndef TIMSK0
#define TIMSK0 TIMSK
#endif*/

volatile u8	meta, len, off;
volatile u8 data[16];

#define PAR		1
#define	LAST	2
#define	WHICH	4

void jbus_init() {
	// Timer 1 konfigurieren
	OCR1A = 0;
	TCCR1 = PRESCALER;//|(1<<CTC1); // Prescaler 64
	//TCCR0B = 3; // Prescaler 64 

	//ACTIVE_TIMER();

	PIN_MSK |= (1<<PIN);	//enable int. on pin

	meta=0;
	len=off=0;

	GIMSK  |= EN_INT;	//enable interrupt

}

char jbus_error() {
	return meta&0x10;
}

char jbus_sending() {
	return (meta&0xc0)==0x80;
}

void jbus_send(volatile const u8 l) {
	if( (meta&0x40)==0 && PPIN) {
		meta=0x80;
		off=0xff;
		len=l;
  		TCNT = 255;//
		ACTIVE_TIMER();
	}
	else {
		meta|=0x10;
		jbus_on_failure(data,len);
	}
}

ISR (PIN_VEC)
{
	if( (meta&0xc0)==0 && !PPIN) {
		TCNT = 256-TIME_0/2;//256-(TIME_0*2+TIME_0/2);
		meta=0x40;
		len=0xff;
		ACTIVE_TIMER();
	}
	else if( (meta&0xc0)!=0x40 ) {
		if(!PPIN && (meta&LAST) ) {
			PDDR&=~(1<<PIN);
			if( (meta&0xc0)==0x80) {
				meta=0x10;
				jbus_on_failure(data,len);
			}
		}
	}
	else if((meta&0xc0)==0x40 ) {
		TCNT = 256-TIME_0/2;
	}
}

void jb0() {
	PDDR|=(1<<PIN);
	PPORT&=~(1<<PIN);
	meta&=~LAST;
}

void jb1() {
	PDDR&=~(1<<PIN);
	meta|=LAST;
}

ISR (TIM1_OVF_vect)
{
  	TCNT = 256-TIME_0;

	if( (meta&0xc0)==0x40 ) {

		if(len>250) {
			if( PPIN ) goto failure;
			++len;
		}
		else if( (meta&WHICH)!=0 ) {
			if( PPIN ) {
				if( meta&LAST ) {
					if( meta&PAR )
						goto done;
					else
						goto failure;
				}
				else
					data[len>>3]&=~(1<<(len&7));
			}
			else {
				if( meta&LAST ) {
					data[len>>3]|=(1<<(len&7));
					meta ^= PAR; }
				else {
					if( !(meta&PAR) )
						goto done;
					else
						goto failure;
				}
			}
			++len;
			meta&=~WHICH;
		}
		else {
			if( PPIN ) 
				meta|=LAST;
			else
				meta&=~LAST;
			meta|=WHICH;
		}
	}
	else if( (meta&0xc0)==0x80 ) {
		GIMSK  &= ~EN_INT;
		if(off<len) {
			u8 s=data[off>>3]&(1<<(off&7));
			if( meta&WHICH ) {
				if( s )
					jb0();
				else
					jb1();
				++off;
				meta&=~WHICH;
			}
			else {
				if( s ) {
					jb1();
					meta^=PAR;}
				else {
					jb0();
				}
				meta|=WHICH;
			}
		}
		else if( len+2==off ) {
			meta=0;
			jb1();
			jbus_on_done(data,len);
		}
		else {
			if( meta&PAR )
				jb1();
			else 
				jb0();
			++off;
		}
		GIMSK  |= EN_INT;

	}
	else
		INACTIVE_TIMER();

	return;

done:
	meta=0;

	jbus_on_receive(data,len);

	return;

failure:
	meta=0;
	
	jbus_on_failure(data,len);

	return;
}
