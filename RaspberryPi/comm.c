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

#define ONE_WIRE_PIN 3
#define DEFAULT_PORT 2739

#include "sensor.h"
#include "comm.h"
#include "crc8.h"
#include "control.h"

#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>
#include <errno.h>
#include <pthread.h>

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

#define READ_BUFFER_SIZE 250

typedef struct {
	byte msgId;
	byte length;
	void (*processFunction)(byte * serialBuffer, int offset);
} ReadMessage;

ReadMessage readMessages[2];
byte readMessagesCount = 2;

long lastSuccessfulControlIdTime = 0;

long lastControlIdTime() {
	return lastSuccessfulControlIdTime;
}

int readInt(byte *buffer, int offset) {
	int out;
	byte *data;
	data = (byte *) &out;
	data[0] = buffer[offset];
	data[1] = buffer[offset + 1];
	return out;
}

void writeInt(byte *buffer, int offset, int value) {
	byte *data;
	data = (byte *) &value;
	buffer[offset] = data[0];
	buffer[offset + 1] = data[1];
}
void writeLong(byte *buffer, int offset, long value) {
	byte *data;
	data = (byte *) &value;
	buffer[offset] = data[0];
	buffer[offset + 1] = data[1];
	buffer[offset + 2] = data[2];
	buffer[offset + 3] = data[3];
}

float readFloat(byte *buffer, int offset) {
	float out;
	byte *data;
	data = (byte *) &out;

	data[0] = buffer[offset];
	data[1] = buffer[offset + 1];
	data[2] = buffer[offset + 2];
	data[3] = buffer[offset + 3];

	return out;
}
void writeFloat(byte *buffer, int offset, float value) {
	byte *data;
	data = (byte *) &value;
	buffer[offset] = data[0];
	buffer[offset + 1] = data[1];
	buffer[offset + 2] = data[2];
	buffer[offset + 3] = data[3];
}

void writeStatus(int clntSocket) {

	Control* control = getControl();

	byte buffer[250];
	byte offset = 0;

	buffer[offset++] = '\r';
	int crcStart = offset;
	buffer[offset++] = DATA_START;
	buffer[offset++] = HARDWARE_CONTROL;

	writeInt(buffer, offset, control->controlId);
	offset += 2;
	writeLong(buffer, offset, millis());
	offset += 4;

	buffer[offset++] = (control->mode);
	buffer[offset++] = (control->maxAmps);

	{
		byte boolValues = 0x00;
		if (control->turnOffOnCommLoss) {
			boolValues = boolValues | COMM_LOSS_MASK;
		}
		buffer[offset++] = boolValues;
	}

	int crcLength = offset - crcStart;
	buffer[offset++] = computeCrc8(buffer, crcStart, crcLength);
	buffer[offset++] = DATA_END;

	int controlPointCount = getControlPointCount();
	for (byte cpIndex = 0; cpIndex < controlPointCount; cpIndex++) {
		ControlPoint *controlPoint = getControlPointByIndex(cpIndex);

		int crcStart = offset;
		buffer[offset++] = DATA_START;
		buffer[offset++] = CONTROL_POINT;

		buffer[offset++] = controlPoint->controlPin;
		buffer[offset++] = (byte) controlPoint->duty;
		buffer[offset++] = (controlPoint->fullOnAmps);

		byte boolValues = 0x00;
		if (controlPoint->automaticControl) {
			boolValues = boolValues | AUTO_MASK;
		}
		if (controlPoint->hasDuty) {
			boolValues = boolValues | HAS_DUTY_MASK;
		}
		buffer[offset++] = boolValues;

		writeFloat(buffer, offset, controlPoint->targetTemp);
		offset += 4;

		for (byte i = 0; i < 8; i++) {
			buffer[offset++] = controlPoint->tempSensorAddress[i];
		}

		crcLength = offset - crcStart;
		buffer[offset++] = computeCrc8(buffer, crcStart++, crcLength);
		buffer[offset++] = DATA_END;
	}

	int sensorCount = getSensorCount();
	for (int sensorIndex = 0; sensorIndex < sensorCount; sensorIndex++) {
		TempSensor *sensor = getSensorByIndex(sensorIndex);
		int crcStart = offset;
		buffer[offset++] = DATA_START;
		buffer[offset++] = SENSOR_CONTROL;

		for (int i = 0; i < 8; i++) {
			buffer[offset++] = sensor->address[i];
		}
		buffer[offset++] = sensor->reading;
		writeFloat(buffer, offset, sensor->lastTemp);
		offset += 4;
		crcLength = offset - crcStart;
		buffer[offset++] = computeCrc8(buffer, crcStart++, crcLength);
		buffer[offset++] = DATA_END;
	}

	write(clntSocket, &buffer, offset);

}

