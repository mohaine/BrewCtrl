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

#include "duty.h"
#include "logger.h"
#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>
#include <linux/limits.h>
#include <limits.h>

#define GPIO_ROOT SYS_PATH"/class/gpio"

void pinMode(int pin, bool inout) {
#ifdef MOCK
	printf("          Pin %d In/Out to %s\n", pin, inout ? "In" : "Out");
#else
	char tmp[10];
	char path[PATH_MAX];
	sprintf(path, "%s/export", GPIO_ROOT);

	FILE* f = fopen(path, "wb");
	if (f) {
		sprintf(tmp, "%d", pin);
		fwrite(tmp, 1, strlen(tmp), f);
		fclose(f);
	} else {
		fprintf(stderr, "Failed export pin %d In/Out to %s\n", pin, inout ? "In" : "Out");
	}
	sprintf(path, "%s/gpio%d/direction", GPIO_ROOT, pin);
	f = fopen(path, "wb");
	if (f) {
		sprintf(tmp, "%s", inout ? "in" : "out");
		fwrite(tmp, 1, strlen(tmp), f);
		fclose(f);
	} else {
		fprintf(stderr, "Failed to set direction on pin %d In/Out to %s\n", pin, inout ? "In" : "Out");
	}
#endif
}

void digitalWrite(int pin, bool hilow) {

	if (hilow) {
		//DBG("   %d -> ON\n", pin);
	} else {
		//DBG("   %d -> OFF\n", pin);
	}

#ifdef MOCK
//	printf("Pin %d set to %s\n", pin, hilow ? "On" : "Off");

#else
	char tmp[10];
	char path[PATH_MAX];
	sprintf(path, "%s/gpio%d/value", GPIO_ROOT, pin);
	FILE* f = fopen(path, "wb");
	if (f) {
		sprintf(tmp, "%s", hilow ? "1" : "0");
		fwrite(tmp, 1, strlen(tmp), f);
		fclose(f);
	} else {
		fprintf(stderr, "Failed to set output on pin %d to %s\n", pin, hilow ? "On" : "Off");
	}
#endif

}

void setupDutyController(DutyController * hs, int pin) {
	DBG("setupDutyController %d\n",pin);

	pinMode(pin, OUTPUT);
	digitalWrite(pin, LOW);
	hs->controlPin = pin;
	hs->dutyLastCheckTime = 0;
	hs->timeOn = 0;
	hs->timeOff = 0;
	hs->duty = 0;
	hs->on = false;
	hs->pinState = false;
}

void resetDutyState(DutyController * hs) {
	hs->timeOn = 0;
	hs->timeOff = 0;
	hs->dutyLastCheckTime = millis();
	hs->dutyOnOffLastChange = hs->dutyLastCheckTime;
}

void updateForPinState(DutyController * hs, bool newHeatPinState) {

	newHeatPinState = newHeatPinState & hs->on;
	if (newHeatPinState != hs->pinState) {
		hs->dutyOnOffLastChange = millis();
		hs->pinState = newHeatPinState;
		digitalWrite(hs->controlPin, hs->pinState ? HIGH : LOW);
	}
}

void setHeatOn(DutyController * hs, bool newState) {
	if (hs->on != newState) {
		hs->on = newState;
		if (newState) {
			resetDutyState(hs);
		} else {
			updateForPinState(hs, false);
		}
	}
}

void updateHeatForStateAndDuty(DutyController * hs) {
	unsigned long now = millis();
	bool newHeatPinState = false;

//	DBG("                 ON  : %d\n", hs->on);

	if (hs->on) {
//		DBG("                    hs->pinState  : %d\n", hs->pinState);
		if (hs->pinState) {
			hs->timeOn += (now - hs->dutyLastCheckTime);
		} else {
			hs->timeOff += (now - hs->dutyLastCheckTime);
		}
		hs->dutyLastCheckTime = now;

		if (hs->duty == 100) {
			newHeatPinState = true;
		} else if (hs->duty == 0) {
			newHeatPinState = false;
		} else {
			int percentOn = ((double) hs->timeOn / (hs->timeOn + hs->timeOff)) * 1000;
			if (percentOn >= hs->duty * 10) {
				newHeatPinState = false;
			} else {
				newHeatPinState = true;
			}
		}
	} else {
		newHeatPinState = false;
	}

	updateForPinState(hs, newHeatPinState);

}

void setHeatDuty(DutyController * hs, int duty) {

	if (duty < 0) {
		duty = 0;
	}

	if (duty != hs->duty) {
		hs->duty = duty;
		resetDutyState(hs);
	}

}

