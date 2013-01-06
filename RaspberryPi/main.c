/* Hello World program */

#include <stdio.h>
#include <unistd.h>

#include "duty.h"
#include "loop.h"
#include "brewctrl.h"
#include "comm.h"
#include "json.h"
#include "config.h"

int main() {

	initConfiguration();
	Configuration* configuration = getConfiguration();
	if (configuration != NULL) {
		printf("Valid CONFIG\n");
		printf("%s\n", formatJsonConfiguration(configuration));
	} else {
		printf("NULL CONFIG\n");
	}

	//printf("Start Comm\n");
	//startComm();
	//printf("Start Loop\n");
	//loop();

}

