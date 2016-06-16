package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/14.
 */
import com.seaofheart.app.chat.core.e;

class LeaveRoom implements e {
    String roomId = "";
    ChatRoomManager roomMgr = null;

    LeaveRoom(String var1, ChatRoomManager var2) {
        this.roomId = var1;
        this.roomMgr = var2;
    }

    public void run() throws Exception {
        this.roomMgr.cmdExitRoom(this.roomId);
    }

    public boolean equals(Object var1) {
        return var1 instanceof LeaveRoom && ((LeaveRoom)var1).roomId.equals(this.roomId);
    }

    public String toString() {
        return "EMLeaveRoom :  roomId : " + this.roomId;
    }
}
