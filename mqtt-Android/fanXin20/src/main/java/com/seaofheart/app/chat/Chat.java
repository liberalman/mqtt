package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.seaofheart.app.analytics.ActiveCollector;
import com.seaofheart.app.chat.core.AdvanceDebugManager;
import com.seaofheart.app.chat.core.j;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.CallBack;
import com.seaofheart.app.DebugHelper;
import com.seaofheart.app.util.EMLog;

import java.util.Random;

public class Chat {
    private static final String TAG = "EaseMob";
    private static Chat instance = new Chat();
    boolean appInited = false;
    private boolean autoLogin = true;
    private boolean initSingleProcess = true;
    private boolean sdkInited = false;
    private Context appContext = null;
    private String username = null;
    private String password = null;
    private static final String PID_FILE = ".easemob.pid";

    public Chat() {
    }

    public static Chat getInstance() {
        return instance;
    }

    public void setDebugMode(boolean var1) {
        if(getInstance().isSDKInited()) {
            String var2 = AdvanceDebugManager.getInstance().e();
            if(var2 != null) {
                var1 = Boolean.parseBoolean(var2);
            }
        }

        p.getInstance().g(var1);
        EMLog.debugMode = var1;
    }

    public void setLogMode(EMLog.ELogMode var1) {
        EMLog.setLogMode(var1);
    }

    public void setAutoLogin(boolean var1) {
        this.autoLogin = var1;
    }

    public void setAppInited() {
        this.appInited = true;
        ChatManager.getInstance().onAppInited();
    }

    public void setInitSingleProcess(boolean var1) {
        this.initSingleProcess = var1;
    }

    public void uploadLog(CallBack var1) {
        DebugHelper.uploadLog(this.appContext, j.getInstance().c().host, var1);
    }

    public void setUserName(String userName) {
        if(userName != null && !userName.equals("")) {
            this.username = userName;
        }
    }

    public void setPassword(String password) {
        if(password != null && !password.equals("")) {
            this.password = password;
        }
    }

    public void init(Context var1) {
        if(this.initSingleProcess && this.checkSDKInited(var1)) {
            Log.d("EaseMob", "skip init easemob since already inited");
        } else {
            EMLog.e("EaseMob", "easemob init in process:" + Process.myPid());
            this.appContext = var1.getApplicationContext();
            boolean var2 = ChatConfig.getInstance().loadConfig(this.appContext);
            if(!var2) {
                Log.e("EaseMob", "wrong configuration");
                throw new RuntimeException("Initialization failed! : wrong configuration");
            } else {
                InitSmackStaticCode.initStaticCode(var1);
                ChatManager chatManager = ChatManager.getInstance().onInit(); // 这里是第一次生成全局ChatManager类对象
                SessionManager var4 = SessionManager.getInstance().onInit(); // 这里是第一次生成全局SessionManager类对象
                String username = this.username;
                EMLog.e("EaseMob", "passed userName : " + this.username);
                if(username == null) {
                    username = var4.getLastLoginUser();
                }

                ActiveCollector.sendActivePacket(this.appContext);
                EMLog.e("EaseMob", "is autoLogin : " + this.autoLogin);
                EMLog.e("EaseMob", "lastLoginUser : " + username);
                if(this.autoLogin) { // 自动登录
                    if(this.isLoggedIn()) {
                        String var6 = this.password;
                        if(var6 == null) {
                            var6 = var4.getLastLoginPwd();
                        }

                        var4.login(username, var6, false, (CallBack)null);
                    }
                } else if(username != null && !username.equals("")) {
                    chatManager.initDB(username);
                    chatManager.loadDB();
                }

                EMLog.e("EaseMob", "HuanXin SDK is initialized with version : " + p.getInstance().g());
                this.sdkInited = true;
            }
        }
    }

    public String getVersion() {
        return p.getInstance().g();
    }

    public Context getAppContext() {
        return this.appContext;
    }

    public void setEnv(ChatConfig.EnvMode var1) {
        ChatConfig.getInstance().setEnv(var1);
    }

    void setSDKMode(ChatConfig.SDKMode var1) {
        ChatConfig.getInstance().setSDKMode(var1);
    }

    public void setAppkey(String var1) {
        ChatConfig.getInstance().APPKEY = var1;
        p.getInstance().d(var1);
    }

    public String getAppkey() {
        return p.getInstance().y();
    }

    public void enalbeDNSConfig(boolean var1) {
        p.getInstance().c(var1);
    }

    public boolean isLoggedIn() {
        if(!TextUtils.isEmpty(this.username) && !TextUtils.isEmpty(this.password)) {
            return true;
        } else {
            SessionManager var1 = SessionManager.getInstance();
            String var2 = var1.getLastLoginUser();
            String var3 = var1.getLastLoginPwd();
            return var2 != null && var3 != null && !var2.equals("") && !var3.equals("");
        }
    }

    public void setServerAddress(String[] var1, String[] var2) {
        if(var1 != null && var2 != null && var1.length >= 1 && var2.length >= 1) {
            this.enalbeDNSConfig(false);
            int var3 = (new Random()).nextInt(var1.length);
            String var4 = var1[var3];
            String[] var5 = var4.split(":");
            var4 = var5[0];
            p.getInstance().b(var4);
            if(var5.length == 2) {
                p.getInstance().a(Integer.valueOf(var5[1]).intValue());
            }

            var3 = (new Random()).nextInt(var2.length);
            var4 = var2[var3];
            var4 = var4.split(":")[0];
            p.getInstance().c(var4);
        }
    }

    boolean isSDKInited() {
        return this.sdkInited;
    }

    void clear() {
        this.username = null;
        this.password = null;
    }

    private boolean checkSDKInited(Context var1) {
        return this.sdkInited;
    }
}

