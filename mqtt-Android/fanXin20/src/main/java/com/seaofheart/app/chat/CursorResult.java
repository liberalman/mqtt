package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/14.
 */
import java.util.List;

public class CursorResult<T> extends Result<List<T>> {
    private String cursor;

    public CursorResult() {
    }

    void setCursor(String var1) {
        this.cursor = var1;
    }

    public String getCursor() {
        return this.cursor;
    }
}

