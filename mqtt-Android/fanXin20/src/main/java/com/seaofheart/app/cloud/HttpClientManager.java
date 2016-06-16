package com.seaofheart.app.cloud;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.text.TextUtils;
import android.util.Pair;

import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.chat.core.j;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.exceptions.NetworkUnconnectedException;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.NetUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpClientManager {
    private static final String TAG = "HttpClientManager";
    public static String Method_GET = "GET";
    public static String Method_POST = "POST";
    public static String Method_PUT = "PUT";
    public static String Method_DELETE = "DELETE";
    private static final int MAX_RETRIES_TIMES_ON_CONNECTION_REFUSED = 20;
    private static volatile long retrivedTokenTime = 0L;
    private static final int retriveInterval = 120000;
    private static volatile boolean isRetring = false;

    public HttpClientManager() {
    }

    public static String sendHttpRequest(String var0, Map<String, String> var1, String var2, String var3) throws EaseMobException, IOException {
        String var4 = null;
        IOException var5 = null;
        EaseMobException var6 = null;
        String var7 = var0;
        Object var8 = null;

        for(int var9 = 0; var9 < 20; ++var9) {
            EMLog.d("HttpClientManager", "try send request, request url: " + var0 + " with number: " + var9);
            var5 = null;
            var6 = null;
            Object var10 = null;

            try {
                var4 = sendHttpRequestWithCountDown(var7, var1, var2, var3);
            } catch (IOException var15) {
                var5 = var15;
                var10 = var15;
            } catch (EaseMobException var16) {
                var6 = var16;
                var10 = var16;
            }

            String var11 = "";
            if(var10 != null && ((Exception)var10).getMessage() != null) {
                var11 = ((Exception)var10).getMessage();
            }

            if(!p.getInstance().i() || !j.getInstance().h()) {
                break;
            }

            boolean var12 = var5 != null && (var5 instanceof SocketTimeoutException || var5 instanceof ConnectTimeoutException);
            boolean var13 = var12 || var11.toLowerCase().contains("refused");
            if(!NetUtils.hasNetwork(Chat.getInstance().getAppContext()) || !var13) {
                break;
            }

            j.Address var14 = j.getInstance().g();
            if(var12 || var8 != null && !TextUtils.isEmpty(((j.Address)var8).host) && ((j.Address)var8).host.equals(var14.host)) {
                break;
            }

            var7 = getNewHost(var0, var14);
        }

        if(var5 != null) {
            throw var5;
        } else if(var6 != null) {
            throw var6;
        } else {
            return var4;
        }
    }

    public static Pair<Integer, String> sendRequestWithToken(String var0, Map<String, String> var1, String var2, String var3) throws EaseMobException {
        if(var1 == null) {
            var1 = new HashMap();
        }

        ((Map)var1).put("Authorization", "Bearer " + p.getInstance().A());

        try {
            return sendHttpRequestWithRetryToken(var0, (Map)var1, var2, var3);
        } catch (IOException var6) {
            String var5 = " send request : " + var0 + " failed!";
            if(var6 != null && var6.toString() != null) {
                var5 = var6.toString();
            }

            EMLog.d("HttpClientManager", var5);
            throw new EaseMobException(-998, var5);
        }
    }

    static Pair<Integer, String> sendHttpRequestWithRetryToken(String var0, Map<String, String> var1, String var2, String var3) throws EaseMobException, IOException {
        Pair var4 = sendRequest(var0, var1, var2, var3);
        if(var4 != null && ((Integer)var4.first).intValue() == 401) {
            String var5 = null;
            long var6 = System.currentTimeMillis();
            if(var6 - retrivedTokenTime > 120000L && !isRetring) {
                isRetring = true;
                var5 = p.getInstance().B();
                isRetring = false;
                retrivedTokenTime = System.currentTimeMillis();
                if(var5 != null) {
                    var1.put("Authorization", "Bearer " + var5);
                    var4 = sendRequest(var0, var1, var2, var3);
                }
            }
        }

        return var4;
    }

    public static Pair<Integer, String> sendRequest(String var0, Map<String, String> var1, String var2, String var3) throws IOException, EaseMobException {
        Pair var4 = null;
        IOException var5 = null;
        EaseMobException var6 = null;
        String var7 = var0;

        for(int var8 = 0; var8 < 20; ++var8) {
            EMLog.d("HttpClientManager", "try send request, request url: " + var0 + " with number: " + var8);
            var5 = null;
            var6 = null;
            Object var9 = null;

            try {
                HttpResponse var10 = sendRequestWithCountDown(var7, var1, var2, var3);
                HttpEntity var11 = var10.getEntity();
                if(var11 != null) {
                    String var12 = EntityUtils.toString(var11, "UTF-8");
                    var4 = new Pair(Integer.valueOf(var10.getStatusLine().getStatusCode()), var12);
                }
            } catch (IOException var14) {
                var5 = var14;
                var9 = var14;
            } catch (EaseMobException var15) {
                var6 = var15;
                var9 = var15;
            }

            String var17 = "failed to send request, request url: " + var0;
            if(var9 != null) {
                if(((Exception)var9).getMessage() != null) {
                    var17 = ((Exception)var9).getMessage();
                } else if(((Exception)var9).toString() != null) {
                    var17 = ((Exception)var9).toString();
                }
            }

            if(!p.getInstance().i() || !j.getInstance().h()) {
                break;
            }

            boolean var16 = var5 != null && (var5 instanceof SocketTimeoutException || var5 instanceof ConnectTimeoutException);
            boolean var18 = var16 || var17.toLowerCase().contains("refused");
            if(!NetUtils.hasNetwork(Chat.getInstance().getAppContext()) || !var18) {
                break;
            }

            j.Address var13 = j.getInstance().g();
            if(var16) {
                break;
            }

            var7 = getNewHost(var0, var13);
        }

        if(var5 != null) {
            throw var5;
        } else if(var6 != null) {
            throw var6;
        } else {
            return var4;
        }
    }

    private static HttpResponse sendRequestWithCountDown(String var0, Map<String, String> var1, String var2, String var3) throws EaseMobException, IOException {
        HttpResponse var4 = null;

        try {
            var4 = getHttpResponse(var0, var1, var2, var3);
            return var4;
        } catch (IOException var7) {
            throw var7;
        } catch (Exception var8) {
            if(var8 != null) {
                var8.printStackTrace();
            }

            String var6 = "http request failed : " + var0;
            if(var8 != null && var8.toString() != null) {
                var6 = var8.toString();
            }

            if(var6.contains("Unable to resolve host")) {
                throw new NetworkUnconnectedException(-1001, "EMNetworkUnconnectedException:Unable to resolve host");
            } else {
                throw new EaseMobException(-1003, var6);
            }
        }
    }

    private static String sendHttpRequestWithCountDown(String var0, Map<String, String> var1, String var2, String var3) throws EaseMobException, IOException {
        String var4 = null;

        try {
            HttpResponse var5 = getHttpResponse(var0, var1, var2, var3);
            HttpEntity var9 = var5.getEntity();
            if(var9 != null) {
                var4 = EntityUtils.toString(var9, "UTF-8");
            }

            return var4;
        } catch (IOException var7) {
            throw var7;
        } catch (Exception var8) {
            if(var8 != null) {
                var8.printStackTrace();
            }

            String var6 = "http request failed : " + var0;
            if(var8 != null && var8.toString() != null) {
                var6 = var8.toString();
            }

            if(var6.contains("Unable to resolve host")) {
                throw new NetworkUnconnectedException(-1001, "EMNetworkUnconnectedException:Unable to resolve host");
            } else {
                throw new EaseMobException(-1003, var6);
            }
        }
    }

    static String getNewHost(String var0, j.Address var1) {
        String var2 = var1.host;
        int var3 = var1.port;
        String var4 = var1.protocol;
        String var5 = var4 + "://" + var2 + ":" + var3;
        String var6 = var0.substring(var0.indexOf("/", 8));
        String var7 = var5 + var6;
        return var7;
    }

    static Map<String, String> addDomainToHeaders(Map<String, String> var0) {
        if(p.getInstance().i()) {
            j.Address var1 = j.getInstance().c();
            if(var1 != null && var1.dnsHost != null && var1.protocol != null && var1.protocol.contains("https") && var1.dnsHost.domain != null && !var1.dnsHost.domain.trim().equals("")) {
                if(var0 == null) {
                    var0 = new HashMap();
                }

                ((Map)var0).put("Host", var1.dnsHost.domain);
            }
        }

        return (Map)var0;
    }

    static void checkAndProcessSSL(String var0, DefaultHttpClient var1) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, KeyManagementException, UnrecoverableKeyException {
    }

    public static HttpResponse getHttpResponse(String var0, Map<String, String> var1, String var2, String var3) throws KeyStoreException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {
        var1 = addDomainToHeaders(var1);
        return httpExecute(var0, var1, var2, var3);
    }

    public static HttpResponse httpExecute(String var0, Map<String, String> var1, String var2, String var3) throws ClientProtocolException, IOException, KeyStoreException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException {
        HttpResponse var4 = null;
        int var5 = com.easemob.cloud.HttpClientConfig.getTimeout(var1);
        DefaultHttpClient var6 = com.easemob.cloud.HttpClientConfig.getDefaultHttpClient(var5);
        Object var7 = null;
        if(var3.equals(Method_POST)) {
            HttpPost var8 = new HttpPost(var0);
            var8.setEntity(new StringEntity(var2, "UTF-8"));
            var7 = var8;
        } else if(var3.equals(Method_PUT)) {
            HttpPut var11 = new HttpPut(var0);
            var11.setEntity(new StringEntity(var2, "UTF-8"));
            var7 = var11;
        } else if(var3.equals(Method_GET)) {
            var7 = new HttpGet(var0);
        } else if(var3.equals(Method_DELETE)) {
            var7 = new HttpDelete(var0);
        }

        if(var7 != null) {
            if(var1 != null) {
                Iterator var9 = var1.entrySet().iterator();

                while(var9.hasNext()) {
                    Map.Entry var10 = (Map.Entry)var9.next();
                    ((HttpRequestBase)var7).setHeader((String)var10.getKey(), (String)var10.getValue());
                }
            }

            var4 = var6.execute((HttpUriRequest)var7);
        }

        return var4;
    }
}

