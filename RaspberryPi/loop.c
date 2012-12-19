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

#define LOOP_FUNCTIONS 4

typedef struct {
	unsigned int delayTime;
	long lastRunTime;
	void (*workFunction)();
} LoopFunction;

LoopFunction loopFunctions[LOOP_FUNCTIONS];

long lastHeatUpdate = 0;
long lastDutyUpdate = 0;
long lastPumpOnChangeTime = 0;
long lastSearchTime = 0;

Pid pid;

void updateDuty() {
	printf("update duty \n");

	readSensors();
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

void updatePinsForSetDuty() {
	Control* control = getControl();

	if (control->mode == MODE_ON) {
		int currentAmps = 0;

		int controlPointCount = getControlPointCount();
		for (byte cpIndex = 0; cpIndex < controlPointCount; cpIndex++) {
			ControlPoint *cp = getControlPointByIndex(cpIndex);

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

		}
	}

}

void setupLoop(void) {
	setupControl();
	turnOff();
	searchForTempSensors();
	listSensors();

	loopFunctions[0].delayTime = 1000;
	loopFunctions[0].workFunction = checkForControlTimeout;

	loopFunctions[1].delayTime = 1000;
	loopFunctions[1].workFunction = updateDuty;

	loopFunctions[2].delayTime = 100;
	loopFunctions[2].workFunction = updatePinsForSetDuty;

	loopFunctions[3].delayTime = 10000;
	loopFunctions[3].workFunction = searchForTempSensors;

}

void loop(void) {

	setupLoop();

	while (1) {

		//  Serial.println("LOOP");
		unsigned long now = millis();

		for (int i = 0; i < LOOP_FUNCTIONS; i++) {
			LoopFunction* lf = &loopFunctions[i];
			if (now - lf->lastRunTime > lf->delayTime) {
				lf->lastRunTime = lf->lastRunTime + lf->delayTime;
				lf->workFunction();
			}
		}

		// Caculate time
		usleep(100);
	}
}
