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
	private static final int COMM_LOSS_MASK = 0x01;
	private HardwareControl control;
	private ByteUtils byteUtils = new ByteUtils();
	private ReadListener<ControlMessageReaderWriter> listener;

	public ControlMessageReaderWriter() {
		super(SerialConstants.HARDWARE_CONTROL, 9);
	}

	@Override
	public void readFrom(byte[] buffer, int offset) {
		control.setControlId(byteUtils.getShort(buffer, offset));
		offset += 2;
		control.setMillis(byteUtils.getInt(buffer, offset));
		offset += 4;
		control.setMode(buffer[offset++] == 1 ? HeaterMode.ON : HeaterMode.OFF);
		control.setMaxAmps(buffer[offset++]);

		int booleanValues = buffer[offset++];
		control.setTurnOffOnCommLoss((booleanValues & COMM_LOSS_MASK) != 0);

		if (listener != null) {
			listener.onRead(this);
		}
	}

	@Override
	public void writeTo(byte[] buffer, int offset) {

		byteUtils.putShort(buffer, offset, (short) control.getControlId());
		offset += 2;
		byteUtils.putInt(buffer, offset, (int) control.getMillis());
		offset += 4;
		buffer[offset++] = (byte) (control.getMode() == HeaterMode.ON ? 1 : 0);
		buffer[offset++] = (byte) control.getMaxAmps();

		int booleanValues = 0x00;
		if (control.isTurnOffOnCommLoss()) {
			booleanValues = booleanValues | COMM_LOSS_MASK;
		}

		buffer[offset++] = (byte) booleanValues;

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