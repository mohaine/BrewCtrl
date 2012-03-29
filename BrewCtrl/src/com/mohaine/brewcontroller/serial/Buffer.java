package com.mohaine.brewcontroller.serial;

import java.io.IOException;

class Buffer {
	private byte[] serialBuffer;
	private int readOffset;
	private int writeOffset;
	private boolean hasData = false;

	public Buffer(int length) {
		super();
		this.serialBuffer = new byte[length];
		this.readOffset = 0;
		this.writeOffset = 0;
	}

	public synchronized void write(int b) throws IOException {

		if (hasData && readOffset == writeOffset) {
			throw new IOException("Overflow");
		}

		hasData = true;
		serialBuffer[writeOffset++] = (byte) b;

		if (writeOffset == serialBuffer.length) {
			writeOffset = 0;
		}
	}

	public synchronized int read() throws IOException {

		if (!hasData && readOffset == writeOffset) {
			throw new IOException("Read past data");
		}

		int read = serialBuffer[readOffset++];

		if (readOffset == serialBuffer.length) {
			readOffset = 0;
		}

		if (readOffset == writeOffset) {
			hasData = false;
		}

		return read;
	}

	public synchronized int available() {
		int avil = writeOffset - readOffset;
		if (avil == 0 && hasData) {
			avil = serialBuffer.length;
		}

		if (avil < 0) {
			avil = serialBuffer.length + avil;
		}

		return avil;
	}
}