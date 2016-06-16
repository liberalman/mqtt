package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.util.EMLog;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

class EMRosterListener implements RosterListener {
    private static final String TAG = "contact";
    private ContactManager contactManager;
    private Roster roster;

    public EMRosterListener(ContactManager var1, Roster var2) {
        this.contactManager = var1;
        this.roster = var2;
    }

    public void entriesAdded(Collection<String> var1) {
        EMLog.d("contact", "on contact entries added:" + var1);
        ArrayList var2 = new ArrayList();
        Iterator var4 = var1.iterator();

        while(true) {
            while(true) {
                String var3;
                RosterEntry var5;
                do {
                    if(!var4.hasNext()) {
                        if(this.contactManager.contactListener != null && var2.size() != 0) {
                            this.contactManager.contactListener.onContactAdded(var2);
                        }

                        return;
                    }

                    var3 = (String)var4.next();
                    var5 = this.roster.getEntry(var3);
                } while(var5 == null);

                if(var5.getType() != RosterPacket.ItemType.both && var5.getType() != RosterPacket.ItemType.from) {
                    EMLog.d("contact", "ignore entry type:" + var5.getType());
                } else {
                    EMLog.d("contact", "entry add: roster entry name:" + var5.getName() + " user:" + var5.getUser());
                    String var6 = ContactManager.getUserNameFromEid(var3);
                    String var7 = ContactManager.getBareEidFromUserName(var6);
                    var2.add(var6);
                }
            }
        }
    }

    public void entriesDeleted(Collection<String> var1) {
        EMLog.d("contact", "on contact entries deleted:" + var1);
        ArrayList var2 = new ArrayList();
        Iterator var4 = var1.iterator();

        while(var4.hasNext()) {
            String var3 = (String)var4.next();
            String var5 = ContactManager.getUserNameFromEid(var3);
            if(this.contactManager.hasContact(var5)) {
                var2.add(var5);
                this.contactManager.removeContactByUsername(var5);
            }
        }

        if(this.contactManager.contactListener != null && var2.size() != 0) {
            this.contactManager.contactListener.onContactDeleted(var2);
        }

    }

    public void entriesUpdated(Collection<String> var1) {
        EMLog.d("contact", "on contact entries updated:" + var1);
        ArrayList var2 = new ArrayList();
        ArrayList var3 = new ArrayList();
        Iterator var5 = var1.iterator();

        while(true) {
            String var7;
            do {
                do {
                    if(!var5.hasNext()) {
                        return;
                    }

                    String var4 = (String)var5.next();
                    RosterEntry var6 = this.roster.getEntry(var4);
                    if(var6.getType() == RosterPacket.ItemType.both || var6.getType() == RosterPacket.ItemType.from) {
                        var7 = ContactManager.getUserNameFromEid(var4);
                        var2.add(var7);
                    }

                    EMLog.d("contact", "entry.getType() = " + var6.getType() == null?"null":var6.getType().toString());
                    if(var6.getType() == RosterPacket.ItemType.none) {
                        if(ContactManager.getInstance().deleteContactsSet.contains(var4)) {
                            var3.add(ContactManager.getUserNameFromEid(var4));
                        } else {
                            if(var6.getStatus() != null && RosterPacket.ItemStatus.SUBSCRIPTION_PENDING.toString().equals(var6.getStatus().toString())) {
                                return;
                            }

                            if(this.contactManager.contactListener != null) {
                                this.contactManager.contactListener.onContactRefused(ContactManager.getUserNameFromEid(var4));
                            }
                        }

                        try {
                            this.roster.removeEntry(var6);
                        } catch (Exception var9) {
                            ;
                        }
                    }

                    if(this.contactManager.contactListener != null && var2.size() != 0) {
                        this.contactManager.contactListener.onContactAdded(var2);
                    }
                } while(this.contactManager.contactListener == null);
            } while(var3.size() == 0);

            Iterator var8 = var3.iterator();

            while(var8.hasNext()) {
                var7 = (String)var8.next();
                ChatManager.getInstance().deleteConversation(var7);
            }

            this.contactManager.contactListener.onContactDeleted(var3);
        }
    }

    public void presenceChanged(Presence var1) {
    }
}

