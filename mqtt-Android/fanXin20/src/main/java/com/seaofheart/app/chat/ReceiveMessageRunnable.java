package com.seaofheart.app.chat;

import android.content.ContentValues;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import com.seaofheart.app.CallBack;
import com.seaofheart.app.MYApplication;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.chat.protocol.ProtocolMessage.MTMessage;
import com.seaofheart.app.util.EMLog;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import java.util.Hashtable;

/**
 * Created by Administrator on 2016/4/22.
 */
public class ReceiveMessageRunnable implements Runnable {
    private static final String TAG = "ReceiveMessageRunnable";
    private Message msg;
    private CallBack callback;
    private FutureConnection futureConnection;
    static Hashtable<String, Object> receiveLocks;
    static Hashtable<String, Object> errorMsgWaitLocks = new Hashtable<String, Object>();
    private Object receiveMutex = new Object();

    public ReceiveMessageRunnable(FutureConnection futureConnection, Message msg, CallBack callBack) {
        this.futureConnection = futureConnection;
        this.msg = msg;
        this.callback = callBack;
    }
    public ReceiveMessageRunnable(FutureConnection futureConnection, CallBack callBack) {
        this.futureConnection = futureConnection;
        this.callback = callBack;
    }
    public ReceiveMessageRunnable(FutureConnection futureConnection) {
        this.futureConnection = futureConnection;
    }

    static synchronized void addReceiveLock(String msgId, Object mutex) {
        if (null == receiveLocks) {
            receiveLocks = new Hashtable<String, Object>();
        }
        receiveLocks.put(msgId, mutex);
    }

    public void run() {
        while (true) {
            this.checkConnection();
            try{
                MYApplication myApplication = MYApplication.getInstance();
                String username = myApplication.getUserName();
                if(null == username || "" == username){
                    Thread.sleep(500); // 休眠1秒再试
                    continue;
                }

                Log.i(TAG, "topic:" + username);
                // 订阅我的id号，用以接收别人发给我的消息
                Future<byte[]> f2 = this.futureConnection.subscribe(new Topic[]{new Topic(username, QoS.AT_LEAST_ONCE)}); // mytopic
                byte[] qoses = f2.await();

                // We can start future receive..
                Future<org.fusesource.mqtt.client.Message> receive = this.futureConnection.receive();

                // Then the receive will get the message.
                org.fusesource.mqtt.client.Message message = receive.await();
                message.ack();
                //Log.i(TAG, "MQTT receive:" + UTF8Buffer.utf8(message.getPayloadBuffer()));


                MTMessage mtMessage = MTMessage.parseFrom(message.getPayload());
                //System.out.printf("MQTT receive[from:%s,to:%s", msg.getFrom(), msg.getTo());
                Message msg = new Message(ProtocolMessage.TYPE.TXT);
                msg.setFrom(String.valueOf(mtMessage.getFrom())); // 11240739
                msg.setTo(String.valueOf(mtMessage.getTo())); // 11240731
                msg.setChatType(mtMessage.getChatType());
                msg.setMsgId(String.valueOf(mtMessage.getMsgId())); // 100001
                msg.setType(mtMessage.getType());
                //TextMessageBody txtBody = new TextMessageBody(UTF8Buffer.utf8(message.getPayloadBuffer()).toString());
                if (ProtocolMessage.TYPE.TXT == mtMessage.getType()) {
                    TextMessageBody txtBody = new TextMessageBody(mtMessage.getContent());
                    msg.addBody(txtBody);
                } else {

                }
                msg.status = ProtocolMessage.STATUS.SUCCESS;
                if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                    this.updateMsgState(msg);
                }
                ConversationManager.getInstance().addMessage(msg);
                ChatManager.getInstance().notifyMessage(msg);

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void updateMsgState(Message msg) {
        ContentValues var2 = new ContentValues();
        var2.put("status", String.valueOf(msg.status.ordinal()));
        DBManager.getInstance().updateMessage(msg.msgId, var2);
    }

    private void checkConnection() {
        try{
            ChatManager.getInstance().checkConnection();
        } catch (Exception e){ // 出现异常重连
            Object mutex = this.receiveMutex;
            synchronized(this.receiveMutex) {
                //this.receiveMutex(this.msg.getMsgId(), this.receiveMutex);
                ChatManager.getInstance().tryToReconnectOnGCM();
                try {
                    this.receiveMutex.wait(60000L);
                    EMLog.d("sender", "wait send message time out");
                } catch (InterruptedException var4) {
                    var4.printStackTrace();
                }
            }
        }
    }
}
