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

package com.mohaine.brewcontroller.comm;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.HardwareBase;
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HeaterMode;

public class SerialHardwareComm extends HardwareBase implements Hardware {

	public static final String STATUS_CONNECT_NO_PORT = "NO TTY";
	public static final String STATUS_CONNECT_ERROR = "NO CONNECT";
	public static final String STATUS_NO_COMM_WRITE = "NO WRITE";
	public static final String STATUS_CONTROL_ID = "CONTROL ID";
	public static final String STATUS_NO_COMM_READ = "NO READ";
	public static final String STATUS_COMM_GOOD = "Ok";

	static final int MAX_CONTROL_ID = 999;

	private HardwareControl hardareControl = new HardwareControl();

	private String status = STATUS_CONNECT_ERROR;

	boolean run = true;
	private ReadWriteThread readWriteThread;

	@Inject
	public SerialHardwareComm(SerialConnection serialConn, ConfigurationLoader configurationLoader) {
		hardareControl.setMode(HeaterMode.OFF);
		readWriteThread = new ReadWriteThread(this, serialConn, configurationLoader.getConfiguration().isLogMessages());
		new Thread(readWriteThread).start();
	}

	@Override
	public HardwareControl getHardwareStatus() {
		return readWriteThread.getHardwareStatus();
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

	public boolean isRun() {
		return run;
	}

}
