package com.seaofheart.app.cloud;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.text.TextUtils;

import com.seaofheart.app.analytics.PerformanceCollector;
import com.seaofheart.app.analytics.TimeTag;
import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.chat.core.j;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.NetUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import internal.org.apache.http.entity.mime.content.FileBody;
import internal.org.apache.http.entity.mime.content.StringBody;

public class HttpFileManager extends CloudFileManager {
    private static final long MAX_ALLOWED_FILE_SIZE = 10485760L;
    private long totalSize;
    private Context appContext;
    boolean tokenRetrieved = false;
    private static final int max_retry_times_on_connection_refused = 20;

    public HttpFileManager() {
        this.appContext = Chat.getInstance().getAppContext();
    }

    public HttpFileManager(Context var1, String var2) {
        this.appContext = var1.getApplicationContext();
    }

    public HttpFileManager(Context var1) {
        this.appContext = var1.getApplicationContext();
    }

    public boolean authorization() {
        return true;
    }

    private void sendFiletoServerHttp(String var1, String var2, String var3, String var4, Map<String, String> var5, CloudOperationCallback var6) {
        this.sendFiletoServerHttpWithCountDown(var1, var2, var3, var4, var5, var6, -1, false);
    }

    private void sendFiletoServerHttpWithCountDown(final String var1, final String var2, final String var3, final String var4, Map<String, String> var5, final CloudOperationCallback var6, final int var7, boolean var8) {
        EMLog.d("CloudFileManager", "sendFiletoServerHttpWithCountDown .....");
        File var9 = new File(var1);
        if(!var9.isFile()) {
            EMLog.e("CloudFileManager", "Source file doesn\'t exist");
            var6.onError("Source file doesn\'t exist");
        } else if(var9.length() > 10485760L) {
            var6.onError("file doesn\'t bigger than 10 M");
        } else {
            final Map var10 = HttpClientManager.addDomainToHeaders(var5);
            HttpResponse var11 = null;
            String var12 = com.easemob.cloud.HttpClientConfig.getFileRemoteUrl(var2);
            EMLog.d("CloudFileManager", " remote path url : " + var12 + " --countDown: " + var7);
            int var13 = com.easemob.cloud.HttpClientConfig.getTimeout(var10);
            DefaultHttpClient var14 = com.easemob.cloud.HttpClientConfig.getDefaultHttpClient(var13);

            try {
                HttpPost var15 = new HttpPost(var12);
                CustomMultiPartEntity var26 = new CustomMultiPartEntity(new CustomMultiPartEntity.ProgressListener() {
                    public void transferred(long var1) {
                        int var3 = (int)((float)var1 / (float)HttpFileManager.this.totalSize * 100.0F);
                        if(var3 != 100 && var6 != null) {
                            var6.onProgress(var3);
                        }

                    }
                });
                if(var3 != null) {
                    var26.addPart("app", new StringBody(var3));
                }

                if(var4 != null) {
                    var26.addPart("id", new StringBody(var4));
                }

                if(var10 != null) {
                    Iterator var30 = var10.entrySet().iterator();

                    while(var30.hasNext()) {
                        Map.Entry var28 = (Map.Entry)var30.next();
                        var15.addHeader((String)var28.getKey(), (String)var28.getValue());
                    }
                }

                String var27 = var2;
                String var29;
                if(var2.indexOf("/") > 0) {
                    var29 = var2.substring(0, var2.lastIndexOf("/"));
                    var27 = var2.substring(var2.lastIndexOf("/"));
                    var26.addPart("path", new StringBody(var29));
                }

                var29 = getMimeType(var9);
                EMLog.d("CloudFileManager", " remote file name : " + var27);
                var26.addPart("file", new FileBody(var9, var27, var29, "UTF-8"));
                this.totalSize = var26.getContentLength();
                var15.setEntity(var26);
                if(p.getInstance().Q()) {
                    HttpClientManager.checkAndProcessSSL(var12, var14);
                }

                TimeTag var31 = new TimeTag();
                var31.start();
                var11 = var14.execute(var15);
                var31.stop();
                if(var31.timeSpent() > 0L) {
                    PerformanceCollector.collectUploadFileTime(var31, this.totalSize, var12);
                }

                int var20 = var11.getStatusLine().getStatusCode();
                EMLog.d("CloudFileManager", "server responseCode:" + var20 + " localFilePath : " + var1);
                String var22;
                HttpEntity var32;
                switch(var20) {
                    case 200:
                        var6.onProgress(100);
                        var32 = var11.getEntity();
                        var22 = EntityUtils.toString(var32);
                        var6.onSuccess(var22);
                        return;
                    case 401:
                        long var21 = p.getInstance().C();
                        if(System.currentTimeMillis() - var21 <= 600000L) {
                            if(var6 != null) {
                                var6.onError("unauthorized file");
                            }

                            return;
                        } else if(this.tokenRetrieved) {
                            var6.onError("unauthorized file");
                            return;
                        } else {
                            String var23 = p.getInstance().B();
                            this.tokenRetrieved = true;
                            if(var23 == null) {
                                var6.onError("unauthorized token is null");
                                return;
                            }

                            var10.put("Authorization", "Bearer " + var23);
                            if(!var8) {
                                (new Thread() {
                                    public void run() {
                                        HttpFileManager.this.sendFiletoServerHttpWithCountDown(var1, var2, var3, var4, var10, var6, 3, true);
                                    }
                                }).start();
                            } else if(var7 > 0) {
                                //--var7; // 这个不能用了，下面用到的地方减一
                                (new Thread() {
                                    public void run() {
                                        HttpFileManager.this.sendFiletoServerHttpWithCountDown(var1, var2, var3, var4, var10, var6, var7 - 1, true);
                                    }
                                }).start();
                            }

                            return;
                        }
                    default:
                        var32 = var11.getEntity();
                        var22 = EntityUtils.toString(var32);
                        var22 = "Http response error : " + var20 + " error msg : " + var22;
                        EMLog.e("CloudFileManager", var22);
                        if(var6 != null) {
                            var6.onError(var22);
                        }

                }
            } catch (Exception var25) {
                String var16 = var25 != null && var25.getMessage() != null?var25.getMessage():"failed to upload the files";
                EMLog.e("CloudFileManager", "sendFiletoServerHttp:" + var16);
                if(p.getInstance().i()) {
                    boolean var17 = var25 != null && (var25 instanceof SocketTimeoutException || var25 instanceof ConnectTimeoutException);
                    if(NetUtils.hasNetwork(this.appContext) && var17) {
                        j.getInstance().g();
                    } else if(var16.toLowerCase().contains("refused") && NetUtils.hasNetwork(this.appContext) && j.getInstance().h()) {
                        final String var19;
                        j.Address var18;
                        if(!var8) {
                            var18 = j.getInstance().g();
                            var19 = HttpClientManager.getNewHost(var2, var18);
                            (new Thread() {
                                public void run() {
                                    HttpFileManager.this.sendFiletoServerHttpWithCountDown(var1, var19, var3, var4, var10, var6, 20, true);
                                }
                            }).start();
                            return;
                        }

                        if(var7 > 0) {
                            var18 = j.getInstance().g();
                            var19 = HttpClientManager.getNewHost(var12, var18);
                            //--var7; //这个不能用了
                            (new Thread() {
                                public void run() {
                                    HttpFileManager.this.sendFiletoServerHttpWithCountDown(var1, var19, var3, var4, var10, var6, var7 - 1, true);
                                }
                            }).start();
                            return;
                        }
                    }
                }

                if(var6 != null) {
                    var6.onError(var16);
                }

            }
        }
    }

