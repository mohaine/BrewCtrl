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

final class ReadWriteThread implements Runnable {

	private final class SensorMessageReader extends BinaryMessage implements MessageReader {
		private ByteUtils byteUtils = new ByteUtils();

		private SensorMessageReader() {
			super(SerialConstants.SENSOR_CONTROL, 13);
		}

		@Override
		public void readFrom(byte[] readBuffer, int offset) {
			String address = readAddress(readBuffer, offset);
			offset += 8;
			HardwareSensor sensor = null;

			List<HardwareSensor> tempSensors = serialHardwareComm.getSensors();

			for (HardwareSensor tempSensor : tempSensors) {
				if (tempSensor.getAddress().equals(address)) {
					sensor = tempSensor;
					break;
				}
			}
			if (sensor == null) {
				String defaultName = "Sensor " + (tempSensors.size() + 1);
				sensor = new HardwareSensor(address);
				sensor.setName(prefs.getSensorName(address, defaultName));
				tempSensors.add(sensor);
			}

			sensor.setReading(readBuffer[offset++] == SerialConstants.TRUE);
			sensor.setTempatureC(byteUtils.getFloat(readBuffer, offset));
			offset += 4;

		}
	}

	private final SerialHardwareComm serialHardwareComm;

	private final SerialConnection conn;

	private int controlId;
	private int lastWriteControlId = -1;
	private long lastWriteTime = System.currentTimeMillis() - 10000;

	private long lastReadTime;

	private BrewPrefs prefs;
	private final MessageProcessor processor;
	private ControlMessageReaderWriter controlMsgWriter = new ControlMessageReaderWriter();
	private ControlMessageReaderWriter controlMsgReader = new ControlMessageReaderWriter();

	{
		HardwareControl control = new HardwareControl();
		control.setControlPoints(new ArrayList<ControlPoint>());
		controlMsgReader.setControl(control);

		ArrayList<MessageReader> readers = new ArrayList<MessageReader>();
		readers.add(controlMsgReader);
		processor = new MessageProcessor(readers);
	}

	public ReadWriteThread(SerialHardwareComm serialHardwareComm, BrewPrefs prefs, SerialConnection conn) {
		this.serialHardwareComm = serialHardwareComm;
		this.conn = conn;
		this.prefs = prefs;
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

	private static final String HEXES = "0123456789abcdef";

	private String readAddress(byte[] buffer, int offset) {
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < 8; j++) {

			byte b = buffer[offset++];
			sb.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));

		}
		String string = sb.toString();
		return string;
	}

	@SuppressWarnings("unused")
	private void writeAddress(byte[] buffer, int offset, String value) {
		for (int j = 0; j < 8; j++) {
			int parseInt;
			if (value != null && value.length() > j + 1) {
				int strOffset = j * 2;
				String substring = value.substring(strOffset, strOffset + 2);
				parseInt = Integer.parseInt(substring, 16);
			} else {
				parseInt = 0;
			}
			buffer[offset + j] = (byte) parseInt;
		}
	}

	public HardwareControl getHardwareStatus() {
		return controlMsgReader.getControl();
	}

}