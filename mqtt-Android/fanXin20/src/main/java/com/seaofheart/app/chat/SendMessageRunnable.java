//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.seaofheart.app.chat;


import android.content.ContentValues;
import android.graphics.BitmapFactory.Options;
import android.text.TextUtils;

import com.seaofheart.app.chat.core.ConnectionManager;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.core.k;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.chat.core.x;
import com.seaofheart.app.CallBack;
import com.seaofheart.app.analytics.MessageCollector;
import com.seaofheart.app.analytics.TimeTag;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.chat.protocol.ProtocolMessage.MTMessage;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.ImageUtils;
import com.seaofheart.app.util.PerfUtils;

import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.QoS;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import org.jivesoftware.smack.Chat;
//import org.jivesoftware.smack.packet.Message;
//import org.jivesoftware.smack.packet.ProtocolMessage.TYPE;
//import org.jivesoftware.smackx.muc.MultiUserChat;

class SendMessageRunnable implements Runnable {
    private static final String TAG = "SendMessageRunnable";
    private static final String PERF = "perf";
    //private org.jivesoftware.smack.Chat chat;
    private org.jivesoftware.smackx.muc.MultiUserChat muc;
    private Message msg;
    private CallBack callback;
    private static final int WAIT_TIME_OUT = 40;
    static Hashtable<String, Object> sendLocks;
    static Hashtable<String, Object> errorMsgWaitLocks = new Hashtable();
    private Object mutex = new Object();
    private Object errorWaitLock = new Object();
    private static final int WAIT_SEND_TIME_OUT = 60;
    static Hashtable<String, Object> sendMsgLocks;
    private Object sendMutex = new Object();
    private String groupId;
    private boolean connectedBeforeSend = true;
    private int numberOfRetried = 3;
    private static long lastForceReconnectTime = 0L;
    private static ConcurrentLinkedQueue<SendMessageRunnable> pendingMsgQueue = new ConcurrentLinkedQueue();
    private static long lastSendMessageTime = -1L;
    private static Object trafficLock = new Object();
    private static ExecutorService sendThreadPool = Executors.newFixedThreadPool(3);
    private FutureConnection futureConnection;
    TimeTag timeTag = new TimeTag();

    public SendMessageRunnable(FutureConnection futureConnection, Message msg, CallBack callBack) {
        this.futureConnection = futureConnection;
        this.msg = msg;
        this.callback = callBack;
    }


    public SendMessageRunnable(org.jivesoftware.smackx.muc.MultiUserChat var1, Message var2, CallBack var3) {
        this.muc = var1;
        this.msg = var2;
        this.callback = var3;
    }

    SendMessageRunnable(FutureConnection futureConnection, String groupId, Message msg, CallBack callBack) {
        this.futureConnection = futureConnection;
        this.groupId = groupId;
        this.msg = msg;
        this.callback = callBack;
    }

    static synchronized void addSendLock(String var0, Object var1) {
        if(sendLocks == null) {
            sendLocks = new Hashtable();
        }

        sendLocks.put(var0, var1);
    }

    static synchronized void notifySendLock(String var0) {
        if(sendLocks != null) {
            Object var1 = sendLocks.remove(var0);
            if(var1 != null) {
                synchronized(var1) {
                    var1.notify();
                }
            }

        }
    }

    static void addErrorMsgWaitLock(String var0, Object var1) {
        errorMsgWaitLocks.put(var0, var1);
    }

    static boolean notifyErrorMsgWaitLock(String var0) {
        Object var1 = errorMsgWaitLocks.remove(var0);
        if(var1 != null) {
            synchronized(var1) {
                var1.notify();
                return true;
            }
        } else {
            return false;
        }
    }

    static synchronized void flushPendingQueue() {
        EMLog.d("sender", "start flush Pending Queue");

        for(SendMessageRunnable var0 = (SendMessageRunnable)pendingMsgQueue.poll(); var0 != null; var0 = (SendMessageRunnable)pendingMsgQueue.poll()) {
            EMLog.d("sender", "resend msg : " + var0.msg.getMsgId());
            sendThreadPool.submit(var0);
        }

    }

