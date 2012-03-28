package com.mohaine.brewcontroller.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MockComm implements SerialConnection {

	@Override
	public String reconnectIfNeeded() {
		return null;
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				System.out.println("Write " + b);
			}

		};
	}

	@Override
	public InputStream getInputStream() {

		return new InputStream() {
			@Override
			public int read() throws IOException {
				return 0;
			}
		};
	}
}
