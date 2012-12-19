#include "crc8.h"

byte dscrc_table[256];
bool validTable = false;

void buildTable() {

	if (!validTable) {
		int acc;
		int crc;

		for (int i = 0; i < 256; i++) {
			acc = i;
			crc = 0;

			for (int j = 0; j < 8; j++) {
				if (((acc ^ crc) & 0x01) == 0x01) {
					crc = ((crc ^ 0x18) >> 1) | 0x80;
				} else
					crc = crc >> 1;

				acc = acc >> 1;
			}

			dscrc_table[i] = (byte) crc;

		}
		validTable = true;
	}
}

byte computeCrc8(byte * buffer, int offset, int length) {

	buildTable();
	byte crc = 0;

	for (int i = 0; i < length; i++) {
		crc = dscrc_table[(crc ^ buffer[i + offset]) & 0x0FF];
	}

	return (crc & 0x0FF);
	return crc;

}
