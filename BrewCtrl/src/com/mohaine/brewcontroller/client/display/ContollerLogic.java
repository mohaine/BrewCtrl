package com.mohaine.brewcontroller.client.display;

import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.layout.BrewHardwareControl;
import com.mohaine.brewcontroller.client.layout.BreweryComponent;
import com.mohaine.brewcontroller.client.layout.Sensor;

public class ContollerLogic {

	public static boolean canModify(ControllerHardware controller, BreweryComponent control) {
		ControlStep selectedStep = controller.getSelectedStep();
		if (selectedStep != null) {

			if (control instanceof BrewHardwareControl) {
				ControlPoint controlPoint = selectedStep.getControlPointForPin(((BrewHardwareControl) control).getIo());
				if (controlPoint != null && !controlPoint.isAutomaticControl()) {
					return true;
				}
			} else if (control instanceof Sensor) {
				ControlPoint controlPoint = selectedStep.getControlPointForAddress(((Sensor) control).getAddress());
				if (controlPoint != null && controlPoint.isAutomaticControl()) {
					return true;
				}
			}

		}
		return false;
	}
}
