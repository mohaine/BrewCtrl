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

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.mohaine.brewcontroller.BrewPrefs;
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HardwareStatus;
import com.mohaine.brewcontroller.bean.HeaterMode;
import com.mohaine.brewcontroller.bean.HardwareSensor;

final class ReadWriteThread implements Runnable {

	// TODO Add windows comm ports
	private final String[] COMM_PORTS = { "/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3", "/dev/ttyACM0" };

	private final class ControlMessageWriter extends BinaryMessage implements MessageWriter {

		private ControlMessageWriter() {
			super(SerialConstants.HARDWARE_CONTROL, 29);
		}

		@Override
		public void writeTo(byte[] buffer, int offset) {
			HardwareControl control = serialHardwareComm.getHardareControl();
			byteUtils.putShort(buffer, offset, (short) controlId);
			offset += 2;

			buffer[offset++] = (byte) (control.getMode() == HeaterMode.ON ? 1 : 0);
			buffer[offset++] = (byte) control.getBoilDuty();
			buffer[offset++] = (byte) (control.isMashOn() ? 1 : 0);

			writeAddress(buffer, offset, control.getHltSensor());
			offset += 8;
			writeAddress(buffer, offset, control.getTunSensor());
			offset += 8;

			byteUtils.putFloat(buffer, offset, (float) control.getHltTargetTemp());
			offset += 4;
			byteUtils.putFloat(buffer, offset, (float) control.getTunTargetTemp());
			offset += 4;

		}
	}

	private final class SensorMessageReader extends BinaryMessage implements MessageReader {
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

	private final class ControlMessageReader extends BinaryMessage implements MessageReader {
		private ControlMessageReader() {
			super(SerialConstants.STATUS_CONTROL, 31);
		}

		@Override
		public void readFrom(byte[] readBuffer, int offset) {
			lastReadTime = System.currentTimeMillis();
			if (serialHardwareComm.getStatus().equals(SerialHardwareComm.STATUS_NO_COMM_READ)) {
				serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_COMM);
			}

			status.setControlId(byteUtils.getShort(readBuffer, offset));
			offset += 2;

			status.setMode(readBuffer[offset++] == SerialConstants.TRUE ? HeaterMode.ON : HeaterMode.OFF);
			status.setHltDuty(readBuffer[offset++]);
			status.setBoilDuty(readBuffer[offset++]);
			status.setPumpOn(readBuffer[offset++] == SerialConstants.TRUE);
			status.setMashOn(readBuffer[offset++] == SerialConstants.TRUE);

			status.setHltSensor(readAddress(readBuffer, offset));
			offset += 8;
			status.setTunSensor(readAddress(readBuffer, offset));
			offset += 8;

			status.setHltTargetTemp(byteUtils.getFloat(readBuffer, offset));
			offset += 4;
			status.setTunTargetTemp(byteUtils.getFloat(readBuffer, offset));
			offset += 4;
		}
	}

	private ControlMessageWriter writer = new ControlMessageWriter();
	private final SerialHardwareComm serialHardwareComm;
	private InputStream inputStream;

	private int controlId;
	private OutputStream outputStream;
	private int lastWriteControlId = -1;
	private long lastWriteTime = System.currentTimeMillis() - 10000;

	private SerialPort serialPort;

	private HardwareStatus status = new HardwareStatus();

	private long lastReadTime;

	private ByteUtils byteUtils = new ByteUtils();
	private BrewPrefs prefs;
	private final MessageProcessor processor;

	{

		ArrayList<MessageReader> readers = new ArrayList<MessageReader>();
		readers.add(new ControlMessageReader());
		readers.add(new SensorMessageReader());
		processor = new MessageProcessor(readers);
	}

	public ReadWriteThread(SerialHardwareComm serialHardwareComm, BrewPrefs prefs) {
		this.serialHardwareComm = serialHardwareComm;
		this.prefs = prefs;
		status.setMode(HeaterMode.OFF);
//		status.setHltSensor(prefs.getHltSensorAddress());
//		status.setTunSensor(prefs.getTunSensorAddress());
	}

	public void run() {
		try {
			while (this.serialHardwareComm.run) {

				if (reconnectIfNeeded()) {
					processRead();
					processWrite();
					processControlId();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		} finally {
			disconnect();
		}
	}

	private boolean reconnectIfNeeded() {
		if (inputStream == null) {
			try {

				if (System.getProperty("gnu.io.rxtx.SerialPorts") == null) {

					StringBuffer sb = new StringBuffer();
					for (String string : COMM_PORTS) {
						if (sb.length() > 0) {
							sb.append(File.pathSeparator);
						}
						sb.append(string);
					}
					System.setProperty("gnu.io.rxtx.SerialPorts",sb.toString());
				}
				Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
				while (portList.hasMoreElements()) {
					CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
					if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
						SerialPort serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);

						inputStream = serialPort.getInputStream();
						outputStream = serialPort.getOutputStream();
						this.serialPort = serialPort;
						serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

						this.serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_NO_COMM_READ);
						break;
					}
				}

				if (inputStream == null) {
					this.serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_CONNECT_NO_PORT);
				}

			} catch (Exception e) {
				this.serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_CONNECT_ERROR);
			}
		}
		return inputStream != null;
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
				int lastReadControlId = status.getControlId();
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
					int offset = MessageEnvelope.writeMessage(buffer, 0, writer);

					// System.out.println("Write: " );
					// for (int i = 0; i < offset; i++) {
					// System.out.println(i + " " + ((byte) buffer[i]));
					// }

					outputStream.write(buffer, 0, offset);
					outputStream.flush();

				}

			}
		} catch (Exception e) {
			e.printStackTrace();

			this.serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_NO_COMM_WRITE);
			disconnect();
		}
	}

	private void disconnect() {

		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// ignore
			}
			inputStream = null;
		}
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				// ignore
			}
			outputStream = null;
		}
		if (serialPort != null) {
			serialPort.close();
		}
	}

	private void processRead() {
		try {
			boolean changes = processor.readStream(inputStream);
			if (changes) {
				// TODO Check for changes first
				this.serialHardwareComm.fireStateChangeHandlers();
			}

		} catch (Exception e) {
			this.serialHardwareComm.changeStatus(SerialHardwareComm.STATUS_NO_COMM_READ);
			disconnect();
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

	public HardwareStatus getStatus() {
		return status;
	}
}