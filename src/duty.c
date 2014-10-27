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
#include <unistd.h>

#define GPIO_ROOT SYS_PATH"/class/gpio"

void ioMode(int io, bool inout) {
#ifdef MOCK
	printf("          Pin %d In/Out to %s\n", io, inout ? "In" : "Out");
#else
	char tmp[10];
	char path[PATH_MAX];
	sprintf(path, "%s/export", GPIO_ROOT);

	FILE* f = fopen(path, "wb");
	if (f) {
		sprintf(tmp, "%d", io);
		fwrite(tmp, 1, strlen(tmp), f);
		fclose(f);
	} else {
		ERR("Failed export io %d In/Out to %s\n", io, inout ? "In" : "Out");
	}
	sprintf(path, "%s/gpio%d/direction", GPIO_ROOT, io);
	f = fopen(path, "wb");
	if (f) {
		sprintf(tmp, "%s", inout ? "in" : "out");
		fwrite(tmp, 1, strlen(tmp), f);
		fclose(f);
	} else {
		ERR("Failed to set direction on io %d In/Out to %s\n", io, inout ? "In" : "Out");
	}
#endif
}

void digitalWrite(int io, bool hilow) {

//	if (hilow) {
//		DBG("   %d -> ON\n", io);
//	} else {
//		DBG("   %d -> OFF\n", io);
//	}

#ifdef MOCK
	printf("Pin %d set to %s\n", io, hilow ? "On" : "Off");
#else
	char tmp[10];
	char path[PATH_MAX];
	sprintf(path, "%s/gpio%d/value", GPIO_ROOT, io);
	FILE* f = fopen(path, "wb");
	if (f) {
		sprintf(tmp, "%s", hilow ? "1" : "0");
		fwrite(tmp, 1, strlen(tmp), f);
		fclose(f);
	} else {
		ERR("Failed to set output on io %d to %s\n", io, hilow ? "On" : "Off");
	}
#endif

}

void testOutputs() {
	int gpios[] = { 2, 3, 4, 14, 15, 17, 18, 27, 22, 23, 24, 10, 9, 25, 11, 8, 7 };
	int i;
	int numberOfPins = sizeof(gpios) / sizeof(gpios[0]);
	for (i = 0; i < numberOfPins; i++) {
		ioMode(gpios[i], OUTPUT);
	}

	DBG("INIT\n");
	while (true) {
		for (i = 0; i < numberOfPins; i++) {
			digitalWrite(gpios[i], LOW);
		}
		sleep(1);
		for (i = 0; i < numberOfPins; i++) {
			digitalWrite(gpios[i], HIGH);
		}
		sleep(1);
	}
}

void setupDutyController(DutyController * hs, int io) {
	DBG("setupDutyController %d\n",io);

	ioMode(io, OUTPUT);
	digitalWrite(io, LOW);
	hs->controlIo = io;
	hs->lastUpdateOnOffTimes = millis();
	hs->dutyTimeOn = 0;
	hs->dutyTimeOff = 0;
	hs->duty = 0;
	hs->on = false;
	hs->ioState = false;
}

void resetDutyState(DutyController * hs) {
	//DBG("resetDutyState %d\n",hs->controlIo);
	hs->dutyTimeOn = 0;
	hs->dutyTimeOff = 0;
	hs->lastUpdateOnOffTimes = millis();
}

void updateForPinState(DutyController * hs, bool newHeatPinState) {
	unsigned long now = millis();
	unsigned long timeSinceLast = now - hs->lastUpdateOnOffTimes;
	if (timeSinceLast > 30000) {
		// Over 30 seconds since last change. Dump silly values
		hs->dutyTimeOn = 0;
		hs->dutyTimeOff = 0;
		timeSinceLast = 0;

	}

	if (hs->ioState) {
		hs->dutyTimeOn += (timeSinceLast);
	} else {
		hs->dutyTimeOff += (timeSinceLast);
	}
	hs->lastUpdateOnOffTimes = now;

	newHeatPinState = newHeatPinState & hs->on;
	if (newHeatPinState != hs->ioState) {
		hs->ioState = newHeatPinState;
		digitalWrite(hs->controlIo, hs->ioState ? HIGH : LOW);
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

void updateOfOverAmps(DutyController * hs) {
	updateForPinState(hs, false);
}

void updateHeatForStateAndDuty(DutyController * hs) {
	bool newHeatPinState = false;
	if (hs->on) {
		if (hs->duty == 100) {
			newHeatPinState = true;
		} else if (hs->duty == 0) {
			newHeatPinState = false;
		} else {

			unsigned long timeSinceLast = millis() - hs->lastUpdateOnOffTimes;
			unsigned long timeOn = hs->dutyTimeOn;
			unsigned long timeOff = hs->dutyTimeOff;

			if (hs->ioState) {
				timeOn += (timeSinceLast);
			} else {
				timeOff += (timeSinceLast);
			}

			unsigned long totalTime = timeOn + timeOff;
			double percentOn = ((double) timeOn) / totalTime;
			int percentOnTest = (int) (percentOn * 1000);

			if (percentOnTest >= hs->duty * 10) {
				newHeatPinState = false;
			} else {
				newHeatPinState = true;
			}
			/*
			 if (hs->controlIo == 10) {
			 DBG("     On: %s OnTime: %lu Off Time: %lu totalTime:  %lu  Persent ON  : %f\n",(newHeatPinState?"ON " : "OFF"), timeOn , timeOff , totalTime , percentOn * 100);
			 }
			 */

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

