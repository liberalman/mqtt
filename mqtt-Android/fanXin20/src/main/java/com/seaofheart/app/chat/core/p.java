package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.text.TextUtils;
import android.util.Pair;

import com.seaofheart.app.analytics.LoginCollector;
import com.seaofheart.app.analytics.TimeTag;
import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.cloud.HttpClientConfig;
import com.seaofheart.app.exceptions.AuthenticationException;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.CryptoUtils;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.chat.ChatConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class p { // class p
    private static final String TAG = p.class.getName(); // f
    static int a = 443; // 443端口
    private static final String g = "access_token";
    private String userId = null; // h
    private String password = null; // i
    private String pjs = null; // j
    private long savedTime = 0L; // k
    private boolean l = true;
    private String m = null;
    private String n = null;
    private static p instance = null;
    //private static final String p = "im1.easemob.com";
    private static final String q = "a1.easemob.com";
    private static final String r = "im1.vip1.easemob.com";
    private static final String s = "a1.vip1.easemob.com";
    private String t = "im1.easemob.com";
    private String u = "a1.easemob.com";
    //private static final String v = "im1.sandbox.easemob.com";
    private static final String w = "a1.sdb.easemob.com";
    private String x = "im1.sandbox.easemob.com";
    private String y = "a1.sdb.easemob.com";
    private static final String z = "im1.dev.easemob.com";
    private static final String A = "a1.dev.easemob.com";
    private String B = "im1.dev.easemob.com";
    private String C = "a1.dev.easemob.com";
    private static String D = "121.41.105.183";
    private static int E = 3478;
    List<p.classc> bbb = new ArrayList(); // b
    private CryptoUtils F = null;
    ChatConfig.EnvMode envMode; // c
    ChatConfig.SDKMode sdkMode; // d
    boolean e;
    private boolean G;
    private boolean H;
    private boolean I;
    private static final String J = "www.easemob.com";
    private static String[] K = new String[]{"Hy+xe9oDskKv5ZSkV4gLlCEW+t5gJOlzVd2oYYSJ9PY=", "6UJPCvc50DcJzJPQrh9GTxzLx7ExBUk/d/QSveCDBHA=", "IW07cwaTAhAm741v3TDuuvW/i8SGwkKPuxFbXhtyegk=", "MxmVut7Ui09MCvgOTcmgB+cDXhq+g0vPxG5Sz+OVkfI=", "Ok6j+A2TySWNmuZms7cji8eTdxYMoxuugbsghZT5Oss=", "f8K5HQ82hreMKpawCmtAikMcvoTfGm/pSPtHgwUvsPk=", "ZrJ41xgzvmNvhKDz7ZhTaRzLx7ExBUk/d/QSveCDBHA="};
    private String L;
    private boolean M;
    private boolean N;
    private p.b O;
    private String P;
    private p.a Q;

    public boolean a() {
        return this.M;
    }

    public void a(boolean var1) {
        this.M = var1;
        if(var1) {
            this.a(p.b.a);
        }

    }

    public boolean b() {
        return this.N;
    }

    public void b(boolean var1) {
        this.N = var1;
        if(var1) {
            this.a(p.b.b);
        }

    }

    public void a(p.b var1) {
        this.O = var1;
    }

    public p.b c() {
        return this.O;
    }

    public String d() {
        return this.P;
    }

    public void a(String var1) {
        this.P = var1;
    }

    private p() {
        this.envMode = ChatConfig.EnvMode.EMProductMode;
        this.sdkMode = ChatConfig.SDKMode.EMChatMode;
        this.e = false;
        this.G = false;
        this.H = false;
        this.I = false;
        this.L = "2.2.5";
        this.M = false;
        this.N = false;
        this.P = null;
        p.classc var1 = new p.classc();
        var1.a = D;
        var1.b = E;
        this.bbb.add(var1);
        this.c(true);
        this.F = new CryptoUtils();
        this.F.initAES();
    }

    public static synchronized p getInstance() {
        if(instance == null) {
            instance = new p();
        }

        return instance;
    }

    public void a(p.a var1) {
        this.Q = var1;
    }

    public p.a f() {
        return this.Q;
    }

    public String g() {
        return this.L;
    }

    public void c(boolean var1) {
        this.d(var1);
        this.e(var1);
        this.f(var1);
    }

    public void d(boolean var1) {
        this.G = var1;
    }

    public boolean h() {
        return this.G;
    }

    public void e(boolean var1) {
        this.H = var1;
    }

    public boolean i() {
        return this.H;
    }

    public void f(boolean var1) {
        this.I = var1;
    }

    public boolean j() {
        return this.I;
    }

    public String k() {
        return this.n()?"im1.vip1.easemob.com":this.t;
    }

    public boolean l() {
        return false;
    }

    public void g(boolean var1) {
        this.e = var1;
    }

    public boolean m() {
        return this.e;
    }

    boolean n() {
        String[] var4 = K;
        int var3 = K.length;

        for(int var2 = 0; var2 < var3; ++var2) {
            String var1 = var4[var2];

            try {
                if(this.F.decryptBase64String(var1).equals(ChatConfig.getInstance().APPKEY)) {
                    return true;
                }
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }

        return false;
    }

    public String o() {
        return this.n()?"a1.vip1.easemob.com":this.u;
    }

    public String p() {
        return "www.easemob.com";
    }

    public void b(String var1) {
        this.t = var1;
    }

    public void c(String var1) {
        this.u = var1;
    }

    public String q() {
        return this.x;
    }

    public String r() {
        return this.y;
    }

    public String s() {
        return this.B;
    }

    public String t() {
        return this.C;
    }

    public String u() {
        return "im1.easemob.com";
    }

    public String v() {
        return "a1.easemob.com";
    }

    public void a(int var1) {
        a = var1;
    }

    public void a(ChatConfig.EnvMode var1) {
        this.envMode = var1;
        if(this.envMode == ChatConfig.EnvMode.EMSandboxMode) {
            this.t = this.x;
            this.u = this.y;
            this.c(false);
        } else if(this.envMode == ChatConfig.EnvMode.EMDevMode) {
            this.t = this.B;
            this.u = this.C;
            this.c(true);
        } else {
            this.t = "im1.easemob.com";
            this.u = "a1.easemob.com";
            this.c(true);
        }

    }

    public ChatConfig.EnvMode w() {
        return this.envMode;
    }

    public void a(ChatConfig.SDKMode var1) {
        this.sdkMode = var1;
    }

    public ChatConfig.SDKMode x() {
        return this.sdkMode;
    }

    public void d(String var1) {
        this.m = var1;
    }

    public String y() {
        return this.m;
    }

    public void e(String var1) {
        this.n = var1;
    }

    public String z() {
        return this.n;
    }

    public synchronized String login(String hxid, String password) throws EaseMobException, AuthenticationException, JSONException, IOException {
        this.userId = hxid;
        this.password = password;
        p.token var3 = DBManager.getInstance().getToken(hxid);
        long var4 = System.currentTimeMillis();
        if(var3 != null && var3.getValue() != null) {
            this.pjs = var3.getValue();
            this.savedTime = var3.getSavedTime();
        } else {
            this.pjs = null;
        }

        if(this.pjs == null || var4 - this.savedTime > 561600000L) {
            StringBuilder var6 = new StringBuilder();
            var6.append(HttpClientConfig.getBaseUrlByAppKey());
            var6.append("/token");
            JSONObject var7 = new JSONObject();

            try {
                var7.put("grant_type", "password");
                var7.put("username", hxid);
                var7.put("password", this.password);
            } catch (JSONException var14) {
                var14.printStackTrace();
            }

            TimeTag var8 = new TimeTag();
            var8.start();
            EMLog.d(TAG, "try to retrieve token : " + var6.toString());
            Pair var9 = null;
            HashMap var10 = new HashMap();
            var10.put(HttpClientConfig.EM_TIME_OUT_KEY, String.valueOf(20000));
            var9 = HttpClient.getInstance().sendRequest(var6.toString(), var10, var7.toString(), HttpClient.POST);
            LoginCollector.collectRetrieveTokenTime(var8.stop());
            if(var9 == null) {
                throw new EaseMobException(-999, "failed to retrieve token");
            }

            if(((Integer)var9.first).intValue() != 200) {
                if(((Integer)var9.first).intValue() != 401 && ((Integer)var9.first).intValue() != 400 && ((Integer)var9.first).intValue() != 404) {
                    throw new EaseMobException(-999, "failed to retrieve token with error code : " + var9.first);
                }

                throw new AuthenticationException("invalid user or password!");
            }

            if(TextUtils.isEmpty((CharSequence)var9.second)) {
                EMLog.d(TAG, "return code is ok, but content is empty!");
                throw new EaseMobException(-999, "failed to retrieve token, the content is empty!");
            }

            JSONObject var11 = null;

            try {
                var11 = new JSONObject((String)var9.second);
                this.pjs = var11.getString("access_token");
            } catch (Exception var13) {
                EMLog.d(TAG, "accesstoken:" + (String)var9.second);
                throw new EaseMobException(-999, "wrong content is returned : " + (String)var9.second);
            }

            this.savedTime = System.currentTimeMillis();
            if(var3 == null) {
                var3 = new p.token();
            }

            var3.setValue(this.pjs).setSavedTime(this.savedTime);
            DBManager.getInstance().setToken(hxid, var3);
            EMLog.d(TAG, "accesstoken : " + (String)var9.second);
        }

        return this.pjs;
    }

    public synchronized String A() {
        if(this.userId != null && this.password != null) {
            String var1 = null;

            try {
                var1 = this.login(this.userId, this.password);
            } catch (Exception var3) {
                var3.printStackTrace();
                EMLog.d(TAG, var3.getMessage());
            }

            return var1;
        } else {
            return null;
        }
    }

    public String B() {
        getInstance().D();
        String var1 = null;
        int var2 = 3;

        while(var2 > 0) {
            var1 = this.A();
            if(var1 != null) {
                break;
            }

            try {
                Thread.sleep(2000L);
                --var2;
            } catch (InterruptedException var4) {
                var4.printStackTrace();
            }
        }

        return var1;
    }

    public long C() {
        return this.savedTime;
    }

    public void D() {
        p.token var1 = new p.token();
        var1.setSavedTime(0L);
        var1.setValue("");
        DBManager.getInstance().setToken(this.userId, var1);
    }

    public void h(boolean var1) {
        this.l = var1;
    }

    public boolean E() {
        return this.l;
    }

    public String f(String var1) {
        String var2 = "";

        try {
            InputStreamReader var3 = new InputStreamReader(Chat.getInstance().getAppContext().getResources().getAssets().open(var1));
            BufferedReader var4 = new BufferedReader(var3);

            for(String var5 = ""; (var5 = var4.readLine()) != null; var2 = var2 + var5) {
                ;
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return var2;
    }

    public void g(String var1) {
        v.a().a(var1);
    }

    public void h(String var1) {
        v.a().b(var1);
    }

    public void a(long var1) {
        v.a().a(var1);
    }

    public void b(long var1) {
        v.a().b(var1);
    }

    public long F() {
        return v.a().b();
    }

    public String G() {
        return v.a().c();
    }

    public String H() {
        return v.a().d();
    }

    public long I() {
        return v.a().e();
    }

    public boolean J() {
        return this.I() != -1L;
    }

    public boolean K() {
        return v.a().f();
    }

    public long L() {
        return v.a().g();
    }

    public void c(long var1) {
        v.a().c(var1);
    }

    public void M() {
        v.a().h();
    }

    public String i(String var1) {
        return HttpClientConfig.getFileRemoteUrl(var1);
    }

    public String N() {
        return HttpClientConfig.getFileDirRemoteUrl();
    }

    public String O() {
        return HttpClientConfig.getBaseUrlByAppKey();
    }

    public void a(List<p.classc> var1) {
    }

    public List<p.classc> P() {
        return this.bbb;
    }

    public boolean Q() {
        j.Address var1 = j.getInstance().c();
        return var1.protocol.equals("https");
    }

    public com.seaofheart.app.chat.core.v R() {
        return v.a();
    }

    public String S() {
        return v.a().m();
    }

    public void j(String var1) {
        v.a().d(var1);
    }

    public static class a {
        public String a;
        public String b;

        public a(String var1, String var2) {
            this.a = var1;
            this.b = var2;
        }
    }

    public static enum b {
        a,
        b;

        private b() {
        }
    }

    public static class classc { // class c
        public String a = null;
        public int b = -1;
        public p.classc.Ca c;

        public classc() {
            this.c = p.classc.Ca.a;
        }

        public static enum Ca {
            a,
            b;

            private Ca() {
            }
        }
    }

    public static class token { // class d
        String value;
        long savedTime;

        public token() {
        }

        public token(String var1, long var2) {
            this.value = var1;
            this.savedTime = var2;
        }

        public String getValue() {
            if(this.savedTime <= 0L) {
                this.value = null;
            }

            return this.value;
        }

        public p.token setValue(String var1) {
            this.value = var1;
            return this;
        }

        public long getSavedTime() {
            return this.savedTime;
        }

        public p.token setSavedTime(long var1) {
            this.savedTime = var1;
            return this;
        }
    }
}