    public void run() {
        this.checkConnection();
        /*if(this.msg.getChatType() != Message.ChatType.Chat && this.muc == null && this.groupId != null) {
            String var1 = ContactManager.getEidFromGroupId(this.groupId);
            try {
                org.jivesoftware.smackx.muc.MultiUserChat var2 = MultiUserChatManager.getInstance().getMUCWithoutJoin(var1, this.msg.getChatType());
                this.muc = var2;
            } catch (Exception var3) {
                ;
            }
        }*/

        this.msg.status = ProtocolMessage.STATUS.INPROGRESS;
        this.timeTag.start();
        switch(this.msg.type) {
            case TXT:
            case LOCATION:
            case CMD:
                this.sendMessage(this.msg);
                break;
            case IMAGE:
                this.sendImageMessage(this.msg, this.callback);
                break;
            case VIDEO:
                this.sendVideoMessage(this.msg, this.callback);
                break;
            case VOICE:
                this.sendFileMessage(this.msg, this.callback);
                break;
            case FILE:
                this.sendFileMessage(this.msg, this.callback);
                break;
            default:
                EMLog.e("sender", "unsupport msg type, need to check:" + this.msg.type);
        }

    }

    /*private void sendMesssageWithTrafficLimit(org.jivesoftware.smack.packet.Message var1, boolean var2) throws XMPPException {
        Object var3 = trafficLock;
        synchronized(trafficLock) {
            if(lastSendMessageTime > 0L) {
                long var4 = System.currentTimeMillis();
                long var6 = var4 - lastSendMessageTime;
                if(var6 < 700L) {
                    try {
                        trafficLock.wait(700L - var6);
                    } catch (InterruptedException var9) {
                        ;
                    }
                }
            }
        }

        if(var2) {
            this.muc.sendMessage(var1);
        } else {
            this.chat.sendMessage(var1);
        }

        lastSendMessageTime = System.currentTimeMillis();
    }*/
    private void sendMesssageWithTrafficLimit(Message msg, boolean isMultiChat) throws Exception {
        Object var3 = trafficLock;
        synchronized(trafficLock) {
            if(lastSendMessageTime > 0L) {
                long var4 = System.currentTimeMillis();
                long var6 = var4 - lastSendMessageTime;
                if(var6 < 700L) {
                    try {
                        trafficLock.wait(700L - var6);
                    } catch (InterruptedException var9) {
                        ;
                    }
                }
            }
        }

        if(isMultiChat) {
            //this.muc.sendMessage(var1);
        } else {
            sendMessage(msg);
        }

        lastSendMessageTime = System.currentTimeMillis();
    }

