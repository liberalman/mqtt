package com.seaofheart.app.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.util.Base64;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    Cipher cipher = null;
    Cipher decipher = null;
    public static final int ALGORIGHM_DES = 0;
    public static final int ALGORIGHM_AES = 1;
    byte[] keyBytes = new byte[]{(byte)74, (byte)111, (byte)104, (byte)110, (byte)115, (byte)111, (byte)110, (byte)77, (byte)97, (byte)74, (byte)105, (byte)70, (byte)97, (byte)110, (byte)103, (byte)74, (byte)101, (byte)114, (byte)118, (byte)105, (byte)115, (byte)76, (byte)105, (byte)117, (byte)76, (byte)105, (byte)117, (byte)83, (byte)104, (byte)97, (byte)111, (byte)90};
    String key = "TongliforniaJohnson";
    static final String HEXES = "0123456789ABCDEF";

    public CryptoUtils() {
    }

    public void init(int var1) {
        if(var1 == 0) {
            this.initDES();
        } else {
            this.initAES();
        }

    }

    public void initDES() {
        try {
            MessageDigest var1 = MessageDigest.getInstance("md5");
            byte[] var2 = var1.digest(this.key.getBytes("utf-8"));
            this.keyBytes = Arrays.copyOf(var2, 24);
            int var3 = 0;

            for(int var4 = 16; var3 < 8; this.keyBytes[var4++] = this.keyBytes[var3++]) {
                ;
            }

            SecretKeySpec var6 = new SecretKeySpec(this.keyBytes, "DESede");
            IvParameterSpec var7 = new IvParameterSpec(new byte[8]);
            this.cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            this.cipher.init(1, var6, var7);
            this.decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            this.decipher.init(2, var6, var7);
            com.easemob.util.EMLog.d("encrypt", "initital for DES");
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public void initAES() {
        try {
            this.cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec var1 = new SecretKeySpec(this.keyBytes, "AES");
            this.cipher.init(1, var1);
            this.decipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            this.decipher.init(2, var1);
            com.easemob.util.EMLog.d("encrypt", "initital for AES");
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public String encryptBase64String(String var1) throws Exception {
        byte[] var2 = this.encrypt(var1);
        return new String(Base64.encode(var2, 0));
    }

    public String decryptBase64String(String var1) throws Exception {
        byte[] var2 = Base64.decode(var1, 0);
        byte[] var3 = this.decrypt(var2);
        return new String(var3, "UTF-8");
    }

    public byte[] encrypt(String var1) throws Exception {
        byte[] var2 = var1.getBytes("UTF-8");
        byte[] var3 = this.cipher.doFinal(var2);
        return var3;
    }

    public byte[] encrypt(byte[] var1) throws Exception {
        return this.cipher.doFinal(var1);
    }

    public byte[] decrypt(byte[] var1) throws Exception {
        return this.decipher.doFinal(var1);
    }

    public static String getHex(byte[] var0) {
        if(var0 == null) {
            return null;
        } else {
            StringBuilder var1 = new StringBuilder(2 * var0.length);
            byte[] var5 = var0;
            int var4 = var0.length;

            for(int var3 = 0; var3 < var4; ++var3) {
                byte var2 = var5[var3];
                var1.append("0123456789ABCDEF".charAt((var2 & 240) >> 4)).append("0123456789ABCDEF".charAt(var2 & 15));
            }

            return var1.toString();
        }
    }
}

