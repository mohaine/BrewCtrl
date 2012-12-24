/* Hello World program */

#include <stdio.h>
#include <unistd.h>

#include "duty.h"
#include "loop.h"
#include "brewctrl.h"
#include "comm.h"

int main() {

	printf("Start Comm\n");
	startComm();
	printf("Start Loop\n");
	loop();

}

