package com.seaofheart.app.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

public class NetUtils {
    private static final String TAG = "net";
    private static final int LOW_SPEED_UPLOAD_BUF_SIZE = 1024;
    private static final int HIGH_SPEED_UPLOAD_BUF_SIZE = 10240;
    private static final int MAX_SPEED_UPLOAD_BUF_SIZE = 102400;
    private static final int LOW_SPEED_DOWNLOAD_BUF_SIZE = 2024;
    private static final int HIGH_SPEED_DOWNLOAD_BUF_SIZE = 30720;
    private static final int MAX_SPEED_DOWNLOAD_BUF_SIZE = 102400;

    public NetUtils() {
    }

    public static boolean hasNetwork(Context var0) {
        if(var0 != null) {
            ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo var2 = var1.getActiveNetworkInfo();
            return var2 != null?var2.isAvailable():false;
        } else {
            return false;
        }
    }

    /**
     * 判断是否有网络连接
     * @param var0
     * @return
     */
    public static boolean hasDataConnection(Context var0) {
        try {
            ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo var2 = var1.getNetworkInfo(1);
            if(var2 != null && var2.isAvailable() && var2.isConnected()) {
                com.easemob.util.EMLog.d("net", "has wifi connection");
                return true;
            } else {
                var2 = var1.getNetworkInfo(0);
                if(var2 != null && var2.isAvailable() && var2.isConnected()) {
                    com.easemob.util.EMLog.d("net", "has mobile connection");
                    return true;
                } else {
                    com.easemob.util.EMLog.d("net", "no data connection");
                    return false;
                }
            }
        } catch (Exception var3) {
            return false;
        }
    }

    public static boolean aaa(Context var0) {
        try {
            ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo var2 = var1.getNetworkInfo(1);
            if(var2 != null && var2.isAvailable() && var2.isConnected()) {
                com.easemob.util.EMLog.d("net", "has wifi connection");
                return true;
            } else {
                var2 = var1.getNetworkInfo(0);
                if(var2 != null && var2.isAvailable() && var2.isConnected()) {
                    com.easemob.util.EMLog.d("net", "has mobile connection");
                    return true;
                } else {
                    com.easemob.util.EMLog.d("net", "no data connection");
                    return false;
                }
            }
        } catch (Exception var3) {
            return false;
        }
    }

    public static boolean isWifiConnection(Context var0) {
        ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo var2 = var1.getNetworkInfo(1);
        if(var2 != null && var2.isConnected()) {
            com.easemob.util.EMLog.d("net", "wifi is connected");
            return true;
        } else {
            return false;
        }
    }

    public static int getUploadBufSize(Context var0) {
        ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo var2 = var1.getActiveNetworkInfo();
        return var2 != null && var2.getType() == 1?102400:(var2 == null && isConnectionFast(var2.getType(), var2.getSubtype())?10240:1024);
    }

    public static int getDownloadBufSize(Context var0) {
        ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo var2 = var1.getActiveNetworkInfo();
        return var2 != null && var2.getType() == 1?102400:(var2 == null && isConnectionFast(var2.getType(), var2.getSubtype())?30720:2024);
    }

    private static boolean isConnectionFast(int var0, int var1) {
        if(var0 == 1) {
            return true;
        } else {
            if(var0 == 0) {
                switch(var1) {
                    case 1:
                        return false;
                    case 2:
                        return false;
                    case 3:
                        return true;
                    case 4:
                        return false;
                    case 5:
                        return true;
                    case 6:
                        return true;
                    case 7:
                        return false;
                    case 8:
                        return true;
                    case 9:
                        return true;
                    case 10:
                        return true;
                    default:
                        if(Build.VERSION.SDK_INT >= 11 && (var1 == 14 || var1 == 13)) {
                            return true;
                        }

                        if(Build.VERSION.SDK_INT >= 9 && var1 == 12) {
                            return true;
                        }

                        if(Build.VERSION.SDK_INT >= 8 && var1 == 11) {
                            return false;
                        }
                }
            }

            return false;
        }
    }

    public static String getNetworkType(Context var0) {
        ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo var2 = var1.getActiveNetworkInfo();
        if(var2 != null && var2.isAvailable()) {
            int var3 = var2.getType();
            if(var3 == 1) {
                return "WIFI";
            } else {
                TelephonyManager var4 = (TelephonyManager)var0.getSystemService(Context.TELEPHONY_SERVICE);
                switch(var4.getNetworkType()) {
                    case 1:
                    case 2:
                    case 4:
                    case 7:
                    case 11:
                        return "2G";
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                    case 10:
                    case 12:
                    case 14:
                    case 15:
                        return "3G";
                    case 13:
                        return "4G";
                    default:
                        return "unkonw network";
                }
            }
        } else {
            return "no network";
        }
    }
}

