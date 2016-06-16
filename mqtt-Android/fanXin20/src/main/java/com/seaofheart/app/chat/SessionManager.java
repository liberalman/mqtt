package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.seaofheart.app.analytics.LoginCollector;
import com.seaofheart.app.analytics.TimeTag;
import com.seaofheart.app.util.net;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.cloud.HttpClientConfig;
import com.seaofheart.app.exceptions.AuthenticationException;
import com.seaofheart.app.exceptions.NetworkUnconnectedException;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.CryptoUtils;
import com.seaofheart.app.chat.core.ConnectionManager;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.NetUtils;
import com.seaofheart.app.util.PathUtil;
import com.seaofheart.app.CallBack;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

class SessionManager {
    private static final String TAG = "Session";
    private static SessionManager instance = new SessionManager();
    private ConnectionManager connectionManager = null;
    private Context appContext = null;
    private static final String PREF_KEY_LOGIN_USER = "easemob.chat.loginuser";
    private static final String PREF_KEY_LOGIN_PWD = "easemob.chat.loginpwd";
    public Contact currentUser = null;
    private String lastLoginUser = null;
    private String lastLoginPwd = null;
    SmartHeartBeat smartHeartbeat = null;
    private Object loginLock = new Object();

    SessionManager() {
    }

    public static synchronized SessionManager getInstance(Context var0) {
        if(var0 != null) {
            instance.appContext = var0;
        }

        return instance;
    }

    public static synchronized SessionManager getInstance() {
        if(instance.appContext == null) {
            instance.appContext = Chat.getInstance().getAppContext();
        }

        return instance;
    }

    synchronized SessionManager onInit() {
        if(this.appContext == null) {
            this.appContext = Chat.getInstance().getAppContext();
        }

        return this;
    }

