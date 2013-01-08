#ifndef _LOGGER_H
#define _LOGGER_H

// Enables or disables debug output
#if __DEBUG
#define DBG(fmt, args...) logMessage(fmt, ## args)
#else
#define DBG(fmt, args...)
#endif

#define ERR(fmt, args...) logError(fmt, ## args)

#if __DEBUG
void logMessage(const char *fmt, ...);
void logError(const char *fmt, ...);
#endif

void logError(const char *fmt, ...);
void initLogFile();
void closeLogFile();

#endif
