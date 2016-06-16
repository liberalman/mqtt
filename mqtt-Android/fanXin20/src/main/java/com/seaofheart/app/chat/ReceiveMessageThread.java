package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.ContentValues;
import android.text.TextUtils;

import com.seaofheart.app.chat.EMCloudOperationCallback;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.PathUtil;

import java.io.File;
import java.util.HashMap;

class ReceiveMessageThread implements Runnable {
    private static final String TAG = "receiver";
    private Message msg;
    private FileMessageBody msgbody;
    private boolean encrypted = false;

    public ReceiveMessageThread(Message var1) {
        this.msg = var1;
        this.msgbody = (FileMessageBody)var1.body;
    }

    public ReceiveMessageThread(Message var1, boolean var2) {
        this.msg = var1;
        this.msgbody = (FileMessageBody)var1.body;
        this.encrypted = var2;
    }

    public void run() {
        this.msg.status = ProtocolMessage.STATUS.INPROGRESS;
        String var1 = this.msgbody.localUrl;
        String var2 = this.msgbody.remoteUrl;
        String var3 = this.msgbody.fileName;
        if(this.msg.type == ProtocolMessage.TYPE.IMAGE) {
            if(!TextUtils.isEmpty(((ImageMessageBody)this.msgbody).thumbnailUrl)) {
                var2 = ((ImageMessageBody)this.msgbody).thumbnailUrl;
            }
        } else if(this.msg.type != ProtocolMessage.TYPE.VOICE && this.msg.type == ProtocolMessage.TYPE.VIDEO) {
            var2 = ((VideoMessageBody)this.msgbody).thumbnailUrl;
        }

        final String var4;
        if(this.msg.type == ProtocolMessage.TYPE.IMAGE) {
            var3 = "th" + var2.substring(var2.lastIndexOf("/") + 1, var2.length());
            var4 = PathUtil.getInstance().getImagePath() + "/" + var3;
        } else if(this.msg.type == ProtocolMessage.TYPE.VIDEO) {
            var3 = var2.substring(var2.lastIndexOf("/") + 1, var2.length());
            var4 = PathUtil.getInstance().getImagePath() + "/" + var3;
            ((VideoMessageBody)this.msgbody).localThumb = var4;
            ((VideoMessageBody)this.msgbody).localUrl = PathUtil.getInstance().getVideoPath() + "/" + var3 + ".mp4";
        } else if(this.msg.type == ProtocolMessage.TYPE.VOICE) {
            var3 = var2.substring(var2.lastIndexOf("/") + 1, var2.length());
            if(!ChatManager.getInstance().getChatOptions().getAudioFileWithExt()) {
                var4 = PathUtil.getInstance().getVoicePath() + "/" + var3;
            } else {
                var4 = PathUtil.getInstance().getVoicePath() + "/" + var3 + ".amr";
            }

            this.msgbody.localUrl = var4;
        } else if(this.msg.type == ProtocolMessage.TYPE.FILE) {
            var4 = PathUtil.getInstance().getFilePath() + "/" + var3;
            this.msgbody.localUrl = var4;
        } else {
            var4 = var1;
        }

        if(!TextUtils.isEmpty(var3) && !var3.equals("th")) {
            EMLog.d("receiver", "localUrl:" + this.msgbody.localUrl + " remoteurl:" + var2 + " localThumb:" + var4);
            HashMap var6 = new HashMap();
            String var7;
            if(this.msg.type == ProtocolMessage.TYPE.IMAGE) {
                var7 = ((ImageMessageBody)this.msgbody).thumbnailSecret;
                if(TextUtils.isEmpty(var7)) {
                    var7 = this.msgbody.secret;
                }

                if(!TextUtils.isEmpty(var7)) {
                    var6.put("share-secret", var7);
                }
            } else if(this.msg.type == ProtocolMessage.TYPE.VIDEO) {
                var7 = ((VideoMessageBody)this.msgbody).thumbnailSecret;
                if(!TextUtils.isEmpty(var7)) {
                    var6.put("share-secret", var7);
                }
            } else if(this.msg.type == ProtocolMessage.TYPE.VOICE && this.msgbody.secret != null) {
                var6.put("share-secret", this.msgbody.secret);
            }

            if(this.msg.type == ProtocolMessage.TYPE.IMAGE || this.msg.type == ProtocolMessage.TYPE.VIDEO) {
                var6.put("thumbnail", "true");
            }

            HttpClient.getInstance().downloadFile(var2, var4, var6, new EMCloudOperationCallback() {
                public void onSuccess(String var1) {
                    File var2 = new File(var4);
                    EMLog.d("receiver", "file downloaded:" + var4 + " size:" + var2.length());
                    if(ReceiveMessageThread.this.encrypted) {
                        EncryptUtils.decryptFile(var2.getAbsolutePath(), ReceiveMessageThread.this.msg.getFrom());
                    }

                    ReceiveMessageThread.this.msgbody.downloaded = true;
                    ReceiveMessageThread.this.msg.status = ProtocolMessage.STATUS.SUCCESS;
                    ReceiveMessageThread.this.updateMsgState();
                    ReceiveMessageThread.this.msg.progress = 100;
                    if(ReceiveMessageThread.this.msgbody.downloadCallback != null) {
                        ReceiveMessageThread.this.msgbody.downloadCallback.onProgress(100, (String)null);
                        ReceiveMessageThread.this.msgbody.downloadCallback.onSuccess();
                    }

                    if(ReceiveMessageThread.this.msg.messageStatusCallBack != null) {
                        ReceiveMessageThread.this.msg.messageStatusCallBack.onProgress(100, (String)null);
                        ReceiveMessageThread.this.msg.messageStatusCallBack.onSuccess();
                    }

                    if(ReceiveMessageThread.this.msg.type == ProtocolMessage.TYPE.VOICE || ReceiveMessageThread.this.msg.type == ProtocolMessage.TYPE.VIDEO) {
                        ReceiveMessageThread.this.updateMsgBody(ReceiveMessageThread.this.msg);
                    }

                }

                public void onError(String var1) {
                    ReceiveMessageThread.this.msg.status = ProtocolMessage.STATUS.FAIL;
                    EMLog.e("receiver", "download file localThumbnailFilePath:" + var4 + ",error : " + var1);
                    if(var4 != null && (new File(var4)).exists()) {
                        File var2 = new File(var4);

                        try {
                            if(var2.isFile()) {
                                var2.delete();
                            }
                        } catch (Exception var4x) {
                            EMLog.d("receiver", "temp file del fail." + var4);
                        }
                    }

                    ReceiveMessageThread.this.updateMsgState();
                    if(ReceiveMessageThread.this.msgbody.downloadCallback != null) {
                        ReceiveMessageThread.this.msgbody.downloadCallback.onError(-998, var1);
                    }

                    if(ReceiveMessageThread.this.msg.messageStatusCallBack != null) {
                        ReceiveMessageThread.this.msg.messageStatusCallBack.onError(-998, var1);
                    }

                }

                public void onProgress(int var1) {
                    ReceiveMessageThread.this.msg.progress = var1;
                    if(ReceiveMessageThread.this.msgbody.downloadCallback != null) {
                        ReceiveMessageThread.this.msgbody.downloadCallback.onProgress(var1, (String)null);
                    }

                    if(ReceiveMessageThread.this.msg.messageStatusCallBack != null) {
                        ReceiveMessageThread.this.msg.messageStatusCallBack.onProgress(var1, (String)null);
                    }

                }
            });
        } else {
            this.msg.status = ProtocolMessage.STATUS.FAIL;
            this.updateMsgState();
            if(this.msgbody.downloadCallback != null) {
                this.msgbody.downloadCallback.onError(-1, "file name is null or empty");
            }

            if(this.msg.messageStatusCallBack != null) {
                this.msg.messageStatusCallBack.onError(-1, "file name is null or empty");
            }

        }
    }

    protected void updateMsgState() {
        ContentValues var1 = new ContentValues();
        var1.put("status", Integer.valueOf(this.msg.status.ordinal()));
        DBManager.getInstance().updateMessage(this.msg.getMsgId(), var1);
    }

    protected void updateMsgBody(Message var1) {
        ContentValues var2 = new ContentValues();
        var2.put("msgbody", MessageEncoder.getJSONMsg(var1, true));
        DBManager.getInstance().updateMessage(this.msg.getMsgId(), var2);
    }
}

