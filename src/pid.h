/*
 Copyright 2009-2013
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

#ifndef PID_H_
#define PID_H_


#include "pid.h"
#include "brewctrl.h"

typedef struct {
	double Kp;
	double Kd;
	double Ki;
	double maxOutput;
	double minOutput;
	double previousError;
	double integral;
	//unsigned long second;
	byte output;
	byte updateInt;
} Pid;


int getDutyFromAdjuster(Pid * pid, double targetTemp, double currentTemp);
void setupPid(Pid * da);

#endif

