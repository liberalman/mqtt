/**
* @brief 配置管理
* @author 首超
* @date 2015-05-17
*/
#include <config.h>

/*
int parse_int(char **token, const char *name, int *value, char *saveptr){
    *token = strtok_r(NULL, " ", &saveptr);
	if(*token){
		*value = atoi(*token);
	}else{
		_log(ERROR, "Error: Empty %s value in configuration.", name);
		return INVALID;
	}

	return SUCCESS;
}*/

/**
 * 配置复位，设为默认值
 */
static void conf_reset(libertymqtt_config *config){
    config->daemon = false;
    _libertymqtt_free(config->config_file);
    config->config_file = NULL;
    _libertymqtt_free(config->pid_file);
    config->pid_file = NULL;
    _libertymqtt_free(config->log_file);
    config->log_file = NULL;
    config->log_type = 0x00; // 0x11111
}

static int load_conf_from_file(libertymqtt_config *config, bool reset, const char *filename, int level, int *line){
    const int MAX_LINE = 1024;
    char buf[MAX_LINE];
    FILE *fptr = NULL;
    fptr = fopen(filename, "rt");
    if(!fptr){
        _log(ERROR, "无法打开文件 %s, %s\n", filename, strerror(errno));
        return ERR_INVALID;
    }
    *line = 0;
    while(fgets(buf, MAX_LINE, fptr)){ // 每次读取一行
        (*line)++; // 行号递增
        if('#' != buf[0] && 10 != buf[0] && 13 != buf[0]){ // 略过注释#，以及换行\n和\r
            // 去除末尾的多个\n和\r的字符，都替换为0。
            while(buf[strlen(buf)-1] == 10 || buf[strlen(buf)-1] == 13){
				buf[strlen(buf)-1] = 0;
			}
            // 接下来按”=“号切割字符串
            char *token;
	        char *saveptr = NULL;
            token = strtok_r(buf, "=", &saveptr);
            //_log(INFO, "token:%s saveptr:%s\n", token, trim(saveptr));
            token = trim(token);
            if(token){
                if(!strcmp(token, "host")){ // 地址
                } else if (!strcmp(token, "log_type")) { // 日志类型
                    if(reset)
                        continue;
                    _log(INFO, "log_type %d %s\n", config->log_type, trim(saveptr));
                    if(strstr(saveptr, "INFO"))
                        config->log_type = config->log_type | INFO;
                    if(strstr(saveptr, "DEBUG"))
                        config->log_type = config->log_type | DEBUG;
                    if(strstr(saveptr, "ERROR"))
                        config->log_type = config->log_type | ERROR;
                    _log(INFO, "log_type %d\n", config->log_type);
                } else if (!strcmp(token, "port")) { // 端口的配置
                    if(reset)
                        continue;
                    int port = atoi(trim(saveptr));
                    if(port > 65535 || port<1){
                        _log(WARNING, "端口%d无效，已使用默认1883端口，端口应在1和65535之间!\n", port);
                        config->default_listener.port = 1883;
                    } else {
                        config->default_listener.port = port;
                    }
                    _log(INFO, "port %d\n", config->default_listener.port);
                } else if (!strcmp(token, "daemon")) { // 后台运行
                    if(reset)
                        continue;
                    int daemon = atoi(trim(saveptr));
                    config->daemon = (0 == daemon) ? false : true;
                    _log(INFO, "daemon %d\n", daemon);
                } else if (!strcmp(token, "pid_file")) { // 后台运行
                    if(reset)
                        continue;
                    config->pid_file = _libertymqtt_strdup(trim(saveptr)); // 申请一块新内存，获取文件名，记得要释放内存
                    if (!config->pid_file){
                        _log(ERROR, error_str[ERR_NOMEMORY]);
                        fclose(fptr);
                        return ERR_NOMEMORY;
                    }
                    _log(INFO, "pid_file %s\n", config->pid_file);
                } else if (!strcmp(token, "log_file")) { // 后台运行
                    if(reset)
                        continue;
                    config->log_file = _libertymqtt_strdup(trim(saveptr)); // 申请一块新内存，获取文件名，记得要释放内存
                    if (!config->log_file){
                        _log(ERROR, error_str[ERR_NOMEMORY]);
                        fclose(fptr);
                        return ERR_NOMEMORY;
                    }
                    _log(INFO, "log_file %s\n", config->log_file);
                }
            }
        }
    }
    fclose(fptr);
    return SUCCESS;
}

