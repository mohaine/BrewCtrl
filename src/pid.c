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

#include "pid.h"

void setupPid(Pid * da) {
//  da->Kp = 21.1263;
//  da->Kd = 1521.1382;
//  da->Ki = 0.2772;
//  da->updateInt = 8;

    da->Kp = 78.2577;
    da->Kd = 269.7192;
    da->Ki = 0.9532;
    //da->updateInt = 1;

    da->minOutput = 0;
    da->maxOutput = 100;
    da->previousError = 0;
    da->integral = 0;
}

void setMaxDuty(Pid * da, int maxDuty) {
    da->maxOutput = maxDuty;
}

int getDutyFromAdjuster(Pid * pid, double targetTemp, double currentTemp) {

//	if (pid->second % pid->updateInt == 0) {

//		float dt = pid->updateInt;
    float dt = 1;

    float error = targetTemp - currentTemp;
    pid->integral = pid->integral + (error * dt * pid->Ki);

    if (pid->integral > pid->maxOutput) {
        pid->integral = pid->maxOutput;
    } else if (pid->integral < pid->minOutput) {
        pid->integral = pid->minOutput;
    }

    float derivative = (error - pid->previousError) / dt;
    float newOutput = (pid->Kp * error) + (pid->integral) + (pid->Kd * derivative);
    pid->previousError = error;

    if (newOutput >= pid->maxOutput) {
        newOutput = pid->maxOutput;
    } else if (newOutput <= pid->minOutput) {
        newOutput = pid->minOutput;
    }
    pid->output = (int) newOutput;
//	}

//	pid->second++;
    return pid->output;

}

