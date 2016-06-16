package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/14.
 */
public interface ChatRoomChangeListener {
    void onChatRoomDestroyed(String var1, String var2);

    void onMemberJoined(String var1, String var2);

    void onMemberExited(String var1, String var2, String var3);

    void onMemberKicked(String var1, String var2, String var3);
}