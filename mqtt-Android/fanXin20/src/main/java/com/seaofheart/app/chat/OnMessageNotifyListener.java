package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/15.
 */
public interface OnMessageNotifyListener {
    String onNewMessageNotify(Message var1);

    String onLatestMessageNotify(Message var1, int var2, int var3);

    String onSetNotificationTitle(Message var1);

    int onSetSmallIcon(Message var1);
}