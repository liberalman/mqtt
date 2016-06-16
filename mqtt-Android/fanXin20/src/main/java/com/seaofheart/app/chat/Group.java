package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.graphics.Bitmap;

public class Group extends MultiUserChatRoomModelBase {
    protected boolean isPublic;
    protected boolean allowInvites;
    protected boolean membersOnly;

    public Group(String var1) {
        super(var1);
        this.username = var1;
        this.isPublic = false;
        this.allowInvites = false;
        this.membersOnly = false;
    }

    public String getGroupId() {
        return this.getId();
    }

    public void setGroupId(String var1) {
        this.setId(var1);
    }

    public String getGroupName() {
        return this.getName();
    }

    public void setGroupName(String var1) {
        this.setName(var1);
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public void setIsPublic(boolean var1) {
        this.isPublic = var1;
    }

    public boolean isAllowInvites() {
        return this.allowInvites;
    }

    public boolean isMembersOnly() {
        return this.membersOnly;
    }

    Bitmap getGroupAvator() {
        Exception var1 = new Exception("group avator not supported yet");
        var1.printStackTrace();
        return null;
    }

    void copyGroup(Group var1) {
        this.copyModel(var1);
        this.isPublic = var1.isPublic;
        this.allowInvites = var1.allowInvites;
        this.membersOnly = var1.membersOnly;
    }

    public void setPublic(boolean var1) {
        this.isPublic = var1;
    }
}

