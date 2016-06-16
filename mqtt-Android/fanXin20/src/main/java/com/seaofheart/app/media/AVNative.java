package com.seaofheart.app.media;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;

import com.seaofheart.app.util.EMLog;

public class AVNative {
    static final String TAG = AVNative.class.getSimpleName();

    static {
        System.loadLibrary("easemob_jni");
    }

    public AVNative() {
    }

    native int nativeVoeClient_Register(IGxStatusCallback var1, Context var2, int var3, String var4, int var5, String var6, String var7, int var8, String var9, String var10, boolean var11, int var12);

    native int nativeVoeClient_FullDuplexSpeech(String var1);

    native int nativeVoeClient_Stop(String var1);

    native int nativeVoeClient_Release(String var1);

    native int nativeVoeClient_GetAudioLevel(String var1);

    native int nativeVoeClient_StartRecodeMic(IGxStatusCallback var1, Context var2, String var3, String var4);

    native int nativeVoeClient_StopRecodeMic(String var1);

    native int nativeVoeClient_GetAudioOutputLevel(String var1);

    public native int nativeTakePicture(String var1);

    public native int nativeProcessPcm(byte[] var1, int var2, String var3);

    public int register(IGxStatusCallback var1, Context var2, int var3, String var4, int var5, String var6, String var7, int var8, String var9, String var10, boolean var11, int var12) {
        EMLog.v(TAG, "VoeEngine register local_port = " + var3);
        EMLog.v(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + var7 + ";" + var7.length());
        EMLog.v("PASSWORD", "register with password:" + var10);
        int var13 = this.nativeVoeClient_Register(var1, var2, var3, var4, var5, var6, var7.trim(), var8, var9, var10, var11, var12);
        EMLog.v(TAG, "VoeEngine register have registered index:" + var13 + "conferenceId:" + var7);
        return var13;
    }

    public int unregister(String var1) {
        EMLog.v(TAG, "VoeEngine unregister conferenceId:" + var1);
        return this.nativeVoeClient_Release(var1);
    }

    public int stop(String var1) {
        EMLog.v(TAG, "VoeEngine stop conferenceId:" + var1);
        return this.nativeVoeClient_Stop(var1);
    }

    public int setFullDuplexSpeech(String var1) {
        EMLog.v(TAG, "VoeEngine setFullDuplexSpeech conferenceId:" + var1);
        return this.nativeVoeClient_FullDuplexSpeech(var1);
    }

    public int GetAudioInputLevel(String var1) {
        EMLog.v(TAG, "VoeEngine setFullDuplexSpeech conferenceId:" + var1);
        return this.nativeVoeClient_GetAudioLevel(var1);
    }

    public int GetAudioOutputLevel(String var1) {
        EMLog.v(TAG, "VoeEngine setFullDuplexSpeech conferenceId:" + var1);
        return this.nativeVoeClient_GetAudioOutputLevel(var1);
    }

    public native int nativeStartVideo(IGxStatusCallback var1, int var2, int var3, String var4, String var5, int var6, String var7, String var8, int var9, int var10, int var11, boolean var12, boolean var13);

    public native void nativeStopVideo();

    public native int nativeProcessYUV(int var1, int var2, byte[] var3);

    public native void nativeInit(VideoCallBridge var1);

    public native void nativeQuit();

    public native void nativePause();

    public native void nativeResume();

    public native void onNativeResize(int var1, int var2, int var3);

    public native void onNativeKeyDown(int var1);

    public native void onNativeKeyUp(int var1);

    public native void onNativeTouch(int var1, int var2, int var3, float var4, float var5, float var6);

    public native void onNativeAccel(float var1, float var2, float var3);

    public native int nativeSetRenderFlag(boolean var1);

    public native int nativeGetVideoTimedelay();

    public native int nativeGetVideoFramerate();

    public native int nativeGetVideoLostcnt();

    public native int nativeGetVideoWidth();

    public native int nativeGetVideoHeight();

    public native int nativeGetRemoteBitrate();

    public native int nativeGetLocalBitrate();

    public native int nativeSetVideoEncodeFlag(boolean var1);

    public native int nativeVoeClient_GetRemoteBitrate(String var1);

    public native int nativeVoeClient_GetTimeDelay(String var1);

    public native int nativeVoeClient_GetLostcnt(String var1);

    public native int nativeVoeClient_GetLocalBitrate(String var1);
}