    private void sendMessage(Message msg) {
        TextMessageBody txtBody = (TextMessageBody) msg.getBody();

        MTMessage.Builder builder = MTMessage.newBuilder();
        MTMessage mtMessage = builder.build();
        builder.setMsgId(msg.getMsgId());
        builder.setType(msg.type);
        builder.setContent(txtBody.getMessage());
        builder.setStatus(msg.status);
        builder.setChatType(msg.chatType);
        builder.setDirect(msg.direct);
        builder.setFrom(msg.from.getEid());
        builder.setTo(msg.to.getEid());
        byte[] buf = mtMessage.toByteArray();

        try {
            String topic = msg.to.getUsername();
            this.futureConnection.publish(topic, buf, QoS.AT_LEAST_ONCE, false).await(); // mytopic
            //this.futureConnection.publish("mytopic", txtBody.getMessage().getBytes(), QoS.AT_LEAST_ONCE, false).await();

            /*String jsonMsg = MessageEncoder.getJSONMsg(msg, false);
            EMLog.d(TAG, "try to send msg to:" + msg.to + " msg:" + jsonMsg);
            org.jivesoftware.smack.packet.Message var3 = new org.jivesoftware.smack.packet.Message();
            var3.setPacketID(msg.getMsgId());
            ChatOptions var4 = ChatManager.getInstance().getChatOptions();
            if(var4.getUseEncryption()) {
                jsonMsg = EncryptUtils.encryptMessage(jsonMsg, msg.getTo());
                var3.addExtension(new k());
            }

            //var3.setBody(var2);
            if(var4.getRequireServerAck()) { // 是否需要服务器应答，需要应答的话，要先对该id的消息加锁。
                addSendLock(var3.getPacketID(), this.mutex);
            }

            this.connectedBeforeSend = ChatManager.getInstance().isConnected();
            if(msg.getChatType() != ProtocolMessage.CHAT_TYPE.CHAT_GROUP && msg.getChatType() != ProtocolMessage.CHAT_TYPE.CHAT_ROOM) {
                //this.sendMesssageWithTrafficLimit(var3, false);
            } else {
                var3.setType(org.jivesoftware.smack.packet.ProtocolMessage.TYPE.groupchat);
                var3.setTo(this.muc.getRoom());
                if(msg.getChatType() == ProtocolMessage.CHAT_TYPE.CHAT_ROOM) {
                    var3.addExtension(new x());
                }

                EMLog.d("sender", "send message to muc:" + this.muc.getRoom());
                //this.sendMesssageWithTrafficLimit(var3, true);
            }

            Object var5;
            if(var4.getRequireServerAck()) {
                EMLog.d("sender", "wait for server ack...");
                var5 = this.mutex;
                synchronized(this.mutex) {
                    if(sendLocks.containsKey(var3.getPacketID())) {
                        this.mutex.wait(40000L);
                    }
                }

                EMLog.d("sender", "exit from wait");
                if(sendLocks.remove(var3.getPacketID()) != null) {
                    EMLog.e("sender", "did not receive ack from server for msg:" + var3.getPacketID());
                    if(this.connectedBeforeSend && ChatManager.getInstance().isConnected()) {
                        --this.numberOfRetried;
                        if(this.numberOfRetried <= 0) {
                            msg.status = ProtocolMessage.STATUS.FAIL;
                            if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                                this.updateMsgState(msg);
                            }

                            if(this.callback != null) {
                                this.callback.onError(-2, "no response from server");
                            }

                            return;
                        }

                        pendingMsgQueue.offer(this);
                        EMLog.d("sender", "add new msg to pending msg queue : " + msg.getMsgId());
                        if(lastForceReconnectTime != 0L && System.currentTimeMillis() - lastForceReconnectTime <= 30000L) {
                            if(ChatManager.getInstance().isConnected()) {
                                flushPendingQueue();
                            }
                        } else {
                            lastForceReconnectTime = System.currentTimeMillis();
                            ChatManager.getInstance().forceReconnect();
                        }
                    } else {
                        msg.status = ProtocolMessage.STATUS.FAIL;
                        if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                            this.updateMsgState(msg);
                        }

                        MessageCollector.collectSendMsgTime(this.timeTag.stop(), msg);
                        if(this.callback != null) {
                            this.callback.onError(-2, "no response from server");
                        }
                    }

                    return;
                }
            }

            addErrorMsgWaitLock(msg.getMsgId(), this.errorWaitLock);
            var5 = this.errorWaitLock;
            synchronized(this.errorWaitLock) {
                this.errorWaitLock.wait(50L);
                errorMsgWaitLocks.remove(msg.getMsgId());
            }

            if(msg.status == ProtocolMessage.STATUS.FAIL) {
                EMLog.d("sender", "server send the msg error : " + msg.getError());
                MessageCollector.collectSendMsgTime(this.timeTag.stop(), msg);
                if(this.callback != null) {
                    int var9 = -2;
                    if(msg.getError() != 0) {
                        var9 = msg.getError();
                    }

                    this.callback.onError(var9, "send message fail");
                }

                return;
            }*/

            // 设置消息状态为成功，这样回调函数在处理的时候，就会相应的更新ui状态。
            msg.status = ProtocolMessage.STATUS.SUCCESS;
            if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                this.updateMsgState(msg);
            }

            // 调用传入的回调函数
            MessageCollector.collectSendMsgTime(this.timeTag.stop(), msg);
            if(this.callback != null) {
                this.callback.onSuccess();
            }
        } catch (Exception var8) {
            var8.printStackTrace();
            if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                msg.status = ProtocolMessage.STATUS.FAIL;
            }

