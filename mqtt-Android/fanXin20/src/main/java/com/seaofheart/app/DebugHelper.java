package com.seaofheart.app;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.Pair;

import com.seaofheart.app.chat.ChatConfig;
import com.seaofheart.app.chat.ChatManager;
import com.seaofheart.app.chat.EMCloudOperationCallback;
import com.seaofheart.app.cloud.HttpClient;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.ZipUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DebugHelper {
    static final String UUID = "uuid";
    static final String TOKEN_ENTITY = "entities";
    private static final String TAG = "DebugHelper";

    public DebugHelper() {
    }

    public static void uploadLog(Context var0, final String var1, final CallBack var2) {
        (new Thread() {
            public void run() {
                try {
                    File var1x = com.easemob.util.EMLog.getLogRoot();
                    if(var1x == null || !var1x.exists()) {
                        com.easemob.util.EMLog.w("DebugHelper", "log root did not exist");
                        var2.onSuccess();
                        return;
                    }

                    com.easemob.util.EMLog.freeLogFiles();
                    File var2x = var1x.getParentFile();
                    File var3 = new File(var2x, "easemoblog.zip");
                    if(var3.exists()) {
                        var3.delete();
                        com.easemob.util.EMLog.d("EMChat", "zipFile was deleted!");
                    }

                    ZipUtils.zip(var1x, var3);
                    String var4 = "http://" + var1 + "/" + "easemob#logger".replaceFirst("#", "/") + "/chatfiles/";
                    HashMap var5 = new HashMap();
                    HttpClient.getInstance().uploadFile(var3.getAbsolutePath(), var4, var5, new EMCloudOperationCallback() {
                        public void onSuccess(String var1x) {
                            Log.i("DebugHelper", var1x);

                            try {
                                JSONObject var3 = new JSONObject(var1x);
                                JSONObject var2x = var3.getJSONArray("entities").getJSONObject(0);
                                String var4 = var2x.getString("uuid");
                                if(var4 == null) {
                                    var2.onError(5, "authentificate failed");
                                    return;
                                }

                                Date var5 = new Date();
                                SimpleDateFormat var6 = new SimpleDateFormat("yyyy-MM-dd");
                                String var7 = var6.format(var5);
                                JSONObject var8 = new JSONObject();
                                var8.put("sdk_version", Build.VERSION.RELEASE);
                                var8.put("os_version", Build.VERSION.RELEASE);
                                var8.put("model", Build.MODEL + ":" + Build.DEVICE + ":" + Build.PRODUCT);
                                var8.put("uploadDate", var7);
                                var8.put("login_username", ChatManager.getInstance().getCurrentUser());
                                var8.put("appkey", ChatConfig.getInstance().APPKEY);
                                var8.put("logfile_uuid", var4);
                                com.easemob.util.EMLog.i("DebugHelper", "post body :" + var8.toString());
                                String var9 = "http://" + var1 + "/" + "easemob#logger".replaceFirst("#", "/") + "/devicelogs/";
                                com.easemob.util.EMLog.i("DebugHelper", "start post uri : " + var9);
                                Pair var10 = HttpClient.getInstance().sendRequest(var9, (Map)null, var8.toString(), HttpClient.POST);
                                String var11 = (String)var10.second;
                                if(var11 != null) {
                                    com.easemob.util.EMLog.i("DebugHelper", var11);
                                    var2.onSuccess();
                                } else {
                                    var2.onError(5, "send post by uuid failed");
                                    com.easemob.util.EMLog.e("DebugHelper", "send post by uuid failed");
                                }
                            } catch (JSONException var12) {
                                var12.printStackTrace();
                                var2.onError(5, var12.getMessage());
                            } catch (EaseMobException var13) {
                                var13.printStackTrace();
                                var2.onError(5, var13.getMessage());
                            } catch (IOException var14) {
                                var14.printStackTrace();
                                var2.onError(5, var14.getMessage());
                            } catch (Exception var15) {
                                var15.printStackTrace();
                                var2.onError(-998, var15.getMessage());
                            }

                        }

                        public void onError(String var1x) {
                            var2.onError(5, var1x);
                        }

                        public void onProgress(int var1x) {
                            var2.onProgress(var1x, (String)null);
                        }
                    });
                } catch (IOException var6) {
                    var6.printStackTrace();
                    var2.onError(5, var6.getMessage());
                }

            }
        }).start();
    }
}