/** 静态函数，只能在该文件内部调用，不能被其他文件调用 */
static void print_usage(void){
    //printf("libertymqtt version %s (build date %s)\n\n", VERSION, TIMESTAMP);
    printf("libertymqtt version %s \n\n", VERSION);
	printf("libertymqtt is an MQTT v3.1 broker.\n\n");
	printf("Usage: libertymqtt [-c config_file] [-d] [-h] [-p port]\n\n");
	printf(" -c : specify the broker config file.\n");
	printf(" -d : put the broker into the background after starting.\n");
	printf(" -h : display this help.\n");
	printf(" -p : start the broker listening on the specified port.\n");
	printf("      Not recommended in conjunction with the -c option.\n");
	printf(" -v : verbose mode - enable all logging types. This overrides\n");
	printf("      any logging options given in the config file.\n");
	printf("\nSee https://github.com/liberalman/mqtt/tree/master/libertymqtt/ for more information.\n\n");
}

/**
 * 读取配置文件
 * @param [out] config
 * @param [in] reset 配置复位标志，true表示先把原来的配置复位为默认值，再读取配置文件。
 */
static int config_read(libertymqtt_config *config, bool reset){
    int ret = SUCCESS, line = 0;
    if (reset)
        conf_reset(config);
    ret = load_conf_from_file(config, false, config->config_file, 0, &line);
    if(ret){
        _log(ERROR, "没找到 %s:%d\n", config->config_file, line);
        return ret;
    }
    return -1;
}

int _init_conf(libertymqtt_config *config, int argc, char *argv[]) {
    _log(INFO, "%s\n",  "start ...");
    // 先设置配置为默认
    memset(config, 0, sizeof(libertymqtt_config));
    conf_reset(config);
    // 然后读取启动输入参数
    int i = 0;
    for( i = 1; i < argc; i++){
        if(! strcmp(argv[i], "-c") || !strcmp(argv[i], "--config-file")){ // 有配置文件的情况下，载入配置文件
            if (i + 1 < argc){ // 检查参数数量，如果-c之后还带有文件名，则读取文件
                config->config_file = _libertymqtt_strdup(argv[i+1]); // 申请一块新内存，获取文件名，记得要释放内存
                if (!config->config_file){
                    _log(ERROR, error_str[ERR_NOMEMORY]);
                    return ERR_NOMEMORY;
                }
                if (!config_read(config, false)){
                    _log(ERROR, "无法打开文件 %s\n", config->config_file);
                    return ERR_INVALID;
                }
            } else { // -c 之后没有带文件名，参数不全
                _log(ERROR, "您的参数 -c 或 --config-file 之后没有给定配置文件名称\n");
                return ERR_INVALID;
            }
        } else if (! strcmp(argv[i], "-d") || !strcmp(argv[i], "--daemon")){
            config->daemon = true;
        } else if (! strcmp(argv[i], "-h") || !strcmp(argv[i], "--help")){
            print_usage();
        } else if (! strcmp(argv[i], "-p") || !strcmp(argv[i], "--port")){
            if (i + 1 < argc){
                int port = atoi(argv[i+1]);
                if (port < 1 || port > 65535) {
                    _log(ERROR, "端口 %d 不可用，注意端口要在1和65535之间，且不可被占用\n", port);
                    return ERR_INVALID;
                } else {
                    config->default_listener.port = port;
                }
            } else { // -p 之后没有带端口，参数不全
                _log(ERROR, "您的参数 -p 或 --port 之后没有端口\n");
                return ERR_INVALID;
            }
        }
    }
    _log(INFO, "end!\n");
    return SUCCESS;
}

void _clear_all(libertymqtt_config *config){
    _log(INFO, "start ...\n");
    _libertymqtt_free(config->config_file);
    _libertymqtt_free(config->log_file);
    _libertymqtt_free(config->pid_file);
    _log(INFO, "end\n");
}