    public static String getMimeType(File var0) {
        String var1 = var0.getName();
        return !var1.endsWith(".3gp") && !var1.endsWith(".amr")?(!var1.endsWith(".jpe") && !var1.endsWith(".jpeg") && !var1.endsWith(".jpg")?(var1.endsWith(".amr")?"audio/amr":(var1.endsWith(".mp4")?"video/mp4":"image/png")):"image/jpeg"):"audio/3gp";
    }

    public void uploadFileInBackground(final String var1, final String var2, final String var3, final String var4, final Map<String, String> var5, final CloudOperationCallback var6) {
        (new Thread() {
            public void run() {
                try {
                    HttpFileManager.this.sendFiletoServerHttp(var1, var2, var3, var4, var5, var6);
                } catch (Exception var2x) {
                    if(var2x != null && var2x.toString() != null) {
                        EMLog.e("CloudFileManager", var2x.toString());
                        var6.onError(var2x.toString());
                    } else {
                        var6.onError("failed to upload the file : " + var1 + " remote path : " + var2);
                    }
                }

            }
        }).start();
    }

    public void uploadFile(String var1, String var2, String var3, String var4, Map<String, String> var5, CloudOperationCallback var6) {
        try {
            this.sendFiletoServerHttp(var1, var2, var3, var4, var5, var6);
        } catch (Exception var8) {
            EMLog.e("CloudFileManager", "uploadFile error:" + var8.toString());
            var6.onError(var8.toString());
        }

    }

