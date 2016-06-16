package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.os.PowerManager;

import com.seaofheart.app.chat.core.aDefaultPacketExtension;
import com.seaofheart.app.chat.core.AdvanceDebugManager;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.core.x;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.util.DateUtils;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.PathUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MessageListener implements PacketListener {
    private static final String TAG = "chat";
    private static final String INTERNAL_ACTION_PREFIX = "em_";
    protected String previousFrom = "";
    protected String previousBody = "";
    protected long previousTime = System.currentTimeMillis();
    protected ChatManager chatManager = null;
    protected ExecutorService recvThreadPool = null;
    protected long previousPacketTime;
    protected ArrayBlockingQueue<String> recentMsgIdQueue;
    protected static final int RECENT_QUEUE_SIZE = 20;

    public MessageListener(ChatManager var1) {
        this.chatManager = var1;
        this.recvThreadPool = Executors.newCachedThreadPool();
        this.recentMsgIdQueue = new ArrayBlockingQueue(20);
    }

    protected static void ackMessage(org.jivesoftware.smack.packet.Message var0) {
        String var1 = var0.getPacketID();
        if(var1 != null && !var1.equals("")) {
            org.jivesoftware.smack.packet.Message var2 = new org.jivesoftware.smack.packet.Message();
            var2.setPacketID(var1);
            var2.setTo(ChatConfig.DOMAIN);
            var2.setFrom(var0.getTo());
            aDefaultPacketExtension var3 = new aDefaultPacketExtension("received");
            var3.setValue("id", var1);
            var2.addExtension(var3);
            //SessionManager.getInstance().getConnection().sendPacket(var2);
            EMLog.d("chat", "send ack message back to server:" + var2);
            if(var0.getType() == org.jivesoftware.smack.packet.Message.Type.chat && ChatManager.getInstance().getChatOptions().getRequireDeliveryAck()) {
                org.jivesoftware.smack.packet.Message var4 = new org.jivesoftware.smack.packet.Message();
                var4.setTo(var0.getFrom());
                var4.setFrom(var0.getTo());
                aDefaultPacketExtension var5 = new aDefaultPacketExtension("delivery");
                var5.setValue("id", var1);
                var4.addExtension(var5);
                var4.setBody(var1);
                EMLog.d("chat", "send delivered ack msg to:" + var0.getFrom() + " for msg:" + var1);
                var4.setType(org.jivesoftware.smack.packet.Message.Type.normal);
                //SessionManager.getInstance().getConnection().sendPacket(var4);
                DBManager.getInstance().updateMsgAck(var1, true);
            }

        }
    }

    x getRoomTypeExtension(Packet var1) {
        x var2 = null;

        try {
            var2 = (x)var1.getExtension("roomtype", "easemob:x:roomtype");
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return var2;
    }

    x.a getRoomType(Packet var1) {
        x var2 = this.getRoomTypeExtension(var1);
        return var2 != null?var2.a():null;
    }

    protected boolean isDuplicateMsg(org.jivesoftware.smack.packet.Message var1) {
        x var2 = this.getRoomTypeExtension(var1);
        if(var2 != null && var2.a() == x.a.a) {
            return false;
        } else {
            boolean var3 = false;
            if(var1.getFrom().equals(this.previousFrom) && var1.getBody().equals(this.previousBody) && System.currentTimeMillis() - this.previousTime < 1000L) {
                EMLog.d("chat", "ignore duplicate msg with same from and body:" + this.previousFrom);
                var3 = true;
            }

            this.previousFrom = var1.getFrom();
            this.previousBody = var1.getBody();
            this.previousTime = System.currentTimeMillis();
            String var4 = var1.getPacketID();
            if(var4 == null) {
                return var3;
            } else {
                Iterator var6 = this.recentMsgIdQueue.iterator();

                while(var6.hasNext()) {
                    String var5 = (String)var6.next();
                    if(var4.equals(var5)) {
                        EMLog.d("chat", "ignore duplicate msg:" + var1);
                        return true;
                    }
                }

                if(this.recentMsgIdQueue.size() == 20) {
                    try {
                        this.recentMsgIdQueue.poll();
                    } catch (Exception var7) {
                        var7.printStackTrace();
                    }
                }

                this.recentMsgIdQueue.add(var1.getPacketID());
                var3 = false;
                return var3;
            }
        }
    }

    protected boolean processMessage(org.jivesoftware.smack.packet.Message var1) {
        ackMessage(var1);
        if(var1.getBody() != null && !var1.getBody().equals("")) {
            if(this.isDuplicateMsg(var1)) {
                EMLog.d("chat", "ignore duplicate msg");
                return true;
            } else {
                String var2 = StringUtils.parseBareAddress(var1.getFrom());
                EMLog.d("chat", "chat listener receive msg from:" + var2 + " body:" + var1.getBody());
                if(var1.getType() != org.jivesoftware.smack.packet.Message.Type.chat) {
                    return false;
                } else {
                    Message var3 = MessageEncoder.parseXmppMsg(var1);
                    if(var1.getExtension("encrypt", "jabber:client") != null) {
                        var3.setAttribute("isencrypted", true);
                    }

                    return this.processMessage(var3);
                }
            }
        } else {
            return true;
        }
    }

    protected boolean processMessage(Message msg) {
        if(msg == null) {
            return false;
        } else {
            if(msg.getMsgId() == null) {
                msg.msgId = DateUtils.getTimestampStr();
            }

            if(msg.type == ProtocolMessage.TYPE.CMD) {
                this.handleCmdMessage(msg);
                return true;
            } else {
                if(msg.body instanceof FileMessageBody && msg.getType() != ProtocolMessage.TYPE.FILE) {
                    this.setLocalUrl(msg);
                    boolean var2 = msg.getBooleanAttribute("isencrypted", false);
                    ReceiveMessageThread var3 = new ReceiveMessageThread(msg, var2);
                    this.recvThreadPool.execute(var3); // 从线程池中找一个空闲线程，启动接收消息线程
                } else {
                    if(msg.getType() == ProtocolMessage.TYPE.FILE) {
                        this.setLocalUrl(msg);
                    }

                    msg.status = ProtocolMessage.STATUS.SUCCESS;
                }

                ChatManager.getInstance().saveMessage(msg);
                if(msg.offline) {
                    this.chatManager.onNewOfflineMessage(msg);
                } else {
                    this.chatManager.notifyMessage(msg);
                }

                return true;
            }
        }
    }

    private void handleCmdMessage(Message var1) {
        CmdMessageBody var2 = (CmdMessageBody)var1.getBody();
        String var3 = var2.action;
        if(var3.startsWith("em_")) {
            if(this.isAdvanceDebugMessage(var3)) {
                AdvanceDebugManager.a var4 = AdvanceDebugManager.a.valueOf(var3);
                AdvanceDebugManager.getInstance().a(var1, var4);
                return;
            }

            this.handleNormalCmdMessage(var1);
        } else {
            this.handleNormalCmdMessage(var1);
        }

    }

    private boolean isAdvanceDebugMessage(String var1) {
        if(var1.startsWith("em_")) {
            try {
                AdvanceDebugManager.a.valueOf(var1);
                return true;
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

        return false;
    }

    private void handleNormalCmdMessage(Message var1) {
        if(Chat.getInstance().appInited) {
            ChatManager.getInstance().notifyCmdMsg(var1);
        } else {
            ChatManager.getInstance().onNewOfflineCmdMessage(var1);
        }

    }

    protected void setLocalUrl(Message var1) {
        FileMessageBody var2 = (FileMessageBody)var1.body;
        String var3 = var2.remoteUrl.substring(var2.remoteUrl.lastIndexOf("/") + 1);
        if(var1.type == ProtocolMessage.TYPE.IMAGE) {
            var2.localUrl = PathUtil.getInstance().getImagePath() + "/" + var3;
        } else if(var1.type == ProtocolMessage.TYPE.VOICE) {
            if(!ChatManager.getInstance().getChatOptions().getAudioFileWithExt()) {
                var2.localUrl = PathUtil.getInstance().getVoicePath() + "/" + var3;
            } else {
                var2.localUrl = PathUtil.getInstance().getVoicePath() + "/" + var3 + ".amr";
            }
        } else if(var1.type == ProtocolMessage.TYPE.VIDEO) {
            var2.localUrl = PathUtil.getInstance().getVideoPath() + "/" + var3;
        } else if(var1.type == ProtocolMessage.TYPE.FILE) {
            var2.localUrl = PathUtil.getInstance().getFilePath() + "/" + var2.fileName;
        } else {
            var2.localUrl = PathUtil.getInstance().getVideoPath() + "/" + var3;
        }

    }

    public synchronized void processPacket(Packet var1) {
        if(!(var1 instanceof org.jivesoftware.smack.packet.Message)) {
            EMLog.d("chat", "packet is not message, skip");
        } else {
            if(System.currentTimeMillis() - this.previousPacketTime >= 120000L) {
                this.previousPacketTime = System.currentTimeMillis();
                PowerManager.WakeLock var2 = SessionManager.getInstance().getWakeLock();
                if(var2 != null && !var2.isHeld()) {
                    EMLog.d("chat", "temp acquire 3s");
                    var2.acquire(3000L);
                }
            }

            org.jivesoftware.smack.packet.Message var3 = (org.jivesoftware.smack.packet.Message)var1;
            this.processMessage(var3);
        }
    }

    void clear() {
        this.recentMsgIdQueue.clear();
    }
}

