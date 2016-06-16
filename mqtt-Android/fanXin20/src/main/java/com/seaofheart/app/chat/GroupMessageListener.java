package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.chat.core.x;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.util.EMLog;

import org.jivesoftware.smack.util.StringUtils;

class GroupMessageListener extends MessageListener {
    private static final String TAG = "groupchatlistener";
    x extension = null;

    public GroupMessageListener(ChatManager var1) {
        super(var1);
    }

    protected boolean processMessage(org.jivesoftware.smack.packet.Message var1) {
        ackMessage(var1);
        String var2 = var1.getFrom();
        var2.substring(var2.lastIndexOf("/") + 1);
        this.getRoomType(var1);
        if(var1.getBody() != null && !var1.getBody().equals("")) {
            if(this.isDuplicateMsg(var1)) {
                EMLog.d("groupchatlistener", "ignore duplicate msg");
                return true;
            } else {
                String var5 = StringUtils.parseBareAddress(var1.getFrom());
                EMLog.d("groupchatlistener", "groupchat listener receive msg from:" + var5 + " body:" + var1.getBody());
                if(var1.getType() != org.jivesoftware.smack.packet.Message.Type.groupchat) {
                    return false;
                } else {
                    Message var6 = MessageEncoder.parseXmppMsg(var1);
                    if(var6 == null) {
                        return false;
                    } else {
                        this.processGroupMessage(var1, var6);
                        if(var1.getExtension("encrypt", "jabber:client") != null) {
                            var6.setAttribute("isencrypted", true);
                        }

                        return this.processMessage(var6);
                    }
                }
            }
        } else {
            return true;
        }
    }

    private void processGroupMessage(org.jivesoftware.smack.packet.Message var1, Message var2) {
        String var3 = var1.getFrom();
        int var4 = var3.indexOf("/");
        String var6 = null;
        String var5;
        if(var4 > 0) {
            var6 = var3.substring(var4 + 1);
            var5 = var3.substring(0, var4 - 1);
        } else {
            EMLog.d("groupchatlistener", "the message is from muc itself");
            var5 = var3;
            var6 = "EaseMobGroup";
        }

        String var7 = ContactManager.getGroupIdFromEid(var5);
        EMLog.d("groupchatlistener", "group msg groupjid:" + var5 + " groupid:" + var7 + " usrname:" + var6);
        var2.setChatType(ProtocolMessage.CHAT_TYPE.CHAT_GROUP);

        try {
            this.extension = (x)var1.getExtension("roomtype", "easemob:x:roomtype");
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        if(this.extension != null && this.extension.a() == x.a.a) {
            var2.setChatType(ProtocolMessage.CHAT_TYPE.CHAT_ROOM);
        }

        var2.setTo(var7);
    }
}