    public void downloadFile(String var1, String var2, String var3, Map<String, String> var4, CloudOperationCallback var5) {
        if(TextUtils.isEmpty(var1)) {
            if(var5 != null) {
                var5.onError("remotefilepath is null or empty");
            }

            EMLog.e("CloudFileManager", "remotefilepath is null or empty");
        } else {
            String var6 = HttpClientConfig.getFileRemoteUrl(var1);
            this.downloadFile(var6, var2, var4, var5);
        }

    }

    public void downloadFile(String var1, String var2, Map<String, String> var3, CloudOperationCallback var4) {
        try {
            this.downloadFileWithCountDown(var1, var2, var3, var4, 20);
        } catch (Exception var7) {
            String var6 = "failed to download file : " + var1;
            if(var7 != null && var7.toString() != null) {
                var6 = var7.toString();
            }

            if(var4 != null) {
                var4.onError(var6);
            }
        }

    }

    private void downloadFileWithCountDown(final String var1, final String var2, Map<String, String> var3, final CloudOperationCallback var4, final int var5) {
        if(var1 != null && var1.length() > 0) {
            final Map var6 = HttpClientManager.addDomainToHeaders(var3);
            String remoteUrl = HttpClientConfig.getFileRemoteUrl(var1);
            EMLog.d("CloudFileManager", "remoteUrl:" + remoteUrl + " localFilePath:" + var2);
            remoteUrl = this.processUrl(remoteUrl);
            EMLog.d("CloudFileManager", "download file: remote url : " + remoteUrl + " , local file : " + var2);
            File var7 = new File(var2);
            EMLog.d("CloudFileManager", "local exists:" + var7.exists());
            if(!var7.getParentFile().exists()) {
                var7.getParentFile().mkdirs();
            }

            TimeTag var8 = new TimeTag();
            var8.start();
            int var9 = com.easemob.cloud.HttpClientConfig.getTimeout(var6);
            DefaultHttpClient var10 = com.easemob.cloud.HttpClientConfig.getDefaultHttpClient(var9);

            try {
                HttpGet var11 = new HttpGet(remoteUrl);
                this.processHeaders(var11, var6);
                HttpClientManager.checkAndProcessSSL(remoteUrl, var10);
                HttpResponse var19 = var10.execute(var11);
                int var20 = var19.getStatusLine().getStatusCode();
                long var21;
                switch(var20) {
                    case 200:
                        var21 = this.onDownloadCompleted(var19, var4, var2);
                        if(var21 <= 0L) {
                            if(var4 != null) {
                                var4.onError("downloaded content size is zero!");
                            }

                            return;
                        }

                        var8.stop();
                        if(var8.timeSpent() > 0L) {
                            PerformanceCollector.collectDownloadFileTime(var8, var21, remoteUrl);
                        }

                        if(var4 != null) {
                            var4.onSuccess("download successfully");
                        }
                        break;
                    case 401:
                        var21 = p.getInstance().C();
                        if(System.currentTimeMillis() - var21 <= 600000L) {
                            if(var4 != null) {
                                var4.onError("unauthorized file");
                            }

                            return;
                        }

                        if(this.tokenRetrieved) {
                            if(var4 != null) {
                                var4.onError("unauthorized file");
                            }

                            return;
                        }

                        (new Thread() {
                            public void run() {
                                String var1x = p.getInstance().B();
                                if(var1x == null) {
                                    var4.onError("unauthorized token is null");
                                } else {
                                    HttpFileManager.this.tokenRetrieved = true;
                                    if(var6 != null) {
                                        var6.put("Authorization", "Bearer " + var1x);
                                        HttpFileManager.this.downloadFile(var1, var2, var6, var4); // var1 这里又出现了
                                    } else {
                                        HttpFileManager.this.tokenRetrieved = false;
                                        if(var4 != null) {
                                            var4.onError("unauthorized token is null");
                                        }
                                    }

                                }
                            }
                        }).start();
                        break;
                    default:
                        EMLog.e("CloudFileManager", "error response code is :" + var20);
                        if(var4 != null) {
                            var4.onError(String.valueOf(var20));
                        }
                }
            } catch (Exception var18) {
                String var12 = var18.getMessage();
                if(var12 == null) {
                    var12 = var18.toString();
                    if(var12 == null) {
                        var12 = "failed to download file";
                    }
                }

                if(p.getInstance().i()) {
                    boolean var13 = var18 != null && (var18 instanceof SocketTimeoutException || var18 instanceof ConnectTimeoutException);
                    if(NetUtils.hasNetwork(this.appContext) && var13) {
                        j.getInstance().g();
                    } else {
                        boolean var14 = var12.toLowerCase().contains("refused");
                        if(NetUtils.hasNetwork(this.appContext) && var14 && j.getInstance().h() && var5 > 0) {
                            j.Address var15 = j.getInstance().g();
                            final String var16 = HttpClientManager.getNewHost(remoteUrl, var15);
                            //--var5; //这个不能用，所以只好在下面使用的时候，直接减一了
                            (new Thread() {
                                public void run() {
                                    try {
                                        HttpFileManager.this.downloadFileWithCountDown(var16, var2, var6, var4, var5 - 1);
                                    } catch (Exception var2x) {
                                        if(var2x != null && var2x.toString() != null) {
                                            var4.onError(var2x.toString());
                                        } else {
                                            var4.onError("failed to download the file : " + var16);
                                        }
                                    }

                                }
                            }).start();
                            return;
                        }
                    }
                }

                EMLog.e("CloudFileManager", var12);
                if(var4 != null) {
                    var4.onError(var12);
                }
            }

        } else {
            var4.onError("invalid remoteUrl");
        }
    }

