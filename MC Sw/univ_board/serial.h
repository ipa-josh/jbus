#pragma once

#define SEND_0		0x80
#define SEND_1		0x81
#define SEND_OK		0x82
#define SEND_FAILED	0x83
#define RECV_FAILED	0x84
#define SEND_DONE	0x00
#define RECV_CON	0x12
#define SET_TIME	0xfc
#define GET_ERROR	0xfd
#define SEND_MASK	0xc0

#include "softuart.h"

void ToHost_Transmit_Byte( unsigned char data );
