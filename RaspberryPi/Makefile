CC=gcc
CFLAGS=-c -Wall -std=c99 
LDFLAGS=
SOURCES=main.c pid.c duty.c brewctrl.c comm.c crc8.c loop.c control.c sensor.c
OBJECTS=$(SOURCES:.c=.o)
EXECUTABLE=brewctrl

all: $(SOURCES) $(EXECUTABLE)
	
$(EXECUTABLE): $(OBJECTS) 
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.c.o:
	$(CC) $(CFLAGS) $< -o $@

run: all
	./$(EXECUTABLE)
	
	
default: all
	
clean: 
	rm -f $(EXECUTABLE) *.o	