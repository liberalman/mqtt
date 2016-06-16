package com.seaofheart.app.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.util.Log;

import java.io.File;

public class EMLog {
    public static boolean debugMode = true;
    private static EMLog.ELogMode logMode;
    private static FileLogger fileLog;

    static {
        logMode = EMLog.ELogMode.KLogConsoleFile;
        fileLog = new FileLogger();
    }

    public EMLog() {
    }

    public static void d(String var0, String var1) {
        if(var1 != null) {
            if(debugMode) {
                switch(logMode) {
                    case KLogConsoleOnly:
                        Log.d(var0, var1);
                        break;
                    case KLogFileOnly:
                        fileLog.d(var0, var1);
                        break;
                    case KLogConsoleFile:
                        Log.d(var0, var1.contains("&quot;")?var1.replaceAll("&quot;", "\""):var1);
                        fileLog.d(var0, var1);
                        break;
                    default:
                        Log.d(var0, var1);
                }
            }

        }
    }

    public static void d(String var0, String var1, Throwable var2) {
        if(debugMode) {
            Log.d(var0, var1, var2);
        }

    }

    public static void e(String var0, String var1) {
        if(var1 != null) {
            switch(logMode) {
                case KLogConsoleOnly:
                    Log.e(var0, var1);
                    break;
                case KLogFileOnly:
                    fileLog.e(var0, var1);
                    break;
                case KLogConsoleFile:
                    Log.d(var0, var1);
                    fileLog.e(var0, var1);
                    break;
                default:
                    Log.e(var0, var1);
            }

        }
    }

    public static void e(String var0, String var1, Throwable var2) {
        if(var1 != null) {
            Log.e(var0, var1, var2);
        }
    }

    public static void i(String var0, String var1) {
        if(var1 != null) {
            if(debugMode) {
                switch(logMode) {
                    case KLogConsoleOnly:
                        Log.i(var0, var1);
                        break;
                    case KLogFileOnly:
                        fileLog.i(var0, var1);
                        break;
                    case KLogConsoleFile:
                        Log.i(var0, var1);
                        fileLog.i(var0, var1);
                        break;
                    default:
                        Log.i(var0, var1);
                }
            }

        }
    }

    public static void v(String var0, String var1) {
        if(var1 != null) {
            if(debugMode) {
                switch(logMode) {
                    case KLogConsoleOnly:
                        Log.v(var0, var1);
                        break;
                    case KLogFileOnly:
                        fileLog.v(var0, var1);
                        break;
                    case KLogConsoleFile:
                        Log.v(var0, var1);
                        fileLog.v(var0, var1);
                        break;
                    default:
                        Log.v(var0, var1);
                }
            }

        }
    }

    public static void w(String var0, String var1) {
        if(var1 != null) {
            switch(logMode) {
                case KLogConsoleOnly:
                    Log.w(var0, var1);
                    break;
                case KLogFileOnly:
                    fileLog.w(var0, var1);
                    break;
                case KLogConsoleFile:
                    Log.w(var0, var1);
                    fileLog.w(var0, var1);
                    break;
                default:
                    Log.w(var0, var1);
            }

        }
    }

    public static void w(String var0, String var1, Throwable var2) {
        if(debugMode) {
            Log.w(var0, var1, var2);
        }

    }

    public static void setLogMode(EMLog.ELogMode var0) {
        logMode = var0;
    }

    public static File getLogRoot() {
        return fileLog.getLogRoot();
    }

    public static void freeLogFiles() {
        fileLog.checkAndFreeLogFiles();
    }

    public static enum ELogMode {
        KLogConsoleOnly,
        KLogFileOnly,
        KLogConsoleFile;

        private ELogMode() {
        }
    }
}
