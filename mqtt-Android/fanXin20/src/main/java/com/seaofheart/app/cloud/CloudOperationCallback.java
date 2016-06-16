package com.seaofheart.app.cloud;

/**
 * Created by Administrator on 2016/4/15.
 */
public interface CloudOperationCallback {
    void onSuccess(String var1);

    void onError(String var1);

    void onProgress(int var1);
}
