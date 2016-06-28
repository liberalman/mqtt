#include <config.h>
#include <pthread.h>
#include "sys/epoll.h"
#include <fcntl.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <time_libertymqtt.h>

int epoll_fd = 0;
int listenfd = 0;
struct epoll_event events[1024];
#define THREAD_MAX 2
// 线程池参数
static pthread_t               tds[THREAD_MAX]; // 线程池
//static unsigned int            thread_paras[THREAD_MAX][8]; // 线程池参数，每个线程对应一个参数数组，每个数组从0~7位置分别为
// thread_paras[0] = 1 线程占用，0空闲；
// thread_paras[1] 客户端fd
// thread_paras[2] 监听索引
// thread_paras[7] 线程索引编号

typedef struct {
    uint8_t free; // 1 线程占用，0空闲；
    int client_fd; // 客户端fd
    int thread_id; // 线程编号
    pthread_mutex_t mutex; // 线程锁
    char *buf; // 读或写的缓冲区
} thread_para_t;
static thread_para_t            thread_paras[THREAD_MAX]; // 线程池参数，每个线程对应一个参数数组，每个数组从0~7位置分别为


static pthread_mutex_t    mutexs[THREAD_MAX]; // 线程池锁，每个线程一把锁


static void *worker(thread_para_t *thread_para){
    int pool_index; // 本线程在线程池中的索引编号
    int sock_client; // 客户端fd
    int listen_index;  // 监听索引
    int len = 0;
    char buf[4096];    // 传输缓冲区
    // 线程脱离创建者
    pthread_detach(pthread_self());
    pool_index = thread_para->thread_id;
    _log(INFO, "worker %d\n", pool_index);
    while(1){
        pthread_mutex_lock(&thread_para->mutex); // 互斥访问任务队列，等待主线程的解锁操作
        sock_client = thread_para->client_fd;
        
        // 接收请求串
        len = recv(sock_client, buf, 4096, MSG_NOSIGNAL);
        if(len > 0){
            snprintf(buf, 4096, "Hello, I'm worker %d\n", pool_index);
            // 发送响应
            send(sock_client, buf, 4096, MSG_NOSIGNAL);
        }
        //shutdown(sock_client, SHUT_RDWR);
        //close(sock_client);
        
        /*if(NULL != thread_para->buf){
            _log(INFO, "read:%s\n", thread_para->buf);
            snprintf(thread_para->buf, 4096, "Hello, I'm worker %d\n", pool_index);
            struct epoll_event event;
            memset(&event, 0, sizeof(event));
            event.data.ptr = thread_para->buf;
            event.events = EPOLLOUT | EPOLLET;
            epoll_ctl(epoll_fd, EPOLL_CTL_MOD, thread_para->client_fd, &event);
        }*/
        
        _log(ERROR, "worker:%d sleep 5s\n", pool_index);
        sleep(5);

        thread_para->free = 0; // 设置线程占用标志为"空闲"
    }
}

static void *check_connect_timeout(void* arg){
    _log(INFO, "check_connect_timeout\n");
}

static void init_thread_pool(int num){
    pthread_t td;
    int i = 0, ret = 0;
    // 初始化线程池参数
    for (i = 0; i < num; i++){
        thread_paras[i].free = 0; // 设置线程占用标志为"空闲"
        thread_paras[i].thread_id = i; // 设置线程池索引编号
        //pthread_mutex_lock(mutexs + i); // 对应的线锁加锁
        pthread_mutex_lock(&thread_paras[i].mutex); // 对应的线锁加锁
    }

    // 创建线程池
    for ( i = 0; i < num; i++){
        ret = pthread_create(tds + i, 0, worker, (void*)&thread_paras[i]);
        if(ret){
            _log(ERROR, error_str[ERR_CREATE_THREAD]);
            exit(ERR_CREATE_THREAD);
        }
    }
    // 创建心跳包线程
    ret = pthread_create(&td, 0, check_connect_timeout, (void *)0);
    if(ret){
        _log(ERROR, error_str[ERR_CREATE_THREAD]);
        exit(ERR_CREATE_THREAD);
    }
}

