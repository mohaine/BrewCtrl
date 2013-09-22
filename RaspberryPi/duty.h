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



#ifndef DUTY_H_
#define DUTY_H_


#include "brewctrl.h"

typedef struct
{

  unsigned long dutyOnOffLastChange;
  unsigned long dutyLastCheckTime;  
  unsigned long timeOn;  
  unsigned long timeOff;  
  int duty;
  bool on;
  bool ioState;
  byte controlIo;
} 
DutyController;


void setHeatOn(DutyController * hs, bool newState);
void updateOfOverAmps(DutyController * hs);
void updateHeatForStateAndDuty(DutyController * hs);
void updateForPinState(DutyController * hs, bool newHeatPinState);
void setHeatDuty(DutyController * hs, int duty);
void resetDutyState(DutyController * hs);
void setupDutyController(DutyController * hs, int io);

#endif








