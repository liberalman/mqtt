CC = gcc
RM = rm
DIRS=lib client src
VERSION=0.1
CFLAGS += -DVERSION=\"${VERSION}\"
TARGETS := libertymqtt

all : $(TARGETS)

$(TARGETS) : main.c
	$(CC) $(CFLAGS) $^ -o $@

clean :
	-$(RM) -f *.o
	-$(RM) -f $(TARGETS)

test : mosquitto
	-$(MAKE) -C test test
