/**
* @file 配置管理
* @author 首超
* @date 2015-05-17
*/
#include <config.h>

/** 错误码对应文字说明 */
static char error_str[5][64] = {
    "NONE",
    "成功 ",
    "服务异常",
    "内存申请失败，无可用内存",
    "ERROR"};

int parse_int(char **token, const char *name, int *value, char *saveptr){
    *token = strtok_r(NULL, " ", &saveptr);
	if(*token){
		*value = atoi(*token);
	}else{
		_log(ERROR, "Error: Empty %s value in configuration.", name);
		return INVALID;
	}

	return SUCCESS;
}

/**
 * 去掉两头空格
 */
char *tr(char *s)
{
  int i = 0;
  int j = strlen ( s ) - 1;
  int k = 0;
  while ( isspace ( s[i] ) && s[i] != '\0' )
    i++;
  while ( isspace ( s[j] ) && j >= 0 )
    j--;
  while ( i <= j )
    s[k++] = s[i++];
  s[k] = '\0';
  return s;
}

/**
 * 配置复位，设为默认值
 */
void conf_reset(libertymqtt_config *config){
    config->daemon = false;
    config->config_file = NULL;
    config->pid_file = NULL;
}

int load_conf_from_file(libertymqtt_config *config, bool reset, const char *filename, int level, int *line){
    int ret = 0;
    const int MAX_LINE = 1024;
    char buf[MAX_LINE];
    FILE *fptr = NULL;
    fptr = fopen(filename, "rt");
    if(!fptr){
        _log(ERROR, "无法打开文件 %s, %s\n", filename, strerror(errno));
        return 1;
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
            _log(INFO, "token:%s saveptr:%s\n", token, tr(saveptr));
            token = tr(token);
            if(token){
                if(!strcmp(token, "host")){ // 地址
                } else if (!strcmp(token, "port")) { // 端口的配置
                    if(reset)
                        continue;
                    /*if(parse_int(&token, "port", &config->port, saveptr))
                        return INVALID;*/
                    config->port = atoi(tr(saveptr));
                    if(config->port > 65535 || config->port<1){
                        _log(WARNING, "端口%d无效，已使用默认1883端口，端口应在1和65535之间!\n", config->port);
                        config->port = 1883;
                    }
                    _log(INFO, "port %d\n", config->port);
                } else if (!strcmp(token, "log_type")) { // 日志开关
                }
            }
        }
    }
    fclose(fptr);
    return SUCCESS;
}

/**
 * 读取配置文件
 * @param [out] config
 * @param [in] reset 配置复位标志，true表示先把原来的配置复位为默认值，再读取配置文件。
 */
int config_read(libertymqtt_config *config, bool reset){
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
    _log(INFO, "%s\n",  "init config start ...");
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
                    _log(ERROR, error_str[NOMEMORY]);
                    return NOMEMORY;
                }
                if (!config_read(config, false)){
                    _log(ERROR, "无法打开文件 %s\n", config->config_file);
                    return INVALID;
                }
            } else { // -c 之后没有带文件名，参数不全
                _log(ERROR, "您的参数 -c 或 --config-file 之后没有给定配置文件名称\n");
                return INVALID;
            }
        } else if (! strcmp(argv[i], "-d") || !strcmp(argv[i], "--daemon")){
            config->daemon = true;
        } else if (! strcmp(argv[i], "-p") || !strcmp(argv[i], "--port")){
            if (i + 1 < argc){
                int port = atoi(argv[i+1]);
                if (port < 1 || port > 65535) {
                    _log(ERROR, "端口 %d 不可用，注意端口要在1和65535之间，且不可被占用\n", port);
                    return INVALID;
                } else {
                    
                    


                }
            } else { // -p 之后没有带端口，参数不全
                _log(ERROR, "您的参数 -p 或 --port 之后没有端口\n");
                return INVALID;
            }
        }
    }
    _log(INFO, "init config end!\n");
    return SUCCESS;
}