bool validateMessage(byte* buffer, int offset, byte messageId,
		int messageLength) {
	bool valid = true;
	int startOffset = offset;

	valid = valid && buffer[offset++] == DATA_START;
	valid = valid && buffer[offset++] == messageId;
	if (valid) {
		offset += messageLength;
		int crcLength = offset - startOffset;

		valid = valid
				&& buffer[offset++]
						== computeCrc8(buffer, startOffset++, crcLength);

		valid = valid && buffer[offset++] == DATA_END;
	}
	return valid;
}
void readControlPoint(byte * serialBuffer, int offset) {

	byte pin = serialBuffer[offset++];
	byte duty = serialBuffer[offset++];
	byte fullOnAmps = serialBuffer[offset++];

	ControlPoint* cp = NULL;

	int controlPointCount = getControlPointCount();
	for (byte cpIndex = 0; cpIndex < controlPointCount; cpIndex++) {
		ControlPoint *controlPoint = getControlPointByIndex(cpIndex);
		if (controlPoint->controlPin == pin) {
			cp = controlPoint;
			break;
		}
	}

	byte boolValues = serialBuffer[offset++];
	bool automaticControl = (boolValues & AUTO_MASK) != 0;
	bool hasDuty = (boolValues & HAS_DUTY_MASK) != 0;

	Control* control = getControl();

	if (cp == NULL && controlPointCount < MAX_CP_COUNT && pin != ONE_WIRE_PIN) {
		cp = getControlPointByIndex(controlPointCount);
		cp->controlPin = pin;
		//Toogle so we init below
		cp->hasDuty = !hasDuty;
		cp->automaticControl = !automaticControl;

		setHeatOn(&cp->dutyController, control->mode == MODE_ON);
		resetDutyState(&cp->dutyController);

		addControlPoint();
	}

	if (cp != NULL) {
		cp->fullOnAmps = fullOnAmps;

		if (control->mode != MODE_ON) {
			duty = 0;
			automaticControl = false;
		}

		if (automaticControl != cp->automaticControl) {
			cp->automaticControl = automaticControl;
			if (automaticControl) {
				cp->duty = 0;
				setupPid(&cp->pid);
			}
		}

		if (!automaticControl) {
			cp->duty = duty;
		}

		if (hasDuty != cp->hasDuty) {
			cp->hasDuty = hasDuty;
			setupDutyController(&cp->dutyController, cp->controlPin);
		}

		cp->targetTemp = readFloat(serialBuffer, offset);
		offset += 4;

		for (byte i = 0; i < 8; i++) {
			cp->tempSensorAddress[i] = serialBuffer[offset + i];
		}

	}
}

void readControlMessage(byte * serialBuffer, int offset) {
	Control* control = getControl();

	lastSuccessfulControlIdTime = millis();
	control->controlId = readInt(serialBuffer, offset);
	offset += 2;
// Skip Time
	offset += 4;

	control->mode = serialBuffer[offset++];
	control->maxAmps = serialBuffer[offset++];
	byte boolValues = serialBuffer[offset++];
	control->turnOffOnCommLoss = (boolValues & COMM_LOSS_MASK) != 0;

	if (control->mode == MODE_ON) {
		int controlPointCount = getControlPointCount();
		for (byte cpIndex = 0; cpIndex < controlPointCount; cpIndex++) {
			ControlPoint *controlPoint = getControlPointByIndex(cpIndex);
			setHeatOn(&controlPoint->dutyController, true);
		}
	} else {
		turnOff();
	}

}

void handleExtra(byte* data, int offset, int length) {

}

