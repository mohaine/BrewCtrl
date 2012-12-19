/* Hello World program */

#include<stdio.h>
#include <unistd.h>

#include "sensor.h"
#include "brewctrl.h"

int main() {

//	printf("Search For Sensors\n");
//	for (int i = 0; i < 1000000; i++) {
//		searchForTempSensors();
//	}
//	if (getSensorCount() > 0) {
//		printf("Read Sensors\n");
//		for (int i = 0; i < 1000000; i++) {
//			readSensors();
//		}
//	}

	searchForTempSensors();
	readSensors();

	listSensors();

	return 0;

}

