/* Hello World program */

#include <stdio.h>
#include <unistd.h>
#include <execinfo.h>
#include <signal.h>
#include <stdlib.h>

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

	for (int i = 0; i < 1000; i++) {
		initConfiguration();
		Configuration* configuration = getConfiguration();
		if (configuration != NULL) {
			char * json = formatJsonConfiguration(configuration);
			//printf("%s\n", json);

			free(json);
		}

	}

	closeLogFile();

	//printf("Start Comm\n");
	//startComm();
	//printf("Start Loop\n");
	//loop();

}

