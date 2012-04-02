/*
    Copyright 2009-2012
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
  unsigned long second;
  byte output;
  byte updateInt;
} 
Pid;


void setupPid(Pid * da){
  // Old Tune
  // Kp: 10.5715 Kd: 299.8764 Ki: 0.0020 Time: 07:01 overShoot: 0.4032 finalError: 0.0005
  // Tune with random read errors and 30 point history
  // double kp = 5.9279, kd = 156.6051, ki = 0.0015;
  // Tune with random read errors and 10 point history, Random target/abiemt
  //  kp=4.5904, kd= 102.3394, ki=  0.0025 
  //  kp=3.3242, kd= 5.9397, ki=  0.0026   

  da->Kp = 21.1263;
  da->Kd = 1521.1382;
  da->Ki = 0.2772;
  da->updateInt = 8;

  da->minOutput = 0;
  da->maxOutput = 100;
  da->previousError = 0;
  da->integral = 0;
}


void setMaxDuty(Pid * da, int maxDuty) {
  da->maxOutput = maxDuty;
}



int getDutyFromAdjuster(Pid * pid, double targetTemp, double currentTemp) {

  if (pid->second % pid->updateInt == 0) {

    float dt = pid->updateInt;

    float error = targetTemp - currentTemp;
    pid->integral = pid->integral + (error * dt * pid->Ki);

    if (pid->integral > pid->maxOutput) {
      pid->integral = pid->maxOutput;
    } 
    else if (pid->integral < pid->minOutput) {
      pid->integral = pid->minOutput;
    }

    float derivative = (error - pid->previousError) / dt;
    float newOutput = (pid->Kp * error) + (pid->integral) + (pid->Kd * derivative);
    pid->previousError = error;

    if (newOutput >= pid->maxOutput) {
      newOutput = pid->maxOutput;
    } 
    else if (newOutput <= pid->minOutput) {
      newOutput = pid->minOutput;
    }
    pid->output = (int) newOutput;
  }

  pid->second++;
  return pid->output;

} 



#endif







