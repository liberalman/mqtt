package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.util.Pair;
import com.seaofheart.app.CallBack;
import com.seaofheart.app.ValueCallBack;
import com.seaofheart.app.EMConnectionListener;
import com.seaofheart.app.analytics.TimeTag;
import com.seaofheart.app.analytics.PerformanceCollector;
import com.seaofheart.app.chat.core.Cleaner;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.chat.core.r;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.EMLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class ChatRoomManager implements r {
    private static final String TAG = "ChatRoomManager";
    Map<String, ChatRoom> allChatRooms = new ConcurrentHashMap();
    ArrayList<ChatRoomChangeListener> chatRoomChangeListeners = new ArrayList();
    ArrayList<ChatRoomManager.ChatRoomChangeEvent> offlineChatRoomEvents = new ArrayList();
    boolean allChatRoomLoaded;
    private MultiUserChatProcessor chatProcessor = null;
    private Cleaner cleaner = null;
    private boolean deadRoomLoaded = false;
    private EMConnectionListener cnnListener = null;
    private ExecutorService threadPool = null;

    ChatRoomManager() {
        this.chatProcessor = new MultiUserChatProcessor();
        this.cleaner = Cleaner.getInstance(1);
    }

    CursorResult<ChatRoom> getChatRoomsFromServer(int var1, String var2) throws EaseMobException {
        this.checkConnection();
        String var3 = p.getInstance().O() + "/chatrooms";
        Pair var4 = HttpClient.getInstance().sendRequestWithToken(var3, (String)null, HttpClient.GET);
        int var5 = ((Integer)var4.first).intValue();
        String var6 = (String)var4.second;
        if(var5 != 200 && var5 != 204) {
            throw new EaseMobException(var6);
        } else {
            try {
                JSONObject var7 = new JSONObject(var6);
                String var8 = null;
                ArrayList var9 = new ArrayList();
                CursorResult var10 = new CursorResult();
                if(var7.has("cursor")) {
                    var8 = var7.getString("cursor");
                    var10.setCursor(var8);
                }

                if(var7.has("data")) {
                    JSONArray var11 = var7.getJSONArray("data");

                    for(int var12 = 0; var12 < var11.length(); ++var12) {
                        JSONObject var13 = var11.getJSONObject(var12);
                        ChatRoom var14 = this.parseRoom(false, var13);
                        var9.add(var14);
                    }

                    var10.setCursor(var8);
                    var10.setData(var9);
                }

                return var10;
            } catch (JSONException var15) {
                var15.printStackTrace();
                throw new EaseMobException(var15.toString());
            }
        }
    }

    List<ChatRoom> getJoinedChatRooms() throws EaseMobException {
        ChatManager.getInstance().checkConnection();

        try {
            List var1 = this.retrieveChatRooms(false);
            this.syncWithServers(var1);
            return var1;
        } catch (Exception var2) {
            return null;
        }
    }

    private List<ChatRoom> retrieveChatRooms(boolean var1) throws EaseMobException {
        String var2 = p.getInstance().O() + "/users/" + ChatManager.getInstance().getCurrentUser() + "/joined_chatrooms";
        Pair var3 = HttpClient.getInstance().sendRequestWithToken(var2, (String)null, HttpClient.GET);
        int var4 = ((Integer)var3.first).intValue();
        String var5 = (String)var3.second;
        ArrayList var6 = null;
        if(var4 != 200 && var4 != 204) {
            throw new EaseMobException(var5);
        } else {
            try {
                JSONObject var7 = new JSONObject(var5);
                if(var7.has("data")) {
                    var6 = new ArrayList();
                    JSONArray var8 = var7.getJSONArray("data");

                    for(int var9 = 0; var9 < var8.length(); ++var9) {
                        JSONObject var10 = var8.getJSONObject(var9);
                        ChatRoom var11 = this.parseRoom(var1, var10);
                        var6.add(var11);
                    }
                }

                return var6;
            } catch (JSONException var12) {
                var12.printStackTrace();
                throw new EaseMobException(var12.toString());
            }
        }
    }

    private ChatRoom parseRoom(boolean var1, JSONObject var2) throws JSONException {
        ChatRoom var3 = new ChatRoom();
        MultiUserChatManager.getInstance().parseRoom(var3, var1, var2);
        return var3;
    }

    private void syncWithServers(List<ChatRoom> var1) {
        Iterator var3 = var1.iterator();

        while(var3.hasNext()) {
            ChatRoom var2 = (ChatRoom)var3.next();
            this.saveRoomInCache(var2);
            this.saveRoomInDB(var2);
        }

        Set var9 = this.allChatRooms.keySet();
        ArrayList var10 = new ArrayList();
        Iterator var5 = var9.iterator();

        String var4;
        while(var5.hasNext()) {
            var4 = (String)var5.next();
            boolean var6 = false;
            Iterator var8 = var1.iterator();

            while(var8.hasNext()) {
                ChatRoom var7 = (ChatRoom)var8.next();
                if(var7.getId().equals(var4)) {
                    var6 = true;
                    break;
                }
            }

            if(!var6) {
                var10.add(var4);
            }
        }

        var5 = var10.iterator();

        while(var5.hasNext()) {
            var4 = (String)var5.next();
            EMLog.d("ChatRoomManager", "delete local room which did not exists on server:" + var4);
            this.deleteLocalChatRoom(var4);
        }

    }

    ChatRoom getChatRoomFromServer(String var1) throws EaseMobException {
        this.checkConnection();
        return this.getChatRoomFromRest(var1, false);
    }

    public List<ChatRoom> getAllChatRooms() {
        return Collections.unmodifiableList(new ArrayList(this.allChatRooms.values()));
    }

    private Map<String, ChatRoom> loadAllChatRooms() {
        TimeTag var1 = new TimeTag();
        var1.start();
        this.allChatRooms = DBManager.getInstance().getChatRooms();
        EMLog.d("ChatRoomManager", "load all chat rooms from db. size:" + this.allChatRooms.values().size());
        if(this.allChatRooms != null && this.allChatRooms.size() > 0) {
            PerformanceCollector.collectLoadAllLocalChatRooms(this.allChatRooms.size(), var1.stop());
        }

        return this.allChatRooms;
    }

    public ChatRoom getChatRoom(String var1) {
        return (ChatRoom)this.allChatRooms.get(var1);
    }

    private ChatRoom getChatRoomFromRest(String var1, boolean var2) throws EaseMobException {
        String var3 = p.getInstance().O() + "/chatrooms/" + var1;
        Pair var4 = HttpClient.getInstance().sendRequestWithToken(var3, (String)null, HttpClient.GET, 20000);
        int var5 = ((Integer)var4.first).intValue();
        String var6 = (String)var4.second;
        if(var5 == 200 || var5 == 204) {
            try {
                JSONObject var7 = new JSONObject(var6);
                if(var7.has("data")) {
                    JSONObject var8 = var7.getJSONArray("data").getJSONObject(0);
                    ChatRoom var9 = this.parseRoom(true, var8);
                    return var9;
                }
            } catch (JSONException var10) {
                EMLog.e("ChatRoomManager", var10.getMessage());
                throw new EaseMobException(var10.toString());
            }
        }

        throw new EaseMobException(var6);
    }

    public void addChangeListener(ChatRoomChangeListener var1) {
        EMLog.d("ChatRoomManager", "add chat room change listener:" + var1.getClass().getName());
        if(!this.chatRoomChangeListeners.contains(var1)) {
            this.chatRoomChangeListeners.add(var1);
        }

    }

    void removeChangeListener(ChatRoomChangeListener var1) {
        this.chatRoomChangeListeners.remove(var1);
    }

    public void joinChatRoom(final String var1, final ValueCallBack<ChatRoom> var2) {
        this.threadPool.submit(new Runnable() {
            public void run() {
                ChatRoom var1x = null;

                try {
                    var1x = ChatRoomManager.this.joinChatRoom(var1);
                    if(var2 != null) {
                        var2.onSuccess(var1x);
                    }
                } catch (EaseMobException var3) {
                    if(var2 != null) {
                        var2.onError(var3.getErrorCode(), var3.toString());
                    }
                }

            }
        });
    }

    public ChatRoom joinChatRoom(String var1) throws EaseMobException {
        ChatManager.getInstance().checkConnection();
        ChatRoom var2 = null;

        try {
            this.cleaner.b(new LeaveRoom(var1, this));
            String var3 = ContactManager.getEidFromGroupId(var1);
            MultiUserChat var4 = this.chatProcessor.getMUC(var3, 20000L);
            if(var4 != null && !var4.isJoined()) {
                var4.join(ChatManager.getInstance().getCurrentUser(), 20000L);
            }

            var2 = new ChatRoom(var1);
            this.saveRoomInDB(var2);
            this.saveRoomInCache(var2);
        } catch (Exception var6) {
            throw new EaseMobException(var6.toString());
        }

        try {
            var2 = this.getChatRoomFromServer(var1);
            this.saveRoomInDB(var2);
            this.saveRoomInCache(var2);
        } catch (EaseMobException var5) {
            EMLog.e("ChatRoomManager", var5.getMessage());
        }

        return var2;
    }

    public void exitChatRoom(final String var1, final CallBack var2) {
        this.threadPool.submit(new Runnable() {
            public void run() {
                ChatRoomManager.this.exitChatRoom(var1);
                if(var2 != null) {
                    var2.onSuccess();
                }

            }
        });
    }

    public void exitChatRoom(String var1) {
        try {
            this.cmdExitRoom(var1);
        } catch (EaseMobException var3) {
            this.cleaner.a(new LeaveRoom(var1, this));
        }

    }

    void cmdExitRoom(String var1) throws EaseMobException {
        ChatRoom var2 = this.getChatRoom(var1);
        if(var2 == null) {
            EMLog.w("ChatRoomManager", "this room is not exist,roomid:" + var1);
        } else if(var2.getOwner().equals(ChatManager.getInstance().getCurrentUser()) && !ChatManager.getInstance().getChatOptions().isChatroomOwnerLeaveAllowed()) {
            EMLog.w("ChatRoomManager", "owner should not leave the room : " + var1);
        } else {
            EMLog.d("ChatRoomManager", "try to exit room : " + var1);
            String var3 = ContactManager.getEidFromGroupId(var1);

            try {
                ChatManager.getInstance().deleteConversation(var1, true);
                ChatManager.getInstance().checkConnection();
                this.chatProcessor.leaveMUCWithoutJoin(var3);
                EMLog.d("ChatRoomManager", "roomId : " + var1 + " was exited");
                this.deleteLocalChatRoom(var1);
            } catch (XMPPException var5) {
                EMLog.d("ChatRoomManager", "exit room : " + var1 + " with error :" + var5.toString());
                throw new EaseMobException(-998, var5.toString());
            }
        }
    }

    void handleRoomDestroy(String var1) {
        String var2 = ContactManager.getGroupIdFromEid(var1);
        ChatRoom var3 = (ChatRoom)this.allChatRooms.get(var2);
        String var4 = "";
        if(var3 != null) {
            var4 = var3.getName();
        }

        EMLog.d("ChatRoomManager", "chat room has been destroy on server:" + var2 + " name:" + var4);
        this.deleteLocalChatRoom(var2);
        Iterator var6 = this.chatRoomChangeListeners.iterator();

        while(var6.hasNext()) {
            ChatRoomChangeListener var5 = (ChatRoomChangeListener)var6.next();
            var5.onChatRoomDestroyed(var2, var4);
        }

    }

    void handleUserRemove(String var1) {
        Pair var2 = this.parseRoomJid(var1);
        if(var2 != null) {
            String var3 = (String)var2.first;
            String var4 = (String)var2.second;
            String var5 = "";
            EMLog.d("ChatRoomManager", "user " + var3 + " has been removed from chat room:" + var4);
            ChatRoom var6 = (ChatRoom)this.allChatRooms.get(var4);
            if(var6 != null) {
                var5 = var6.getName();
            }

            if(var3.equals(ChatManager.getInstance().getCurrentUser())) {
                this.deleteLocalChatRoom(var4);
            }

            ArrayList var7 = this.chatRoomChangeListeners;
            synchronized(this.chatRoomChangeListeners) {
                ChatRoom var8 = this.getChatRoom(var4);
                if(var8 != null) {
                    var8.removeMember(var3);
                    DBManager.getInstance().updateChatRoom(var8);
                }

                Iterator var10 = this.chatRoomChangeListeners.iterator();

                while(var10.hasNext()) {
                    ChatRoomChangeListener var9 = (ChatRoomChangeListener)var10.next();
                    var9.onMemberKicked(var4, var5, var3);
                }
            }
        }

    }

    void onMemberExit(String var1) {
        Pair var2 = this.parseRoomJid(var1);
        if(var2 != null) {
            String var3 = "";
            EMLog.d("ChatRoomManager", "user " + (String)var2.first + " has been removed from chat room:" + (String)var2.second);
            ChatRoom var4 = (ChatRoom)this.allChatRooms.get(var2.second);
            if(var4 != null) {
                var3 = var4.getName();
            }

            if(((String)var2.first).equals(ChatManager.getInstance().getCurrentUser())) {
                this.deleteLocalChatRoom((String)var2.second);
            } else {
                ArrayList var5 = this.chatRoomChangeListeners;
                synchronized(this.chatRoomChangeListeners) {
                    Iterator var7 = this.chatRoomChangeListeners.iterator();

                    while(var7.hasNext()) {
                        ChatRoomChangeListener var6 = (ChatRoomChangeListener)var7.next();
                        var6.onMemberExited((String)var2.second, var3, (String)var2.first);
                    }
                }
            }
        }

    }

    void onMemberJoined(String var1) {
        Pair var2 = this.parseRoomJid(var1);
        if(var2 != null) {
            EMLog.d("ChatRoomManager", "member " + (String)var2.first + " join the room : " + (String)var2.second);
            ArrayList var3 = this.chatRoomChangeListeners;
            synchronized(this.chatRoomChangeListeners) {
                ChatRoom var4 = this.getChatRoom((String)var2.second);
                if(var4 != null) {
                    var4.addMember((String)var2.first);
                    DBManager.getInstance().updateChatRoom(var4);
                }

                Iterator var6 = this.chatRoomChangeListeners.iterator();

                while(var6.hasNext()) {
                    ChatRoomChangeListener var5 = (ChatRoomChangeListener)var6.next();
                    var5.onMemberJoined((String)var2.second, (String)var2.first);
                }
            }
        }

    }

    private Pair<String, String> parseRoomJid(String var1) {
        int var2 = var1.indexOf("/");
        if(var2 > 0) {
            String var3 = var1.substring(var2 + 1);
            String var4 = var1.substring(0, var2);
            String var5 = ContactManager.getGroupIdFromEid(var4);
            return new Pair(var3, var5);
        } else {
            return null;
        }
    }

    void deleteLocalChatRoom(String var1) {
        EMLog.d("ChatRoomManager", "delete local chatroom:" + var1);
        String var2 = ContactManager.getEidFromGroupId(var1);
        this.chatProcessor.removeMuc(var2);
        DBManager.getInstance().delChatRoomMessages(var1);
        this.getAllChatRoom().remove(var1);
        ChatManager.getInstance().deleteConversation(var1, true);
    }

    MultiUserChat getMUCWithoutJoin(String var1) throws XMPPException {
        return this.chatProcessor.getMUCWithoutJoin(var1);
    }

    private ChatRoom saveRoomInCache(ChatRoom var1) {
        String var2 = var1.getId();
        ChatRoom var3 = (ChatRoom)this.getAllChatRoom().get(var2);
        if(var3 != null) {
            var3.copyModel(var1);
            return var3;
        } else {
            this.getAllChatRoom().put(var2, var1);
            return var1;
        }
    }

    private void saveRoomInDB(ChatRoom var1) {
        String var2 = var1.getId();
        ChatRoom var3 = DBManager.getInstance().getChatRoom(var2);
        if(var3 == null) {
            DBManager.getInstance().saveChatRoom(var1);
        } else {
            DBManager.getInstance().updateChatRoom(var1);
        }

    }

    Map<String, ChatRoom> getAllChatRoom() {
        return this.allChatRooms;
    }

    void checkConnection() throws EaseMobException {
        SessionManager.getInstance().checkConnection();
    }

    void onInvitation(String var1, String var2, String var3) {
        String var4 = var1;
        ChatRoom var5 = new ChatRoom(var1, var1);

        try {
            EMLog.d("ChatRoomManager", "accept chat room invitation for room:" + var4);
            ChatRoom var6 = this.joinChatRoom(var1);
            if(var6 != null && var6.getName() != null && !var6.getName().equals("")) {
                var5.copyModel(var6);
            }

            var4 = var5.getName();
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        if(Chat.getInstance().appInited) {
            Iterator var7 = this.chatRoomChangeListeners.iterator();

            while(var7.hasNext()) {
                ChatRoomChangeListener var9 = (ChatRoomChangeListener)var7.next();
                EMLog.d("ChatRoomManager", "fire chat room inviatation received event for room:" + var4);
            }
        } else {
            EMLog.d("ChatRoomManager", "aff offline group inviatation received event for group:" + var4);
            this.offlineChatRoomEvents.add(new ChatRoomManager.ChatRoomChangeEvent(ChatRoomManager.ChatRoomEventType.Invitate, var1, var4, var2, var3));
        }

    }

    void processOfflineMessages() {
        EMLog.d("ChatRoomManager", "process offline chat room event start: " + this.offlineChatRoomEvents.size());
        Iterator var2 = this.offlineChatRoomEvents.iterator();

        while(true) {
            while(var2.hasNext()) {
                ChatRoomManager.ChatRoomChangeEvent var1 = (ChatRoomManager.ChatRoomChangeEvent)var2.next();
                switch(var1.type.ordinal()) { // $SWITCH_TABLE$com$easemob$chat$ChatRoomManager$ChatRoomEventType()[var1.type.ordinal()]
                    case 1:
                        Iterator var4 = this.chatRoomChangeListeners.iterator();

                        while(var4.hasNext()) {
                            ChatRoomChangeListener var3 = (ChatRoomChangeListener)var4.next();
                            EMLog.d("ChatRoomManager", "fire chatroom inviatation received event for chatroom:" + var1.roomName + " listener:" + var3.hashCode());
                        }
                }
            }

            this.offlineChatRoomEvents.clear();
            EMLog.d("ChatRoomManager", "proess offline group event finish");
            return;
        }
    }

    public void onInit() {
        this.threadPool = Executors.newFixedThreadPool(1);
        this.chatProcessor.onInit();
        this.cleaner.onInit();
        if(!ChatManager.getInstance().getChatOptions().isAutomaticallyLeaveChatroomDisabledOnLogin()) {
            this.loadAllChatRooms();
        }

        if(this.cnnListener == null) {
            this.cnnListener = new EMConnectionListener() {
                public void onConnected() {
                    if(!ChatManager.getInstance().getChatOptions().isAutomaticallyLeaveChatroomDisabledOnLogin()) {
                        if(!ChatRoomManager.this.deadRoomLoaded) {
                            List var1 = ChatRoomManager.this.getAllChatRooms();
                            if(var1.size() > 0) {
                                ArrayList var2 = new ArrayList();
                                Iterator var4 = var1.iterator();

                                while(var4.hasNext()) {
                                    ChatRoom var3 = (ChatRoom)var4.next();
                                    var2.add(new LeaveRoom(var3.getId(), ChatRoomManager.this));
                                }

                                ChatRoomManager.this.cleaner.a(var2);
                            }

                            ChatRoomManager.this.deadRoomLoaded = true;
                        }
                    }
                }

                public void onDisconnected(int var1) {
                }
            };
        }

        ChatManager.getInstance().addConnectionListener(this.cnnListener);
    }

    public void onDestroy() {
        this.allChatRoomLoaded = false;
        this.deadRoomLoaded = false;
        if(this.allChatRooms != null) {
            this.allChatRooms.clear();
        }

        if(this.offlineChatRoomEvents != null) {
            this.offlineChatRoomEvents.clear();
        }

        EMLog.d("ChatRoomManager", "init chat room manager");
        this.chatProcessor.onDestroy();
        this.cleaner.onDestroy();
    }

    private class ChatRoomChangeEvent {
        String roomId;
        String roomName;
        String inviterUserName;
        String reason;
        ChatRoomManager.ChatRoomEventType type;

        public ChatRoomChangeEvent(ChatRoomManager.ChatRoomEventType var2, String var3, String var4, String var5, String var6) {
            this.type = var2;
            this.roomId = var3;
            this.roomName = var4;
            this.inviterUserName = var5;
            this.reason = var6;
        }
    }

    private static enum ChatRoomEventType {
        Invitate;

        private ChatRoomEventType() {
        }
    }
}

