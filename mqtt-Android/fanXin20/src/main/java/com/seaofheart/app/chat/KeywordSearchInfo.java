package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/14.
 */
public class KeywordSearchInfo {
    private String username;
    private Message message;
    private long count;

    public KeywordSearchInfo() {
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String var1) {
        this.username = var1;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message var1) {
        this.message = var1;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long var1) {
        this.count = var1;
    }
}