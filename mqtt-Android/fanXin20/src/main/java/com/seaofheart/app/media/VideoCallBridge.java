package com.seaofheart.app.media;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.util.Log;

import com.seaofheart.app.a.a;

public class VideoCallBridge implements IGxStatusCallback {
    private static VideoCallBridge instance = null;

    private VideoCallBridge() {
    }

    public static VideoCallBridge getInstance() {
        if(instance == null) {
            instance = new VideoCallBridge();
        }

        return instance;
    }

    public static boolean createGLContext(int var0, int var1) {
        Log.d("SDL", "to call initEGL");
        return a.a(var0, var1);
    }

    public static void flipBuffers() {
        a.c();
    }

    public static void setActivityTitle(String var0) {
        a.a(var0);
    }

    public void updateStatus(int var1) {
    }
}

