package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/14.
 */
class Result<T> {
    private T data;

    Result() {
    }

    public T getData() {
        return this.data;
    }

    void setData(T var1) {
        this.data = var1;
    }
}