void setupMessages() {
	readMessages[0].msgId = HARDWARE_CONTROL;
	readMessages[0].length = 9;
	readMessages[0].processFunction = readControlMessage;
	readMessages[1].msgId = CONTROL_POINT;
	readMessages[1].length = 16;
	readMessages[1].processFunction = readControlPoint;
}

void* handleClientThread(void *ptr) {
	int * number = (int *) ptr;
	int clntSocket = *number;
	bool readMessage = false;

	byte serialBuffer[READ_BUFFER_SIZE];
	int serialBufferOffset = 0;

	while (1) {
		ssize_t readSize;

		readSize = read(clntSocket, &serialBuffer + serialBufferOffset,
				READ_BUFFER_SIZE - serialBufferOffset);

		if (readSize < 0) {
			//Read error.  Clean up.
			break;
		}

		int newDataStart = 0;
		int dataEnd = serialBufferOffset + readSize;
		for (int bufferOffset = serialBufferOffset; bufferOffset < dataEnd;
				bufferOffset++) {

			if (serialBuffer[bufferOffset] == DATA_END) {
				// On stop bit
				for (int messageIndex = 0; messageIndex < readMessagesCount;
						messageIndex++) {

					int messageStart = bufferOffset
							- (readMessages[messageIndex].length
									+ MESSAGE_ENEVELOP_SIZE - 1);
					if (messageStart >= 0) {
						if (validateMessage(serialBuffer, messageStart,
								readMessages[messageIndex].msgId,
								readMessages[messageIndex].length)) {
							readMessages[messageIndex].processFunction(
									serialBuffer,
									messageStart + MESSAGE_ENEVELOP_START_SIZE);
							readMessage = true;
							if (messageStart > 0) {
								int extraLength = messageStart - 1;
								if (extraLength > 0) {
									handleExtra(serialBuffer, 0, extraLength);
								}
							}
							newDataStart = bufferOffset + 1;
						}
					}
				}
			}
		}

		serialBufferOffset = dataEnd;

		if (newDataStart > 0) {
			if (newDataStart == serialBufferOffset) {
				serialBufferOffset = 0;
			} else {
				int dataLength = dataEnd - newDataStart;
				memcpy(serialBuffer, serialBuffer + newDataStart, dataLength);
				serialBufferOffset = dataLength;
			}
		}

		//Overflow
		if (serialBufferOffset == sizeof(serialBuffer)) {
			handleExtra(serialBuffer, 0, serialBufferOffset);
			serialBufferOffset = 0;
		}

		if (readMessage) {
			writeStatus(clntSocket);
		}

	}
	close(clntSocket);

	return NULL;
}

void* listenThread(void *ptr) {

	int sock;
	int connected;
	int setSockOp = 1;
	pthread_t thread;

	setupMessages();

	struct sockaddr_in server_addr, client_addr;
	socklen_t sin_size;

	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
		perror("Socket");
		return NULL;
	}

	if (setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &setSockOp, sizeof(int))
			== -1) {
		perror("Setsockopt");
		return NULL;
	}

	server_addr.sin_family = AF_INET;
	server_addr.sin_port = htons(DEFAULT_PORT);
	server_addr.sin_addr.s_addr = INADDR_ANY;
	bzero(&(server_addr.sin_zero), 8);

	if (bind(sock, (struct sockaddr *) &server_addr, sizeof(struct sockaddr))
			== -1) {
		perror("Unable to bind");
		return NULL;
	}

	if (listen(sock, 5) == -1) {
		perror("Listen");
		return NULL;
	}

	printf("Waiting for client on port %d\n", DEFAULT_PORT);
	fflush(stdout);

	while (1) {
		sin_size = sizeof(struct sockaddr_in);
		connected = accept(sock, (struct sockaddr *) &client_addr, &sin_size);
		printf("Connection from (%s , %d)\n", inet_ntoa(client_addr.sin_addr),
				ntohs(client_addr.sin_port));

		pthread_create(&thread, NULL, handleClientThread, (void*) &connected);
	}
	return NULL;
}
void startComm() {
	pthread_t thread;
	pthread_create(&thread, NULL, listenThread, NULL);

}
