#ifndef LIBERTYMQTT_INTERNAL_H
#define LIBERTYMQTT_INTERNAL_H

struct libertymqtt {
    int sock; // �ͻ��˺ͷ������������ӵ��ļ�������
    char *client_id; // �ͻ���id��ȡÿ���ֻ���Ψһ��ʶ���������ظ�
    char *address; // ��ַ
    char *username; // �û���
    char *password; // ����
    uint16_t keepalive; // ����ʱ����sΪ��λ
#ifdef WITH_TLS
#endif
}

#endif // LIBERTYMQTT_INTERNAL_H