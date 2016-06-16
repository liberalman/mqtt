package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.os.Parcel;
import android.os.Parcelable;

import com.seaofheart.app.util.EMLog;

import java.io.File;

public class ImageMessageBody extends FileMessageBody implements Parcelable {
    String thumbnailUrl;
    String thumbnailSecret;
    int width;
    int height;
    private boolean sendOriginalImage;
    public static final Creator<ImageMessageBody> CREATOR = new Creator() {
        public ImageMessageBody createFromParcel(Parcel var1) {
            //return new ImageMessageBody(var1, (ImageMessageBody)null);
            return new ImageMessageBody(var1);
        }

        public ImageMessageBody[] newArray(int var1) {
            return new ImageMessageBody[var1];
        }
    };

    public ImageMessageBody() {
        this.thumbnailSecret = null;
    }

    public ImageMessageBody(File var1) {
        this.thumbnailSecret = null;
        this.localUrl = var1.getAbsolutePath();
        this.fileName = var1.getName();
        EMLog.d("imagemsg", "create image message body for:" + var1.getAbsolutePath());
    }

    ImageMessageBody(String var1, String var2, String var3) {
        this.thumbnailSecret = null;
        this.fileName = var1;
        this.remoteUrl = var2;
        this.thumbnailUrl = var3;
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    public void setThumbnailUrl(String var1) {
        this.thumbnailUrl = var1;
    }

    public String toString() {
        return "image:" + this.fileName + ",localurl:" + this.localUrl + ",remoteurl:" + this.remoteUrl + ",thumbnial:" + this.thumbnailUrl;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeString(this.fileName);
        var1.writeString(this.localUrl);
        var1.writeString(this.remoteUrl);
        var1.writeString(this.thumbnailUrl);
        var1.writeInt(this.width);
        var1.writeInt(this.height);
    }

    private ImageMessageBody(Parcel var1) {
        this.thumbnailSecret = null;
        this.fileName = var1.readString();
        this.localUrl = var1.readString();
        this.remoteUrl = var1.readString();
        this.thumbnailUrl = var1.readString();
        this.width = var1.readInt();
        this.height = var1.readInt();
    }

    public void setThumbnailSecret(String var1) {
        this.thumbnailSecret = var1;
    }

    public String getThumbnailSecret() {
        return this.thumbnailSecret;
    }

    public void setSendOriginalImage(boolean var1) {
        this.sendOriginalImage = var1;
    }

    public boolean isSendOriginalImage() {
        return this.sendOriginalImage;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}

