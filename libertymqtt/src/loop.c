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
// �̳߳ز���
static pthread_t               tds[THREAD_MAX]; // �̳߳�
//static unsigned int            thread_paras[THREAD_MAX][8]; // �̳߳ز�����ÿ���̶߳�Ӧһ���������飬ÿ�������0~7λ�÷ֱ�Ϊ
// thread_paras[0] = 1 �߳�ռ�ã�0���У�
// thread_paras[1] �ͻ���fd
// thread_paras[2] ��������
// thread_paras[7] �߳��������

typedef struct {
    uint8_t free; // 1 �߳�ռ�ã�0���У�
    int client_fd; // �ͻ���fd
    int thread_id; // �̱߳��
    pthread_mutex_t mutex; // �߳���
    char *buf; // ����д�Ļ�����
} thread_para_t;
static thread_para_t            thread_paras[THREAD_MAX]; // �̳߳ز�����ÿ���̶߳�Ӧһ���������飬ÿ�������0~7λ�÷ֱ�Ϊ


static pthread_mutex_t    mutexs[THREAD_MAX]; // �̳߳�����ÿ���߳�һ����


static void *worker(thread_para_t *thread_para){
    int pool_index; // ���߳����̳߳��е��������
    int sock_client; // �ͻ���fd
    int listen_index;  // ��������
    int len = 0;
    char buf[4096];    // ���仺����
    // �߳����봴����
    pthread_detach(pthread_self());
    pool_index = thread_para->thread_id;
    _log(INFO, "worker %d\n", pool_index);
    while(1){
        pthread_mutex_lock(&thread_para->mutex); // �������������У��ȴ����̵߳Ľ�������
        sock_client = thread_para->client_fd;
        
        // ��������
        len = recv(sock_client, buf, 4096, MSG_NOSIGNAL);
        if(len > 0){
            snprintf(buf, 4096, "Hello, I'm worker %d\n", pool_index);
            // ������Ӧ
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

        thread_para->free = 0; // �����߳�ռ�ñ�־Ϊ"����"
    }
}

static void *check_connect_timeout(void* arg){
    _log(INFO, "check_connect_timeout\n");
}

static void init_thread_pool(int num){
    pthread_t td;
    int i = 0, ret = 0;
    // ��ʼ���̳߳ز���
    for (i = 0; i < num; i++){
        thread_paras[i].free = 0; // �����߳�ռ�ñ�־Ϊ"����"
        thread_paras[i].thread_id = i; // �����̳߳��������
        //pthread_mutex_lock(mutexs + i); // ��Ӧ����������
        pthread_mutex_lock(&thread_paras[i].mutex); // ��Ӧ����������
    }

    // �����̳߳�
    for ( i = 0; i < num; i++){
        ret = pthread_create(tds + i, 0, worker, (void*)&thread_paras[i]);
        if(ret){
            _log(ERROR, error_str[ERR_CREATE_THREAD]);
            exit(ERR_CREATE_THREAD);
        }
    }
    // �����������߳�
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
    struct pollfd *pool_pollfd = NULL; // pollfd��������
    int pollfd_count = 0; // pollfd������
    int pollfd_index; // pollfd��ǰ����

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
        // �ȴ�epoll�¼�
        /*
        int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout)
        �ȴ��¼��Ĳ�����������select()���á�����events�������ں˵õ��¼��ļ��ϣ�
        maxevents��ʾÿ���ܴ��������¼�������֮�ں����events�ж��
        ���maxevents��ֵ���ܴ��ڴ���epoll_create()ʱ��size��
        ����timeout�ǳ�ʱʱ�䣨���룬0���������أ�-1����ȷ����Ҳ��˵��˵��������������
        �ú���������Ҫ������¼���Ŀ���緵��0��ʾ�ѳ�ʱ��
        */
        int nfds = epoll_wait(epoll_fd, events, 65535, 500);
        if(nfds > 0){
            for(i = 0; i < nfds; i++){
                struct epoll_event event;
                if (events[i].data.fd == listenfd) { // ���µ�����
                    event.data.fd = accept(listenfd, (struct sockaddr*)&clientaddr, &addrlen); // �����������
                    event.events = EPOLLIN; // EPOLLET����ʾ��Ӧ���ļ����������¼�������
                    epoll_ctl(epoll_fd, EPOLL_CTL_ADD, event.data.fd, &event);  //���µ�fd��ӵ�epoll�ļ���������
                } else if (events[i].events & EPOLLIN){ // ���յ����ݣ���ȡsocket
                    /*char buf[1024];
                    int line = 1;
                    int MAXLINE = 1024;
                    datalen = read(listenfd, line, MAXLINE);
                    event.data.ptr = buf; // д���ݵ�buf
                    event.events = EPOLLOUT | EPOLLET;
                    epoll_ctl(epoll_fd, EPOLL_CTL_MOD, listenfd, &event); //�޸ı�ʶ�����ȴ���һ��ѭ��ʱ�������ݣ��첽����ľ���
                    */
                } else if (events[i].events & EPOLLOUT){ // �����ݴ����ͣ�дsocket
                    /*struct myepoll_data* md = (myepoll_data*)events[i].data.ptr;    //ȡ����
                    int client_fd= md->fd;
                    send(client_fd, md->ptr, strlen((char*)md->ptr), 0 );        //��������
                    ev.data.fd=sockfd;
                    ev.events=EPOLLIN|EPOLLET;
                    epoll_ctl(epoll_fd,EPOLL_CTL_MOD, client_fd, &event); //�޸ı�ʶ�����ȴ���һ��ѭ��ʱ��������
                    */
                } else {
                    //to do
                }

                
                    // ��ѯ�����̶߳�
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
            // �����йز���
            thread_paras[j].free = 1;// ���û��־Ϊ"�"
            thread_paras[j].client_fd = event.data.fd;// �ͻ�������
            //thread_paras[j][2] = listen_index;// ��������
            // �߳̽���
            pthread_mutex_unlock(&thread_paras[j].mutex);

            }
        }
    }

    if(pool_pollfd)
        _libertymqtt_free(pool_pollfd);
    _log(INFO, "end\n");
    return SUCCESS;
}
