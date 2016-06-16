package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import com.seaofheart.app.ValueCallBack;
import com.seaofheart.app.analytics.PerformanceCollector;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.exceptions.ExceptionUtils;
import com.seaofheart.app.exceptions.PermissionException;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.DateUtils;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.CallBack;
import com.seaofheart.app.analytics.TimeTag;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.packet.MUCUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupManager {
    private static String TAG = "group";
    public static final String MUC_NS_USER = "http://jabber.org/protocol/muc#user";
    private static final String PERMISSION_ERROR_ADD = "only group owner can add member";
    private static final String PERMISSION_ERROR_REMOVE = "only group owner can remove member";
    private static final String PERMISSION_ERROR_DELETE = "only group owner can delete group";
    private static final String PERMISSION_ERROR = "only group owner has this permission";
    private static final int DEFAULT_MAX_USERS = 200;
    Map<String, Group> allGroups = new Hashtable();
    private static GroupManager instance = new GroupManager();
    private Context appContext;
    ArrayList<GroupChangeListener> groupChangeListeners = new ArrayList();
    boolean autoAcceptInvitation = true;
    ArrayList<GroupManager.GroupChangeEvent> offlineGroupEvents = new ArrayList();
    private final GroupManager.MucApplyListener applyListener = new GroupManager.MucApplyListener();
    private boolean allGroupLoaded = false;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    XMPPConnection connection = null;
    private MultiUserChatProcessor chatProcessor = null;
    private Object mutex = new Object();
    private boolean receivedQuery = false;

    private GroupManager() {
        this.chatProcessor = new MultiUserChatProcessor();
    }

    public static GroupManager getInstance() {
        return instance;
    }

    private synchronized void syncLoadGroups(CallBack var1) {
        if(this.allGroupLoaded) {
            if(var1 != null) {
                var1.onSuccess();
            }

        } else {
            this.loadGroups();
        }
    }

    public void loadAllGroups(final CallBack var1) {
        (new Thread() {
            public void run() {
                GroupManager.this.syncLoadGroups(var1);
            }
        }).start();
    }

    public List<Group> getAllGroups() {
        return Collections.unmodifiableList(new ArrayList(this.allGroups.values()));
    }

    public Group getGroup(String var1) {
        return (Group)this.allGroups.get(var1);
    }

    public void loadAllGroups() {
        this.syncLoadGroups((CallBack)null);
    }

    private void loadGroups() {
        TimeTag var1 = new TimeTag();
        var1.start();
        this.allGroups = DBManager.getInstance().getGroups();
        EMLog.d(TAG, "load all groups from db. size:" + this.allGroups.values().size());
        this.allGroupLoaded = true;
        if(this.allGroups != null && this.allGroups.size() > 0) {
            PerformanceCollector.collectLoadAllLocalGroups(this.allGroups.size(), var1.stop());
        }

    }

    public void joinGroupsAfterLogin() {
        Thread var1 = new Thread() {
            public void run() {
                List var1 = GroupManager.this.getAllGroups();
                EMLog.d(GroupManager.TAG, "join groups. size:" + var1.size());
                Iterator var3 = var1.iterator();

                while(var3.hasNext()) {
                    Group var2 = (Group)var3.next();
                    String var4 = ContactManager.getEidFromGroupId(var2.getGroupId());

                    try {
                        GroupManager.this.chatProcessor.getMUC(var4);
                    } catch (Exception var6) {
                        var6.printStackTrace();
                    }
                }

                EMLog.d(GroupManager.TAG, "join groups thread finished.");
            }
        };
        var1.start();
    }

    private String generateGroupId() {
        return DateUtils.getTimestampStr();
    }

    public Group createPublicGroup(String var1, String var2, String[] var3, boolean var4) throws EaseMobException {
        return this.createPublicGroup(var1, var2, var3, var4, 200);
    }

    private String[] filterOwnerFromMembers(String var1, String[] var2) throws EaseMobException {
        if(var2 != null && var2.length >= 1) {
            int var3 = 0;

            int var4;
            for(var4 = 0; var4 < var2.length; ++var4) {
                if(var2[var4].equals(var1)) {
                    ++var3;
                }

                if(var2[var4] == null || var2[var4].equals("")) {
                    throw new EaseMobException("Your added a null number, Please add valid members!");
                }
            }

            if(var3 == 0) {
                return var2;
            } else {
                var4 = var2.length - var3;
                if(var4 == 0) {
                    throw new EaseMobException("Please add members who should not be the owner!");
                } else {
                    String[] var5 = new String[var4];
                    int var6 = 0;

                    for(int var7 = 0; var6 < var2.length; ++var6) {
                        if(!var2[var6].equals(var1)) {
                            var5[var7] = var2[var6];
                            ++var7;
                        }
                    }

                    return var5;
                }
            }
        } else {
            return var2;
        }
    }

    public Group createPublicGroup(String var1, String var2, String[] var3, boolean var4, int var5) throws EaseMobException {
        String var6 = this.generateGroupId();
        String var7 = ChatManager.getInstance().getCurrentUser();
        String var8 = ContactManager.getEidFromGroupId(var6);
        String[] var9 = this.filterOwnerFromMembers(var7, var3);
        if(var9 == null && var5 < 1) {
            throw new EaseMobException(-1018, "the max group members are reached!");
        } else if(var9 != null && var9.length >= var5) {
            throw new EaseMobException(-1018, "the max group members are reached!");
        } else {
            try {
                this.createPublicXmppMUC(var8, var1, var2, var7, var4, var5);
                if(var9 != null) {
                    String[] var13 = var9;
                    int var12 = var9.length;

                    for(int var11 = 0; var11 < var12; ++var11) {
                        String var10 = var13[var11];
                        String var14 = ContactManager.getEidFromUserName(var10);
                        this.addUserToMUC(var8, var14, true);
                    }
                }

                Group var17 = new Group(var6);
                var17.setGroupName(var1);
                var17.setDescription(var2);
                var17.setOwner(ChatManager.getInstance().getCurrentUser());
                var17.setPublic(true);
                var17.setMaxUsers(var5);
                ArrayList var19 = new ArrayList();
                var19.add(var17.getOwner());
                if(var9 != null) {
                    String[] var15 = var9;
                    int var21 = var9.length;

                    for(int var20 = 0; var20 < var21; ++var20) {
                        String var18 = var15[var20];
                        var19.add(var18);
                    }
                }

                var17.setMembers(var19);
                var17.setAffiliationsCount(var19.size());
                DBManager.getInstance().saveGroup(var17);
                this.allGroups.put(var17.getGroupId(), var17);
                return var17;
            } catch (Exception var16) {
                var16.printStackTrace();
                throw new EaseMobException(var16.toString());
            }
        }
    }

    public Group createPrivateGroup(String var1, String var2, String[] var3) throws EaseMobException {
        return this.createPrivateGroup(var1, var2, var3, false);
    }

    public Group createPrivateGroup(String var1, String var2, String[] var3, boolean var4) throws EaseMobException {
        return this.createPrivateGroup(var1, var2, var3, var4, 200);
    }

    public Group createPrivateGroup(String var1, String var2, String[] var3, boolean var4, int var5) throws EaseMobException {
        String var6 = this.generateGroupId();
        String var7 = ChatManager.getInstance().getCurrentUser();
        String var8 = ContactManager.getEidFromGroupId(var6);
        String[] var9 = this.filterOwnerFromMembers(var7, var3);
        if(var9 == null && var5 < 1) {
            throw new EaseMobException(-1018, "the max group members are reached!");
        } else if(var9 != null && var9.length >= var5) {
            throw new EaseMobException(-1018, "the max group members are reached!");
        } else {
            try {
                this.createPrivateXmppMUC(var8, var1, var2, var7, var4, var5);
                if(var9 != null) {
                    String[] var13 = var9;
                    int var12 = var9.length;

                    for(int var11 = 0; var11 < var12; ++var11) {
                        String var10 = var13[var11];
                        String var14 = ContactManager.getEidFromUserName(var10);
                        this.addUserToMUC(var8, var14, true);
                    }
                }

                Group var17 = new Group(var6);
                var17.setGroupName(var1);
                var17.setDescription(var2);
                var17.setMaxUsers(var5);
                var17.setOwner(ChatManager.getInstance().getCurrentUser());
                ArrayList var19 = new ArrayList();
                var19.add(var17.getOwner());
                if(var9 != null) {
                    String[] var15 = var9;
                    int var21 = var9.length;

                    for(int var20 = 0; var20 < var21; ++var20) {
                        String var18 = var15[var20];
                        var19.add(var18);
                    }
                }

                var17.setMembers(var19);
                var17.setAffiliationsCount(var19.size());
                DBManager.getInstance().saveGroup(var17);
                this.getAllGroup().put(var17.getGroupId(), var17);
                return var17;
            } catch (Exception var16) {
                var16.printStackTrace();
                throw new EaseMobException(var16.toString());
            }
        }
    }

    Map<String, Group> getAllGroup() {
        return this.allGroups;
    }

    public Group createGroup(String var1, String var2, String[] var3) throws EaseMobException {
        return this.createPrivateGroup(var1, var2, var3);
    }

    private void createPrivateXmppMUC(String var1, String var2, String var3, String var4, boolean var5, int var6) throws Exception {
        MultiUserChat var7 = new MultiUserChat(this.connection, var1);
        EMLog.d(TAG, "create muc room jid:" + var1 + " roomName:" + var2 + " owner:" + var4 + " allowInvites:" + var5);

        try {
            var7.create(var4);
            Form var8 = var7.getConfigurationForm();
            Form var9 = var8.createAnswerForm();
            Iterator var10 = var8.getFields();

            while(var10.hasNext()) {
                FormField var11 = (FormField)var10.next();
                if(!"hidden".equals(var11.getType()) && var11.getVariable() != null) {
                    var9.setDefaultAnswer(var11.getVariable());
                }
            }

            var9.setAnswer("muc#roomconfig_persistentroom", true);
            var9.setAnswer("muc#roomconfig_membersonly", true);
            var9.setAnswer("muc#roomconfig_moderatedroom", true);
            if(var6 > 0) {
                var9.setAnswer("muc#roomconfig_maxusers", var6);
            }

            var9.setAnswer("muc#roomconfig_publicroom", false);
            var9.setAnswer("members_by_default", true);
            var9.setAnswer("muc#roomconfig_allowinvites", var5);
            var9.setAnswer("muc#roomconfig_roomname", var2);
            var9.setAnswer("muc#roomconfig_roomdesc", var3);
            var7.sendConfigurationForm(var9);
            var7.join(var4);
            this.chatProcessor.addMuc(var1, var7);
            EMLog.d(TAG, "muc created:" + var7.getRoom());
        } catch (XMPPException var12) {
            var12.printStackTrace();
            throw var12;
        }
    }

    private void createPublicXmppMUC(String var1, String var2, String var3, String var4, boolean var5, int var6) throws Exception {
        MultiUserChat var7 = new MultiUserChat(this.connection, var1);
        EMLog.d(TAG, "create muc room jid:" + var1 + " roomName:" + var2 + " owner:" + var4);

        try {
            var7.create(var4);
            Form var8 = var7.getConfigurationForm();
            Form var9 = var8.createAnswerForm();
            Iterator var10 = var8.getFields();

            while(var10.hasNext()) {
                FormField var11 = (FormField)var10.next();
                if(!"hidden".equals(var11.getType()) && var11.getVariable() != null) {
                    var9.setDefaultAnswer(var11.getVariable());
                }
            }

            var9.setAnswer("muc#roomconfig_persistentroom", true);
            var9.setAnswer("muc#roomconfig_moderatedroom", false);
            var9.setAnswer("muc#roomconfig_publicroom", true);
            if(var5) {
                var9.setAnswer("muc#roomconfig_membersonly", true);
            } else {
                var9.setAnswer("muc#roomconfig_membersonly", false);
            }

            var9.setAnswer("members_by_default", true);
            var9.setAnswer("muc#roomconfig_roomname", var2);
            var9.setAnswer("muc#roomconfig_roomdesc", var3);
            if(var6 > 0) {
                var9.setAnswer("muc#roomconfig_maxusers", var6);
            }

            var7.sendConfigurationForm(var9);
            var7.join(var4);
            this.chatProcessor.addMuc(var1, var7);
            EMLog.d(TAG, "muc created:" + var7.getRoom());
        } catch (XMPPException var12) {
            var12.printStackTrace();
            throw var12;
        }
    }

    public void exitAndDeleteGroup(String var1) throws EaseMobException {
        Group var2 = (Group)this.getAllGroup().get(var1);
        if(var2 == null) {
            throw new EaseMobException(-1016, "group doesn\'t exist in local:" + var1);
        } else {
            this.checkGroupOwner(var2, "only group owner can delete group");

            try {
                String var3 = ContactManager.getEidFromGroupId(var1);
                this.chatProcessor.deleteMUC(var3);
                Group var4 = (Group)this.getAllGroup().get(var1);
                if(var4 != null) {
                    this.deleteLocalGroup(var1);
                }

            } catch (Exception var5) {
                var5.printStackTrace();
                throw new EaseMobException(var5.toString());
            }
        }
    }

    public void deleteLocalGroup(String gruopId) {
        EMLog.d(TAG, "delete local group:" + gruopId);
        DBManager.getInstance().delGroup(gruopId);
        this.removeGroupFromCache(gruopId);
    }

    private void removeGroupFromCache(String var1) {
        String var2 = ContactManager.getEidFromGroupId(var1);
        this.chatProcessor.removeMuc(var2);
        this.getAllGroup().remove(var1);
        if(ChatManager.getInstance().getChatOptions().isDeleteMessagesAsExitGroup()) {
            ChatManager.getInstance().deleteConversation(var1, true, true);
        }

    }

    private void checkGroupOwner(Group var1, String var2) throws PermissionException {
        String var3 = var1.getOwner();
        String var4 = ChatManager.getInstance().getCurrentUser();
        if(var3 == null || !var4.equals(var3)) {
            throw new PermissionException(-1020, var2);
        }
    }

    public void addUsersToGroup(String var1, String[] var2) throws EaseMobException {
        Group var3 = (Group)this.getAllGroup().get(var1);
        if(var3 == null) {
            throw new EaseMobException(-1016, "group doesn\'t exist in local:" + var1);
        } else {
            this.checkGroupOwner(var3, "only group owner can add member");

            try {
                String var4 = ContactManager.getEidFromGroupId(var1);
                Group var5 = this.getMUC(var4, ChatManager.getInstance().getCurrentUser(), false, true);
                int var6 = var5.getAffiliationsCount();
                int var7 = var5.getMaxUsers();
                if(var6 >= var7) {
                    throw new EaseMobException(-1018, "the max group members are reached!");
                } else if(var7 - var6 < var2.length) {
                    throw new EaseMobException(-1019, "there is no room to add new members");
                } else {
                    String[] var11 = var2;
                    int var10 = var2.length;

                    String var8;
                    int var9;
                    for(var9 = 0; var9 < var10; ++var9) {
                        var8 = var11[var9];
                        String var12 = ContactManager.getEidFromUserName(var8);
                        this.addUserToMUC(var4, var12, true);
                    }

                    var11 = var2;
                    var10 = var2.length;

                    for(var9 = 0; var9 < var10; ++var9) {
                        var8 = var11[var9];
                        if(!var3.getMembers().contains(var8)) {
                            var3.addMember(var8);
                        }
                    }

                    var3.setAffiliationsCount(var3.getMembers().size());
                    DBManager.getInstance().updateGroup(var3);
                }
            } catch (Exception var13) {
                var13.printStackTrace();
                if(var13 instanceof EaseMobException) {
                    throw (EaseMobException)var13;
                } else {
                    throw new EaseMobException(-1, var13.getMessage());
                }
            }
        }
    }

    private void addUserToMUC(String var1, String var2, boolean var3) throws XMPPException {
        EMLog.d(TAG, "muc add user:" + var2 + " to chat room:" + var1);
        MultiUserChat var4 = this.chatProcessor.getMUC(var1);
        if(var3) {
            var4.invite(var2, "EaseMob-Group");
        }

        var4.grantMembership(var2);
    }

    public void removeUserFromGroup(String var1, String var2) throws EaseMobException {
        Group var3 = (Group)this.getAllGroup().get(var1);
        if(var3 == null) {
            throw new EaseMobException(-1016, "group doesn\'t exist in local:" + var1);
        } else {
            this.checkGroupOwner(var3, "only group owner can remove member");
            String var4 = ContactManager.getEidFromGroupId(var1);
            String var5 = ContactManager.getEidFromUserName(var2);

            try {
                this.removeUserFromMUC(var4, var5);
                var3.removeMember(var2);
                var3.setAffiliationsCount(var3.getMembers().size());
                DBManager.getInstance().updateGroup(var3);
            } catch (Exception var7) {
                var7.printStackTrace();
            }

        }
    }

    private void removeUserFromMUC(String var1, String var2) throws Exception {
        EMLog.d(TAG, "muc remove user:" + var2 + " from chat room:" + var1);
        MultiUserChat var3 = this.chatProcessor.getMUC(var1);
        var3.revokeMembership(var2);
        String var4 = ContactManager.getUserNameFromEid(var2);

        try {
            EMLog.d(TAG, "try to kick user if already joined");
            var3.kickParticipant(var4, "RemoveFromGroup");
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    public void exitFromGroup(String var1) throws EaseMobException {
        try {
            String var2 = ContactManager.getEidFromGroupId(var1);
            String var3 = ChatManager.getInstance().getCurrentUser();
            String var4 = ContactManager.getEidFromUserName(var3);
            this.chatProcessor.leaveMUCRemoveMember(var2, var4);
            this.deleteLocalGroup(var1);
        } catch (Exception var5) {
            var5.printStackTrace();
            throw new EaseMobException(var5.toString());
        }
    }

    public Group getGroupFromServer(String var1) throws EaseMobException {
        if(var1 == null) {
            throw new EaseMobException(-1017, "group id is null");
        } else {
            this.checkConnection();
            Group var2 = this.getGroupFromRestServer(var1, true);
            return var2;
        }
    }

    private Group getGroupFromRestServer(String var1, boolean var2) throws EaseMobException {
        String var3 = p.getInstance().O() + "/chatgroups/" + var1 + "?version=v2";
        if(var2) {
            var3 = var3 + "&needmembers=true";
        }

        TimeTag var4 = new TimeTag();
        var4.start();
        Pair var5 = HttpClient.getInstance().sendRequestWithToken(var3, (String)null, HttpClient.GET);
        int var6 = ((Integer)var5.first).intValue();
        String var7 = (String)var5.second;
        if(var6 != 200 && var6 != 204) {
            if(var6 != 400 && var6 != 404) {
                throw new EaseMobException(var7);
            } else {
                throw new EaseMobException(-1017, "no group on server with groupid: " + var1);
            }
        } else {
            try {
                JSONObject var8 = new JSONObject(var7);
                if(var8.has("data")) {
                    JSONObject var9 = var8.getJSONArray("data").getJSONObject(0);
                    if(var9.has("error")) {
                        String var10 = var9.getString("error");
                        if("group id doesn\'t exist".equals(var10)) {
                            throw new EaseMobException(-1017, "no group on server with groupid: " + var1);
                        }
                    }

                    Group var12 = this.parseGroupFromData(true, var9);
                    PerformanceCollector.collectRetrieveGroupFromServer(var12, var4.stop());
                    return var12;
                } else {
                    return null;
                }
            } catch (JSONException var11) {
                var11.printStackTrace();
                throw new EaseMobException(var11.toString());
            }
        }
    }

    public Group createOrUpdateLocalGroup(Group group) {
        Group var2 = DBManager.getInstance().getGroup(group.getGroupId());
        if(var2 == null) {
            DBManager.getInstance().saveGroup(group);
        } else {
            DBManager.getInstance().updateGroup(group);
        }

        return this.updateCache(group);
    }

    private Group updateCache(Group var1) {
        Group var2 = (Group)this.getAllGroup().get(var1.getGroupId());
        if(var2 != null) {
            var2.copyGroup(var1);
            return var2;
        } else {
            this.getAllGroup().put(var1.getGroupId(), var1);
            return var1;
        }
    }

    Group getMUC(String var1, String var2, boolean var3, boolean var4) throws XMPPException {
        RoomInfo var5 = MultiUserChat.getRoomInfo(this.connection, var1);
        if(var5 == null) {
            return null;
        } else {
            String var6 = var5.getRoomName();
            String var7 = var5.getDescription();
            String var8 = ContactManager.getUserNameFromEid(var1);
            Group var9 = new Group(var8);
            var9.setGroupName(var6);
            var9.setDescription(var7);
            var9.membersOnly = var5.isMembersOnly();
            var9.isPublic = var5.isPublic();
            var9.allowInvites = var5.isAllowInvites();
            var9.maxUsers = var5.getMaxUsers();
            var9.affiliationsCount = var5.getAffiliationsCount();
            if(var5.getOwner() != null) {
                var9.owner = ContactManager.getGroupIdFromEid(var5.getOwner());
            }

            EMLog.d(TAG, "get room info for roomjid:" + var1 + " name:" + var6 + " desc:" + var7 + "owner:" + var5.getOwner() + " ispublic:" + var9.isPublic() + " ismemberonly:" + var9.isMembersOnly() + " isallowinvites:" + var9.isAllowInvites() + " maxusers:" + var9.maxUsers + " affCounts:" + var9.affiliationsCount + " isjoin:" + var4 + " owner:" + var9.owner);
            MultiUserChat var10 = this.chatProcessor.getMUCWithoutJoin(var1);
            if(var4) {
                var10.join(var2);
            }

            if(!var3) {
                return var9;
            } else {
                try {
                    Collection var11 = var10.getOwners();
                    Iterator var13 = var11.iterator();
                    String var15;
                    if(var13.hasNext()) {
                        Affiliate var12 = (Affiliate)var13.next();
                        String var14 = var12.getJid();
                        var15 = ContactManager.getUserNameFromEid(var14);
                        var9.setOwner(var15);
                        EMLog.d(TAG, " room owner:" + var15);
                    }

                    var9.addMember(var9.getOwner());
                    Collection var21 = var10.getMembers();
                    Iterator var24 = var21.iterator();

                    String var16;
                    while(var24.hasNext()) {
                        Affiliate var23 = (Affiliate)var24.next();
                        var15 = var23.getJid();
                        var16 = ContactManager.getUserNameFromEid(var15);
                        var9.addMember(var16);
                        EMLog.d(TAG, "  room member:" + var16);
                    }

                    try {
                        Collection var22 = var10.getAdmins();

                        String var17;
                        for(Iterator var26 = var22.iterator(); var26.hasNext(); EMLog.d(TAG, "  room blockedmsg member:" + var17)) {
                            Affiliate var25 = (Affiliate)var26.next();
                            var16 = var25.getJid();
                            var17 = ContactManager.getUserNameFromEid(var16);
                            var9.addMember(var17);
                            String var18 = ChatManager.getInstance().getCurrentUser();
                            if(var17.equals(var18)) {
                                EMLog.d(TAG, " this room is blocked group msg:" + var8);
                                var9.isMsgBlocked = true;
                            }
                        }
                    } catch (Exception var19) {
                        EMLog.d(TAG, "error when retrieve blocked members:" + var19.toString());
                    }

                    return var9;
                } catch (Exception var20) {
                    var20.printStackTrace();
                    EMLog.d(TAG, "error when retrieve group info from server:" + var20.toString());
                    this.chatProcessor.removeMuc(var1);
                    return null;
                }
            }
        }
    }

    public synchronized List<Group> getGroupsFromServer() throws EaseMobException {
        try {
            this.checkConnection();
            TimeTag var1 = new TimeTag();
            var1.start();
            List var2 = this.getGroupsFromRestServer(true);
            var1.stop();
            PerformanceCollector.collectRetrieveGroupsFromServerTime(var2.size(), var1.timeSpent());
            var1.start();
            this.syncGroupsWithRemoteGroupList(var2);
            var1.stop();
            PerformanceCollector.collectSyncWithServerGroups(var2.size(), var1.timeSpent());
            return var2;
        } catch (Exception var3) {
            var3.printStackTrace();
            if(var3 instanceof EaseMobException) {
                throw (EaseMobException)var3;
            } else {
                throw new EaseMobException(var3.toString());
            }
        }
    }

    public synchronized List<Group> getJoinedGroupsFromServer() throws EaseMobException {
        return this.getGroupsFromServer();
    }

    private void checkConnection() throws EaseMobException {
        SessionManager.getInstance().checkConnection();
    }

    private List<Group> getGroupsFromRestServer(boolean var1) throws EaseMobException {
        String var2 = p.getInstance().O() + "/users/" + ChatManager.getInstance().getCurrentUser() + "/joined_chatgroups";
        if(var1) {
            var2 = var2 + "?detail=true";
        }

        ArrayList var3 = new ArrayList();
        Pair var4 = HttpClient.getInstance().sendRequestWithToken(var2, (String)null, HttpClient.GET);
        int var5 = ((Integer)var4.first).intValue();
        String var6 = (String)var4.second;
        if(var5 != 200 && var5 != 204) {
            throw new EaseMobException(var6);
        } else {
            try {
                JSONObject var7 = new JSONObject(var6);
                if(var7.has("data")) {
                    JSONArray var8 = var7.getJSONArray("data");

                    for(int var9 = 0; var9 < var8.length(); ++var9) {
                        JSONObject var10 = var8.getJSONObject(var9);
                        Group var11 = this.parseGroupFromData(var1, var10);
                        var3.add(var11);
                    }
                }

                return var3;
            } catch (JSONException var12) {
                var12.printStackTrace();
                throw new EaseMobException(var12.toString());
            }
        }
    }

    private Group parseGroupFromData(boolean var1, JSONObject var2) throws JSONException {
        String var3 = var2.getString("groupid");
        String var4 = var2.getString("groupname");
        Group var5 = new Group(var3);
        var5.setGroupName(var4);
        if(var1) {
            if(var2.has("owner")) {
                var5.setOwner(var2.getString("owner"));
            }

            if(var2.has("membersonly")) {
                var5.membersOnly = var2.getBoolean("membersonly");
            }

            if(var2.has("allowinvites")) {
                var5.allowInvites = var2.getBoolean("allowinvites");
            }

            if(var2.has("public")) {
                var5.isPublic = var2.getBoolean("public");
            }

            if(var2.has("description")) {
                var5.description = var2.getString("description");
            }

            if(var2.has("maxusers")) {
                var5.maxUsers = var2.getInt("maxusers");
            }

            if(var2.has("shieldgroup")) {
                var5.isMsgBlocked = var2.getBoolean("shieldgroup");
            }

            if(var2.has("affiliations_count")) {
                var5.affiliationsCount = var2.getInt("affiliations_count");
            }

            if(var2.has("member")) {
                ArrayList var6 = new ArrayList();
                JSONArray var7 = var2.getJSONArray("member");

                for(int var8 = 0; var8 < var7.length(); ++var8) {
                    String var9 = var7.getString(var8);
                    if(var9.equals(var5.getOwner())) {
                        var6.add(0, var9);
                    } else {
                        var6.add(var9);
                    }
                }

                var5.setMembers(var6);
            }
        }

        return var5;
    }

    public void asyncGetGroupsFromServer(final ValueCallBack<List<Group>> var1) {
        if(var1 == null) {
            throw new RuntimeException("callback is null!");
        } else {
            this.threadPool.submit(new Runnable() {
                public void run() {
                    try {
                        List var1x = GroupManager.this.getGroupsFromServer();
                        var1.onSuccess(var1x);
                    } catch (EaseMobException var3) {
                        var3.printStackTrace();
                        int var2 = ExceptionUtils.fromExceptionToErrorCode(var3);
                        if(var2 == -999) {
                            var2 = -998;
                        }

                        var1.onError(var2, var3.getMessage());
                    }

                }
            });
        }
    }

    public List<GroupInfo> getAllPublicGroupsFromServer() throws EaseMobException {
        try {
            String var1 = ContactManager.getEidFromUserName(ChatManager.getInstance().getCurrentUser());
            List var2 = this.getPublicMUCs(var1, ChatConfig.getInstance().APPKEY);
            return var2;
        } catch (Exception var3) {
            var3.printStackTrace();
            throw new EaseMobException(var3.toString());
        }
    }

    public CursorResult<GroupInfo> getPublicGroupsFromServer(int var1, String var2) throws EaseMobException {
        this.checkConnection();
        return this.getPublicGroupsFromRest(var1, var2);
    }

    private CursorResult<GroupInfo> getPublicGroupsFromRest(int var1, String var2) throws EaseMobException {
        String var3 = p.getInstance().O() + "/publicchatgroups" + "?limit=" + var1;
        if(var2 != null) {
            var3 = var3 + "&cursor=" + var2;
        }

        CursorResult var4 = new CursorResult();
        ArrayList var5 = new ArrayList();
        Pair var6 = HttpClient.getInstance().sendRequestWithToken(var3, (String)null, HttpClient.GET);
        int var7 = ((Integer)var6.first).intValue();
        String var8 = (String)var6.second;
        if(var7 != 200 && var7 != 204) {
            throw new EaseMobException(var8);
        } else {
            try {
                JSONObject var9 = new JSONObject(var8);
                String var10 = null;
                if(var9.has("cursor")) {
                    var10 = var9.getString("cursor");
                    var4.setCursor(var10);
                }

                if(var9.has("data")) {
                    JSONArray var11 = var9.getJSONArray("data");

                    for(int var12 = 0; var12 < var11.length(); ++var12) {
                        JSONObject var13 = var11.getJSONObject(var12);
                        String var14 = var13.getString("groupid");
                        String var15 = var13.getString("groupname");
                        GroupInfo var16 = new GroupInfo(var14, var15);
                        var5.add(var16);
                    }

                    var4.setData(var5);
                }

                return var4;
            } catch (JSONException var17) {
                var17.printStackTrace();
                throw new EaseMobException(var17.toString());
            }
        }
    }

    public void asyncGetAllPublicGroupsFromServer(final ValueCallBack<List<GroupInfo>> var1) {
        if(var1 == null) {
            throw new RuntimeException("callback is null!");
        } else {
            this.threadPool.submit(new Runnable() {
                public void run() {
                    try {
                        List var1x = GroupManager.this.getAllPublicGroupsFromServer();
                        var1.onSuccess(var1x);
                    } catch (EaseMobException var3) {
                        var3.printStackTrace();
                        int var2 = ExceptionUtils.fromExceptionToErrorCode(var3);
                        if(var2 == -999) {
                            var2 = -998;
                        }

                        var1.onError(var2, var3.getMessage());
                    }

                }
            });
        }
    }

    private void syncGroupsWithRemoteGroupList(List<Group> var1) {
        Group var2;
        for(Iterator var3 = var1.iterator(); var3.hasNext(); this.updateCache(var2)) {
            var2 = (Group)var3.next();
            if(this.getAllGroup().containsKey(var2.getGroupId())) {
                EMLog.d(TAG, " group sync. local already exists:" + var2.getGroupId());
            }
        }

        DBManager.getInstance().a(var1);
        Set var9 = this.getAllGroup().keySet();
        ArrayList var10 = new ArrayList();
        Iterator var5 = var9.iterator();

        String var4;
        while(var5.hasNext()) {
            var4 = (String)var5.next();
            boolean var6 = false;
            Iterator var8 = var1.iterator();

            while(var8.hasNext()) {
                Group var7 = (Group)var8.next();
                if(var7.getGroupId().equals(var4)) {
                    var6 = true;
                    break;
                }
            }

            if(!var6) {
                var10.add(var4);
            }
        }

        EMLog.d(TAG, "delete local groups which not exists on server:" + var10);
        DBManager.getInstance().b(var10);
        var5 = var10.iterator();

        while(var5.hasNext()) {
            var4 = (String)var5.next();
            this.removeGroupFromCache(var4);
        }

    }

    void batchRemoveGroups(List<String> var1) {
    }

    List<Group> getMUCs(String var1, boolean var2) throws EaseMobException, XMPPException {
        EMLog.d(TAG, "needJoin : " + var2);
        TimeTag var3 = new TimeTag();
        var3.start();
        ArrayList var4 = new ArrayList();
        Collection var5 = MultiUserChat.getHostedRooms(this.connection, ChatConfig.MUC_DOMAIN);
        EMLog.d(TAG, "joined room size:" + var5.size());
        Iterator var7 = var5.iterator();

        while(var7.hasNext()) {
            HostedRoom var6 = (HostedRoom)var7.next();
            EMLog.d(TAG, "joined room room jid:" + var6.getJid() + " name:" + var6.getName());
            String var8 = ChatManager.getInstance().getCurrentUser();

            try {
                Group var9 = this.getMUC(var6.getJid(), var8, false, var2);
                if(var9 != null) {
                    EMLog.d(TAG, "  get group detail:" + var9.getGroupName());
                    var4.add(var9);
                }
            } catch (Exception var10) {
                var10.printStackTrace();
            }
        }

        EMLog.d(TAG, " retrieved groups from server:" + var4.size());
        if(var4 != null && var4.size() > 0) {
            PerformanceCollector.collectRetrieveGroupsFromServerTime(var4.size(), var3.stop());
        }

        return var4;
    }

    private List<GroupInfo> getPublicMUCs(String var1, String var2) throws EaseMobException, XMPPException {
        ArrayList var3 = new ArrayList();
        Collection var4 = MultiUserChat.getPublicRooms(this.connection, ChatConfig.MUC_DOMAIN, var2);
        EMLog.d(TAG, "public room size:" + var4.size());
        Iterator var6 = var4.iterator();

        while(var6.hasNext()) {
            HostedRoom var5 = (HostedRoom)var6.next();
            String var7 = this.formatGroupName(var5.getName());
            EMLog.d(TAG, "joined room room jid:" + var5.getJid() + " name:" + var7);
            GroupInfo var8 = new GroupInfo(ContactManager.getGroupIdFromEid(var5.getJid()), var7);
            var3.add(var8);
        }

        EMLog.d(TAG, " retrieved public groups from server:" + var3.size());
        return var3;
    }

    private String formatGroupName(String var1) {
        int var2 = var1.lastIndexOf(" ");
        return var1.endsWith(")") && var2 > 0?var1.substring(0, var2):var1;
    }

    synchronized void clear() {
        this.allGroupLoaded = false;
        EMLog.d(TAG, "group manager clear");
        if(this.allGroups != null) {
            this.allGroups.clear();
        }

    }

    void removeMucs() {
    }

    private synchronized void retrieveUserMucsOnServer(String var1) throws Exception {
        GroupManager.MUCSearchIQ var2 = new GroupManager.MUCSearchIQ(var1, var1);
        PacketTypeFilter var3 = new PacketTypeFilter(IQ.class) {
            public boolean accept(Packet var1) {
                if(var1 instanceof IQ) {
                    IQ var2 = (IQ)var1;
                    if(var2.getType().equals(IQ.Type.RESULT)) {
                        String var3 = var2.getChildElementXML();
                        EMLog.e(GroupManager.TAG, "childXML:" + var3);
                        Exception var4 = new Exception();
                        var4.printStackTrace();
                        return true;
                    }
                }

                return false;
            }
        };
        //GroupManager.SearchPacketListener var4 = new GroupManager.SearchPacketListener((GroupManager.SearchPacketListener)null);
        GroupManager.SearchPacketListener var4 = new GroupManager.SearchPacketListener();
        this.connection.addPacketListener(var4, var3);
        this.receivedQuery = true;
        this.connection.sendPacket(var2);
        Object var5 = this.mutex;
        synchronized(this.mutex) {
            this.mutex.wait(10000L);
        }

        if(!this.receivedQuery) {
            EMLog.e(TAG, "server no response for group search");
            throw new EaseMobException("server timeout");
        }
    }

    public void joinGroup(String var1) throws EaseMobException {
        try {
            EMLog.d(TAG, "try to joinPublicGroup, current user:" + ChatManager.getInstance().getCurrentUser() + " groupId:" + var1);
            String var2 = ContactManager.getEidFromGroupId(var1);
            Group var3 = this.getGroupFromServer(var1);
            this.createOrUpdateLocalGroup(var3);
            MultiUserChat var4 = this.chatProcessor.getMUC(var2);
            if(var4 != null && !var4.isJoined()) {
                var4.join(ChatManager.getInstance().getCurrentUser());
            }

            String var5 = ChatManager.getInstance().getCurrentUser();
            String var6 = ContactManager.getEidFromUserName(var5);
        } catch (Exception var7) {
            var7.printStackTrace();
            throw new EaseMobException(var7.toString());
        }
    }

    public void changeGroupName(String var1, String var2) throws EaseMobException {
        String var3 = ContactManager.getEidFromGroupId(var1);
        Group var4 = (Group)this.allGroups.get(var1);
        if(var4 == null) {
            throw new EaseMobException(-1016, "group doesn\'t exist in local:" + var1);
        } else {
            this.checkGroupOwner(var4, "only group owner has this permission");

            try {
                MultiUserChat var5 = this.chatProcessor.getMUC(var3);
                Form var6 = var5.getConfigurationForm();
                Form var7 = var6.createAnswerForm();
                Iterator var8 = var6.getFields();

                while(var8.hasNext()) {
                    FormField var9 = (FormField)var8.next();
                    if(!"hidden".equals(var9.getType()) && var9.getVariable() != null) {
                        var7.setDefaultAnswer(var9.getVariable());
                    }
                }

                var7.setAnswer("muc#roomconfig_roomname", var2);
                var5.sendConfigurationForm(var7);
                if(var5 != null && !var5.isJoined()) {
                    var5.join(ChatManager.getInstance().getCurrentUser());
                }

                var4.setGroupName(var2);
                DBManager.getInstance().updateGroup(var4);
            } catch (XMPPException var10) {
                throw new EaseMobException(var10.getMessage());
            }
        }
    }

    public void addGroupChangeListener(GroupChangeListener var1) {
        EMLog.d(TAG, "add group change listener:" + var1.getClass().getName());
        if(!this.groupChangeListeners.contains(var1)) {
            this.groupChangeListeners.add(var1);
        }

    }

    public void removeGroupChangeListener(GroupChangeListener var1) {
        EMLog.d(TAG, "remove group change listener:" + var1.getClass().getName());
        this.groupChangeListeners.remove(var1);
    }

    public Group acceptInvitation(String var1) throws EaseMobException {
        try {
            String var2 = ContactManager.getEidFromGroupId(var1);
            MultiUserChat var3 = this.chatProcessor.getMUCWithoutJoin(var2);

            try {
                var3.join(ChatManager.getInstance().getCurrentUser());
                EMLog.d(TAG, "join muc when acceptInvitation()");
            } catch (XMPPException var5) {
                var5.printStackTrace();
                if(var5.getXMPPError().getCode() == 407) {
                    throw var5;
                }
            }

            Group var4 = this.getGroupFromRestServer(var1, false);
            this.createOrUpdateLocalGroup(var4);
            return var4;
        } catch (Exception var6) {
            var6.printStackTrace();
            throw new EaseMobException(var6.toString());
        }
    }

    void declineInvitation(String var1, String var2, String var3) {
        EMLog.d(TAG, "decline invitation:" + var1 + " inviter:" + var2 + " reason" + var3);

        try {
            String var4 = ContactManager.getEidFromGroupId(var1);
            String var5 = ContactManager.getEidFromUserName(var2);
            MultiUserChat.decline(this.connection, var4, var5, var3);
            this.deleteLocalGroup(var1);
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    public void acceptApplication(String var1, String var2) throws EaseMobException {
        Group var3 = (Group)this.allGroups.get(var2);
        if(var3 == null) {
            throw new EaseMobException("group doesn\'t exist:" + var2);
        } else {
            try {
                String var4 = ContactManager.getEidFromGroupId(var2);
                String var5 = ContactManager.getEidFromUserName(var1);
                this.addUserToMUC(var4, var5, false);
                var3.addMember(var1);
                this.accept(var4, var3.getGroupName(), var5);
                DBManager.getInstance().updateGroup(var3);
            } catch (Exception var6) {
                var6.printStackTrace();
                throw new EaseMobException(var6.getMessage());
            }
        }
    }

    public void declineApplication(String var1, String var2, String var3) throws EaseMobException {
        try {
            String var4 = ContactManager.getEidFromGroupId(var2);
            String var5 = ContactManager.getEidFromUserName(var1);
            Group var6 = (Group)this.allGroups.get(var2);
            if(var6 == null) {
                throw new EaseMobException(-1016, "group doesn\'t exist in local:" + var2);
            } else {
                this.decline(var4, var6.getGroupName(), var5, var3);
            }
        } catch (Exception var7) {
            throw new EaseMobException(var7.getMessage());
        }
    }

    public void setAutoAcceptInvitation(boolean var1) {
        this.autoAcceptInvitation = var1;
    }

    public void inviteUser(String var1, String[] var2, String var3) throws EaseMobException {
        try {
            EMLog.d(TAG, "invite usernames:" + var2 + " to group:" + var1 + " reason:" + var3);
            if(var3 == null) {
                var3 = "";
            }

            String var4 = ContactManager.getEidFromGroupId(var1);
            ArrayList var5 = new ArrayList();
            String[] var9 = var2;
            int var8 = var2.length;

            int var7;
            for(var7 = 0; var7 < var8; ++var7) {
                String var6 = var9[var7];
                String var10 = ContactManager.getEidFromUserName(var6);
                var5.add(var10);
            }

            Group var12 = this.getMUC(var4, ChatManager.getInstance().getCurrentUser(), false, true);
            var7 = var12.getAffiliationsCount();
            var8 = var12.getMaxUsers();
            if(var7 == var8) {
                throw new EaseMobException(-1018, "群成员数已满");
            } else if(var8 - var7 < var5.size()) {
                throw new EaseMobException(-1019, "要加入的用户人数超过剩余可加入的人数");
            } else {
                this.inviteUserMUC(var4, var5, var3);
            }
        } catch (Exception var11) {
            var11.printStackTrace();
            if(var11 instanceof EaseMobException) {
                throw (EaseMobException)var11;
            } else {
                throw new EaseMobException(-1, var11.getMessage());
            }
        }
    }

    private void inviteUserMUC(String var1, List<String> var2, String var3) throws XMPPException {
        MultiUserChat var4 = this.chatProcessor.getMUC(var1);
        Group var5 = getInstance().getGroup(ContactManager.getGroupIdFromEid(var1));
        if(var2 != null && var2.size() != 0) {
            Iterator var7 = var2.iterator();

            while(var7.hasNext()) {
                String var6 = (String)var7.next();
                var4.invite(var6, var3);
                if(var5.isAllowInvites()) {
                    var5.addMember(ContactManager.getUserNameFromEid(var6));
                }
            }
        }

        var5.setAffiliationsCount(var5.getMembers().size());
    }

    public void applyJoinToGroup(String var1, String var2) throws EaseMobException {
        String var3 = ContactManager.getEidFromGroupId(var1);
        String var4 = ChatManager.getInstance().getCurrentUser();
        String var5 = ContactManager.getEidFromUserName(var4);
        this.apply(new org.jivesoftware.smack.packet.Message(), var3, var5, var2);
    }

    public void blockGroupMessage(String var1) throws EaseMobException {
        EMLog.d(TAG, "try to block group msg:" + var1);
        String var2 = ContactManager.getEidFromGroupId(var1);

        try {
            MultiUserChat var3 = this.chatProcessor.getMUC(var2);
            String var4 = ChatManager.getInstance().getCurrentUser();
            Group var5 = this.getGroup(var1);
            if(var5 == null) {
                throw new EaseMobException(-1016, "group not exist in local");
            } else if(var5.getOwner().equals(var4)) {
                throw new PermissionException(-1020, "group owner can not block group messages");
            } else {
                String var6 = ContactManager.getEidFromUserName(var4);
                var3.grantAdmin(var6);
                EMLog.d(TAG, "block group msg done:" + var1);
                var5.setMsgBlocked(true);
                DBManager.getInstance().updateGroup(var5);
            }
        } catch (Exception var7) {
            var7.printStackTrace();
            throw new EaseMobException(var7.toString());
        }
    }

    public void unblockGroupMessage(String var1) throws EaseMobException {
        EMLog.d(TAG, "try to unblock group msg:" + var1);
        String var2 = ContactManager.getEidFromGroupId(var1);

        try {
            MultiUserChat var3 = this.chatProcessor.getMUC(var2);
            String var4 = ChatManager.getInstance().getCurrentUser();
            String var5 = ContactManager.getEidFromUserName(var4);
            Group var6 = this.getGroup(var1);
            if(var6 == null) {
                throw new EaseMobException(-1016, "group not exist in local");
            } else {
                var3.grantMembership(var5);
                var6.setMsgBlocked(false);
                DBManager.getInstance().updateGroup(var6);
                EMLog.d(TAG, "block group msg done:" + var1);
            }
        } catch (Exception var7) {
            var7.printStackTrace();
            throw new EaseMobException(var7.toString());
        }
    }

    public void blockUser(String var1, String var2) throws EaseMobException {
        EMLog.d(TAG, "block user for groupid:" + var1 + " username:" + var2);
        Group var3 = (Group)this.allGroups.get(var1);
        if(var3 == null) {
            throw new EaseMobException(-1016, "group doesn\'t exist in local:" + var1);
        } else {
            this.checkGroupOwner(var3, "only group owner has this permission");

            try {
                String var4 = ContactManager.getEidFromGroupId(var1);
                String var5 = ContactManager.getEidFromUserName(var2);
                this.blockUserMuc(var4, var5);
                Group var6 = this.getGroupFromServer(var1);
                this.createOrUpdateLocalGroup(var6);
            } catch (Exception var7) {
                var7.printStackTrace();
                throw new EaseMobException(var7.toString());
            }
        }
    }

    private void blockUserMuc(String var1, String var2) throws XMPPException {
        MultiUserChat var3 = this.chatProcessor.getMUCWithoutJoin(var1);

        try {
            var3.kickParticipant(var2, "block");
        } catch (Exception var5) {
            ;
        }

        var3.banUser(var2, "easemob-block");
    }

    public void unblockUser(String var1, String var2) throws EaseMobException {
        EMLog.d(TAG, "unblock user groupid:" + var1 + " username:" + var2);
        Group var3 = (Group)this.allGroups.get(var1);
        if(var3 == null) {
            throw new EaseMobException(-1016, "group doesn\'t exist in local:" + var1);
        } else {
            this.checkGroupOwner(var3, "only group owner has this permission");

            try {
                String var4 = ContactManager.getEidFromGroupId(var1);
                String var5 = ContactManager.getEidFromUserName(var2);
                this.unblockUserMuc(var4, var5);
            } catch (Exception var6) {
                var6.printStackTrace();
                throw new EaseMobException(var6.toString());
            }
        }
    }

    private void unblockUserMuc(String var1, String var2) throws XMPPException {
        MultiUserChat var3 = this.chatProcessor.getMUCWithoutJoin(var1);
        var3.revokeMembership(var2);
    }

    public List<String> getBlockedUsers(String var1) throws EaseMobException {
        EMLog.d(TAG, "get blocked users for group:" + var1);

        try {
            String var2 = ContactManager.getEidFromGroupId(var1);
            return this.getBlockedUsersMuc(var2);
        } catch (Exception var3) {
            var3.printStackTrace();
            throw new EaseMobException(var3.toString());
        }
    }

    private List<String> getBlockedUsersMuc(String var1) throws XMPPException {
        ArrayList var2 = new ArrayList();
        MultiUserChat var3 = this.chatProcessor.getMUCWithoutJoin(var1);
        Collection var4 = var3.getOutcasts();
        Iterator var6 = var4.iterator();

        while(var6.hasNext()) {
            Affiliate var5 = (Affiliate)var6.next();
            var5.getJid();
        }

        try {
            Collection var12 = var3.getOutcasts();
            Iterator var7 = var12.iterator();

            while(var7.hasNext()) {
                Affiliate var13 = (Affiliate)var7.next();
                EMLog.d(TAG, "blocked  member jid:" + var13.getJid());
                String var8 = var13.getJid();
                String var9 = ContactManager.getUserNameFromEid(var8);
                var2.add(var9);
            }
        } catch (XMPPException var10) {
            throw var10;
        } catch (Exception var11) {
            var11.printStackTrace();
        }

        return var2;
    }

    void onInvitation(String var1, String var2, String var3) {
        String var4 = var1;
        Group var5 = new Group(var1);
        var5.setGroupName(var1);
        this.createOrUpdateLocalGroup(var5);
        if(this.autoAcceptInvitation) {
            try {
                EMLog.d(TAG, "auto accept group invitation for group:" + var4);
                Group var6 = this.acceptInvitation(var1);
                if(var6 != null && !TextUtils.isEmpty(var6.getName())) {
                    var4 = var6.getName();
                }
            } catch (Exception var8) {
                var8.printStackTrace();
                if(var8.getMessage().contains("407")) {
                    return;
                }
            }
        }

        if(Chat.getInstance().appInited) {
            Iterator var7 = this.groupChangeListeners.iterator();

            while(var7.hasNext()) {
                GroupChangeListener var9 = (GroupChangeListener)var7.next();
                EMLog.d(TAG, "fire group inviatation received event for group:" + var4);
                var9.onInvitationReceived(var1, var4, var2, var3);
            }
        } else {
            EMLog.d(TAG, "aff offline group inviatation received event for group:" + var4);
            this.offlineGroupEvents.add(new GroupManager.GroupChangeEvent(GroupManager.GroupEventType.Invitate, var1, var4, var2, var3));
        }

    }

    void processOfflineMessages() {
        EMLog.d(TAG, "process offline group event start: " + this.offlineGroupEvents.size());
        Iterator var2 = this.offlineGroupEvents.iterator();

        while(true) {
            label55:
            while(var2.hasNext()) {
                GroupManager.GroupChangeEvent var1 = (GroupManager.GroupChangeEvent)var2.next();
                GroupChangeListener var3;
                Iterator var4;
                switch(var1.type.ordinal()) { // $SWITCH_TABLE$com$easemob$chat$GroupManager$GroupEventType()[var1.type.ordinal()]
                    case 1:
                        var4 = this.groupChangeListeners.iterator();

                        for(; var4.hasNext(); var3.onInvitationReceived(var1.groupId, var1.groupName, var1.inviterUserName, var1.reason)) {
                            var3 = (GroupChangeListener)var4.next();
                            EMLog.d(TAG, "fire group inviatation received event for group:" + var1.groupName + " listener:" + var3.hashCode());
                            Group var10 = (Group)this.allGroups.get(var1.groupId);
                            if(var10 == null || !TextUtils.isEmpty(var10.getName())) {
                                Group var11 = new Group(var1.groupId);
                                var11.setGroupName(var1.groupName);
                                this.createOrUpdateLocalGroup(var11);
                            }
                        }
                        break;
                    case 2:
                        var4 = this.groupChangeListeners.iterator();

                        while(true) {
                            if(!var4.hasNext()) {
                                continue label55;
                            }

                            var3 = (GroupChangeListener)var4.next();
                            EMLog.d(TAG, "fire group application received event for group:" + var1.groupName + " listener:" + var3.hashCode());
                            var3.onApplicationReceived(var1.groupId, var1.groupName, var1.inviterUserName, var1.reason);
                        }
                    case 3:
                        try {
                            String var8 = ContactManager.getEidFromGroupId(var1.groupId);
                            Group var9 = this.getMUC(var8, ChatManager.getInstance().getCurrentUser(), false, true);
                            this.createOrUpdateLocalGroup(var9);
                            Iterator var6 = this.groupChangeListeners.iterator();

                            while(true) {
                                if(!var6.hasNext()) {
                                    continue label55;
                                }

                                GroupChangeListener var5 = (GroupChangeListener)var6.next();
                                EMLog.d(TAG, "fire group application accept received event for group:" + var1.groupName + " listener:" + var5.hashCode());
                                var5.onApplicationAccept(var1.groupId, var1.groupName, var1.inviterUserName);
                            }
                        } catch (XMPPException var7) {
                            var7.printStackTrace();
                            break;
                        }
                    case 4:
                        var4 = this.groupChangeListeners.iterator();

                        while(var4.hasNext()) {
                            var3 = (GroupChangeListener)var4.next();
                            EMLog.d(TAG, "fire group application declind received event for group:" + var1.groupName + " listener:" + var3.hashCode());
                            var3.onApplicationDeclined(var1.groupId, var1.groupName, var1.inviterUserName, var1.reason);
                        }
                }
            }

            this.offlineGroupEvents.clear();
            EMLog.d(TAG, "proess offline group event finish");
            return;
        }
    }

    MultiUserChat getMUCWithoutJoin(String var1) throws XMPPException {
        return this.chatProcessor.getMUCWithoutJoin(var1);
    }

    void handleRoomDestroy(String var1) {
        String var2 = ContactManager.getGroupIdFromEid(var1);
        Group var3 = (Group)this.allGroups.get(var2);
        String var4 = "";
        if(var3 != null) {
            var4 = var3.getGroupName();
        }

        EMLog.d(TAG, "group has been destroy on server:" + var2 + " name:" + var4);
        this.deleteLocalGroup(var2);
        Iterator var6 = this.groupChangeListeners.iterator();

        while(var6.hasNext()) {
            GroupChangeListener var5 = (GroupChangeListener)var6.next();
            var5.onGroupDestroy(var2, var4);
        }

    }

    void handleUserRemove(String var1) {
        int var2 = var1.indexOf("/");
        String var4 = null;
        if(var2 > 0) {
            var4 = var1.substring(var2 + 1);
            String var3 = var1.substring(0, var2);
            String var5 = ContactManager.getGroupIdFromEid(var3);
            if(var4.equals(ChatManager.getInstance().getCurrentUser())) {
                EMLog.d(TAG, "user " + var4 + " has been removed from group:" + var5);
                String var6 = "";
                Group var7 = (Group)this.allGroups.get(var5);
                if(var7 != null) {
                    var6 = var7.getGroupName();
                }

                this.deleteLocalGroup(var5);
                Iterator var9 = this.groupChangeListeners.iterator();

                while(var9.hasNext()) {
                    GroupChangeListener var8 = (GroupChangeListener)var9.next();
                    var8.onUserRemoved(var5, var6);
                }
            }
        }

    }

    private void apply(org.jivesoftware.smack.packet.Message var1, String var2, String var3, String var4) throws EaseMobException {
        try {
            Group var5 = (Group)this.getAllGroup().get(ContactManager.getGroupIdFromEid(var2));
            if(var5 == null) {
                var5 = this.getGroupFromRestServer(ContactManager.getGroupIdFromEid(var2), false);
            }

            var1.setTo(ContactManager.getEidFromUserName(var5.getOwner()));
            MUCUser var6 = new MUCUser();
            MUCUser.Apply var7 = new MUCUser.Apply();
            var7.setFrom(var3);
            var7.setTo(var2);
            var7.setReason(var4);
            var7.setToNick(var5.getGroupName());
            var6.setApply(var7);
            var1.addExtension(var6);
            this.connection.sendPacket(var1);
        } catch (Exception var8) {
            throw new EaseMobException(var8.getMessage());
        }
    }

    private void decline(String var1, String var2, String var3, String var4) {
        org.jivesoftware.smack.packet.Message var5 = new org.jivesoftware.smack.packet.Message(var3);
        MUCUser var6 = new MUCUser();
        MUCUser.Decline var7 = new MUCUser.Decline();
        var7.setTo(var1);
        var7.setFrom(ContactManager.getEidFromUserName(ChatManager.getInstance().getCurrentUser()));
        var7.setFromNick(var2);
        var7.setReason(var4);
        var7.setType(MUCUser.MucType.Apply);
        var6.setDecline(var7);
        var5.addExtension(var6);
        this.connection.sendPacket(var5);
    }

    private void accept(String var1, String var2, String var3) {
        org.jivesoftware.smack.packet.Message var4 = new org.jivesoftware.smack.packet.Message(var3);
        MUCUser var5 = new MUCUser();
        MUCUser.Accept var6 = new MUCUser.Accept();
        var6.setFrom(ContactManager.getEidFromUserName(ChatManager.getInstance().getCurrentUser()));
        var6.setTo(var1);
        var6.setFromNick(var2);
        var5.setAccept(var6);
        var4.addExtension(var5);
        this.connection.sendPacket(var4);
    }

    void onInit() {
        EMLog.d(TAG, "init group manager");
        this.appContext = Chat.getInstance().getAppContext();
        //this.connection = SessionManager.getInstance().getConnection();
        //PacketExtensionFilter var1 = new PacketExtensionFilter("x", "http://jabber.org/protocol/muc#user");
        //this.connection.addPacketListener(this.applyListener, var1);
        this.chatProcessor.onInit();
    }

    void onDestroy() {
        this.allGroupLoaded = false;
        EMLog.d(TAG, "group manager logout");
        if(this.allGroups != null) {
            this.allGroups.clear();
        }

        if(this.groupChangeListeners != null) {
            this.groupChangeListeners.clear();
        }

        if(this.offlineGroupEvents != null) {
            this.offlineGroupEvents.clear();
        }

    }

    private class GroupChangeEvent {
        String groupId;
        String groupName;
        String inviterUserName;
        String reason;
        GroupManager.GroupEventType type;

        public GroupChangeEvent(GroupManager.GroupEventType var2, String var3, String var4, String var5, String var6) {
            this.type = var2;
            this.groupId = var3;
            this.groupName = var4;
            this.inviterUserName = var5;
            this.reason = var6;
        }
    }

    private static enum GroupEventType {
        Invitate,
        Apply,
        ApplicationAccept,
        ApplicationDeclind;

        private GroupEventType() {
        }
    }

    private class MUCSearchIQ extends IQ {
        public MUCSearchIQ(String var2, String var3) {
            this.setType(Type.GET);
            this.setFrom(var2);
            this.setTo(var3);
        }

        public String getChildElementXML() {
            return "<query xmlns=\'http://jabber.org/protocol/disco#items\' node=\'http://jabber.org/protocol/muc#rooms\'/>";
        }
    }

    class MucApplyListener implements PacketListener {
        private static final String TAG = "MucApplyListener";

        MucApplyListener() {
        }

        public void processPacket(Packet var1) {
            if(var1 instanceof org.jivesoftware.smack.packet.Message) {
                org.jivesoftware.smack.packet.Message var2 = (org.jivesoftware.smack.packet.Message)var1;
                PacketExtension var3 = var2.getExtension("x", "http://jabber.org/protocol/muc#user");
                if(var3 != null) {
                    MUCUser var4 = (MUCUser)var3;
                    MUCUser.Apply var5 = var4.getApply();
                    MUCUser.Accept var6 = var4.getAccept();
                    MUCUser.Decline var7 = var4.getDecline();
                    String var8;
                    String var9;
                    GroupChangeListener var10;
                    Iterator var11;
                    if(var5 != null) {
                        var8 = ContactManager.getUserNameFromEid(var5.getFrom());
                        var9 = ContactManager.getGroupIdFromEid(var5.getTo());
                        if(Chat.getInstance().appInited) {
                            var11 = GroupManager.this.groupChangeListeners.iterator();

                            while(var11.hasNext()) {
                                var10 = (GroupChangeListener)var11.next();
                                EMLog.d("MucApplyListener", "fire group application received event for group:" + var5.getToNick());
                                var10.onApplicationReceived(var9, var5.getToNick(), var8, var5.getReason());
                            }
                        } else {
                            EMLog.d("MucApplyListener", "aff offline group application received event for group:" + var5.getToNick());
                            GroupManager.this.offlineGroupEvents.add(GroupManager.this.new GroupChangeEvent(GroupManager.GroupEventType.Apply, var9, var5.getToNick(), var8, var5.getReason()));
                        }
                    } else if(var6 != null) {
                        var8 = ContactManager.getUserNameFromEid(var6.getFrom());
                        var9 = ContactManager.getGroupIdFromEid(var6.getTo());
                        if(Chat.getInstance().appInited) {
                            try {
                                Group var15 = GroupManager.this.getMUC(var6.getTo(), ChatManager.getInstance().getCurrentUser(), false, true);
                                GroupManager.this.createOrUpdateLocalGroup(var15);
                                Iterator var12 = GroupManager.this.groupChangeListeners.iterator();

                                while(var12.hasNext()) {
                                    GroupChangeListener var14 = (GroupChangeListener)var12.next();
                                    EMLog.d("MucApplyListener", "fire group application accept received event for group:" + var6.getFromNick());
                                    var14.onApplicationAccept(var9, var6.getFromNick(), var8);
                                }
                            } catch (XMPPException var13) {
                                var13.printStackTrace();
                            }
                        } else {
                            EMLog.d("MucApplyListener", "aff offline group application accetpt received event for group:" + var6.getFromNick());
                            GroupManager.this.offlineGroupEvents.add(GroupManager.this.new GroupChangeEvent(GroupManager.GroupEventType.ApplicationAccept, var9, var6.getFromNick(), var8, var6.getReason()));
                        }
                    } else if(var7 != null) {
                        var8 = ContactManager.getUserNameFromEid(var7.getFrom());
                        var9 = ContactManager.getGroupIdFromEid(var7.getTo());
                        if(Chat.getInstance().appInited) {
                            var11 = GroupManager.this.groupChangeListeners.iterator();

                            while(var11.hasNext()) {
                                var10 = (GroupChangeListener)var11.next();
                                EMLog.d("MucApplyListener", "fire group application declind received event for group:" + var7.getFromNick());
                                var10.onApplicationDeclined(var9, var7.getFromNick(), var8, var7.getReason());
                            }
                        } else {
                            EMLog.d("MucApplyListener", "aff offline group application declind received event for group:" + var7.getFromNick());
                            GroupManager.this.offlineGroupEvents.add(GroupManager.this.new GroupChangeEvent(GroupManager.GroupEventType.ApplicationDeclind, var9, var7.getFromNick(), var8, var7.getReason()));
                        }
                    }
                }

            }
        }
    }

    private class MucUserStatusListener implements UserStatusListener {
        private String roomJid;

        public MucUserStatusListener(String var2) {
            this.roomJid = var2;
        }

        public void adminGranted() {
            EMLog.d(GroupManager.TAG, "admin granted");
        }

        public void adminRevoked() {
            EMLog.d(GroupManager.TAG, "admin revoked");
        }

        public void banned(String var1, String var2) {
            EMLog.d(GroupManager.TAG, "banned actor:" + var1 + " reason:" + var2);
        }

        public void kicked(String var1, String var2) {
            try {
                String var3 = ContactManager.getUserNameFromEid(var1);
                EMLog.d(GroupManager.TAG, "kicked actor:" + var3 + " reason:" + var2);
                String var4 = ContactManager.getGroupIdFromEid(this.roomJid);
                EMLog.d(GroupManager.TAG, "current user has been revoked membership. delete local group:" + var4);
                GroupManager.this.deleteLocalGroup(var4);
                Iterator var6 = GroupManager.this.groupChangeListeners.iterator();

                while(var6.hasNext()) {
                    GroupChangeListener var5 = (GroupChangeListener)var6.next();
                    var5.onUserRemoved(var4, "");
                }
            } catch (Exception var7) {
                var7.printStackTrace();
            }

        }

        public void membershipGranted() {
            EMLog.d(GroupManager.TAG, "membership granted");
        }

        public void membershipRevoked() {
            EMLog.d(GroupManager.TAG, "membership revoked");
            String var1 = ContactManager.getGroupIdFromEid(this.roomJid);
            EMLog.d(GroupManager.TAG, "current user has been revoked membership. delete local group:" + var1);
            GroupManager.this.deleteLocalGroup(var1);
            Iterator var3 = GroupManager.this.groupChangeListeners.iterator();

            while(var3.hasNext()) {
                GroupChangeListener var2 = (GroupChangeListener)var3.next();
                var2.onUserRemoved(var1, "");
            }

        }

        public void moderatorGranted() {
            EMLog.d(GroupManager.TAG, "moderator granted");
        }

        public void moderatorRevoked() {
            EMLog.d(GroupManager.TAG, "moderator revoked");
        }

        public void ownershipGranted() {
            EMLog.d(GroupManager.TAG, "ownership granted");
        }

        public void ownershipRevoked() {
            EMLog.d(GroupManager.TAG, "ownership revoked");
        }

        public void voiceGranted() {
            EMLog.d(GroupManager.TAG, "voice granted");
        }

        public void voiceRevoked() {
            EMLog.d(GroupManager.TAG, "voice revoked");
        }
    }

    private class RoomQueryIQ extends IQ {
        private RoomQueryIQ() {
        }

        public String getChildElementXML() {
            return "<query xmlns=\"http://jabber.org/protocol/disco#items\" node=\"http://jabber.org/protocol/muc#rooms\"></query>";
        }
    }

    private class SearchPacketListener implements PacketListener {
        private SearchPacketListener() {
        }

        public void processPacket(Packet var1) {
            synchronized(GroupManager.this.mutex) {
                GroupManager.this.receivedQuery = true;
                GroupManager.this.mutex.notify();
            }
        }
    }
}

