package com.seaofheart.app.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class EasyUtils {
    private static Hashtable<String, String> resourceTable = new Hashtable();

    public EasyUtils() {
    }

    public static boolean isAppRunningForeground(Context var0) {
        ActivityManager var1 = (ActivityManager)var0.getSystemService(Context.ACTIVITY_SERVICE);
        List var2 = var1.getRunningTasks(1);
        boolean var3 = var0.getPackageName().equalsIgnoreCase(((ActivityManager.RunningTaskInfo)var2.get(0)).baseActivity.getPackageName());
        com.easemob.util.EMLog.d("utils", "app running in foregroud：" + var3);
        return var3;
    }

    public static String getTopActivityName(Context var0) {
        ActivityManager var1 = (ActivityManager)var0.getSystemService(Context.ACTIVITY_SERVICE);
        List var2 = var1.getRunningTasks(1);
        return ((ActivityManager.RunningTaskInfo)var2.get(0)).topActivity.getClassName();
    }

    public static List<String> getRunningApps(Context var0) {
        ArrayList var1 = new ArrayList();
        ActivityManager var2 = (ActivityManager)var0.getSystemService(Context.ACTIVITY_SERVICE);
        List var3 = var2.getRunningAppProcesses();
        Iterator var5 = var3.iterator();

        while(var5.hasNext()) {
            ActivityManager.RunningAppProcessInfo var4 = (ActivityManager.RunningAppProcessInfo)var5.next();
            String var6 = var4.processName;
            if(var6.contains(":")) {
                var6 = var6.substring(0, var6.indexOf(":"));
            }

            if(!var1.contains(var6)) {
                var1.add(var6);
            }
        }

        return var1;
    }

    public static String getTimeStamp() {
        Date var0 = new Date(System.currentTimeMillis());
        SimpleDateFormat var1 = new SimpleDateFormat("yyyyMMddHHmmss");
        return var1.format(var0);
    }

    public static boolean writeToZipFile(byte[] var0, String var1) {
        FileOutputStream var2 = null;
        GZIPOutputStream var3 = null;

        label109: {
            try {
                var2 = new FileOutputStream(var1);
                var3 = new GZIPOutputStream(new BufferedOutputStream(var2));
                var3.write(var0);
                break label109;
            } catch (Exception var20) {
                var20.printStackTrace();
            } finally {
                if(var3 != null) {
                    try {
                        var3.close();
                    } catch (IOException var19) {
                        var19.printStackTrace();
                    }
                }

                if(var2 != null) {
                    try {
                        var2.close();
                    } catch (IOException var18) {
                        var18.printStackTrace();
                    }
                }

            }

            return false;
        }

        if(com.easemob.util.EMLog.debugMode) {
            File var4 = new File(var1);
            DecimalFormat var5 = new DecimalFormat("#.##");
            double var6 = (double)var4.length() / (double)var0.length * 100.0D;
            double var8 = Double.valueOf(var5.format(var6)).doubleValue();
            com.easemob.util.EMLog.d("zip", "data size:" + var0.length + " zip file size:" + var4.length() + "zip file ratio%: " + var8);
        }

        return true;
    }

    public static String getAppResourceString(Context var0, String var1) {
        String var2 = (String)resourceTable.get(var1);
        if(var2 != null) {
            return var2;
        } else {
            int var3 = var0.getResources().getIdentifier(var1, "string", var0.getPackageName());
            var2 = var0.getString(var3);
            if(var2 != null) {
                resourceTable.put(var1, var2);
            }

            return var2;
        }
    }

    public static String convertByteArrayToString(byte[] var0) {
        StringBuffer var1 = new StringBuffer();
        byte[] var5 = var0;
        int var4 = var0.length;

        for(int var3 = 0; var3 < var4; ++var3) {
            byte var2 = var5[var3];
            var1.append(String.format("0x%02X", new Object[]{Byte.valueOf(var2)}));
        }

        return var1.toString();
    }

    public static boolean isSdcardExist() {
        return Environment.getExternalStorageState().equals("mounted");
    }
}

