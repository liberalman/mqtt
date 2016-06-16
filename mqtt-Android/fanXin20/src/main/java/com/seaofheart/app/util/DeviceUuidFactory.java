package com.seaofheart.app.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;

public class DeviceUuidFactory {
    protected static final String PREFS_FILE = "device_id.xml";
    protected static final String PREFS_DEVICE_ID = "device_id";
    protected static UUID uuid;

    public DeviceUuidFactory(Context var1) {
        if(uuid == null) {
            Class var2 = DeviceUuidFactory.class;
            synchronized(DeviceUuidFactory.class) {
                if(uuid == null) {
                    SharedPreferences var3 = var1.getSharedPreferences("device_id.xml", 0);
                    String var4 = var3.getString("device_id", (String)null);
                    if(var4 != null) {
                        uuid = UUID.fromString(var4);
                    } else {
                        String var5 = Settings.Secure.getString(var1.getContentResolver(), "android_id");

                        try {
                            if(!"9774d56d682e549c".equals(var5)) {
                                uuid = UUID.nameUUIDFromBytes(var5.getBytes("utf8"));
                            } else {
                                String var6 = ((TelephonyManager)var1.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                                uuid = var6 != null?UUID.nameUUIDFromBytes(var6.getBytes("utf8")):this.generateDeviceUuid(var1);
                            }
                        } catch (UnsupportedEncodingException var7) {
                            throw new RuntimeException(var7);
                        }

                        var3.edit().putString("device_id", uuid.toString()).commit();
                    }
                }
            }
        }

    }

    private UUID generateDeviceUuid(Context var1) {
        String var2 = Build.BOARD + Build.BRAND + Build.CPU_ABI + Build.DEVICE + Build.DISPLAY + Build.FINGERPRINT + Build.HOST + Build.ID + Build.MANUFACTURER + Build.MODEL + Build.PRODUCT + Build.TAGS + Build.TYPE + Build.USER;
        TelephonyManager var3 = (TelephonyManager)var1.getSystemService(Context.TELEPHONY_SERVICE);
        String var4 = var3.getDeviceId();
        String var5 = Settings.Secure.getString(var1.getContentResolver(), "android_id");
        WifiManager var6 = (WifiManager)var1.getSystemService(Context.WIFI_SERVICE);
        String var7 = var6.getConnectionInfo().getMacAddress();
        if(isEmpty(var4) && isEmpty(var5) && isEmpty(var7)) {
            return UUID.randomUUID();
        } else {
            String var8 = var2.toString() + var4 + var5 + var7;
            return UUID.nameUUIDFromBytes(var8.getBytes());
        }
    }

    public UUID getDeviceUuid() {
        return uuid;
    }

    private static boolean isEmpty(Object var0) {
        return var0 == null?true:(var0 instanceof String && ((String)var0).trim().length() == 0?true:(var0 instanceof Map ?((Map)var0).isEmpty():false));
    }
}