    private String processUrl(String var1) {
        if(var1.contains("+")) {
            var1 = var1.replaceAll("\\+", "%2B"); // var1 = var1.replaceAll("+", "%2B");
        }

        if(var1.contains("#")) {
            var1 = var1.replaceAll("#", "%23");
        }

        return var1;
    }

    private void processHeaders(HttpGet var1, Map<String, String> var2) {
        var1.addHeader("Authorization", "Bearer " + p.getInstance().A());
        var1.addHeader("Accept", "application/octet-stream");
        if(var2 != null) {
            Iterator var4 = var2.entrySet().iterator();

            while(var4.hasNext()) {
                Map.Entry var3 = (Map.Entry)var4.next();
                if(!((String)var3.getKey()).equals("Authorization") && !((String)var3.getKey()).equals("Accept")) {
                    var1.addHeader((String)var3.getKey(), (String)var3.getValue());
                }
            }
        }

    }

    private long onDownloadCompleted(HttpResponse var1, CloudOperationCallback var2, String var3) throws IOException, IllegalStateException {
        HttpEntity var4 = var1.getEntity();
        if(var4 == null) {
            return 0L;
        } else {
            InputStream var5 = null;
            FileOutputStream var6 = null;
            boolean var7 = false;
            int var8 = 0;
            long var9 = var4.getContentLength();

            try {
                var5 = var4.getContent();
            } catch (IllegalStateException var26) {
                var26.printStackTrace();
                throw var26;
            } catch (IOException var27) {
                var27.printStackTrace();
                throw var27;
            }

            File var11 = new File(var3);

            try {
                var6 = new FileOutputStream(var11);
            } catch (FileNotFoundException var25) {
                var25.printStackTrace();
                var5.close();
                throw var25;
            }

            int var12 = NetUtils.getDownloadBufSize(this.appContext);
            byte[] var13 = new byte[var12];
            long var14 = 0L;

            long var18;
            try {
                int var30;
                for(; (var30 = var5.read(var13)) != -1; var6.write(var13, 0, var30)) {
                    var14 += (long)var30;
                    int var16 = (int)(var14 * 100L / var9);
                    EMLog.d("HttpFileManager", String.valueOf(var16));
                    if(var16 == 100 || var16 > var8 + 5) {
                        var8 = var16;
                        if(var2 != null) {
                            var2.onProgress(var16);
                        }
                    }
                }

                var18 = var11.length();
            } catch (IOException var28) {
                var28.printStackTrace();
                throw var28;
            } finally {
                var6.close();
                var5.close();
            }

            return var18;
        }
    }

