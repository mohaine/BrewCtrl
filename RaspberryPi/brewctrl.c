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
#include "logger.h"

#include <sys/time.h>
#include <math.h>
#include <stdlib.h>
#include <stdint.h>

struct timeval startTime;
void initBrewCtrl() {
    gettimeofday(&startTime, NULL);
}
long millis() {
    struct timeval time;

    gettimeofday(&time, NULL);

    long mills = (time.tv_sec - startTime.tv_sec) * 1000;
    mills += (time.tv_usec - startTime.tv_usec ) / 1000;

    return mills;
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
