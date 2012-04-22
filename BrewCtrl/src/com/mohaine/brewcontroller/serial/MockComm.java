package com.mohaine.brewcontroller.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.serial.msg.ControlMessageReaderWriter;
import com.mohaine.brewcontroller.serial.msg.ControlPointReaderWriter;
import com.mohaine.brewcontroller.serial.msg.SensorMessageReaderWriter;

public class MockComm implements SerialConnection, Runnable {
	private final HardwareControl control = new HardwareControl();

	private Buffer fromJava = new Buffer(126);
	private Buffer toJava = new Buffer(2500);
	private MessageProcessor processor;
	private ControlMessageReaderWriter controlMsgWriter = new ControlMessageReaderWriter();
	private SensorMessageReaderWriter sensorMessageWriter = new SensorMessageReaderWriter();
	private List<HardwareSensor> sensors = new ArrayList<HardwareSensor>();
	ControlPointReaderWriter controlPointWriter = new ControlPointReaderWriter();

	@Inject
	public MockComm() {
		control.setControlPoints(new ArrayList<ControlPoint>());

		HardwareSensor sensor1 = new HardwareSensor();
		sensor1.setAddress("0000000000000001");
		sensor1.setReading(true);
		sensor1.setTempatureC(25);
		sensors.add(sensor1);
		HardwareSensor sensor2 = new HardwareSensor();
		sensor2.setAddress("0000000000000002");
		sensor2.setReading(true);
		sensor2.setTempatureC(26);
		sensors.add(sensor2);

		ArrayList<MessageReader> readers = new ArrayList<MessageReader>();
		readers.add(controlMsgWriter);
		readers.add(createCpReader());
		controlMsgWriter.setControl(control);

		processor = new MessageProcessor(false,readers);

		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();

	}

	private ControlPointReaderWriter createCpReader() {
		ControlPointReaderWriter controlPointReader = new ControlPointReaderWriter();
		controlPointReader.setControlPoint(new ControlPoint());
		controlPointReader.setListener(new ControlPointReaderListUpdater(control.getControlPoints()));
		return controlPointReader;
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
						byte[] buffer = new byte[1260];
						int offset = MessageEnvelope.writeMessage(buffer, 0, controlMsgWriter);

						for (HardwareSensor sensor : sensors) {
							sensorMessageWriter.setSensor(sensor);
							offset = MessageEnvelope.writeMessage(buffer, offset, sensorMessageWriter);
						}

						List<ControlPoint> controlPoints = control.getControlPoints();
						synchronized (controlPoints) {
							for (ControlPoint controlPoint : controlPoints) {
								synchronized (controlPoint) {
									controlPointWriter.setControlPoint(controlPoint);
									offset = MessageEnvelope.writeMessage(buffer, offset, controlPointWriter);
								}
							}
						}
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

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}

		}

	}
}
