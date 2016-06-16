package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.CallBack;
import com.seaofheart.app.ValueCallBack;
import com.seaofheart.app.chat.core.r;
import com.seaofheart.app.chat.core.x;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.EMLog;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.MUCUser;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

final class MultiUserChatManager implements r {
    private static final String TAG = "MultiUserChatManager";
    private static final String MUC_ELEMENT_NAME = "x";
    public static final String MUC_NS_USER = "http://jabber.org/protocol/muc#user";
    private static MultiUserChatManager instance = null;
    private MultiUserChatManager.MUCInvitationListener invitationListener = null;
    private ChatRoomManager chatRoomManager = null;
    private GroupManager groupChatManager = null;

    private MultiUserChatManager() {
        this.chatRoomManager = new ChatRoomManager();
        this.groupChatManager = GroupManager.getInstance();
    }

    public static synchronized MultiUserChatManager getInstance() {
        if(instance == null) {
            instance = new MultiUserChatManager();
        }

        return instance;
    }

    public void loadLocalData() {
        this.groupChatManager.loadAllGroups();
    }

    public void addChatRoomChangeListener(ChatRoomChangeListener var1) {
        this.chatRoomManager.addChangeListener(var1);
    }

    public void removeChatRoomChangeListener(ChatRoomChangeListener var1) {
        this.chatRoomManager.removeChangeListener(var1);
    }

    void joinChatRoom(String var1) throws EaseMobException {
        this.chatRoomManager.joinChatRoom(var1);
    }

    public void joinChatRoom(String var1, ValueCallBack<ChatRoom> var2) {
        this.chatRoomManager.joinChatRoom(var1, var2);
    }

    void leaveChatRoom(String var1) {
        this.chatRoomManager.exitChatRoom(var1, (CallBack)null);
    }

    List<ChatRoom> fetchJoinedChatRoomsFromServer() throws EaseMobException {
        return this.chatRoomManager.getJoinedChatRooms();
    }

    public CursorResult<ChatRoom> fetchPublicChatRoomsFromServer(int var1, String var2) throws EaseMobException {
        return this.chatRoomManager.getChatRoomsFromServer(var1, var2);
    }

    public ChatRoom fetchChatRoomFromServer(String var1) throws EaseMobException {
        return this.chatRoomManager.getChatRoomFromServer(var1);
    }

    public ChatRoom getChatRoom(String var1) {
        return this.chatRoomManager.getChatRoom(var1);
    }

    public List<ChatRoom> getAllChatRooms() {
        return new ArrayList(this.chatRoomManager.getAllChatRoom().values());
    }

    public void addGroupChangeListener(GroupChangeListener var1) {
        this.groupChatManager.addGroupChangeListener(var1);
    }

    public void removeGroupChangeListener(GroupChangeListener var1) {
        this.groupChatManager.removeGroupChangeListener(var1);
    }

    public void joinGroup(String var1) throws EaseMobException {
        this.groupChatManager.joinGroup(var1);
    }

    public void leaveGroup(String var1) throws EaseMobException {
        this.groupChatManager.exitFromGroup(var1);
    }

    public void dismissGroup(String var1) throws EaseMobException {
        this.groupChatManager.exitAndDeleteGroup(var1);
    }

    public List<Group> fetchJoinedGroupsFromServer() throws EaseMobException {
        return this.groupChatManager.getGroupsFromServer();
    }

    public Group fetchGroupFromServer(String var1) throws EaseMobException {
        return this.groupChatManager.getGroupFromServer(var1);
    }

    public CursorResult<GroupInfo> fetchPublicGroupsFromServer(int var1, String var2) throws EaseMobException {
        return this.groupChatManager.getPublicGroupsFromServer(var1, var2);
    }

    public Group createPrivateGroup(String var1, String var2, String[] var3, boolean var4, int var5) throws EaseMobException {
        return this.groupChatManager.createPrivateGroup(var1, var2, var3, var4, var5);
    }

    public Group createPublicGroup(String var1, String var2, String[] var3, boolean var4, int var5) throws EaseMobException {
        return this.groupChatManager.createPublicGroup(var1, var2, var3, var4, var5);
    }

    public List<Group> getAllGroups() {
        return this.groupChatManager.getAllGroups();
    }

    public Group getGroup(String var1) {
        return this.groupChatManager.getGroup(var1);
    }

    public void addUsersToGroup(String var1, String[] var2) throws EaseMobException {
        this.groupChatManager.addUsersToGroup(var1, var2);
    }

    public void removeUserFromGroup(String var1, String var2) throws EaseMobException {
        this.groupChatManager.removeUserFromGroup(var1, var2);
    }

