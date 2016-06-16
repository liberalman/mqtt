package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.seaofheart.app.chat.core.j;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.EasyUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class HeartBeatReceiver extends BroadcastReceiver {
    private static final String TAG = "HeartBeatReceiver";
    SmartHeartBeat smartHeartbeat = null;
    private ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public HeartBeatReceiver(SmartHeartBeat var1) {
        this.smartHeartbeat = var1;
    }

    public void onReceive(Context var1, Intent var2) {
        EMLog.d("HeartBeatReceiver", "onReceive HeartBeatReceiver");
        if(this.smartHeartbeat != null) {
            this.smartHeartbeat.start();
        }

        this.onCheckFroreground();
    }

    private void onCheckFroreground() {
        final Context var1 = Chat.getInstance().getAppContext();
        if(var1 != null) {
            if(PushNotificationHelper.getInstance().isUsingGCM()) {
                this.threadPool.submit(new Runnable() {
                    public void run() {
                        j.Jsalahe var1x = j.getInstance().i();
                        boolean var2 = var1x != null?var1x.e:true;
                        if(var2) {
                            if(!EasyUtils.isAppRunningForeground(var1)) {
                                SessionManager.getInstance().disconnect();
                            } else {
                                ChatManager.getInstance().tryToReconnectOnGCM();
                            }

                        }
                    }
                });
            }
        }
    }
}

