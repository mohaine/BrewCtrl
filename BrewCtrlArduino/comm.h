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

#ifndef COMM_H_
#define COMM_H_

#define DATA_START 0x01
#define SENSOR_CONTROL 0x12
#define HARDWARE_CONTROL  0x13
#define CONTROL_POINT  0x14
#define DATA_END '\r'
#define MESSAGE_ENEVELOP_SIZE 4
#define MESSAGE_ENEVELOP_START_SIZE 2

#define AUTO_MASK  0x01
#define HAS_DUTY_MASK  0x02

byte serialBuffer[250];
int serialBufferOffset = 0;


int readInt( byte *buffer, int offset) {
  int out;
  byte *data;
  data = (unsigned byte *) &out;
  data[0] =  buffer[offset];
  data[1] =  buffer[offset+1];
  return out;
}

void writeInt(byte *buffer, int offset, int value) {
  byte *data;
  data = (unsigned byte *) &value;
  buffer[offset] = data[0];
  buffer[offset+1] = data[1];
}

float readFloat( byte *buffer, int offset) {
  float out;
  byte *data;
  data = (unsigned byte *) &out;

  data[0] =  buffer[offset];
  data[1] =  buffer[offset+1];
  data[2] =  buffer[offset+2];
  data[3] =  buffer[offset+3];

  return out;
}
void writeFloat(byte *buffer, int offset,float value) {
  byte *data;
  data = (unsigned byte *) &value;
  buffer[offset] = data[0];
  buffer[offset+1] = data[1];
  buffer[offset+2] = data[2];
  buffer[offset+3] = data[3];
}



byte computeCrc8(byte * buffer,int offset, int length){
  return OneWire::crc8( buffer + offset  , length);  
}

void serialWriteStatus(){

  byte buffer[250];
  byte offset = 0;

  buffer[offset++] = '\r';
  int crcStart = offset;
  buffer[offset++] = DATA_START;
  buffer[offset++] = HARDWARE_CONTROL;

  writeInt(buffer,offset, control.controlId);
  offset+=2;

  buffer[offset++] = (control.mode);
  int crcLength = offset - crcStart;
  buffer[offset++] = computeCrc8(buffer, crcStart, crcLength);
  buffer[offset++] = DATA_END;

  for (byte cpIndex=0;cpIndex<controlPointCount;cpIndex++){
    ControlPoint *controlPoint = &controlPoints[cpIndex];

    int crcStart = offset;
    buffer[offset++] = DATA_START;
    buffer[offset++] = CONTROL_POINT;


    buffer[offset++] = controlPoint->controlPin;
    buffer[offset++] = (byte) controlPoint->duty;

    byte booleanValues = 0x00;

    if (controlPoint->automaticControl) {
      booleanValues = booleanValues | AUTO_MASK;
    }
    if (controlPoint-> hasDuty) {
      booleanValues = booleanValues | HAS_DUTY_MASK;
    }

    buffer[offset++] = booleanValues;

    writeFloat(buffer, offset, controlPoint->targetTemp);
    offset += 4;

    for(byte i=0;i<8;i++){
      buffer[offset++] = controlPoint->tempSensorAddress[i];
    }

    crcLength = offset - crcStart;    
    buffer[offset++] = computeCrc8(buffer, crcStart++, crcLength);
    buffer[offset++] = DATA_END;
  }

  for (byte sensorIndex=0;sensorIndex<sensorCount;sensorIndex++){
    TempSensor *sensor = &sensors[sensorIndex];

    int crcStart = offset;
    buffer[offset++] = DATA_START;
    buffer[offset++] = SENSOR_CONTROL;

    for(byte i=0;i<8;i++){
      buffer[offset++] = sensor->address[i];
    }
    buffer[offset++] = sensor->reading;
    writeFloat(buffer,offset,sensor->lastTemp);
    offset+=4;
    crcLength = offset - crcStart;    
    buffer[offset++] = computeCrc8(buffer, crcStart++, crcLength);
    buffer[offset++] = DATA_END;
  }

  Serial.write(buffer,offset);

}

bool validateMessage(byte* buffer, int offset, byte messageId,int messageLength) {
  boolean valid = true;
  int startOffset = offset;

  valid = valid && buffer[offset++] == DATA_START;
  valid = valid && buffer[offset++] == messageId;

  if (valid) {
    offset += messageLength;
    int crcLength = offset - startOffset;

    valid = valid && buffer[offset++] == computeCrc8(buffer, startOffset++, crcLength);
    valid = valid && buffer[offset++] == DATA_END;
  }
  return valid;
}


void readControlMessage(byte * serialBuffer,int offset){
  lastControlIdTime = millis();
  control.controlId = readInt(serialBuffer,offset);
  offset+=2;

  control.mode = serialBuffer[offset++];
  if(control.mode ==  MODE_ON){
    setHeatOn(&hltDutyController,true);            
    setHeatOn(&boilDutyController,true);            
  }
  else {
    turnOff();   
  }

  /*
  int boilDuty = serialBuffer[offset++];
   if(boilDuty >= 0 && boilDuty <= 100){
   setHeatDuty(&boilDutyController,boilDuty);
   }
   
   control.mashOn = serialBuffer[offset++];
   
   for (byte sensorIndex=0;sensorIndex<sensorCount;sensorIndex++){
   TempSensor *sensor = &sensors[sensorIndex];
   boolean same = true;
   for(byte i=0;i<8 && same;i++){             
   same = sensor->address[i] == serialBuffer[offset+ i] ;
   }
   if(same){
   hltSensor = sensor;
   break;
   }
   } 
   offset+=8;
   for (byte sensorIndex=0;sensorIndex<sensorCount;sensorIndex++){
   TempSensor *sensor = &sensors[sensorIndex];
   boolean same = true;
   for(byte i=0;i<8 && same;i++){             
   same = sensor->address[i] == serialBuffer[offset+ i] ;
   }
   if(same){
   tunSensor = sensor;
   break;
   }
   } 
   offset+=8;    
   
   control.hltTargetTemp = readFloat(serialBuffer,offset);
   offset+=4;
   control.tunTargetTemp = readFloat(serialBuffer,offset);
   offset+=4;  
   */
}

void handleExtra(byte* data, int offset, int length) {

}

bool  readSerial() {
  bool readMessage = false;
  while (Serial.available() > 0 && serialBufferOffset < sizeof(serialBuffer)) {

    serialBuffer[serialBufferOffset++] = Serial.read();

    int bufferOffset = serialBufferOffset - 1;
    if (serialBuffer[bufferOffset] == DATA_END) {
      // On stop bit
      int messageStart = bufferOffset - (3 + MESSAGE_ENEVELOP_SIZE - 1);
      if (messageStart >= 0) {
        if (validateMessage(serialBuffer, messageStart, HARDWARE_CONTROL, 3)) {
          readControlMessage(serialBuffer, messageStart + MESSAGE_ENEVELOP_START_SIZE);
          readMessage = true;
          if (messageStart > 0) {
            int extraLength = messageStart - 1;
            if (extraLength > 0) {
              handleExtra(serialBuffer, 0, extraLength);
            }
          }
          serialBufferOffset = 0;
        }
      }
    }
  }

  if (serialBufferOffset >= sizeof(serialBuffer) ){
    handleExtra(serialBuffer, 0, serialBufferOffset);
    serialBufferOffset = 0;
  }

  if(readMessage){
    serialWriteStatus();
  }

  return readMessage;
}


#endif



































