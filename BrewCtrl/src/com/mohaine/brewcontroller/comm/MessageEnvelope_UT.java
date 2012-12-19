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

package com.mohaine.brewcontroller.comm;

import java.util.Random;

import junit.framework.TestCase;

public class MessageEnvelope_UT extends TestCase {

	public void testWrite() throws Exception {
		byte[] buffer = new byte[50];

		validateWrite(new SimpleBinaryMessage(4), buffer, 0);
		validateWrite(new SimpleBinaryMessage(32), buffer, 2);
		validateWrite(new SimpleBinaryMessage(41), buffer, 3);
		validateWrite(new SimpleBinaryMessage(2), buffer, 1);
	}

	public void testCrc() throws Exception {
		SimpleBinaryMessage sbe = new SimpleBinaryMessage(5);
		byte[] buffer = new byte[50];
		int length = MessageEnvelope.writeMessage(buffer, 0, sbe);

		assertTrue(MessageEnvelope.validateMessage(buffer, 0, sbe));

		byte[] copyBuffer = new byte[length];

		int invalid = 0;
		Random r = new Random();

		System.arraycopy(buffer, 0, copyBuffer, 0, length);

		int total = 100000;

		for (int i = 0; i < total; i++) {
			System.arraycopy(buffer, 0, copyBuffer, 0, length);
			changeBit(length, copyBuffer, r);
			changeBit(length, copyBuffer, r);
			changeBit(length, copyBuffer, r);
			changeBit(length, copyBuffer, r);
			changeBit(length, copyBuffer, r);
			changeBit(length, copyBuffer, r);
			changeBit(length, copyBuffer, r);
			changeBit(length, copyBuffer, r);

			if (!MessageEnvelope.validateMessage(copyBuffer, 0, sbe)) {
				invalid++;
			}

		}

		double caughtPercent = ((double) invalid) / total * 100;
		System.out.println("Caught " + caughtPercent + "% " + invalid + "/" + total);

		assertTrue(caughtPercent > 0.99);
	}

	private void changeBit(int length, byte[] copyBuffer, Random r) {
		int byteToChange = r.nextInt(length);
		byte oldValue = copyBuffer[byteToChange];

		byte newValue = oldValue;
		while (oldValue == newValue) {
			newValue = (byte) r.nextInt();
			int bitToChange = r.nextInt(8);
			boolean andor = r.nextInt(2) == 1;

			int mask = 1 << bitToChange;
			if (andor) {
				mask = ~mask;
				newValue = (byte) (oldValue & mask);
			} else {
				newValue = (byte) (oldValue | mask);
			}

			mask = 2 << bitToChange;
			if (andor) {
				mask = ~mask;
				newValue = (byte) (oldValue & mask);
			} else {
				newValue = (byte) (oldValue | mask);
			}
		}
		copyBuffer[byteToChange] = newValue;
	}

	private void validateWrite(SimpleBinaryMessage sbe, byte[] buffer, int offset) {
		int returnOffset = MessageEnvelope.writeMessage(buffer, offset, sbe);

		// Two start bits + crc8 + end
		assertEquals(offset + sbe.getLength() + MessageEnvelope.SIZE, returnOffset);

		// TODO

		int testOffset = offset;
		int startOffset = testOffset;
		assertEquals(CommConstants.DATA_START, buffer[testOffset++]);
		assertEquals(sbe.getMessageId(), buffer[testOffset++]);
		for (int i = 0; i < sbe.getLength(); i++) {
			assertEquals(sbe.getByte(i), buffer[testOffset++]);
		}

		int crcLength = testOffset - startOffset;
		assertEquals((byte) CRC8.compute(buffer, startOffset++, crcLength), buffer[testOffset++]);
		assertEquals(CommConstants.DATA_END, buffer[testOffset++]);
	}

	private static class SimpleBinaryMessage extends BinaryMessage implements MessageReader, MessageWriter {
		byte[] parseData;

		public SimpleBinaryMessage(int length) {
			super((byte) length, length);
		}

		@Override
		public void readFrom(byte[] data, int offset) {
			parseData = new byte[getLength()];
			System.arraycopy(data, offset, parseData, 0, getLength());
		}

		public void writeTo(byte[] data, int offset) {
			for (int i = 0; i < getLength(); i++) {
				data[offset + i] = getByte(i);
			}
		}

		private byte getByte(int i) {
			return (byte) i;
		}

	}
}
