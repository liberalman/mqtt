package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.content.Intent;

import com.seaofheart.app.NotifierEvent;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.util.EMLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class OfflineMessageHandler {
    private static final String TAG = "OfflineMessageHandler";
    Set<String> offlineMsgUserSenders = Collections.synchronizedSet(new HashSet());
    Set<String> offlineMsgGrpSenders = Collections.synchronizedSet(new HashSet());
    List<Message> offlineMessages = new ArrayList();
    List<Message> offlineCmdMessagesList = new ArrayList();
    private Message curOfflineMsg;
    private Message preOfflineMsg;
    private static final long OFFLINE_INTERVAL = 3000L;
    private long publishInterval = 3000L;
    private Thread notifyThread = null;
    private boolean needSend = false;

    void onAppReady() {
        this.needSend = true;
    }

    OfflineMessageHandler() {
    }

    void setPublishInterval(long var1) {
        this.publishInterval = var1;
    }

    void onNewOfflineMessage(Message var1) {
        if(!this.offlineMessages.contains(var1)) {
            this.offlineMessages.add(var1);
        }

        this.curOfflineMsg = var1;
        this.startOfflineThread();
        EMLog.d("OfflineMessageHandler", " offline msg, do not send notify for msg:" + var1.msgId);
        String var2;
        if(var1.getChatType() == ProtocolMessage.CHAT_TYPE.CHAT_GROUP) {
            var2 = var1.getTo();
            EMLog.d("notify", "offline group msg");
            if(!this.offlineMsgGrpSenders.contains(var2)) {
                this.offlineMsgGrpSenders.add(var2);
            }
        } else {
            var2 = var1.getFrom();
            if(!this.offlineMsgUserSenders.contains(var2)) {
                this.offlineMsgUserSenders.add(var2);
            }
        }

    }

    void addOfflineCmdMessage(Message var1) {
        if(!this.offlineCmdMessagesList.contains(var1)) {
            this.offlineCmdMessagesList.add(var1);
        }

    }

    void processOfflineCmdMessages() {
        Iterator var2 = this.offlineCmdMessagesList.iterator();

        while(var2.hasNext()) {
            Message var1 = (Message)var2.next();
            ChatManager.getInstance().notifyCmdMsg(var1);
        }

        this.offlineCmdMessagesList.clear();
    }

    void stop() {
        this.sendOfflineBroadcast();
        this.stopOfflineThread();
    }

    synchronized void startOfflineThread() {
        if(this.notifyThread == null || !this.notifyThread.isAlive()) {
            this.notifyThread = new Thread() {
                public void run() {
                    EMLog.d("OfflineMessageHandler", "start offline notify thread...");
                    long var1 = OfflineMessageHandler.this.publishInterval / 1000L;

                    while(true) {
                        try {
                            do {
                                sleep(1000L);
                                --var1;
                            } while(var1 > 0L);

                            var1 = OfflineMessageHandler.this.publishInterval / 1000L;
                            if(OfflineMessageHandler.this.curOfflineMsg == OfflineMessageHandler.this.preOfflineMsg) {
                                if(OfflineMessageHandler.this.curOfflineMsg != null) {
                                    OfflineMessageHandler.this.sendOfflineBroadcast();
                                    OfflineMessageHandler.this.notifyThread = null;
                                    OfflineMessageHandler.this.curOfflineMsg = null;
                                    OfflineMessageHandler.this.preOfflineMsg = null;
                                }

                                return;
                            }

                            OfflineMessageHandler.this.preOfflineMsg = OfflineMessageHandler.this.curOfflineMsg;
                            OfflineMessageHandler.this.sendOfflineBroadcast();
                        } catch (InterruptedException var4) {
                            OfflineMessageHandler.this.notifyThread = null;
                            OfflineMessageHandler.this.curOfflineMsg = null;
                            OfflineMessageHandler.this.preOfflineMsg = null;
                            return;
                        }
                    }
                }
            };
            this.notifyThread.start();
        }
    }

    void stopOfflineThread() {
        if(this.notifyThread != null) {
            this.notifyThread.interrupt();
        }

    }

    private synchronized void sendOfflineBroadcast() {
        if(this.offlineMsgUserSenders.size() > 0 || this.offlineMsgGrpSenders.size() > 0) {
            Intent var1 = new Intent(ChatManager.getInstance().getOfflineMessageBroadcastAction());
            String[] var2 = (String[])this.offlineMsgUserSenders.toArray(new String[0]);
            var1.putExtra("fromuser", var2);
            EMLog.d("OfflineMessageHandler", "send offline message broadcast for users:" + var2.length);
            String[] var3 = (String[])this.offlineMsgGrpSenders.toArray(new String[0]);
            var1.putExtra("fromgroup", var3);
            EMLog.d("OfflineMessageHandler", "send offline message broadcast for groups:" + var3.length);

            try {
                Context var4 = Chat.getInstance().getAppContext();
                if(var4 != null && this.curOfflineMsg != null) {
                    var4.sendOrderedBroadcast(var1, (String)null);
                    ChatManager.getInstance().broadcastMessage(this.curOfflineMsg);
                }
            } catch (Exception var5) {
                var5.printStackTrace();
            }

            this.offlineMsgUserSenders.clear();
            this.offlineMsgGrpSenders.clear();
        }

        if(this.offlineMessages.size() > 0) {
            ArrayList var6 = new ArrayList();
            var6.addAll(this.offlineMessages);
            Notifier.getInstance(Chat.getInstance().getAppContext()).publishEvent(NotifierEvent.Event.EventOfflineMessage, var6);
            this.offlineMessages.clear();
        }

    }

    void reset() {
        this.curOfflineMsg = null;
        this.preOfflineMsg = null;
        if(this.notifyThread != null) {
            this.notifyThread.interrupt();
        }

        this.offlineMsgUserSenders.clear();
        this.offlineMsgGrpSenders.clear();
        this.notifyThread = null;
        this.offlineMessages.clear();
        this.offlineCmdMessagesList.clear();
    }
}

