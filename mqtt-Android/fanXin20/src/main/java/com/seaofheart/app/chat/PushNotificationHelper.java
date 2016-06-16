package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.os.Build;
import android.util.Pair;

import com.seaofheart.app.chat.core.j;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.EMLog;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.gcm.GoogleCloudMessaging;
//import com.xiaomi.mipush.sdk.MiPushClient;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

class PushNotificationHelper {
    public static final String TAG = "PushNotificationHelper";
    private static PushNotificationHelper instance;
    private Thread pushThread = null;
    private Object sendTokenLock = new Object();
    private boolean isLogout = false;
    private String notifyDeviceToken;

    PushNotificationHelper() {
    }

    public static PushNotificationHelper getInstance() {
        if(instance == null) {
            instance = new PushNotificationHelper();
        }

        return instance;
    }

    boolean checkAvailablePushService() {
        boolean var1 = j.getInstance().i() != null?j.getInstance().i().d:false;
        EMLog.d("PushNotificationHelper", "GCM is enabled : " + var1);
        boolean var2 = false;

        try {
            if(var1 && Class.forName("com.google.android.gms.common.GooglePlayServicesUtil") != null) {
                /*int var3 = GooglePlayServicesUtil.isGooglePlayServicesAvailable(Chat.getInstance().getAppContext());
                var2 = var3 == 0;
                EMLog.d("PushNotificationHelper", "GCM service available : " + var2);
                p.getInstance().a(var2);*/
            }
        } catch (ClassNotFoundException var5) {
            EMLog.e("PushNotificationHelper", var5.toString());
        } catch (Exception var6) {
            ;
        }

        if(var2) {
            return var2;
        } else {
            /*try {
                if(Class.forName("com.xiaomi.mipush.sdk.MiPushClient") != null) {
                    var2 = MiPushClient.shouldUseMIUIPush(Chat.getInstance().getAppContext());
                    EMLog.d("PushNotificationHelper", "mipush available : " + var2);
                    p.getInstance().b(var2);
                }
            } catch (ClassNotFoundException var4) {
                var4.printStackTrace();
            }*/

            return var2;
        }
    }

    boolean isPushServiceEnabled() {
        return p.getInstance().a() || p.getInstance().b();
    }

    boolean isUsingGCM() {
        return p.getInstance().a();
    }

    void onInit() {
        this.isLogout = false;
    }

    void sendDeviceTokenToServer() {
        if(!p.getInstance().a() && !p.getInstance().b()) {
            EMLog.d("PushNotificationHelper", "GCM not available");
        } else {
            EMLog.d("PushNotificationHelper", "third-party push available");
            if(this.isLogout) {
                return;
            }

            if(this.pushThread != null && this.pushThread.isAlive()) {
                return;
            }

            if(this.pushThread == null) {
                this.pushThread = new Thread() {
                    public void run() {
                        try {
                            String var1 = PushNotificationHelper.this.getDeviceToken();
                            int var2;
                            if(var1 == null) {
                                var2 = 0;

                                while(true) {
                                    if(var2 < 3) {
                                        var1 = PushNotificationHelper.this.getDeviceToken();
                                        if(var1 == null) {
                                            ++var2;
                                            continue;
                                        }
                                    }

                                    if(var1 == null) {
                                        p.getInstance().a(false);
                                        p.getInstance().b(false);
                                        ChatManager.getInstance().doStopService();
                                        ChatManager.getInstance().doStartService();
                                        return;
                                    }
                                    break;
                                }
                            }

                            var2 = -1;
                            RandomDelay var3 = new RandomDelay();
                            if(this.isInterrupted()) {
                                return;
                            }

                            boolean var4;
                            if(p.getInstance().S() == null) {
                                for(var4 = PushNotificationHelper.this.sendDeviceInfo(var1); !var4; var4 = PushNotificationHelper.this.sendDeviceInfo(var1)) {
                                    ++var2;

                                    try {
                                        sleep((long)(var3.timeDelay(var2) * 1000));
                                    } catch (Exception var7) {
                                        var7.printStackTrace();
                                        return;
                                    }
                                }
                            }

                            var3.reset();
                            var2 = -1;
                            if(this.isInterrupted()) {
                                return;
                            }

                            for(var4 = PushNotificationHelper.this.sendTokenToServer(var1); !var4; var4 = PushNotificationHelper.this.sendTokenToServer(var1)) {
                                ++var2;

                                try {
                                    sleep((long)(var3.timeDelay(var2) * 1000));
                                } catch (Exception var6) {
                                    var6.printStackTrace();
                                    return;
                                }
                            }
                        } catch (Exception var8) {
                            EMLog.e("PushNotificationHelper", var8.toString());
                        }

                    }
                };
                this.pushThread.start();
            }
        }

    }

