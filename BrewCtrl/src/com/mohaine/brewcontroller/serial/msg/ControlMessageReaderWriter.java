package com.mohaine.brewcontroller.serial.msg;

import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HeaterMode;
import com.mohaine.brewcontroller.serial.BinaryMessage;
import com.mohaine.brewcontroller.serial.ByteUtils;
import com.mohaine.brewcontroller.serial.MessageReader;
import com.mohaine.brewcontroller.serial.MessageWriter;
import com.mohaine.brewcontroller.serial.ReadListener;
import com.mohaine.brewcontroller.serial.SerialConstants;

public class ControlMessageReaderWriter extends BinaryMessage implements MessageWriter, MessageReader {
	private HardwareControl control;
	private ByteUtils byteUtils = new ByteUtils();
	private ReadListener<ControlMessageReaderWriter> listener;

	public ControlMessageReaderWriter() {
		super(SerialConstants.HARDWARE_CONTROL, 4);
	}

	@Override
	public void readFrom(byte[] buffer, int offset) {
		control.setControlId(byteUtils.getShort(buffer, offset));
		offset += 2;
		control.setMode(buffer[offset++] == 1 ? HeaterMode.ON : HeaterMode.OFF);
		control.setMaxAmps( buffer[offset++]   );
		if (listener != null) {
			listener.onRead(this);
		}
	}

	@Override
	public void writeTo(byte[] buffer, int offset) {

		byteUtils.putShort(buffer, offset, (short) control.getControlId());
		offset += 2;

		buffer[offset++] = (byte) (control.getMode() == HeaterMode.ON ? 1 : 0);

		buffer[offset++] = (byte) control.getMaxAmps();
	}

	public void setListener(ReadListener<ControlMessageReaderWriter> listener) {
		this.listener = listener;
	}

	public HardwareControl getControl() {
		return control;
	}

	public void setControl(HardwareControl control) {
		this.control = control;
	}

}