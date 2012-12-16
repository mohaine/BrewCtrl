/* Hello World program */

#include<stdio.h>
#include <unistd.h>

#include "duty.h"
#include "loop.h"
#include "brewctrl.h"

int main() {
	int i = 0;

	printf("Hello World\n");

	for (i = 0; i < 10; i++) {

		if (i > 0)
			sleep(1);
		printf(" millis: %lu\n", millis());

	}

	return 0;

}

