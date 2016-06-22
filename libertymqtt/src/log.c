/**
* @file 日志
* @author 首超
* @date 2015-05-17
*/
#include <config.h>

#ifdef WIN32
#include <windows.h>
#include <io.h>
#else
#include <unistd.h>
#include <sys/time.h>
#include <pthread.h>
#define  CRITICAL_SECTION   pthread_mutex_t
#define  _vsnprintf         vsnprintf
#endif

/** @brief 日志大小 */
#define MAXLOGSIZE 20000000
#define MAXLINSIZE 16000
#include <time.h>
#include <sys/timeb.h>
#include <stdarg.h>
char logfilename1[]="libertymqtt_1.log";
char logfilename2[]="libertymqtt_2.log";
static char logstr[MAXLINSIZE+1];
char datestr[16];
char timestr[16];
char mss[4];
CRITICAL_SECTION cs_log;
FILE *pf;

//extern static libertymqtt_config _config;
static uint8_t _is_init = 0;
static log_level_t _log_level = NONE | INFO | WARNING | DEBUG | ERROR ;
static char log_level_str[5][7] = {"NONE","INFO ","WARNING","DEBUG","ERROR"};

#ifdef WIN32
void Lock(CRITICAL_SECTION *l) {
    EnterCriticalSection(l);
}
void Unlock(CRITICAL_SECTION *l) {
    LeaveCriticalSection(l);
}
#else
void Lock(CRITICAL_SECTION *l) {
    pthread_mutex_lock(l);
}
void Unlock(CRITICAL_SECTION *l) {
    pthread_mutex_unlock(l);
}
#endif

int init(){
    _is_init = 1;
#ifdef WIN32
    InitializeCriticalSection(&cs_log);
#else
    pthread_mutex_init(&cs_log,NULL);
#endif
    return 0;
}
/*
void print(const char *pszFmt,va_list argp) {
struct tm *now;
struct timeb tb;

if (NULL==pszFmt||0==pszFmt[0])
return;
_vsnprintf(logstr,MAXLINSIZE,pszFmt,argp);
ftime(&tb);
now = localtime(&tb.time);
sprintf(datestr,"%04d-%02d-%02d",now->tm_year+1900,now->tm_mon+1,now->tm_mday);
sprintf(timestr,"%02d:%02d:%02d",now->tm_hour, now->tm_min  ,now->tm_sec );
sprintf(mss,"%03d",tb.millitm);
printf("%s %s.%s %s",datestr,timestr,mss,logstr);
pf=fopen(logfilename1,"a");
if (NULL!=pf) {
fprintf(pf,"%s %s.%s %s",datestr,timestr,mss,logstr);
if (ftell(pf)>MAXLOGSIZE) {
fclose(pf);
if (rename(logfilename1,logfilename2)) {
remove(logfilename2);
rename(logfilename1,logfilename2);
}
} else {
fclose(pf);
}
}
}*/
void _get_logtime(char *buf){
    struct tm *now;
    struct timeb tb;
    ftime(&tb);
    now = localtime(&tb.time);
    snprintf(buf, 23, "%04d-%02d-%02d %02d:%02d:%02d.%03d", now->tm_year+1900,now->tm_mon+1,now->tm_mday, now->tm_hour, now->tm_min  , now->tm_sec, tb.millitm);
}

void _log_printf(log_level_t level, const char *externs, const char *fmt, ...){
    if (_log_level & level){ // 匹配打印级别
        if(!_is_init)
            init();
        va_list argp;
        Lock(&cs_log);
        va_start(argp,fmt);

        if (NULL==fmt||0==fmt[0])
            return;
        _vsnprintf(logstr,MAXLINSIZE,fmt,argp);
        printf("%s|%s|%s", log_level_str[level], externs, logstr);
        pf=fopen(logfilename1, "a");
        if (NULL!=pf) {
            fprintf(pf,"%s|%s|%s", log_level_str[level], externs, logstr);
            if (ftell(pf)>MAXLOGSIZE) {
                fclose(pf);
                if (rename(logfilename1,logfilename2)) {
                    remove(logfilename2);
                    rename(logfilename1,logfilename2);
                }
            } else {
                fclose(pf);
            }
        }

        va_end(argp);
        Unlock(&cs_log);
    }
}

int _log_close(){
    _is_init = 0;
#ifdef WIN32
    DeleteCriticalSection(&cs_log);
#else
    pthread_mutex_destroy(&cs_log);
#endif
    if (NULL != pf)
        fclose(pf);
    return 0;
}