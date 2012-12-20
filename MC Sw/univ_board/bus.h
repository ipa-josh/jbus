#pragma once

typedef unsigned int u16;
typedef unsigned char u8;

//enables interrupts
void jbus_init();
//returns 0 on success
char jbus_error();
char jbus_sending();
void jbus_send(volatile const u8 l);
void jbus_sendW(volatile const u8 l);	//waits 5ms before sending

//called on successful send
void jbus_on_done(volatile u8 *data, volatile u8 len);

//called on unsucessful send
void jbus_on_failure(volatile u8 *data, volatile u8 len);

//called on finished receive
void jbus_on_receive(volatile u8 *data, volatile u8 len);


void pin_int();
void setTimeJB(unsigned char t);

//some helpers

#define MAKE_ID(_class, _dev) ( (_class&0x07)|((_dev&0x03)<<3))

#define PRE_CENTRAL		0
#define PRE_HEATING		1

//CMDS
#define CMD0	0x40
#define CMD1	0x80
#define CMD2	0xA0
#define CMD3	0xC0
