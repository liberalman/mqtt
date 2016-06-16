package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;

import com.seaofheart.app.chat.core.s;
import com.seaofheart.app.util.EasyUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Monitor {
    private static Monitor _instance = null;
    private static String FILENAME = "pid";
    private static final String TAG = "Monitor";
    private boolean libraryLoaded = false;
    private boolean nativeServiceStarted = false;
    private Context mContext;
    private boolean wakeuped;
    private s monitorDB = new s();

    private Monitor() {
    }

    static synchronized Monitor getInstance() {
        if(_instance == null) {
            _instance = new Monitor();
            _instance.loadLibrary();
        }

        return _instance;
    }

    void start(Context var1, String var2) {
        if(this.libraryLoaded) {
            this.mContext = var1;
            if(!this.nativeServiceStarted) {
                this.startMonitor(var2);
                this.nativeServiceStarted = true;
            }

        }
    }

    void startWakeup(Context var1, String var2) {
        if(this.libraryLoaded) {
            if(!this.wakeuped && !"wakeup".equals(var2)) {
                this.wakeuped = true;
                ArrayList var3 = new ArrayList();
                List var4 = this.monitorDB.a();
                List var5 = EasyUtils.getRunningApps(var1);
                Iterator var7 = var4.iterator();

                while(var7.hasNext()) {
                    String var6 = (String)var7.next();
                    if(!var5.contains(var6)) {
                        var6 = var6 + "/" + EMChatService.class.getName();
                        var3.add(var6);
                    }
                }

                if(var3.size() != 0) {
                    this.startWakeup((String[])var3.toArray(new String[var3.size()]));
                }

            }
        }
    }

    public s getMonitorDB() {
        return this.monitorDB;
    }

    private native void startMonitor(String var1);

    private native void startWakeup(String[] var1);

    private void loadLibrary() {
        try {
            System.loadLibrary("easemobservice");
            this.libraryLoaded = true;
        } catch (Throwable var2) {
            this.libraryLoaded = false;
        }

    }
}

