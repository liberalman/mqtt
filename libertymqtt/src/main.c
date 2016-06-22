#include <config.h> 

const int MAX_BUF = 1024;
const int MAX_ERROR = 256;

int main(int argc, char *argv[])
{
    int ret = 0;
    char buf[MAX_BUF]; // 数据缓冲区
    //char error[MAX_ERROR]; // 错误描述缓冲区
    //FILE *pid; // 进程文件锁指针描述符
    libertymqtt_config config;
    
    #ifndef VERSION
        char VERSION[32];
        snprintf(VERSION, 32, "%s", "0.0");
    #else
        
    #endif
    
    // 输出版本信息
    snprintf(buf, MAX_BUF, "version %s", VERSION);
    _log(DEBUG, "%s\n", buf);

    // 载入配置文件
    ret = _init_conf(&config, argc, argv);
    if (SUCCESS != ret)
        exit(ret);

    // 退出前不要忘了关闭日志
    _log_close();
    return 0;
}
