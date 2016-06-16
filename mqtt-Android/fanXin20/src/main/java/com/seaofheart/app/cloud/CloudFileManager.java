package com.seaofheart.app.cloud;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.text.format.Time;

import java.util.Map;
import java.util.Properties;

public abstract class CloudFileManager {
    protected Properties sessionContext;
    protected static final String TAG = "CloudFileManager";
    public static CloudFileManager instance = null;

    public CloudFileManager() {
    }

    public abstract boolean authorization();

    public String getRemoteFileName(String var1, String var2) {
        Time var3 = new Time();
        var3.setToNow();
        String var4 = var2.substring(var2.lastIndexOf("."), var2.length());
        String var5 = var1 + var3.toString().substring(0, 15);
        return var5 + var4;
    }

    public abstract void uploadFileInBackground(String var1, String var2, String var3, String var4, Map<String, String> var5, CloudOperationCallback var6);

    public abstract void downloadFile(String var1, String var2, String var3, Map<String, String> var4, CloudOperationCallback var5);

    public abstract void deleteFileInBackground(String var1, String var2, String var3, CloudOperationCallback var4);
}

