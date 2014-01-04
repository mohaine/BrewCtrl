CC=gcc 
CFLAGSD=  

SRCDIR=src
OBJDIR=obj

CFGFILE = BrewControllerConfig.json

CFLAGS=-c -Wall -std=c99 -D_GNU_SOURCE $(CFLAGSD)  -lpthread
LDFLAGS=-lpthread -lm 
SOURCES= $(wildcard $(SRCDIR)/*.c) 
OBJECTS  := $(SOURCES:$(SRCDIR)/%.c=$(OBJDIR)/%.o)


BREWCTRL=brewctrl

#LDFLAGS += -g -rdynamic -finstrument-functions
#CFLAGS += -g -rdynamic -finstrument-functions

#CFLAGSD+= -DMOCK -DDEFAULT_PORT=2739
#CFLAGSD+= -D__DEBUG -D__DEBUG_FILE
#CFLAGSD+= -D__DEBUG

-include Makefile.local


CFLAGS += -O2
LDFLAGS += -O2

CFLAGS += $(shell pkg-config --cflags json)
LDFLAGS += $(shell pkg-config --libs json)

all: $(SOURCES) $(BREWCTRL) $(LISTSENSORS)
	
$(BREWCTRL): create-dir $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@ 

$(OBJECTS): $(OBJDIR)/%.o : $(SRCDIR)/%.c
	$(CC) $(CFLAGS) -c $< -o $@   

default: all
	
	
create-dir:
	test -d $(OBJDIR) || mkdir $(OBJDIR)
	test -f $(CFGFILE) || cp $(CFGFILE).dist $(CFGFILE)

clean: 
	rm -f $(BREWCTRL) $(OBJDIR)/*.o	
	! test -d $(OBJDIR) || rmdir $(OBJDIR)
