#include "sensor.h"

TempSensor sensors[8];
int sensorCount = 0;

void readSensors() {

}

void searchForTempSensors() {

}
TempSensor* getSensor(byte* address) {
	return 0;
}

TempSensor* getSensorByIndex(int i) {
	return &sensors[i];
}

int getSensorCount() {
	return sensorCount;
}
