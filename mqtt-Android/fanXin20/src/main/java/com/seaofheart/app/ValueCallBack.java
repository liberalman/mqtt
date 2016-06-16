package com.seaofheart.app;

/**
 * Created by Administrator on 2016/4/14.
 */
public interface ValueCallBack<T> {
    void onSuccess(T var1);

    void onError(int var1, String var2);
}
