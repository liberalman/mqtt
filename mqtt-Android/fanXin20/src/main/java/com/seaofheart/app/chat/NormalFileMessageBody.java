package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class NormalFileMessageBody extends FileMessageBody implements Parcelable {
    long fileSize;
    public static final Creator<NormalFileMessageBody> CREATOR = new Creator() {
        public NormalFileMessageBody createFromParcel(Parcel var1) {
            //return new NormalFileMessageBody(var1, (NormalFileMessageBody)null);
            return new NormalFileMessageBody(var1);
        }

        public NormalFileMessageBody[] newArray(int var1) {
            return new NormalFileMessageBody[var1];
        }
    };

    public NormalFileMessageBody(File var1) {
        this.localUrl = var1.getAbsolutePath();
        this.fileName = var1.getName();
        this.fileSize = var1.length();
    }

    NormalFileMessageBody(String var1, String var2) {
        this.fileName = var1;
        this.remoteUrl = var2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeString(this.fileName);
        var1.writeString(this.localUrl);
        var1.writeString(this.remoteUrl);
        var1.writeLong(this.fileSize);
        var1.writeString(this.secret);
    }

    public NormalFileMessageBody() {
    }

    private NormalFileMessageBody(Parcel var1) {
        this.fileName = var1.readString();
        this.localUrl = var1.readString();
        this.remoteUrl = var1.readString();
        this.fileSize = var1.readLong();
        this.secret = var1.readString();
    }

    public String toString() {
        return "normal file:" + this.fileName + ",localUrl:" + this.localUrl + ",remoteUrl:" + this.remoteUrl + ",file size:" + this.fileSize;
    }

    public long getFileSize() {
        return this.fileSize;
    }
}

