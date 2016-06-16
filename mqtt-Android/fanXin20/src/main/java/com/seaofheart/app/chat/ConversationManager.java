package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.CallBack;
import com.seaofheart.app.NotifierEvent;
import com.seaofheart.app.analytics.PerformanceCollector;
import com.seaofheart.app.analytics.TimeTag;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.util.EMLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

class ConversationManager {
    private static final String TAG = ConversationManager.class.getSimpleName();
    private static final int DEFAULT_LOAD_MESSAGE_COUNT = 20;
    private static ConversationManager instance = new ConversationManager();
    private Hashtable<String, Message> allMessages = new Hashtable(); // 全局消息队列
    private Hashtable<String, Conversation> conversations = new Hashtable();
    private Hashtable<String, Conversation> tempConversations = new Hashtable();
    private Hashtable<Conversation.ConversationType, List<Conversation>> typeConversations = new Hashtable();
    private boolean allConversationsLoaded = false;

    ConversationManager() {
    }

    public static ConversationManager getInstance() {
        return instance;
    }

    void loadAllConversations() {
        this.loadAllConversations(20);
    }

    synchronized void loadAllConversations(int var1) {
        if(!this.allConversationsLoaded) {
            this.conversations.clear();
            this.tempConversations.clear();
            TimeTag var2 = new TimeTag();
            var2.start();
            EMLog.d(TAG, "start to load converstations:");
            if(var1 == 1) {
                this.conversations = DBManager.getInstance().c();
            } else {
                this.conversations = DBManager.getInstance().a(var1);
            }

            Hashtable var3 = this.conversations;
            Conversation var4;
            Iterator var5;
            synchronized(this.conversations) {
                var5 = this.conversations.values().iterator();

                while(true) {
                    if(!var5.hasNext()) {
                        break;
                    }

                    var4 = (Conversation)var5.next();
                    EMLog.d(TAG, "loaded user " + var4.getUserName());
                }
            }

            var3 = this.conversations;
            synchronized(this.conversations) {
                var5 = this.conversations.values().iterator();

                while(true) {
                    if(!var5.hasNext()) {
                        break;
                    }

                    var4 = (Conversation)var5.next();
                    Iterator var7 = var4.msgList.iterator();

                    while(var7.hasNext()) {
                        Message var6 = (Message)var7.next();
                        Hashtable var8 = this.allMessages;
                        synchronized(this.allMessages) {
                            this.allMessages.put(var6.msgId, var6);
                        }
                    }
                }
            }

            if(this.conversations != null && this.allMessages != null && this.conversations.size() > 0) {
                PerformanceCollector.collectLoadingAllConversations(this.conversations.size(), this.allMessages.size(), var2.stop());
            }

            this.allConversationsLoaded = true;
        }
    }

    public void asyncloadAllConversations(final CallBack var1, final int var2) {
        (new Thread() {
            public void run() {
                ConversationManager.this.loadAllConversations(var2);
                if(var1 != null) {
                    var1.onSuccess();
                }

            }
        }).start();
    }

    public void asyncloadAllConversations(CallBack var1) {
        this.asyncloadAllConversations(var1, 20);
    }

    synchronized List<Message> getMessagesByMsgType(ProtocolMessage.TYPE var1, Message.ChatType var2, String var3, String var4, int var5) {
        List var6 = DBManager.getInstance().a(var1, var2, var3, var4, var5);
        return var6;
    }

    synchronized long getTotalMessageCountByMsgType(ProtocolMessage.TYPE var1, Message.ChatType var2, String var3) {
        return DBManager.getInstance().a(var1, var2, var3);
    }

    /**
     * 获取和某人或群组的会话信息
     * @param participant 对方用户名，可能是个人也可能是群组、房间名
     * @return
     */
    public Conversation getConversation(String participant) {
        EMLog.d(TAG, "get conversation for user:" + participant);
        Conversation conversation = (Conversation)this.conversations.get(participant);
        if(conversation != null) { // 已有会话对象，直接返回
            return conversation;
        } else {
            conversation = (Conversation)this.tempConversations.get(participant);
            if(conversation != null) {
                return conversation;
            } else {
                List list = null;
                MultiUserChatRoomModelBase var4 = MultiUserChatManager.getInstance().getRoom(participant);
                long var5;
                if(var4 == null) {
                    list = DBManager.getInstance().getMessages(participant, (String)null, 20); // 获取单聊最后20条聊天记录
                    var5 = DBManager.getInstance().getMessageCount(participant, false); // 获取单聊会话数
                    if(CustomerService.getInstance().isCustomServiceAgent(participant)) { // 跳到帮助桌面
                        conversation = new Conversation(participant, list, Conversation.ConversationType.HelpDesk, Long.valueOf(var5));
                    } else { // 跳到聊天框
                        conversation = new Conversation(participant, list, Conversation.ConversationType.Chat, Long.valueOf(var5));
                    }
                } else {
                    list = DBManager.getInstance().getMessages(participant, (String)null, 20); // 获取群聊最后20条聊天记录
                    var5 = DBManager.getInstance().getMessageCount(participant, true); // 获取群聊会话数
                    if(var4 instanceof Group) {
                        conversation = new Conversation(participant, list, Conversation.ConversationType.GroupChat, Long.valueOf(var5));
                    } else if(var4 instanceof ChatRoom) {
                        conversation = new Conversation(participant, list, Conversation.ConversationType.ChatRoom, Long.valueOf(var5));
                    }
                }

                this.tempConversations.put(participant, conversation); // 加入临时会话对象列表中
                return conversation;
            }
        }
    }

