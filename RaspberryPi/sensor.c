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

#include "sensor.h"
#include "logger.h"

#define _GNU_SOURCE

#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>
#include <linux/limits.h>
#include <limits.h>

#define MAX_SENSORS 100

TempSensor sensors[MAX_SENSORS];
int sensorCount = 0;

#define W1_ROOT SYS_PATH"/bus/w1/devices/"

#define BAD_READ "00 00 00 00 00 00 00 00 00 : crc=00 YES"

void readSensors() {

	DBG("Read Sensors\n");

	char tmp[PATH_MAX];

	char data[200];
	int sensorCount = getSensorCount();

	for (int sensorIndex = 0; sensorIndex < sensorCount; sensorIndex++) {
		TempSensor *sensor = &sensors[sensorIndex];
		sprintf(tmp, "%s/%s", sensor->sysfile, "w1_slave");

		FILE* f = fopen(tmp, "r");

		if (f) {
			int readSize = fread(data, 1, sizeof(data) - 1, f);
			if (readSize > 20) {
				data[readSize + 1] = 0;

				if (memcmp(BAD_READ, data, sizeof(BAD_READ) - 1) != 0) {

					char* crcIndex = strstr(data, "crc=");
					if (crcIndex > 0) {

						crcIndex += 7;
						if (strcmp(crcIndex, "YES")) {
							// Have a valid read. Get the value

							char* tIndex = strstr(crcIndex, "t=");

							if (tIndex > 0) {
								tIndex += 2;
								tIndex[10] = 0;
								for (int i = 0; i < 10; i++) {
									char c = tIndex[i];
									if (c < '0' || c > '9') {
										tIndex[i] = 0;
										break;
									}
								}
								int milliCs = atoi(tIndex);

								double tempC = ((double) milliCs) / 1000;

								// 85 is the chips start up temp.  It reads as a valid temp so......
								if (!hasVaildTemp(sensor) && (tempC == 85 || tempC == 0)) {
									DBG("Sensor temp is startup temp. Ignore\n");
								} else if ((sensor->lastTemp < 84 || sensor->lastTemp > 86) && tempC == 85) {
									// Was reading temp ouside of 85+-1 and have temp = 85.0 assume it is startup temp
									DBG("Sensor temp is startup temp 85 and wasn't close last time. Ignore\n");
								} else if ((sensor->lastTemp < -1 || sensor->lastTemp > 1) && tempC == 0) {
									// Was reading temp ouside of 0+-1 and have temp = 0.0 assume it is startup temp
									DBG("Sensor temp is startup temp 0 and wasn't close last time. Ignore\n");
								} else {
									sensor->lastTemp = tempC;
									sensor->lastReadMillis = millis();
								}
							} else {
								DBG("no t= in sensor data\n");
							}
						} else {
							DBG("invalid crc in sensor data\n");
						}
					} else {
						DBG("no crc in sensor data\n");
					}
				} else {
					DBG("read zero temp/crc data from sensor data\n");
				}
			} else {
				DBG("Failed to read 20 from sensor file\n");
			}

			fclose(f);
		} else {
			DBG("Failed to open sensor file\n");
		}

	}
}

bool hasVaildTemp(TempSensor* sensor) {
//printf("%u %u\n",millis(), millis() - sensor->lastReadMillis);
	if (sensor->lastReadMillis > 0) {

		return millis() - sensor->lastReadMillis < 2000;
	}

	return false;
}

void searchForTempSensors() {
	DIR *dp;
	struct dirent *ep;
	byte address[8];

	dp = opendir(W1_ROOT);
	if (dp != NULL) {
		char * restrict tmp = malloc(PATH_MAX);
		while ((ep = readdir(dp))) {
			if (strlen(ep->d_name) == 15 && ep->d_name[2] == '-') {
				sprintf(tmp, "%s/%s", W1_ROOT, ep->d_name);
				TempSensor* sensor = &sensors[sensorCount];

				sensor->sysfile = realpath(tmp, NULL);

				if (sensor->sysfile) {
					bool valid = false;
					sprintf(tmp, "%s/%s", sensor->sysfile, "id");
					FILE* f = fopen(tmp, "rb");
					if (f) {
						int readSize = fread(&address, 1, 8, f);
						if (readSize == 8) {
							char * addressStr = malloc(17);
							sprintf(addressStr, "%02x%02x%02x%02x%02x%02x%02x%02x", address[0] & 0xff, address[1] & 0xff, address[2] & 0xff, address[3] & 0xff, address[4] & 0xff, address[5] & 0xff,
									address[6] & 0xff, address[7] & 0xff);

							if (getSensorByAddress(addressStr) == NULL) {
								DBG("Found New Sensor: %s\n", addressStr);
								sensor->addressPtr = addressStr;
								sensor->lastReadMillis = -1;
								sensorCount++;
								valid = true;
							} else {
								free(addressStr);
							}
						}
						fclose(f);
					}
					if (!valid) {
						free(sensor->sysfile);
						sensor->sysfile = NULL;
					}
				}

			}
		}

		free(tmp);
		(void) closedir(dp);

	} else {
		ERR("Couldn't One Wire directory: %s\n", W1_ROOT);
	}

}

TempSensor* getSensorByAddress(char* address) {

	for (int sensorIndex = 0; sensorIndex < sensorCount; sensorIndex++) {
		TempSensor *sensor = &sensors[sensorIndex];

		bool same = strcmp(sensor->addressPtr, address) == 0;

		if (same) {
			return sensor;
		}
	}
	return NULL;
}

TempSensor* getSensorByIndex(int i) {
	return &sensors[i];
}

int getSensorCount() {
	return sensorCount;
}

byte getHexValue(char iValue) {
	if (iValue >= '0' && iValue <= '9') {
		return (iValue - '0');
	}
	if (iValue >= 'A' && iValue <= 'F') {
		return (iValue - 'A' + 10);
	}
	if (iValue >= 'a' && iValue <= 'f') {
		return (iValue - 'a' + 10);
	}
	return 0;
}

void listSensors() {
	int sensorCount = getSensorCount();

	for (int i = 0; i < sensorCount; i++) {
		TempSensor *sensor = getSensorByIndex(i);

		printf("Sensor %d %s\n", i, sensor->addressPtr);

		if (hasVaildTemp(sensor)) {
			printf(" Temp: %0.3f", sensor->lastTemp);

		}
		printf("\n");

	}
}
