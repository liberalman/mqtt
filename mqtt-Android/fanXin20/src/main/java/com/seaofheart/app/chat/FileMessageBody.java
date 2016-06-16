package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.CallBack;

public abstract class FileMessageBody extends MessageBody {
    public transient CallBack downloadCallback = null;
    public transient boolean downloaded = false;
    String fileName = null;
    String localUrl = null;
    String remoteUrl = null;
    String secret = null;

    public FileMessageBody() {
    }

    public void setDownloadCallback(CallBack var1) {
        if(this.downloaded) {
            var1.onProgress(100, (String)null);
            var1.onSuccess();
        } else {
            this.downloadCallback = var1;
        }
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String var1) {
        this.fileName = var1;
    }

    public String getLocalUrl() {
        return this.localUrl;
    }

    public void setLocalUrl(String var1) {
        this.localUrl = var1;
    }

    public String getRemoteUrl() {
        return this.remoteUrl;
    }

    public void setRemoteUrl(String var1) {
        this.remoteUrl = var1;
    }

    public void setSecret(String var1) {
        this.secret = var1;
    }

    public String getSecret() {
        return this.secret;
    }
}

