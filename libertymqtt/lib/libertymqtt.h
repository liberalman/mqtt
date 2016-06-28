#ifndef LIBERTYMQTT_H
#define LIBERTYMQTT_H

#include "libertymqtt_internal.h"

/** 日志类型 */
typedef enum {
    NONE         = 0x01, /**<  不打印日志，二进制00001*/
    INFO           = 0x02, /**<  信息， 二进制00010*/
    WARNING  = 0x04, /**<  警告， 二进制00100*/
    DEBUG       = 0x08, /**<  调试， 二进制01000*/
    ERROR       = 0x10   /**<  错误，二进制10000*/
} log_type_t;

/** 错误码 */
typedef enum {
    ERR_CONN_PENDING = -1, /**<  连接等待 */
    SUCCESS = 0, /**<  成功 */
    ERR_NOMEMORY = 1, /**< 内存申请失败，无可用内存 */
    ERR_INVALID = 2, /**< 不可用 */
    ERR_INVALID_SOCKET = 3, /**< socket不可用 */
    ERR_CREATE_THREAD = 4 /**< 创建线程失败 */
} libertymqtt_error_t;

struct libertymqtt;

#endif
