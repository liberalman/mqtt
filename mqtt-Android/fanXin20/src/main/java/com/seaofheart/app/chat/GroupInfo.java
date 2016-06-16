package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/14.
 */
import java.io.Serializable;

public class GroupInfo implements Serializable {
    private static final long serialVersionUID = -2004486389398310700L;
    private String groupId;
    private String groupName;

    public GroupInfo(String var1, String var2) {
        this.groupId = var1;
        this.groupName = var2;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String var1) {
        this.groupId = var1;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String var1) {
        this.groupName = var1;
    }

    public String toString() {
        return this.groupName;
    }
}

