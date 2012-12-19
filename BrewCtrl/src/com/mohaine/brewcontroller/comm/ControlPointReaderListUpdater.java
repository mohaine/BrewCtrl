package com.mohaine.brewcontroller.comm;

import java.util.List;

import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.comm.msg.ControlPointReaderWriter;

public class ControlPointReaderListUpdater implements ReadListener<ControlPointReaderWriter> {
	List<ControlPoint> controlPoints;

	public ControlPointReaderListUpdater(List<ControlPoint> controlPoints) {
		super();
		this.controlPoints = controlPoints;
	}

	@Override
	public void onRead(ControlPointReaderWriter r) {
		ControlPoint readPoint = r.getControlPoint();
		ControlPoint changedPoint = null;

		synchronized (controlPoints) {
			for (ControlPoint controlPoint : controlPoints) {
				if (readPoint.getControlPin() == controlPoint.getControlPin()) {
					changedPoint = controlPoint;
					break;
				}
			}

			if (changedPoint == null) {
				changedPoint = new ControlPoint();
				changedPoint.setControlPin(readPoint.getControlPin());
				controlPoints.add(changedPoint);
			}
		}

		synchronized (changedPoint) {
			changedPoint.setAutomaticControl(readPoint.isAutomaticControl());
			changedPoint.setDuty(readPoint.getDuty());
			changedPoint.setHasDuty(readPoint.isHasDuty());
			changedPoint.setTargetTemp(readPoint.getTargetTemp());
			changedPoint.setTempSensorAddress(readPoint.getTempSensorAddress());
		}
	}
}