package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.seaofheart.app.util.EMLog;

import org.jivesoftware.smack.RosterStorage;
import org.jivesoftware.smack.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class RosterStorageImpl implements RosterStorage {
    private static final String TAG = "rosterstorage";
    private static final String PERF_KEY_ROSTERVER = "easemob.roster.ver.";
    private String version;
    private ContactManager contactManager;
    private Context appContext;
    private ArrayList<RosterPacket.Item> rosterItems;

    public RosterStorageImpl(Context var1, ContactManager var2) {
        this.appContext = var1;
        this.contactManager = var2;
        this.version = null;
        this.rosterItems = new ArrayList();
    }

    void loadEntries() {
        Iterator var2 = this.contactManager.contactTable.values().iterator();

        while(var2.hasNext()) {
            Contact var1 = (Contact)var2.next();
            ContactManager.getInstance();
            RosterPacket.Item var3 = new RosterPacket.Item(ContactManager.getEidFromUserName(var1.username), var1.username);
            var3.setItemType(RosterPacket.ItemType.both);
            this.rosterItems.add(var3);
        }

        EMLog.d("rosterstorage", "roster storage load entries, roster items size:" + this.rosterItems.size());
    }

    public void addEntry(RosterPacket.Item var1, String var2) {
        if(var1.getItemType() == RosterPacket.ItemType.both || var1.getItemType() == RosterPacket.ItemType.from) {
            EMLog.d("rosterstorage", "roster storage add new contact:" + var1.getUser());
            String var3 = var1.getUser();
            String var4 = ContactManager.getUserNameFromEid(var3);
            String var5 = ContactManager.getBareEidFromUserName(var4);
            ContactManager.getInstance().addContactInternal(new Contact(var5, var4));
        }

        if(var2 != null && !var2.equals("") && !var2.equals(this.version)) {
            this.updateRosterVersion(var2);
        }

    }

    public List<RosterPacket.Item> getEntries() {
        return this.rosterItems;
    }

    public RosterPacket.Item getEntry(String var1) {
        Iterator var3 = this.rosterItems.iterator();

        while(var3.hasNext()) {
            RosterPacket.Item var2 = (RosterPacket.Item)var3.next();
            if(var2.getName().equals(var1)) {
                return var2;
            }
        }

        EMLog.e("rosterstorage", "cant find roster entry for jid:" + var1);
        return null;
    }

    public int getEntryCount() {
        EMLog.d("rosterstorage", "get entry count return:" + this.rosterItems.size());
        return this.rosterItems.size();
    }

    public String getRosterVersion() {
        if(this.version == null) {
            this.version = PreferenceManager.getDefaultSharedPreferences(this.appContext).getString("easemob.roster.ver." + SessionManager.getInstance().currentUser.eid, "");
            EMLog.d("rosterstorage", "load roster storage for jid" + SessionManager.getInstance().currentUser.eid + " version:" + this.version);
        }

        return this.version;
    }

    private void updateRosterVersion(String var1) {
        this.version = var1;
        SharedPreferences.Editor var2 = PreferenceManager.getDefaultSharedPreferences(this.appContext).edit();
        var2.putString("easemob.roster.ver." + SessionManager.getInstance().currentUser.eid, var1);
        var2.commit();
        EMLog.d("rosterstorage", "updated roster version to:" + var1);
    }

    public void removeEntry(String var1, String var2) {
        String var3 = ContactManager.getUserNameFromEid(var1);
        ContactManager.getInstance().deleteContactInternal(var3);
        if(var2 != null && !var2.equals("") && !var2.equals(this.version)) {
            this.updateRosterVersion(var2);
        }

    }

    public void updateLocalEntry(RosterPacket.Item var1) {
        EMLog.d("rosterstorage", "[skip]roster storage uplodateLocalEntry:" + var1);
    }
}

