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

#ifndef CONFIG_H_
#define CONFIG_H_

#include "brewctrl.h"
#include "brewctrl.h"

typedef struct {
	int count;void* data;
} Array;

typedef struct {
	char* address;
} Sensor;

typedef struct {
	char* name;
	char* address;
	char* location;
} SensorConfig;

typedef struct {
	char* name;
	int pin;bool hasDuty;
	int fullOnAmps;
} HeatElement;

typedef struct {
	char* name;
	Sensor* sensor;
	HeatElement* heater;
} Tank;
typedef struct {
	char* name;
	int pin;bool hasDuty;
} Pump;

typedef struct {
	int maxAmps;
	Array tanks;
	Array pumps;
} BreweryLayout;

typedef struct {
	double targetTemp;
	char* targetName;
	char* controlName;
} StepControlPoint;

typedef struct {
	char* name;
	char* time;
	Array controlPoints;
} Step;

typedef struct {
	char* name;
	Array steps;
} StepList;

typedef struct {
	char* version;bool logMessages;
	BreweryLayout * brewLayout;
	Array sensors;
	Array stepLists;
} Configuration;

Configuration * getConfiguration();
void setConfiguration(Configuration * config);
bool initConfiguration();
void writeConfiguration(Configuration * config);

byte* formatJsonConfiguration(Configuration * cfg);
Configuration * parseJsonConfiguration(byte *data);
void changeConfigVersion(Configuration * cfg);
#endif

