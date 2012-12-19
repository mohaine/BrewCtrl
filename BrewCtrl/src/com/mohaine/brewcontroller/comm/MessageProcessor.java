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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MessageProcessor {

	public static final int BUFFER_SIZE = 5000;
	private byte[] serialBuffer = new byte[BUFFER_SIZE];
	private int serialBufferOffset = 0;
	private Collection<MessageReader> binaryMessages;
	private boolean logMessages;

	public MessageProcessor(boolean logMessages, Collection<MessageReader> binaryMessages) {
		this.logMessages = logMessages;
		this.binaryMessages = binaryMessages;
	}

	public boolean readStream(InputStream inputStream) throws IOException {
		boolean readMessage = false;
		while (inputStream.available() > 0 && serialBufferOffset < serialBuffer.length) {

			int readValue = inputStream.read();

			if (readValue < 0) {
				throw new RuntimeException("Read Smaller Then Zero");
			}

			// System.out.println("      READ " + readValue);
			serialBuffer[serialBufferOffset++] = (byte) readValue;

			int bufferOffset = serialBufferOffset - 1;
			if (serialBuffer[bufferOffset] == CommConstants.DATA_END) {

				for (MessageReader mr : binaryMessages) {
					// On stop bit
					int messageStart = bufferOffset - (mr.getLength() + MessageEnvelope.SIZE - 1);
					if (messageStart >= 0) {

						if (MessageEnvelope.validateMessage(serialBuffer, messageStart, mr)) {
							mr.readFrom(serialBuffer, messageStart + MessageEnvelope.START_SIZE);

							if (logMessages) {
								log(serialBuffer, messageStart, bufferOffset - messageStart + 1);
							}

							readMessage = true;
							if (messageStart > 0) {
								int extraLength = messageStart - 1;
								if (extraLength > 0) {
									handleExtra(serialBuffer, 0, extraLength);
								}
							}
							serialBufferOffset = 0;
						}
					}
				}
			}
		}

		if (serialBufferOffset >= serialBuffer.length) {
			handleExtra(serialBuffer, 0, serialBufferOffset);
			serialBufferOffset = 0;
		}

		return readMessage;
	}

	private void log(byte[] serialBuffer, int offset, int length) throws IOException {
		File file = new File("brewlog.bin");
		FileOutputStream fos = new FileOutputStream(file, true);
		try {
			fos.write(serialBuffer, offset, length);
		} finally {
			fos.close();
		}

	}

	private void handleExtra(byte[] data, int offset, int length) {
		System.out.println(new String(data, offset, length));

	}
}
