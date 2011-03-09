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


#ifndef PID_H_
#define PID_H_

typedef struct
{
  double Kp;
  double Kd;
  double Ki;
  double maxOutput;
  double minOutput;
  double previousError;
  double integral;


} 
DutyAdjuster;


void setupDutyAdjuster(DutyAdjuster * da){
//Kp: 1745.8436 Kd: 1.0440 Ki: 0.0025 Time: 56:24  overShoot: 0.1028 finalError: 0.0888

  da->Kp = 1745.8436;
  da->Kd = 1.0440;
  da->Ki = 0.0025;


  da->minOutput = 0;
  da->maxOutput = 100;
  da->previousError = 0;
  da->integral = 0;
}


void setMaxDuty(DutyAdjuster * da, int maxDuty) {
  da->maxOutput = maxDuty;
}



int getDutyFromAdjuster(DutyAdjuster * pid, double targetTemp, double currentTemp) {

  double dt = 1;

  double error = targetTemp - currentTemp;
  pid->integral = pid->integral + (error * dt);
  double derivative = (error - pid->previousError) / dt;
  double output = (pid->Kp * error) + (pid->Ki * pid->integral) + (pid->Kd * derivative);
  pid->previousError = error;

  if (output >= pid->maxOutput) {
    output = pid->maxOutput;
  } 
  else if (output <= pid->minOutput) {
    output = pid->minOutput;
  }

  return (int) output;

} 



#endif

