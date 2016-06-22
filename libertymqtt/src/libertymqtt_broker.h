#ifndef LIBERTYMQTT_BROKER_H
#define LIBERTYMQTT_BROKER_H

#include <config.h>


/**
 * 保存订阅某一个topic的所有用户的双向链表
 */
typedef struct {
    struct _libertymqtt_subleaf *prev;
    struct _libertymqtt_subleaf *next;
    libertymqtt *context; /**< 订阅客户端的用户内容 */
    int qos;
} _libertymqtt_subleaf ;

/**
 * 用于保存订阅树的节点的数据结构，孩子-兄弟链表法
 */
typedef struct {
    struct _libertymqtt_subhier *children; /**< 孩子节点的头指针，第一个孩子节点。 */
    struct _libertymqtt_subhier *next; /**< 下一个兄弟节点 */
    struct _libertymqtt_subleaf *subs; /**< 订阅列表 */
    char* topic; /**< 订阅主题 */
    struct _libertymqtt_msg_store *retained;
} _libertymqtt_subhier;

///////////////////////////////////////////////////////////////////// 配置相关 //////////////////////////////////////////////////////////////////
/** 配置文件  */
typedef struct {
    char *config_file;  /**< 配置文件名 */
    char *log_file;  /**< 日志文件名 */
    FILE *log_fptr;  /**< 配置文件FILE指针 */
    char *pid_file; /**< 进程锁文件名 */
    bool daemon;  /**< 后台运行 */
    int port;
} libertymqtt_config;

/**
 * 初始化配置，读取配置文件 
 * @param [out] config 输出读取的配置结果
 * @param [in] argc 程序启动输入参数数量
 * @param [in] argv 程序启动输入参数列表
 * @return 错误码
 */
int _init_conf(libertymqtt_config *config, int argc, char *argv[]);

///////////////////////////////////////////////////////////////////// 日志相关 //////////////////////////////////////////////////////////////////
/**  日志输出终端 */
#define LOG_STDOUT 0x04

/**
 * 打印日志函数 
 * @param [in] level 日志级别，可选如下：
 * NONE 不打印
 * INFO 信息
 * WARNING 警告
 * DEBUG 调试
 * ERROR 错误
 * @param [in] fmt 格式
 * @param [in] ... 可变参数
 * @return
 */
void _log_printf(log_level_t level, const char *externs, const char *fmt, ...) __attribute__((format(printf, 3, 4)));

/**
 * 增加日志打印时间、所在文件名、所在行、父函数名称等信息到日志行前缀中
 * @param [out] buf 输出类似于"DEBUG|2016-06-22 15:11:45.10|src/main.c:23|main()|"这样的日志行前缀
 */
void _get_logtime(char *buf);

/**
 * 关闭日志
 */
int _log_close();
 
#endif