    /**
     * 初始化MQTT连接
     * @param username
     * @param password
     */
    private void initMqttConnection(String username, String password) {
        if(this.connectionManager != null) {
            try {
                EMLog.d("Session", "try to disconnect previous connection");
                this.connectionManager.disconnect();
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        } else {
            this.connectionManager = new ConnectionManager();
        }

        this.connectionManager.onInit(username, password);
        ChatManager.getInstance().onNewConnectionCreated(this.connectionManager);
        if(this.smartHeartbeat == null) {
            this.smartHeartbeat = SmartHeartBeat.create();
        }

        //this.smartHeartbeat.onInit(this.getConnection());
    }

    void checkConnection() throws EaseMobException {
        //EMLog.d("Session", "check connection...");
        if(this.connectionManager == null) {
            throw new NetworkUnconnectedException("xmppConnectionManager is null");
        } else if(this.connectionManager.getConnection() == null) {
            throw new NetworkUnconnectedException("connection is null");
        //} else if(this.connectionManager.isConnected() && this.connectionManager.getConnection().isAuthenticated()) {
        } else if(this.connectionManager.isConnected()) {
            EMLog.d("Session", "check connection ok");
        } else {
            EMLog.e("Session", "network unconnected");
            throw new NetworkUnconnectedException(-1001, "connection is not connected");
        }
    }

    void loadDB() {
        ChatManager.getInstance().loadDB();
    }

    /**
     * 登录
     * @param hxid
     * @param password
     * @param var3
     * @param var4
     */
    private synchronized void loginSync(String hxid, String password, boolean var3, CallBack var4) {
        if(hxid != null && password != null && !hxid.equals("") && !password.equals("")) {
            String var5 = this.getLastLoginUser();
            String var6 = this.getLastLoginPwd();
            boolean var7 = false;
            if(var5 != null && var6 != null) {
                var7 = var5.equals(hxid) && var6.equals(password);
            }

            EMLog.d("Session", "loginSync : in process " + Process.myPid());
            String eid = ContactManager.getBareEidFromUserName(hxid);
            EMLog.d("Session", "login with eid:" + eid); // eid 示例 fanxin888#fanxin_11240731
            TimeTag var9 = new TimeTag();
            var9.start();
            if(this.isConnected() && var7) {
                EMLog.d("Session", "resue existing connection manager");
                this.connectionManager.reuse();
                EMLog.d("Session", "already loggedin and conected. skip login");
                if(var4 != null) {
                    var4.onSuccess();
                }

            } else {
                if(ChatConfig.isDebugTrafficMode()) {
                    net.a();
                }

                this.currentUser = new Contact(eid, hxid);

                try {
                    this.loadDB();
                } catch (Exception var26) {
                    var26.printStackTrace();
                    if(var4 != null) {
                        var4.onError(-1, var26.toString());
                    }
                }

                try {
                    if(var3) {
                        int var10 = 3;

                        while(true) {
                            --var10;
                            if(var10 <= 0) {
                                break;
                            }

                            try {
                                p.getInstance().login(hxid, password);
                            } catch (SocketTimeoutException var12) {
                                EMLog.d("Session", "SocketTimeoutException happened when retrieveing the token");
                            } catch (ConnectTimeoutException var13) {
                                EMLog.d("Session", "ConnectTimeoutException happened when retrieveing the token");
                            }
                        }
                    } else {
                        p.getInstance().login(hxid, password);
                    }
                } catch (JSONException var16) {
                    if(var3) {
                        if(var4 != null) {
                            var4.onError(-1000, "Wrong response result was returned from server : " + var16.getMessage());
                        }

                        return;
                    }
                } catch (UnknownHostException var17) {
                    EMLog.e("Session", "unknow host exception:" + var17.toString());
                    if(var3 && !NetUtils.hasNetwork(this.appContext)) {
                        if(var4 != null) {
                            var4.onError(-1001, "there is not network connction, please check you network");
                        }

                        return;
                    }

                    if(var3) {
                        if(var4 != null) {
                            var4.onError(-1002, "can\'t resolve DNS host");
                        }

                        return;
                    }
                } catch (NoRouteToHostException var18) {
                    EMLog.e("Session", "NoRouteToHostException:" + var18.toString());
                    if(var3) {
                        if(var4 != null) {
                            var4.onError(-1003, "can not connect to server : " + var18.toString());
                        }

                        return;
                    }
                } catch (ConnectException var19) {
                    EMLog.e("Session", "ConnectException:" + var19.toString());
                    if(var3) {
                        if(var4 != null) {
                            var4.onError(-1003, "can not connect to server : " + var19.toString());
                        }

                        return;
                    }
                } catch (SocketException var20) {
                    EMLog.e("Session", "SocketException:" + var20.toString());
                    if(var3) {
                        if(var4 != null) {
                            var4.onError(-1003, "can not connect to server : " + var20.toString());
                        }

                        return;
                    }
                } catch (SocketTimeoutException var21) {
                    EMLog.e("Session", "SocketTimeoutException:" + var21.toString());
                    if(var3) {
                        if(var4 != null) {
                            var4.onError(-1004, "server response timer out");
                        }

                        return;
                    }
                } catch (AuthenticationException var22) {
                    EMLog.e("Session", "AuthenticationException:" + var22.toString());
                    if(var3) {
                        if(var4 != null) {
                            var4.onError(-1005, "invalid user or password");
                        }

                        return;
                    }
                } catch (ConnectTimeoutException var23) {
                    EMLog.e("Session", "ConnectTimeoutException:" + var23.toString());
                    if(var3) {
                        if(var4 != null) {
                            var4.onError(-1004, "connection timer out");
                        }

                        return;
                    }
                } catch (IOException var24) {
                    EMLog.e("Session", "IOException:" + var24.toString());
                    if(var3) {
                        if(var4 != null) {
                            var4.onError(-1007, "IO exception : " + var24.toString());
                        }

                        return;
                    }
                } catch (Exception var25) {
                    if(var25 != null) {
                        EMLog.e("Session", "Exception:" + var25.toString());
                    }

                    if(var3) {
                        if(var4 != null) {
                            var4.onError(-1003, "failed to connect to server ：" + var25.toString());
                        }

                        return;
                    }
                }

                boolean var27 = false;

                try {
                    var27 = PushNotificationHelper.getInstance().checkAvailablePushService();
                    if(p.getInstance().x() == ChatConfig.SDKMode.EMHelpDeskMode) {
                        CustomerService.getInstance().scheduleLogout(CustomerService.EMScheduleLogoutReason.EMLogin);
                    }

                    this.appContext = Chat.getInstance().getAppContext();
                    this.initMqttConnection(eid, password);
                    PathUtil.getInstance().initDirs(ChatConfig.getInstance().APPKEY, hxid, this.appContext);
                    this.connectionManager.setChatTag(var9);
                    this.connectionManager.connectSync(var3);
                    String var11 = this.getLastLoginUser();
                    if(!hxid.equals(var11)) {
                        this.setLastLoginUser(hxid);
                        this.setLastLoginPwd(password);
                    }

                    ContactManager.getInstance().init(this.appContext, this.connectionManager);
                } catch (AuthenticationException var14) {
                    var14.printStackTrace();
                    if(var4 != null) {
                        EMLog.e("Session", "AuthenticationException failed: " + var14.toString());
                        var4.onError(-1005, "invalid password or username");
                    }

                    return;
                } catch (Exception var15) {
                    if(var15 != null) {
                        var15.printStackTrace();
                        EMLog.e("Session", "xmppConnectionManager.connectSync() failed: " + var15.getMessage());
                    }

                    if(var4 != null) {
                        var4.onError(-1003, var15.getMessage());
                    }

                    return;
                }

                EMLog.i("Session", "EaseMob Server connected.");
                LoginCollector.collectLoginTime(var9.stop());
                if(var27) {
                    PushNotificationHelper.getInstance().sendDeviceTokenToServer();
                }

                if(var4 != null) {
                    var4.onSuccess();
                }

            }
        } else {
            if(var4 != null) {
                var4.onError(-1005, "the username or password is null or empty!");
            }

        }
    }

    void login(final String hxid, final String var2, final boolean var3, final CallBack var4) {
        Log.d("Session", "initDB (" + hxid + "," + var2);
        ChatManager.getInstance().initDB(hxid);
        Thread var5 = new Thread() { // 起个新线程连接服务器认证
            public void run() {
                SessionManager.this.loginSync(hxid, var2, var3, var4);
            }
        };
        var5.setPriority(9);
        var5.start();
    }

    public PowerManager.WakeLock getWakeLock() {
        return this.connectionManager.getWakeLock();
    }

    public void logout() {
        Thread var1 = new Thread() {
            public void run() {
                EMLog.d("Session", "Session logout");
                SessionManager.this.syncLogout();
            }
        };
        var1.setPriority(9);
        var1.start();
    }

    public void syncLogout() {
        EMLog.d("Session", "Session logout");
        if(this.smartHeartbeat != null) {
            this.smartHeartbeat.stop();
        }

        try {
            this.connectionManager.disconnect();
            if(p.getInstance().E()) {
                p.getInstance().D();
            }
        } catch (Exception var2) {
            EMLog.d("Session", var2.toString());
        }

    }

    String getLastLoginUser() {
        if(this.lastLoginUser == null) {
            SharedPreferences var1 = PreferenceManager.getDefaultSharedPreferences(this.appContext);
            this.lastLoginUser = var1.getString("easemob.chat.loginuser", "");
        }

        return this.lastLoginUser;
    }

    void setLastLoginUser(String var1) {
        if(var1 != null) {
            this.lastLoginUser = var1;
            SharedPreferences var2 = PreferenceManager.getDefaultSharedPreferences(this.appContext);
            SharedPreferences.Editor var3 = var2.edit();
            var3.putString("easemob.chat.loginuser", var1);
            var3.commit();
        }
    }

    String getLastLoginPwd() {
        if(this.lastLoginPwd == null) {
            SharedPreferences var1 = PreferenceManager.getDefaultSharedPreferences(this.appContext);
            String var2 = var1.getString("easemob.chat.loginpwd", "");
            if(var2.equals("")) {
                this.lastLoginPwd = "";
                return this.lastLoginPwd;
            }

            try {
                CryptoUtils var3 = ChatManager.getInstance().getCryptoUtils();
                this.lastLoginPwd = var3.decryptBase64String(var2);
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

        return this.lastLoginPwd;
    }

    void clearLastLoginUser() {
        try {
            this.lastLoginUser = "";
            SharedPreferences var1 = PreferenceManager.getDefaultSharedPreferences(this.appContext);
            SharedPreferences.Editor var2 = var1.edit();
            var2.putString("easemob.chat.loginuser", this.lastLoginUser);
            var2.commit();
        } catch (Exception var3) {
            ;
        }

    }

    void clearLastLoginPwd() {
        try {
            this.lastLoginPwd = "";
            SharedPreferences var1 = PreferenceManager.getDefaultSharedPreferences(this.appContext);
            SharedPreferences.Editor var2 = var1.edit();
            var2.putString("easemob.chat.loginpwd", this.lastLoginPwd);
            var2.commit();
        } catch (Exception var3) {
            ;
        }

    }

    void setLastLoginPwd(String var1) {
        if(var1 != null) {
            this.lastLoginPwd = var1;
            SharedPreferences var2 = PreferenceManager.getDefaultSharedPreferences(this.appContext);
            SharedPreferences.Editor var3 = var2.edit();

            try {
                CryptoUtils var4 = ChatManager.getInstance().getCryptoUtils();
                String var5 = var4.encryptBase64String(var1);
                var3.putString("easemob.chat.loginpwd", var5);
                var3.commit();
            } catch (Exception var6) {
                var6.printStackTrace();
            }

        }
    }

    public boolean isConnected() {
        //return this.connectionManager == null?false:this.connectionManager.isConnected() & this.connectionManager.isAuthentificated();
        return this.connectionManager == null?false:this.connectionManager.isConnected();
    }

    /*XMPPConnection getConnection() {
        return this.connectionManager != null?this.connectionManager.getConnection():null;
    }*/

    public String getLoginUserName() {
        return this.currentUser.username;
    }

    /*
    void changePasswordXMPP(String var1) throws EaseMobException {
        if(this.connectionManager != null && this.connectionManager.isConnected() && this.connectionManager.isAuthentificated()) {
            AccountManager var2 = this.connectionManager.getConnection().getAccountManager();

            try {
                var2.changePassword(var1);
            } catch (Exception var4) {
                EMLog.e("Session", "changePasswordInBackground XMPP failed: usr:" + this.getLoginUserName() + ", newPassword:" + var1 + ", " + var4.toString());
                throw new EaseMobException(var4.getMessage());
            }
        } else {
            EMLog.e("Session", "changePasswordInBackground failed. xmppConnectionManager is null. ");
            throw new NetworkUnconnectedException();
        }
    }

    public void createAccountXMPP(String var1, String var2) throws EaseMobException {
        try {
            if(this.connectionManager != null) {
                this.connectionManager.disconnect();
            } else {
                this.connectionManager = new ConnectionManager();
            }

            this.connectionManager.onInit();
            this.connectionManager.connect();
            AccountManager var3 = this.connectionManager.getConnection().getAccountManager();
            var3.createAccount(var1, var2);
            this.connectionManager.disconnect();
            EMLog.d("Session", "created xmpp user:" + var1);
        } catch (Exception var4) {
            if(this.connectionManager != null) {
                this.connectionManager.disconnect();
            }

            var4.printStackTrace();
            throw new EaseMobException(var4.toString());
        }
    }*/

    void forceReconnect() {
        if(this.connectionManager != null) {
            this.connectionManager.forceReconnect();
        }

    }

    boolean reuse() {
        return this.connectionManager == null?false:this.connectionManager.reuse();
    }

    boolean disconnect() {
        EMLog.d("Session", "mannualy disconnect the connection");
        return this.connectionManager == null?false:this.connectionManager.disconnect();
    }

    boolean isFinished() {
        return this.connectionManager == null?true:this.connectionManager.isFinished();
    }

    public void createAccountRest(String var1, String var2) throws EaseMobException {
        if(TextUtils.isEmpty(var1)) {
            throw new EaseMobException("username is empty");
        } else if(TextUtils.isEmpty(var2)) {
            throw new EaseMobException("password is empty");
        } else if(TextUtils.isEmpty(ChatConfig.getInstance().APPKEY)) {
            throw new EaseMobException("appkey is not set");
        } else {
            String var3 = null;

            String var4;
            try {
                var4 = HttpClientConfig.getBaseUrlByAppKey() + "/users/";
                JSONObject var5 = new JSONObject();
                var5.put("username", var1);
                var5.put("password", var2);
                Pair var6 = HttpClient.getInstance().sendRequest(var4, (Map)null, var5.toString(), HttpClient.POST);
                var3 = (String)var6.second;
            } catch (JSONException var8) {
                throw new EaseMobException(-1000, var8.getMessage());
            } catch (Exception var9) {
                if(var9 instanceof EaseMobException) {
                    throw new EaseMobException(((EaseMobException)var9).getErrorCode(), var9.getMessage());
                }

                throw new EaseMobException(-998, var9.getMessage());
            }

            if(TextUtils.isEmpty(var3)) {
                throw new EaseMobException(-1007, "response result is null");
            } else if(var3.contains("error")) {
                var4 = "";
                String var10 = "";

                try {
                    JSONObject var11 = new JSONObject(var3);
                    var4 = var11.getString("error");
                    var10 = var11.getString("error_description");
                } catch (JSONException var7) {
                    var7.printStackTrace();
                    throw new EaseMobException(-1000, var7.getMessage());
                }

                if(var4.equalsIgnoreCase("duplicate_unique_property_exists")) {
                    throw new EaseMobException(-1015, "conflict");
                } else if(var4.equalsIgnoreCase("unauthorized")) {
                    throw new EaseMobException(-1021, "unauthorized:" + var10);
                } else {
                    throw new EaseMobException(-999, var10);
                }
            }
        }
    }
}

