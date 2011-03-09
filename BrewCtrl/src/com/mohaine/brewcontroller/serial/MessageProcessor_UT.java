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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

public class MessageProcessor_UT extends TestCase {

	public void testOverflow() throws Exception {
		byte[] stringBytes = "OverflowTest\n".getBytes();

		SimpleBinaryMessage msg2 = new SimpleBinaryMessage(2);
		SimpleBinaryMessage msg3 = new SimpleBinaryMessage(3);
		ArrayList<MessageReader> binaryMessages = new ArrayList<MessageReader>();
		binaryMessages.add(msg2);
		binaryMessages.add(msg3);
		MessageProcessor mp = new MessageProcessor(binaryMessages);

		for (int i = 0; i < 1.5 * (MessageProcessor.BUFFER_SIZE / stringBytes.length); i++) {
			assertFalse(mp.readStream(new ByteArrayInputStream(stringBytes, 0, stringBytes.length)));
		}

		// Make sure new valid data works
		byte[] data = new byte[500];
		int offset = 0;
		offset = MessageEnvelope.writeMessage(data, offset, msg2);
		boolean readStream = mp.readStream(new ByteArrayInputStream(data, 0, offset));
		if (!readStream) {
			readStream = mp.readStream(new ByteArrayInputStream(data, 0, offset));
		}
		assertTrue(readStream);
		msg2.assertDataClear(0);

	}

	public void testSimple() throws Exception {
		SimpleBinaryMessage msg2 = new SimpleBinaryMessage(2);
		SimpleBinaryMessage msg3 = new SimpleBinaryMessage(3);
		ArrayList<MessageReader> binaryMessages = new ArrayList<MessageReader>();
		binaryMessages.add(msg2);
		binaryMessages.add(msg3);
		MessageProcessor mp = new MessageProcessor(binaryMessages);

		byte[] data = new byte[0];
		assertFalse(mp.readStream(new ByteArrayInputStream(data, 0, 0)));

		data = new byte[500];

		int offset = 0;
		offset = MessageEnvelope.writeMessage(data, offset, msg2);

		assertTrue(mp.readStream(new ByteArrayInputStream(data, 0, offset)));
		msg2.assertDataClear(0);

		offset = 0;
		offset = MessageEnvelope.writeMessage(data, offset, msg2);
		offset = MessageEnvelope.writeMessage(data, offset, msg3);
		// for (int i = 0; i < offset; i++) {
		// System.out.println(i + " " + ((int) data[i]));
		// }

		assertTrue(mp.readStream(new ByteArrayInputStream(data, 0, offset)));
		msg2.assertDataClear(1);
		msg3.assertDataClear(0);

		offset = 0;
		byte[] stringBytes = "Test String\n".getBytes();
		System.arraycopy(stringBytes, 0, data, offset, stringBytes.length);
		offset += stringBytes.length;

		int msg2Start = offset;

		offset = MessageEnvelope.writeMessage(data, offset, msg2);
		System.arraycopy(stringBytes, 0, data, offset, stringBytes.length);
		offset += stringBytes.length;

		int msg3Start = offset;

		offset = MessageEnvelope.writeMessage(data, offset, msg3);

		assertTrue(mp.readStream(new ByteArrayInputStream(data, 0, offset)));
		msg2.assertDataClear(2);
		msg3.assertDataClear(1);

		int tryStart = msg2Start + 3;
		assertFalse(mp.readStream(new ByteArrayInputStream(data, 0, tryStart)));
		msg2.assertNoParse();
		msg3.assertNoParse();
		assertTrue(mp.readStream(new ByteArrayInputStream(data, tryStart, offset - tryStart)));
		msg2.assertDataClear(2);
		msg3.assertDataClear(1);

		tryStart = msg3Start + 1;
		assertTrue(mp.readStream(new ByteArrayInputStream(data, 0, tryStart)));
		msg2.assertDataClear(2);
		msg3.assertNoParse();

		assertTrue(mp.readStream(new ByteArrayInputStream(data, tryStart, offset - tryStart)));
		msg2.assertNoParse();
		msg3.assertDataClear(1);

	}

	private static class SimpleBinaryMessage extends BinaryMessage implements MessageReader, MessageWriter {
		byte[] parseData;

		int genOffset = 0;

		public SimpleBinaryMessage(int length) {
			super((byte) length, length);
		}

		public void assertNoParse() {
			assertNull(parseData);
		}

		@Override
		public void readFrom(byte[] data, int offset) {
			parseData = new byte[getLength()];
			System.arraycopy(data, offset, parseData, 0, getLength());
		}

		public void writeTo(byte[] data, int offset) {
			for (int i = 0; i < getLength(); i++) {
				data[offset + i] = getByte(i, genOffset);
			}
			genOffset++;
		}

		private void assertDataClear(int genOffset) {
			assertNotNull(parseData);
			for (int i = 0; i < getLength(); i++) {
				assertEquals(getByte(i, genOffset), parseData[i]);
			}

			parseData = null;
		}

		private byte getByte(int i, int genOffset) {
			return (byte) (i + genOffset);
		}

	}

}
