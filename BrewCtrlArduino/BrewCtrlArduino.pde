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

#define HLT_HEAT_CONTROL_PIN 9
#define PUMP_CONTROL_PIN 8
#define BOIL_HEAT_CONTROL_PIN 7 


#define ONE_WIRE_PIN 3

#include <OneWire.h>


#define MODE_OFF 0
#define MODE_ON 1

struct Control {
  bool pumpOn;
  bool mode;
  bool mashOn;
  int controlId;
  double hltTargetTemp;
  double tunTargetTemp;
};
Control control;



#include <avr/pgmspace.h>

#include "sensor.h"
#include "duty.h"
#include "pid.h"


TempSensor *hltSensor = NULL;
TempSensor *tunSensor = NULL;
DutyController hltDutyController;
DutyController boilDutyController;


void turnOff(void);
unsigned long lastControlIdTime = 0;   


#include "comm.h"


#define HEAT_DELAY 100
#define DUTY_DELAY 1000
#define SEARCH_DELAY 10000




unsigned long lastHeatUpdate = 0; 
unsigned long lastDutyUpdate = 0;  
unsigned long lastPumpOnChangeTime = 0;  
unsigned long lastSearchTime = 0;  



DutyAdjuster dutyAdjuster;





void turnOff(void) {
  control.mode = MODE_OFF;  
  control.mashOn = MODE_OFF;  
  control.pumpOn = false;
  digitalWrite(PUMP_CONTROL_PIN, LOW);
  setHeatOn(&hltDutyController, false);
  setHeatOn(&boilDutyController,false);
  lastPumpOnChangeTime = millis();
}



void setup(void) {
  // start serial port
  Serial.begin(9600);

  // initialie inputs/outputs
  //  pinMode(ledPin, OUTPUT); 

  pinMode(PUMP_CONTROL_PIN, OUTPUT);
  control.controlId = 0;

  turnOff();

  setupDutyController(&hltDutyController,HLT_HEAT_CONTROL_PIN );
  setupDutyController(&boilDutyController,BOIL_HEAT_CONTROL_PIN );
  setupDutyAdjuster(&dutyAdjuster); 
  searchForTempSensors();
}




void loop(void) {

  //  Serial.println("LOOP");


  unsigned long now = millis();  

  // Now will roll over after so long, just reset if it does
  if(lastHeatUpdate > now){
    lastHeatUpdate = 0;
  }
  if(lastDutyUpdate > now){
    lastDutyUpdate = 0;
  }
  if(lastControlIdTime > now){
    lastControlIdTime = 0;
  }    
  if(lastPumpOnChangeTime > now){
    lastPumpOnChangeTime = 0;
  }
  if(lastSearchTime > now){
    lastSearchTime = 0;
  }  

  if(now - lastControlIdTime > 10000){
    //Serial.println("TURN OFF");
    turnOff();    
  }

  if(now - lastDutyUpdate > DUTY_DELAY){

    //Serial.println("DUTY");

    lastDutyUpdate = lastDutyUpdate + DUTY_DELAY;
    readSensors();
    updateHtlTunDutyPump();
  }

  if(now - lastHeatUpdate > HEAT_DELAY){

    //Serial.println("HEAT");


    lastHeatUpdate = lastHeatUpdate + HEAT_DELAY;
    updateHeatForStateAndDuty(&hltDutyController);

    bool boilOn = control.mode == MODE_ON && !hltDutyController.pinState;

    setHeatOn(&boilDutyController,boilOn);
    updateHeatForStateAndDuty(&boilDutyController);
  }

  //Serial.println("READ");

  readSerial();

  if(now - lastSearchTime > SEARCH_DELAY){
    //Serial.println("SEARCH");

    lastSearchTime = lastSearchTime + SEARCH_DELAY;
    searchForTempSensors();
  }

}


byte getHexValue(char iValue){
  if(iValue >='0' && iValue <='9'){
    return (iValue - '0');
  }
  if(iValue >='A' && iValue <='F'){
    return (iValue - 'A'+10);
  }
  if(iValue >='a' && iValue <='f'){
    return (iValue - 'a'+10);
  }
  return 0;
}


void updateHtlTunDutyPump(){
  int duty = 0;
  bool newPumpOn = false;


  if(control.mode == MODE_ON && control.mashOn == MODE_ON && hltSensor != NULL && hltSensor->reading ){
    duty = getDutyFromAdjuster(&dutyAdjuster,control.hltTargetTemp,hltSensor->lastTemp);
    if(tunSensor != NULL){
      if(tunSensor->reading){
        if(tunSensor->lastTemp < control.tunTargetTemp) {
          newPumpOn = true;
        }
      }
    }
  } 

  setHeatDuty(&hltDutyController, duty);

  // pump has min off/on time.
  if(newPumpOn != control.pumpOn){
    unsigned long now = millis(); 
    if(now - 5000 > lastPumpOnChangeTime || control.mode != MODE_ON || control.mashOn != MODE_ON){
      lastPumpOnChangeTime = now;   
      control.pumpOn = newPumpOn;
      digitalWrite(PUMP_CONTROL_PIN, control.pumpOn ? HIGH:LOW);
    }
  }


}













