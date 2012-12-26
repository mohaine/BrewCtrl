#include "control.h"
#include "comm.h"

#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <string.h>
#include <strings.h>
long stepMillisOffset = 0;

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

// get millis since startup. Has a nicer size
int getStepMillis() {
	if (stepMillisOffset <= 0) {
		stepMillisOffset = millis() - 300000;
	}
	return (int) (millis() - stepMillisOffset);
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

int getTotalCompletedControlStepTime(ControlStep * step) {
	int total = step->extraCompletedTime;
	if (step->lastStartTime > 0) {
		total += (getStepMillis() - step->lastStartTime);
	}
	return total;
}

void stopControlStepTimer(ControlStep * step) {
	if (step->lastStartTime > 0) {
		step->extraCompletedTime += (getStepMillis() - step->lastStartTime);
	}
	step->lastStartTime = 0;
}
void startControlStepTimer(ControlStep * step) {
	int now = getStepMillis();
	if (step->lastStartTime > 0) {
		stopControlStepTimer(step);
	}
	step->lastStartTime = now;
}

bool isControlStepComplete(ControlStep * step) {
	bool complete = step->stepTime > 0 && getTotalCompletedControlStepTime(step) >= step->stepTime;
	return complete;
}
bool isControlStepStarted(ControlStep * step) {
	return step->lastStartTime > 0;
}
void updateStepTimer() {
	Control* control = getControl();
	lockSteps();

	if (stepCount > 0) {
		ControlStep * step = &controlSteps[0];

		if (control->mode == MODE_ON) {

			if (!isControlStepStarted(step)) {

				printf("Start %s, LST %d ECT %d Time %d\n", step->name, step->lastStartTime, step->extraCompletedTime, step->stepTime);

				startControlStepTimer(step);
			} else {

				printf("RUN %s, LST %d ECT %d Time %d\n", step->name, step->lastStartTime, step->extraCompletedTime, step->stepTime);

				if (isControlStepComplete(step)) {
					printf("Complete %s\n", step->name);
					stepCount--;
					for (int i = 0; i < stepCount; i++) {
						printf("  Copy %s over %s\n", controlSteps[i + 1].name, controlSteps[i].name);
						memcpy(&controlSteps[i], &controlSteps[i + 1], sizeof(ControlStep));
					}
				}
			}

		} else if (control->mode == MODE_OFF || control->mode == MODE_HOLD) {
			stopControlStepTimer(step);
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

