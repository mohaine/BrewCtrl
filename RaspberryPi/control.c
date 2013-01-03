#include "control.h"
#include "comm.h"

#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <string.h>
#include <strings.h>

long lastOnTime = 0;

pthread_mutex_t stepMutux = PTHREAD_MUTEX_INITIALIZER;

Control control;

int stepCount = 0;
ControlStep controlSteps[MAX_STEP_COUNT];

void setControlStepCount(int count) {
	stepCount = count;
}

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

void setupControlPoint(ControlPoint *cp) {
	if (!cp->initComplete) {

		if (cp->automaticControl) {
			cp->duty = 0;
			setupPid(&cp->pid);
		}

		if (cp->hasDuty) {
			setupDutyController(&cp->dutyController, cp->controlPin);
		}
		cp->initComplete = true;
	}
}

void updateStepTimer() {
	Control* control = getControl();
	lockSteps();

	if (stepCount > 0) {
		ControlStep * step = &controlSteps[0];

		if (control->mode == MODE_ON) {

			if (!step->active) {
				lastOnTime = 0;
				step->active = true;
			}
			int stepTime = step->stepTime;
			if (stepTime > 0) {
				long now = millis();
				long onTime = 0;
				if (lastOnTime > 0) {
					onTime = now - lastOnTime;
					while (onTime > 1000) {
						int newStepTime = stepTime - 1;
						onTime -= 1000;
						if (newStepTime <= 0) {
							stepCount--;

							if (stepCount == 0) {
								turnOff();
							} else {
								for (int i = 0; i < stepCount; i++) {
									memcpy(&controlSteps[i], &controlSteps[i + 1], sizeof(ControlStep));
									controlSteps[i].active = false;
								}
							}
							break;
						} else {
							step->stepTime = newStepTime;
						}
					}

				}
				// Put extra back into lastOnTime;
				lastOnTime = now - onTime;
			}

		} else if (control->mode == MODE_HOLD) {
			lastOnTime = 0;
			step->active = true;

		} else if (control->mode == MODE_OFF) {
			lastOnTime = 0;
			step->active = false;
		}

	}
	unlockSteps();
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
				setupControlPoint(cp);
				if (cp->automaticControl) {
					TempSensor* sensor = getSensor(cp->tempSensorAddress);
					if (sensor != NULL) {
						if (sensor->reading) {
							if (cp->hasDuty) {
								cp->duty = getDutyFromAdjuster(&cp->pid, cp->targetTemp, sensor->lastTemp);
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
				setupControlPoint(cp);
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
