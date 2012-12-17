/* Hello World program */

#include<stdio.h>
#include <unistd.h>

#include "sensor.h"
#include "brewctrl.h"

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

int main() {
	searchForTempSensors();
	readSensors();
	int sensorCount = getSensorCount();

	printf("Found %d Sensors \n", sensorCount);

	for (int i = 0; i < sensorCount; i++) {
		TempSensor *sensor = getSensorByIndex(i);

		printf("Sensor %d ", i);

		for (int j = 0; j < 8; j++) {
			printf("%02x", sensor->address[j]);
		}

		if (sensor->reading) {
			printf("%0.000f", sensor->lastTemp);

		}
		printf("\n");

	}

	return 0;

}

