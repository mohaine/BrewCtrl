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

#include <ctype.h>
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
#include "sensor.h"

#ifndef DEFAULT_PORT
#define DEFAULT_PORT 80
#endif

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

int main(int argc, char **argv) {
	signal(SIGSEGV, handler);

	int showHelp = 0;
	int onlyListSensors = 0;
	int onlyTestOutput = 0;
	int startServer = 1;
	int port = DEFAULT_PORT;

	int c;
	opterr = 0;

	while ((c = getopt(argc, argv, "hlop:")) != -1)
		switch (c) {
		case 'h':
			showHelp = 1;
			startServer = 0;
			break;
		case 'l':
			onlyListSensors = 1;
			startServer = 0;
			break;
		case 'o':
			onlyTestOutput = 1;
			startServer = 0;
			break;
		case 'p':
			if (sscanf(optarg, "%i", &port) != 1) {
				fprintf(stderr, "Port is not an integer\n");
				return 1;
			}
			break;
		case '?':
			if (optopt == 'p')
				fprintf(stderr, "Option -%c requires an argument.\n", optopt);
			else if (isprint(optopt))
				fprintf(stderr, "Unknown option `-%c'.\n", optopt);
			else
				fprintf(stderr, "Unknown option character `\\x%x'.\n", optopt);
			return 1;
		default:
			showHelp = 1;
			startServer = 0;
			break;
		}

	if (showHelp) {
		fprintf(stderr, "BrewCtrl [Options]\n");
		fprintf(stderr, "         -h  Show This Message\n");
		fprintf(stderr, "         -l  List Sensors\n");
		fprintf(stderr, "         -p <PORT> Listen on Port\n");
	}

	if (onlyListSensors) {
		searchForTempSensors();
		readSensors();
		listSensors();
	} else if (onlyTestOutput) {
		testOutputs();
	} else if (startServer) {
		initBrewCtrl();
		initLogFile();
		initConfiguration();
		startComm(port);
		printf("Start Loop\n");
		loop();
		closeLogFile();
	}
}

