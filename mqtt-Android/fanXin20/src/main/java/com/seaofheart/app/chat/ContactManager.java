package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;

import com.seaofheart.app.analytics.PerformanceCollector;
import com.seaofheart.app.analytics.TimeTag;
import com.seaofheart.app.chat.core.ConnectionManager;
import com.seaofheart.app.chat.core.DBManager;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.NetUtils;

import org.jivesoftware.smack.PrivacyList;
import org.jivesoftware.smack.PrivacyListManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterStorage;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PrivacyItem;
import org.jivesoftware.smack.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactManager {
    private static final String TAG = "contact";
    Map<String, Contact> contactTable = new Hashtable(100);
    private EMRosterListener rosterListener = null;
    private Roster roster;
    RosterStorageImpl rosterStorage;
    private static ContactManager instance = null;
    private static final String black_list_name = "special";
    boolean enableRosterVersion = true;
    ContactListener contactListener = null;
    Set<String> deleteContactsSet = null;
    List<String> blackList = null;
    private boolean hasGetBlacklist = false;
    private static final String BROADCAST_CONTACT_CHANGED_ACTION = "com.seaofheart.app.contact.changed";
    private ConnectionManager xmppConnectionManager;
    private Context context;
    private boolean isIntied = false;

    private ContactManager() {
        this.blackList = Collections.synchronizedList(new ArrayList());
    }

    public static ContactManager getInstance() {
        if(instance == null) {
            instance = new ContactManager();
        }

        return instance;
    }

    void init(Context var1, ConnectionManager var2) {
        if(!this.isIntied) {
            EMLog.d("contact", "try to init contact manager");
            this.context = var1;
            this.xmppConnectionManager = var2;
            this.deleteContactsSet = Collections.synchronizedSet(new HashSet());
            if(this.enableRosterVersion) {
                this.loadContacts();
            }

            if(this.needGetRosterFromServer()) {
                TimeTag var3 = new TimeTag();
                var3.start();
                //this.roster = var2.getConnection().getRoster();
                //PerformanceCollector.collectRetrieveRoster(this.roster.getEntries().size(), var3.stop());
            } else {
                //this.roster = var2.getConnection().getRosterWithoutLoad();
            }

//            this.rosterListener = new EMRosterListener(this, this.roster);
//            this.roster.addRosterListener(this.rosterListener);
            this.isIntied = true;
            EMLog.d("contact", "created contact manager");
        }
    }

    boolean needGetRosterFromServer() {
        return DBManager.getInstance().k() || ChatManager.getInstance().getChatOptions().getUseRoster();
    }

    public void addContact(String var1, String var2) throws EaseMobException {
        this.addContactToRosterThroughPresence(var1.toLowerCase(), var2);
    }

    public void deleteContact(String var1) throws EaseMobException {
        this.removeContactFromRoster(var1);
        ChatManager.getInstance().deleteConversation(var1);
    }

    void addContactInternal(Contact contact) {
        EMLog.d("contact", "internal add contact:" + contact.eid);
        this.contactTable.put(contact.username, contact);
        DBManager.getInstance().addContact(contact.eid, contact.username);
    }

    void deleteContactInternal(String var1) {
        EMLog.d("contact", "delete contact:" + var1);
        Contact var2 = (Contact)this.contactTable.remove(var1);
        if(var2 != null) {
            DBManager.getInstance().delContact(var2.eid);
        } else {
            EMLog.w("contact", "local contact doesnt exists will try to delete:" + var1);
        }

        var2 = null;
        ChatManager.getInstance().deleteConversation(var1, false);
    }

    Contact getContactByUserName(String var1) {
        Contact var2 = (Contact)this.contactTable.get(var1);
        if(var2 == null) {
            var2 = new Contact(var1);
        }

        return var2;
    }

    void removeContactFromRoster(String var1) throws EaseMobException {
        try {
            RosterEntry var2 = this.roster.getEntry(getEidFromUserName(var1));
            if(var2 != null) {
                this.roster.removeEntry(var2);
            }

        } catch (Exception var3) {
            EMLog.e("contact", "Failed to delete contact:", var3);
            throw new EaseMobException("Failed to delete contact:" + var3);
        }
    }

    void removeContactByUsername(String var1) {
        Contact var2 = (Contact)this.contactTable.remove(var1);
        if(var2 != null) {
            DBManager.getInstance().delContact(var2.eid);
        }

        ChatManager.getInstance().deleteConversation(var1, false);
        EMLog.d("contact", "removed contact:" + var2);
        var2 = null;
    }

    public void reset() {
        this.contactTable.clear();
        this.blackList.clear();
        this.hasGetBlacklist = false;
        this.roster = null;
        this.rosterStorage = null;
        this.removeContactListener();
        this.isIntied = false;
    }

    RosterStorage getRosterStorage(Context var1) {
        if(this.rosterStorage == null) {
            this.rosterStorage = new RosterStorageImpl(var1, this);
        }

        return this.rosterStorage;
    }

    List<String> getRosterUserNames() throws EaseMobException {
        ChatManager.getInstance().checkConnection();
        if(this.enableRosterVersion) {
            this.loadContacts();
        }

        EMLog.d("contact", "start to get roster for user:" + SessionManager.getInstance().getLoginUserName());
        TimeTag var1 = new TimeTag();
        var1.start();
        //this.roster = this.xmppConnectionManager.getConnection().getRoster();
        Collection var2 = this.roster.getEntries();
        if(var2 != null) {
            PerformanceCollector.collectRetrieveRoster(var2.size(), var1.stop());
        }

        EMLog.d("contact", "get roster return size:" + var2.size());
        ArrayList var3 = new ArrayList();
        Iterator var5 = var2.iterator();

        while(true) {
            RosterEntry var4;
            do {
                if(!var5.hasNext()) {
                    return var3;
                }

                var4 = (RosterEntry)var5.next();
                EMLog.d("contact", "entry name:" + var4.getName() + " user:" + var4.getUser());
            } while(var4.getType() != RosterPacket.ItemType.both && var4.getType() != RosterPacket.ItemType.from);

            String var6 = var4.getName();
            if(var6 == null || var6.equals("")) {
                var6 = getUserNameFromEid(var4.getUser());
            }

            if(var6.startsWith(ChatConfig.getInstance().APPKEY)) {
                var6 = var6.substring((ChatConfig.getInstance().APPKEY + "_").length());
            }

            EMLog.d("contact", "get roster contact:" + var6);
            var3.add(var6);
        }
    }

    void addContactToRosterThroughPresence(String var1, String var2) throws EaseMobException {
        try {
            ChatManager.getInstance().checkConnection();
            Presence var3 = new Presence(Presence.Type.subscribe);
            String var4 = getEidFromUserName(var1);
            var3.setTo(var4);
            if(var2 != null && !"".equals(var2)) {
                var3.setStatus(var2);
            }

            //SessionManager.getInstance().getConnection().sendPacket(var3);
        } catch (Exception var5) {
            throw new EaseMobException(var5.getMessage());
        }
    }

    static String getBareEidFromUserName(String username) {
        return ChatConfig.getInstance().APPKEY + "_" + username;
    }

    static String getEidFromUserName(String var0) {
        return var0.contains("@")?var0:(var0.equals("bot")?"bot@echo.easemob.com":ChatConfig.getInstance().APPKEY + "_" + var0 + "@" + ChatConfig.DOMAIN);
    }

    public static String getUserNameFromEid(String var0) {
        String var1;
        if(var0.contains("@")) {
            var1 = var0.substring(0, var0.indexOf("@"));
        } else {
            var1 = var0;
        }

        if(var1 == null || "".equals(var1)) {
            var1 = var0;
        }

        return var1.startsWith(ChatConfig.getInstance().APPKEY)?var1.substring((ChatConfig.getInstance().APPKEY + "_").length()):var1;
    }

    static String getEidFromGroupId(String var0) {
        return var0.contains("@")?var0:ChatConfig.getInstance().APPKEY + "_" + var0 + ChatConfig.MUC_DOMAIN_SUFFIX;
    }

    static String getGroupIdFromEid(String var0) {
        String var1;
        if(var0.contains("@")) {
            var1 = var0.substring(0, var0.indexOf("@"));
        } else {
            var1 = var0;
        }

        if(var1 == null || "".equals(var1)) {
            var1 = var0;
        }

        return var1.startsWith(ChatConfig.getInstance().APPKEY)?var1.substring((ChatConfig.getInstance().APPKEY + "_").length()):var1;
    }

    /*String getCurrentUserFullJid() {
        String var1 = SessionManager.getInstance().currentUser.username;
        String var2 = ConnectionManager.getXmppResource(this.context);
        String var3 = getEidFromUserName(var1);
        String var4 = var3 + "/" + var2;
        return var4;
    }*/

    public List<String> getContactUserNames() throws EaseMobException {
        return this.getRosterUserNames();
    }

    public void setContactListener(ContactListener var1) {
        this.contactListener = var1;
    }

    public void removeContactListener() {
        this.contactListener = null;
    }

    public void addUserToBlackList(String var1, boolean var2) throws EaseMobException {
        String var3 = getEidFromUserName(var1);
        this.addToPrivacyList(var3, var2);
        DBManager.getInstance().addBlackList(var1);
        if(!this.blackList.contains(var1)) {
            this.blackList.add(var1);
        }

    }

    public void deleteUserFromBlackList(String var1) throws EaseMobException {
        String var2 = getEidFromUserName(var1);
        this.deleteFromPrivacyList(var2);
        DBManager.getInstance().delBlackList(var1);
        if(this.blackList.contains(var1)) {
            this.blackList.remove(var1);
        }

    }

    public List<String> getBlackListUsernames() {
        if(this.blackList.size() == 0) {
            List var1 = DBManager.getInstance().j();
            if(var1.size() != 0) {
                this.blackList.addAll(var1);
            }
        }

        return this.blackList;
    }

    public List<String> getBlackListUsernamesFromServer() throws EaseMobException {
        ArrayList var1 = new ArrayList();
        if(this.xmppConnectionManager != null && this.xmppConnectionManager.getConnection() != null && this.xmppConnectionManager.getConnection().isConnected()) {
            /*PrivacyListManager var2 = PrivacyListManager.getInstanceFor(this.xmppConnectionManager.getConnection());
            if(var2 != null) {
                try {
                    PrivacyList var3 = var2.getPrivacyList("special");
                    if(var3 != null) {
                        List var4 = var3.getItems();
                        Iterator var6 = var4.iterator();

                        while(var6.hasNext()) {
                            PrivacyItem var5 = (PrivacyItem)var6.next();
                            String var7 = var5.getValue();
                            var1.add(getUserNameFromEid(var7));
                        }
                    }
                } catch (XMPPException var8) {
                    if(var8 == null || var8.getMessage() == null || !var8.getMessage().contains("item-not-found")) {
                        throw new EaseMobException(var8.getMessage());
                    }
                }
            }*/

            return var1;
        } else {
            throw new EaseMobException("connetion is not connected");
        }
    }

    public void saveBlackList(List<String> var1) {
        if(var1 != null) {
            this.blackList.clear();
            this.blackList.addAll(var1);
            DBManager.getInstance().addBlackList(var1);
        }

    }

    private void addToPrivacyList(String var1, boolean var2) throws EaseMobException {
        try {
            /*PrivacyListManager var3 = PrivacyListManager.getInstanceFor(this.xmppConnectionManager.getConnection());
            if(var3 == null) {
                throw new EaseMobException("PrivacyListManager is null");
            } else {
                PrivacyList[] var4 = var3.getPrivacyLists();
                if(var4.length == 0) {
                    ArrayList var12 = new ArrayList();
                    PrivacyItem var13 = new PrivacyItem("jid", false, 100);
                    if(!var2) {
                        var13.setFilterMessage(true);
                    }

                    var13.setValue(var1);
                    var12.add(var13);
                    var3.updatePrivacyList("special", var12);
                    var3.setDefaultListName("special");
                    var3.setActiveListName("special");
                } else {
                    PrivacyList var5 = var3.getPrivacyList("special");
                    if(var5 != null) {
                        List var6 = var5.getItems();
                        boolean var7 = false;
                        Iterator var9 = var6.iterator();

                        PrivacyItem var8;
                        while(var9.hasNext()) {
                            var8 = (PrivacyItem)var9.next();
                            String var10 = var8.getValue();
                            EMLog.d("contact", "addToPrivacyList item.getValue=" + var8.getValue());
                            if(var10.equalsIgnoreCase(var1)) {
                                var7 = true;
                                break;
                            }
                        }

                        if(var7) {
                            EMLog.d("contact", "current user is already in black list");
                            return;
                        }

                        var8 = new PrivacyItem("jid", false, 100);
                        var8.setValue(var1);
                        var6.add(var8);
                        if(!var2) {
                            var8.setFilterMessage(true);
                        }

                        EMLog.d("contact", "addToPrivacyList item.getValue=" + var8.toXML());
                        var3.updatePrivacyList("special", var6);
                        var3.setActiveListName("special");
                    }

                }
            }*/
        } catch (Exception var11) {
            throw new EaseMobException(var11.getMessage());
        }
    }

    private void deleteFromPrivacyList(String var1) throws EaseMobException {
        if(this.xmppConnectionManager != null && this.xmppConnectionManager.getConnection() != null) {
            /*PrivacyListManager var2 = PrivacyListManager.getInstanceFor(this.xmppConnectionManager.getConnection());
            if(var2 == null) {
                throw new EaseMobException("PrivacyListManager is null");
            } else {
                try {
                    PrivacyList[] var3 = var2.getPrivacyLists();
                    if(var3.length != 0) {
                        PrivacyList var4 = var2.getPrivacyList("special");
                        if(var4 != null) {
                            List var5 = var4.getItems();
                            if(var5 != null && var5.size() != 0) {
                                boolean var6 = false;
                                Iterator var8 = var5.iterator();

                                while(var8.hasNext()) {
                                    PrivacyItem var7 = (PrivacyItem)var8.next();
                                    String var9 = var7.getValue();
                                    EMLog.d("contact", "PrivacyList item.getValue=" + var7.getValue());
                                    if(var9.equalsIgnoreCase(var1)) {
                                        var5.remove(var7);
                                        var6 = true;
                                        break;
                                    }
                                }

                                if(!var6) {
                                    EMLog.d("contact", "current user is not exsit in the black list");
                                    return;
                                }

                                var2.declineDefaultList();
                                var2.updatePrivacyList("special", var5);
                                if(var5.size() > 0) {
                                    var2.setDefaultListName("special");
                                    var2.setActiveListName("special");
                                }
                            } else {
                                EMLog.d("contact", "current user is not exsit in the black list");
                            }
                        }
                    }

                } catch (Exception var10) {
                    var10.printStackTrace();
                    throw new EaseMobException(var10.getMessage());
                }
            }*/
        } else {
            throw new EaseMobException("connection is null, please login first");
        }
    }

    public static String getContactChangeAction() {
        return "com.seaofheart.app.contact.changed_" + ChatConfig.getInstance().APPKEY;
    }

    void checkConnection() throws EaseMobException {
        if(this.xmppConnectionManager != null) {
            if(this.xmppConnectionManager.getConnection() != null) {
                //if(!this.xmppConnectionManager.getConnection().isConnected() || !this.xmppConnectionManager.getConnection().isAuthenticated()) {
                if(!this.xmppConnectionManager.getConnection().isConnected()) {
                    EMLog.e("contact", "network unconnected");
                    if(NetUtils.hasDataConnection(Chat.getInstance().getAppContext())) {
                        EMLog.d("contact", "try to reconnect after check connection failed");
                    }
                }

            }
        }
    }

    void loadContacts() {
        if(!ChatManager.getInstance().getChatOptions().getUseRoster() && !this.enableRosterVersion) {
            EMLog.d("contact", "roster is disabled, skip load contacts from db");
        } else if(DBManager.getInstance() == null) {
            EMLog.d("contact", "first time exec. no contact db");
        } else {
            List var1 = DBManager.getInstance().i();
            Iterator var3 = var1.iterator();

            while(var3.hasNext()) {
                Contact var2 = (Contact)var3.next();
                this.contactTable.put(var2.username, var2);
            }

            EMLog.d("contact", "loaded contacts:" + this.contactTable.size());
            if(this.rosterStorage != null) {
                EMLog.d("contact", "sync roster storage with db");
                this.rosterStorage.loadEntries();
            }

        }
    }

    boolean hasContact(String var1) {
        return this.contactTable.get(var1) != null;
    }
}

