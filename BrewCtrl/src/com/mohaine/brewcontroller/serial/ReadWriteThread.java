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

/**
 * 
 */
package com.mohaine.brewcontroller.serial;

import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.BrewPrefs;
import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.serial.msg.ControlMessageReaderWriter;
import com.mohaine.brewcontroller.serial.msg.ControlPointReaderWriter;
import com.mohaine.brewcontroller.serial.msg.SensorMessageReaderWriter;

final class ReadWriteThread implements Runnable {

	private final SerialHardwareComm serialHardwareComm;

	private final SerialConnection conn;

	private int controlId;
	private int lastWriteControlId = -1;
	private long lastWriteTime = System.currentTimeMillis() - 10000;

	private long lastReadTime;

	private final MessageProcessor processor;
	private ControlMessageReaderWriter controlMsgWriter = new ControlMessageReaderWriter();
	private ControlMessageReaderWriter controlMsgReader = new ControlMessageReaderWriter();
	private SensorMessageReaderWriter sensorMessageReader = new SensorMessageReaderWriter();
	private ControlPointReaderWriter controlPointReader = new ControlPointReaderWriter();
	private ControlPointReaderWriter controlPointWriter = new ControlPointReaderWriter();

	{
		HardwareControl control = new HardwareControl();
		control.setControlPoints(new ArrayList<ControlPoint>());
		controlMsgReader.setControl(control);

		sensorMessageReader.setSensor(new HardwareSensor());

		ArrayList<MessageReader> readers = new ArrayList<MessageReader>();
		readers.add(controlMsgReader);
		readers.add(sensorMessageReader);
		readers.add(controlPointReader);
		processor = new MessageProcessor(readers);

		controlPointReader.setListener(new ReadListener<ControlPointReaderWriter>() {

			@Override
			public void onRead(ControlPointReaderWriter w) {
				System.out.println("Read CP from HW");

			}
		});

	}

	public ReadWriteThread(SerialHardwareComm serialHardwareComm, final BrewPrefs prefs, SerialConnection conn) {
		this.serialHardwareComm = serialHardwareComm;
		this.conn = conn;

		sensorMessageReader.setListener(new ReadListener<SensorMessageReaderWriter>() {
			@Override
			public void onRead(SensorMessageReaderWriter r) {
				HardwareSensor readSensor = r.getSensor();

				HardwareSensor sensor = null;

				List<HardwareSensor> sensors = ReadWriteThread.this.serialHardwareComm.getSensors();
				for (HardwareSensor tempSensor : sensors) {
					if (tempSensor.getAddress().equals(readSensor.getAddress())) {
						sensor = tempSensor;
						break;
					}
				}
				if (sensor == null) {
					String defaultName = "Sensor " + (sensors.size() + 1);
					sensor = new HardwareSensor();
					sensor.setAddress(readSensor.getAddress());
					sensor.setName(prefs.getSensorName(readSensor.getAddress(), defaultName));
					sensor.setReading(readSensor.isReading());
					sensor.setTempatureC(readSensor.getTempatureC());
					sensors.add(sensor);
				}
			}
		});

	}

	public void run() {
		try {
			while (this.serialHardwareComm.run) {

				String connectError = conn.reconnectIfNeeded();

				if (connectError == null) {
					processRead();
					processWrite();
					processControlId();
				} else {
					serialHardwareComm.changeStatus(connectError);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		} finally {
			conn.disconnect();
		}
	}

	private void processControlId() {
		try {

			if (!this.serialHardwareComm.getStatus().equals(SerialHardwareComm.STATUS_NO_COMM_READ)) {
				long now = System.currentTimeMillis();

				if (now - lastReadTime > 5000) {
					this.serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_NO_COMM_READ);
				}
			}

			if (this.serialHardwareComm.getStatus().equals(SerialHardwareComm.STATUS_COMM) || serialHardwareComm.getStatus().equals(SerialHardwareComm.STATUS_CONTROL_ID)) {
				int lastReadControlId = controlMsgReader.getControl().getControlId();

				boolean invalid = lastWriteControlId - lastReadControlId > 5;

				if (lastWriteControlId < lastReadControlId && lastWriteControlId != 0) {
					invalid = true;
				}

				if (invalid) {
					this.serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_CONTROL_ID);
				} else {
					this.serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_COMM);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processWrite() {
		try {
			long now = System.currentTimeMillis();
			int timeSinceLast = (int) (now - lastWriteTime);

			if (timeSinceLast > 500) {

				HardwareControl control = this.serialHardwareComm.getHardareControl();
				if (control != null) {
					lastWriteTime = now;
					controlId++;
					if (controlId > Short.MAX_VALUE) {
						controlId = 0;
					}
					lastWriteControlId = controlId;
					byte[] buffer = new byte[126];

					control.setControlId(controlId);

					controlMsgWriter.setControl(control);
					int offset = MessageEnvelope.writeMessage(buffer, 0, controlMsgWriter);

					if (MessageEnvelope.canFit(controlPointWriter, offset, buffer)) {
						List<ControlPoint> controlPointsWriter = controlMsgWriter.getControl().getControlPoints();
						if (controlPointsWriter != null) {
							synchronized (controlPointsWriter) {
								for (ControlPoint controlPointToWrite : controlPointsWriter) {

									List<ControlPoint> controlPointsReader = controlMsgReader.getControl().getControlPoints();
									if (controlPointsReader != null) {
										synchronized (controlPointsReader) {
											boolean writeCp = true;
											for (ControlPoint controlPointReader : controlPointsReader) {

												if (controlPointToWrite.getControlPin() == controlPointReader.getControlPin()) {
													if (!controlPointToWrite.equals(controlPointReader)) {
														writeCp = false;
													}
												}
											}

											if (writeCp) {
												System.out.println("Write: " + controlPointToWrite.getControlPin() + " at " + offset);
												controlPointWriter.setControlPoint(controlPointToWrite);
												offset = MessageEnvelope.writeMessage(buffer, offset, controlPointWriter);

												if (!MessageEnvelope.canFit(controlPointWriter, offset, buffer)) {
													break;
												}
											}
										}
									}
								}
							}
						}

					}
					conn.getOutputStream().write(buffer, 0, offset);
					conn.getOutputStream().flush();

				}

			}
		} catch (Exception e) {
			e.printStackTrace();

			this.serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_NO_COMM_WRITE);
			conn.disconnect();
		}
	}

	private void processRead() {
		try {
			boolean changes = processor.readStream(conn.getInputStream());
			if (changes) {

				lastReadTime = System.currentTimeMillis();
				if (serialHardwareComm.getStatus().equals(SerialHardwareComm.STATUS_NO_COMM_READ)) {
					serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_COMM);
				}

				this.serialHardwareComm.fireStateChangeHandlers();
			}

		} catch (Exception e) {
			this.serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_NO_COMM_READ);
			conn.disconnect();
		}
	}

	public HardwareControl getHardwareStatus() {
		return controlMsgReader.getControl();
	}

}