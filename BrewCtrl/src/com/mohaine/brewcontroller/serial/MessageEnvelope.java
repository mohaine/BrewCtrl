/*
    Copyright 2009-2011 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package com.mohaine.brewcontroller.serial;

import com.mohaine.brewcontroller.serial.msg.ControlPointReaderWriter;

public class MessageEnvelope {

	public static final int START_SIZE = 2;
	public static final int END_SIZE = 2;
	public static final int SIZE = START_SIZE + END_SIZE;

	public static int writeMessage(byte[] buffer, int offset, MessageWriter writer) {
		int startOffset = offset;

		buffer[offset++] = SerialConstants.DATA_START;
		buffer[offset++] = writer.getMessageId();

		writer.writeTo(buffer, offset);
		offset += writer.getLength();

		// skip newline
		int crcLength = offset - startOffset;

		// This is only 8 bits so double calculate

		buffer[offset++] = computeCrc8(buffer, startOffset++, crcLength);
		buffer[offset++] = SerialConstants.DATA_END;

		return offset;
	}

	public static boolean validateMessage(byte[] buffer, int offset, MessageReader reader) {
		boolean valid = true;
		int startOffset = offset;

		valid = valid && buffer[offset++] == SerialConstants.DATA_START;
		valid = valid && buffer[offset++] == reader.getMessageId();

		if (valid) {
			offset += reader.getLength();
			int crcLength = offset - startOffset;

			valid = valid && buffer[offset++] == computeCrc8(buffer, startOffset++, crcLength);
			valid = valid && buffer[offset++] == SerialConstants.DATA_END;
		}
		return valid;
	}

	private static byte computeCrc8(byte[] buffer, int offset, int crcLength) {
		return (byte) CRC8.compute(buffer, offset, crcLength);
	}

	public static int getMessageSize(MessageWriter writer) {
		return writer.getLength() + 4;
	}

	public static boolean canFit(ControlPointReaderWriter writer, int offset, byte[] buffer) {
		return offset + MessageEnvelope.getMessageSize(writer) <= buffer.length;
	}
}
