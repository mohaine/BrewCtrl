/* Hello World program */

#include<stdio.h>
#include <unistd.h>

#include "duty.h"
#include "loop.h"
#include "brewctrl.h"
#include "comm.h"

int main() {
	/*
	 char out[20000];
	 char data[] =
	 "layout=%7B%22tanks%22%3A%5B%7B%22name%22%3A%22HLT%22%2C%22sensor%22%3A%7B%22name%22%3Anull%2C%22reading%22%3Atrue%2C%22tempatureC%22%3A21.125%2C%22address%22%3A%22289c167d02000048%22%7D%2C%22heater%22%3A%7B%22name%22%3A%22HTL+Element%22%2C%22pin%22%3A9%2C%22duty%22%3A0%2C%22canPulse%22%3Atrue%2C%22mode%22%3Anull%2C%22fullOnAmps%22%3A18%7D%7D%2C%7B%22name%22%3A%22TUN%22%2C%22sensor%22%3A%7B%22name%22%3Anull%2C%22reading%22%3Afalse%2C%22tempatureC%22%3A0.0%2C%22address%22%3Anull%7D%2C%22heater%22%3Anull%7D%2C%7B%22name%22%3A%22Kettle%22%2C%22sensor%22%3Anull%2C%22heater%22%3A%7B%22name%22%3A%22Kettle+Element%22%2C%22pin%22%3A7%2C%22duty%22%3A0%2C%22canPulse%22%3Atrue%2C%22mode%22%3Anull%2C%22fullOnAmps%22%3A23%7D%7D%5D%2C%22pumps%22%3A%5B%7B%22name%22%3A%22HLT+Loop%22%2C%22pin%22%3A8%2C%22duty%22%3A0%2C%22canPulse%22%3Afalse%7D%5D%2C%22maxAmps%22%3A25%2C%22turnOffOnCommLoss%22%3Atrue%7\r\ntest=test%22%3AAfter";

	 int size = readParam("layout", &data, sizeof(data), &out);

	 printf("Size: %d\n", size);

	 if (size > 0) {
	 printf("Param: '%s'\n", out);
	 }
	 */

	printf("Start Comm\n");
	startComm();
	printf("Start Loop\n");
	loop();

}

