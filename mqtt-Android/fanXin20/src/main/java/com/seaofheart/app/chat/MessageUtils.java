package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.CallBack;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.util.EMLog;

import org.jivesoftware.smack.packet.Packet;

import java.io.File;

class MessageUtils {
    private static final String TAG = MessageUtils.class.getSimpleName();

    MessageUtils() {
    }

    static int checkMessageError(Message var0) {
        String var1;
        File var2;
        if(var0.getType() == ProtocolMessage.TYPE.FILE) {
            var1 = ((NormalFileMessageBody)var0.getBody()).localUrl;
            var2 = new File(var1);
            if(!var2.exists()) {
                EMLog.e(TAG, "file doesn\'t exists:" + var1);
                return -1012;
            }

            if(var2.length() == 0L) {
                EMLog.e(TAG, "file size is 0:" + var1);
                return -1011;
            }
        } else if(var0.getType() == ProtocolMessage.TYPE.IMAGE) {
            var1 = ((ImageMessageBody)var0.getBody()).localUrl;
            var2 = new File(var1);
            if(!var2.exists()) {
                EMLog.e(TAG, "image doesn\'t exists:" + var1);
                return -1012;
            }

            if(var2.length() == 0L) {
                EMLog.e(TAG, "image size is 0:" + var1);
                return -1011;
            }
        } else if(var0.getType() == ProtocolMessage.TYPE.VOICE) {
            var1 = ((VoiceMessageBody)var0.getBody()).localUrl;
            var2 = new File(var1);
            if(!var2.exists()) {
                EMLog.e(TAG, "voice file doesn\'t exists:" + var1);
                return -1012;
            }

            if(var2.length() == 0L) {
                EMLog.e(TAG, "voice file size is 0:" + var1);
                return -1011;
            }
        } else if(var0.getType() == ProtocolMessage.TYPE.VIDEO) {
            var1 = ((VideoMessageBody)var0.getBody()).localUrl;
            String var5 = ((VideoMessageBody)var0.getBody()).localThumb;
            File var3 = new File(var1);
            File var4 = new File(var5);
            if(!var3.exists()) {
                EMLog.e(TAG, "video file doesn\'t exists:" + var1);
                return -1012;
            }

            if(var3.length() == 0L) {
                EMLog.e(TAG, "video file size is 0:" + var1);
                return -1011;
            }

            if(!var4.exists()) {
                EMLog.e(TAG, "video thumb image doesn\'t exists:" + var5);
                return -1012;
            }

            if(var4.length() == 0L) {
                EMLog.e(TAG, "video thum image size is 0:" + var5);
                return -1011;
            }
        }

        return 0;
    }

    static void asyncCallback(final CallBack var0, final int var1, final String var2) {
        if(var0 != null) {
            (new Thread() {
                public void run() {
                    var0.onError(var1, var2);
                }
            }).start();
        }
    }

    static String getUniqueMessageId() {
        String var0 = Long.toHexString(System.currentTimeMillis());
        var0 = var0.substring(6);
        return Packet.nextID() + "-" + var0;
    }
}

