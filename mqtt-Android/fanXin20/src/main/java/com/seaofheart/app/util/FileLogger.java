package com.seaofheart.app.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.chat.ChatConfig;

class FileLogger {
    private static final String TAG = "FileLogger";
    private static final long MB = 1048576L;
    private static final long LOG_LIMIT = 8388608L;
    private static final long FREE_SPACE_LIMIT = 20971520L;
    private static final long FREE_SPACE_TIMER = 1200000L;
    private static final String INFO_COLOR = "green";
    private static final String ERROR_COLOR = "red";
    private static final String WARN_COLOR = "orange";
    private static final String DEBUG_COLOR = "blue";
    private ExecutorService singleThreadService = Executors.newSingleThreadExecutor();
    private File logRoot = null;
    private Timer timer = null;
    private boolean spaceAvailable = true;

    FileLogger() {
    }

    void d(String var1, String var2) {
        this.startThreadService("blue", "[" + var1 + "]" + var2);
    }

    void e(String var1, String var2) {
        this.startThreadService("red", "[" + var1 + "]" + "[ERROR]" + var2);
    }

    void i(String var1, String var2) {
        this.startThreadService("green", "[" + var1 + "]" + var2);
    }

    void w(String var1, String var2) {
        this.startThreadService("orange", "[" + var1 + "]" + "[WARN]" + var2);
    }

    void v(String var1, String var2) {
        this.startThreadService("green", "[" + var1 + "]" + var2);
    }

    private void startThreadService(String var1, String var2) {
        File var3 = this.getLogRoot();
        if(var3 != null && var3.exists()) {
            this.singleThreadService.execute(this.getWriterRunnable(var1, var2));
        }
    }

    private Runnable getWriterRunnable(final String var1, final String var2) {
        return new Runnable() {
            public void run() {
                try {
                    File var1x = FileLogger.this.getLogRoot();
                    if(var1x == null || !var1x.exists()) {
                        return;
                    }

                    if(FileLogger.this.timer == null && !FileLogger.this.freeSpace()) {
                        return;
                    }

                    FileLogger.this.startCleanUpTimer();
                    File var2x = FileLogger.this.getAvailableFile();
                    if(var2x == null) {
                        return;
                    }

                    boolean var3 = false;
                    if(!var2x.exists()) {
                        try {
                            var2x.createNewFile();
                            var3 = true;
                        } catch (IOException var28) {
                            var28.printStackTrace();
                        }
                    }

                    if(!var2x.exists()) {
                        return;
                    }

                    FileOutputStream var4 = null;

                    try {
                        var4 = new FileOutputStream(var2x, true);
                        if(var3) {
                            String var5 = "<header>";
                            var5 = var5 + "<meta http-equiv=" + "\"" + "Content-Type" + "\"" + " content=" + "\"" + "text/html; charset=UTF-8" + "\">";
                            var5 = var5 + "</header>";
                            var4.write(var5.getBytes());
                        }

                        Date var32 = new Date();
                        SimpleDateFormat var6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                        String var7 = var6.format(var32);
                        String var8 = var2.replaceAll(">", "&gt;");
                        var8 = var8.replaceAll("<", "&lt;");
                        String var9 = "<p><font color =\"" + var1 + "\">" + var7 + " " + var8 + "</p>";
                        byte[] var10 = var9.getBytes();
                        var4.write(var10);
                    } catch (FileNotFoundException var25) {
                        var25.printStackTrace();
                    } catch (IOException var26) {
                        var26.printStackTrace();
                    } finally {
                        try {
                            if(var4 != null) {
                                var4.close();
                            }
                        } catch (IOException var24) {
                            var24.printStackTrace();
                        }

                    }
                } catch (NullPointerException var29) {
                    var29.printStackTrace();
                    Log.e("FileLogger", var29.getMessage());
                } catch (Exception var30) {
                    var30.printStackTrace();
                    Log.e("FileLogger", var30.getMessage());
                } catch (Throwable var31) {
                    Log.e("FileLogger", "trrowable exception");
                }

            }
        };
    }

