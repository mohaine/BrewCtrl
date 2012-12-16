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

#ifndef SENSOR_H_
#define SENSOR_H_

#include "brewctrl.h"

typedef struct {
	byte address[8];
	double lastTemp;bool bModel;bool reading;
} TempSensor;

void readSensors();
void searchForTempSensors();
TempSensor* getSensor(byte* address);
TempSensor* getSensorByIndex(int i);
int getSensorCount();

#endif