    public void onDestroy(boolean var1) throws EaseMobException {
        EMLog.d("PushNotificationHelper", "push notification helper ondestory");
        this.onReceiveMipushToken((String)null);
        if(this.pushThread != null) {
            this.pushThread.interrupt();
            this.pushThread = null;
        }

        this.isLogout = true;
        if(var1 && this.isPushServiceEnabled()) {
            boolean var2 = this.sendTokenToServer("");
            if(!var2) {
                EMLog.d("PushNotificationHelper", "unbind device token faild");
                throw new EaseMobException(-3000, "unbind device token failed");
            }

            p.getInstance().a(false);
            p.getInstance().b(false);
        }

    }

    boolean sendTokenToServer(String var1) {
        Object var2 = this.sendTokenLock;
        synchronized(this.sendTokenLock) {
            String var3 = p.getInstance().O() + "/users/" + ChatManager.getInstance().getCurrentUser();

            try {
                JSONObject var4 = new JSONObject();
                var4.put("device_token", var1);
                var4.put("notifier_name", p.getInstance().d());
                if(p.getInstance().b()) {
                    var4.put("notifier_name", p.getInstance().f().a);
                }

                EMLog.d("PushNotificationHelper", "send device token to server, token = " + var1 + ",url = " + var3);
                Pair var5 = HttpClient.getInstance().sendRequestWithToken(var3, var4.toString(), HttpClient.PUT);
                int var6 = ((Integer)var5.first).intValue();
                String var7 = (String)var5.second;
                switch(var6) {
                    case 200:
                        EMLog.d("PushNotificationHelper", "sendTokenToServer SC_OK:");
                        return true;
                    default:
                        EMLog.d("PushNotificationHelper", "sendTokenToServer error:" + var7);
                }
            } catch (Exception var8) {
                EMLog.e("PushNotificationHelper", var8.toString());
            }

            return false;
        }
    }

    boolean sendDeviceInfo(String var1) {
        String var2 = p.getInstance().O() + "/devices";
        JSONObject var3 = new JSONObject();

        try {
            var3.put("model", "android");
            var3.put("name", var1);
            var3.put("token", var1);
            var3.put("sdk_version", Chat.getInstance().getVersion());
            var3.put("os_version", Build.VERSION.RELEASE);
            Pair var4 = HttpClient.getInstance().sendRequest(var2, (Map)null, var3.toString(), HttpClient.POST);
            int var5 = ((Integer)var4.first).intValue();
            String var6 = (String)var4.second;
            switch(var5) {
                case 200:
                    p.getInstance().j(var1);
                    EMLog.d("PushNotificationHelper", "sendDeviceToServer SC_OK:");
                    return true;
                default:
                    if(var6.contains("duplicate_unique_property_exists")) {
                        p.getInstance().j(var1);
                        return true;
                    }

                    EMLog.d("PushNotificationHelper", "sendDeviceToServer error : " + var6);
            }
        } catch (Exception var7) {
            EMLog.e("PushNotificationHelper", var7.toString());
        }

        return false;
    }

    String getDeviceToken() {
        String var1 = p.getInstance().S();
        if(var1 != null) {
            return var1;
        } else {
            try {
                p.b var2 = p.getInstance().c();
                if(var2 != null && var2 != p.b.a) {
                    if(var2 == p.b.b) {
                        p.a var9 = p.getInstance().f();
                        if(var9 != null) {
                            //MiPushClient.registerPush(Chat.getInstance().getAppContext(), var9.a, var9.b);
                            Object var4 = this.sendTokenLock;
                            synchronized(this.sendTokenLock) {
                                try {
                                    this.sendTokenLock.wait();
                                } catch (InterruptedException var6) {
                                    var6.printStackTrace();
                                }
                            }

                            var1 = this.notifyDeviceToken;
                        }
                    }
                } else if(p.getInstance().d() != null) {
                    /*GoogleCloudMessaging var3 = null;
                    if(var3 == null) {
                        var3 = GoogleCloudMessaging.getInstance(Chat.getInstance().getAppContext());
                    }

                    var1 = var3.register(new String[]{p.getInstance().d()});*/
                }
            }catch (Exception e){e.printStackTrace();}/* catch (IOException var8) {
                var8.printStackTrace();
            }*/

            EMLog.d("PushNotificationHelper", "devicetoken = " + var1);
            return var1;
        }
    }

    void onReceiveMipushToken(String var1) {
        this.notifyDeviceToken = var1;
        Object var2 = this.sendTokenLock;
        synchronized(this.sendTokenLock) {
            try {
                this.sendTokenLock.notify();
            } catch (Exception var4) {
                var4.printStackTrace();
            }

        }
    }

    private boolean notificationDisplayStyle(int var1) {
        String var2 = p.getInstance().O() + "/users/" + ChatManager.getInstance().getCurrentUser();

        try {
            JSONObject var3 = new JSONObject();
            var3.put("notification_display_style", var1);
            Pair var4 = HttpClient.getInstance().sendRequest(var2, (Map)null, var3.toString(), HttpClient.PUT);
            int var5 = ((Integer)var4.first).intValue();
            String var6 = (String)var4.second;
            switch(var5) {
                case 200:
                    EMLog.d("PushNotificationHelper", "notificationDisplayStyle SC_OK");
                    return true;
                default:
                    EMLog.d("PushNotificationHelper", "notificationDisplayStyle error:" + var6);
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return false;
    }
}