            this.updateMsgState(msg);
            if(this.callback != null) {
                this.callback.onError(-2, var8.toString());
            }
        }

    }

    /*private void sendMessageXmpp(Message msg) {
        try {
            String var2 = MessageEncoder.getJSONMsg(msg, false);
            EMLog.d("sender", "try to send msg to:" + msg.to + " msg:" + var2);
            org.jivesoftware.smack.packet.Message var3 = new org.jivesoftware.smack.packet.Message();
            var3.setPacketID(msg.getMsgId());
            ChatOptions var4 = ChatManager.getInstance().getChatOptions();
            if(var4.getUseEncryption()) {
                var2 = EncryptUtils.encryptMessage(var2, msg.getTo());
                var3.addExtension(new k());
            }

            var3.setBody(var2);
            if(var4.getRequireServerAck()) {
                addSendLock(var3.getPacketID(), this.mutex);
            }

            this.connectedBeforeSend = ChatManager.getInstance().isConnected();
            if(msg.getChatType() != Message.ChatType.GroupChat && msg.getChatType() != Message.ChatType.ChatRoom) {
                this.sendMesssageWithTrafficLimit(var3, false);
            } else {
                var3.setType(org.jivesoftware.smack.packet.ProtocolMessage.TYPE.groupchat);
                var3.setTo(this.muc.getRoom());
                if(msg.getChatType() == Message.ChatType.ChatRoom) {
                    var3.addExtension(new x());
                }

                EMLog.d("sender", "send message to muc:" + this.muc.getRoom());
                this.sendMesssageWithTrafficLimit(var3, true);
            }

            Object var5;
            if(var4.getRequireServerAck()) {
                EMLog.d("sender", "wait for server ack...");
                var5 = this.mutex;
                synchronized(this.mutex) {
                    if(sendLocks.containsKey(var3.getPacketID())) {
                        this.mutex.wait(40000L);
                    }
                }

                EMLog.d("sender", "exit from wait");
                if(sendLocks.remove(var3.getPacketID()) != null) {
                    EMLog.e("sender", "did not receive ack from server for msg:" + var3.getPacketID());
                    if(this.connectedBeforeSend && ChatManager.getInstance().isConnected()) {
                        --this.numberOfRetried;
                        if(this.numberOfRetried <= 0) {
                            msg.status = Message.Status.FAIL;
                            if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                                this.updateMsgState(msg);
                            }

                            if(this.callback != null) {
                                this.callback.onError(-2, "no response from server");
                            }

                            return;
                        }

                        pendingMsgQueue.offer(this);
                        EMLog.d("sender", "add new msg to pending msg queue : " + msg.getMsgId());
                        if(lastForceReconnectTime != 0L && System.currentTimeMillis() - lastForceReconnectTime <= 30000L) {
                            if(ChatManager.getInstance().isConnected()) {
                                flushPendingQueue();
                            }
                        } else {
                            lastForceReconnectTime = System.currentTimeMillis();
                            ChatManager.getInstance().forceReconnect();
                        }
                    } else {
                        msg.status = Message.Status.FAIL;
                        if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                            this.updateMsgState(msg);
                        }

                        MessageCollector.collectSendMsgTime(this.timeTag.stop(), msg);
                        if(this.callback != null) {
                            this.callback.onError(-2, "no response from server");
                        }
                    }

                    return;
                }
            }

            addErrorMsgWaitLock(msg.getMsgId(), this.errorWaitLock);
            var5 = this.errorWaitLock;
            synchronized(this.errorWaitLock) {
                this.errorWaitLock.wait(50L);
                errorMsgWaitLocks.remove(msg.getMsgId());
            }

            if(msg.status == Message.Status.FAIL) {
                EMLog.d("sender", "server send the msg error : " + msg.getError());
                MessageCollector.collectSendMsgTime(this.timeTag.stop(), msg);
                if(this.callback != null) {
                    int var9 = -2;
                    if(msg.getError() != 0) {
                        var9 = msg.getError();
                    }

                    this.callback.onError(var9, "send message fail");
                }

                return;
            }

            msg.status = Message.Status.SUCCESS;
            if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                this.updateMsgState(msg);
            }

            MessageCollector.collectSendMsgTime(this.timeTag.stop(), msg);
            if(this.callback != null) {
                this.callback.onSuccess();
            }
        } catch (Exception var8) {
            var8.printStackTrace();
            if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                msg.status = Message.Status.FAIL;
            }

            this.updateMsgState(msg);
            if(this.callback != null) {
                this.callback.onError(-2, var8.toString());
            }
        }

    }*/

    /**
     * 发图片消息，我修改了很多
     * @param msg
     * @param var2
     */
    private void sendImageMessage(final Message msg, final CallBack var2) {
        File file = null; //final File file = null;
        final ImageMessageBody imgMsgBdy = (ImageMessageBody) msg.body;
        String localurl = imgMsgBdy.localUrl;
        if(localurl != null) {
            file = new File(localurl);
            if(!file.exists()) {
                localurl = this.getThumbnailImagePath(localurl);
                file = new File(localurl);
            }
        }

        if(file != null && file.exists()) {
            boolean var6 = false; //final boolean var6 = false;
            if(!imgMsgBdy.isSendOriginalImage()) {
                String var7 = ImageUtils.getScaledImage(Chat.getInstance().getAppContext(), localurl);
                if(!var7.equals(localurl)) {
                    EMLog.d("sender", "send scaled image:" + var7);
                    var6 = true;
                    file = new File(var7);
                    long var8 = (new File(localurl)).length();
                    long msg0 = file.length();
                    EMLog.d("perf", "original image size:" + var8 + " scaled image size:" + msg0 + " ratio:" + (int)(msg0 / var8) + "%");
                }

                localurl = var7;
            }

            final boolean isFileExists = var6;
            final File file1 = file;
            final long var20 = file.length();
            EMLog.d("sender", "start to send file:" + localurl + " size:" + var20);
            final long var9 = System.currentTimeMillis();
            Options msg1 = ImageUtils.getBitmapOptions(localurl);
            int msg2 = msg1.outWidth;
            int msg3 = msg1.outHeight;
            imgMsgBdy.width = msg2;
            imgMsgBdy.height = msg3;
            final String msg4 = p.getInstance().N();
            EMLog.d("sender", "remote file path:" + msg4);
            ChatOptions msg7 = ChatManager.getInstance().getChatOptions();
            if(msg7.getUseEncryption()) {
                localurl = EncryptUtils.encryptFile(localurl, msg.getTo());
            }

            HashMap msg8 = new HashMap();
            msg8.put("restrict-access", "true");
            String msg9 = ChatManager.getInstance().getAccessToken();
            if(TextUtils.isEmpty(msg9)) {
                msg.status = ProtocolMessage.STATUS.FAIL;
                this.updateMsgState(msg);
                if(var2 != null) {
                    var2.onError(-2, "unauthorized token is null");
                }

            } else {
                msg8.put("Authorization", "Bearer " + msg9);
                HttpClient.getInstance().uploadFile(localurl, msg4, msg8, new EMCloudOperationCallback() {
                    public void onProgress(int msgx) {
                        msg.progress = msgx;
                        if(msgx == 100) {
                            long var2x = System.currentTimeMillis() - var9;
                            EMLog.d("perf", "upload " + msgx + "% file size:" + var20 + "(bytes) time:" + var2x + "(ms) speed:" + (int)(var20 / var2x) + "(byte/ms)");
                        }

                        if(var2 != null) {
                            var2.onProgress(msgx, (String)null);
                            EMLog.d("sender", "sendfile progress:" + msgx);
                        }

                    }

                    public void onError(String msgx) {
                        EMLog.d("sender", "upload error:" + msgx);
                        if(isFileExists) {
                            file1.delete();
                        }

                        msg.status = ProtocolMessage.STATUS.FAIL;
                        SendMessageRunnable.this.updateMsgState(msg);
                        if(var2 != null) {
                            var2.onProgress(100, (String)null);
                            var2.onError(-2, msgx);
                        }

                    }

                    public void onSuccess(String msgx) {
                        String var2x = "";
                        String filex = "";

                        try {
                            JSONObject localurl = new JSONObject(msgx);
                            JSONObject imgMsgBdyx = localurl.getJSONArray("entities").getJSONObject(0);
                            var2x = imgMsgBdyx.getString("uuid");
                            if(imgMsgBdyx.has("share-secret")) {
                                filex = imgMsgBdyx.getString("share-secret");
                            }
                        } catch (Exception msg1) {
                            EMLog.e("sendImageMessage", "json parse exception remotefilepath:" + msg4);
                        }

                        try {
                            String msg3 = "";
                            String msg4x = "";
                            String var6x = "";
                            ChatOptions var7 = ChatManager.getInstance().getChatOptions();
                            if(var7.getUseEncryption()) {
                                EMLog.d("sender", "start to upload encrypted thumbnail");
                                Map var8 = SendMessageRunnable.this.uploadEncryptedThumbnailImage(file1, msg);
                                msg3 = (String)var8.get("uuid");
                                msg4x = (String)var8.get("share-secret");
                                var6x = p.getInstance().N() + msg3;
                                EMLog.d("sender", "encryptd thumbnail uploaded to:" + var6x);
                                if(TextUtils.isEmpty(msg3)) {
                                    if(var2 != null) {
                                        EMLog.e("sender", "upload thumb uuid is null");
                                        var2.onError(-2, "upload file fail ");
                                    }

                                    return;
                                }
                            }

                            long msg5 = System.currentTimeMillis() - var9;
                            EMLog.d("perf", "uploaded file size:" + var20 + "(bytes) time:" + msg5 + "(ms) speed:" + (int)(var20 / msg5) + "(byte/ms)");
                            if(TextUtils.isEmpty(var2x)) {
                                if(var2 != null) {
                                    var2.onError(-2, "upload file fail ");
                                }
                            } else {
                                String msg0 = p.getInstance().N() + var2x;
                                msg0 = msg0.replaceAll("#", "%23");
                                var6x = var6x.replaceAll("#", "%23");
                                imgMsgBdy.remoteUrl = msg0;
                                imgMsgBdy.thumbnailUrl = TextUtils.isEmpty(var6x)?msg0:var6x;
                                imgMsgBdy.secret = filex;
                                imgMsgBdy.thumbnailSecret = msg4x;
                                //SendMessageRunnable.this.sendMessageXmpp(msg);
                                EMLog.d("sender", "sent msg successfully:" + msg.toString());
                            }
                        } catch (Exception msg2) {
                            msg2.printStackTrace();
                            if(var2 != null) {
                                var2.onProgress(100, (String)null);
                                var2.onError(-2, msg2.toString());
                            }
                        }

                        if(isFileExists) {
                            file1.delete();
                        }

                    }
                });
            }
        } else {
            if(var2 != null) {
                var2.onError(-3, "file doesn\'t exist");
            }

        }
    }

    private void sendVideoMessage(final Message var1, final CallBack var2) {
        String var3 = null;
        final VideoMessageBody var4 = (VideoMessageBody)var1.body;
        var3 = var4.localUrl;
        final String var5 = var4.localThumb;
        File var6 = new File(var3);
        if(var3 != null && var6.exists()) {
            if(var5 != null && (new File(var5)).exists()) {
                final String var7 = p.getInstance().N();
                EMLog.d("sender", "remote file path:" + var7);
                ChatOptions var8 = ChatManager.getInstance().getChatOptions();
                if(var8.getUseEncryption()) {
                    var3 = EncryptUtils.encryptFile(var3, var1.getTo());
                }

                HashMap var9 = new HashMap();
                var9.put("restrict-access", "true");
                String var10 = ChatManager.getInstance().getAccessToken();
                if(TextUtils.isEmpty(var10)) {
                    var1.status = ProtocolMessage.STATUS.FAIL;
                    this.updateMsgState(var1);
                    if(var2 != null) {
                        var2.onError(-2, "unauthorized token is null");
                    }

                } else {
                    var9.put("Authorization", "Bearer " + var10);
                    HttpClient.getInstance().uploadFile(var3, var7, var9, new EMCloudOperationCallback() {
                        public void onSuccess(String var1x) {
                            String var2x = "";
                            String var3 = "";

                            try {
                                JSONObject var4x = new JSONObject(var1x);
                                JSONObject var5x = var4x.getJSONArray("entities").getJSONObject(0);
                                var2x = var5x.getString("uuid");
                                if(var5x.has("share-secret")) {
                                    var3 = var5x.getString("share-secret");
                                }
                            } catch (Exception var9) {
                                EMLog.e("sender", "json parse exception remotefilePath:" + var7);
                            }

                            try {
                                String var11 = "";
                                String var12 = "";
                                String var6 = "";
                                EMLog.d("sender", "start to upload encrypted thumbnail");
                                Map var7x = SendMessageRunnable.this.uploadEncryptedThumbnailImage(new File(var5), var1);
                                var11 = (String)var7x.get("uuid");
                                var12 = (String)var7x.get("share-secret");
                                var6 = p.getInstance().N() + var11;
                                EMLog.d("sender", "thumbail uploaded to:" + var6);
                                if(TextUtils.isEmpty(var2x)) {
                                    if(var2 != null) {
                                        var2.onProgress(100, (String)null);
                                        var2.onError(-2, "upload file fail");
                                    }
                                } else if(TextUtils.isEmpty(var11)) {
                                    if(var2 != null) {
                                        var2.onProgress(100, (String)null);
                                        var2.onError(-2, "upload file fail");
                                    }
                                } else {
                                    String var8 = p.getInstance().N() + var2x;
                                    var8 = var8.replaceAll("#", "%23");
                                    var6 = var6.replaceAll("#", "%23");
                                    var4.remoteUrl = var8;
                                    var4.thumbnailUrl = TextUtils.isEmpty(var6)?var8:var6;
                                    var4.secret = var3;
                                    var4.thumbnailSecret = var12;
                                    //SendMessageRunnable.this.sendMessageXmpp(var1);
                                    EMLog.d("sender", "sent msg sucessfully:" + var1.toString());
                                }
                            } catch (Exception var10) {
                                var10.printStackTrace();
                                if(var2 != null) {
                                    var2.onProgress(100, (String)null);
                                    var2.onError(-2, var10.getMessage());
                                }
                            }

                        }

                        public void onProgress(int var1x) {
                            var1.progress = var1x;
                            if(var2 != null) {
                                var2.onProgress(var1x, (String)null);
                            }

                        }

                        public void onError(String var1x) {
                            EMLog.d("sender", "upload error:" + var1x);
                            var1.status = ProtocolMessage.STATUS.FAIL;
                            SendMessageRunnable.this.updateMsgState(var1);
                            if(var2 != null) {
                                var2.onProgress(100, (String)null);
                                var2.onError(-2, var1x);
                            }

                        }
                    });
                }
            } else {
                if(var2 != null) {
                    var2.onError(-3, "video thumb file doesn\'t exist");
                }

            }
        } else {
            if(var2 != null) {
                var2.onError(-3, "video file doesn\'t exist");
            }

        }
    }

    private Map<String, String> uploadEncryptedThumbnailImage(File var1, Message var2) {
        final HashMap var3 = new HashMap();
        byte var4 = 100;
        String var5 = ImageUtils.getThumbnailImage(var1.getAbsolutePath(), var4);
        ChatOptions var6 = ChatManager.getInstance().getChatOptions();
        if(var6.getUseEncryption()) {
            var5 = EncryptUtils.encryptFile(var5, var2.getTo());
        }

        String var7 = p.getInstance().N();
        HashMap var8 = new HashMap();
        var8.put("restrict-access", "true");
        String var9 = ChatManager.getInstance().getAccessToken();
        if(!TextUtils.isEmpty(var9)) {
            var8.put("Authorization", "Bearer " + var9);
        }

        final Object var10 = new Object();
        HttpClient.getInstance().uploadFile(var5, var7, var8, new EMCloudOperationCallback() {
            public void onSuccess(String var1) {
                EMLog.d("sender", "encrypted thumbnail uploaded");
                String var2 = "";
                String var3x = "";

                try {
                    JSONObject var4 = new JSONObject(var1);
                    JSONObject var5 = var4.getJSONArray("entities").getJSONObject(0);
                    var2 = var5.getString("uuid");
                    if(var5.has("share-secret")) {
                        var3x = var5.getString("share-secret");
                    }
                } catch (Exception var7) {
                    ;
                }

                var3.put("uuid", var2);
                var3.put("share-secret", var3x);
                Object var8 = var10;
                synchronized(var10) {
                    var10.notify();
                }
            }

            public void onError(String var1) {
                EMLog.e("sender", "encrypted thumbnail upload error:" + var1);
                Object var2 = var10;
                synchronized(var10) {
                    var10.notify();
                }
            }

            public void onProgress(int var1) {
            }
        });
        synchronized(var10) {
            try {
                var10.wait(60000L);
            } catch (InterruptedException var13) {
                ;
            }

            return var3;
        }
    }

    private void sendFileMessage(final Message var1, final CallBack var2) {
        File var3 = null;
        String var4 = null;
        final FileMessageBody var5 = (FileMessageBody)var1.body;
        var4 = var5.localUrl;
        if(var4 != null) {
            var3 = new File(var4);
        }

        if(var3 != null && var3.exists()) {
            ChatOptions var6 = ChatManager.getInstance().getChatOptions();
            if(var6.getUseEncryption()) {
                var4 = EncryptUtils.encryptFile(var4, var1.getTo());
            }

            final long var7 = var3.length();
            EMLog.d("sender", "start to send file:" + var4 + " size:" + var7);
            final long var9 = System.currentTimeMillis();
            final String var11 = p.getInstance().N();
            HashMap var12 = new HashMap();
            var12.put("restrict-access", "true");
            String var13 = ChatManager.getInstance().getAccessToken();
            if(TextUtils.isEmpty(var13)) {
                var1.status = ProtocolMessage.STATUS.FAIL;
                this.updateMsgState(var1);
                if(var2 != null) {
                    var2.onError(-2, "unauthorized token is null");
                }

            } else {
                var12.put("Authorization", "Bearer " + var13);
                HttpClient.getInstance().uploadFile(var4, var11, var12, new EMCloudOperationCallback() {
                    public void onProgress(int var1x) {
                        var1.progress = var1x;
                        if(var1x == 100) {
                            long var2x = (long)PerfUtils.getTimeSpendSecond(var9);
                            EMLog.d("perf", "upload " + var1x + "% file size(byte)" + var7 + " time(s)" + var2x + " speed(byte/s)" + PerfUtils.getSpeed(var7, System.currentTimeMillis() - var9));
                        }

                        if(var2 != null) {
                            var2.onProgress(var1x, (String)null);
                        }

                    }

                    public void onError(String var1x) {
                        EMLog.d("sender", "upload error:" + var1x);
                        var1.status = ProtocolMessage.STATUS.FAIL;
                        SendMessageRunnable.this.updateMsgState(var1);
                        if(var2 != null) {
                            var2.onProgress(100, (String)null);
                            var2.onError(-2, var1x);
                        }

                    }

                    public void onSuccess(String var1x) {
                        try {
                            String var2x = "";
                            String var3 = "";

                            try {
                                JSONObject var4 = new JSONObject(var1x);
                                JSONObject var5x = var4.getJSONArray("entities").getJSONObject(0);
                                var2x = var5x.getString("uuid");
                                if(var5x.has("share-secret")) {
                                    var3 = var5x.getString("share-secret");
                                }
                            } catch (Exception var7x) {
                                if(var7x != null && var7x.getMessage() != null) {
                                    EMLog.d("sendFileMessage", var7x.getMessage());
                                }
                            }

                            if(TextUtils.isEmpty(var2x)) {
                                if(var2 != null) {
                                    var2.onProgress(100, (String)null);
                                    var2.onError(-2, "upload file fail");
                                }
                            } else {
                                String var9x = var11 + var2x;
                                var9x = var9x.replaceAll("#", "%23").replaceAll(" ", "%20");
                                var5.remoteUrl = var9x;
                                var5.secret = var3;
                                long var10 = (long)PerfUtils.getTimeSpendSecond(var9);
                                EMLog.d("perf", "uploaded file size(bytes)" + var7 + " time(s)" + var10 + " speed(bytes/s)" + PerfUtils.getSpeed(var7, System.currentTimeMillis() - var9));
                                //SendMessageRunnable.this.sendMessageXmpp(var1);
                                EMLog.d("sender", "sent msg successfully:" + var1.toString());
                            }
                        } catch (Exception var8) {
                            var8.printStackTrace();
                            if(var2 != null) {
                                var2.onProgress(100, (String)null);
                                var2.onError(-2, var8.toString());
                            }
                        }

                    }
                });
            }
        } else {
            if(var2 != null) {
                var2.onError(-3, "file doesn\'t exist");
            }

        }
    }

    private void updateMsgState(Message msg) {
        ContentValues var2 = new ContentValues();
        var2.put("status", String.valueOf(msg.status.ordinal()));
        DBManager.getInstance().updateMessage(msg.msgId, var2);
    }

    private String getThumbnailImagePath(String var1) {
        String var2 = var1.substring(0, var1.lastIndexOf("/") + 1);
        var2 = var2 + "th" + var1.substring(var1.lastIndexOf("/") + 1, var1.length());
        EMLog.d("msg", "original image path:" + var1);
        EMLog.d("msg", "thum image path:" + var2);
        return var2;
    }

    private void checkConnection() {
        try {
            ChatManager.getInstance().checkConnection();
        } catch (Exception var6) {
            Object var2 = this.sendMutex;
            synchronized(this.sendMutex) {
                this.addSendMsgLock(this.msg.getMsgId(), this.sendMutex);
                ChatManager.getInstance().tryToReconnectOnGCM();

                try {
                    this.sendMutex.wait(60000L);
                    EMLog.d("sender", "wait send message time out");
                } catch (InterruptedException var4) {
                    var4.printStackTrace();
                }
            }
        }

    }

    synchronized void addSendMsgLock(String var1, Object var2) {
        if(sendMsgLocks == null) {
            sendMsgLocks = new Hashtable();
        }

        sendMsgLocks.put(var1, var2);
    }

    static synchronized void notifySendMsgLocks() {
        if(sendMsgLocks != null && sendMsgLocks.size() != 0) {
            Iterator var1 = sendMsgLocks.values().iterator();

            while(var1.hasNext()) {
                Object var0 = var1.next();
                synchronized(var0) {
                    var0.notify();
                }
            }

            sendMsgLocks.clear();
        }
    }

    static void onInit() {
        sendThreadPool = Executors.newFixedThreadPool(3);
    }

    static void onDestroy() {
        try {
            sendThreadPool.shutdownNow();

            for(SendMessageRunnable var0 = (SendMessageRunnable)pendingMsgQueue.poll(); var0 != null; var0 = (SendMessageRunnable)pendingMsgQueue.poll()) {
                if(var0.msg != null) {
                    var0.msg.status = ProtocolMessage.STATUS.FAIL;
                    if(var0.msg.getType() != ProtocolMessage.TYPE.CMD) {
                        var0.updateMsgState(var0.msg);
                    }
                }
            }
        } catch (Exception var1) {
            var1.printStackTrace();
        }

    }

    public static void onConnected() {
        notifySendMsgLocks();
        flushPendingQueue();
    }
}
