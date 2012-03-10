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

package com.mohaine.brewcontroller.serial;

import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.BrewPrefs;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.HardwareBase;
import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HardwareStatus;
import com.mohaine.brewcontroller.bean.HeaterMode;

public class SerialHardwareComm extends HardwareBase implements Hardware {

	/**
	 * This should be cleaned up. The Arduino has no hardware flow control and
	 * only a 126 byte buffer Keeping the write size < 126 with current code is
	 * VERY important
	 * 
	 * Should really have some flow control in the read write code. i.e. Join
	 * the read/write threads into one thread that only writes after a read
	 * shows that the buffer has room for the write.
	 * 
	 */

	static final String STATUS_CONNECT_NO_PORT = "NO TTY";
	static final String STATUS_CONNECT_ERROR = "NO CONNECT";
	static final String STATUS_NO_COMM_WRITE = "NO WRITE";
	static final String STATUS_CONTROL_ID = "CONTROL ID";
	static final String STATUS_NO_COMM_READ = "NO READ";
	static final String STATUS_COMM = "Ok";

	static final int MAX_CONTROL_ID = 999;

	private HardwareControl hardareControl = new HardwareControl();

	private String status = STATUS_CONNECT_ERROR;

	boolean run = true;
	private ReadWriteThread readWriteThread;

	@Inject
	public SerialHardwareComm(BrewPrefs prefs) {
		hardareControl.setMode(HeaterMode.OFF);
		readWriteThread = new ReadWriteThread(this, prefs);
		new Thread(readWriteThread).start();
	}

	@Override
	public HardwareStatus getHardwareStatus() {
		return readWriteThread.getStatus();
	}

	@Override
	public void setHardwareControl(HardwareControl hc) {
		this.hardareControl = hc;
	}

	public HardwareControl getHardareControl() {
		return hardareControl;
	}

	public void setHardareControl(HardwareControl hardareControl) {
		this.hardareControl = hardareControl;
	}

	public String getStatus() {
		return status;
	}

	public void changeStatus(String status) {
		boolean different = !status.equals(this.status);
		if (different) {
			this.status = status;
			fireStateChangeHandlers();
		}
	}

	@Override
	public List<ControlPoint> getControlPoints() {
		// TODO Auto-generated method stub
		return null;
	}

}
