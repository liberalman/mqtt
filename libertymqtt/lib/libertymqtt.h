#ifndef LIBERTYMQTT_H
#define LIBERTYMQTT_H

#include "libertymqtt_internal.h"

/** 日志级别 */
typedef enum {
    NONE = 0, /**<  不打印日志 */
    INFO = 1, /**<  信息 */
    WARNING = 2, /**<  警告 */
    DEBUG = 3, /**<  调试 */
    ERROR = 4 /**<  错误 */
} log_level_t;

/** 错误码 */
typedef enum {
    CONN_PENDING = -1, /**<  连接等待 */
    SUCCESS = 0, /**<  警告 */
    NOMEMORY, /**< 内存申请失败，无可用内存 */
    INVALID = 3 /**< 不可用 */
} libertymqtt_error_t;

struct libertymqtt;

#endif
