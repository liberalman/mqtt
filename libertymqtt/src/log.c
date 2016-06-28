/**************************************************************************/
/** 
* @brief 日志模块
* @details 
* @addtogroup log 日志
* @author 首超
* @version 0.01 
* @date 2015/05/17 
****************************************************************************** 
* Copyright (c), 2015, Liberty Co., Ltd. 
****************************************************************************** 
* Edit History /n 
* -------------------------------------------------------------------------/n 
* DATE NAME DESCRIPTION /n 
* 2015/05/17 首超 Create./n 
******************************************************************************
* @{
*****************************************************************************/
#include <config.h>

#ifdef WIN32
#include <windows.h>
#include <io.h>
#else
#include <unistd.h>
#include <sys/time.h>
#include <pthread.h>
#define  mutex   pthread_mutex_t
#define  _vsnprintf         vsnprintf
#endif

/** @brief 日志大小 */
#define MAXLINSIZE 16000
#include <time.h>
#include <sys/timeb.h>
#include <stdarg.h>

char logfilename[64] = "libertymqtt_1.log";
static char logstr[MAXLINSIZE+1];
mutex mutex_log;
FILE *fptr;

static uint8_t _is_init = 0;
//static log_type_t _log_type = NONE | INFO | WARNING | DEBUG | ERROR ;
static log_type_t _log_type = ERROR ;

#ifdef WIN32
void Lock(mutex *l) {
    EnterCriticalSection(l);
}
void Unlock(mutex *l) {
    LeaveCriticalSection(l);
}
#else
void Lock(mutex *l) {
    pthread_mutex_lock(l);
}
void Unlock(mutex *l) {
    pthread_mutex_unlock(l);
}
#endif

static char* log_str(log_type_t log_type){
    static char log_type_str[5][7] = {"","INFO ","WARNING","DEBUG","ERROR"};
    switch(log_type){
        case INFO:
        return log_type_str[1];
        case WARNING:
        return log_type_str[2];
        case DEBUG:
        return log_type_str[3];
        case ERROR:
        return log_type_str[4];
        default:
            return log_type_str[0];
    }
}

int _init_log(const char* filename, log_type_t log_type){
    _log_type = log_type;
    //printf("log_type %d\n", log_type);
    if(NULL != fptr)
        fclose(fptr);
    snprintf(logfilename, 64, "%s", filename);
    fptr = fopen(filename, "a");
    if (NULL==fptr) {
        printf("打开文件%s失败,%s\n", filename, strerror(errno));
        exit(-3);
    }
#ifdef WIN32
    InitializeCriticalSection(&mutex_log);
#else
    pthread_mutex_init(&mutex_log,NULL);
#endif
    _is_init = 1;
    return 0;
}

void _init_log_1(){
    _init_log(logfilename, _log_type);
}

void _get_logtime(char *buf){
    struct tm *now;
    struct timeb tb;
    ftime(&tb);
    now = localtime(&tb.time);
    snprintf(buf, 23, "%04d-%02d-%02d %02d:%02d:%02d.%03d", now->tm_year+1900,now->tm_mon+1,now->tm_mday, now->tm_hour, now->tm_min  , now->tm_sec, tb.millitm);
}

void _log_printf(log_type_t log_type, const char *externs, const char *fmt, ...){
    if (NULL==fmt||0==fmt[0])
            return;
    if (_log_type & log_type){ // 匹配打印级别
        if(!_is_init)
            _init_log_1();
        va_list argp;
        Lock(&mutex_log);
        va_start(argp,fmt);

        _vsnprintf(logstr,MAXLINSIZE,fmt,argp);
        printf("%s|%s|%s", log_str(log_type), externs, logstr);
        if (NULL!=fptr) {
            fprintf(fptr,"%s|%s|%s", log_str(log_type), externs, logstr);
        } else {
            _init_log_1();
        }

        va_end(argp);
        Unlock(&mutex_log);
    }
}

int _log_close(){
    _is_init = 0;
#ifdef WIN32
    DeleteCriticalSection(&mutex_log);
#else
    pthread_mutex_destroy(&mutex_log);
#endif
    if (NULL != fptr)
        fclose(fptr);
    return 0;
}
/** @}***********************************************************/