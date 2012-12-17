#include "sensor.h"

#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>
#include <linux/limits.h>

TempSensor sensors[8];
int sensorCount = 0;

#define W1_ROOT "mock/sys/bus/w1/devices/"

void readSensors() {
	char tmp[PATH_MAX];

	char data[200];

	for (byte sensorIndex = 0; sensorIndex < sensorCount; sensorIndex++) {
		TempSensor *sensor = &sensors[sensorIndex];
		sprintf(tmp, "%s/%s", sensor->sysfile, "w1_slave");
		FILE* f = fopen(tmp, "rb");
		if (f) {
			int readSize = fread(data, 8, sizeof(data), f);
			printf("  readSize: %d\n", readSize);
			if (readSize == 8) {
				sensor->reading = false;
				sensorCount++;
			}
		}
	}

}

void searchForTempSensors() {
	DIR *dp;
	struct dirent *ep;

	dp = opendir(W1_ROOT);
	if (dp != NULL) {
		while ((ep = readdir(dp))) {
			if (strlen(ep->d_name) == 15 && ep->d_name[2] == '-') {
				char tmp[PATH_MAX];
				sprintf(tmp, "%s/%s", W1_ROOT, ep->d_name);
				TempSensor* sensor = &sensors[sensorCount];
				if (realpath(&tmp, sensor->sysfile)) {
					sprintf(tmp, "%s/%s", sensor->sysfile, "id");
					FILE* f = fopen(tmp, "rb");
					if (f) {
						int readSize = fread(&sensor->address, 1, 8, f);
						if (readSize == 8) {
							sensor->reading = false;
							sensorCount++;
						}
					}

				}

			}
		}
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
