#include <config.h> 

const int MAX_BUF = 1024;
const int MAX_ERROR = 256;

int main(int argc, char *argv[])
{
    int ret = 0;
    char buf[MAX_BUF]; // ���ݻ�����
    //char error[MAX_ERROR]; // ��������������
    //FILE *pid; // �����ļ���ָ��������
    libertymqtt_config config;
    
    #ifndef VERSION
        char VERSION[32];
        snprintf(VERSION, 32, "%s", "0.0");
    #else
        
    #endif
    
    // ����汾��Ϣ
    snprintf(buf, MAX_BUF, "version %s", VERSION);
    _log(DEBUG, "%s\n", buf);

    // ���������ļ�
    ret = _init_conf(&config, argc, argv);
    if (SUCCESS != ret)
        exit(ret);

    // �˳�ǰ��Ҫ���˹ر���־
    _log_close();
    return 0;
}
