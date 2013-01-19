#include "control.h"
#include "comm.h"
#include "config.h"
#include "logger.h"

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
		setupDutyController(&cp->dutyController, cp->controlPin);
		cp->initComplete = true;
	}
}

void selectReadingSensors() {
	Configuration * cfg = getConfiguration();

	if (cfg != NULL) {
		// Make sure all found sensors have a SensorConfig entry
		for (int tsI = 0; tsI < getSensorCount(); tsI++) {
			TempSensor * s = getSensorByIndex(tsI);

			bool found = false;
			for (int tsI = 0; tsI < getSensorCount(); tsI++) {
				if (cfg->sensors.data != NULL && cfg->sensors.count > 0) {
					SensorConfig * sA = (SensorConfig *) cfg->sensors.data;
					for (int scI = 0; scI < cfg->sensors.count; scI++) {
						SensorConfig * sc = &sA[scI];
						if (strcmp(sc->address, s->addressPtr) == 0) {
							found = true;
							break;
						}
					}
				}
			}

			if (!found) {
				int count = cfg->sensors.count;
				SensorConfig * oldData = (SensorConfig *) cfg->sensors.data;
				SensorConfig * data = (SensorConfig *) malloc(sizeof(SensorConfig) * (count + 1));
				memcpy(data, oldData, sizeof(SensorConfig) * count);

				SensorConfig * sc = &data[count];

				int addressLength = strlen(s->addressPtr) + 1;
				char * addressCopy = malloc(addressLength);
				if (addressCopy == NULL) {
					ERR("Malloc Failed\n");
					exit(-1);
				}
				memcpy(addressCopy, s->addressPtr, addressLength);

				sc->name = NULL;
				sc->location = NULL;
				sc->address = addressCopy;

				cfg->sensors.data = (void*) data;
				cfg->sensors.count = count + 1;

				free(oldData);

				changeConfigVersion(cfg);
				writeConfiguration(cfg);

			}
		}
	}

// Update Tank selected Sensors
	if (cfg != NULL && cfg->sensors.data != NULL && cfg->sensors.count > 0) {

		BreweryLayout * bl = cfg->brewLayout;
		if (bl != NULL && bl->tanks.data != NULL) {
			Tank * tA = (Tank *) bl->tanks.data;
			for (int tankIndex = 0; tankIndex < bl->tanks.count; tankIndex++) {
				Tank * t = &tA[tankIndex];
				if (t->sensor != NULL) {
					bool found = false;

					if (t->sensor->address != NULL) {
						TempSensor * s = getSensorByAddress(t->sensor->address);
						found = s != NULL && s->reading;
					}

					if (!found) {
//						DBG("Missing Sensor for %s\n", t->name);
						SensorConfig * sA = (SensorConfig *) cfg->sensors.data;
						for (int scI = 0; scI < cfg->sensors.count; scI++) {
							SensorConfig * sc = &sA[scI];
							if (strcmp(sc->location, t->name) == 0) {
								TempSensor * s = getSensorByAddress(sc->address);
								if (s != NULL && s->reading) {
									char * newAddressString = malloc(17);
									sprintf(newAddressString, "%s", sc->address);

									char * oldAddress = t->sensor->address;

									t->sensor->address = newAddressString;
									changeConfigVersion(cfg);

									DBG("Changed tank %s sensor to %s\n", t->name, t->sensor->address);

									if (oldAddress != NULL) {
										free(oldAddress);
									}

									writeConfiguration(cfg);
								}
							}
						}
					}
				}
			}
		}
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
	if (stepCount == 0) {
		DBG("Create new Manual Step\n");
		ControlStep * step = &controlSteps[0];
		step->stepTime = 0;
		step->active = false;
		sprintf(step->name, "Manual Step");

		char * stepId = generateRandomId();
		sprintf(step->id, "%s", stepId);
		free(stepId);

		step->controlPointCount = 0;

		Configuration * cfg = getConfiguration();
		if (cfg != NULL) {

			BreweryLayout * bl = cfg->brewLayout;
			if (bl != NULL && bl->tanks.data != NULL) {
				Tank * tA = (Tank *) bl->tanks.data;
				for (int tankIndex = 0; tankIndex < bl->tanks.count; tankIndex++) {
					Tank * t = &tA[tankIndex];
					if (t->heater != NULL) {
						ControlPoint * cp = &step->controlPoints[step->controlPointCount];
						cp->controlPin = t->heater->pin;
						cp->automaticControl = false;
						cp->duty = 0;
						cp->fullOnAmps = t->heater->fullOnAmps;
						cp->hasDuty = t->heater->hasDuty;
						cp->initComplete = false;
						setupControlPoint(cp);
						step->controlPointCount++;
					}
				}

				Pump * pA = (Pump *) bl->pumps.data;
				for (int pumpIndex = 0; pumpIndex < bl->pumps.count; pumpIndex++) {
					Pump * p = &pA[pumpIndex];
					ControlPoint * cp = &step->controlPoints[step->controlPointCount];
					cp->controlPin = p->pin;
					cp->automaticControl = false;
					cp->duty = 0;
					cp->fullOnAmps = 0;
					cp->hasDuty = p->hasDuty;
					cp->initComplete = false;
					setupControlPoint(cp);
					step->controlPointCount++;
				}
			}
			stepCount = 1;
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
			for (int cpIndex = 0; cpIndex < controlPointCount; cpIndex++) {
				ControlPoint *cp = &step->controlPoints[cpIndex];
				cp->dutyController.on = true;
				if (cp->automaticControl) {
					TempSensor* sensor = getSensorByAddress(cp->tempSensorAddressPtr);
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
//	DBG("updatePinsForSetDuty() Mode : %d\n",control->mode);

	if (control->mode == MODE_ON || control->mode == MODE_HOLD) {

		int currentAmps = 0;
		lockSteps();
		if (stepCount > 0) {
			ControlStep * step = &controlSteps[0];

//			DBG("       step: %s\n", step->name);

			int controlPointCount = step->controlPointCount;
			for (int cpIndex = 0; cpIndex < controlPointCount; cpIndex++) {
				ControlPoint *cp = &step->controlPoints[cpIndex];
				setupControlPoint(cp);

				int duty = cp->duty;

				int maxAmps = 0;

				Configuration* configuration = getConfiguration();
				if (configuration != NULL && configuration->brewLayout != NULL) {
					maxAmps = configuration->brewLayout->maxAmps;
				}

				if (currentAmps + cp->fullOnAmps > maxAmps) {
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

	lockSteps();
	for (int csIndex = 0; csIndex < getControlStepCount() && csIndex < MAX_STEP_COUNT; csIndex++) {
		ControlStep * cs = getControlStep(csIndex);
		for (int cpIndex = 0; cpIndex < cs->controlPointCount && cpIndex < MAX_CP_COUNT; cpIndex++) {
			cs->controlPoints[cpIndex].duty = 0;
			setHeatOn(&cs->controlPoints[cpIndex].dutyController, false);
		}
	}
	unlockSteps();

}

