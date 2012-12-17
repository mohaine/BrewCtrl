#include "control.h"
#include "comm.h"

Control control;
int controlPointCount = 0;

ControlPoint controlPoints[MAX_CP_COUNT];

void setupControl(){
	control.controlId = 0;

}

Control* getControl() {
	return &control;
}

ControlPoint* getControlPointByIndex(int i) {
	return &controlPoints[i];
}

int getControlPointCount() {
	return controlPointCount;
}

void addControlPoint() {
	controlPointCount++;
}

void turnOff(void) {
	control.mode = MODE_OFF;
	for (int cpIndex = 0; cpIndex < controlPointCount && cpIndex < MAX_CP_COUNT;
			cpIndex++) {
		controlPoints[cpIndex].duty = 0;
		setHeatOn(&controlPoints[cpIndex].dutyController, false);
	}
}

void checkForControlTimeout() {
	if (control.turnOffOnCommLoss) {
		if (millis() - lastControlIdTime() > 10000) {
			turnOff();
		}
	}
}
