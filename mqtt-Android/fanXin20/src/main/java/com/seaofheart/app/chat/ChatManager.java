package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.text.TextUtils;
import android.util.Pair;

import com.seaofheart.app.Error;
import com.seaofheart.app.util.net;
import com.seaofheart.app.chat.core.j;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.chat.core.ChatConnectionListener;
import com.seaofheart.app.CallBack;
import com.seaofheart.app.EMConnectionListener;
import com.seaofheart.app.EventListener;
import com.seaofheart.app.NotifierEvent;
import com.seaofheart.app.ValueCallBack;
import com.seaofheart.app.chat.core.ConnectionManager;
import com.seaofheart.app.chat.core.AdvanceDebugManager;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.cloud.HttpClientConfig;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.exceptions.ExceptionUtils;
import com.seaofheart.app.exceptions.NetworkUnconnectedException;
import com.seaofheart.app.exceptions.NoActiveCallException;
import com.seaofheart.app.exceptions.ServiceNotReadyException;
import com.seaofheart.app.util.CryptoUtils;
import com.seaofheart.app.util.EMLog;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class ChatManager {
    private static final String TAG = "ChatManager";
    private static final String NEW_MSG_BROADCAST = "easemob.newmsg.";
    private static final String READ_ACK_MSG_BROADCAST = "easemob.ackmsg.";
    private static final String DELIVERY_ACK_MSG_BROADCAST = "easemob.deliverymsg.";
    private static final String CONTACT_INVITE_EVENT_BROADCAST = "easemob.contact.invite.";
    private static final String OFFLINE_MSG_BROADCAST = "easemob.offlinemsg.";
    private static final String INCOMING_VOICE_CALL_BROADCAST = "easemob.incomingvoicecall.invite";
    private static final String INCOMING_CALL_BROADCAST = "easemob.incomingcall.invite";
    private static final String CMD_MSG_BROADCAST = "easemob.cmdmsg";
    private static ChatManager instance = new ChatManager();
    //private org.jivesoftware.smack.ChatManager xmppChatManager;
    private ConnectionManager connectionManager;
    //private ChatManager.EMChatManagerListener chatManagerListener = new ChatManager.EMChatManagerListener();
    //private final MessageListener msgListener = new MessageListener(this);
    private final GroupMessageListener groupChatListener = new GroupMessageListener(this);
    private final RecvAckListener recvAckListener = new RecvAckListener();
    //private Map<String, org.jivesoftware.smack.Chat> chats = new HashMap();
    private final List<ConnectionListener> connectionListeners = Collections.synchronizedList(new ArrayList());
    private List<EMConnectionListener> newConnectionListeners = Collections.synchronizedList(new ArrayList());
    private Handler handler = new Handler();
    private final ChatManager.XmppConnectionListener xmppConnectionListener = new ChatManager.XmppConnectionListener();
    private Context applicationContext;
    private Notifier notifier;
    private CryptoUtils cryptoUtils = new CryptoUtils();
    private EncryptProvider encryptProvider = null;
    private ExecutorService threadPool = null;
    private ChatOptions chatOptions;
    //private ArrayList<Presence> offlineRosterPresenceList = new ArrayList();
    ExecutorService msgCountThreadPool;
    OfflineMessageHandler offlineHandler = null;
    OfflineMessageHandler chatroomOfflineHandler = null;
    boolean stopService;

    public void login(String hxid, String password, final CallBack var3) {
        if(!Chat.getInstance().isSDKInited()) {
            throw new RuntimeException("SDK is not initialized!");
        } else if(TextUtils.isEmpty(ChatConfig.getInstance().APPKEY)) {
            throw new RuntimeException("please setup your appkey either in AndroidManifest.xml or through Chat.setAppkey");
        } else if(var3 == null) {
            throw new IllegalArgumentException("callback is null!");
        } else if(hxid != null && password != null && !hxid.equals("") && !password.equals("")) {
            EMLog.e("ChatManager", "Chat manager login in process:" + Process.myPid());
            hxid = hxid.toLowerCase();
            SessionManager.getInstance().login(hxid, password, true, new CallBack() {
                public void onSuccess() {
                    ChatManager.this.doStartService();
                    ChatManager.this.saveAppname();
                    var3.onSuccess();
                }

                public void onProgress(int var1, String var2) {
                }

                public void onError(int var1, String var2) {
                    j.getInstance().l();
                    ChatManager.this.doStopService();
                    var3.onError(var1, var2);
                }
            });
        } else {
            throw new IllegalArgumentException("username or password is null or empty!");
        }
    }

    public void logout() {
        EMLog.d("ChatManager", " SDK Logout");
        SessionManager.getInstance().clearLastLoginUser();
        SessionManager.getInstance().clearLastLoginPwd();
        Chat.getInstance().clear();

        try {
            ContactManager.getInstance().reset();
            this.groupChatListener.clear();
            //this.msgListener.clear();
            this.offlineHandler.reset();
            this.chatroomOfflineHandler.reset();
            //this.chats.clear();
            ConversationManager.getInstance().clear();
            MessageHandler.getInstance().onDestroy();
            CustomerService.getInstance().onDestroy();
            AdvanceDebugManager.getInstance().onDestroy();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        MultiUserChatManager.getInstance().onDestroy();

        try {
            if(DBManager.getInstance() != null) {
                DBManager.getInstance().onDestroy();
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        try {
            SessionManager.getInstance().syncLogout();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

        j.getInstance().l();
        Chat.getInstance().appInited = false;
        if(ChatConfig.isDebugTrafficMode()) {
            net.c();
        }

        Monitor.getInstance().getMonitorDB().b(this.applicationContext.getPackageName());
        this.doStopService();
    }

    public void logout(final CallBack var1) {
        Thread var2 = new Thread() {
            public void run() {
                if(var1 != null) {
                    var1.onProgress(0, (String)null);
                }

                ChatManager.this.logout();
                if(var1 != null) {
                    var1.onSuccess();
                }

            }
        };
        var2.setPriority(9);
        var2.start();
    }

    public void logout(final boolean var1, final CallBack var2) {
        if(var2 == null) {
            throw new RuntimeException("callback is null");
        } else {
            Thread var3 = new Thread() {
                public void run() {
                    var2.onProgress(0, (String)null);
                    int var1x = ChatManager.this.logout(var1);
                    if(var1x == 0) {
                        var2.onSuccess();
                    } else {
                        var2.onError(Error.ERROR_UNBIND_DEVICETOKEN, "unbind devicetoken failed");
                    }

                }
            };
            var3.setPriority(9);
            var3.start();
        }
    }

    public int logout(boolean var1) {
        try {
            PushNotificationHelper.getInstance().onDestroy(var1);
        } catch (EaseMobException var3) {
            return Error.ERROR_UNBIND_DEVICETOKEN;
        } catch (Exception var4) {
            return Error.ERROR_UNBIND_DEVICETOKEN;
        }

        this.logout();
        return 0;
    }

    void changePasswordOnServer(String var1) throws EaseMobException {
        //SessionManager.getInstance().changePasswordXMPP(var1);
    }

    public void createAccountOnServer(String var1, String var2) throws EaseMobException {
        var1 = var1.toLowerCase();
        Pattern var3 = Pattern.compile("^[a-zA-Z0-9_-]{1,}$");
        boolean var4 = var3.matcher(var1).find();
        if(!var4) {
            throw new EaseMobException(Error.ILLEGAL_USER_NAME, "illegal user name");
        } else {
            SessionManager.getInstance().createAccountRest(var1, var2);
        }
    }

    public boolean isConnected() {
        return SessionManager.getInstance().isConnected();
    }

    public void registerEventListener(EventListener var1) {
        Notifier.getInstance(this.applicationContext).registerEventListener(var1);
    }

    public void registerEventListener(EventListener var1, NotifierEvent.Event[] var2) {
        Notifier.getInstance(this.applicationContext).registerEventListener(var1, var2);
    }

    public void unregisterEventListener(EventListener var1) {
        Notifier.getInstance(this.applicationContext).removeEventListener(var1);
    }

    private ChatManager() {
        this.cryptoUtils.init(1);
        this.threadPool = Executors.newCachedThreadPool();
        this.msgCountThreadPool = Executors.newSingleThreadExecutor();
        this.chatOptions = new ChatOptions();
        new ChatManager.ChatServiceConnection();
        this.offlineHandler = new OfflineMessageHandler();
        this.chatroomOfflineHandler = new OfflineMessageHandler();
        this.chatroomOfflineHandler.setPublishInterval(1000L);
    }

    public static synchronized ChatManager getInstance() {
        if(instance.applicationContext == null) {
            instance.applicationContext = Chat.getInstance().getAppContext();
        }

        return instance;
    }

    synchronized ChatManager onInit() {
        EMLog.d("ChatManager", "init chat manager");
        if(this.applicationContext == null) {
            this.applicationContext = Chat.getInstance().getAppContext();
        }

        this.notifier = Notifier.getInstance(this.applicationContext);
        AdvanceDebugManager.getInstance().onInit();
        return this;
    }

    void onNewConnectionCreated(ConnectionManager connectionManager) {
        EMLog.d("ChatManager", "on new connection created");
        this.init(connectionManager);
        MultiUserChatManager.getInstance().onInit();
        PushNotificationHelper.getInstance().onInit();
        CustomerService.getInstance().onInit();
        //this.addPacketListeners(var1.getConnection());
        if(ContactManager.getInstance().enableRosterVersion) {
            EMLog.d("ChatManager", "enable roster version. set roster storage");
            //var1.getConnection().setRosterStorage(ContactManager.getInstance().getRosterStorage(this.applicationContext));
            ContactManager.getInstance().loadContacts();
        }

        this.doStartService();
    }

    void onAppInited() {
        try {
            //this.processOfflinePresenceMessages();
            MultiUserChatManager.getInstance().onAppReady();
            this.processOfflineMessages();
            this.processOfflineCmdMessages();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    /*private void addPacketListeners(XMPPConnection var1) {
        if(!var1.isConnected() || !var1.isAuthenticated()) {
            MessageTypeFilter var2 = new MessageTypeFilter(org.jivesoftware.smack.packet.Message.Type.chat);
            var1.addPacketListener(this.msgListener, var2);
            MessageTypeFilter var3 = new MessageTypeFilter(org.jivesoftware.smack.packet.Message.Type.groupchat);
            var1.addPacketListener(this.groupChatListener, var3);
            MessageTypeFilter var4 = new MessageTypeFilter(org.jivesoftware.smack.packet.Message.Type.normal);
            var1.addPacketListener(this.recvAckListener, var4);
            MessageTypeFilter var5 = new MessageTypeFilter(org.jivesoftware.smack.packet.Message.Type.notify);
            var1.addPacketListener(new PacketListener() {
                public void processPacket(Packet var1) {
                    MessageListener.ackMessage((org.jivesoftware.smack.packet.Message)var1);
                }
            }, var5);
            PacketTypeFilter var6 = new PacketTypeFilter(Presence.class) {
                public boolean accept(Packet var1) {
                    if(var1 instanceof Presence) {
                        Presence var2 = (Presence)var1;
                        if(var2.getType().equals(org.jivesoftware.smack.packet.Presence.Type.subscribed) || var2.getType().equals(org.jivesoftware.smack.packet.Presence.Type.subscribe) || var2.getType().equals(org.jivesoftware.smack.packet.Presence.Type.unsubscribed) || var2.getType().equals(org.jivesoftware.smack.packet.Presence.Type.unsubscribe)) {
                            return true;
                        }
                    }

                    return false;
                }
            };
            //this.connectionManager.getConnection().addPacketListener(new ChatManager.SingleInvitationListener((ChatManager.SingleInvitationListener)null), var6);
            //this.connectionManager.getConnection().addPacketListener(new ChatManager.SingleInvitationListener(), var6);
        }
    }*/

    private void init(ConnectionManager connectionManager) {
        EMLog.d("ChatManager", "init chat manager");
        if(connectionManager != null && connectionManager.getConnection() != null) {
            try {
                AdvanceDebugManager.getInstance().a(connectionManager);
                //this.chats.clear();
                this.connectionManager = connectionManager;
                //this.xmppChatManager = connectionManager.getConnection().getChatManager();
                //this.xmppChatManager.addChatListener(this.chatManagerListener);
                MessageHandler.getInstance().onInit();
                connectionManager.setChatConnectionListener(this.xmppConnectionListener);

                try {
                    if(Class.forName("com.seaofheart.app.chat.VoiceCallManager") != null) {
                        VoiceCallManager.getInstance().init();
                    }
                } catch (Throwable var3) {
                    ;
                }

                this.offlineHandler.setPublishInterval(this.getChatOptions().getOfflineInterval());
            } catch (Exception var4) {
                var4.printStackTrace();
            }

        } else {
            EMLog.e("ChatManager", "error in Chat Manage init. connection is null");
        }
    }

    /*void processOfflinePresenceMessages() {
        EMLog.d("ChatManager", "process offline RosterPresence msg start");
        Iterator var2 = this.offlineRosterPresenceList.iterator();

        while(var2.hasNext()) {
            Presence var1 = (Presence)var2.next();
            this.processRosterPresence(var1);
        }

        this.offlineRosterPresenceList.clear();
        EMLog.d("ChatManager", "proess offline RosterPresence msg finish");
    }*/

    void processOfflineCmdMessages() {
        this.offlineHandler.processOfflineCmdMessages();
        this.chatroomOfflineHandler.processOfflineCmdMessages();
    }

    void processOfflineMessages() {
        this.offlineHandler.onAppReady();
        this.chatroomOfflineHandler.onAppReady();
    }

    void notifyCmdMsg(Message var1) {
        this.notifier.sendCmdMsgBroadcast(var1);
    }

    void onNewOfflineCmdMessage(Message var1) {
        if(var1.getChatType() == ProtocolMessage.CHAT_TYPE.CHAT_ROOM) {
            this.chatroomOfflineHandler.addOfflineCmdMessage(var1);
        } else {
            this.offlineHandler.addOfflineCmdMessage(var1);
        }
    }

    void onNewOfflineMessage(Message var1) {
        if(var1.getChatType() == ProtocolMessage.CHAT_TYPE.CHAT_ROOM) {
            this.chatroomOfflineHandler.onNewOfflineMessage(var1);
        } else {
            this.offlineHandler.onNewOfflineMessage(var1);
        }
    }

    private void processRosterPresence(Presence var1) {
        PresenceHandler.getInstance().processRosterPresence(var1);
    }

    private void acceptInvitation(String var1, boolean var2) throws EaseMobException {
        PresenceHandler.getInstance().acceptInvitation(var1, var2);
    }

    public void acceptInvitation(String var1) throws EaseMobException {
        this.acceptInvitation(ContactManager.getEidFromUserName(var1), true);
    }

    public void refuseInvitation(String var1) throws EaseMobException {
        PresenceHandler.getInstance().refuseInvitation(var1);
    }

    public void sendMessage(Message msg) throws EaseMobException {
        this.sendMessage(msg, (CallBack)null);
    }

    /**
     * 发消息
     * @param msg
     * @param callBack
     */
    public void sendMessage(Message msg, CallBack callBack) {
        CallBack myCallBack = this.getInnerCallBack(callBack, msg);
        if(this.connectionManager == null) {
            MessageUtils.asyncCallback(myCallBack, Error.CONNECTION_INIT_FAILED, "connection init is failed due to failed login");
        } else {
            int var4 = MessageUtils.checkMessageError(msg);
            if(var4 != 0) {
                msg.status = ProtocolMessage.STATUS.FAIL;
                ContentValues var5 = new ContentValues();
                var5.put("status", String.valueOf(msg.status.ordinal()));
                DBManager.getInstance().updateMessage(msg.msgId, var5);
                if(myCallBack != null) {
                    MessageUtils.asyncCallback(myCallBack, var4, "send message error");
                }
            } else if(msg.getChatType() != ProtocolMessage.CHAT_TYPE.CHAT_GROUP && msg.getChatType() != ProtocolMessage.CHAT_TYPE.CHAT_ROOM) {
                String toEid = msg.to.eid;
                if(!toEid.contains("@")) {
                    StringBuilder var10000 = (new StringBuilder(String.valueOf(toEid))).append("@");
                    ChatConfig.getInstance();
                    toEid = var10000.append(ChatConfig.DOMAIN).toString();
                }

                MessageHandler.getInstance().sendMessage(this.connectionManager.getConnection(), msg, myCallBack);
            } else {
                MessageHandler.getInstance().sendGroupMessage(this.connectionManager.getConnection(), msg, myCallBack);
            }

        }
    }

    private CallBack getInnerCallBack(final CallBack var1, final Message var2) {
        CallBack var3 = new CallBack() {
            public void onSuccess() {
                if(var1 != null) {
                    var1.onSuccess();
                }

                if(var2.messageStatusCallBack != null) {
                    var2.messageStatusCallBack.onSuccess();
                }

            }

            public void onProgress(int var1x, String var2x) {
                if(var1 != null) {
                    var1.onProgress(var1x, var2x);
                }

                if(var2.messageStatusCallBack != null) {
                    var2.messageStatusCallBack.onProgress(var1x, var2x);
                }

            }

            public void onError(int var1x, String var2x) {
                if(var1 != null) {
                    var1.onError(var1x, var2x);
                }

                if(var2.messageStatusCallBack != null) {
                    var2.messageStatusCallBack.onError(var1x, var2x);
                }

            }
        };
        return var3;
    }

    public void sendGroupMessage(Message msg, CallBack callBack) {
        MessageHandler.getInstance().sendGroupMessage(this.connectionManager.getConnection(), msg, callBack);
    }

    void notifyMessage(Message var1) {
        this.offlineHandler.stop();
        this.chatroomOfflineHandler.stop();
        this.notifier.notifyChatMsg(var1);
    }

    void broadcastMessage(Message var1) {
        EMLog.d("ChatManager", "broad offline msg");
        this.notifier.sendBroadcast(var1);
    }

    void notifiyReadAckMessage(String var1, String var2) {
        this.notifier.sendReadAckMsgBroadcast(var1, var2);
    }

    void notifyDeliveryAckMessage(String var1, String var2) {
        this.notifier.sendDeliveryAckMsgBroadcast(var1, var2);
    }

    void notifyMessageChanged(MessageChangeEventData var1) {
        this.notifier.publishEvent(NotifierEvent.Event.EventMessageChanged, var1);
    }

    void notifyIncomingCall(String var1, VoiceCallManager.CallType var2) {
        this.notifier.sendIncomingVoiceCallBroadcast(var1, var2.toString());
    }

    public String getNewMessageBroadcastAction() {
        return "easemob.newmsg." + this.getBroadcastSuffix();
    }

    public String getCmdMessageBroadcastAction() {
        return "easemob.cmdmsg" + this.getBroadcastSuffix();
    }

    public String getAckMessageBroadcastAction() {
        return "easemob.ackmsg." + this.getBroadcastSuffix();
    }

    public String getDeliveryAckMessageBroadcastAction() {
        return "easemob.deliverymsg." + this.getBroadcastSuffix();
    }

    public String getContactInviteEventBroadcastAction() {
        return "easemob.contact.invite." + this.getBroadcastSuffix();
    }

    public String getOfflineMessageBroadcastAction() {
        return "easemob.offlinemsg." + this.getBroadcastSuffix();
    }

    public String getIncomingVoiceCallBroadcastAction() {
        return "easemob.incomingvoicecall.invite" + this.getBroadcastSuffix();
    }

    public String getIncomingCallBroadcastAction() {
        return "easemob.incomingcall.invite" + this.getBroadcastSuffix();
    }

    private String getBroadcastSuffix() {
        if(TextUtils.isEmpty(ChatConfig.getInstance().APPKEY)) {
            throw new RuntimeException("please setup your appkey either in AndroidManifest.xml or through Chat.setAppkey");
        } else {
            return this.applicationContext != null?ChatConfig.getInstance().APPKEY.replaceAll("#", ".").replaceAll("-", ".") + this.applicationContext.getPackageName():ChatConfig.getInstance().APPKEY.replaceAll("#", ".").replaceAll("-", ".");
        }
    }

    public void ackMessageRead(String var1, String var2) throws EaseMobException {
        if(!this.chatOptions.getRequireAck()) {
            EMLog.d("ChatManager", "chat option reqire ack set to false. skip send out ask msg read");
        } else {
            this.checkConnection();
            String var3 = ContactManager.getEidFromUserName(var1);
            /*org.jivesoftware.smack.Chat var4 = (org.jivesoftware.smack.Chat)this.chats.get(var3);
            if(var4 == null) {
                //var4 = this.xmppChatManager.createChat(var3, (org.jivesoftware.smack.MessageListener)null);
                this.chats.put(var3, var4);
            }*/

            MessageHandler.getInstance().ackMessageRead(this.getCurrentUser(), var1, var2);
        }
    }

    /**
     * 设置消息为已监听状态
     * @param msg
     */
    public void setMessageListened(Message msg) {
        msg.setListened(true);
        DBManager.getInstance().updateMsgListen(msg.getMsgId(), true);
    }

    void checkConnection() throws EaseMobException {
        SessionManager.getInstance().checkConnection();
    }

    public void addConnectionListener(final ConnectionListener var1) {
        if(var1 != null) {
            this.connectionListeners.add(var1);
            if(this.connectionManager != null && this.connectionManager.getConnection() != null && this.connectionManager.getConnection().isConnected()) {
                this.handler.post(new Runnable() {
                    public void run() {
                        Iterator var2 = ChatManager.this.connectionListeners.iterator();

                        while(var2.hasNext()) {
                            ConnectionListener var1x = (ConnectionListener)var2.next();
                            if(var1x != null && var1x.equals(var1)) {
                                var1x.onConnected();
                            }
                        }

                    }
                });
            } else {
                this.handler.post(new Runnable() {
                    public void run() {
                        Iterator var2 = ChatManager.this.connectionListeners.iterator();

                        while(var2.hasNext()) {
                            ConnectionListener var1x = (ConnectionListener)var2.next();
                            if(var1x != null && var1x.equals(var1)) {
                                var1x.onDisConnected("connection is disconnected");
                            }
                        }

                    }
                });
            }
        }

    }

    /**
     * 添加一个监听连接状态的listener
     * @param emConnectionListener
     */
    public void addConnectionListener(final EMConnectionListener emConnectionListener) {
        if(emConnectionListener != null) {
            if(!this.connectionListeners.contains(emConnectionListener)) {
                this.newConnectionListeners.add(emConnectionListener);
                if(this.connectionManager != null && this.connectionManager.isConnected()) { // xampp服务器链接成功
                    this.threadPool.submit(new Runnable() {
                        public void run() {
                            emConnectionListener.onConnected(); //调用继承于EMConnectionListener的子类MainActivity.MyConnectionListener下的onConnected()函数
                        }
                    });
                } else { // xampp服务器链接失败
                    this.threadPool.submit(new Runnable() {
                        public void run() {
                            emConnectionListener.onDisconnected(Error.NONETWORK_ERROR);
                        }
                    });
                }

            }
        }
    }

    public void removeConnectionListener(ConnectionListener var1) {
        if(var1 != null) {
            this.connectionListeners.remove(var1);
        }

    }

    public void removeConnectionListener(EMConnectionListener var1) {
        if(var1 != null) {
            this.newConnectionListeners.remove(var1);
        }

    }

    public Message getMessage(String var1) {
        return ConversationManager.getInstance().getMessage(var1);
    }

    void replaceMessageId(String var1, String var2) {
        ConversationManager.getInstance().replaceMessageId(var1, var2);
    }

    void addMessage(Message var1) {
        ConversationManager.getInstance().addMessage(var1);
    }

    void addMessage(Message var1, boolean var2) {
        ConversationManager.getInstance().addMessage(var1, var2);
    }

    public Conversation getConversation(String var1) {
        return ConversationManager.getInstance().getConversation(var1);
    }

    public Conversation getConversation(String var1, boolean var2) {
        return var2?ConversationManager.getInstance().getConversation(var1, var2, Conversation.ConversationType.GroupChat):ConversationManager.getInstance().getConversation(var1, var2, Conversation.ConversationType.Chat);
    }

    public Conversation getConversationByType(String var1, Conversation.ConversationType var2) {
        boolean var3 = var2 != Conversation.ConversationType.Chat;
        return ConversationManager.getInstance().getConversation(var1, var3, var2);
    }

    public boolean deleteConversation(String var1) {
        return ConversationManager.getInstance().deleteConversation(var1);
    }

    public void deleteAllConversation() {
        ConversationManager.getInstance().deleteAllConversations();
    }

    public void resetAllUnreadMsgCount() {
        ConversationManager.getInstance().resetAllUnreadMsgCount();
    }

    public void markAllConversationsAsRead() {
        ConversationManager.getInstance().resetAllUnreadMsgCount();
    }

    public boolean deleteConversation(String var1, boolean var2) {
        return ConversationManager.getInstance().deleteConversation(var1, var2);
    }

    public boolean deleteConversation(String var1, boolean var2, boolean var3) {
        return ConversationManager.getInstance().deleteConversation(var1, var2, var3);
    }

    public boolean clearConversation(String var1) {
        return ConversationManager.getInstance().clearConversation(var1);
    }

    public void loadAllConversations(CallBack var1) {
        ConversationManager.getInstance().asyncloadAllConversations(var1, this.chatOptions.getNumberOfMessagesLoaded());
    }

    public void asyncLoadAllConversations(CallBack var1) {
        this.asyncLoadAllConversations(var1, this.chatOptions.getNumberOfMessagesLoaded());
    }

    void asyncLoadAllConversations(CallBack var1, int var2) {
        ConversationManager.getInstance().asyncloadAllConversations(var1, var2);
    }

    public void loadAllConversations() {
        ConversationManager.getInstance().loadAllConversations(this.chatOptions.getNumberOfMessagesLoaded());
    }

    public Hashtable<String, Conversation> getAllConversations() {
        return ConversationManager.getInstance().getAllConversations();
    }

    public int getUnreadMsgsCount() {
        return ConversationManager.getInstance().getUnreadMsgsCount();
    }

    public void activityResumed() {
        if(this.notifier != null) {
            this.notifier.resetNotificationCount();
            this.notifier.cancelNotificaton();
        }

    }

    public void saveMessage(Message var1) {
        ConversationManager.getInstance().saveMessage(var1);
    }

    public void saveMessage(Message var1, boolean var2) {
        ConversationManager.getInstance().saveMessage(var1, var2);
    }

    public boolean updateMessageBody(Message var1) {
        return DBManager.getInstance().updateMessage(var1);
    }

    void updateMessageState(Message var1) {
        ContentValues var2 = new ContentValues();
        var2.put("status", String.valueOf(var1.status.ordinal()));
        DBManager.getInstance().updateMessage(var1.msgId, var2);
    }

    public List<String> getContactUserNames() throws EaseMobException {
        return ContactManager.getInstance().getRosterUserNames();
    }

    public String getCurrentUser() {
        return SessionManager.getInstance().currentUser.username;
    }

    public ChatOptions getChatOptions() {
        return this.chatOptions;
    }

    public void setChatOptions(ChatOptions var1) {
        this.chatOptions = var1;
    }

    public void setEncryptProvider(EncryptProvider var1) {
        this.encryptProvider = var1;
    }

    public EncryptProvider getEncryptProvider() {
        if(this.encryptProvider == null) {
            EMLog.d("ChatManager", "encrypt provider is not set, create default");
            this.encryptProvider = new EncryptProvider() {
                public byte[] encrypt(byte[] var1, String var2) {
                    try {
                        return ChatManager.this.cryptoUtils.encrypt(var1);
                    } catch (Exception var4) {
                        var4.printStackTrace();
                        return var1;
                    }
                }

                public byte[] decrypt(byte[] var1, String var2) {
                    try {
                        return ChatManager.this.cryptoUtils.decrypt(var1);
                    } catch (Exception var4) {
                        var4.printStackTrace();
                        return var1;
                    }
                }
            };
        }

        return this.encryptProvider;
    }

    public void addVoiceCallStateChangeListener(CallStateChangeListener var1) {
        VoiceCallManager.getInstance().addStateChangeListener(var1);
    }

    public void addCallStateChangeListener(CallStateChangeListener var1) {
        VoiceCallManager.getInstance().addStateChangeListener(var1);
    }

    public void removeCallStateChangeListener(CallStateChangeListener var1) {
        VoiceCallManager.getInstance().removeStateChangeListener(var1);
    }

    public void makeVoiceCall(String var1) throws ServiceNotReadyException {
        VoiceCallManager.getInstance().makeCall(var1, VoiceCallManager.CallType.audio);
    }

    public void answerCall() throws NoActiveCallException, NetworkUnconnectedException {
        VoiceCallManager.getInstance().answerCall();
    }

    public void rejectCall() throws NoActiveCallException {
        VoiceCallManager.getInstance().rejectCall();
    }

    public void endCall() {
        VoiceCallManager.getInstance().endCall();
    }

    public void makeVideoCall(String var1) throws ServiceNotReadyException {
        VoiceCallManager.getInstance().makeCall(var1, VoiceCallManager.CallType.video);
    }

    CryptoUtils getCryptoUtils() {
        return this.cryptoUtils;
    }

    void doStartService() {
        EMLog.d("ChatManager", "do start service: context:" + this.applicationContext);
        this.stopService = false;
        Intent var1 = new Intent(this.applicationContext, EMChatService.class);
        this.applicationContext.startService(var1);
    }

    void doStopService() {
        try {
            if(this.applicationContext == null) {
                EMLog.w("ChatManager", "applicationContext is null, the server is not started before");
                return;
            }

            EMLog.d("ChatManager", "do stop service");
            this.stopService = true;
            Intent var1 = new Intent(this.applicationContext, EMChatService.class);
            this.applicationContext.stopService(var1);
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    void saveAppname() {
        Monitor.getInstance().getMonitorDB().a(this.applicationContext.getPackageName());
    }

    public void onReconnectionSuccessful() {
        this.handler.post(new Runnable() {
            public void run() {
                Iterator var2 = ChatManager.this.connectionListeners.iterator();

                while(var2.hasNext()) {
                    ConnectionListener var1 = (ConnectionListener)var2.next();
                    if(var1 != null) {
                        var1.onReConnected();
                    }
                }

            }
        });
        this.threadPool.submit(new Runnable() {
            public void run() {
                Iterator var2 = ChatManager.this.newConnectionListeners.iterator();

                while(var2.hasNext()) {
                    ConnectionListener var1 = (ConnectionListener)var2.next();
                    var1.onConnected();
                }

            }
        });
    }

    public void asyncFetchMessage(Message var1) {
        MessageHandler.getInstance().asyncFetchMessage(var1);
    }

    public String importMessage(Message var1, boolean var2) {
        DBManager.getInstance().c(var1);
        if(var2) {
            this.addMessage(var1);
            this.notifyMessage(var1);
        }

        return var1.getMsgId();
    }

    public synchronized void importMessages(List<Message> var1) {
        DBManager.getInstance().c(var1);
    }

    public boolean updateCurrentUserNick(String var1) {
        if(TextUtils.isEmpty(var1)) {
            EMLog.e("ChatManager", "nick name is null or empty");
            return false;
        } else {
            String var2 = this.getCurrentUser();
            if(TextUtils.isEmpty(var2)) {
                EMLog.e("ChatManager", "currentUser is null or empty");
                return false;
            } else {
                String var3 = this.getAccessToken();
                if(TextUtils.isEmpty(var3)) {
                    EMLog.e("ChatManager", "token is null or empty");
                    return false;
                } else {
                    String var4 = HttpClientConfig.getBaseUrlByAppKey();
                    StringBuilder var5 = new StringBuilder(var4);
                    var5.append("/");
                    var5.append("users/");
                    var5.append(var2);
                    HashMap var6 = new HashMap();
                    var6.put("Authorization", "Bearer " + var3);
                    JSONObject var7 = new JSONObject();

                    try {
                        var7.put("nickname", var1);
                        Pair var8 = HttpClient.getInstance().sendRequest(var5.toString(), var6, var7.toString(), HttpClient.PUT);
                        String var9 = (String)var8.second;
                        if(var9.contains("error")) {
                            EMLog.e("ChatManager", "response error : " + var9);
                            return false;
                        } else {
                            return true;
                        }
                    } catch (Exception var10) {
                        EMLog.e("ChatManager", "error:" + var10.getMessage());
                        return false;
                    }
                }
            }
        }
    }

    void initDB(String var1) {
        Context var2 = Chat.getInstance().getAppContext();
        if(var2 != null) {
            ChatDB.initDB(var1);
        }
    }

    void loadDB() {
        MultiUserChatManager.getInstance().clearRooms();
        ConversationManager.getInstance().clear();
        Thread var1 = new Thread() {
            public void run() {
                EMLog.d("ChatManager", "");
                MultiUserChatManager.getInstance().loadLocalData();
                ChatManager.this.loadAllConversations();
            }
        };
        var1.setPriority(9);
        var1.start();
    }

    public String getAccessToken() {
        if(this.applicationContext == null) {
            EMLog.e("ChatManager", "applicationContext is null");
            return null;
        } else {
            String var1 = ChatConfig.getInstance().APPKEY;
            if(TextUtils.isEmpty(var1)) {
                EMLog.e("ChatManager", "appkey is null or empty");
                return null;
            } else {
                try {
                    return p.getInstance().A();
                } catch (Exception var3) {
                    EMLog.e("ChatManager", "gettoken is error:" + var3.getMessage());
                    return null;
                }
            }
        }
    }

    private void notifyMessageHandlerConnectionConnected() {
        MessageHandler.getInstance().onConnected();
    }

    public List<String> getConversationsUnread() {
        return ConversationManager.getInstance().getConversationsUnread();
    }

    public boolean areAllConversationsLoaded() {
        return ConversationManager.getInstance().areAllConversationsLoaded();
    }

    public List<Conversation> getConversationsByType(Conversation.ConversationType var1) {
        return ConversationManager.getInstance().getConversationsByType(var1);
    }

    void forceReconnect() {
        EMLog.d("ChatManager", "manually force to reconnect to server");
        SessionManager.getInstance().forceReconnect();
    }

    void tryToReconnectOnGCM() {
        if(Chat.getInstance().isLoggedIn()) {
            if(PushNotificationHelper.getInstance().isUsingGCM()) {
                j.Jsalahe var1 = j.getInstance().i();
                boolean var2 = var1 != null?var1.e:true;
                if(var2) {
                    if(SessionManager.getInstance().isFinished() && SessionManager.getInstance().reuse()) {
                        this.forceReconnect();
                    }

                }
            }
        }
    }

    public boolean isSlientMessage(Message var1) {
        return var1.getBooleanAttribute("em_ignore_notification", false);
    }

    void configureCustomService(CustomerServiceConfiguration var1) {
        CustomerService.getInstance().setConfiguration(var1);
    }

    public void downloadFile(String var1, String var2, Map<String, String> var3, final CallBack var4) {
        HttpClient.getInstance().downloadFile(var1, var2, var3, new EMCloudOperationCallback() {
            public void onSuccess(String var1) {
                if(var4 != null) {
                    var4.onSuccess();
                }

            }

            public void onError(String var1) {
                if(var4 != null) {
                    var4.onError(-998, var1);
                }

            }

            public void onProgress(int var1) {
                if(var4 != null) {
                    var4.onProgress(var1, (String)null);
                }

            }
        });
    }

    public void addChatRoomChangeListener(ChatRoomChangeListener var1) {
        MultiUserChatManager.getInstance().addChatRoomChangeListener(var1);
    }

    public void removeChatRoomChangeListener(ChatRoomChangeListener var1) {
        MultiUserChatManager.getInstance().removeChatRoomChangeListener(var1);
    }

    public void joinChatRoom(String var1, ValueCallBack<ChatRoom> var2) {
        MultiUserChatManager.getInstance().joinChatRoom(var1, var2);
    }

    public void leaveChatRoom(String var1) {
        MultiUserChatManager.getInstance().leaveChatRoom(var1);
    }

    public CursorResult<ChatRoom> fetchPublicChatRoomsFromServer(int var1, String var2) throws EaseMobException {
        return MultiUserChatManager.getInstance().fetchPublicChatRoomsFromServer(var1, var2);
    }

    public ChatRoom fetchChatRoomFromServer(String var1) throws EaseMobException {
        return MultiUserChatManager.getInstance().fetchChatRoomFromServer(var1);
    }

    public ChatRoom getChatRoom(String var1) {
        return MultiUserChatManager.getInstance().getChatRoom(var1);
    }

    public List<ChatRoom> getAllChatRooms() {
        return MultiUserChatManager.getInstance().getAllChatRooms();
    }

    public List<Contact> getRobotsFromServer() throws EaseMobException {
        return ExtraService.getInstance().getRobotsFromServer();
    }

    public void setGCMProjectNumber(String var1) {
        p.getInstance().a(var1);
    }

    public void setMipushConfig(String var1, String var2) {
        p.getInstance().a(new p.a(var1, var2));
    }

    public boolean isDirectCall() {
        return VoiceCallManager.getInstance().isDirectCall();
    }

    public Map<String, KeywordSearchInfo> getKeywordInfoList(String var1) {
        return DBManager.getInstance().q(var1);
    }

    public List<Message> getMessagesByKeyword(String var1, String var2, int var3, String var4) {
        if(TextUtils.isEmpty(var1)) {
            return null;
        } else {
            Message.ChatType var5 = GroupManager.getInstance().getGroup(var4) != null? Message.ChatType.GroupChat: Message.ChatType.Chat;
            return DBManager.getInstance().a(var5, var1, var2, var3, var4);
        }
    }

    public void pauseVoiceTransfer() {
        VoiceCallManager.getInstance().pauseVoiceTransfer();
    }

    public void resumeVoiceTransfer() {
        VoiceCallManager.getInstance().resumeVoiceTransfer();
    }

    public void pauseVideoTransfer() {
        VoiceCallManager.getInstance().pauseVideoTransfer();
    }

    public void resumeVideoTransfer() {
        VoiceCallManager.getInstance().resumeVideoTransfer();
    }

    public int getVoiceInputLevel() {
        return VoiceCallManager.getInstance().getVoiceInputLevel();
    }

    public List<Message> getMessagesByMsgType(ProtocolMessage.TYPE var1, Message.ChatType var2, String var3, String var4, int var5) {
        return var3 == null?null:ConversationManager.getInstance().getMessagesByMsgType(var1, var2, var3, var4, var5);
    }

    public long getTotalMessageCountByMsgType(ProtocolMessage.TYPE var1, Message.ChatType var2, String var3) {
        return var3 == null?-1L:ConversationManager.getInstance().getTotalMessageCountByMsgType(var1, var2, var3);
    }

    private class ChatServiceConnection implements ServiceConnection {
        private ChatServiceConnection() {
        }

        public void onServiceConnected(ComponentName var1, IBinder var2) {
            ((EMChatService.LocalBinder)var2).getService();
            EMLog.d("ChatManager", "service connected");
        }

        public void onServiceDisconnected(ComponentName var1) {
            EMLog.d("ChatManager", "EaseMobService is disconnected");
            EMLog.d("ChatManager", "service disconnected");
        }
    }

    /*private class EMChatManagerListener implements org.jivesoftware.smack.ChatManagerListener {
        private EMChatManagerListener() {
        }

        public void chatCreated(org.jivesoftware.smack.Chat var1, boolean var2) {
            String var3 = var1.getParticipant();
            EMLog.d("ChatManager", "xmpp chat created for: " + var3);
            //ChatManager.this.chats.put(var3, var1);
        }
    }*/

    private class SingleInvitationListener implements PacketListener {
        private SingleInvitationListener() {
        }

        public void processPacket(Packet var1) {
            if(var1 instanceof Presence) {
                Presence var2 = (Presence)var1;
                if(Chat.getInstance().appInited) {
                    ChatManager.this.processRosterPresence(var2);
                } else {
                    EMLog.d("ChatManager", "received roster presence, but app is not ready");
                    //ChatManager.this.offlineRosterPresenceList.add(var2);
                }
            }

        }
    }

    private class XmppConnectionListener implements ChatConnectionListener {
        private XmppConnectionListener() {
        }

        public void connectionClosed() {
            EMLog.d("ChatManager", "closing connection");
            ChatManager.this.handler.post(new Runnable() {
                public void run() {
                    Iterator var2 = ChatManager.this.connectionListeners.iterator();

                    while(var2.hasNext()) {
                        ConnectionListener var1 = (ConnectionListener)var2.next();
                        if(var1 != null) {
                            var1.onDisConnected("connectionClosed");
                        }
                    }

                }
            });
            ChatManager.this.threadPool.submit(new Runnable() {
                public void run() {
                    Iterator var2 = ChatManager.this.newConnectionListeners.iterator();

                    while(var2.hasNext()) {
                        EMConnectionListener var1 = (EMConnectionListener)var2.next();
                        var1.onDisconnected(-1013);
                    }

                }
            });
        }

        public void connectionClosedOnError(final Exception var1) {
            EMLog.d("ChatManager", "connectionClosedOnError");
            ChatManager.this.handler.post(new Runnable() {
                public void run() {
                    Iterator var2 = ChatManager.this.connectionListeners.iterator();

                    while(var2.hasNext()) {
                        ConnectionListener var1x = (ConnectionListener)var2.next();
                        if(var1x != null) {
                            var1x.onDisConnected("connectionClosedOnError:" + var1.getMessage());
                        }
                    }

                }
            });
            ChatManager.this.threadPool.submit(new Runnable() {
                public void run() {
                    int var1x = -1013;
                    if(var1 != null) {
                        int var2 = ExceptionUtils.fromExceptionToErrorCode(var1);
                        if(var2 != -999) {
                            var1x = var2;
                        }
                    }

                    Iterator var3 = ChatManager.this.newConnectionListeners.iterator();

                    while(var3.hasNext()) {
                        EMConnectionListener var6 = (EMConnectionListener)var3.next();

                        try {
                            var6.onDisconnected(var1x);
                        } catch (Exception var5) {
                            var5.printStackTrace();
                        }
                    }

                }
            });
        }

        public void reconnectingIn(int var1) {
            EMLog.d("ChatManager", "reconnectingIn in " + var1);
        }

        public void reconnectionFailed(final Exception var1) {
            EMLog.d("ChatManager", "reconnectionFailed");
            ChatManager.this.handler.post(new Runnable() {
                public void run() {
                    Iterator var2 = ChatManager.this.connectionListeners.iterator();

                    while(var2.hasNext()) {
                        ConnectionListener var1x = (ConnectionListener)var2.next();
                        if(var1x != null) {
                            var1x.onDisConnected(var1.getMessage());
                        }
                    }

                }
            });
            ChatManager.this.threadPool.submit(new Runnable() {
                public void run() {
                    int var1x = -1013;
                    if(var1 != null) {
                        int var2 = ExceptionUtils.fromExceptionToErrorCode(var1);
                        if(var2 != -999) {
                            var1x = var2;
                        }
                    }

                    Iterator var3 = ChatManager.this.newConnectionListeners.iterator();

                    while(var3.hasNext()) {
                        EMConnectionListener var6 = (EMConnectionListener)var3.next();

                        try {
                            var6.onDisconnected(var1x);
                        } catch (Exception var5) {
                            var5.printStackTrace();
                        }
                    }

                }
            });
        }

        public void reconnectionSuccessful() {
            EMLog.d("ChatManager", "reconnectionSuccessful");
            ChatManager.this.onReconnectionSuccessful();
        }

        public void onConnectionSuccessful() {
            EMLog.d("ChatManager", "onConnectionSuccessful");
            ChatManager.this.notifyMessageHandlerConnectionConnected();
            ContactManager.getInstance().init(Chat.getInstance().getAppContext(), ChatManager.this.connectionManager);
            if(ChatManager.this.connectionManager != null) {
                String var1 = SessionManager.getInstance().getLastLoginUser();
                String var2 = ChatManager.this.connectionManager.getCurrentUser();
                p.getInstance().e(var2);
                if(var2 != null && (var1 == null || !var1.equals(var2))) {
                    SessionManager.getInstance().setLastLoginUser(var2);
                    SessionManager.getInstance().setLastLoginPwd(ChatManager.this.connectionManager.getCurrentPwd());
                }
            }

            ChatManager.this.handler.post(new Runnable() {
                public void run() {
                    Iterator var2 = ChatManager.this.connectionListeners.iterator();

                    while(var2.hasNext()) {
                        ConnectionListener var1 = (ConnectionListener)var2.next();
                        if(var1 != null) {
                            var1.onConnected();
                        }
                    }

                }
            });
            ChatManager.this.threadPool.submit(new Runnable() {
                public void run() {
                    Iterator var2 = ChatManager.this.newConnectionListeners.iterator();

                    while(var2.hasNext()) {
                        ConnectionListener var1 = (ConnectionListener)var2.next();
                        var1.onConnected();
                    }

                }
            });
        }

        public void onConnecting() {
            EMLog.d("ChatManager", "onConnecting...");
        }
    }
}
