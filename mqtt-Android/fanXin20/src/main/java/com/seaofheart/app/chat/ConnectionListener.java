package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/14.
 */
public interface ConnectionListener {
    void onConnected();

    void onDisConnected(String var1);

    void onReConnected();

    void onReConnecting();

    void onConnecting(String var1);
}