#include <stdio.h>

const int MAX_BUF = 1024;
const int MAX_ERROR = 256;

int main(int argc, char *argv[])
{
    char buf[MAX_BUF]; // 数据缓冲区
    char error[MAX_ERROR]; // 错误描述缓冲区
    FILE *pid; // 进程文件锁指针描述符
    
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
