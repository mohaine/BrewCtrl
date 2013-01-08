// Standard Linux heders
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <getopt.h>
#include <unistd.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/resource.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <limits.h>
#include <sys/time.h>
#include <stdarg.h>

#include "logger.h"
#include "brewctrl.h"

#define LOG_FILE "log.txt"

unsigned long starttimeus;

#ifdef __DEBUG
FILE* logFp = NULL;
#endif

void initLogFile() {
	struct timeval tv;
	gettimeofday(&tv, NULL);
	starttimeus = tv.tv_sec * 1000000 + tv.tv_usec;

#ifdef __DEBUG
	logFp = fopen(LOG_FILE, "w");
	if (logFp == NULL) {
		fprintf(stderr,"Failed to open log file %s for writing\n", LOG_FILE);
	}
#endif
}
void closeLogFile() {

#ifdef __DEBUG
	if (logFp) {
		fclose(logFp);
	}
#endif
}

void logError(const char *fmt, ...) {
	va_list args;
	va_start(args, fmt);
	char* logFmt;

	struct timeval tv;
	if (gettimeofday(&tv, NULL) == -1) {
		ERR("Failed to get os time\n");
		return;
	}
	unsigned long microsec = tv.tv_sec * 1000000 + tv.tv_usec;

	microsec = microsec - starttimeus;

	logFmt = malloc(strlen(fmt) + 100);
	if (logFmt == NULL) {
		fprintf(stderr, "malloc fail in logMessage\n");
		return;
	}
	sprintf(logFmt, "%lu\t%s", microsec, fmt);
	(void) vfprintf(stderr, logFmt, args);

#ifdef __DEBUG
	if(logFp) {
		(void) vfprintf(logFp, logFmt, args);
	}
#endif

	free(logFmt);

#ifdef __DEBUG
	fflush(logFp);
#endif
	va_end(args);
}

#ifdef __DEBUG

void logMessage(const char *fmt, ...) {
	va_list args;
	va_start(args, fmt);
	char* logFmt;

	struct timeval tv;
	if (gettimeofday(&tv, NULL) == -1) {
		ERR("Failed to get os time\n");
		return;
	}
	unsigned long microsec = tv.tv_sec * 1000000 + tv.tv_usec;

	microsec = microsec - starttimeus;

	logFmt = malloc(strlen(fmt) + 100);
	if (logFmt == NULL) {
		fprintf(stderr, "malloc fail in logMessage\n");
		return;
	}

	sprintf(logFmt,"%lu\t%s", microsec, fmt);

	(void) vfprintf(stderr, logFmt, args);

	if(logFp) {
		va_end(args);
		va_start(args, fmt);
		(void) vfprintf(logFp, logFmt, args);
		fflush(logFp);
	}

	free(logFmt);

	va_end(args);
}
#endif

