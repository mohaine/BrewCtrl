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

#include <stdio.h>
#include <unistd.h>
#include <execinfo.h>
#include <signal.h>
#include <stdlib.h>
#include <linux/types.h>

#include "duty.h"
#include "loop.h"
#include "brewctrl.h"
#include "comm.h"
#include "json.h"
#include "config.h"
#include "logger.h"

void handler(int sig) {
	void *array[10];
	size_t size;

	// get void*'s for all entries on the stack
	size = backtrace(array, 10);

	// print out all the frames to stderr
	fprintf(stderr, "Error: signal %d:\n", sig);
	backtrace_symbols_fd(array, size, 2);
	exit(1);
}

int main() {
	signal(SIGSEGV, handler);

	initLogFile();
	initConfiguration();

//	Configuration* configuration = getConfiguration();
//	if (configuration != NULL) {
//
//		writeConfiguration(configuration);
//	}
//	for (int i = 0; i < 100; i++) {
//		initConfiguration();
//		Configuration* configuration = getConfiguration();
//		if (configuration != NULL) {
//			char * json = formatJsonConfiguration(configuration);
//			//printf("%s\n", json);
//
//			free(json);
//		}
//
//	}

	printf("Start Comm\n");
	startComm();
	printf("Start Loop\n");
	loop();

	closeLogFile();
}

