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
	int controlPin;
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
