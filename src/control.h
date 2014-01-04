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

#ifndef CONTROL_H_
#define CONTROL_H_

#include "brewctrl.h"
#include "pid.h"
#include "duty.h"
#include "sensor.h"
#define MAX_CP_COUNT 8
#define MAX_STEP_COUNT 20
#define STEP_FIELD_LENGTH 100

#define MODE_OFF 0
#define MODE_ON 1
#define MODE_HOLD 2
#define MODE_HEAT_OFF 3

typedef struct {
	int mode;
} Control;

typedef struct {
	bool initComplete;
	int controlIo;
	int duty;
	int fullOnAmps;
	char tempSensorAddressPtr[17];
	double targetTemp;bool hasDuty;bool automaticControl;
	DutyController dutyController;
	Pid pid;
} ControlPoint;

typedef struct {
	char id[STEP_FIELD_LENGTH];
	char name[STEP_FIELD_LENGTH];
	int stepTime;bool active;
	ControlPoint controlPoints[MAX_CP_COUNT];
	int controlPointCount;
} ControlStep;

void turnOff();
void turnHeatOff();


void setupControl();
Control* getControl();

int getControlStepCount();
void setControlStepCount(int count);
ControlStep * getControlStep(int index);

void setupControlPoint(ControlPoint *cp);

void updateDuty();
void updatePinsForSetDuty();

void lockSteps();
void unlockSteps();
void updateStepTimer();
void selectReadingSensors();

#endif
