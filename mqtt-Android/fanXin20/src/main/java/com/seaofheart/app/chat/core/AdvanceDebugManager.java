//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.seaofheart.app.chat.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.seaofheart.app.CallBack;
import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.chat.ChatManager;
import com.seaofheart.app.chat.Message;
import com.seaofheart.app.chat.Message.Type;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.util.EMLog;

public class AdvanceDebugManager implements r { // class c
    private static final String TAG = "EMAdvanceDebugManager";
    private static String b = null; // b
    private static AdvanceDebugManager instance = null; // c
    private BroadcastReceiver broadcastReceiver = null; // d
    private Context context = Chat.getInstance().getAppContext(); // e
    private ConnectionManager connectionManager = null; // f

    private AdvanceDebugManager() {
        b = this.context.getPackageName() + ".debug.ipc.cmd";
    }

    public static synchronized AdvanceDebugManager getInstance() {
        if(instance == null) {
            instance = new AdvanceDebugManager();
        }

        return instance;
    }

    public void a(ConnectionManager var1) {
        this.connectionManager = var1;
    }

    private void h() {
        if(this.broadcastReceiver == null) {
            this.broadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context var1, Intent var2) {
                    if(var2.getAction().equals(AdvanceDebugManager.b)) {
                        String var3 = var2.getStringExtra("action");
                        AdvanceDebugManager.a var4 = null;

                        try {
                            var4 = AdvanceDebugManager.a.valueOf(var3);
                        } catch (Exception var6) {
                            ;
                        }

                        if(var4 == null) {
                            EMLog.e("EMAdvanceDebugManager", "unknow cmd action");
                            return;
                        }

                        Message var5 = Message.createReceiveMessage(ProtocolMessage.TYPE.CMD);
                        if(var2.getStringExtra("appkey") != null) {
                            var5.setAttribute("appkey", var2.getStringExtra("appkey"));
                        }

                        if(var2.getStringExtra("im_server") != null) {
                            var5.setAttribute("im_server", var2.getStringExtra("im_server"));
                        }

                        if(var2.getStringExtra("rest_server") != null) {
                            var5.setAttribute("rest_server", var2.getStringExtra("rest_server"));
                        }

                        if(var2.getBooleanExtra("enable_dns", false)) {
                            var5.setAttribute("enable_dns", true);
                        }

                        AdvanceDebugManager.this.a(var5, var4);
                    }

                }
            };
            this.context.registerReceiver(this.broadcastReceiver, new IntentFilter(b));
        }

    }

    public void a(Message var1, AdvanceDebugManager.a var2) {
        switch(var2.ordinal()) { // g()[var2.ordinal()]
        case 1:
            (new Thread(new Runnable() {
                public void run() {
                    EMLog.d("EMAdvanceDebugManager", "retrieve_dns");
                    j.getInstance().n();
                    if(j.getInstance().i() != null) {
                        j.getInstance().m();
                        if(AdvanceDebugManager.this.connectionManager != null) {
                            AdvanceDebugManager.this.connectionManager.onDnsConfigChanged();
                        }
                    }

                }
            })).start();
            break;
        case 2:
            EMLog.d("EMAdvanceDebugManager", "upload dns");
            j.Jsalahe var3 = j.getInstance().i();
            break;
        case 3:
            this.a(true);
            Chat.getInstance().setDebugMode(true);
            EMLog.d("EMAdvanceDebugManager", "debugmode set to true");
            break;
        case 4:
            this.a(false);
            EMLog.d("EMAdvanceDebugManager", "debugmode set to false");
            Chat.getInstance().setDebugMode(false);
            break;
        case 5:
            Chat.getInstance().uploadLog(new CallBack() {
                public void onSuccess() {
                    EMLog.d("EMAdvanceDebugManager", "upload log success");
                }

                public void onError(int var1, String var2) {
                    EMLog.d("EMAdvanceDebugManager", "upload log fail, error: " + var2);
                }

                public void onProgress(int var1, String var2) {
                }
            });
            break;
        case 6:
            boolean var4 = EMLog.debugMode;
            if(!var4) {
                EMLog.debugMode = true;
            }

            String var5 = "\r\n";
            EMLog.d("EMAdvanceDebugManager", " usename : " + ChatManager.getInstance().getCurrentUser() + "\r\n" + " appkey  : " + p.getInstance().y() + "\r\n" + " SDK     : " + p.getInstance().g());
            EMLog.debugMode = var4;
            break;
        case 7:
            String var6 = var1.getStringAttribute("appkey", (String)null);
            EMLog.d("EMAdvanceDebugManager", "received change appkey cmd, appkey: " + var6);
            if(var6 != null) {
                this.a(var6);
                Chat.getInstance().setAppkey(var6);
                Intent var11 = new Intent(this.context.getPackageName() + ".em_internal_debug");
                var11.putExtra("debug_action", "change_appkey");
                this.context.sendBroadcast(var11);
            }
            break;
        case 8:
            String var7 = var1.getStringAttribute("im_server", (String)null);
            String var8 = var1.getStringAttribute("rest_server", (String)null);
            boolean var9 = var1.getBooleanAttribute("enable_dns", false);
            if(var9) {
                if(p.getInstance().h()) {
                    return;
                }

                p.getInstance().c(true);
                this.a((String)null, (String)null);
            } else {
                EMLog.d("EMAdvanceDebugManager", "change servers to " + var7 + " and " + var8);
                if(var7 != null && var8 != null) {
                    p.getInstance().c(false);
                    this.a(var7, var8);
                    p.getInstance().b(var7);
                    p.getInstance().c(var8);
                    if(var7.contains(":")) {
                        p.getInstance().b(var7.split(":")[0]);
                        p.getInstance().a(Integer.valueOf(var7.split(":")[1]).intValue());
                    }

                    if(var8.contains(":")) {
                        p.getInstance().c(var8.split(":")[0]);
                    }
                }
            }

            Intent var10 = new Intent(this.context.getPackageName() + ".em_internal_debug");
            var10.putExtra("debug_action", "change_servers");
            this.context.sendBroadcast(var10);
        }

    }

    public void a(String var1, String var2) {
        v.a().a(var1, var2);
    }

    public String b() {
        return v.a().i();
    }

    public String c() {
        return v.a().j();
    }

    public void a(String var1) {
        v.a().c(var1);
    }

    public String d() {
        return v.a().k();
    }

    public void a(boolean var1) {
        v.a().a(var1);
    }

    public String e() {
        return v.a().l();
    }

    public void onInit() {
        this.h();
    }

    public void onDestroy() {
        this.connectionManager = null;
    }

    public static enum a {
        a,
        b,
        c,
        d,
        e,
        f,
        g,
        h;

        private a() {
        }
    }
}
