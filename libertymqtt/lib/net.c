#include <config.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <unistd.h>
#include <netdb.h>
#include <fcntl.h>


/**
* 创建socket，并监听
*/
int libertymqtt_listen(_libertymqtt_listener *listener){
    int sockd = -1, opt = 1 /* 非阻塞模式 */, ss_opt = 1;
    char error[MAX_ERROR], service[10];
    struct addrinfo hints, *result, *iptr;
    if(!listener)
        return ERR_INVALID;
    snprintf(service, 10, "%d", listener->port);
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = PF_UNSPEC;
    hints.ai_flags = AI_PASSIVE;
    hints.ai_socktype = SOCK_STREAM;
    _log(ERROR, error_str[ERR_NOMEMORY]);
    /*
    包含头文件
    #include<netdb.h>

    函数原型
    int getaddrinfo( const char *hostname, const char *service, const struct addrinfo *hints, struct addrinfo **result );

    参数说明
    hostname:一个主机名或者地址串(IPv4的点分十进制串或者IPv6的16进制串)
    service：服务名可以是十进制的端口号，也可以是已定义的服务名称，如ftp、http等
    hints：可以是一个空指针，也可以是一个指向某个addrinfo结构体的指针，调用者在这个结构中填入关于期望返回的信息类型的暗示。举例来说：如果指定的服务既支持TCP也支持UDP，那么调用者可以把hints结构中的ai_socktype成员设置成SOCK_DGRAM使得返回的仅仅是适用于数据报套接口的信息。
    result：本函数通过result指针参数返回一个指向addrinfo结构体链表的指针。
    返回值：0——成功，非0——出错
    */
    if(getaddrinfo(listener->host, service, &hints, &result))
        return ERR_INVALID_SOCKET;

    listener->sockets_count = 0;
    listener->sockets_pool = NULL;

    for(iptr = result; iptr; iptr = iptr->ai_next){
        if(iptr->ai_family == AF_INET){
            _log(INFO, "Opening ipv4 listen socket on port %d.\n", ntohs(((struct sockaddr_in *)iptr->ai_addr)->sin_port));
        }else if(iptr->ai_family == AF_INET6){
            _log(INFO, "Opening ipv6 listen socket on port %d.\n", ntohs(((struct sockaddr_in6 *)iptr->ai_addr)->sin6_port));
        }else{
            continue;
        }

        // 创建socket
        sockd = socket(iptr->ai_family, iptr->ai_socktype, iptr->ai_protocol);
        if(sockd == -1){
            strerror_r(errno, error, MAX_ERROR);
            _log(ERROR, "%s\n", error);
            continue;
        }
        listener->sockets_count++;
        listener->sockets_pool = _libertymqtt_realloc(listener->sockets_pool, sizeof(int)*listener->sockets_count);
        if(!listener->sockets_pool){
            _log(ERROR, error_str[ERR_NOMEMORY]);
            freeaddrinfo(result);
            return ERR_NOMEMORY;
        }
        listener->sockets_pool[listener->sockets_count-1] = sockd; // 拷贝socket到池中

        // 设置地址重用
        ss_opt = 1;
        setsockopt(sockd, SOL_SOCKET, SO_REUSEADDR, &ss_opt, sizeof(ss_opt));

        /* 设置非阻塞模式 */
        opt = fcntl(sockd, F_GETFL, 0);
        if(opt == -1 || fcntl(sockd, F_SETFL, opt | O_NONBLOCK) == -1){
            freeaddrinfo(result);
            close(sockd);
            return ERR_INVALID_SOCKET;
        }

        if(bind(sockd, iptr->ai_addr, iptr->ai_addrlen) == -1){
            strerror_r(errno, error, MAX_ERROR);
            _log(ERROR, "%s\n", error);
            freeaddrinfo(result);
            close(sockd);
            return ERR_INVALID_SOCKET;
        }

        /*
        listen的第二个参数，是 等待连接队列的最大长度。listen()仅适用于支持连接的套接口，如SOCK_STREAM类型的。
        套接口处于一种“变动”模式，申请进入的连接请求被确认，并排队等待被接受。这个函数特别适用于同时有多个连接请求的服务器；
        如果当一个连接请求到来时，队列已满，那么客户将收到一个错误。
        */
        if(listen(sockd, 100) == -1){
            strerror_r(errno, error, MAX_ERROR);
            _log(ERROR, "%s\n", error);
            close(sockd);
            freeaddrinfo(result);
            return ERR_INVALID_SOCKET;
        }

        listener->fd = sockd;
    }
    freeaddrinfo(result);

    /* We need to have at least one working socket. */
    if(listener->sockets_count > 0){
        return SUCCESS;
    }else{
        return -1;
    }

    return SUCCESS;
}