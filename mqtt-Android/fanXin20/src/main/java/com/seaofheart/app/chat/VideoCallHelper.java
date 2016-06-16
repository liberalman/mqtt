package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.view.SurfaceView;

import com.seaofheart.app.a.a;

public class VideoCallHelper {
    private static VideoCallHelper instance = null;
    private VideoCallHelper.EMVideoOrientation videoOrientation;

    private VideoCallHelper() {
        this.videoOrientation = VideoCallHelper.EMVideoOrientation.EMPortrait;
    }

    public static VideoCallHelper getInstance() {
        if(instance == null) {
            instance = new VideoCallHelper();
        }

        return instance;
    }

    public void processPreviewData(int var1, int var2, byte[] var3) {
        a.a().a(var1, var2, var3);
    }

    public void onWindowResize(int var1, int var2, int var3) {
        a.a().a(var1, var2, var3);
    }

    public void setSurfaceView(SurfaceView var1) {
        a.a().a(var1);
    }

    public void setResolution(int var1, int var2) {
        a.a().b(var1, var2);
    }

    public int takePicture(String var1) {
        return a.a().b(var1);
    }

    public void setVideoOrientation(VideoCallHelper.EMVideoOrientation var1) {
        this.videoOrientation = var1;
    }

    public VideoCallHelper.EMVideoOrientation getVideoOrientation() {
        return this.videoOrientation;
    }

    public int getVideoTimedelay() {
        return a.a().i();
    }

    public int getVideoFramerate() {
        return a.a().j();
    }

    public int getVideoLostcnt() {
        return a.a().k();
    }

    public int getVideoWidth() {
        return a.a().l();
    }

    public int getVideoHeight() {
        return a.a().m();
    }

    public int getRemoteBitrate() {
        return a.a().n();
    }

    public int getLocalBitrate() {
        return a.a().o();
    }

    public void setRenderFlag(boolean var1) {
        a.a().a(var1);
    }

    public void setVideoBitrate(int var1) {
        a.a().b(var1);
    }

    public static enum EMVideoOrientation {
        EMPortrait,
        EMLandscape;

        private EMVideoOrientation() {
        }
    }
}

