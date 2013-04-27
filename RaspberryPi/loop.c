/*
 Copyright 2009-2013 Michael Graessle
 
 
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

#define _GNU_SOURCE

#include "brewctrl.h"
#include "sensor.h"
#include "duty.h"
#include "pid.h"
#include "comm.h"
#include "control.h"

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <strings.h>
#include <errno.h>
#include <pthread.h>

#define LOOP_FUNCTIONS 4

typedef struct {
	unsigned int delayTime;
	long lastRunTime;
	void (*workFunction)();
} LoopFunction;

Pid pid;

void* loopFunctionPThread(void *ptr) {
	LoopFunction *lf = ptr;

	lf->lastRunTime = millis() - lf->delayTime;

	while (true) {
		int sleepTime = (lf->lastRunTime + lf->delayTime) - millis();

		if (sleepTime > 0) {
			usleep(sleepTime * 1000);
		} else {
			lf->lastRunTime = lf->lastRunTime + lf->delayTime;
			lf->workFunction();
		}
	}
	free(lf);

	return NULL;
}

void startLoopFunction(int delayTime, void (*workFunction)()) {
	LoopFunction *lf = malloc(sizeof(LoopFunction));

	lf->delayTime = delayTime;
	lf->workFunction = workFunction;

	pthread_t thread;
	pthread_create(&thread, NULL, loopFunctionPThread, lf);

}
void test(void) {
	printf("Test %lu\n", millis());
}

void loop(void) {
	setupControl();
	turnOff();
	searchForTempSensors();

	startComm();

	startLoopFunction(1000, updateDuty);
	startLoopFunction(100, updatePinsForSetDuty);
	startLoopFunction(250, updateStepTimer);
	startLoopFunction(10000, searchForTempSensors);
	startLoopFunction(1000, selectReadingSensors);

	while (true) {
		sleep(100000);
	}

}
