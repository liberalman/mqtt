package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.seaofheart.app.chat.core.AdvanceDebugManager;
import com.seaofheart.app.chat.core.j;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.util.EMLog;

public class ChatConfig {
    private static final String TAG = "conf";
    private static final String CONFIG_EASEMOB_APPKEY = "EASEMOB_APPKEY";
    private static final String CONFIG_EASEMOB_CHAT_ADDRESS = "EASEMOB_CHAT_ADDRESS";
    private static final String CONFIG_EASEMOB_CHAT_DOMAIN = "EASEMOB_CHAT_DOMAIN";
    private static final String CONFIG_EASEMOB_GROUP_DOMAIN = "EASEMOB_GROUP_DOMAIN";
    private static final String CONFIG_EASEMOB_API_URL = "EASEMOB_API_URL";
    static String DOMAIN = "easemob.com";
    static String DOMAIN_SUFFIX = "@easemob.com";
    static String MUC_DOMAIN = "conference.easemob.com";
    static String MUC_DOMAIN_SUFFIX = "@conference.easemob.com";
    static final String UUID = "uuid";
    static final String SHARE_SERCRET = "share-secret";
    static final String TOKEN_ENTITY = "entities";
    public String APPKEY = null;
    private static boolean debugTrafficMode = false;
    private static ChatConfig instance = null;
    private String chatServer;
    private String restServer;
    private int xmppPort = 0;

    private ChatConfig() {
    }

    public static ChatConfig getInstance() {
        if(instance == null) {
            instance = new ChatConfig();
        }

        return instance;
    }

    public static boolean isDebugTrafficMode() {
        return debugTrafficMode;
    }

    boolean loadConfig(Context var1) {
        String var2 = var1.getPackageName();
        ApplicationInfo var3 = null;

        try {
            var3 = var1.getPackageManager().getApplicationInfo(var2, 128);
        } catch (PackageManager.NameNotFoundException var11) {
            EMLog.e("conf", var11.getMessage());
            EMLog.e("conf", "找不到ApplicationInfo");
        }

        if(var3 != null) {
            Bundle var4 = var3.metaData;
            if(var4 == null) {
                EMLog.w("conf", "请确认meta属性写在清单文件里的application节点以内");
            } else {
                String var5 = var4.getString("EASEMOB_APPKEY");
                if(var5 == null && this.APPKEY == null) {
                    Log.w("conf", "EASEMOB_APPKEY is not set in AndroidManifest file");
                } else if(TextUtils.isEmpty(this.APPKEY)) {
                    this.APPKEY = var5;
                }

                String var6 = var4.getString("EASEMOB_CHAT_ADDRESS");
                if(var6 != null) {
                    this.chatServer = var6;
                }

                String var7 = var4.getString("EASEMOB_API_URL");
                if(var7 != null) {
                    this.restServer = var7;
                }

                String var8 = var4.getString("EASEMOB_CHAT_DOMAIN");
                if(var8 != null) {
                    DOMAIN = var8;
                }

                String var9 = var4.getString("EASEMOB_GROUP_DOMAIN");
                if(var9 != null) {
                    MUC_DOMAIN = var9;
                }

                String var10 = var4.getString("GCM_PROJECT_NUMBER");
                if(var10 != null && p.getInstance().d() == null) {
                    p.getInstance().a(var10);
                }
            }
        }

        DOMAIN_SUFFIX = "@" + DOMAIN;
        MUC_DOMAIN_SUFFIX = "@" + MUC_DOMAIN;
        this.setAdvanceDebugConfig();
        p.getInstance().d(this.APPKEY);
        EMLog.i("conf", "EASEMOB_APPKEY is set to:" + this.APPKEY);
        if(this.chatServer != null && !this.chatServer.equals("")) {
            p.getInstance().b(this.chatServer);
        }

        if(this.restServer != null && !this.restServer.equals("")) {
            p.getInstance().c(this.restServer);
        }

        if(this.xmppPort != 0) {
            p.getInstance().a(this.xmppPort);
        }

        this.printConfig();
        return true;
    }

    private void setAdvanceDebugConfig() {
        try {
            String var1 = AdvanceDebugManager.getInstance().e();
            if(var1 != null) {
                EMLog.debugMode = Boolean.parseBoolean(var1);
            }

            if(AdvanceDebugManager.getInstance().d() != null) {
                this.APPKEY = AdvanceDebugManager.getInstance().d();
            }

            String var2 = AdvanceDebugManager.getInstance().b();
            String var3 = AdvanceDebugManager.getInstance().c();
            if(var2 != null && var3 != null) {
                if(var2.contains(":")) {
                    this.xmppPort = Integer.valueOf(var2.split(":")[1]).intValue();
                    var2 = var2.split(":")[0];
                }

                if(var3.contains(":")) {
                    var2 = var3.split(":")[0];
                }

                this.chatServer = var2;
                this.restServer = var3;
                p.getInstance().c(false);
            }
        } catch (Exception var4) {
            ;
        }

    }

    private void printConfig() {
        EMLog.d("conf", " APPKEY:" + this.APPKEY + " CHATSERVER:" + p.getInstance().k() + " domain:" + DOMAIN);
        EMLog.d("conf", "STORAGE_URL:" + p.getInstance().o());
    }

    public String getDomain() {
        return DOMAIN;
    }

    public String getStorageUrl() {
        return j.getInstance().c().host;
    }

    void setEnv(ChatConfig.EnvMode var1) {
        p.getInstance().a(var1);
    }

    void setSDKMode(ChatConfig.SDKMode var1) {
        p.getInstance().a(var1);
    }

    ChatConfig.SDKMode getSDKMode() {
        return p.getInstance().x();
    }

    ChatConfig.EnvMode getEnvMode() {
        return p.getInstance().w();
    }

    public static enum EnvMode {
        EMSandboxMode,
        EMProductMode,
        EMDevMode;

        private EnvMode() {
        }
    }

    public static enum SDKMode {
        EMChatMode,
        EMHelpDeskMode;

        private SDKMode() {
        }
    }
}

