/*
 Copyright 2009-2012 Michael Graessle
 
 
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
/*  OLD
 #define HLT_HEAT_CONTROL_PIN 9
 #define PUMP_CONTROL_PIN 8
 #define BOIL_HEAT_CONTROL_PIN 7
 */

#include "brewctrl.h"
#include "sensor.h"
#include "duty.h"
#include "pid.h"
#include "comm.h"
#include "control.h"

#define HEAT_DELAY 100
#define DUTY_DELAY 1000
#define SEARCH_DELAY 10000

unsigned long lastHeatUpdate = 0;
unsigned long lastDutyUpdate = 0;
unsigned long lastPumpOnChangeTime = 0;
unsigned long lastSearchTime = 0;

Pid pid;

void setup(void) {

	setupControl();
	turnOff();
	setupComm();

	searchForTempSensors();
}

void updateControlPointState();

void loop(void) {

	//  Serial.println("LOOP");
	unsigned long now = millis();

	Control* control = getControl();

	if (control->turnOffOnCommLoss) {
		if (now - lastControlIdTime() > 10000) {
			turnOff();
		}
	}

	// Update automaticly controlled duty/on/off states
	if (lastDutyUpdate > now) {
		lastDutyUpdate = 0;
	}
	if (now - lastDutyUpdate > DUTY_DELAY) {
		lastDutyUpdate = lastDutyUpdate + DUTY_DELAY;
		readSensors();
		updateControlPointState();
	}

	// Update pin so we acheive the correct on/off ratio for our selected duty
	if (lastHeatUpdate > now) {
		lastHeatUpdate = 0;
	}
	if (now - lastHeatUpdate > HEAT_DELAY) {
		lastHeatUpdate = lastHeatUpdate + HEAT_DELAY;

		if (control->mode == MODE_ON) {
			int currentAmps = 0;

			int controlPointCount = getControlPointCount();
			for (byte cpIndex = 0; cpIndex < controlPointCount; cpIndex++) {
				ControlPoint *cp = getControlPointByIndex(cpIndex);

				/*
				 Serial.print(cp->controlPin);
				 Serial.print(" has duty: ");
				 Serial.print(cp->hasDuty? "true":"false");
				 Serial.println();
				 */

				int duty = cp->duty;
				if (currentAmps + cp->fullOnAmps > control->maxAmps) {
					duty = 0;
				}

				if (cp->hasDuty) {
					setHeatDuty(&cp->dutyController, duty);
					updateHeatForStateAndDuty(&cp->dutyController);
				} else {
					updateForPinState(&cp->dutyController, duty > 0);
				}
				if (cp->dutyController.pinState) {
					currentAmps += cp->fullOnAmps;
				}

				/*
				 Serial.print(" Full On: " );
				 Serial.print(cp->fullOnAmps);
				 Serial.print(" currentAmps: " );
				 Serial.print(currentAmps);
				 Serial.println();
				 */
			}
		}
	}

	readSerial();

	// Search for new sensors
	if (lastSearchTime > now) {
		lastSearchTime = 0;
	}
	if (now - lastSearchTime > SEARCH_DELAY) {
		lastSearchTime = lastSearchTime + SEARCH_DELAY;
		searchForTempSensors();
	}

}

byte getHexValue(char iValue) {
	if (iValue >= '0' && iValue <= '9') {
		return (iValue - '0');
	}
	if (iValue >= 'A' && iValue <= 'F') {
		return (iValue - 'A' + 10);
	}
	if (iValue >= 'a' && iValue <= 'f') {
		return (iValue - 'a' + 10);
	}
	return 0;
}

void updateControlPointState() {
	Control* control = getControl();
	if (control->mode == MODE_ON) {
		int controlPointCount = getControlPointCount();
		for (byte cpIndex = 0; cpIndex < controlPointCount; cpIndex++) {
			ControlPoint *cp = getControlPointByIndex(cpIndex);
			if (cp->automaticControl) {
				TempSensor* sensor = getSensor(cp->tempSensorAddress);
				if (sensor != NULL) {
					if (sensor->reading) {
						if (cp->hasDuty) {
							cp->duty = getDutyFromAdjuster(&cp->pid,
									cp->targetTemp, sensor->lastTemp);
						} else {
							//TODO MIN TOGGLE TIME!!!!
							if (sensor->lastTemp < cp->targetTemp) {
								cp->duty = 100;
							} else {
								cp->duty = 0;
							}
						}
					} else {
						//Serial.println("Sensor not reading");
						cp->duty = 0;
					}
				} else {
					//Serial.println("Failed to find sensor");
				}
			}
		}
	}
}

