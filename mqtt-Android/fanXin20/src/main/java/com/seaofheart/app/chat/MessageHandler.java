package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import android.content.ContentValues;
import android.text.TextUtils;

import com.seaofheart.app.CallBack;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.core.aDefaultPacketExtension;
import com.seaofheart.app.chat.core.r;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.PathUtil;

import org.fusesource.mqtt.client.FutureConnection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Packet;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MessageHandler implements r {
    private static final String TAG = MessageHandler.class.getSimpleName();
    private static final MessageHandler instance = new MessageHandler();
    ExecutorService sendThreadPool = Executors.newCachedThreadPool();
    ExecutorService singleThread = Executors.newSingleThreadExecutor();
    long lastSentTime = -1L;
    Object sendLimtLock = new Object();
    /*PacketListener errorMsgLlistener = new PacketListener() {
        private void updateMessage(String var1, int var2) {
            Message var3 = ChatManager.getInstance().getMessage(var1);
            if(var3 != null) {
                ProtocolMessage.STATUS var4 = var3.status;
                var3.setError(var2);
                var3.status = ProtocolMessage.STATUS.FAIL;
                ChatManager.getInstance().updateMessageState(var3);
                if(!SendMessageRunnable.notifyErrorMsgWaitLock(var1)) {
                    MessageChangeEventData var5 = new MessageChangeEventData();
                    var5.source = MessageChangeEventData.ChangeSource.MessageState;
                    var5.changedMsg = var3;
                    var5.setNewValue(ProtocolMessage.STATUS.FAIL);
                    var5.setOldValue(var4);
                    ChatManager.getInstance().notifyMessageChanged(var5);
                }
            }

        }

        public void processPacket(Packet var1) {
            if(var1 != null) {
                if(var1.getError() != null) {
                    String var2 = var1.getPacketID();
                    EMLog.e(MessageHandler.TAG, "received error " + var1.getError() + " , id = " + var2);
                    int var3 = var1.getError().getCode();
                    if(var3 == 406) {
                        this.updateMessage(var2, -2000);
                    } else if(var3 == 408) {
                        Message var4 = ChatManager.getInstance().getMessage(var2);
                        if(var4 == null) {
                            EMLog.d(MessageHandler.TAG, "the message : " + var2 + " is not found in sdk!");
                            return;
                        }

                        this.updateMessage(var2, -2001);
                    } else if(var3 == 500) {
                        this.updateMessage(var2, -2002);
                    }

                }
            }
        }
    };*/

    MessageHandler() {
    }

    public static MessageHandler getInstance() {
        return instance;
    }

    public void sendGroupMessage(FutureConnection futureConnection, Message msg, CallBack callback) {
        try {
            if(msg.getChatType() == null || msg.getChatType() == ProtocolMessage.CHAT_TYPE.CHAT_SINGLE) {
                msg.setChatType(ProtocolMessage.CHAT_TYPE.CHAT_GROUP);
            }

            if(msg.msgId == null) {
                msg.msgId = MessageUtils.getUniqueMessageId();
            }

            if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                ConversationManager.getInstance().saveMessage(msg);
            }

            msg.status = ProtocolMessage.STATUS.INPROGRESS;
            msg.from = SessionManager.getInstance().currentUser;
            String groupId = msg.getTo();
            if(msg.getChatType() == ProtocolMessage.CHAT_TYPE.CHAT_GROUP) {
                EMLog.d(TAG, "start send group message:" + groupId + " message:" + msg.toString());
            } else {
                EMLog.d(TAG, "start send chat room message:" + groupId + " message:" + msg.toString());
            }

            SendMessageRunnable var6 = new SendMessageRunnable(futureConnection, groupId, msg, callback);
            this.sendThreadPool.execute(var6);
        } catch (Exception var5) {
            msg.status = ProtocolMessage.STATUS.FAIL;
            ContentValues var4 = new ContentValues();
            var4.put("status", String.valueOf(msg.status.ordinal()));
            DBManager.getInstance().updateMessage(msg.msgId, var4);
            var5.printStackTrace();
            if(callback != null) {
                MessageUtils.asyncCallback(callback, -2, var5.getLocalizedMessage());
            }
        }

    }

    public void sendMessage(FutureConnection futureConnection, Message msg, CallBack callBack) {
        try {
            if(msg.msgId == null) {
                msg.msgId = MessageUtils.getUniqueMessageId();
            }

            if(msg.getType() != ProtocolMessage.TYPE.CMD) {
                ConversationManager.getInstance().saveMessage(msg); // 消息保存到数据库
            }

            msg.status = ProtocolMessage.STATUS.INPROGRESS;
            msg.from = SessionManager.getInstance().currentUser;
            SendMessageRunnable var4 = new SendMessageRunnable(futureConnection, msg, callBack);
            this.sendThreadPool.execute(var4); // 从线程池中找一个空闲线程执行发送消息的回调类
        } catch (Exception var6) {
            msg.status = ProtocolMessage.STATUS.FAIL;
            ContentValues var5 = new ContentValues();
            var5.put("status", String.valueOf(msg.status.ordinal()));
            DBManager.getInstance().updateMessage(msg.msgId, var5);
            var6.printStackTrace();
            MessageUtils.asyncCallback(callBack, -2, var6.getLocalizedMessage());
        }

    }

    private void postMessageWithTrafficLimt(long var1, FutureConnection futureConnection, Message msg, CallBack callBack) {
        if(this.lastSentTime > 0L) {
            long var6 = System.currentTimeMillis() - this.lastSentTime;
            if(var6 <= var1) {
                Object var8 = this.sendLimtLock;
                synchronized(this.sendLimtLock) {
                    try {
                        this.sendLimtLock.wait(var1 - var6);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        SendMessageRunnable sendMessageRunnable = new SendMessageRunnable(futureConnection, msg, callBack);
        this.sendThreadPool.execute(sendMessageRunnable);
        this.lastSentTime = System.currentTimeMillis();
    }

    public void asyncFetchMessage(final Message msg) {
        final FileMessageBody fileMessageBody = (FileMessageBody)msg.body;
        String var3 = fileMessageBody.localUrl;
        String var4 = fileMessageBody.remoteUrl;
        String var5 = fileMessageBody.fileName;
        if(TextUtils.isEmpty(var4)) {
            if(fileMessageBody.downloadCallback != null) {
                fileMessageBody.downloadCallback.onError(-1, "remoteUrl is null or empty");
            }

            if(msg.messageStatusCallBack != null) {
                msg.messageStatusCallBack.onError(-1, "remoteUrl is null or empty");
            }

        } else {
            if(msg.type == ProtocolMessage.TYPE.IMAGE) {
                if(!TextUtils.isEmpty(((ImageMessageBody)fileMessageBody).thumbnailUrl)) {
                    var4 = ((ImageMessageBody)fileMessageBody).thumbnailUrl;
                }
            } else if(msg.type != ProtocolMessage.TYPE.VOICE && msg.type == ProtocolMessage.TYPE.VIDEO) {
                var4 = ((VideoMessageBody)fileMessageBody).thumbnailUrl;
            }

            msg.status = ProtocolMessage.STATUS.INPROGRESS;
            final String var6;
            if(msg.type == ProtocolMessage.TYPE.IMAGE) {
                var5 = "th" + var4.substring(var4.lastIndexOf("/") + 1, var4.length());
                var6 = PathUtil.getInstance().getImagePath() + "/" + var5;
            } else if(msg.type == ProtocolMessage.TYPE.VIDEO) {
                var5 = var4.substring(var4.lastIndexOf("/") + 1, var4.length());
                var6 = PathUtil.getInstance().getImagePath() + "/" + var5;
                ((VideoMessageBody)fileMessageBody).localThumb = var6;
                ((VideoMessageBody)fileMessageBody).localUrl = PathUtil.getInstance().getVideoPath() + "/" + var5 + ".mp4";
            } else if(msg.type == ProtocolMessage.TYPE.VOICE) {
                var5 = var4.substring(var4.lastIndexOf("/") + 1, var4.length());
                var6 = PathUtil.getInstance().getVoicePath() + "/" + var5;
                fileMessageBody.localUrl = var6;
            } else if(msg.type == ProtocolMessage.TYPE.FILE) {
                var6 = PathUtil.getInstance().getFilePath() + "/" + var5;
                fileMessageBody.localUrl = var6;
            } else {
                var6 = var3;
            }

            if(!TextUtils.isEmpty(var5) && !var5.equals("th")) {
                EMLog.d(TAG, "localUrl:" + fileMessageBody.localUrl + " remoteurl:" + var4);
                HashMap var8 = new HashMap();
                String var9;
                if(msg.type == ProtocolMessage.TYPE.IMAGE) {
                    var9 = ((ImageMessageBody)fileMessageBody).thumbnailSecret;
                    if(TextUtils.isEmpty(var9)) {
                        var9 = fileMessageBody.secret;
                    }

                    if(!TextUtils.isEmpty(var9)) {
                        var8.put("share-secret", var9);
                    }
                } else if(msg.type == ProtocolMessage.TYPE.VIDEO) {
                    var9 = ((VideoMessageBody)fileMessageBody).thumbnailSecret;
                    if(!TextUtils.isEmpty(var9)) {
                        var8.put("share-secret", var9);
                    }
                } else if(msg.type == ProtocolMessage.TYPE.VOICE && fileMessageBody.secret != null) {
                    var8.put("share-secret", fileMessageBody.secret);
                }

                if(msg.type == ProtocolMessage.TYPE.IMAGE || msg.type == ProtocolMessage.TYPE.VIDEO) {
                    var8.put("thumbnail", "true");
                }

                HttpClient.getInstance().downloadFile(var4, var6, var8, new EMCloudOperationCallback() {
                    public void onSuccess(String var1x) {
                        File var2x = new File(var6);
                        EMLog.d(MessageHandler.TAG, "file downloaded:" + var6 + " size:" + var2x.length());
                        if(ChatManager.getInstance().getChatOptions().getUseEncryption()) {
                            EncryptUtils.decryptFile(var2x.getAbsolutePath(), msg.getFrom());
                        }

                        fileMessageBody.downloaded = true;
                        msg.status = ProtocolMessage.STATUS.SUCCESS;
                        MessageHandler.this.updateMsgState(msg);
                        msg.progress = 100;
                        if(fileMessageBody.downloadCallback != null) {
                            fileMessageBody.downloadCallback.onProgress(100, (String)null);
                            fileMessageBody.downloadCallback.onSuccess();
                        }

                        if(msg.messageStatusCallBack != null) {
                            msg.messageStatusCallBack.onProgress(100, (String)null);
                            msg.messageStatusCallBack.onSuccess();
                        }

                        if(msg.type == ProtocolMessage.TYPE.VOICE || msg.type == ProtocolMessage.TYPE.VIDEO) {
                            MessageHandler.this.updateMsgBody(msg);
                        }

                    }

                    public void onError(String var1x) {
                        msg.status = ProtocolMessage.STATUS.FAIL;
                        EMLog.e(MessageHandler.TAG, "download file localThumbnailFilePath:" + var6 + ",error:" + var1x);
                        if(var6 != null && (new File(var6)).exists()) {
                            File var2x = new File(var6);

                            try {
                                if(var2x.isFile()) {
                                    var2x.delete();
                                }
                            } catch (Exception var4) {
                                EMLog.d(MessageHandler.TAG, "temp file del fail." + var6);
                            }
                        }

                        MessageHandler.this.updateMsgState(msg);
                        if(fileMessageBody.downloadCallback != null) {
                            fileMessageBody.downloadCallback.onError(-1, var1x);
                        }

                        if(msg.messageStatusCallBack != null) {
                            msg.messageStatusCallBack.onError(-998, var1x);
                        }

                    }

                    public void onProgress(int var1x) {
                        msg.progress = var1x;
                        if(fileMessageBody.downloadCallback != null) {
                            fileMessageBody.downloadCallback.onProgress(var1x, (String)null);
                        }

                        if(msg.messageStatusCallBack != null) {
                            msg.messageStatusCallBack.onProgress(var1x, (String)null);
                        }

                    }
                });
            } else {
                msg.status = ProtocolMessage.STATUS.FAIL;
                this.updateMsgState(msg);
                if(fileMessageBody.downloadCallback != null) {
                    fileMessageBody.downloadCallback.onError(-1, "fileName is null or empty");
                }

            }
        }
    }

    /**
     * 发送已读回执
     * @param var1
     * @param var2
     * @param var3
     * @throws EaseMobException
     */
    public void ackMessageRead(String var1, String var2, String var3) throws EaseMobException {
        org.jivesoftware.smack.packet.Message var4 = new org.jivesoftware.smack.packet.Message();
        String var5 = ContactManager.getEidFromUserName(var2);

        try {
            aDefaultPacketExtension var6 = new aDefaultPacketExtension("acked");
            var6.setValue("id", var3);
            var4.addExtension(var6);
            var4.setBody(var3);
            EMLog.d(TAG, "send ack msg to:" + var2 + " for msg:" + var3);
            var4.setType(org.jivesoftware.smack.packet.Message.Type.normal);
            var4.setTo(var5);
            String var7 = ContactManager.getEidFromUserName(var1);
            var4.setFrom(var7);
            //SessionManager.getInstance().getConnection().sendPacket(var4);
            Message var8 = ChatManager.getInstance().getMessage(var3);
            if(var8 != null) {
                var8.setAcked(true);
            }

            DBManager.getInstance().updateMsgAck(var3, true);
        } catch (Exception var9) {
            var9.printStackTrace();
            throw new EaseMobException(var9.getMessage());
        }
    }

    private void updateMsgState(Message msg) {
        ContentValues var2 = new ContentValues();
        var2.put("status", Integer.valueOf(msg.status.ordinal()));
        DBManager.getInstance().updateMessage(msg.getMsgId(), var2);
    }

    private void updateMsgBody(Message msg) {
        ContentValues var2 = new ContentValues();
        var2.put("msgbody", MessageEncoder.getJSONMsg(msg, true));
        DBManager.getInstance().updateMessage(msg.getMsgId(), var2);
    }

    void onConnected() {
        SendMessageRunnable.onConnected();
    }

    public void onInit() {
        SendMessageRunnable.onInit();
        //MessageTypeFilter var1 = new MessageTypeFilter(org.jivesoftware.smack.packet.Message.Type.error);
        //XMPPConnection var2 = SessionManager.getInstance().getConnection();
        //var2.addPacketListener(this.errorMsgLlistener, var1);
    }

    public void onDestroy() {
        this.lastSentTime = -1L;
        SendMessageRunnable.onDestroy();
    }
}

