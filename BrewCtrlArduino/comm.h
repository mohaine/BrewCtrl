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


#define DATA_START 0x11
#define SENSOR_CONTROL 0x12
#define HARDWARE_CONTROL  0x13
#define CONTROL_POINT  0x14
#define DATA_END '\r'
#define MESSAGE_ENEVELOP_SIZE 4
#define MESSAGE_ENEVELOP_START_SIZE 2

#define COMM_LOSS_MASK 0x01

#define AUTO_MASK  0x01
#define HAS_DUTY_MASK  0x02

typedef struct {
  byte msgId;  
  byte length;  
  void (*processFunction)(byte * serialBuffer,int offset) ;
} 
ReadMessage;

ReadMessage readMessages[2];
byte readMessagesCount = 2;

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
void writeLong(byte *buffer, int offset, long value) {
  byte *data;
  data = (unsigned byte *) &value;
  buffer[offset] = data[0];
  buffer[offset+1] = data[1];
  buffer[offset+2] = data[2];
  buffer[offset+3] = data[3];
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
  writeLong(buffer,offset, millis());
  offset+=4;

  buffer[offset++] = (control.mode);
  buffer[offset++] = (control.maxAmps);

  {
    byte booleanValues = 0x00;
    if (control.turnOffOnCommLoss) {
      booleanValues = booleanValues | COMM_LOSS_MASK;
    }
    buffer[offset++] = booleanValues;
  }

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
    buffer[offset++] = (controlPoint->fullOnAmps);



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

  for(int i=0;i<offset;i++){
    Serial.write(buffer[i]);
  }
 // Serial.write(buffer,offset);

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
void readControlPoint(byte * serialBuffer,int offset){

  byte pin = serialBuffer[offset++];
  byte duty = serialBuffer[offset++];
  byte fullOnAmps = serialBuffer[offset++];

  ControlPoint* cp = NULL;

  for(int cpIndex=0;cpIndex<controlPointCount && cpIndex<MAX_CP_COUNT;cpIndex++){    
    if(controlPoints[cpIndex].controlPin == pin){
      cp = &controlPoints[cpIndex];
      break;
    }    
  }

  byte booleanValues = serialBuffer[offset++];
  bool automaticControl =  (booleanValues & AUTO_MASK) != 0;
  bool hasDuty = (booleanValues & HAS_DUTY_MASK)  != 0;

  if(cp == NULL && controlPointCount<MAX_CP_COUNT && pin != ONE_WIRE_PIN){
    cp = &controlPoints[controlPointCount++];
    cp->controlPin = pin;
    //Toogle so we init below
    cp->hasDuty = !hasDuty;
    cp->automaticControl = !automaticControl;
    setHeatOn(&cp->dutyController,control.mode ==  MODE_ON);            
    resetDutyState(&cp->dutyController);
  } 

  if(cp != NULL){
    cp->fullOnAmps = fullOnAmps;

    if(control.mode !=  MODE_ON){
      duty = 0;
      automaticControl  = false;
    }

    if(automaticControl!= cp->automaticControl){
      cp->automaticControl = automaticControl;
      if(automaticControl){
        cp->duty = 0;
        setupPid(&cp->pid);
      }
    } 

    if(!automaticControl){   
      cp->duty = duty;
    }

    if(hasDuty!= cp->hasDuty){
      cp->hasDuty = hasDuty;
      setupDutyController(&cp->dutyController,cp->controlPin);    
    }


    cp->targetTemp = readFloat(serialBuffer,offset);
    offset+=4;    

    for(byte i=0;i<8;i++){             
      cp->tempSensorAddress[i] = serialBuffer[offset+i] ;
    }

  }
}

void readControlMessage(byte * serialBuffer,int offset){

  lastControlIdTime = millis();
  control.controlId = readInt(serialBuffer,offset);
  offset+=2;
  // Skip Time
  offset+=4;

  control.mode = serialBuffer[offset++];
  control.maxAmps = serialBuffer[offset++];
  byte booleanValues = serialBuffer[offset++];
  control.turnOffOnCommLoss =  (booleanValues & COMM_LOSS_MASK) != 0;

  if(control.mode ==  MODE_ON){
    for(int cpIndex=0;cpIndex<controlPointCount && cpIndex<MAX_CP_COUNT;cpIndex++){    
      setHeatOn(&controlPoints[cpIndex].dutyController,true);            
    }
  }
  else {
    turnOff();   
  }

}

void handleExtra(byte* data, int offset, int length) {



}

void setupComm(){
  readMessages[0].msgId = HARDWARE_CONTROL; 
  readMessages[0].length = 9; 
  readMessages[0].processFunction = readControlMessage;
  readMessages[1].msgId = CONTROL_POINT; 
  readMessages[1].length = 16; 
  readMessages[1].processFunction = readControlPoint;


}


bool  readSerial() {
  bool readMessage = false;
  while (Serial.available() > 0 && serialBufferOffset < sizeof(serialBuffer)) {

    serialBuffer[serialBufferOffset++] = Serial.read();

    int bufferOffset = serialBufferOffset - 1;
    if (serialBuffer[bufferOffset] == DATA_END) {
      // On stop bit
      for(int messageIndex=0;messageIndex<readMessagesCount;messageIndex++){
        int messageStart = bufferOffset - (readMessages[messageIndex].length + MESSAGE_ENEVELOP_SIZE - 1);
        if (messageStart >= 0) {
          if (validateMessage(serialBuffer, messageStart, readMessages[messageIndex].msgId, readMessages[messageIndex].length)) {
            readMessages[messageIndex].processFunction(serialBuffer, messageStart + MESSAGE_ENEVELOP_START_SIZE);
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












