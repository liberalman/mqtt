package com.seaofheart.app.analytics;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.chat.ChatConfig;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.DeviceUuidFactory;
import com.seaofheart.app.cloud.HttpClientManager;

import org.json.JSONObject;

import java.util.HashMap;

public class ActiveCollector {
    private static final String perf_actived = "actived";

    public ActiveCollector() {
    }

    public static String collectActiveInfo(Context var0) {
        JSONObject var1 = new JSONObject();

        try {
            var1.put("version", Build.VERSION.RELEASE);
            var1.put("manufacturer", Build.MANUFACTURER);
            var1.put("model", Build.MODEL);

            try {
                TelephonyManager var2 = (TelephonyManager)var0.getSystemService(Context.TELEPHONY_SERVICE);
                var1.put("imei", var2.getDeviceId());
                var1.put("operator", var2.getNetworkOperatorName());
            } catch (Exception var5) {
                if(var5 != null) {
                    EMLog.d("actived", var5.getMessage());
                }
            }

            var1.put("easemob.version", Chat.getInstance().getVersion());

            try {
                LocationManager var7 = (LocationManager)var0.getSystemService(Context.LOCATION_SERVICE);
                Location var3 = var7.getLastKnownLocation("gps");
                if(var3 == null) {
                    var3 = var7.getLastKnownLocation("network");
                }

                if(var3 != null) {
                    var1.put("loc.lat", var3.getLatitude());
                    var1.put("loc.lng", var3.getLongitude());
                } else {
                    EMLog.d("ana", "no last location info to use");
                }
            } catch (Exception var4) {
                if(var4 != null) {
                    EMLog.d("actived", var4.getMessage());
                }
            }

            DeviceUuidFactory var8 = new DeviceUuidFactory(var0);
            String var9 = var8.getDeviceUuid().toString();
            var1.put("token", var9);
        } catch (Exception var6) {
            if(var6 != null) {
                EMLog.d("actived", var6.getMessage());
            }
        }

        return var1.toString();
    }

    public static void sendActivePacket(final Context var0) {
        boolean var1 = PreferenceManager.getDefaultSharedPreferences(var0).getBoolean("actived", false);
        if(var1) {
            EMLog.d("init", "s");
        } else {
            try {
                EMLog.d("init", "d");
                (new Thread(new Runnable() {
                    public void run() {
                        StringBuilder var1 = new StringBuilder();
                        if(p.getInstance().l()) {
                            var1.append("https://");
                        } else {
                            var1.append("http://");
                        }

                        String var2 = p.getInstance().o();
                        if(var2.startsWith("http")) {
                            var1 = new StringBuilder(var2);
                        } else {
                            var1.append(var2);
                        }

                        var1.append("/");
                        var1.append(ChatConfig.getInstance().APPKEY.replaceFirst("#", "/"));
                        var1.append("/devices");
                        String var3 = null;

                        try {
                            String var4 = ActiveCollector.collectActiveInfo(var0);
                            HashMap var8 = new HashMap();
                            var3 = HttpClientManager.sendHttpRequest(var1.toString(), var8, var4, HttpClientManager.Method_POST);
                            if(var3.contains("uuid") || var3.contains("duplicate_unique_property_exists")) {
                                SharedPreferences.Editor var6 = PreferenceManager.getDefaultSharedPreferences(var0).edit();
                                var6.putBoolean("actived", true);
                                var6.commit();
                            }
                        } catch (Exception var7) {
                            if(var7.toString().contains("duplicate_unique")) {
                                SharedPreferences.Editor var5 = PreferenceManager.getDefaultSharedPreferences(var0).edit();
                                var5.putBoolean("actived", true);
                                var5.commit();
                            }
                        }

                    }
                })).start();
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        }
    }

    public void sendUninstallPacket() {
    }
}

