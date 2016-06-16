package com.seaofheart.app.cloud;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.util.Pair;

import com.seaofheart.app.chat.EMCloudOperationCallback;
import com.easemob.chat.core.p;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.EMLog;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {
    private static final String TAG = "EMHttpClient";
    public static String GET = "GET";
    public static String POST = "POST";
    public static String PUT = "PUT";
    public static String DELETE = "DELETE";
    private static HttpClient instance = null;

    private HttpClient() {
    }

    public static synchronized HttpClient getInstance() {
        if(instance == null) {
            instance = new HttpClient();
        }

        return instance;
    }

    public Pair<Integer, String> sendRequest(String var1, Map<String, String> var2, String var3, String var4) throws IOException, EaseMobException {
        return HttpClientManager.sendRequest(var1, var2, var3, var4);
    }

    public HttpResponse httpExecute(String var1, Map<String, String> var2, String var3, String var4) throws KeyManagementException, UnrecoverableKeyException, ClientProtocolException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        return HttpClientManager.httpExecute(var1, var2, var3, var4);
    }

    public void downloadFile(final String var1, final String var2, final Map<String, String> var3, final EMCloudOperationCallback var4) {
        (new Thread() {
            public void run() {
                try {
                    (new HttpFileManager()).downloadFile(var1, var2, var3, var4);
                } catch (Exception var2x) {
                    if(var4 != null) {
                        var4.onError(var2x != null && var2x.getMessage() != null?var2x.getMessage():"failed to download the file : " + var1);
                    }
                }

            }
        }).start();
    }

    public void uploadFile(final String var1, final String var2, final Map<String, String> var3, final EMCloudOperationCallback var4) {
        EMLog.d("EMHttpClient", "upload file :  localFilePath : " + var1 + " remoteUrl : " + var2);
        Thread var5 = new Thread() {
            public void run() {
                try {
                    EMLog.d("EMHttpClient", "run HttpFileManager().uploadFile");
                    (new HttpFileManager()).uploadFile(var1, var2, p.e().y(), p.e().z(), var3, var4);
                } catch (Exception var2x) {
                    if(var4 != null) {
                        var4.onError(var2x != null && var2x.getMessage() != null?var2x.getMessage():"failed to upload the file : " + var2);
                    }
                }

            }
        };
        var5.start();
    }

    public Pair<Integer, String> sendRequestWithToken(String var1, String var2, String var3) throws EaseMobException {
        return HttpClientManager.sendRequestWithToken(var1, (Map)null, var2, var3);
    }

    public Pair<Integer, String> sendRequestWithToken(String var1, String var2, String var3, int var4) throws EaseMobException {
        HashMap var5 = new HashMap();
        var5.put(HttpClientConfig.EM_TIME_OUT_KEY, String.valueOf(var4));
        return HttpClientManager.sendRequestWithToken(var1, var5, var2, var3);
    }
}

