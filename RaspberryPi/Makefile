CC=gcc 
CFLAGSD=  
 
CFLAGSD+=-DMOCK 

CFLAGS=-c -Wall -std=c99 $(CFLAGSD)-g 
LDFLAGS=-lpthread  
SOURCES= pid.c duty.c brewctrl.c comm.c crc8.c loop.c control.c sensor.c 
OBJECTS=$(SOURCES:.c=.o) 
BREWCTRL=brewctrl
LISTSENSORS=listsensors

CFLAGS += -g -O0
LDFLAGS += -g -O0


CFLAGS += $(shell pkg-config --cflags json)
LDFLAGS += $(shell pkg-config --libs json)

all: $(SOURCES) $(BREWCTRL) $(LISTSENSORS)
	
$(LISTSENSORS): $(OBJECTS) listsensors.o
	$(CC) $(LDFLAGS) $(OBJECTS) listsensors.o -o $@

$(BREWCTRL): $(OBJECTS) main.o
	$(CC) $(LDFLAGS) $(OBJECTS) main.o -o $@ 

.c.o:
	$(CC) $(CFLAGS) $< -o $@

brewctrlrun: $(BREWCTRL)
	./$(BREWCTRL)
	
listrun: $(LISTSENSORS)
	./$(LISTSENSORS)
	

	
default: all
	
clean: 
	rm -f $(BREWCTRL) $(LISTSENSORS) *.o	