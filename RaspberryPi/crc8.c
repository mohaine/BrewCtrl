#include "crc8.h"

/*
 * crc8.c
 *
 * Computes a 8-bit CRC
 *
 */

#include <stdio.h>

#define GP  0x107   /* x^8 + x^2 + x + 1 */
#define DI  0x07

static unsigned char crc8_table[256]; /* 8-bit table */
static int made_table = 0;

static void init_crc8()
/*
 * Should be called before any other crc function.
 */
{
	int i, j;
	unsigned char crc;

	if (!made_table) {
		for (i = 0; i < 256; i++) {
			crc = i;
			for (j = 0; j < 8; j++)
				crc = (crc << 1) ^ ((crc & 0x80) ? DI : 0);
			crc8_table[i] = crc & 0xFF;
			/* printf("table[%d] = %d (0x%X)\n", i, crc, crc); */
		}
		made_table = 1;
	}
}

void crc8(unsigned char *crc, unsigned char m)
/*
 * For a byte array whose accumulated crc value is stored in *crc, computes
 * resultant crc obtained by appending m to the byte array
 */
{
	if (!made_table)
		init_crc8();

	*crc = crc8_table[(*crc) ^ m];
	*crc &= 0xFF;
}

byte computeCrc8(byte * buffer, int offset, int length) {
	byte result = 0;
	for (int i = offset; i < length; i++) {
		crc8(&result, buffer[i]);
	}
	return result;
}
