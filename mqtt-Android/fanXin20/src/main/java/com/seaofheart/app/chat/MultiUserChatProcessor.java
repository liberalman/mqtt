package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import com.seaofheart.app.chat.core.r;
import com.seaofheart.app.util.EMLog;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class MultiUserChatProcessor implements r {
    private static final String TAG = "EMMultiUserChatProcessor";
    private Map<String, MultiUserChat> multiUserChats = new ConcurrentHashMap();
    XMPPConnection connection = null;

    MultiUserChatProcessor() {
    }

    public void onInit() {
        //this.connection = SessionManager.getInstance().getConnection();
        this.multiUserChats.clear();
    }

    public void onDestroy() {
        this.multiUserChats.clear();
    }

    void joinMUC(String var1, String var2) throws XMPPException {
        MultiUserChat var3 = (MultiUserChat)this.multiUserChats.get(var1);
        if(var3 == null) {
            var3 = new MultiUserChat(this.connection, var1);
        }

        var3.join(var2);
        EMLog.d("EMMultiUserChatProcessor", "joined muc:" + var1);

        try {
            Collection var4 = var3.getMembers();
            EMLog.d("EMMultiUserChatProcessor", "  room members size:" + var4.size());
            Iterator var6 = var4.iterator();

            while(var6.hasNext()) {
                Affiliate var5 = (Affiliate)var6.next();
                EMLog.d("EMMultiUserChatProcessor", "  member jid:" + var5.getJid() + " role:" + var5.getRole());
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    synchronized MultiUserChat getMUC(String var1) throws XMPPException {
        return this.getMUC(var1, 40000L);
    }

    synchronized MultiUserChat getMUC(String var1, long var2) throws XMPPException {
        if(!var1.contains("@")) {
            var1 = var1 + ChatConfig.MUC_DOMAIN_SUFFIX;
        }

        MultiUserChat var4 = (MultiUserChat)this.multiUserChats.get(var1);
        if(var4 == null) {
            var4 = new MultiUserChat(this.connection, var1);
            this.addMuc(var1, var4);
        }

        if(!var4.isJoined()) {
            String var5 = ChatManager.getInstance().getCurrentUser();
            var4.join(var5, var2);
            com.easemob.util.EMLog.d("EMMultiUserChatProcessor", "joined muc:" + var4.getRoom() + " with eid:" + var5);
        }

        return var4;
    }

    synchronized MultiUserChat getMUCWithoutJoin(String var1) throws XMPPException {
        if(!var1.contains("@")) {
            var1 = var1 + ChatConfig.MUC_DOMAIN_SUFFIX;
        }

        MultiUserChat var2 = (MultiUserChat)this.multiUserChats.get(var1);
        if(var2 == null) {
            var2 = new MultiUserChat(this.connection, var1);
            this.addMuc(var1, var2);
        }

        return var2;
    }

    void deleteMUC(String var1) throws XMPPException {
        MultiUserChat var2 = this.getMUC(var1);
        var2.destroy("delete-group", (String)null);
    }

    void leaveMUC(String var1, String var2) throws XMPPException {
        MultiUserChat var3 = this.getMUC(var1);
        var3.leave();
    }

    void leaveMUCWithoutJoin(String var1) throws XMPPException {
        MultiUserChat var2 = this.getMUCWithoutJoin(var1);
        var2.leaveAndWait(20000L);
    }

    void leaveMUCRemoveMember(String var1, String var2) throws XMPPException {
        MultiUserChat var3 = null;

        try {
            var3 = this.getMUC(var1);
        } catch (XMPPException var7) {
            if(!var7.getMessage().contains("403") && !var7.getMessage().contains("407")) {
                throw new XMPPException(var7);
            }

            return;
        }

        try {
            var3.grantMembership(var2);
        } catch (Exception var6) {
            ;
        }

        var3.leave();

        try {
            var3.revokeMembership(var2);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    void addMuc(String var1, MultiUserChat var2) {
        this.multiUserChats.put(var1, var2);
    }

    void removeMuc(String var1) {
        this.multiUserChats.remove(var1);
    }
}

