package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MultiUserChatRoomModelBase extends Contact {
    protected String description;
    protected String owner;
    protected ArrayList<String> members;
    protected long lastModifiedTime;
    protected int maxUsers = 0;
    protected int affiliationsCount = -1;
    protected boolean isMsgBlocked = false;

    private void init() {
        this.lastModifiedTime = 0L;
        this.members = new ArrayList();
        this.description = "";
        this.owner = "";
    }

    protected MultiUserChatRoomModelBase() {
        this.init();
    }

    public MultiUserChatRoomModelBase(String var1) {
        this.username = var1;
        this.eid = ContactManager.getEidFromGroupId(var1);
        this.init();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String var1) {
        this.description = var1;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String var1) {
        this.owner = var1;
    }

    public synchronized void addMember(String var1) {
        this.members.add(var1);
    }

    public synchronized void removeMember(String var1) {
        this.members.remove(var1);
    }

    public synchronized List<String> getMembers() {
        return Collections.unmodifiableList(this.members);
    }

    public synchronized void setMembers(List<String> var1) {
        this.members.addAll(var1);
    }

    public String getId() {
        return this.username;
    }

    public void setId(String var1) {
        this.username = var1;
        this.eid = ContactManager.getEidFromGroupId(var1);
    }

    public String getName() {
        return this.nick;
    }

    public void setName(String var1) {
        this.nick = var1;
    }

    public int getMaxUsers() {
        return this.maxUsers;
    }

    public void setMaxUsers(int var1) {
        this.maxUsers = var1;
    }

    public int getAffiliationsCount() {
        return this.affiliationsCount;
    }

    public void setAffiliationsCount(int var1) {
        this.affiliationsCount = var1;
    }

    public boolean getMsgBlocked() {
        return this.isMsgBlocked;
    }

    public boolean isMsgBlocked() {
        return this.isMsgBlocked;
    }

    public void setMsgBlocked(boolean var1) {
        this.isMsgBlocked = var1;
    }

    public String toString() {
        return this.nick;
    }

    Bitmap getGroupAvator() {
        Exception var1 = new Exception("group avator not supported yet");
        var1.printStackTrace();
        return null;
    }

    protected void copyModel(MultiUserChatRoomModelBase var1) {
        this.eid = var1.eid;
        this.description = var1.description;
        this.lastModifiedTime = System.currentTimeMillis();
        this.members.clear();
        this.members.addAll(var1.getMembers());
        this.nick = var1.nick;
        this.owner = var1.owner;
        this.username = var1.username;
        this.maxUsers = var1.maxUsers;
        this.affiliationsCount = var1.affiliationsCount;
        this.isMsgBlocked = var1.isMsgBlocked;
    }

    public long getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    public void setLastModifiedTime(long var1) {
        this.lastModifiedTime = var1;
    }
}

