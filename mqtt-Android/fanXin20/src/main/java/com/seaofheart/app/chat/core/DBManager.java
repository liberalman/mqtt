package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.chat.ChatManager;
import com.seaofheart.app.chat.ChatRoom;
import com.seaofheart.app.chat.Contact;
import com.seaofheart.app.chat.Conversation;
import com.seaofheart.app.chat.Group;
import com.seaofheart.app.chat.KeywordSearchInfo;
import com.seaofheart.app.chat.Message;
import com.seaofheart.app.chat.MessageEncoder;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.util.EMLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class DBManager { // class i
    private static String TAG = "DBManager"; // EMDBManager
    public static final String a = "_emmsg.db";
    private static final int e = 12;
    private static final String f = "_id";
    private static final String g = "msgid";
    private static final String h = "msgtime";
    private static final String i = "msgdir";
    private static final String j = "participant";
    public static final String b = "msgbody";
    private static final String k = "groupname";
    private static final String l = "isacked";
    private static final String m = "isdelivered";
    public static final String c = "status";
    private static final String n = "islistened";
    private static final String o = "msgtype";
    private static final String p = "chat";
    private static final String q = "Group";
    private static final String r = "name";
    private static final String s = "nick";
    private static final String t = "desc";
    private static final String u = "owner";
    private static final String v = "members";
    private static final String w = "members_size";
    private static final String x = "modifiedtime";
    private static final String y = "jid";
    private static final String z = "ispublic";
    private static final String A = "isblocked";
    private static final String B = "max_users";
    private static final String C = "chatroom";
    private static final String D = "name";
    private static final String E = "nick";
    private static final String F = "desc";
    private static final String G = "owner";
    private static final String H = "members";
    private static final String I = "members_size";
    private static final String J = "isblocked";
    private static final String K = "max_users";
    private static final String L = "unreadcount";
    private static final String M = "username";
    private static final String N = "count";
    private static final String O = "token";
    private static final String P = "username";
    private static final String Q = "value";
    private static final String R = "saved_time";
    private static final String S = "contact";
    private static final String T = "jid";
    private static final String U = "username";
    private static final String V = "nick";
    private static final String W = "black_list";
    private static final String X = "username";
    private static final String Y = "conversation_list";
    private static final String Z = "username";
    private static final String aa = "groupname";
    private static final String ab = "ext";
    private static final String ac = "conversation_type";
    private static final String ad = "create table chat (_id integer primary key autoincrement, msgid text, msgtime integer, msgdir integer, isacked integer, isdelivered integer, status integer,participant text not null, islistened integer, msgbody text not null,msgtype integer, groupname text);";
    private static final String ae = "create table Group (name text primary key, jid text not null, nick text not null, owner text not null, modifiedtime integer, ispublic integer, desc text, members_size integer, isblocked integer, members text, max_users integer);";
    private static final String af = "create table chatroom (name text primary key, nick text, owner text, desc text, members_size integer, isblocked integer, members text, max_users integer);";
    private static final String ag = "create table unreadcount (username text primary key, count integer);";
    private static final String ah = "create table token (username text primary key, value text, saved_time integer);";
    private static final String ai = "create table contact (jid text primary key, username text, nick );";
    private static final String aj = "create table black_list (username text primary key);";
    private static final String ak = "create table if not exists conversation_list (username text primary key, groupname text, ext text, conversation_type integer);";
    private static DBManager instance = null; // al
    private boolean am = true;
    private String dbname = null; // an
    private Context context; // ao
    private boolean ap = false;

    private DBManager() {
    }

    public static synchronized void initDB(String dbname) {
        EMLog.e(TAG, "initDB : " + dbname);
        // 对于重复初始化的，这里释放并重新new
        if(instance != null) {
            if(instance.dbname != null && instance.dbname.equals(dbname)) {
                return;
            }

            instance.onDestroy();
        }

        if(instance == null) {
            instance = new DBManager();
            instance.context = Chat.getInstance().getAppContext();
        }

        instance.dbname = dbname;
        instance.am = false;
    }

    public static synchronized DBManager getInstance() {
        if(instance == null) {
            EMLog.e(TAG, "Please login first!");
            throw new IllegalStateException("Please login first!");
        } else {
            return instance;
        }
    }

    public synchronized void onDestroy() {
        try {
            DBManager.iSQLiteOpenHelper.closeDB();
            EMLog.d(TAG, "close msg db");
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    private void OnCreate() {
        SQLiteDatabase var1 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();

        try {
            var1.execSQL("drop table chat");
            var1.execSQL("drop table Group");
            var1.execSQL("drop table unreadcount");
            var1.execSQL("drop table token");
            var1.execSQL("drop table contact");
            var1.execSQL("drop table black_list");
            var1.execSQL("drop table conversation_list");
            var1.execSQL("create table chat (_id integer primary key autoincrement, msgid text, msgtime integer, msgdir integer, isacked integer, isdelivered integer, status integer,participant text not null, islistened integer, msgbody text not null,msgtype integer, groupname text);");
            var1.execSQL("create table Group (name text primary key, jid text not null, nick text not null, owner text not null, modifiedtime integer, ispublic integer, desc text, members_size integer, isblocked integer, members text, max_users integer);");
            var1.execSQL("create table unreadcount (username text primary key, count integer);");
            var1.execSQL("create table token (username text primary key, value text, saved_time integer);");
            var1.execSQL("create table contact (jid text primary key, username text, nick );");
            var1.execSQL("create table black_list (username text primary key);");
            var1.execSQL("create table if not exists conversation_list (username text primary key, groupname text, ext text, conversation_type integer);");
        } catch (Exception var3) {
            ;
        }

    }

    private SQLiteDatabase getSqliteDb() {
        return DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
    }

    /**
     * 保存新消息
     * @param msg
     * @return
     */
    public boolean saveMessage(Message msg) {
        try {
            if(msg.getChatType() == ProtocolMessage.CHAT_TYPE.CHAT_ROOM && this.isMessageExist(msg.getMsgId())) {
                return true;
            } else {
                SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
                ContentValues var3 = new ContentValues();
                var3.put("msgid", msg.getMsgId());
                var3.put("msgtime", Long.valueOf(msg.getMsgTime()));
                var3.put("isacked", Boolean.valueOf(msg.isAcked));
                var3.put("isdelivered", Boolean.valueOf(msg.isDelivered));
                var3.put("msgdir", Integer.valueOf(msg.direct.ordinal()));
                var3.put("msgtype", Integer.valueOf(msg.getChatType().ordinal()));
                ProtocolMessage.STATUS var4 = msg.status;
                if(var4 == ProtocolMessage.STATUS.INPROGRESS) {
                    var4 = ProtocolMessage.STATUS.CREATE;
                }

                var3.put("status", Integer.valueOf(var4.ordinal()));
                String var5;
                if(msg.getFrom().equals(this.dbname)) {
                    var5 = msg.getTo();
                } else {
                    var5 = msg.getFrom();
                }

                String var6 = var5;
                var3.put("participant", var5);
                var3.put("msgbody", MessageEncoder.getJSONMsg(msg, true));
                if(msg.getChatType() == ProtocolMessage.CHAT_TYPE.CHAT_SINGLE) {
                    var3.putNull("groupname");
                } else {
                    var3.put("groupname", msg.getTo());
                    var6 = msg.getTo();
                }

                var3.put("islistened", Integer.valueOf(msg.isListened()?1:0));
                if(!var5.equals("bot")) {
                    var2.insert("chat", (String)null, var3);
                }

                boolean var7 = msg.getChatType() != ProtocolMessage.CHAT_TYPE.CHAT_SINGLE;
                String var8 = !var7?var5:(String)var3.get("groupname");
                this.a(var8, Conversation.msgType2ConversationType(var6, msg.getChatType()));
                EMLog.d(TAG, "save msg to db");
                return true;
            }
        } catch (Exception var9) {
            var9.printStackTrace();
            EMLog.e(TAG, "save msg has error: " + var9);
            return false;
        }
    }

    /**
     * 更新已有消息
     * @param msg
     * @return
     */
    public boolean updateMessage(Message msg) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var3 = new ContentValues();
            String var4 = msg.getMsgId();
            String var5 = MessageEncoder.getJSONMsg(msg, true);
            var3.put("msgbody", var5);
            var2.update("chat", var3, "msgid = ?", new String[]{var4});
            EMLog.d(TAG, "update msg:" + var4 + " messagebody:" + var5);
            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    /**
     * 删除消息
     * @param msgId
     */
    public void delMessage(String msgId) {
        try {
            SQLiteDatabase db = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            String var3 = "";
            boolean isGroup = false;
            Cursor var5 = db.rawQuery("select participant, groupname from chat where msgid = ? limit 1", new String[]{msgId});
            if(!var5.moveToFirst()) {
                var5.close();
                return;
            }

            if(var5.getString(1) == null) {
                var3 = var5.getString(0);
                isGroup = false;
            } else {
                var3 = var5.getString(1);
                isGroup = true;
            }

            var5.close();
            int var6 = db.delete("chat", "msgid = ?", new String[]{msgId});
            EMLog.d(TAG, "delete msg:" + msgId + " return:" + var6);
            this.delFormConversationList(var3, isGroup);
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    private String a(Cursor var1) {
        if(var1 == null) {
            return "";
        } else {
            boolean var2 = !var1.isNull(var1.getColumnIndex("groupname"));
            String var3 = !var2?var1.getString(var1.getColumnIndex("participant")):var1.getString(var1.getColumnIndex("groupname"));
            return var3;
        }
    }

    Conversation.ConversationType getConversationType(boolean isGroup, String name, SQLiteDatabase db) {
        String var4 = isGroup ? "groupname":"username";
        Cursor var5 = db.rawQuery("select conversation_type from conversation_list where " + var4 + " = ?", new String[]{name});
        var5.moveToFirst();
        int var6 = var5.getInt(0);
        var5.close();
        return this.b(var6);
    }

    public Hashtable<String, Conversation> c() {
        Hashtable var1 = new Hashtable();

        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            String var3 = "SELECT *, MAX(msgtime), COUNT(*) AS msgCount FROM chat AS A  where A.groupname is null and A.participant in (select username from conversation_list) GROUP BY participant ";
            String var4 = "SELECT *, MAX(msgtime), COUNT(*) AS msgCount FROM chat AS A  where A.groupname is not null and A.groupname in (select groupname from conversation_list) GROUP BY groupname ";
            String[] var5 = new String[]{var3, var4};
            String[] var9 = var5;
            int var8 = var5.length;

            for(int var7 = 0; var7 < var8; ++var7) {
                String var6 = var9[var7];
                Cursor cursor = var2.rawQuery(var6, (String[])null);
                if(!cursor.moveToFirst()) {
                    cursor.close();
                } else {
                    do {
                        Message var11 = this.cursor2Message(cursor);
                        long var12 = cursor.getLong(cursor.getColumnIndex("msgCount"));
                        LinkedList var14 = new LinkedList();
                        var14.add(var11);
                        boolean var15 = !cursor.isNull(cursor.getColumnIndex("groupname"));
                        String var16 = this.a(cursor);
                        Conversation.ConversationType var17 = this.getConversationType(var15, var16, var2);
                        Conversation var18 = new Conversation(var16, var14, var17, Long.valueOf(var12));
                        var1.put(var16, var18);
                    } while(cursor.moveToNext());

                    cursor.close();
                }
            }
        } catch (Exception var19) {
            var19.printStackTrace();
        }

        return var1;
    }

    private Conversation.ConversationType b(int var1) {
        return var1 == Conversation.ConversationType.Chat.ordinal()? Conversation.ConversationType.Chat:(var1 == Conversation.ConversationType.ChatRoom.ordinal()? Conversation.ConversationType.ChatRoom:(var1 == Conversation.ConversationType.GroupChat.ordinal()? Conversation.ConversationType.GroupChat:(var1 == Conversation.ConversationType.HelpDesk.ordinal()? Conversation.ConversationType.HelpDesk: Conversation.ConversationType.Chat)));
    }

    public Hashtable<String, Conversation> a(int var1) {
        Hashtable var2 = new Hashtable();
        Message var3 = null;

        try {
            SQLiteDatabase var4 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            String var5 = "select * from chat where groupname is null and participant in (select username from conversation_list) order by participant, msgtime desc";
            String var6 = "select * from chat where groupname is not null and groupname in (select groupname from conversation_list) order by groupname, msgtime desc";
            String[] var7 = new String[]{var5, var6};
            String[] var11 = var7;
            int var10 = var7.length;

            for(int var9 = 0; var9 < var10; ++var9) {
                String var8 = var11[var9];
                Cursor var12 = var4.rawQuery(var8, (String[])null);
                if(!var12.moveToFirst()) {
                    var12.close();
                } else {
                    LinkedList var13 = null;
                    String var14 = null;
                    long var15 = 0L;
                    boolean var17 = false;
                    Conversation.ConversationType var18 = Conversation.ConversationType.Chat;

                    do {
                        String var19 = this.a(var12);
                        if(var14 != null && var14.equals(var19)) {
                            if(var13.size() < var1) {
                                var3 = this.cursor2Message(var12);
                                var13.add(var3);
                            }

                            ++var15;
                        } else if(var14 == null || !var14.equals(var19)) {
                            if(var14 != null) {
                                Collections.reverse(var13);
                                Conversation var20 = new Conversation(var14, var13, var18, Long.valueOf(var15));
                                var2.put(var14, var20);
                            }

                            var13 = new LinkedList();
                            var13.add(this.cursor2Message(var12));
                            var17 = !var12.isNull(var12.getColumnIndex("groupname"));
                            var14 = var19;
                            var15 = 1L;
                            var18 = this.getConversationType(var17, var19, var4);
                        }
                    } while(var12.moveToNext());

                    if(var14 != null) {
                        Collections.reverse(var13);
                        Conversation var22 = new Conversation(var14, var13, var18, Long.valueOf(var15));
                        var2.put(var14, var22);
                    }

                    var12.close();
                }
            }
        } catch (Exception var21) {
            var21.printStackTrace();
        }

        return var2;
    }

    /**
     * 获单聊或在群聊中的会话消息数量
     * @param name
     * @param isGroup true 群聊,false 单聊
     * @return
     */
    public long getMessageCount(String name, boolean isGroup) {
        SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();

        try {
            String type = !isGroup ? "participant":"groupname"; // 一对一会话 : 群组会话
            Cursor var5 = var3.rawQuery("select count(*) as msgCount from chat where " + type + " = ?", new String[]{name});
            if(!var5.moveToFirst()) {
                var5.close();
                return 0L;
            } else {
                long var6 = var5.getLong(0);
                var5.close();
                return var6;
            }
        } catch (Exception var8) {
            var8.printStackTrace();
            return 0L;
        }
    }

    /**
     * 获取消息
     * @param msgId
     * @return
     */
    public Message getMessage(String msgId) {
        Message msg = null;

        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor cursor = var3.rawQuery("select * from chat where msgid = ?", new String[]{msgId});
            if(!cursor.moveToFirst()) {
                cursor.close();
                return msg;
            }

            msg = this.cursor2Message(cursor);
            cursor.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        EMLog.d(TAG, "load msg msgId:" + msgId);
        return msg;
    }

    /**
     * 检查消息是否存在
     * @param msgId
     * @return
     */
    public boolean isMessageExist(String msgId) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var3 = var2.rawQuery("select * from chat where msgid = ?", new String[]{msgId});
            if(!var3.moveToFirst()) {
                var3.close();
                return false;
            }

            var3.close();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return true;
    }

    /**
     * 获取一对一聊天列表
     * @return 返回用户id列表
     */
    public List<String> getParticipantNames() {
        ArrayList var1 = new ArrayList();

        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var3 = var2.rawQuery("select distinct username from conversation_list where groupname is null", (String[])null);
            if(!var3.moveToFirst()) {
                var3.close();
                return var1;
            }

            do {
                var1.add(var3.getString(0));
            } while(var3.moveToNext());

            var3.close();
            EMLog.d(TAG, "load participants size:" + var1.size());
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return var1;
    }

    /**
     * 获取群组聊天列表
     * @return 返回群组id列表
     */
    public List<String> getGroupNames() {
        ArrayList var1 = new ArrayList();

        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var3 = var2.rawQuery("select distinct groupname from conversation_list where username is null", (String[])null);
            if(!var3.moveToFirst()) {
                var3.close();
                return var1;
            }

            do {
                var1.add(var3.getString(0));
            } while(var3.moveToNext());

            var3.close();
            EMLog.d(TAG, "load msg groups size:" + var1.size());
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return var1;
    }

    /**
     * 根据聊天类型获取单聊或群聊消息列表
     * @param name 单聊对方用户名，或群聊群名称
     * @param type
     * @return
     */
    public List<Message> getMsgList(String name, Message.ChatType type) {
        ArrayList var3 = new ArrayList();

        try {
            SQLiteDatabase var4 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor cursor = null;
            if(type == Message.ChatType.Chat) {
                cursor = var4.rawQuery("select * from chat where participant = ? and groupname = null order by msgtime", new String[]{name});
            } else {
                cursor = var4.rawQuery("select * from chat where groupname = ? order by msgtime", new String[]{name});
            }

            if(!cursor.moveToFirst()) {
                cursor.close();
                return var3;
            }

            do {
                Message var6 = this.cursor2Message(cursor);
                var3.add(var6);
            } while(cursor.moveToNext());

            cursor.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        EMLog.d(TAG, "load msgs size:" + var3.size() + " for username:" + name);
        return var3;
    }

    /**
     * 获取群聊最近数条消息
     * @param groupId 群组id
     * @param startMsgId 起始消息id
     * @param count 消息数量
     * @return
     */
    public List<Message> getMsgList(String groupId, String startMsgId, int count) {
        ArrayList var4 = new ArrayList();

        try {
            SQLiteDatabase var5 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var6 = null;
            Message var7;
            if(startMsgId != null) {
                var7 = ChatManager.getInstance().getMessage(startMsgId);
                if(var7 == null) {
                    EMLog.e(TAG, "can\'t find message for startMsgId");
                    return var4;
                }

                var6 = var5.rawQuery("select * from chat where groupname = ? and msgtime < ? order by msgtime desc limit ?", new String[]{groupId, String.valueOf(var7.getMsgTime()), String.valueOf(count)});
            } else {
                var6 = var5.rawQuery("select * from chat where groupname = ? order by msgtime desc limit ?", new String[]{groupId, String.valueOf(count)});
            }

            if(!var6.moveToLast()) {
                var6.close();
                return var4;
            }

            do {
                var7 = this.cursor2Message(var6);
                var4.add(var7);
            } while(var6.moveToPrevious());

            var6.close();
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        EMLog.d(TAG, "load msgs size:" + var4.size() + " for groupid:" + groupId);
        return var4;
    }

    /**
     * 获取群聊数条消息
     * @param groupId
     * @param startMsgId
     * @param count
     * @param before true 当前时间点之前的消息，false 当前时间点之后的消息
     * @return
     */
    public List<Message> getMsgList(String groupId, String startMsgId, int count, boolean before) {
        ArrayList var5 = new ArrayList();
        if(count <= 0) {
            count = 20;
        }

        try {
            SQLiteDatabase var6 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var7 = null;
            Message var8;
            if(startMsgId != null) {
                var8 = ChatManager.getInstance().getMessage(startMsgId);
                if(var8 == null) {
                    var8 = this.getMessage(startMsgId);
                    if(var8 == null) {
                        EMLog.e(TAG, "can\'t find message for startMsgId");
                        return var5;
                    }
                }

                if(!before) {
                    var7 = var6.rawQuery("select * from chat where groupname = ? and msgtime > ? order by msgtime asc limit ?", new String[]{groupId, String.valueOf(var8.getMsgTime()), String.valueOf(count)});
                } else {
                    var7 = var6.rawQuery("select * from chat where groupname = ? and msgtime < ? order by msgtime desc limit ?", new String[]{groupId, String.valueOf(var8.getMsgTime()), String.valueOf(count)});
                }
            } else {
                var7 = var6.rawQuery("select * from chat where groupname = ? order by msgtime desc limit ?", new String[]{groupId, String.valueOf(count)});
            }

            if(!before) {
                if(!var7.moveToNext()) {
                    var7.close();
                    return var5;
                }

                do {
                    var8 = this.cursor2Message(var7);
                    var5.add(var8);
                } while(var7.moveToNext());
            } else {
                if(!var7.moveToLast()) {
                    var7.close();
                    return var5;
                }

                do {
                    var8 = this.cursor2Message(var7);
                    var5.add(var8);
                } while(var7.moveToPrevious());
            }

            var7.close();
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return var5;
    }

    /**
     * 获取单聊数条消息
     * @param participant
     * @param before
     * @param startMsgId
     * @param count
     * @return
     */
    public List<Message> getMsgList(String participant, boolean before, String startMsgId, int count) {
        ArrayList var5 = new ArrayList();
        if(count <= 0) {
            count = 20;
        }

        try {
            SQLiteDatabase var6 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var7 = null;
            Message var8;
            if(startMsgId != null) {
                var8 = ChatManager.getInstance().getMessage(startMsgId);
                if(var8 == null) {
                    var8 = this.getMessage(startMsgId);
                    if(var8 == null) {
                        EMLog.e(TAG, "can\'t find message for startMsgId");
                        return var5;
                    }
                }

                if(!before) {
                    var7 = var6.rawQuery("select * from chat where participant = ? and msgtime > ? and groupname is null order by msgtime asc limit ?", new String[]{participant, String.valueOf(var8.getMsgTime()), String.valueOf(count)});
                } else {
                    var7 = var6.rawQuery("select * from chat where participant = ? and msgtime < ? and groupname is null order by msgtime desc limit ?", new String[]{participant, String.valueOf(var8.getMsgTime()), String.valueOf(count)});
                }
            } else {
                var7 = var6.rawQuery("select * from chat where participant = ? and groupname is null order by msgtime desc limit ?", new String[]{participant, String.valueOf(count)});
            }

            if(!before) {
                if(!var7.moveToNext()) {
                    var7.close();
                    return var5;
                }

                do {
                    var8 = this.cursor2Message(var7);
                    var5.add(var8);
                } while(var7.moveToNext());
            } else {
                if(!var7.moveToLast()) {
                    var7.close();
                    return var5;
                }

                do {
                    var8 = this.cursor2Message(var7);
                    var5.add(var8);
                } while(var7.moveToPrevious());
            }

            var7.close();
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return var5;
    }

    /**
     * 获取同某人对话的最近数条消息
     * @param participant 对方用户名
     * @param startMsgId 起始消息id
     * @param count 消息数量
     * @return
     */
    public List<Message> getMessages(String participant, String startMsgId, int count) {
        ArrayList arrayList = new ArrayList();

        try {
            SQLiteDatabase db = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor cursor = null;
            Message msg;
            if(startMsgId != null) { // 有起始id，把小于起始id的都查出来
                msg = ChatManager.getInstance().getMessage(startMsgId);
                if(msg == null) {
                    EMLog.e(TAG, "can\'t find message for startMsgId");
                    return arrayList;
                }

                cursor = db.rawQuery("select * from chat where participant = ? and msgtime < ? and groupname is null order by msgtime desc limit ?", new String[]{participant, String.valueOf(msg.getMsgTime()), String.valueOf(count)});
            } else { // 无起始id
                cursor = db.rawQuery("select * from chat where participant = ? and groupname is null order by msgtime desc limit ?", new String[]{participant, String.valueOf(count)});
            }

            if(!cursor.moveToLast()) {
                cursor.close();
                return arrayList;
            }

            // 翻转下顺序，输出
            do {
                msg = this.cursor2Message(cursor);
                arrayList.add(msg);
            } while(cursor.moveToPrevious());

            cursor.close();
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        EMLog.d(TAG, "load msgs size:" + arrayList.size() + " for participant:" + participant);
        return arrayList;
    }

    private Message cursor2Message(Cursor cursor) {
        String var2 = cursor.getString(cursor.getColumnIndex("msgbody"));
        Message msg = MessageEncoder.getMsgFromJson(var2);
        msg.setMsgId(cursor.getString(cursor.getColumnIndex("msgid")));
        msg.setMsgTime(cursor.getLong(cursor.getColumnIndex("msgtime")));
        int var4 = cursor.getInt(cursor.getColumnIndex("msgdir"));
        if(var4 == Message.Direct.SEND.ordinal()) {
            msg.direct = ProtocolMessage.DIRECT.SEND;
        } else {
            msg.direct = ProtocolMessage.DIRECT.RECEIVE;
        }

        int var5 = cursor.getInt(cursor.getColumnIndex("status"));
        if(var5 == ProtocolMessage.STATUS.CREATE.ordinal()) {
            msg.status = ProtocolMessage.STATUS.CREATE;
        } else if(var5 == ProtocolMessage.STATUS.INPROGRESS.ordinal()) {
            msg.status = ProtocolMessage.STATUS.INPROGRESS;
        } else if(var5 == ProtocolMessage.STATUS.SUCCESS.ordinal()) {
            msg.status = ProtocolMessage.STATUS.SUCCESS;
        } else if(var5 == ProtocolMessage.STATUS.FAIL.ordinal()) {
            msg.status = ProtocolMessage.STATUS.FAIL;
        }

        int var6 = cursor.getInt(cursor.getColumnIndex("isacked"));
        if(var6 == 0) {
            msg.isAcked = false;
        } else {
            msg.isAcked = true;
        }

        int var7 = cursor.getInt(cursor.getColumnIndex("isdelivered"));
        if(var7 == 0) {
            msg.isDelivered = false;
        } else {
            msg.isDelivered = true;
        }

        int var8 = cursor.getInt(cursor.getColumnIndex("islistened"));
        msg.setListened(var8 == 1);
        msg.setUnread(false);
        String var9 = cursor.getString(cursor.getColumnIndex("groupname"));
        if(var9 == null) {
            msg.setChatType(ProtocolMessage.CHAT_TYPE.CHAT_SINGLE);
        } else {
            int var10 = cursor.getInt(cursor.getColumnIndex("msgtype"));
            msg.setChatType(ProtocolMessage.CHAT_TYPE.CHAT_GROUP);
            if(var10 == Message.ChatType.ChatRoom.ordinal()) {
                msg.setChatType(ProtocolMessage.CHAT_TYPE.CHAT_ROOM);
            }

            msg.setTo(var9);
        }

        return msg;
    }

    /**
     * 删除单聊消息
     * @param participiant
     */
    public void delParticipaintMessages(String participiant) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            int var3 = var2.delete("chat", "participant = ? and groupname is null", new String[]{participiant});
            EMLog.d(TAG, "delete chat msgs with:" + participiant + " return:" + var3);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public void a(String var1, Conversation.ConversationType var2) {
        boolean var3 = var2 != Conversation.ConversationType.Chat;
        int var4 = var2.ordinal();

        try {
            SQLiteDatabase var5 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            EMLog.d(TAG, "add converstion with:" + var1);
            String var6 = !var3?"username":"groupname";
            var5.execSQL("insert into conversation_list (" + var6 + "," + "conversation_type" + ")" + " select ?,? where not exists (select null from " + "conversation_list" + " where " + var6 + " = ?)", new Object[]{var1, Integer.valueOf(var4), var1});
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    /**
     * 从“我的会话列表”中删除
     * @param name 聊天对象名称，个人或者群组名
     * @param isGroup 是否群聊
     */
    void delFormConversationList(String name, boolean isGroup) {
        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            String var4 = !isGroup ?"participant":"groupname";
            String var5 = !isGroup ?"username":"groupname";
            var3.execSQL("delete from conversation_list where " + var5 + " = ? and " + " not exists (select null from " + "chat" + " where " + var4 + " = ?)", new String[]{name, name});
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    /**
     * 删除会话
     * @param name 单聊或群聊ID
     * @param isGroup true 群聊，false 单聊
     */
    public void delConversation(String name, boolean isGroup) {
        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            String var4 = !isGroup ? "username":"groupname";
            var3.execSQL("delete from conversation_list where " + var4 + " = ?", new String[]{name});
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    /**
     * 检查会话是否存在
     * @param name 单聊或群聊ID
     * @param isGroup true 群聊，false 单聊
     * @return
     */
    public boolean isConversationExist(String name, boolean isGroup) {
        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var4 = null;
            if(!isGroup) {
                var4 = var3.rawQuery("select count(*) from conversation_list where username = ? and groupname is null", new String[]{name});
            } else {
                var4 = var3.rawQuery("select count(*) from conversation_list where username is null and groupname = ?", new String[]{name, ""});
            }

            if(!var4.moveToFirst()) {
                var4.close();
                return false;
            } else {
                int var5 = var4.getInt(0);
                EMLog.d(TAG, "has converstion:" + name + " isGroup:" + isGroup + " count:" + var5);
                var4.close();
                return var5 > 0;
            }
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    /**
     * 修改消息应答状态
     * @param msgId
     * @param isacked true 已达，false 未达
     */
    public void updateMsgAck(String msgId, boolean isacked) {
        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var4 = new ContentValues();
            var4.put("isacked", Boolean.valueOf(isacked));
            var3.update("chat", var4, "msgid = ?", new String[]{msgId});
            EMLog.d(TAG, "update msg:" + msgId + " ack:" + isacked);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    /**
     * 更新消息监听状态
     * @param msgId
     * @param islistened
     */
    public void updateMsgListen(String msgId, boolean islistened) {
        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var4 = new ContentValues();
            var4.put("islistened", Boolean.valueOf(islistened));
            var3.update("chat", var4, "msgid = ?", new String[]{msgId});
            EMLog.d(TAG, "update msg:" + msgId + " isListened:" + islistened);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public void updateMsgDeliver(String msgId, boolean isdelivered) {
        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var4 = new ContentValues();
            var4.put("isdelivered", Boolean.valueOf(isdelivered));
            var3.update("chat", var4, "msgid = ?", new String[]{msgId});
            EMLog.d(TAG, "update msg:" + msgId + " delivered:" + isdelivered);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public void a(String msgId, String var2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("msgid", var2);
        getInstance().updateMessage(msgId, contentValues);
    }

    public void updateMessage(String msgId, ContentValues contentValues) {
        SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
        var3.update("chat", contentValues, "msgid = ?", new String[]{msgId});
    }

    private String list2String(List<String> var1) {
        StringBuffer var2 = new StringBuffer();
        Iterator var4 = var1.iterator();

        while(var4.hasNext()) {
            String var3 = (String)var4.next();
            var2.append(var3);
            var2.append(",");
        }

        return var2.toString();
    }

    public synchronized void saveGroup(Group group) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var3 = new ContentValues();
            var3.put("name", group.getGroupId());
            var3.put("jid", group.getEid());
            var3.put("nick", group.getGroupName());
            var3.put("desc", group.getDescription());
            var3.put("owner", group.getOwner());
            List var4 = group.getMembers();
            var3.put("members", this.list2String(var4));
            var3.put("members_size", Integer.valueOf(group.getAffiliationsCount()));
            var3.put("modifiedtime", Long.valueOf(group.getLastModifiedTime()));
            var3.put("ispublic", Boolean.valueOf(group.isPublic()));
            var3.put("isblocked", Boolean.valueOf(group.isMsgBlocked()));
            var3.put("max_users", Integer.valueOf(group.getMaxUsers()));
            var2.replace("Group", (String)null, var3);
            EMLog.d(TAG, "save group to db groupname:" + group.getGroupName());
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public synchronized void a(List<Group> var1) {
        SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
        var2.beginTransaction();
        Iterator var4 = var1.iterator();

        while(var4.hasNext()) {
            Group group = (Group)var4.next();
            if(this.am) {
                var2.setTransactionSuccessful();
                var2.endTransaction();
                return;
            }

            this.saveGroup(group);
        }

        var2.setTransactionSuccessful();
        var2.endTransaction();
    }

    public synchronized void b(List<String> var1) {
        SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
        var2.beginTransaction();
        Iterator var4 = var1.iterator();

        while(var4.hasNext()) {
            String var3 = (String)var4.next();
            if(this.am) {
                var2.setTransactionSuccessful();
                var2.endTransaction();
                return;
            }

            this.delGroup(var3);
        }

        var2.setTransactionSuccessful();
        var2.endTransaction();
    }

    public void saveChatRoom(ChatRoom var1) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var3 = new ContentValues();
            var3.put("name", var1.getId());
            var3.put("nick", var1.getName());
            var3.put("desc", var1.getDescription());
            var3.put("owner", var1.getOwner());
            List var4 = var1.getMembers();
            var3.put("members", this.list2String(var4));
            var3.put("members_size", Integer.valueOf(var1.getAffiliationsCount()));
            var3.put("max_users", Integer.valueOf(var1.getMaxUsers()));
            var2.insert("chatroom", (String)null, var3);
            EMLog.d(TAG, "save chatroom to db room name:" + var1.getName());
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    /**
     * 获取所有组
     * @return
     */
    public Map<String, Group> getGroups() {
        Hashtable var1 = new Hashtable();

        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var3 = var2.rawQuery("select * from emgroup", new String[0]);
            if(!var3.moveToFirst()) {
                var3.close();
                return var1;
            }

            do {
                Group var4 = this.cursor2Group(var3);
                var1.put(var4.getGroupId(), var4);
            } while(var3.moveToNext());

            var3.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        EMLog.d(TAG, "load groups from db:" + var1.size());
        return var1;
    }

    /**
     * 获取所有聊天室
     * @return
     */
    public Map<String, ChatRoom> getChatRooms() {
        Hashtable var1 = new Hashtable();

        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var3 = var2.rawQuery("select * from chatroom", new String[0]);
            if(!var3.moveToFirst()) {
                var3.close();
                return var1;
            }

            do {
                ChatRoom var4 = this.cursor2ChatRoom(var3);
                var1.put(var4.getId(), var4);
            } while(var3.moveToNext());

            var3.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        EMLog.d(TAG, "load chatrooms from db:" + var1.size());
        return var1;
    }

    /**
     * 获取组信息
     * @param groupId 组名称,id
     * @return
     */
    public Group getGroup(String groupId) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var3 = var2.rawQuery("select * from emgroup where name  =?", new String[]{groupId});
            Group var4 = null;
            if(var3 != null) {
                if(var3.moveToFirst()) {
                    var4 = this.cursor2Group(var3);
                }

                var3.close();
            }

            EMLog.d(TAG, "db load group:" + var4);
            return var4;
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public ChatRoom getChatRoom(String var1) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var3 = var2.rawQuery("select * from chatroom where name  =?", new String[]{var1});
            ChatRoom var4 = null;
            if(var3 != null) {
                if(var3.moveToFirst()) {
                    var4 = this.cursor2ChatRoom(var3);
                }

                var3.close();
            }

            EMLog.d(TAG, "db load chatroom:" + var4);
            return var4;
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

    private Group cursor2Group(Cursor var1) throws Exception {
        String var2 = var1.getString(var1.getColumnIndex("name"));
        Group var3 = new Group(var2);
        var3.setEid(var1.getString(var1.getColumnIndex("jid")));
        var3.setGroupName(var1.getString(var1.getColumnIndex("nick")));
        String var4 = var1.getString(var1.getColumnIndex("owner"));
        var3.setOwner(var4);
        var3.setLastModifiedTime(var1.getLong(var1.getColumnIndex("modifiedtime")));
        var3.setIsPublic(var1.getInt(var1.getColumnIndex("ispublic")) != 0);
        var3.setDescription(var1.getString(var1.getColumnIndex("desc")));
        var3.setAffiliationsCount(var1.getInt(var1.getColumnIndex("members_size")));
        var3.setMsgBlocked(var1.getInt(var1.getColumnIndex("isblocked")) != 0);
        var3.setMaxUsers(var1.getInt(var1.getColumnIndex("max_users")));
        String var5 = var1.getString(var1.getColumnIndex("members"));
        StringTokenizer var6 = new StringTokenizer(var5, ",");

        while(var6.hasMoreTokens()) {
            String var7 = var6.nextToken();
            var3.addMember(var7);
        }

        return var3;
    }

    private ChatRoom cursor2ChatRoom(Cursor var1) throws Exception {
        String var2 = var1.getString(var1.getColumnIndex("name"));
        ChatRoom var3 = new ChatRoom(var2);
        var3.setName(var1.getString(var1.getColumnIndex("nick")));
        String var4 = var1.getString(var1.getColumnIndex("owner"));
        var3.setOwner(var4);
        var3.setDescription(var1.getString(var1.getColumnIndex("desc")));
        var3.setAffiliationsCount(var1.getInt(var1.getColumnIndex("members_size")));
        var3.setMaxUsers(var1.getInt(var1.getColumnIndex("max_users")));
        String var5 = var1.getString(var1.getColumnIndex("members"));
        StringTokenizer var6 = new StringTokenizer(var5, ",");

        while(var6.hasMoreTokens()) {
            String var7 = var6.nextToken();
            var3.addMember(var7);
        }

        return var3;
    }

    public void updateGroup(Group var1) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var3 = new ContentValues();
            var3.put("jid", var1.getEid());
            var3.put("nick", var1.getGroupName());
            var3.put("desc", var1.getDescription());
            var3.put("owner", var1.getOwner());
            List var4 = var1.getMembers();
            var3.put("members", this.list2String(var4));
            var3.put("members_size", Integer.valueOf(var1.getAffiliationsCount()));
            var3.put("modifiedtime", Long.valueOf(var1.getLastModifiedTime()));
            var3.put("ispublic", Boolean.valueOf(var1.isPublic()));
            var3.put("isblocked", Boolean.valueOf(var1.isMsgBlocked()));
            var3.put("max_users", Integer.valueOf(var1.getMaxUsers()));
            var2.update("Group", var3, "name = ?", new String[]{var1.getGroupId()});
            EMLog.d(TAG, "updated group groupname:" + var1.getGroupName());
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public void updateChatRoom(ChatRoom var1) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var3 = new ContentValues();
            var3.put("nick", var1.getName());
            var3.put("desc", var1.getDescription());
            var3.put("owner", var1.getOwner());
            List var4 = var1.getMembers();
            var3.put("members", this.list2String(var4));
            var3.put("members_size", Integer.valueOf(var1.getAffiliationsCount()));
            var3.put("max_users", Integer.valueOf(var1.getMaxUsers()));
            var2.update("chatroom", var3, "name = ?", new String[]{var1.getId()});
            EMLog.d(TAG, "updated chatroom name:" + var1.getName());
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    /**
     * 删除组
     * @param gruopId 组id
     */
    public synchronized void delGroup(String gruopId) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            int var3 = var2.delete("Group", "name = ?", new String[]{gruopId});
            EMLog.d(TAG, "delete group with:" + gruopId + " return:" + var3);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    /**
     * 删除聊天室消息
     * @param chatroom
     */
    public void delChatRoomMessages(String chatroom) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            int var3 = var2.delete("chatroom", "name = ?", new String[]{chatroom});
            EMLog.d(TAG, "delete chatroom with:" + chatroom + " return:" + var3);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    /**
     * 删除群组消息
     * @param groupname
     */
    public void delGroupMessages(String groupname) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            int var3 = var2.delete("chat", "groupname = ?", new String[]{groupname});
            EMLog.d(TAG, "delete group messages with:" + groupname + " return:" + var3);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public boolean c(Message var1) {
        var1.status = ProtocolMessage.STATUS.SUCCESS;
        var1.isAcked = true;
        var1.isDelivered = true;
        if(var1.getMsgId() == null) {
            var1.setMsgId(Long.toString(System.currentTimeMillis()));
        }

        if(var1.getFrom() == null) {
            EMLog.e(TAG, "import msg error: msg from is null");
            return false;
        } else if(var1.getTo() == null) {
            EMLog.e(TAG, "import msg error: msg to is null");
            return false;
        } else {
            return this.saveMessage(var1);
        }
    }

    public synchronized void c(List<Message> var1) {
        HashMap var2 = new HashMap();
        Iterator var4 = var1.iterator();

        while(var4.hasNext()) {
            Message var3 = (Message)var4.next();
            var2.put(var3.getMsgId(), var3);
        }

        boolean var14 = true;
        SQLiteDatabase var15 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();

        while(true) {
            Cursor var9;
            boolean var10;
            do {
                if(var1.size() <= 0) {
                    var1.clear();
                    Iterator var19 = var2.values().iterator();

                    while(var19.hasNext()) {
                        Message var17 = (Message)var19.next();
                        var1.add(var17);
                    }

                    boolean var16 = true;

                    while(var1.size() > 0) {
                        int var18 = 0;
                        var15.beginTransaction();

                        try {
                            while(var1.size() > 0 && var18 < 300) {
                                Message var20 = (Message)var1.remove(0);
                                if(var20 != null) {
                                    this.saveMessage(var20);
                                    ++var18;
                                }
                            }

                            var15.setTransactionSuccessful();
                        } finally {
                            var15.endTransaction();
                        }
                    }

                    return;
                }

                int var5 = 0;
                StringBuilder var6 = new StringBuilder();
                boolean var7 = true;
                var6.append("(");

                for(; var1.size() > 0 && var5 < 1000; ++var5) {
                    Message var8 = (Message)var1.remove(0);
                    if(var7) {
                        var6.append("\'" + var8.getMsgId() + "\'");
                        var7 = false;
                    } else {
                        var6.append(", \'" + var8.getMsgId() + "\'");
                    }
                }

                var6.append(")");
                String var21 = "select msgid from chat where msgid in " + var6.toString();
                var9 = var15.rawQuery(var21, new String[0]);
                var10 = false;
                if(!var9.moveToFirst()) {
                    var9.close();
                    var10 = true;
                }
            } while(var10);

            do {
                String var11 = var9.getString(0);
                var2.remove(var11);
            } while(var9.moveToNext());

            var9.close();
        }
    }

    public int getUnreadCount(String username) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getReadableDatabase();
            int var3 = 0;
            Cursor var4 = var2.rawQuery("select count from unreadcount where username = ?", new String[]{username});
            if(var4.moveToFirst()) {
                var3 = var4.getInt(var4.getColumnIndex("count"));
            }

            var4.close();
            return var3 < 0?0:var3;
        } catch (Exception var5) {
            return 0;
        }
    }

    public void setUnreadCount(String username, int count) {
        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var4 = new ContentValues();
            var4.put("username", username);
            var4.put("count", Integer.valueOf(count));
            var3.replace("unreadcount", (String)null, var4);
        } catch (Exception var5) {
            ;
        }

    }

    public void l(String var1) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            var2.delete("unreadcount", "username = ?", new String[]{var1});
        } catch (Exception var3) {
            ;
        }

    }

    public List<String> h() {
        ArrayList var1 = new ArrayList();

        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getReadableDatabase();
            Cursor var3 = var2.rawQuery("select * from unreadcount", new String[0]);
            if(!var3.moveToFirst()) {
                var3.close();
                return var1;
            }

            do {
                String var4 = var3.getString(0);
                int var5 = var3.getInt(1);
                if(var5 > 0) {
                    var1.add(var4);
                }
            } while(var3.moveToNext());

            var3.close();
        } catch (Exception var6) {
            ;
        }

        return var1;
    }

    public void setToken(String username, p.token token) {
        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var4 = new ContentValues();
            var4.put("username", username);
            var4.put("value", token.getValue());
            var4.put("saved_time", Long.valueOf(token.getSavedTime()));
            var3.replace("token", (String)null, var4);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public p.token getToken(String username) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getReadableDatabase();
            p.token var3 = null;
            Cursor var4 = var2.rawQuery("select * from token where username = ?", new String[]{username});
            if(var4 != null) {
                var3 = new p.token();
                if(var4.moveToFirst()) {
                    String var5 = var4.getString(var4.getColumnIndex("value"));
                    long var6 = var4.getLong(var4.getColumnIndex("saved_time"));
                    if(var5 != null) {
                        var3.setValue(var5);
                    }

                    var3.setSavedTime(var6);
                    var4.close();
                } else {
                    var4.close();
                }
            }

            return var3;
        } catch (Exception var8) {
            return null;
        }
    }

    /**
     * 添加联系人
     * @param jid
     * @param username
     */
    public void addContact(String jid, String username) {
        EMLog.d(TAG, "add contact to db:" + username);

        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            ContentValues var4 = new ContentValues();
            var4.put("jid", jid);
            var4.put("username", username);
            var3.replace("contact", (String)null, var4);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    /**
     * 删除联系人
     * @param jid
     */
    public void delContact(String jid) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            var2.delete("contact", "jid = ?", new String[]{jid});
            EMLog.d(TAG, "delete contact jid:" + jid);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public List<Contact> i() {
        LinkedList var1 = new LinkedList();

        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor var3 = var2.rawQuery("select * from contact", new String[0]);
            if(!var3.moveToFirst()) {
                var3.close();
                return var1;
            }

            do {
                String var4 = var3.getString(0);
                String var5 = var3.getString(1);
                Contact var6 = new Contact(var4, var5);
                var1.add(var6);
            } while(var3.moveToNext());

            var3.close();
            EMLog.d(TAG, "loaded contacts from db:" + var1.size());
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return var1;
    }

    public List<String> j() {
        SQLiteDatabase var1 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getReadableDatabase();
        Cursor var2 = var1.rawQuery("select * from black_list", new String[0]);
        ArrayList var3 = new ArrayList();
        if(!var2.moveToFirst()) {
            var2.close();
            return var3;
        } else {
            do {
                String var4 = var2.getString(var2.getColumnIndex("username"));
                var3.add(var4);
            } while(var2.moveToNext());

            var2.close();
            return var3;
        }
    }

    /**
     * 将多个用户id加入黑名单
     * @param usernames
     */
    public void addBlackList(List<String> usernames) {
        SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();

        try {
            if(var2.isOpen()) {
                var2.execSQL("delete from black_list");
                Iterator var4 = usernames.iterator();

                while(var4.hasNext()) {
                    String var3 = (String)var4.next();
                    ContentValues var5 = new ContentValues();
                    var5.put("username", var3);
                    var2.insert("black_list", (String)null, var5);
                }
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    /**
     * 从黑名单中移除
     * @param username
     */
    public void delBlackList(String username) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            if(var2.isOpen()) {
                var2.delete("black_list", "username = ?", new String[]{username});
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    /**
     * 加入黑名单
     * @param username
     */
    public void addBlackList(String username) {
        try {
            SQLiteDatabase var2 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            if(var2.isOpen()) {
                ContentValues var3 = new ContentValues();
                var3.put("username", username);
                var2.insert("black_list", (String)null, var3);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public void a(String var1, boolean var2, String ext) {
        SQLiteDatabase var4 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getReadableDatabase();

        try {
            ContentValues var5 = new ContentValues();
            var5.put("ext", ext == null?"":ext);
            String var6 = !var2?"username":"groupname";
            var4.update("conversation_list", var5, var6 + " = ?", new String[]{var1});
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    public String h(String var1, boolean var2) {
        SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getReadableDatabase();

        try {
            String var4 = !var2?"username":"groupname";
            Cursor var5 = var3.rawQuery("select ext from conversation_list where " + var4 + " = ?", new String[]{var1});
            if(!var5.moveToFirst()) {
                var5.close();
                return "";
            } else {
                String var6 = var5.getString(0);
                var5.close();
                return var6;
            }
        } catch (Exception var7) {
            var7.printStackTrace();
            return "";
        }
    }

    public boolean k() {
        return this.ap;
    }

    public List<Message> a(ProtocolMessage.TYPE var1, Message.ChatType var2, String var3, String var4, int var5) {
        ArrayList var6 = new ArrayList();

        try {
            SQLiteDatabase var7 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor cursor = null;
            Message var9;
            if(var4 != null) {
                var9 = this.getMessage(var4);
                if(var9 == null) {
                    return var6;
                }

                long var10 = var9.getMsgTime();
                if(var3 != null) {
                    if(var2 == Message.ChatType.Chat) {
                        cursor = var7.rawQuery("select * from chat where participant = ? and msgbody like \'%" + var1.toString().toLowerCase(Locale.getDefault()) + "%\' and groupname is null and " + "msgtime" + " < ? order by " + "msgtime" + " desc limit ?", new String[]{var3, String.valueOf(var10), String.valueOf(var5)});
                    } else if(var2 == Message.ChatType.GroupChat || var2 == Message.ChatType.ChatRoom) {
                        cursor = var7.rawQuery("select * from chat where groupname = ? and msgbody like \'%" + var1.toString().toLowerCase(Locale.getDefault()) + "%\' and " + "msgtime" + " < ? order by " + "msgtime" + " desc limit ?", new String[]{var3, String.valueOf(var10), String.valueOf(var5)});
                    }
                } else {
                    cursor = var7.rawQuery("select * from chat where msgbody like \'%" + var1.toString().toLowerCase(Locale.getDefault()) + "%\' and " + "msgtime" + " < ? order by " + "msgtime" + " desc limit ?", new String[]{String.valueOf(var10), String.valueOf(var5)});
                }
            } else if(var3 != null) {
                if(var2 == Message.ChatType.Chat) {
                    cursor = var7.rawQuery("select * from chat where participant = ? and msgbody like \'%" + var1.toString().toLowerCase(Locale.getDefault()) + "%\' and groupname is null order by " + "msgtime" + " desc limit ?", new String[]{var3, String.valueOf(var5)});
                } else if(var2 == Message.ChatType.GroupChat || var2 == Message.ChatType.ChatRoom) {
                    cursor = var7.rawQuery("select * from chat where groupname = ? and msgbody like \'%" + var1.toString().toLowerCase(Locale.getDefault()) + "%\' order by " + "msgtime" + " desc limit ?", new String[]{var3, String.valueOf(var5)});
                }
            } else {
                cursor = var7.rawQuery("select * from chat where msgbody like \'%" + var1.toString().toLowerCase(Locale.getDefault()) + "%\' order by " + "msgtime" + " desc limit ?", new String[]{String.valueOf(var5)});
            }

            if(!cursor.moveToNext()) {
                cursor.close();
                return var6;
            }

            do {
                var9 = this.cursor2Message(cursor);
                if(var9.getType() == ProtocolMessage.TYPE.TXT) {
                    var6.add(var9);
                } else if(var9.getType() == ProtocolMessage.TYPE.IMAGE) {
                    var6.add(var9);
                } else if(var9.getType() == ProtocolMessage.TYPE.VOICE) {
                    var6.add(var9);
                } else if(var9.getType() == ProtocolMessage.TYPE.VIDEO) {
                    var6.add(var9);
                } else if(var9.getType() == ProtocolMessage.TYPE.LOCATION) {
                    var6.add(var9);
                } else if(var9.getType() == ProtocolMessage.TYPE.FILE) {
                    var6.add(var9);
                }
            } while(cursor.moveToNext());

            cursor.close();
        } catch (Exception var12) {
            var12.printStackTrace();
        }

        return var6;
    }

    public long a(ProtocolMessage.TYPE var1, Message.ChatType var2, String var3) {
        ArrayList var4 = new ArrayList();
        SQLiteDatabase var5 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
        Cursor var6 = null;
        if(var3 != null) {
            if(var2 == Message.ChatType.Chat) {
                var6 = var5.rawQuery("select * from chat where participant = ? and msgbody like \'%" + var1.toString().toLowerCase(Locale.getDefault()) + "%\' and groupname is null limit 5000", new String[]{var3});
            } else if(var2 == Message.ChatType.GroupChat || var2 == Message.ChatType.ChatRoom) {
                var6 = var5.rawQuery("select * from chat where groupname = ? and msgbody like \'%" + var1.toString().toLowerCase(Locale.getDefault()) + "%\' limit 5000", new String[]{var3});
            }
        } else {
            var6 = var5.rawQuery("select * from chat where msgbody like \'%" + var1.toString().toLowerCase(Locale.getDefault()) + "%\' limit 5000", (String[])null);
        }

        if(!var6.moveToNext()) {
            var6.close();
            return (long)var4.size();
        } else {
            do {
                String var7 = var6.getString(var6.getColumnIndex("msgbody"));
                Message var8 = MessageEncoder.getMsgFromJson(var7);
                if(var8.getType() == ProtocolMessage.TYPE.TXT) {
                    var4.add(var8);
                } else if(var8.getType() == ProtocolMessage.TYPE.IMAGE) {
                    var4.add(var8);
                } else if(var8.getType() == ProtocolMessage.TYPE.VOICE) {
                    var4.add(var8);
                } else if(var8.getType() == ProtocolMessage.TYPE.VIDEO) {
                    var4.add(var8);
                } else if(var8.getType() == ProtocolMessage.TYPE.LOCATION) {
                    var4.add(var8);
                } else if(var8.getType() == ProtocolMessage.TYPE.FILE) {
                    var4.add(var8);
                }
            } while(var6.moveToNext());

            var6.close();
            return (long)var4.size();
        }
    }

    public List<Message> a(Message.ChatType var1, String var2, String var3, int var4, String var5) {
        if(var4 <= 0) {
            var4 = 20;
        }

        ArrayList var6 = new ArrayList();

        try {
            SQLiteDatabase var7 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor cursor = null;
            long var9 = 0L;
            if(var3 != null) {
                Message var11 = ChatManager.getInstance().getMessage(var3);
                if(var11 == null) {
                    var11 = this.getMessage(var3);
                    if(var11 == null) {
                        return var6;
                    }
                }

                var9 = var11.getMsgTime();
            }

            if(var1 == Message.ChatType.Chat) {
                if(var9 > 0L) {
                    cursor = var7.rawQuery("select * from chat where participant = ? and msgtime < ? and msgbody like \'%" + var2 + "%\' and groupname is null order by " + "msgtime" + " desc", new String[]{var5, String.valueOf(var9)});
                } else {
                    cursor = var7.rawQuery("select * from chat where participant = ? and msgbody like \'%" + var2 + "%\' and groupname is null order by " + "msgtime" + " desc", new String[]{var5});
                }
            } else if(var9 > 0L) {
                cursor = var7.rawQuery("select * from chat where groupname = ? and msgtime < ? and msgbody like \'%" + var2 + "%\' order by " + "msgtime" + " desc", new String[]{var5, String.valueOf(var9)});
            } else {
                cursor = var7.rawQuery("select * from chat where groupname = ? and msgbody like \'%" + var2 + "%\' order by " + "msgtime" + " desc", new String[]{var5});
            }

            if(!cursor.moveToFirst()) {
                cursor.close();
                return var6;
            }

            do {
                String var15 = cursor.getString(cursor.getColumnIndex("msgbody"));
                String var12 = var15.substring(var15.indexOf("msg"));
                if(var12.contains(var2)) {
                    Message var13 = this.cursor2Message(cursor);
                    var6.add(var13);
                    if(var6.size() == var4) {
                        break;
                    }
                }
            } while(cursor.moveToNext());

            cursor.close();
        } catch (Exception var14) {
            var14.printStackTrace();
        }

        return var6;
    }

    public Map<String, KeywordSearchInfo> q(String var1) {
        HashMap var2 = new HashMap();

        try {
            SQLiteDatabase var3 = DBManager.iSQLiteOpenHelper.getInstance(this.context, this.dbname).getWritableDatabase();
            Cursor cursor = null;
            cursor = var3.rawQuery("select * from chat where msgbody like \'%" + var1 + "%\'" + "order by msgtime desc", (String[])null);

            while(cursor.moveToNext()) {
                String var5 = cursor.getString(cursor.getColumnIndex("msgbody"));
                var5 = var5.substring(var5.indexOf("\"bodies\""));
                if(var5.contains(var1)) {
                    String var6 = cursor.getString(cursor.getColumnIndex("groupname"));
                    if(var6 == null) {
                        var6 = cursor.getString(cursor.getColumnIndex("participant"));
                    }

                    KeywordSearchInfo var7 = (KeywordSearchInfo)var2.get(var6);
                    if(var7 != null) {
                        var7.setCount(var7.getCount() + 1L);
                    } else {
                        var7 = new KeywordSearchInfo();
                        var7.setCount(1L);
                        var7.setUsername(var6);
                        var2.put(var6, var7);
                        var7.setMessage(this.cursor2Message(cursor));
                    }
                }
            }

            cursor.close();
        } catch (Exception var8) {
            ;
        }

        return var2.size() > 0?var2:null;
    }

    private static class iSQLiteOpenHelper extends SQLiteOpenHelper {
        private static DBManager.iSQLiteOpenHelper instance = null;
        private String username;

        private iSQLiteOpenHelper(Context context, String var2) {
            super(context, var2 + "_emmsg.db", (SQLiteDatabase.CursorFactory)null, 12);
            this.username = var2;
            EMLog.d(TAG, "created chatdb for :" + var2);
        }

        public static synchronized DBManager.iSQLiteOpenHelper getInstance(Context context, String var1) {
            if(instance == null) {
                instance = new DBManager.iSQLiteOpenHelper(context, var1);
            }

            return instance;
        }

        public static synchronized void closeDB() {
            if(instance != null) {
                try {
                    SQLiteDatabase var0 = instance.getWritableDatabase();
                    var0.close();
                } catch (Exception var1) {
                    var1.printStackTrace();
                }

                instance = null;
            }

        }

        public void onCreate(SQLiteDatabase var1) {
            var1.execSQL("create table chat (_id integer primary key autoincrement, msgid text, msgtime integer, msgdir integer, isacked integer, isdelivered integer, status integer,participant text not null, islistened integer, msgbody text not null,msgtype integer, groupname text);");
            var1.execSQL("create table emgroup (name text primary key, jid text not null, nick text not null, owner text not null, modifiedtime integer, ispublic integer, desc text, members_size integer, isblocked integer, members text, max_users integer);");
            var1.execSQL("create table unreadcount (username text primary key, count integer);");
            var1.execSQL("create table token (username text primary key, value text, saved_time integer);");
            var1.execSQL("create table contact (jid text primary key, username text, nick );");
            var1.execSQL("create table black_list (username text primary key);");
            var1.execSQL("create table if not exists conversation_list (username text primary key, groupname text, ext text, conversation_type integer);");
            var1.execSQL("create table chatroom (name text primary key, nick text, owner text, desc text, members_size integer, isblocked integer, members text, max_users integer);");
        }

        public void onUpgrade(SQLiteDatabase var1, int var2, int var3) {
            Log.w(TAG, "Upgrading from version " + var2 + " to " + var3);
            if(var2 < 2) {
                var1.execSQL("create table unreadcount (username text primary key, count integer);");
            }

            if(var2 < 3) {
                var1.execSQL("create table token (username text primary key, value text, saved_time integer);");
                var1.execSQL("create table contact (jid text primary key, username text, nick );");
            }

            if(var2 < 4) {
                try {
                    var1.delete("token", "username = ?", new String[]{this.username});
                } catch (Exception var6) {
                    var6.printStackTrace();
                }
            }

            if(var2 < 5) {
                try {
                    var1.execSQL("ALTER TABLE chat ADD COLUMN isdelivered integer ;");
                    EMLog.d(TAG, "db upgrade to vervison 5");
                } catch (Exception var5) {
                    var5.printStackTrace();
                }
            }

            if(var2 < 6) {
                var1.execSQL("create table black_list (username text primary key);");
                var1.execSQL("ALTER TABLE chat ADD COLUMN islistened integer ;");
            }

            if(var2 < 7) {
                var1.execSQL("ALTER TABLE Group ADD COLUMN members_size INTEGER ;");
            }

            if(var2 < 8) {
                var1.execSQL("ALTER TABLE Group ADD COLUMN isblocked INTEGER ;");
            }

            if(var2 < 9) {
                var1.execSQL("ALTER TABLE Group ADD COLUMN max_users INTEGER ;");
            }

            if(var2 < 10) {
                var1.execSQL("create table if not exists conversation_list (username text primary key, groupname text, ext text, conversation_type integer);");
                this.initializeConversation(var1);
                this.clearContactTable_v10(var1);
                DBManager.getInstance().ap = true;
            }

            if(var2 < 12) {
                var1.execSQL("create table chatroom (name text primary key, nick text, owner text, desc text, members_size integer, isblocked integer, members text, max_users integer);");
                if(!this.isColumnExist(var1, "conversation_list", "conversation_type")) {
                    var1.execSQL("ALTER TABLE conversation_list ADD COLUMN conversation_type INTEGER ;");
                }

                var1.execSQL("ALTER TABLE chat ADD COLUMN msgtype INTEGER ;");
                this.migrateFrom10To12(var1);
            }

        }

        private boolean isColumnExist(SQLiteDatabase var1, String var2, String var3) {
            boolean var4 = false;
            Cursor var5 = null;

            try {
                var5 = var1.rawQuery("SELECT * FROM " + var2 + " LIMIT 0", (String[])null);
                var4 = var5 != null && var5.getColumnIndex(var3) != -1;
            } catch (Exception var10) {
                EMLog.e(TAG, "checkColumnExists..." + var10.getMessage());
            } finally {
                if(var5 != null && !var5.isClosed()) {
                    var5.close();
                }

            }

            return var4;
        }

        private void migrateFrom10To12(SQLiteDatabase var1) {
            Cursor var2 = var1.rawQuery("select * from conversation_list", (String[])null);
            if(!var2.moveToFirst()) {
                var2.close();
            } else {
                do {
                    boolean var3 = !var2.isNull(var2.getColumnIndex("groupname"));
                    int var4 = Conversation.ConversationType.Chat.ordinal();
                    String var5 = "username";
                    if(var3) {
                        var4 = Conversation.ConversationType.GroupChat.ordinal();
                        var5 = "groupname";
                    }

                    ContentValues var6 = new ContentValues();
                    var6.put("conversation_type", Integer.valueOf(var4));
                    var1.update("conversation_list", var6, var5 + " = ?", new String[]{var2.getString(var2.getColumnIndex(var5))});
                } while(var2.moveToNext());

                var2.close();
            }
        }

        private void clearContactTable_v10(SQLiteDatabase var1) {
            try {
                EMLog.d(TAG, "add converstion with:" + this.username);
                var1.execSQL("delete from contact", new String[0]);
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        }

        private void initializeConversation(SQLiteDatabase var1) {
            EMLog.d(TAG, "initializeConversation");
            ArrayList var2 = new ArrayList();
            ArrayList var3 = new ArrayList();

            try {
                Cursor var4 = var1.rawQuery("select distinct participant from chat where groupname is null", (String[])null);
                if(var4.moveToFirst()) {
                    do {
                        var2.add(var4.getString(0));
                    } while(var4.moveToNext());
                }

                var4.close();
                var4 = var1.rawQuery("select distinct groupname from chat where groupname is not null", (String[])null);
                if(var4.moveToFirst()) {
                    do {
                        var3.add(var4.getString(0));
                    } while(var4.moveToNext());
                }

                var4.close();
                EMLog.d(TAG, "load participants size:" + var2.size());
            } catch (Exception var6) {
                var6.printStackTrace();
            }

            Iterator var5 = var2.iterator();

            String var7;
            while(var5.hasNext()) {
                var7 = (String)var5.next();
                this.addConversation(var1, var7, false);
            }

            var5 = var3.iterator();

            while(var5.hasNext()) {
                var7 = (String)var5.next();
                this.addConversation(var1, var7, true);
            }

        }

        private void addConversation(SQLiteDatabase var1, String var2, boolean var3) {
            try {
                EMLog.d(TAG, "add converstion with:" + var2);
                String var4 = !var3?"username":"groupname";
                var1.execSQL("insert into conversation_list (" + var4 + ")" + " select ? where not exists (select null from " + "conversation_list" + " where " + var4 + " = ?)", new String[]{var2, var2});
            } catch (Exception var5) {
                var5.printStackTrace();
            }

        }
    }
}

