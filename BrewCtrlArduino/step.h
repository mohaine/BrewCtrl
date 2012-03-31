/*
    Copyright 2009-2012 Michael Graessle

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


#ifndef STEP_H_
#define STEP_H_

#include "sensor.h"

struct Control {
  bool mode;
  int controlId;
  bool mashOn;
  bool pumpOn;
  double hltTargetTemp;
  double tunTargetTemp;
};
Control control;

struct ControlPoint {
  byte controlPin;
  byte duty;
  byte tempSensorAddress[8];
  double targetTemp;
  bool hasDuty;
  bool automaticControl;
};
ControlPoint controlPoints[8];
byte controlPointCount = 0;


#endif