    public Group createOrUpdateLocalGroup(Group var1) {
        return this.groupChatManager.createOrUpdateLocalGroup(var1);
    }

    public void changeGroupName(String var1, String var2) throws EaseMobException {
        this.groupChatManager.changeGroupName(var1, var2);
    }

    public void acceptInvitation(String var1) throws EaseMobException {
        this.groupChatManager.acceptInvitation(var1);
    }

    public void acceptApplication(String var1, String var2) throws EaseMobException {
        this.groupChatManager.acceptApplication(var1, var2);
    }

    public void declineApplication(String var1, String var2, String var3) throws EaseMobException {
        this.groupChatManager.declineApplication(var1, var2, var3);
    }

    public void setAutoAcceptInvitation(boolean var1) {
        this.groupChatManager.setAutoAcceptInvitation(var1);
    }

    public void inviteUser(String var1, String[] var2, String var3) throws EaseMobException {
        this.groupChatManager.inviteUser(var1, var2, var3);
    }

    public void applyJoinToGroup(String var1, String var2) throws EaseMobException {
        this.groupChatManager.applyJoinToGroup(var1, var2);
    }

    public void blockGroupMessage(String var1) throws EaseMobException {
        this.groupChatManager.blockGroupMessage(var1);
    }

    public void unblockGroupMessage(String var1) throws EaseMobException {
        this.groupChatManager.unblockGroupMessage(var1);
    }

    public void blockUser(String var1, String var2) throws EaseMobException {
        this.groupChatManager.blockUser(var1, var2);
    }

    public void unblockUser(String var1, String var2) throws EaseMobException {
        this.groupChatManager.unblockUser(var1, var2);
    }

    public List<String> getBlockedUsers(String var1) throws EaseMobException {
        return this.groupChatManager.getBlockedUsers(var1);
    }

    MultiUserChatRoomModelBase getRoom(String var1) {
        Object var2 = this.chatRoomManager.getChatRoom(var1);
        if(var2 == null) {
            var2 = this.groupChatManager.getGroup(var1);
        }

        return (MultiUserChatRoomModelBase)var2;
    }

    MultiUserChat getMUCWithoutJoin(String var1, Message.ChatType var2) throws XMPPException {
        return var2 == Message.ChatType.ChatRoom?this.chatRoomManager.getMUCWithoutJoin(var1):(var2 == Message.ChatType.GroupChat?this.groupChatManager.getMUCWithoutJoin(var1):null);
    }

