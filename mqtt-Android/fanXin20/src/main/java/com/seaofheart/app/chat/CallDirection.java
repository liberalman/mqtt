package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/15.
 */
enum CallDirection {
    OUTGOING("outgoing"),
    INCOMING("incoming"),
    NONE("none");

    private String direction;

    private CallDirection(String var3) {
        this.direction = var3;
    }

    public String toString() {
        return this.direction;
    }
}
