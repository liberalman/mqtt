#ifndef LIBERTYMQTT_INTERNAL_H
#define LIBERTYMQTT_INTERNAL_H

struct libertymqtt {
    int sock; // 客户端和服务器保持连接的文件描述符
    char *client_id; // 客户端id，取每个手机的唯一标识符，不能重复
    char *address; // 地址
    char *username; // 用户名
    char *password; // 密码
    uint16_t keepalive; // 心跳时长，s为单位
#ifdef WITH_TLS
#endif
}

#endif // LIBERTYMQTT_INTERNAL_H