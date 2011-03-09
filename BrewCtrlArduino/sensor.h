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

#include "WProgram.h"




/*
DS18B20 hooked up to ear phone connectior like this:
 
 tip => Right (VDD)
 middle => Middle (DQ)
 back => Left (GRND)
 
 Phone:
 RJ11:
 
 1         Black
 2  Data   Red
 3  GND    Green 
 4  VDD    Yellow
 
 
 */


typedef struct {
  byte address[8];
  double lastTemp;
  bool reading;
} 
TempSensor;

TempSensor sensors[8];
byte sensorCount = 0;
OneWire  ds(ONE_WIRE_PIN); 
bool passivePowerMode = false;


void  searchForTempSensors(){
  int loopCount = 0;
  //Serial.println("SEARCH RESET" );
  ds.reset_search();
  while (sensorCount < sizeof(sensors) && ds.search(sensors[sensorCount].address) ) {
    if(loopCount++>10){
      break;
    }


    //Serial.print("SEARCH LOOP ");
    //Serial.println((int) sensorCount);   



    if ( OneWire::crc8( sensors[sensorCount].address, 7) != sensors[sensorCount].address[7]) {
      //Serial.print("CRC is not valid!\n");
      continue;
    }
    if ( sensors[sensorCount].address[0] != 0x28) {
      //Serial.print("Device is not a DS18B20 family device.\n");
      continue;
    }
    boolean existingSensor = false;
    for(int i=0;i<sensorCount;i++) {

      boolean same = true;
      for(int j=0;same && j<8;j++){        
        same = sensors[sensorCount].address[j] == sensors[i].address[j];        
      }

      if(same){
        existingSensor = true;
        break;
      }
    }
    if(!existingSensor){
      //Serial.println("ADD SESNOR");

      sensors[sensorCount].reading = false;
      sensors[sensorCount].lastTemp = 0;
      sensorCount++;
    }
  }
  //  Serial.print( "Found ");
  //  Serial.print( (int) sensorCount);
  //  Serial.println( "  DS18B20 Temp Sensors.");
}

double getTemp(byte* data){
  int lsb = data[0];
  int msb = data[1];
  bool negative = false;
  int    value = (msb << 4) | (lsb >> 4);
  int    fraction = ( lsb & 0x0F );
  if(msb >> 7 == 1){
    negative = true;
    value = value ^ 0xFFF;
    fraction = 16 - fraction;
  }
  double temp =  value +  (double) fraction /16;
  if(negative){
    temp = temp * -1;
  }
  return temp;
}



boolean readSensorRetry(TempSensor *sensor){

  byte data[12];


  ds.reset();
  ds.select(sensor->address);
  ds.write(0x44,passivePowerMode ? 1 : 0);         // start conversion, with parasite power on at the end

  if (passivePowerMode){
    delay(1000);     // maybe 750ms is enough, maybe not
    // we might do a ds.depower() here, but the reset will take care of it.
  }

  bool present = ds.reset();
  if (present){
    ds.select(sensor->address);
    ds.write(0xBE);
    for ( byte i = 0; i < 9; i++) {
      data[i] = ds.read();
    }

    if (OneWire::crc8( data , 8) == data[8]) {
      double tempC = getTemp(data);


      // 85 is the chips start up temp.  It reads as a valid temp so......      
      if(!sensor->reading && tempC == 85){
        //Serial.println("READ AN 85");

        // Wasn't reading and have temp = 85.0 assume it is startup temp
        sensor->reading = false;
      } 
      else if( (sensor->lastTemp < 84 || sensor->lastTemp > 86) && tempC == 85){
        // Was reading temp ouside of 85+-1 and have temp = 85.0 assume it is startup temp
        //        Serial.println(" BINK TO 85");
        sensor->reading = false;
      } 
      else {        
        //             Serial.println(" SUCCESS");
        sensor->lastTemp = tempC;
        sensor->reading = true;
      }
    } 
    else {
      //              Serial.println(" CRC FAil");
      sensor->reading = false;
    }

  } 
  else {
    //     Serial.println(" NOT PRESENT");
    sensor->reading = false;
  }
}

void readSensor(TempSensor *sensor){
  readSensorRetry(sensor);
  if(!sensor->reading){
    readSensorRetry(sensor);
  }
  if(!sensor->reading){
    readSensorRetry(sensor);
  }  
}

void readSensors(){
  for (byte sensorIndex=0;sensorIndex<sensorCount;sensorIndex++){
    readSensor(&sensors[sensorIndex]);
  }
}




#endif