    /**
     * 获取和某人或群组的会话信息
     * @param participant 参与者，即对方名称或群组名称
     * @param isGroup 是否群组 true 是,false 否
     * @param covType 会话类型
     * @return
     */
    Conversation getConversation(String participant, boolean isGroup, Conversation.ConversationType covType) {
        EMLog.d(TAG, "get conversation for user:" + participant);
        Conversation conversation = (Conversation)this.conversations.get(participant);
        if(conversation != null) {
            return conversation;
        } else {
            conversation = (Conversation)this.tempConversations.get(participant);
            if(conversation != null) {
                return conversation;
            } else {
                List var5 = null;
                long var6 = 0L;
                if(!isGroup) {
                    var5 = DBManager.getInstance().getMessages(participant, (String)null, 20);
                    var6 = DBManager.getInstance().getMessageCount(participant, false);
                } else {
                    var5 = DBManager.getInstance().getMessages(participant, (String)null, 20);
                    var6 = DBManager.getInstance().getMessageCount(participant, true);
                }

                conversation = new Conversation(participant, var5, covType, Long.valueOf(var6));
                this.tempConversations.put(participant, conversation);
                return conversation;
            }
        }
    }

    /**
     * 删除会话
     * @param participant
     * @return
     */
    public boolean deleteConversation(String participant) {
        EMLog.d(TAG, "remove conversation for user: " + participant);
        Conversation var2 = (Conversation)this.conversations.get(participant);
        if(var2 == null) {
            var2 = this.getConversation(participant);
        }

        return var2 == null?false:this.deleteConversation(participant, var2.isGroup());
    }

    /**
     * 删除会话
     * @param participant
     * @param var2
     * @return
     */
    public boolean deleteConversation(String participant, boolean var2) {
        return this.deleteConversation(participant, var2, true);
    }

    public boolean deleteConversation(String participant, boolean var2, boolean isGroup) {
        EMLog.d(TAG, "remove conversation for user: " + participant);
        Conversation var4 = (Conversation)this.conversations.get(participant);
        if(var4 == null) {
            var4 = this.getConversation(participant);
        }

        if(var4 == null) {
            return false;
        } else {
            if(isGroup) {
                if(var2) {
                    DBManager.getInstance().delGroupMessages(participant);
                } else {
                    DBManager.getInstance().delParticipaintMessages(participant);
                }
            }

            DBManager.getInstance().delConversation(participant, var2);

            try {
                List var5 = var4.getAllMessages();
                synchronized(var5) {
                    Iterator var8 = var5.iterator();

                    while(var8.hasNext()) {
                        Message var7 = (Message)var8.next();
                        if(this.allMessages.containsKey(var7.getMsgId())) {
                            this.allMessages.remove(var7.getMsgId());
                        }
                    }
                }
            } catch (Exception var10) {
                ;
            }

            var4.clear();
            this.conversations.remove(participant);
            if(this.tempConversations.containsKey(participant)) {
                this.tempConversations.remove(participant);
            }

            Notifier.getInstance(Chat.getInstance().getAppContext()).publishEvent(NotifierEvent.Event.EventConversationListChanged, (Object)null);
            return true;
        }
    }

    void deleteAllConversations() {
        Hashtable var1 = this.conversations;
        synchronized(this.conversations) {
            Enumeration var2 = this.conversations.keys();

            while(var2.hasMoreElements()) {
                String var3 = (String)var2.nextElement();
                this.deleteConversation(var3);
            }

        }
    }

    void resetAllUnreadMsgCount() {
        Hashtable var1 = this.conversations;
        synchronized(this.conversations) {
            Enumeration var2 = this.conversations.keys();

            while(var2.hasMoreElements()) {
                String var3 = (String)var2.nextElement();
                ((Conversation)this.conversations.get(var3)).resetUnreadMsgCount();
            }

        }
    }

