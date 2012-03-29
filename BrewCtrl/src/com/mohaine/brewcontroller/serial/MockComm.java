package com.mohaine.brewcontroller.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.inject.Inject;

public class MockComm implements SerialConnection, Runnable {

	private Buffer fromJava = new Buffer(250);
	private Buffer toJava = new Buffer(250);

	@Inject
	public MockComm() {
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public String reconnectIfNeeded() {
		return null;
	}

	@Override
	public void disconnect() {
	}

	@Override
	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				synchronized (fromJava) {
					fromJava.write(b);
				}
			}
		};
	}

	@Override
	public InputStream getInputStream() {

		return new InputStream() {
			@Override
			public int read() throws IOException {
				return toJava.read();
			}

			@Override
			public int available() throws IOException {
				return toJava.available();
			}

		};
	}

	@Override
	public void run() {

		while (true) {
			try {
				while (fromJava.available() > 0) {
					fromJava.read();

					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
