#ifndef CONFIG_H
#define CONFIG_H

#include <stdio.h>
#include <stdarg.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <stdint.h>
#include <errno.h>
#include <assert.h>
#include <ctype.h>
#include <unistd.h>

/** 定义bool类型 */
typedef enum {false = 0,true = !false} bool;
/** 自定义内存操作函数和数据结构 */
#include <memory.h>
/** 这个要在libertymqtt_broker.h前面 */
#include <libertymqtt.h>
#include <libertymqtt_broker.h>
#include <utils.h>
#include <net.h>

#define MAX_BUF 1024
#define MAX_ERROR 256

#ifdef _DEBUG
    char log_time[24];
    char log_extens[64];
    #define _log(log_type, format, args...) \
    _get_logtime(log_time);\
    snprintf(log_extens, 63, "%s|%s:%d|%s()", log_time, __FILE__, __LINE__, __FUNCTION__);\
    _log_printf(log_type, log_extens, format, ##args);
#else
    _log(log_type, format, args...)  _log_printf(log_type, "", format, ##args);
#endif

static char error_str[5][64] ;
static int run = -1;

#endif // CONFIG_H
