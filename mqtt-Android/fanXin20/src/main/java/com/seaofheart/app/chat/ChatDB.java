package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.ContentValues;

import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.core.p;

import java.util.List;
import java.util.Map;

public class ChatDB {
    private static String TAG = "chatdb";
    private static ChatDB instance = null;

    private ChatDB() {
    }

    static void initDB(String var0) {
        instance = new ChatDB();
        DBManager.initDB(var0);
    }

    public static ChatDB getInstance() {
        if(instance == null) {
            Exception var0 = new Exception();
            var0.printStackTrace();
        }

        return instance;
    }

    void closeDatabase() {
        DBManager.getInstance().onDestroy();
    }

    boolean saveMessage(Message var1) {
        return DBManager.getInstance().saveMessage(var1);
    }

    public void deleteMessage(String var1) {
        DBManager.getInstance().delMessage(var1);
    }

    public List<String> findAllParticipantsWithMsg() {
        return DBManager.getInstance().getParticipantNames();
    }

    public List<String> findAllGroupsWithMsg() {
        return DBManager.getInstance().getGroupNames();
    }

    public List<Message> findGroupMessages(String var1) {
        return DBManager.getInstance().getMsgList(var1, Message.ChatType.GroupChat);
    }

    public List<Message> findGroupMessages(String var1, String var2, int var3) {
        return DBManager.getInstance().getMsgList(var1, var2, var3);
    }

    public List<Message> findMessages(String var1) {
        return DBManager.getInstance().getMsgList(var1, Message.ChatType.Chat);
    }

    public List<Message> findMessages(String var1, String startMsgId, int count) {
        return DBManager.getInstance().getMessages(var1, startMsgId, count);
    }

    public void deleteConversions(String participaint) {
        DBManager.getInstance().delParticipaintMessages(participaint);
    }

    public void updateMessageAck(String var1, boolean var2) {
        DBManager.getInstance().updateMsgAck(var1, var2);
    }

    public void updateMessageDelivered(String var1, boolean var2) {
        DBManager.getInstance().updateMsgDeliver(var1, var2);
    }

    public void updateMessage(String msgId, ContentValues var2) {
        DBManager.getInstance().updateMessage(msgId, var2);
    }

    public Map<String, Group> loadAllGroups() {
        return DBManager.getInstance().getGroups();
    }

    public Group loadGroup(String groupname) {
        return DBManager.getInstance().getGroup(groupname);
    }

    public void updateGroup(Group var1) {
        DBManager.getInstance().updateGroup(var1);
    }

    public void deleteGroup(String gruopId) {
        DBManager.getInstance().delGroup(gruopId);
    }

    public void deleteGroupConversions(String groupname) {
        DBManager.getInstance().delGroupMessages(groupname);
    }

    public boolean importMessage(Message var1) {
        return DBManager.getInstance().c(var1);
    }

    public List<String> getConversationsUnread() {
        return DBManager.getInstance().h();
    }

    public void saveToken(String var1, p.token var2) {
        DBManager.getInstance().setToken(var1, var2);
    }

    public p.token getToken(String var1) {
        return DBManager.getInstance().getToken(var1);
    }
}

