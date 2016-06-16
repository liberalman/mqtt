#include <stdio.h>

const int MAX_BUF = 1024;
const int MAX_ERROR = 256;

int main(int argc, char *argv[])
{
    char buf[MAX_BUF]; // ���ݻ�����
    char error[MAX_ERROR]; // ��������������
    FILE *pid; // �����ļ���ָ��������
    
    #ifndef VERSION
        char VERSION[32];
        snprintf(VERSION, 32, "%s", "0.0");
    #else
        
    #endif
    
    // 
    snprintf(buf, MAX_BUF, "version %s", VERSION);
    printf("%s\n", buf);
    return 0;
}
