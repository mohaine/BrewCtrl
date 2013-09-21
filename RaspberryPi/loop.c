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


#include "brewctrl.h"
#include "sensor.h"
#include "duty.h"
#include "pid.h"
#include "comm.h"
#include "control.h"
#include "logger.h"

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <strings.h>
#include <errno.h>
#include <pthread.h>

typedef struct {
    unsigned int delayTime;
    long lastRunTime;
    bool print;
    void (*workFunction)();
} LoopFunction;

Pid pid;

void* loopFunctionPThread(void *ptr) {
    LoopFunction *lf = ptr;

    lf->lastRunTime = millis() - lf->delayTime;

    while (true) {

        long now = millis();

        if (now < lf->lastRunTime) {
            lf->lastRunTime = now;
        }

        long nextTime = lf->lastRunTime + lf->delayTime;
        int sleepTime = (nextTime) - now;

        if (sleepTime > 0) {
            if (lf->print) {
                DBG("Sleep %d\n", sleepTime);
            }
            usleep(sleepTime * 1000);
        } else {
            if (-sleepTime > lf->delayTime) {
                //		DBG("Thread too slow %d\n", sleepTime);
                nextTime = now - lf->delayTime;
            }
            lf->lastRunTime = nextTime;
            lf->workFunction();
        }
    }
    free(lf);

    return NULL;
}

LoopFunction* startLoopFunction(int delayTime, void (*workFunction)(), bool print) {
    LoopFunction *lf = malloc(sizeof(LoopFunction));
    lf->print = print;
    lf->delayTime = delayTime;
    lf->workFunction = workFunction;

    pthread_t thread;
    pthread_create(&thread, NULL, loopFunctionPThread, lf);

    return lf;

}

void loop(void) {
    setupControl();
    turnOff();

    startLoopFunction(30000, searchForTempSensors, false);

    startLoopFunction(1000, updateDuty, false);
    startLoopFunction(100, updatePinsForSetDuty, false);
    startLoopFunction(250, updateStepTimer, false);
    startLoopFunction(1000, readSensors, false);
    startLoopFunction(1000, selectReadingSensors, false);

    while (true) {
        sleep(100000);
    }

}
