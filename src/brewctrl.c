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

#include "brewctrl.h"

#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <time.h>

#include "logger.h"

void initBrewCtrl() {

}

long millis() {
	struct timespec time;
	clock_gettime(CLOCK_MONOTONIC, &time);

	long millisFromSec = (time.tv_sec) * 1000;
	long millisFromUsec = (time.tv_nsec) / 1000000;
	return millisFromSec + millisFromUsec;
}

char * generateRandomId() {
	char * data = malloc(17);

	if (data == NULL) {
		ERR("generateRandomId Malloc Failed\n");
		exit(-1);
	}

	for (int i = 0; i < 16; i++) {
		double x = ((double) rand() / (double) RAND_MAX);

		data[i] = ((char) (0x41 + floor(x * 26.0)));
	}
	data[16] = 0;
	return data;
}

char* mallocStringFromString(char *str) {
	int length = strlen(str);
	char * returnValue = malloc(length + 1);
	if (returnValue == NULL) {
		exit(-1);
	}
	memcpy(returnValue, str, length + 1);
	return returnValue;
}
