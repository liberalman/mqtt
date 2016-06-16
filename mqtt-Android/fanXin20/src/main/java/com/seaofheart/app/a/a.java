package com.seaofheart.app.a;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.util.Log;
import android.view.SurfaceView;

import com.seaofheart.app.media.AVNative;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class a {
    private int a = 320;
    private int b = 240;
    private int c = 150;
    private EGLContext d;
    private EGLSurface e;
    private EGLDisplay f;
    private EGLConfig g;
    private int h;
    private SurfaceView i;
    private AVNative j = new AVNative();
    private static a k = null;

    private a() {
    }

    public static a a() {
        if(k == null) {
            k = new a();
        }

        return k;
    }

    public void a(SurfaceView var1) {
        this.i = var1;
    }

    public void a(int var1, int var2, byte[] var3) {
        this.j.nativeProcessYUV(var1, var2, var3);
    }

    public void a(int var1, int var2, int var3) {
        this.j.onNativeResize(var1, var2, this.a(var3));
    }

    public void b() {
        this.j.nativeResume();
    }

    int a(int var1) {
        int var2 = -2062217214;
        switch(var1) {
            case 1:
                Log.v("SDL", "pixel format RGBA_8888");
                var2 = -2042224636;
                break;
            case 2:
                Log.v("SDL", "pixel format RGBX_8888");
                var2 = -2044321788;
                break;
            case 3:
                Log.v("SDL", "pixel format RGB_888");
                var2 = -2045372412;
                break;
            case 4:
                Log.v("SDL", "pixel format RGB_565");
                var2 = -2062217214;
                break;
            case 5:
            default:
                Log.v("SDL", "pixel format unknown " + var1);
                break;
            case 6:
                Log.v("SDL", "pixel format RGBA_5551");
                var2 = -2059137022;
                break;
            case 7:
                Log.v("SDL", "pixel format RGBA_4444");
                var2 = -2059268094;
                break;
            case 8:
                Log.v("SDL", "pixel format A_8");
                break;
            case 9:
                Log.v("SDL", "pixel format L_8");
                break;
            case 10:
                Log.v("SDL", "pixel format LA_88");
                break;
            case 11:
                Log.v("SDL", "pixel format RGB_332");
                var2 = -2079258623;
        }

        return var2;
    }

    public static boolean a(int var0, int var1) {
        Log.d("SDL", "to call initEGL");
        a().d();
        return a().c(var0, var1);
    }

    public static void c() {
        a().r();
    }

    public static void a(String var0) {
    }

    private boolean c(int var1, int var2) {
        if(this.f == null) {
            Log.v("SDL", "initEGL" + var1 + "." + var2);

            try {
                EGL10 var3 = (EGL10)EGLContext.getEGL();
                EGLDisplay var14 = var3.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
                int[] var15 = new int[2];
                var3.eglInitialize(var14, var15);
                byte var16 = 1;
                byte var17 = 4;
                byte var8 = 0;
                if(var1 == 2) {
                    var8 = var17;
                } else if(var1 == 1) {
                    var8 = var16;
                }

                int[] var9 = new int[]{12352, var8, 12344};
                EGLConfig[] var10 = new EGLConfig[1];
                int[] var11 = new int[1];
                if(!var3.eglChooseConfig(var14, var9, var10, 1, var11) || var11[0] == 0) {
                    Log.e("SDL", "No EGL config available");
                    return false;
                }

                EGLConfig var12 = var10[0];
                this.f = var14;
                this.g = var12;
                this.h = var1;
                this.p();
            } catch (Exception var13) {
                Log.v("SDL", "" + var13);
                StackTraceElement[] var7;
                int var6 = (var7 = var13.getStackTrace()).length;

                for(int var5 = 0; var5 < var6; ++var5) {
                    StackTraceElement var4 = var7[var5];
                    Log.v("SDL", var4.toString());
                }
            }
        } else {
            this.p();
        }

        return true;
    }

    private boolean p() {
        if(this.f != null && this.g != null) {
            EGL10 var1 = (EGL10)EGLContext.getEGL();
            if(this.d == null) {
                this.q();
            }

            Log.v("SDL", "Creating new EGL Surface");
            EGLSurface var2 = var1.eglCreateWindowSurface(this.f, this.g, this.i, (int[])null);
            if(var2 == EGL10.EGL_NO_SURFACE) {
                Log.e("SDL", "Couldn\'t create surface");
                return false;
            } else {
                if(!var1.eglMakeCurrent(this.f, var2, var2, this.d)) {
                    Log.e("SDL", "Old EGL Context doesnt work, trying with a new one");
                    this.q();
                    if(!var1.eglMakeCurrent(this.f, var2, var2, this.d)) {
                        Log.e("SDL", "Failed making EGL Context current");
                        return false;
                    }
                }

                this.e = var2;
                return true;
            }
        } else {
            return false;
        }
    }

    private boolean q() {
        EGL10 var1 = (EGL10)EGLContext.getEGL();
        short var2 = 12440;
        int[] var3 = new int[]{var2, this.h, 12344};
        this.d = var1.eglCreateContext(this.f, this.g, EGL10.EGL_NO_CONTEXT, var3);
        if(this.d == EGL10.EGL_NO_CONTEXT) {
            Log.e("SDL", "Couldn\'t create context");
            return false;
        } else {
            return true;
        }
    }

    private void r() {
        try {
            EGL10 var1 = (EGL10)EGLContext.getEGL();
            var1.eglWaitNative(12379, (Object)null);
            var1.eglWaitGL();
            var1.eglSwapBuffers(this.f, this.e);
        } catch (Exception var6) {
            Log.v("SDL", "flipEGL(): " + var6);
            StackTraceElement[] var5;
            int var4 = (var5 = var6.getStackTrace()).length;

            for(int var3 = 0; var3 < var4; ++var3) {
                StackTraceElement var2 = var5[var3];
                Log.v("SDL", var2.toString());
            }
        }

    }

    public void d() {
        try {
            EGL10 var1 = (EGL10)EGLContext.getEGL();
            if(this.e != null) {
                var1.eglMakeCurrent(this.f, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                var1.eglDestroySurface(this.f, this.e);
                this.e = null;
            }

            if(this.d != null) {
                var1.eglDestroyContext(this.f, this.d);
                this.d = null;
            }

            if(this.f != null) {
                var1.eglTerminate(this.f);
                this.f = null;
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public SurfaceView e() {
        return this.i;
    }

    public void b(SurfaceView var1) {
        this.i = var1;
    }

    public void b(int var1, int var2) {
        this.a = var1;
        this.b = var2;
    }

    public int f() {
        return this.a;
    }

    public int g() {
        return this.b;
    }

    public int h() {
        return this.c;
    }

    public void b(int var1) {
        this.c = var1;
    }

    public int b(String var1) {
        return this.j.nativeTakePicture(var1);
    }

    public int i() {
        return this.j.nativeGetVideoTimedelay();
    }

    public int j() {
        return this.j.nativeGetVideoFramerate();
    }

    public int k() {
        return this.j.nativeGetVideoLostcnt();
    }

    public int l() {
        return this.j.nativeGetVideoWidth();
    }

    public int m() {
        return this.j.nativeGetVideoHeight();
    }

    public int n() {
        return this.j.nativeGetRemoteBitrate();
    }

    public int o() {
        return this.j.nativeGetLocalBitrate();
    }

    public void a(boolean var1) {
        this.j.nativeSetRenderFlag(var1);
    }
}