    private File getAvailableFile() {
        File var1 = this.getLogRoot();
        if(var1 != null && var1.exists()) {
            Date var2 = new Date();
            SimpleDateFormat var3 = new SimpleDateFormat("yyyyMMdd");
            var3.format(var2);
            this.removeOldFolders();
            File var5 = this.getLogFolder();
            int var6 = 0;
            List var7 = null;
            File[] var8 = var5.listFiles();
            String var9;
            if(var8 != null && var8.length > 0) {
                var7 = Arrays.asList(var8);
                if(var7.size() > 1) {
                    getSortedFileListByName(var7);
                    var8 = (File[])var7.toArray();
                }

                var9 = var8[0].getName();
                var9 = var9.substring(0, var9.indexOf("."));

                try {
                    var6 = Integer.parseInt(var9);
                } catch (Exception var11) {
                    var11.printStackTrace();
                    Log.e("FileLogger", "Wrong cntName! : " + var9);
                }

                if(var8[0].length() >= 1048576L) {
                    ++var6;
                }
            }

            var9 = getLogFileName(var6);
            return new File(var5, var9);
        } else {
            return null;
        }
    }

    private static String getLogFileName(int var0) {
        String var1 = String.valueOf(var0);
        String var2 = "000";
        String var3 = var2.substring(var1.length());
        var3 = var3 + var1 + ".html";
        return var3;
    }

    private File getLogFolder() {
        Date var1 = new Date();
        SimpleDateFormat var2 = new SimpleDateFormat("yyyyMMdd");
        String var3 = var2.format(var1);
        File var4 = this.getLogRoot();
        File var5 = new File(var4, var3);
        if(!var5.exists()) {
            var5.mkdirs();
        }

        return var5;
    }

    private void removeFolderBeforeDay(String var1, int var2) {
        String var3 = getSpecifiedDayBefore(var1, var2);
        File var4 = this.getLogRoot();
        File var5 = new File(var4, var3);
        if(var5.exists()) {
            deleteFile(var5);
        }

    }

    private void removeOldFolders() {
        File var1 = this.getLogRoot();
        if(var1 != null && var1.exists()) {
            Date var2 = new Date();
            SimpleDateFormat var3 = new SimpleDateFormat("yyyyMMdd");
            String var4 = var3.format(var2);
            String var5 = getSpecifiedDayBefore(var4, 1);
            File[] var6 = var1.listFiles();
            if(var6 != null) {
                File[] var10 = var6;
                int var9 = var6.length;

                for(int var8 = 0; var8 < var9; ++var8) {
                    File var7 = var10[var8];
                    if(var7.isDirectory() && !var7.getName().contains(var4) && !var7.getName().contains(var5)) {
                        deleteFile(var7);
                    } else {
                        var7.delete();
                    }
                }

            }
        }
    }

    private static void deleteFile(File var0) {
        if(var0 != null) {
            if(var0.exists()) {
                if(var0.isDirectory()) {
                    File[] var1 = var0.listFiles();
                    if(var1 != null) {
                        File[] var5 = var1;
                        int var4 = var1.length;

                        for(int var3 = 0; var3 < var4; ++var3) {
                            File var2 = var5[var3];
                            deleteFile(var2);
                        }
                    }

                    var0.delete();
                } else {
                    var0.delete();
                }
            }

        }
    }

    private static long getDirSize(File var0) {
        if(var0 == null) {
            return 0L;
        } else if(!var0.isDirectory()) {
            return var0.length();
        } else {
            long var1 = 0L;
            File[] var3 = var0.listFiles();
            if(var3 != null) {
                File[] var7 = var3;
                int var6 = var3.length;

                for(int var5 = 0; var5 < var6; ++var5) {
                    File var4 = var7[var5];
                    var1 += getDirSize(var4);
                }
            }

            return var1;
        }
    }

    private static String getSpecifiedDayBefore(String var0, int var1) {
        Calendar var2 = Calendar.getInstance();
        Date var3 = null;

        try {
            var3 = (new SimpleDateFormat("yyyyMMdd")).parse(var0);
        } catch (ParseException var6) {
            var6.printStackTrace();
        }

        var2.setTime(var3);
        int var4 = var2.get(5);
        var2.set(5, var4 - var1);
        String var5 = (new SimpleDateFormat("yyyyMMdd")).format(var2.getTime());
        return var5;
    }

    private static File getStorageDir() {
        return Environment.getExternalStorageState().equals("mounted")?Environment.getExternalStorageDirectory():Environment.getDataDirectory();
    }

    private static void getSortedFileListByName(List<File> list) {
        Collections.sort(list, new Comparator() {
            public int compare(Object var1, Object var2) {
                return ((File)var2).getName().compareTo(((File)var1).getName());
            }
        });
    }

