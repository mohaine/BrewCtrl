package com.mohaine.brewcontroller.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.bean.HardwareControl;

public class MockComm implements SerialConnection, Runnable {

	private Buffer fromJava = new Buffer(250);
	private Buffer toJava = new Buffer(250);
	private MessageProcessor processor;
	private ControlMessageReaderWriter controlMsgWriter = new ControlMessageReaderWriter();

	@Inject
	public MockComm() {

		ArrayList<MessageReader> readers = new ArrayList<MessageReader>();
		readers.add(controlMsgWriter);
		controlMsgWriter.setControl(new HardwareControl());

		processor = new MessageProcessor(readers);

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
		return fromJava.getOuputStream();
	}

	@Override
	public InputStream getInputStream() {
		return toJava.getInputStream();
	}

	@Override
	public void run() {
		InputStream inputStream = fromJava.getInputStream();
		OutputStream ouputStream = toJava.getOuputStream();
		while (true) {
			try {
				while (fromJava.available() > 0) {

					boolean changes = processor.readStream(inputStream);
					if (changes) {
						byte[] buffer = new byte[126];
						int offset = MessageEnvelope.writeMessage(buffer, 0, controlMsgWriter);
						ouputStream.write(buffer, 0, offset);
						ouputStream.flush();
					}

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
