package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
    protected long uid;
    protected String avatar;
    protected String eid;
    protected String username;
    protected String nick;
    public static final Creator<Contact> CREATOR = new Creator() {
        public Contact createFromParcel(Parcel var1) {
            return new Contact(var1);
        }

        public Contact[] newArray(int var1) {
            return new Contact[var1];
        }
    };

    protected Contact() {
    }

    public Contact(String eid, String username) {
        this.eid = eid;
        if(username.contains("@")) {
            this.username = ContactManager.getUserNameFromEid(username);
        } else {
            this.username = username;
        }

    }

    public Contact(String var1) {
        if(var1.contains("@")) {
            this.eid = var1;
            this.username = ContactManager.getUserNameFromEid(var1);
        } else {
            this.username = var1;
            this.eid = ContactManager.getEidFromUserName(var1);
        }

    }

    public void setUsername(String var1) {
        this.username = var1;
    }

    public String getUsername() {
        return this.username;
    }

    public void setNick(String var1) {
        this.nick = var1;
    }

    public String getNick() {
        return this.nick == null?this.username:this.nick;
    }

    public int compare(Contact var1) {
        return this.getNick().compareTo(var1.getNick());
    }

    public String toString() {
        return "<contact jid:" + this.eid + ", username:" + this.username + ", nick:" + this.nick + ">";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeString(this.eid);
        var1.writeString(this.username);
        var1.writeString(this.nick);
    }

    private Contact(Parcel var1) {
        this.eid = var1.readString();
        this.username = var1.readString();
        this.nick = var1.readString();
    }

    public String getEid() {
        return this.eid;
    }

    public void setEid(String var1) {
        this.eid = var1;
    }
}

