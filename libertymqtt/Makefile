CC = gcc
RM = rm
DIRS=lib client src
DIR_INC = lib src
DIR_SRC = ./src
DIR_OBJ = ./obj
DIR_BIN = ./bin
SRC = $(wildcard ${DIR_SRC}/*.c)
OBJ = $(patsubst %.c,${DIR_OBJ}/%.o,$(notdir ${SRC}))
CFLAGS = -g -Wall -I${DIR_INC}
TARGET = libertymqtt
BIN_TARGET = ${DIR_BIN}/${TARGET}

${BIN_TARGET}:${OBJ}
	$(CC) $(OBJ)  -o $@
    
${DIR_OBJ}/%.o:${DIR_SRC}/%.c
	$(CC) $(CFLAGS) -c  $< -o $@
.PHONY:clean
clean:
	find ${DIR_OBJ} -name *.o -exec rm -rf {}