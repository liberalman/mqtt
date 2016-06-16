package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Intent;
import android.text.TextUtils;

import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.EMLog;

import org.jivesoftware.smack.packet.Presence;

class PresenceHandler {
    private static final String TAG = PresenceHandler.class.getSimpleName();
    private static PresenceHandler me = new PresenceHandler();

    PresenceHandler() {
    }

    static PresenceHandler getInstance() {
        return me;
    }

    void acceptInvitation(String var1, boolean var2) throws EaseMobException {
        try {
            SessionManager.getInstance().checkConnection();
            Presence var3 = new Presence(Presence.Type.subscribed);
            var3.setMode(Presence.Mode.available);
            var3.setPriority(24);
            var3.setTo(var1);
            var3.setStatus("[resp:" + var2 + "]");
            //SessionManager.getInstance().getConnection().sendPacket(var3);
            if(var2) {
                Presence var4 = new Presence(Presence.Type.subscribe);
                var4.setStatus("[resp:true]");
                var4.setTo(var1);
                //SessionManager.getInstance().getConnection().sendPacket(var4);
            }

        } catch (Exception var5) {
            throw new EaseMobException(var5.getMessage());
        }
    }

    void refuseInvitation(String var1) throws EaseMobException {
        try {
            SessionManager.getInstance().checkConnection();
            Presence var2 = new Presence(Presence.Type.unsubscribed);
            var2.setTo(ContactManager.getEidFromUserName(var1));
            //SessionManager.getInstance().getConnection().sendPacket(var2);
        } catch (Exception var3) {
            throw new EaseMobException(var3.getMessage());
        }
    }

    void processRosterPresence(Presence var1) {
        boolean var2 = false;
        String var5;
        String var6;
        Intent var13;
        if(var1.getType().equals(Presence.Type.subscribe)) {
            String var3 = null;
            boolean var4 = false;
            if(var1.getStatus() != null) {
                var3 = var1.getStatus();
            }

            var5 = "[resp:";
            if(!TextUtils.isEmpty(var3) && var3.startsWith(var5)) {
                var6 = var3.substring(var5.length(), var3.indexOf("]"));
                var4 = Boolean.parseBoolean(var6);
                if(var3.length() > var3.indexOf("]") + 1) {
                    var3 = var3.substring(var3.indexOf("]1"), var3.length());
                } else {
                    var3 = null;
                }
            }

            EMLog.d(TAG, "isresp:" + var4 + " reason:" + var3);
            if(var4) {
                try {
                    this.acceptInvitation(var1.getFrom(), false);
                    if(!var2) {
                        if(var4) {
                            var13 = new Intent(ChatManager.getInstance().getContactInviteEventBroadcastAction());
                            var13.putExtra("username", ContactManager.getUserNameFromEid(var1.getFrom()));
                            var13.putExtra("isResponse", var4);
                            Chat.getInstance().getAppContext().sendBroadcast(var13);
                            if(ContactManager.getInstance().contactListener != null) {
                                ContactManager.getInstance().contactListener.onContactAgreed(ContactManager.getUserNameFromEid(var1.getFrom()));
                            }
                        }

                        var2 = true;
                    }
                } catch (EaseMobException var9) {
                    EMLog.e(TAG, var9.getMessage());
                    var9.printStackTrace();
                }
            } else {
                if(ChatManager.getInstance().getChatOptions().getAcceptInvitationAlways()) {
                    try {
                        EMLog.d(TAG, "auto acceptance inviation from:" + var1.getFrom());
                        this.acceptInvitation(var1.getFrom(), true);
                    } catch (EaseMobException var8) {
                        EMLog.e(TAG, var8.getMessage());
                        var8.printStackTrace();
                    }

                    return;
                }

                var6 = ContactManager.getUserNameFromEid(var1.getFrom());
                Intent var7 = new Intent(ChatManager.getInstance().getContactInviteEventBroadcastAction());
                var7.putExtra("username", var6);
                var7.putExtra("reason", var3);
                var7.putExtra("isResponse", var4);
                Chat.getInstance().getAppContext().sendOrderedBroadcast(var7, (String)null);
                EMLog.d(TAG, "send roster broadcast username:" + var6 + " reason:" + var3 + "resp:" + var4);
                ContactManager.getInstance().contactListener.onContactInvited(var6, var3);
            }
        } else if(var1.getType().equals(Presence.Type.unsubscribe)) {
            ContactManager.getInstance().deleteContactsSet.add(var1.getFrom());
            Presence var12 = new Presence(Presence.Type.unsubscribed);
            var12.setMode(Presence.Mode.available);
            var12.setPriority(24);
            var12.setTo(var1.getFrom());
            //SessionManager.getInstance().getConnection().sendPacket(var12);
        } else if(var1.getType().equals(Presence.Type.subscribed) && !var2) {
            boolean var10 = false;
            String var11 = null;
            if(var1.getStatus() != null) {
                var11 = var1.getStatus();
            }

            var5 = "[resp:";
            if(var11 != null && var11.startsWith(var5)) {
                var6 = var11.substring(var5.length(), var11.indexOf("]"));
                var10 = Boolean.parseBoolean(var6);
            }

            if(var10) {
                var13 = new Intent(ChatManager.getInstance().getContactInviteEventBroadcastAction());
                var13.putExtra("username", ContactManager.getUserNameFromEid(var1.getFrom()));
                var13.putExtra("isResponse", var10);
                Chat.getInstance().getAppContext().sendBroadcast(var13);
                ContactManager.getInstance().contactListener.onContactAgreed(ContactManager.getUserNameFromEid(var1.getFrom()));
            }

            var2 = true;
        }

    }
}
