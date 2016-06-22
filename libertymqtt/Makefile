VERSION=1.0
CC = gcc
RM = rm
DIR_INC = lib -Isrc
DIR_SRC = ./src
DIR_SRC1 = ./lib
DIR_OBJ = ./obj
DIR_BIN = ./bin
TARGET = libertymqtt
BIN_TARGET = ${DIR_BIN}/${TARGET}
CFLAGS =  -D_DEBUG -g -Wall -I${DIR_INC} -DVERSION=\"${VERSION}\"

SRC = $(wildcard ${DIR_SRC}/*.c) $(wildcard ${DIR_SRC1}/*.c)
$(warning ${SRC}) #打印变量SRC
OBJ = $(patsubst %.c,${DIR_OBJ}/%.o,$(notdir ${SRC}))
$(warning ${OBJ}) #打印变量OBJ

${BIN_TARGET}:${OBJ}
	$(CC) $(OBJ)  -o $@
    
${DIR_OBJ}/%.o:${DIR_SRC}/%.c
	$(CC) $(CFLAGS) -c  $< -o $@
${DIR_OBJ}/%.o:${DIR_SRC1}/%.c
	$(CC) $(CFLAGS) -c  $< -o $@
.PHONY:clean
clean:
	-$(RM) -f ${DIR_OBJ}/*.o
	-$(RM) -f ${DIR_BIN}/${TARGET}
