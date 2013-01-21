/* Hello World program */

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


int main() {

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

