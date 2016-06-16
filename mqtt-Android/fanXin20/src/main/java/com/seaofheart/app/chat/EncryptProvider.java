package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/15.
 */
public interface EncryptProvider {
    byte[] encrypt(byte[] var1, String var2);

    byte[] decrypt(byte[] var1, String var2);
}
