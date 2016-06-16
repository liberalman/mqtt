package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.text.TextUtils;

import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.util.EMLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Conversation {
    private static final String TAG = "conversation";
    List<Message> msgList; // 消息列表
    private int unreadMsgCount = 0; // 未读消息数量
    private String userId; // 用户id
    private boolean isGroup = false; // 是否群聊
    private Contact opposite = null;
    private Conversation.ConversationType type;
    long msgCount; // 消息总数
    private boolean isKeywordSearchEnabled;

    public Conversation(String var1) {
        this.type = Conversation.ConversationType.Chat;
        this.msgCount = 0L;
        this.isKeywordSearchEnabled = false;
        this.isGroup = GroupManager.getInstance().getGroup(var1) != null;
        this.userId = var1;
        if(this.msgList == null) {
            this.msgList = Collections.synchronizedList(new ArrayList());
        }

        if(this.unreadMsgCount <= 0) {
            this.unreadMsgCount = DBManager.getInstance().getUnreadCount(var1);
        }

    }

    public Conversation(String userId, boolean var2) {
        this.type = Conversation.ConversationType.Chat;
        this.msgCount = 0L;
        this.isKeywordSearchEnabled = false;
        this.userId = userId;
        this.isGroup = var2;
        if(this.msgList == null) {
            this.msgList = Collections.synchronizedList(new ArrayList());
        }

        if(this.unreadMsgCount <= 0) {
            this.unreadMsgCount = DBManager.getInstance().getUnreadCount(userId);
        }

    }

    public Conversation(String var1, List<Message> var2, Conversation.ConversationType var3, Long var4) {
        this.type = Conversation.ConversationType.Chat;
        this.msgCount = 0L;
        this.isKeywordSearchEnabled = false;
        this.userId = var1;
        this.type = var3;
        this.isGroup = var3 != Conversation.ConversationType.Chat;
        if(this.msgList == null) {
            this.msgList = Collections.synchronizedList(var2);
        }

        if(this.unreadMsgCount <= 0) {
            this.unreadMsgCount = DBManager.getInstance().getUnreadCount(var1);
        }

        this.msgCount = var4.longValue();
    }

    public void addMessage(Message msg) {
        this.addMessage(msg, true);
    }

    /**
     * 将消息添加到消息列表中,并保存本地数据库
     * @param msg
     * @param var2
     */
    void addMessage(Message msg, boolean var2) {
        if(msg.getChatType() == ProtocolMessage.CHAT_TYPE.CHAT_GROUP) {
            this.isGroup = true;
        }

        if(this.msgList.size() > 0) {
            Message var3 = (Message)this.msgList.get(this.msgList.size() - 1);
            if(msg.getMsgId() != null && var3.getMsgId() != null && msg.getMsgId().equals(var3.getMsgId())) { // 该消息与消息列表中最后一条消息相同，则退出。
                return;
            }
        }

        boolean isRepeat = false; // 消息是否重复
        Iterator iterator = this.msgList.iterator();

        while(iterator.hasNext()) {
            Message var4 = (Message)iterator.next();
            if(var4.getMsgId().equals(msg.getMsgId())) { // 该消息是重复消息，退出。
                isRepeat = true;
                break;
            }
        }

        if(!isRepeat) { // 消息不重复
            this.msgList.add(msg); // 加入消息列表
            ++this.msgCount; // 消息总数+1
            if(msg.direct == ProtocolMessage.DIRECT.RECEIVE && msg.unread && var2) {
                ++this.unreadMsgCount; // 未读消息数量+1
                this.saveUnreadMsgCount(this.unreadMsgCount); // 更新数据库中未读消息数量
            }
        }

    }

    void saveUnreadMsgCount(final int var1) {
        ChatManager.getInstance().msgCountThreadPool.submit(new Runnable() {
            public void run() {
                DBManager.getInstance().setUnreadCount(Conversation.this.userId, var1);
            }
        });
    }

    void deleteUnreadMsgCountRecord() {
        ChatManager.getInstance().msgCountThreadPool.submit(new Runnable() {
            public void run() {
                DBManager.getInstance().l(Conversation.this.userId);
            }
        });
    }

    public int getUnreadMsgCount() {
        if(this.unreadMsgCount < 0) {
            this.unreadMsgCount = 0;
        }

        return this.unreadMsgCount;
    }

    public void resetUnsetMsgCount() {
        this.unreadMsgCount = 0;
        this.saveUnreadMsgCount(0);
    }

    public void resetUnreadMsgCount() {
        this.unreadMsgCount = 0;
        this.saveUnreadMsgCount(0);
    }

    public void markAllMessagesAsRead() {
        this.resetUnreadMsgCount();
    }

    public int getMsgCount() {
        return this.msgList.size();
    }

    public int getAllMsgCount() {
        return (int)this.msgCount;
    }

    public Message getMessage(int var1) {
        return this.getMessage(var1, true);
    }

    public Message getMessage(int var1, boolean var2) {
        if(var1 >= this.msgList.size()) {
            EMLog.e("conversation", "outofbound, messages.size:" + this.msgList.size());
            return null;
        } else {
            Message var3 = (Message)this.msgList.get(var1);
            if(var2 && var3 != null && var3.unread) {
                var3.unread = false;
                if(this.unreadMsgCount > 0) {
                    --this.unreadMsgCount;
                    this.saveUnreadMsgCount(this.unreadMsgCount);
                }
            }

            return var3;
        }
    }

    /**
     * 从数据库中载入最近几条对话消息
     * @param startMsgId 起始消息id
     * @param msgCount 消息数量
     * @return 消息列表
     */
    public List<Message> loadMoreMsgFromDB(String startMsgId, int msgCount) {
        new ArrayList();
        List var3 = DBManager.getInstance().getMessages(this.userId, startMsgId, msgCount);
        this.msgList.addAll(0, var3);
        Iterator var5 = var3.iterator();

        while(var5.hasNext()) {
            Message var4 = (Message)var5.next();
            ChatManager.getInstance().addMessage(var4, false);
        }

        return var3;
    }

    public List<Message> loadMoreMessages(boolean var1, String startMsgId, int count) {
        ArrayList var4 = new ArrayList();
        if(startMsgId == null) {
            return var4;
        } else {
            List var7;
            if(this.isGroup) {
                var7 = DBManager.getInstance().getMsgList(this.userId, startMsgId, count, var1);
            } else {
                var7 = DBManager.getInstance().getMsgList(this.userId, var1, startMsgId, count);
            }

            if(!var1) {
                this.msgList.addAll(var7);
            } else {
                this.msgList.addAll(0, var7);
            }

            if(!this.isKeywordSearchEnabled) {
                Iterator var6 = var7.iterator();

                while(var6.hasNext()) {
                    Message var5 = (Message)var6.next();
                    ChatManager.getInstance().addMessage(var5, false);
                }
            }

            return var7;
        }
    }

    public List<Message> loadMoreGroupMsgFromDB(String var1, int var2) {
        List var3 = DBManager.getInstance().getMsgList(this.userId, var1, var2);
        this.msgList.addAll(0, var3);
        Iterator var5 = var3.iterator();

        while(var5.hasNext()) {
            Message var4 = (Message)var5.next();
            ChatManager.getInstance().addMessage(var4, false);
        }

        return var3;
    }

    public Message getMessage(String var1) {
        return this.getMessage(var1, true);
    }

    public Message getMessage(String var1, boolean var2) {
        for(int var3 = this.msgList.size() - 1; var3 >= 0; --var3) {
            Message var4 = (Message)this.msgList.get(var3);
            if(var4.msgId.equals(var1)) {
                if(var2 && var4.unread) {
                    var4.unread = false;
                    if(this.unreadMsgCount > 0) {
                        --this.unreadMsgCount;
                        this.saveUnreadMsgCount(this.unreadMsgCount);
                    }
                }

                return var4;
            }
        }

        return null;
    }

    public Message loadMessage(String var1) {
        if(TextUtils.isEmpty(var1)) {
            return null;
        } else {
            Message var2 = this.getMessage(var1, false);
            if(var2 == null) {
                var2 = DBManager.getInstance().getMessage(var1);
            }

            return var2;
        }
    }

    public List<Message> loadMessages(List<String> var1) {
        if(var1 == null) {
            return null;
        } else {
            ArrayList var2 = new ArrayList();
            Iterator var4 = var1.iterator();

            while(var4.hasNext()) {
                String var3 = (String)var4.next();
                Message var5 = this.loadMessage(var3);
                if(var5 != null) {
                    var2.add(var5);
                }
            }

            return var2.size() > 0?var2:null;
        }
    }

    public void markMessageAsRead(String var1) {
        this.getMessage(var1);
    }

    public List<Message> getAllMessages() {
        return this.msgList;
    }

    public int getMessagePosition(Message var1) {
        try {
            Iterator var3 = this.msgList.iterator();

            while(var3.hasNext()) {
                Message var2 = (Message)var3.next();
                if(var1.getMsgId().equals(var2.getMsgId())) {
                    return this.msgList.indexOf(var2);
                }
            }
        } catch (Exception var4) {
            ;
        }

        return -1;
    }

    public String getUserName() {
        return this.userId;
    }

    public void removeMessage(String var1) {
        EMLog.d("conversation", "remove msg from conversation:" + var1);

        for(int var2 = this.msgList.size() - 1; var2 >= 0; --var2) {
            Message var3 = (Message)this.msgList.get(var2);
            if(var3.msgId.equals(var1)) {
                if(var3.unread) {
                    var3.unread = false;
                    if(this.unreadMsgCount > 0) {
                        --this.unreadMsgCount;
                        this.saveUnreadMsgCount(this.unreadMsgCount);
                    }
                }

                this.msgList.remove(var2);
                if(this.msgCount > 0L) {
                    --this.msgCount;
                }

                DBManager.getInstance().delMessage(var1);
                ConversationManager.getInstance().removeMessage(var1);
                break;
            }
        }

    }

    public boolean getIsGroup() {
        return this.isGroup;
    }

    public boolean isGroup() {
        return this.isGroup;
    }

    public void setGroup(boolean var1) {
        this.isGroup = var1;
    }

    public Message getLastMessage() {
        return this.msgList.size() == 0?null:(Message)this.msgList.get(this.msgList.size() - 1);
    }

    public void clear() {
        this.msgList.clear();
        this.unreadMsgCount = 0;
        DBManager.getInstance().l(this.userId);
    }

    public void setExtField(String var1) {
        DBManager.getInstance().a(this.userId, this.isGroup, var1);
    }

    public String getExtField() {
        return DBManager.getInstance().h(this.userId, this.isGroup);
    }

    public Conversation.ConversationType getType() {
        return this.type;
    }

    public void markAsKeywordSearch() {
        this.isKeywordSearchEnabled = true;
    }

    void setType(Conversation.ConversationType var1) {
        this.type = var1;
    }

    public static Conversation.ConversationType msgType2ConversationType(String var0, ProtocolMessage.CHAT_TYPE var1) {
        return var1 == ProtocolMessage.CHAT_TYPE.CHAT_SINGLE ?(CustomerService.getInstance().isCustomServiceAgent(var0)?Conversation.ConversationType.HelpDesk:Conversation.ConversationType.Chat):(var1 == ProtocolMessage.CHAT_TYPE.CHAT_GROUP ? Conversation.ConversationType.GroupChat:(var1 == ProtocolMessage.CHAT_TYPE.CHAT_ROOM ? Conversation.ConversationType.ChatRoom:Conversation.ConversationType.Chat));
    }

    public static enum ConversationType {
        Chat,
        GroupChat,
        ChatRoom,
        DiscussionGroup,
        HelpDesk;

        private ConversationType() {
        }
    }
}

