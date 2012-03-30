package com.mohaine.brewcontroller.serial;

import com.mohaine.brewcontroller.bean.HardwareSensor;

public class SensorMessageReaderWriter extends BinaryMessage implements MessageReader, MessageWriter {
	private ByteUtils byteUtils = new ByteUtils();
	private HardwareSensor sensor;
	private ReadListener listener;

	public SensorMessageReaderWriter() {
		super(SerialConstants.SENSOR_CONTROL, 13);
	}

	@Override
	public void writeTo(byte[] readBuffer, int offset) {
		writeAddress(readBuffer, offset, sensor.getAddress());
		offset += 8;
		readBuffer[offset++] = sensor.isReading() ? SerialConstants.TRUE : SerialConstants.FALSE;
		byteUtils.putFloat(readBuffer, offset, (float) sensor.getTempatureC());
		offset += 4;

	}

	@Override
	public void readFrom(byte[] readBuffer, int offset) {
		sensor.setAddress(readAddress(readBuffer, offset));
		offset += 8;

		sensor.setReading(readBuffer[offset++] == SerialConstants.TRUE);
		sensor.setTempatureC(byteUtils.getFloat(readBuffer, offset));
		offset += 4;

		if (listener != null) {
			listener.onRead();
		}
	}

	public void setListener(ReadListener listener) {
		this.listener = listener;
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

	public HardwareSensor getSensor() {
		return sensor;
	}

	public void setSensor(HardwareSensor sensor) {
		this.sensor = sensor;
	}

}