    private boolean spaceIsAlearting() {
        long var1 = getCurrentAvailabeSpace();
        return var1 < 20971520L;
    }

    private boolean logSizeAlearting() {
        long var1 = getDirSize(this.getLogRoot());
        return var1 > 8388608L;
    }

    boolean freeSpace() {
        File var1 = this.getLogRoot();
        if(var1 != null && var1.exists()) {
            if(this.spaceIsAlearting()) {
                Log.w("FileLogger", "there is no availabe free space and try to free space");
                this.freeLogFolder();
                return !this.spaceIsAlearting();
            } else {
                this.checkAndFreeLogFiles();
                return true;
            }
        } else {
            return false;
        }
    }

    private void freeLogFolder() {
        deleteFile(this.getLogRoot());
    }

    private void freeOldFolders() {
        Date var1 = new Date();
        SimpleDateFormat var2 = new SimpleDateFormat("yyyyMMdd");
        String var3 = var2.format(var1);
        File[] var4 = this.getLogRoot().listFiles();
        File[] var8 = var4;
        int var7 = var4.length;

        for(int var6 = 0; var6 < var7; ++var6) {
            File var5 = var8[var6];
            if(var5.isDirectory() && !var5.getName().contains(var3)) {
                deleteFile(var5);
            } else {
                var5.delete();
            }
        }

    }

    private void freeOldFiles() {
        File[] var1 = this.getLogFolder().listFiles();
        if(var1 != null) {
            List var2 = Arrays.asList(var1);
            getSortedFileListByName(var2);
            if(var2.size() > 5) {
                int var3 = var2.size();

                for(int var4 = 5; var4 < var3; ++var4) {
                    Log.w("FileLogger", "try to delete file : " + ((File)var2.get(var4)).getAbsoluteFile());
                    ((File)var2.get(var4)).delete();
                }
            }
        }

    }

    private static long getCurrentAvailabeSpace() {
        StatFs var0 = new StatFs(getStorageDir().getPath());
        if(Build.VERSION.SDK_INT >= 18) {
            return var0.getAvailableBytes();
        } else {
            long var1 = (long)var0.getAvailableBlocks();
            long var3 = (long)var0.getBlockSize();
            return var1 * var3;
        }
    }

    File getLogRoot() {
        String var1 = ChatConfig.getInstance().APPKEY;
        Context var2 = Chat.getInstance().getAppContext();
        String var3 = "/Android/data/";
        if(var2 != null) {
            var3 = var3 + var2.getPackageName();
        }

        if(var1 == null) {
            return null;
        } else {
            var3 = var3 + "/" + var1 + "/log/";

            try {
                File var4 = new File(getStorageDir(), var3);
                synchronized(this) {
                    if(!var4.exists()) {
                        var4.mkdirs();
                    }
                }

                return var4;
            } catch (Exception var7) {
                return null;
            } catch (Throwable var8) {
                return null;
            }
        }
    }

    void checkAndFreeLogFiles() {
        if(this.logSizeAlearting()) {
            Log.w("FileLogger", "the log size is > 8M, try to free log files");
            this.freeOldFolders();
            Log.w("FileLogger", "old folders are deleted");
            if(this.logSizeAlearting()) {
                Log.w("FileLogger", "try to delete old log files");
                this.freeOldFiles();
            }
        }

    }

    private void startCleanUpTimer() {
        synchronized(this) {
            if(this.timer == null) {
                this.timer = new Timer();
                TimerTask var2 = new TimerTask() {
                    public void run() {
                        FileLogger.this.singleThreadService.execute(new Runnable() {
                            public void run() {
                                try {
                                    File var1 = FileLogger.this.getLogRoot();
                                    if(var1 == null || !FileLogger.this.getLogRoot().exists()) {
                                        return;
                                    }

                                    FileLogger.this.spaceAvailable = FileLogger.this.freeSpace();
                                } catch (NullPointerException var2) {
                                    var2.printStackTrace();
                                    Log.e("FileLogger", var2.getMessage());
                                } catch (Exception var3) {
                                    var3.printStackTrace();
                                    Log.e("FileLogger", var3.getMessage());
                                } catch (Throwable var4) {
                                    ;
                                }

                            }
                        });
                    }
                };
                this.timer.schedule(var2, 1200000L, 1200000L);
            }

        }
    }
}