int libertymqtt_main_loop(libertymqtt_config *config){
    _log(INFO, "start ...\n");
    time_t start_time = libertymqtt_time();
    time_t now = start_time;
    _log(DEBUG, "%ld\n", start_time);
    struct pollfd *pool_pollfd = NULL; // pollfd描述符池
    int pollfd_count = 0; // pollfd总数量
    int pollfd_index; // pollfd当前索引

    listenfd = config->default_listener.fd;
    init_thread_pool(THREAD_MAX);
    struct epoll_event event;

    epoll_fd = epoll_create(65535);
    memset(&event, 0, sizeof(event));
    event.data.fd = config->default_listener.fd;
    event.events = EPOLLIN | EPOLLET;
    epoll_ctl(epoll_fd, EPOLL_CTL_ADD, config->default_listener.fd, &event);

    /*while(run){
    }*/
    int i = 0;
    int datalen = 0;
    struct sockaddr_in clientaddr;
    socklen_t addrlen;
    for(;;){
        // 等待epoll事件
        /*
        int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout)
        等待事件的产生，类似于select()调用。参数events用来从内核得到事件的集合，
        maxevents表示每次能处理的最大事件数，告之内核这个events有多大，
        这个maxevents的值不能大于创建epoll_create()时的size，
        参数timeout是超时时间（毫秒，0会立即返回，-1将不确定，也有说法说是永久阻塞）。
        该函数返回需要处理的事件数目，如返回0表示已超时。
        */
        int nfds = epoll_wait(epoll_fd, events, 65535, 500);
        if(nfds > 0){
            for(i = 0; i < nfds; i++){
                struct epoll_event event;
                if (events[i].data.fd == listenfd) { // 有新的连接
                    event.data.fd = accept(listenfd, (struct sockaddr*)&clientaddr, &addrlen); // 接受这个连接
                    event.events = EPOLLIN; // EPOLLET：表示对应的文件描述符有事件发生；
                    epoll_ctl(epoll_fd, EPOLL_CTL_ADD, event.data.fd, &event);  //将新的fd添加到epoll的监听队列中
                } else if (events[i].events & EPOLLIN){ // 接收到数据，读取socket
                    /*char buf[1024];
                    int line = 1;
                    int MAXLINE = 1024;
                    datalen = read(listenfd, line, MAXLINE);
                    event.data.ptr = buf; // 写数据到buf
                    event.events = EPOLLOUT | EPOLLET;
                    epoll_ctl(epoll_fd, EPOLL_CTL_MOD, listenfd, &event); //修改标识符，等待下一个循环时发送数据，异步处理的精髓
                    */
                } else if (events[i].events & EPOLLOUT){ // 有数据待发送，写socket
                    /*struct myepoll_data* md = (myepoll_data*)events[i].data.ptr;    //取数据
                    int client_fd= md->fd;
                    send(client_fd, md->ptr, strlen((char*)md->ptr), 0 );        //发送数据
                    ev.data.fd=sockfd;
                    ev.events=EPOLLIN|EPOLLET;
                    epoll_ctl(epoll_fd,EPOLL_CTL_MOD, client_fd, &event); //修改标识符，等待下一个循环时接收数据
                    */
                } else {
                    //to do
                }

                
                    // 查询空闲线程对
                    int j = 0;
            for (j = 0; j < THREAD_MAX; j++)
            {
                if (0 == thread_paras[j].free)
                break;
            }
            if (j >= THREAD_MAX)
            {
                fprintf(stderr, "The thread pool has overloaded,it's link is going to be abandon\r\n");
                //shutdown(event.data.fd, SHUT_RDWR);
                //close(event.data.fd);
                continue;
            }
            // 复制有关参数
            thread_paras[j].free = 1;// 设置活动标志为"活动"
            thread_paras[j].client_fd = event.data.fd;// 客户端连接
            //thread_paras[j][2] = listen_index;// 监听索引
            // 线程解锁
            pthread_mutex_unlock(&thread_paras[j].mutex);

            }
        }
    }

    if(pool_pollfd)
        _libertymqtt_free(pool_pollfd);
    _log(INFO, "end\n");
    return SUCCESS;
}
