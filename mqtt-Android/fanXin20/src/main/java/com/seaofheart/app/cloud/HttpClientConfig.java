package com.seaofheart.app.cloud;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.text.TextUtils;

import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.chat.ChatConfig;
import com.seaofheart.app.chat.core.j;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;

import java.util.Map;

public class HttpClientConfig {
    private static final String EASEMOB_USERSERVER_DOMAIN_ID = "easemob.com";
    private static final String EASEMOB_PLATFORM = "Android";
    public static String EM_TIME_OUT_KEY = "em_timeout";
    public static int EM_DEFAULT_TIMEOUT = 30000;

    public HttpClientConfig() {
    }

    public static String getEaseMobUserServerDomainId() {
        return "easemob.com";
    }

    private static String getDefaultUserAgent() {
        StringBuffer var0 = new StringBuffer();
        var0.append("Easemob-SDK(");
        var0.append("Android");
        var0.append(") ");
        var0.append(Chat.getInstance().getVersion());
        return var0.toString();
    }

    public static DefaultHttpClient getDefaultHttpClient() {
        return getDefaultHttpClient(EM_DEFAULT_TIMEOUT);
    }

    public static DefaultHttpClient getDefaultHttpClient(int var0) {
        BasicHttpParams var1 = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(var1, var0);
        HttpConnectionParams.setSoTimeout(var1, 20000);
        HttpConnectionParams.setTcpNoDelay(var1, true);
        HttpProtocolParams.setUserAgent(var1, getDefaultUserAgent());
        SchemeRegistry var2 = new SchemeRegistry();
        PlainSocketFactory var3 = PlainSocketFactory.getSocketFactory();
        var2.register(new Scheme("http", var3, 80));
        var2.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ThreadSafeClientConnManager var4 = new ThreadSafeClientConnManager(var1, var2);
        DefaultHttpClient var5 = new DefaultHttpClient(var4, var1);
        var5.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        var5.setReuseStrategy(new DefaultConnectionReuseStrategy());
        return var5;
    }

    public static String getFileRemoteUrl(String var0) {
        if(var0.startsWith("http")) {
            return var0;
        } else {
            String var1 = getFileDirRemoteUrl();
            var1 = var1 + var0;
            return var1;
        }
    }

    public static String getFileDirRemoteUrl() {
        String var0 = getBaseUrlByAppKey();
        var0 = var0 + "/chatfiles/";
        return var0;
    }

    public static String getBaseUrlByAppKey() {
        j.Address var0 = j.getInstance().c();
        String var1 = "http://";
        if(var0.protocol != null && !var0.protocol.equals("") && var0.protocol.equals("https")) {
            var1 = "https://";
        }

        if(TextUtils.isDigitsOnly(var0.host.substring(0, var0.host.indexOf(46)))) {
            var1 = var1 + var0.host + ":" + var0.port;
        } else {
            var1 = var1 + var0.host;
        }

        if(!var1.endsWith("/")) {
            var1 = var1 + "/";
        }

        var1 = var1 + ChatConfig.getInstance().APPKEY.replaceFirst("#", "/");
        return var1;
    }

    public static int getTimeout(Map<String, String> var0) {
        int var1 = EM_DEFAULT_TIMEOUT;
        if(var0 != null && var0.get(EM_TIME_OUT_KEY) != null) {
            var1 = Integer.valueOf((String)var0.get(EM_TIME_OUT_KEY)).intValue();
            var0.remove(EM_TIME_OUT_KEY);
        }

        return var1;
    }
}
