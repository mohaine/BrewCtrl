#include "control.h"
#include "comm.h"

#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>

pthread_mutex_t stepMutux = PTHREAD_MUTEX_INITIALIZER;

Control control;

int stepCount = 0;
ControlStep controlSteps[MAX_STEP_COUNT];

int getControlStepCount() {
	return stepCount;
}
ControlStep * getControlStep(int index) {
	return &controlSteps[index];
}

void setupControl() {
	control.mode = MODE_OFF;
}

Control* getControl() {
	return &control;
}

void lockSteps() {
	pthread_mutex_lock(&stepMutux);
}
void unlockSteps() {
	pthread_mutex_unlock(&stepMutux);
}

void updateDuty() {
	readSensors();
	Control* control = getControl();
	if (control->mode == MODE_ON) {
		lockSteps();
		if (stepCount > 0) {
			ControlStep * step = &controlSteps[0];
			int controlPointCount = step->controlPointCount;
			for (byte cpIndex = 0; cpIndex < controlPointCount; cpIndex++) {
				ControlPoint *cp = &step->controlPoints[cpIndex];
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
		unlockSteps();
	}
}

void updatePinsForSetDuty() {
	Control* control = getControl();

	if (control->mode == MODE_ON) {
		int currentAmps = 0;
		lockSteps();
		if (stepCount > 0) {
			ControlStep * step = &controlSteps[0];
			int controlPointCount = step->controlPointCount;
			for (byte cpIndex = 0; cpIndex < controlPointCount; cpIndex++) {
				ControlPoint *cp = &step->controlPoints[cpIndex];

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
		unlockSteps();
	}
}

void turnOff(void) {
	Control* control = getControl();

	control->mode = MODE_OFF;
	/*
	 for (int cpIndex = 0; cpIndex < controlPointCount && cpIndex < MAX_CP_COUNT;
	 cpIndex++) {
	 controlPoints[cpIndex].duty = 0;
	 setHeatOn(&controlPoints[cpIndex].dutyController, false);
	 }
	 */
}

