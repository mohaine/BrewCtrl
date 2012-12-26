#include "sensor.h"

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
	char tmp[PATH_MAX];

	char data[200];

	for (byte sensorIndex = 0; sensorIndex < sensorCount; sensorIndex++) {
		TempSensor *sensor = &sensors[sensorIndex];
		bool successfulRead = false;
		sprintf(tmp, "%s/%s", sensor->sysfile, "w1_slave");
		FILE* f = fopen(tmp, "rb");

		if (f) {
			int readSize = fread(data, 1, sizeof(data), f);
			if (readSize > 0) {
				data[sizeof(data) - 1] = 0;

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
									}
								}
								int milliCs = atoi(tIndex);
								sensor->lastTemp = ((double) milliCs) / 1000;
								successfulRead = true;
							}
						}
					}

				}
			}

			fclose(f);
		}
		sensor->reading = successfulRead;

	}

}

void searchForTempSensors() {
	DIR *dp;
	struct dirent *ep;

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
						int readSize = fread(&sensor->address, 1, 8, f);
						if (readSize == 8) {
							if (getSensor(sensor->address) == NULL) {
								sensor->reading = false;
								sensorCount++;
								valid = true;
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
		perror("Couldn't open the directory");
	}

}

TempSensor* getSensor(byte* address) {
	for (byte sensorIndex = 0; sensorIndex < sensorCount; sensorIndex++) {
		TempSensor *sensor = &sensors[sensorIndex];
		bool same = true;
		for (int j = 0; same && j < 8; j++) {
			same = sensor->address[j] == address[j];
		}
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

		printf("Sensor %d ", i);

		for (int j = 0; j < 8; j++) {
			printf("%02x", sensor->address[j]);
		}

		if (sensor->reading) {
			printf(" Temp: %0.3f", sensor->lastTemp);

		}
		printf("\n");

	}
}
