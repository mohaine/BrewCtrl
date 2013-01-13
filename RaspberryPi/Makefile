CC=gcc 
CFLAGSD=  


CFLAGS=-c -Wall -std=c99 $(CFLAGSD) 
LDFLAGS=-lpthread   -lm 
SOURCES= pid.c duty.c brewctrl.c comm.c  loop.c control.c sensor.c config.c logger.c
OBJECTS=$(SOURCES:.c=.o) 
BREWCTRL=brewctrl
LISTSENSORS=listsensors

#LDFLAGS += -g -O0
#CFLAGS += -g -O0
#CFLAGSD+= -DMOCK
#CFLAGSD+= -D__DEBUG

CFLAGS += -O2
LDFLAGS += -O2

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