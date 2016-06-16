package com.seaofheart.app.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FileUtils {
    public static String[] fileTypes = new String[]{"apk", "avi", "bmp", "chm", "dll", "doc", "docx", "dos", "gif", "html", "jpeg", "jpg", "movie", "mp3", "dat", "mp4", "mpe", "mpeg", "mpg", "pdf", "png", "ppt", "pptx", "rar", "txt", "wav", "wma", "wmv", "xls", "xlsx", "xml", "zip"};

    public FileUtils() {
    }

    public static File[] loadFiles(File var0) {
        File[] var1 = var0.listFiles();
        if(var1 == null) {
            var1 = new File[0];
        }

        ArrayList var2 = new ArrayList();
        ArrayList var3 = new ArrayList();

        for(int var4 = 0; var4 < var1.length; ++var4) {
            File var5 = var1[var4];
            if(var5.isDirectory()) {
                var2.add(var5);
            } else if(var5.isFile()) {
                var3.add(var5);
            }
        }

        FileUtils.MyComparator var6 = new FileUtils.MyComparator();
        Collections.sort(var2, var6);
        Collections.sort(var3, var6);
        File[] var7 = new File[var2.size() + var3.size()];
        System.arraycopy(var2.toArray(), 0, var7, 0, var2.size());
        System.arraycopy(var3.toArray(), 0, var7, var2.size(), var3.size());
        return var7;
    }

    public static String getMIMEType(File var0) {
        String var1 = "";
        String var2 = var0.getName();
        String var3 = var2.substring(var2.lastIndexOf(".") + 1, var2.length()).toLowerCase();
        var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
        return var1;
    }

    public static String getMIMEType(String var0) {
        String var1 = "";
        String var2 = var0.substring(var0.lastIndexOf(".") + 1, var0.length()).toLowerCase();
        var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var2);
        return var1;
    }

    public static void openFile(File var0, Activity var1) {
        Intent var2 = new Intent();
        var2.addFlags(268435456);
        var2.setAction("android.intent.action.VIEW");
        String var3 = getMIMEType(var0);
        var2.setDataAndType(Uri.fromFile(var0), var3);

        try {
            var1.startActivity(var2);
        } catch (Exception var5) {
            var5.printStackTrace();
            Toast.makeText(var1, "没有找到打开此类文件的程序", Toast.LENGTH_LONG).show();
        }

    }

    public static void openFile(Uri var0, String var1, Activity var2) {
        Intent var3 = new Intent();
        var3.addFlags(268435456);
        var3.setAction("android.intent.action.VIEW");
        var3.setDataAndType(var0, var1);

        try {
            var2.startActivity(var3);
        } catch (Exception var5) {
            var5.printStackTrace();
            Toast.makeText(var2, "没有找到打开此类文件的程序", Toast.LENGTH_LONG).show();
        }

    }

    public static synchronized void saveObjectToFile(Object var0, File var1) throws Exception {
        ObjectOutputStream var2 = new ObjectOutputStream(new FileOutputStream(var1));
        var2.writeObject(var0);
        var2.flush();
        var2.close();
    }

    public static synchronized Object readObjectFromFile(File var0) throws Exception {
        ObjectInputStream var1 = new ObjectInputStream(new FileInputStream(var0));
        Object var2 = var1.readObject();
        return var2;
    }

    public static class MyComparator implements Comparator<File> {
        public MyComparator() {
        }

        public int compare(File var1, File var2) {
            return var1.getName().compareTo(var2.getName());
        }
    }
}
