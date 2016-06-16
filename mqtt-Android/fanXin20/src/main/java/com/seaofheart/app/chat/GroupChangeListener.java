package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/14.
 */
public interface GroupChangeListener {
    void onInvitationReceived(String var1, String var2, String var3, String var4);

    void onApplicationReceived(String var1, String var2, String var3, String var4);

    void onApplicationAccept(String var1, String var2, String var3);

    void onApplicationDeclined(String var1, String var2, String var3, String var4);

    void onInvitationAccpted(String var1, String var2, String var3);

    void onInvitationDeclined(String var1, String var2, String var3);

    void onUserRemoved(String var1, String var2);

    void onGroupDestroy(String var1, String var2);
}
