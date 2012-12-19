#ifndef CONTROL_H_
#define CONTROL_H_

#include "brewctrl.h"
#include "pid.h"
#include "duty.h"
#define MAX_CP_COUNT 8

typedef struct {
	bool mode;
	long controlId;byte maxAmps;bool turnOffOnCommLoss;
} Control;

typedef struct {
	byte controlPin;byte duty;byte fullOnAmps;byte tempSensorAddress[8];
	double targetTemp;bool hasDuty;bool automaticControl;
	DutyController dutyController;
	Pid pid;
} ControlPoint;

void turnOff(void);
void setupControl();
Control* getControl();
ControlPoint* getControlPointByIndex(int i);

int getControlPointCount();
void addControlPoint();
void checkForControlTimeout();

#endif
