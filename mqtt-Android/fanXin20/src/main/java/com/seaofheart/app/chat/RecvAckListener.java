package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.text.TextUtils;

import com.seaofheart.app.chat.core.aDefaultPacketExtension;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.util.EMLog;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

class RecvAckListener implements PacketListener {
    private static final String TAG = "acklistener";

    RecvAckListener() {
    }

    public void processPacket(Packet var1) {
        org.jivesoftware.smack.packet.Message var2 = (org.jivesoftware.smack.packet.Message)var1;
        EMLog.d("acklistener", var2.toXML());
        MessageListener.ackMessage(var2);
        if(!this.processClientAckMessage(var2)) {
            PacketExtension var3 = var2.getExtension("urn:xmpp:receipts");
            if(var3 != null && var3.getElementName().equals("received")) {
                String var4 = var2.getBody();
                Message var5 = ChatManager.getInstance().getMessage(var4);
                if(var5 != null && var3 instanceof aDefaultPacketExtension) {
                    String var6 = ((aDefaultPacketExtension)var3).a();
                    if(!TextUtils.isEmpty(var6)) {
                        EMLog.d("acklistener", " found returned global server msg id : " + var6);
                        ChatManager.getInstance().replaceMessageId(var4, var6);
                    }
                }

                EMLog.d("acklistener", "received server ack for msg:" + var4);
                SendMessageRunnable.notifySendLock(var4);
            }

        }
    }

    private synchronized boolean processClientAckMessage(org.jivesoftware.smack.packet.Message var1) {
        PacketExtension var2 = var1.getExtension("urn:xmpp:receipts");
        if(var2 == null) {
            return false;
        } else {
            String var3 = var2.getElementName();
            if(var3.equals("acked")) {
                if(!ChatManager.getInstance().getChatOptions().getRequireAck()) {
                    EMLog.d("acklistener", "msg read ack is not enabled. skip ack msg received");
                    return true;
                } else {
                    EMLog.d("acklistener", "received message read ack for msg id:" + var1.getBody());
                    this.onReadAckReceived(var1);
                    return true;
                }
            } else if(var3.equals("delivery")) {
                if(!ChatManager.getInstance().getChatOptions().getRequireDeliveryAck()) {
                    EMLog.d("acklistener", "msg delivery ack is not enabled. skip ack msg received");
                    return true;
                } else {
                    EMLog.d("acklistener", "received message delivered ack for msg id:" + var1.getBody());
                    this.onDeliveryAckReceived(var1);
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    private void onReadAckReceived(org.jivesoftware.smack.packet.Message var1) {
        String var2 = var1.getBody();
        Message var3 = ChatManager.getInstance().getMessage(var2);
        if(var3 == null) {
            var3 = DBManager.getInstance().getMessage(var2);
        }

        if(var3 != null) {
            String var4 = ContactManager.getUserNameFromEid(var1.getFrom());
            var3.isAcked = true;
            DBManager.getInstance().updateMsgAck(var2, true);
            ChatManager.getInstance().notifiyReadAckMessage(var4, var2);
        }

    }

    private void onDeliveryAckReceived(org.jivesoftware.smack.packet.Message var1) {
        String var2 = var1.getBody();
        Message var3 = ChatManager.getInstance().getMessage(var2);
        if(var3 == null) {
            var3 = DBManager.getInstance().getMessage(var2);
        }

        if(var3 != null) {
            String var4 = ContactManager.getUserNameFromEid(var1.getFrom());
            var3.isDelivered = true;
            DBManager.getInstance().updateMsgDeliver(var2, true);
            ChatManager.getInstance().notifyDeliveryAckMessage(var4, var2);
        }

    }
}
