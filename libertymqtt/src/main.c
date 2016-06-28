#include <config.h> 

/** 错误码对应文字说明 */
static char error_str[5][64] = {
    "成功 ",
    "内存申请失败，无可用内存",
    "不可用",
    "socket不可用",
    "创建线程失败"};

void daemon_mode(const libertymqtt_config* config){
    _log(INFO, "daemon_mode() start ...\n");
    if (config->daemon && config->pid_file){
        FILE* pid = _libertymqtt_fopen(config->pid_file, "wt");
        if(pid){
            fprintf(pid, "%d", getpid()); // 写入进程id到进程锁文件中。
            fclose(pid); // 不要忘记关闭文件
        } else {
            _log(ERROR, "不能写入进程锁文件pid");
            exit(-2); // 退出程序
        }
    }
    _log(INFO, "daemon_mode() end\n");
}

int main(int argc, char *argv[])
{
    int ret = 0/*, i = 0*/;
    char buf[MAX_BUF]; // 数据缓冲区
    //char error[MAX_ERROR]; // 错误描述缓冲区
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

    // 初始化日志
    _init_log(config.log_file, config.log_type);

    // daemon方式启动
    daemon_mode(&config);

    // 创建socket，监听端口
    libertymqtt_listen(&config.default_listener);
    //for (i = 0; i < config.listener_count; i++){
    //}

    // 主循环
    libertymqtt_main_loop(&config);

    // 退出前不要忘了清理配置、关闭日志等
    _clear_all(&config);
    _log_close();
    return 0;
}
