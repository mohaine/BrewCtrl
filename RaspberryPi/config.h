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

#ifndef CONFIG_H_
#define CONFIG_H_

#include "brewctrl.h"
#include "brewctrl.h"

typedef struct {
} Tank;
typedef struct {
} Pump;
typedef struct {
	int maxAmps;
} BreweryLayout;

typedef struct {
} SensorConfiguration;

typedef struct {
	char* version;bool logMessages;
	BreweryLayout * brewLayout;
	SensorConfiguration * sensors;

} Configuration;

Configuration * getConfiguration();
void setConfiguration(Configuration * config);
bool initConfiguration();

char* formatJsonConfiguration(Configuration * cfg);
Configuration * parseJsonConfiguration(byte *data);
#endif

