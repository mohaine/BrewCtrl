/*
 Copyright 2009-2011 Michael Graessle
 
 
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
#include <stdio.h>

void pinMode(int pin, bool inout) {
	printf("IMPLMENT pinMode\n");
}
void digitalWrite(int pin, bool hilow) {
	printf("IMPLMENT digitalWrite\n");
}

void setupDutyController(DutyController * hs, byte pin) {
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
	if (hs->on) {
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
			int percentOn = ((double) hs->timeOn / (hs->timeOn + hs->timeOff))
					* 1000;
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

