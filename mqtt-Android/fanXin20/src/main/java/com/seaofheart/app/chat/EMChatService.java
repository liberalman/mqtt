//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.seaofheart.app.chat;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.util.EMLog;

public class EMChatService extends Service {
    private static final String TAG = "chatservice";
    private final IBinder mBinder = new EMChatService.LocalBinder();

    public EMChatService() {
    }

    public void onCreate() {
        super.onCreate();
        EMLog.i("chatservice", "chat service created");
    }

    public int onStartCommand(Intent var1, int var2, int var3) {
        if(var1 != null && !p.getInstance().a() && !p.getInstance().b()) {
            String var4 = var1.getStringExtra("reason");
            Monitor.getInstance().start(this, this.getPackageName() + "/" + this.getClass().getName());
            Monitor.getInstance().startWakeup(this, var4);
        }

        if(!p.getInstance().a() && !p.getInstance().b()) {
            EMLog.d("chatservice", "start sticky!");
            return 1;
        } else {
            EMLog.d("chatservice", "start not sticky!");
            return 2;
        }
    }

    public void onDestroy() {
        EMLog.d("chatservice", "onDestroy");

        try {
            if(!p.getInstance().a() && !ChatManager.getInstance().stopService) {
                ChatManager.getInstance().doStartService();
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public IBinder onBind(Intent var1) {
        EMLog.d("chatservice", "onBind");
        return this.mBinder;
    }

    public boolean onUnbind(Intent var1) {
        return true;
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        EMChatService getService() {
            return EMChatService.this;
        }
    }
}
