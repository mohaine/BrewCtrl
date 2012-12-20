package com.mohaine.brewcontroller.comm.msg;

import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HeaterMode;
import com.mohaine.brewcontroller.comm.BinaryMessage;
import com.mohaine.brewcontroller.comm.ByteUtils;
import com.mohaine.brewcontroller.comm.MessageReader;
import com.mohaine.brewcontroller.comm.MessageWriter;
import com.mohaine.brewcontroller.comm.ReadListener;
import com.mohaine.brewcontroller.comm.CommConstants;

public class ControlMessageReaderWriter extends BinaryMessage implements MessageWriter, MessageReader {
	private static final int COMM_LOSS_MASK = 0x01;
	private HardwareControl control;
	private ByteUtils byteUtils = new ByteUtils();
	private ReadListener<ControlMessageReaderWriter> listener;

	public ControlMessageReaderWriter() {
		super(CommConstants.HARDWARE_CONTROL, 19);
	}

	@Override
	public void readFrom(byte[] buffer, int offset) {
		control.setControlId(byteUtils.getLong(buffer, offset));
		offset += 8;
		control.setMillis(byteUtils.getLong(buffer, offset));
		offset += 8;
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

		byteUtils.putLong(buffer, offset, (short) control.getControlId());
		offset += 8;
		byteUtils.putLong(buffer, offset, (int) control.getMillis());
		offset += 8;
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