    /**
     * 清理会话
     * @param participaint
     * @return
     */
    public boolean clearConversation(String participaint) {
        EMLog.d(TAG, "clear conversation for user: " + participaint);
        Conversation var2 = (Conversation)this.conversations.get(participaint);
        if(var2 == null) {
            var2 = this.getConversation(participaint);
        }

        if(var2 == null) {
            return false;
        } else {
            if(var2.isGroup()) {
                DBManager.getInstance().delGroupMessages(participaint);
            } else {
                DBManager.getInstance().delParticipaintMessages(participaint);
            }

            try {
                List var3 = var2.getAllMessages();
                synchronized(var3) {
                    Iterator var6 = var3.iterator();

                    while(var6.hasNext()) {
                        Message var5 = (Message)var6.next();
                        if(this.allMessages.containsKey(var5.getMsgId())) {
                            this.allMessages.remove(var5.getMsgId());
                        }
                    }
                }
            } catch (Exception var8) {
                ;
            }

            var2.clear();
            var2.msgCount = 0L;
            return true;
        }
    }

    void removeMessage(String var1) {
        this.allMessages.remove(var1);
    }

    public Hashtable<String, Conversation> getAllConversations() {
        return this.conversations;
    }

    public Message getMessage(String var1) {
        return (Message)this.allMessages.get(var1);
    }

    void replaceMessageId(String var1, String var2) {
        Message var3 = (Message)this.allMessages.get(var1);
        if(var3 != null) {
            DBManager.getInstance().a(var1, var2);
            this.allMessages.remove(var1);
            var3.msgId = var2;
            this.allMessages.put(var2, var3);
        }

    }

    boolean areAllConversationsLoaded() {
        return this.allConversationsLoaded;
    }

    void addMessage(Message var1) {
        this.addMessage(var1, true);
    }

    void addMessage(Message msg, boolean var2) {
        String msgId = msg.msgId;
        if(!this.allMessages.containsKey(msgId)) { // 消息不重复
            this.allMessages.put(msgId, msg); // 加入到全局消息队列
            boolean var5 = false;
            String toUserId;
            if(msg.getChatType() == ProtocolMessage.CHAT_TYPE.CHAT_GROUP) { // 单聊
                if(msg.direct == ProtocolMessage.DIRECT.RECEIVE) { // 収消息
                    toUserId = msg.from.username; // 对方用户id
                } else { // 发消息
                    toUserId = msg.to.username; // 对方用户id
                }
            } else {
                toUserId = msg.getTo();
                var5 = true;
            }

            Conversation conversation = this.getConversation(toUserId, var5, Conversation.msgType2ConversationType(toUserId, msg.getChatType()));
            conversation.addMessage(msg, var2);
            if(!this.conversations.containsKey(toUserId)) { // 如果是新会话，则将会话添加到全局会话列表中
                this.conversations.put(toUserId, conversation);
            }
        }

    }

    private void addConversationToDB(Message var1) {
        String var2 = "";
        boolean var3 = var1.getChatType() != ProtocolMessage.CHAT_TYPE.CHAT_SINGLE;
        if(var3) {
            var2 = var1.getTo();
        } else if(var1.getFrom().equals(ChatManager.getInstance().getCurrentUser())) {
            var2 = var1.getTo();
        } else {
            var2 = var1.getFrom();
        }

        DBManager.getInstance().a(var2, Conversation.msgType2ConversationType(var2, var1.getChatType()));
    }

    public void saveMessage(Message var1) {
        EMLog.d(TAG, "save message:" + var1.getMsgId());

        try {
            if(!this.allMessages.containsKey(var1.getMsgId())) {
                this.addMessage(var1);
                DBManager.getInstance().saveMessage(var1);
            }

            this.addConversationToDB(var1);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public void saveMessage(Message var1, boolean var2) {
        EMLog.d(TAG, "save message:" + var1.getMsgId());
        try {
            this.addMessage(var1, var2);
            DBManager.getInstance().saveMessage(var1);
            this.addConversationToDB(var1);
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    public int getUnreadMsgsCount() {
        int var1 = 0;
        Hashtable var2 = this.conversations;
        synchronized(this.conversations) {
            Collection var3 = this.conversations.values();
            Iterator var4 = var3.iterator();

            while(true) {
                if(!var4.hasNext()) {
                    break;
                }

                var1 += ((Conversation)var4.next()).getUnreadMsgCount();
            }
        }

        EMLog.d(TAG, "getunreadmsgcount return:" + var1);
        return var1;
    }

    public List<String> getConversationsUnread() {
        return DBManager.getInstance().h();
    }

    public List<Conversation> getConversationsByType(Conversation.ConversationType var1) {
        ArrayList var2 = new ArrayList();
        Hashtable var3 = this.conversations;
        synchronized(this.conversations) {
            Iterator var5 = this.conversations.values().iterator();

            while(var5.hasNext()) {
                Conversation var4 = (Conversation)var5.next();
                if(var4.getType() == var1) {
                    var2.add(var4);
                }
            }

            return var2;
        }
    }

    public synchronized void clear() {
        if(this.conversations != null) {
            this.conversations.clear();
        }

        if(this.tempConversations != null) {
            this.tempConversations.clear();
        }

        if(this.allMessages != null) {
            this.allMessages.clear();
        }

        this.allConversationsLoaded = false;
    }
}