    public void deleteFileInBackground(final String var1, final String var2, String var3, final CloudOperationCallback var4) {
        Thread var5 = new Thread() {
            public void run() {
                HttpURLConnection var1x = null;
                DataOutputStream var2x = null;
                String var3 = "\r\n";
                String var4x = "--";
                String var5 = "*****";
                String var6 = "";
                var6 = HttpClientConfig.getFileRemoteUrl(var1);

                try {
                    URL var7 = new URL(var6);
                    var1x = (HttpURLConnection)var7.openConnection();
                    var1x.setDoInput(true);
                    var1x.setDoOutput(true);
                    var1x.setUseCaches(false);
                    var1x.setRequestMethod("POST");
                    var1x.setRequestProperty("Connection", "Keep-Alive");
                    var1x.setRequestProperty("ENCTYPE", "multipart/form-data");
                    var1x.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + var5);
                    var1x.setRequestProperty("file", var1);
                    var2x = new DataOutputStream(var1x.getOutputStream());
                    var2x.writeBytes(var4x + var5 + var3);
                    if(var2 != null) {
                        var2x.writeBytes("Content-Disposition: form-data; name=\"app\"" + var3 + var3);
                        var2x.writeBytes(var2 + var3);
                        var2x.writeBytes(var4x + var5 + var3);
                    }

                    var2x.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + var1 + "\"" + var3);
                    var2x.writeBytes(var3);
                    BufferedReader var8 = new BufferedReader(new InputStreamReader(var1x.getInputStream()));

                    String var9;
                    while((var9 = var8.readLine()) != null) {
                        EMLog.d("CloudFileManager", "RESULT Message: " + var9);
                    }

                    var8.close();
                    var2x.close();
                    var1x.disconnect();
                    if(var4 != null) {
                        var4.onSuccess((String)null);
                    }
                } catch (Exception var10) {
                    var10.printStackTrace();
                    if(var4 != null) {
                        var4.onError(var10.toString());
                    }
                }

            }
        };
        var5.start();
    }
}

