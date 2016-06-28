#include <config.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <unistd.h>
#include <netdb.h>
#include <fcntl.h>


/**
* ����socket��������
*/
int libertymqtt_listen(_libertymqtt_listener *listener){
    int sockd = -1, opt = 1 /* ������ģʽ */, ss_opt = 1;
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
    ����ͷ�ļ�
    #include<netdb.h>

    ����ԭ��
    int getaddrinfo( const char *hostname, const char *service, const struct addrinfo *hints, struct addrinfo **result );

    ����˵��
    hostname:һ�����������ߵ�ַ��(IPv4�ĵ��ʮ���ƴ�����IPv6��16���ƴ�)
    service��������������ʮ���ƵĶ˿ںţ�Ҳ�������Ѷ���ķ������ƣ���ftp��http��
    hints��������һ����ָ�룬Ҳ������һ��ָ��ĳ��addrinfo�ṹ���ָ�룬������������ṹ����������������ص���Ϣ���͵İ�ʾ��������˵�����ָ���ķ����֧��TCPҲ֧��UDP����ô�����߿��԰�hints�ṹ�е�ai_socktype��Ա���ó�SOCK_DGRAMʹ�÷��صĽ��������������ݱ��׽ӿڵ���Ϣ��
    result��������ͨ��resultָ���������һ��ָ��addrinfo�ṹ�������ָ�롣
    ����ֵ��0�����ɹ�����0��������
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

        // ����socket
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
        listener->sockets_pool[listener->sockets_count-1] = sockd; // ����socket������

        // ���õ�ַ����
        ss_opt = 1;
        setsockopt(sockd, SOL_SOCKET, SO_REUSEADDR, &ss_opt, sizeof(ss_opt));

        /* ���÷�����ģʽ */
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
        listen�ĵڶ����������� �ȴ����Ӷ��е���󳤶ȡ�listen()��������֧�����ӵ��׽ӿڣ���SOCK_STREAM���͵ġ�
        �׽ӿڴ���һ�֡��䶯��ģʽ������������������ȷ�ϣ����Ŷӵȴ������ܡ���������ر�������ͬʱ�ж����������ķ�������
        �����һ������������ʱ��������������ô�ͻ����յ�һ������
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