package com.seaofheart.app.media;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.util.Log;

public class EIce {
    protected static final String TAG = "EIce_Java";
    protected long nativeHandle = 0L;
    protected String localContent = null;
    protected String negoResult = null;
    protected Thread queryThread = null;
    protected boolean stopReq = false;
    private static EIce.LogListener sLogListener;

    static {
        System.loadLibrary("easemob_jni");
        nativeInitEIce();
        sLogListener = null;
    }

    protected static native void nativeInitEIce();

    protected native long nativeNewCaller(String var1);

    protected native long nativeNewCallee(String var1, String var2);

    protected native String nativeGetLocalContent(long var1);

    protected native void nativeCallerNego(long var1, String var3);

    protected native void nativeFreeCall(long var1);

    protected native String nativeGetNegoResult(long var1);

    private EIce() {
    }

    public static EIce newCaller(String var0) {
        EIce var1 = new EIce();
        var1.nativeHandle = var1.nativeNewCaller(var0);
        var1.localContent = var1.nativeGetLocalContent(var1.nativeHandle);
        return var1;
    }

    public static EIce newCallee(String var0, String var1) {
        EIce var2 = new EIce();
        var2.nativeHandle = var2.nativeNewCallee(var0, var1);
        var2.localContent = var2.nativeGetLocalContent(var2.nativeHandle);
        return var2;
    }

    public String getLocalContent() {
        return this.localContent;
    }

    protected void startQueryResult(final EIce.EIceListener var1) {
        this.queryThread = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    String var1x = EIce.this.nativeGetNegoResult(EIce.this.nativeHandle);
                    if(var1x != null) {
                        EIce var2 = EIce.this;
                        synchronized(EIce.this) {
                            EIce.this.negoResult = var1x;
                            EIce.this.notifyAll();
                        }

                        Log.i("EIce_Java", "got nego result: " + var1x);
                        if(var1 != null) {
                            var1.onNegoResult(var1x);
                        }
                    } else {
                        label34: {
                            try {
                                Thread.sleep(200L);
                            } catch (InterruptedException var4) {
                                Log.i("EIce_Java", "queryThread got InterruptedException " + var4.getLocalizedMessage());
                                break label34;
                            }

                            if(!EIce.this.stopReq) {
                                continue;
                            }

                            Log.i("EIce_Java", "queryThread got stop req");
                        }
                    }

                    Log.i("EIce_Java", "queryThread exit");
                    return;
                }
            }
        });
        this.stopReq = false;
        this.queryThread.start();
    }

    public void callerNego(String var1, EIce.EIceListener var2) {
        this.nativeCallerNego(this.nativeHandle, var1);
        this.startQueryResult(var2);
    }

    public void calleeNego(EIce.EIceListener var1) {
        this.startQueryResult(var1);
    }

    public String waitforNegoResult() {
        String var1 = null;

        while(true) {
            synchronized(this) {
                if(this.getNegoResult() == null) {
                    try {
                        this.wait();
                        continue;
                    } catch (InterruptedException var4) {
                        var4.printStackTrace();
                    }
                } else {
                    var1 = this.getNegoResult();
                }

                return var1;
            }
        }
    }

    public String getNegoResult() {
        synchronized(this) {
            return this.negoResult;
        }
    }

    public void freeCall() {
        this.nativeFreeCall(this.nativeHandle);
        this.nativeHandle = 0L;
        if(this.queryThread != null) {
            this.stopReq = true;

            try {
                this.queryThread.join();
            } catch (InterruptedException var2) {
                var2.printStackTrace();
            }

            this.queryThread = null;
        }

    }

    public static void registerLogListener(EIce.LogListener var0) {
        Class var1 = EIce.class;
        synchronized(EIce.class) {
            sLogListener = var0;
        }
    }

    protected static void callbackLog(int var0, String var1) {
        Class var2 = EIce.class;
        synchronized(EIce.class) {
            sLogListener.onLog(var0, var1);
        }
    }

    public interface EIceListener {
        void onNegoResult(String var1);
    }

    public interface LogListener {
        void onLog(int var1, String var2);
    }
}

