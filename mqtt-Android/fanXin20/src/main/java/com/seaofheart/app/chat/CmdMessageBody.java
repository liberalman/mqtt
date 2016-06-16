package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class CmdMessageBody extends MessageBody implements Parcelable {
    public String action;
    public HashMap<String, String> params;
    public static final Creator<CmdMessageBody> CREATOR = new Creator() {
        public CmdMessageBody createFromParcel(Parcel var1) {
            //return new CmdMessageBody(var1, (CmdMessageBody)null);
            return new CmdMessageBody(var1);
        }

        public CmdMessageBody[] newArray(int var1) {
            return new CmdMessageBody[var1];
        }
    };

    public CmdMessageBody(String action, HashMap<String, String> params) {
        this.action = action;
        this.params = params;
    }

    public CmdMessageBody(String action) {
        this.action = action;
    }

    public String toString() {
        return "cmd:\"" + this.action + "\"";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeString(this.action);
        var1.writeMap(this.params);
    }

    private CmdMessageBody(Parcel var1) {
        this.action = var1.readString();
        this.params = var1.readHashMap((ClassLoader)null);
    }
}

