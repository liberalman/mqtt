package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.util.Base64;

import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.EasyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

public class EncryptUtils {
    private static final String TAG = "encrypt";

    public EncryptUtils() {
    }

    static String encryptMessage(String var0, String var1) {
        try {
            EncryptProvider var2 = ChatManager.getInstance().getEncryptProvider();
            byte[] var3 = var0.getBytes("UTF-8");
            EMLog.d("encrypt", "utf-8 bytes:" + EasyUtils.convertByteArrayToString(var3));
            byte[] var4 = var2.encrypt(var3, var1);
            EMLog.d("encrypt", "encrypted bytes:" + EasyUtils.convertByteArrayToString(var4));
            byte[] var5 = Base64.encode(var4, 0);
            EMLog.d("encrypt", "base64 bytes:" + EasyUtils.convertByteArrayToString(var5));
            String var6 = new String(var5);
            EMLog.d("encrypt", "encrypted str:" + var6);
            return var6;
        } catch (Exception var7) {
            var7.printStackTrace();
            EMLog.e("encrypt", "encryption error, send plain msg");
            return var0;
        }
    }

    static String decryptMessage(String var0, String var1) {
        try {
            EMLog.d("encrypt", "encrypted str:" + var0);
            byte[] var2 = Base64.decode(var0, 0);
            EMLog.d("encrypt", "base64 decode bytes:" + EasyUtils.convertByteArrayToString(var2));
            EncryptProvider var3 = ChatManager.getInstance().getEncryptProvider();
            byte[] var4 = var3.decrypt(var2, var1);
            EMLog.d("encrypt", "decrypt bytes:" + EasyUtils.convertByteArrayToString(var4));
            String var5 = new String(var4, "UTF-8");
            EMLog.d("encrypt", "descripted str:" + var5);
            return var5;
        } catch (Exception var6) {
            var6.printStackTrace();
            return var0;
        }
    }

    public static String encryptFile(String var0, String var1) {
        try {
            EMLog.d("encrypt", "try to encrypt file:" + var0);
            RandomAccessFile var2 = new RandomAccessFile(var0, "r");
            int var3 = (int)var2.length();
            EMLog.d("encrypt", "try to encrypt file:" + var0 + " original len:" + var3);
            byte[] var4 = new byte[var3];
            int var5 = var2.read(var4);
            if(var5 != var3) {
                EMLog.e("encrypt", "error read file, file len:" + var3 + " readLen:" + var5);
                return var0;
            } else {
                var2.close();
                EncryptProvider var6 = ChatManager.getInstance().getEncryptProvider();
                byte[] var7 = var6.encrypt(var4, var1);
                String var8 = null;
                int var9 = var0.lastIndexOf(46);
                if(var9 >= 0) {
                    var8 = var0.substring(var9);
                }

                File var10 = File.createTempFile("encrypted", var8);
                FileOutputStream var11 = new FileOutputStream(var10);
                var11.write(var7);
                var11.close();
                String var12 = var10.getAbsolutePath();
                EMLog.d("encrypt", "generated encrypted file:" + var12);
                return var12;
            }
        } catch (Exception var13) {
            var13.printStackTrace();
            return var0;
        }
    }

    public static void decryptFile(String var0, String var1) {
        try {
            EMLog.d("encrypt", "decrypt file:" + var0);
            RandomAccessFile var2 = new RandomAccessFile(var0, "r");
            int var3 = (int)var2.length();
            byte[] var4 = new byte[var3];
            int var5 = var2.read(var4);
            if(var5 != var3) {
                EMLog.e("encrypt", "error read file, file len:" + var3 + " readLen:" + var5);
                return;
            }

            var2.close();
            EncryptProvider var6 = ChatManager.getInstance().getEncryptProvider();
            byte[] var7 = var6.decrypt(var4, var1);
            FileOutputStream var8 = new FileOutputStream(var0, false);
            var8.write(var7);
            var8.close();
            EMLog.d("encrypt", "decrypted file:" + var0);
        } catch (Exception var9) {
            var9.printStackTrace();
        }

    }
}

