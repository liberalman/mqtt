package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.os.Parcel;
import android.os.Parcelable;

import com.seaofheart.app.util.EMLog;

import java.io.File;

public class VideoMessageBody extends FileMessageBody implements Parcelable {
    int length;
    String thumbnailUrl;
    String localThumb;
    String thumbnailSecret;
    long file_length;
    public static final Creator<VideoMessageBody> CREATOR = new Creator() {
        public VideoMessageBody[] newArray(int var1) {
            return new VideoMessageBody[var1];
        }

        public VideoMessageBody createFromParcel(Parcel var1) {
            //return new VideoMessageBody(var1, (VideoMessageBody)null);
            return new VideoMessageBody(var1);
        }
    };

    public VideoMessageBody() {
        this.length = 0;
        this.thumbnailSecret = null;
        this.file_length = 0L;
    }

    public VideoMessageBody(File var1, String var2, int var3, long var4) {
        this.length = 0;
        this.thumbnailSecret = null;
        this.file_length = 0L;
        this.localUrl = var1.getAbsolutePath();
        this.fileName = var1.getName();
        this.localThumb = var2;
        this.length = var3;
        this.file_length = var4;
        EMLog.d("videomsg", "create video,message body for:" + var1.getAbsolutePath());
    }

    VideoMessageBody(String var1, String var2, String var3, int var4) {
        this.length = 0;
        this.thumbnailSecret = null;
        this.file_length = 0L;
        this.fileName = var1;
        this.remoteUrl = var2;
        this.thumbnailUrl = var3;
        this.length = var4;
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    public void setThumbnailUrl(String var1) {
        this.thumbnailUrl = var1;
    }

    public long getVideoFileLength() {
        return this.file_length;
    }

    public void setVideoFileLength(long var1) {
        this.file_length = var1;
    }

    public void setLocalThumb(String var1) {
        this.localThumb = var1;
    }

    public String getLocalThumb() {
        return this.localThumb;
    }

    public int getLength() {
        return this.length;
    }

    public String toString() {
        return "video:" + this.fileName + ",localUrl:" + this.localUrl + ",remoteUrl:" + this.remoteUrl + ",thumbnailUrl:" + this.thumbnailUrl + ",length:" + this.length;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeString(this.fileName);
        var1.writeString(this.localUrl);
        var1.writeString(this.remoteUrl);
        var1.writeString(this.thumbnailUrl);
        var1.writeString(this.localThumb);
        var1.writeInt(this.length);
        var1.writeLong(this.file_length);
    }

    private VideoMessageBody(Parcel var1) {
        this.length = 0;
        this.thumbnailSecret = null;
        this.file_length = 0L;
        this.fileName = var1.readString();
        this.localUrl = var1.readString();
        this.remoteUrl = var1.readString();
        this.thumbnailUrl = var1.readString();
        this.localThumb = var1.readString();
        this.length = var1.readInt();
        this.file_length = var1.readLong();
    }

    public void setThumbnailSecret(String var1) {
        this.thumbnailSecret = var1;
    }

    public String getThumbnailSecret() {
        return this.thumbnailSecret;
    }
}