    x getRoomTypeExtension(Packet var1) {
        x var2 = null;

        try {
            var2 = (x)var1.getExtension("roomtype", "easemob:x:roomtype");
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return var2;
    }

    void clearRooms() {
        this.groupChatManager.clear();
    }

    public void onInit() {
        EMLog.d("MultiUserChatManager", "init MultiUserChatManager");
        //this.invitationListener = new MultiUserChatManager.MUCInvitationListener((MultiUserChatManager.MUCInvitationListener)null);
        this.invitationListener = new MultiUserChatManager.MUCInvitationListener();
        //MultiUserChat.addInvitationListener(SessionManager.getInstance().getConnection(), this.invitationListener);
        AndFilter var1 = new AndFilter(new PacketFilter[]{new MessageTypeFilter(org.jivesoftware.smack.packet.Message.Type.notify), new PacketExtensionFilter("x", "http://jabber.org/protocol/muc#user")});
        AndFilter var2 = new AndFilter(new PacketFilter[]{new PacketTypeFilter(Presence.class), new PacketExtensionFilter("roomtype", "easemob:x:roomtype")});
        OrFilter var3 = new OrFilter(var1, var2);
        //MultiUserChatManager.EMMUCPresenceListener var4 = new MultiUserChatManager.EMMUCPresenceListener((MultiUserChatManager.EMMUCPresenceListener)null);
        MultiUserChatManager.EMMUCPresenceListener var4 = new MultiUserChatManager.EMMUCPresenceListener();
        //SessionManager.getInstance().getConnection().addPacketListener(var4, var3);
        this.chatRoomManager.onInit();
        this.groupChatManager.onInit();
    }

    public void onDestroy() {
        /*if(this.invitationListener != null && SessionManager.getInstance().getConnection() != null) {
            try {
                MultiUserChat.removeInvitationListener(SessionManager.getInstance().getConnection(), this.invitationListener);
            } catch (Exception var2) {
                ;
            }
        }*/

        this.chatRoomManager.onDestroy();
        this.groupChatManager.onDestroy();
    }

    public void onAppReady() {
        this.chatRoomManager.processOfflineMessages();
        this.groupChatManager.processOfflineMessages();
    }

    void parseRoom(MultiUserChatRoomModelBase var1, boolean var2, JSONObject var3) throws JSONException {
        RestResultParser.parseRoom(var1, var2, var3);
    }

    private class MUCInvitationListener implements InvitationListener {
        private MUCInvitationListener() {
        }

        public void invitationReceived(Connection var1, String var2, String var3, String var4, String var5, org.jivesoftware.smack.packet.Message var6) {
            EMLog.d("MultiUserChatManager", "invitation received room:" + var2 + " inviter:" + var3 + " reason:" + var4 + " message:" + var6.getBody());
            String var7 = ContactManager.getUserNameFromEid(var3);
            String var8 = ContactManager.getGroupIdFromEid(var2);
            x var9 = MultiUserChatManager.this.getRoomTypeExtension(var6);
            if(var9 != null && var9.a() == x.a.a) {
                MultiUserChatManager.this.chatRoomManager.onInvitation(var8, var7, var4);
            } else {
                MultiUserChatManager.this.groupChatManager.onInvitation(var8, var7, var4);
            }

        }
    }

    private class EMMUCPresenceListener implements PacketListener {
        private static final String ITEM_DESTROY = "destroy";
        private static final String ITEM_EXITMUC = "<item affiliation=\"none\" role=\"none\">";
        private static final String AFFILIATION_NONE = "affiliation=\"none\"";
        private static final String ROLE_NONE = "role=\"none\"";
        private static final String ROLE_PARTICIPANT = "role=\"participant\"";

        private EMMUCPresenceListener() {
        }

        public synchronized void processPacket(Packet var1) {
            try {
                MUCUser var3 = (MUCUser)var1.getExtension("x", "http://jabber.org/protocol/muc#user");
                if(var3 != null) {
                    String var4 = var3.toXML();
                    boolean var5 = false;
                    x var6 = MultiUserChatManager.this.getRoomTypeExtension(var1);
                    if(var6 != null && var6.a() == x.a.a) {
                        var5 = true;
                    }

                    if(!var5 && var1 instanceof Presence) {
                        return;
                    }

                    String var7;
                    if(var3.getStatus() != null) {
                        var7 = var3.getStatus().getCode();
                        if(var7 != null && var7.contains("307")) {
                            if(!var5) {
                                MultiUserChatManager.this.groupChatManager.handleUserRemove(var1.getFrom());
                            } else {
                                MultiUserChatManager.this.chatRoomManager.handleUserRemove(var1.getFrom());
                            }

                            return;
                        }
                    }

                    if((!(var1 instanceof Presence) || ((Presence)var1).getType() == org.jivesoftware.smack.packet.Presence.Type.unavailable) && (!(var1 instanceof org.jivesoftware.smack.packet.Message) || ((org.jivesoftware.smack.packet.Message)var1).getPresenceType() == org.jivesoftware.smack.packet.Message.PresenceType.unavailable)) {
                        if(var3.getStatus() != null) {
                            var7 = var3.getStatus().getCode();
                            if(var7 != null && var7.contains("110")) {
                                if(var5) {
                                    MultiUserChatManager.this.chatRoomManager.onMemberExit(var1.getFrom());
                                }

                                return;
                            }
                        }

                        if(var4.contains("destroy")) {
                            var7 = var1.getFrom();
                            if(!var5) {
                                String var8 = ContactManager.getGroupIdFromEid(var7);
                                if(MultiUserChatManager.this.chatRoomManager.getChatRoom(var8) != null) {
                                    MultiUserChatManager.this.chatRoomManager.handleRoomDestroy(var7);
                                } else {
                                    MultiUserChatManager.this.groupChatManager.handleRoomDestroy(var7);
                                }
                            } else {
                                MultiUserChatManager.this.chatRoomManager.handleRoomDestroy(var7);
                            }
                        } else if(var4.contains("affiliation=\"none\"") && var4.contains("role=\"none\"")) {
                            if(var5) {
                                MultiUserChatManager.this.chatRoomManager.onMemberExit(var1.getFrom());
                            } else {
                                MultiUserChatManager.this.groupChatManager.handleUserRemove(var1.getFrom());
                            }
                        } else if(var4.contains("role=\"none\"") && var5) {
                            MultiUserChatManager.this.chatRoomManager.onMemberExit(var1.getFrom());
                        }
                    } else if(var4.contains("role=\"participant\"") && var5) {
                        MultiUserChatManager.this.chatRoomManager.onMemberJoined(var1.getFrom());
                    }
                }
            } catch (Exception var9) {
                var9.printStackTrace();
            }

        }
    }
}

