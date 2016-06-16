package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.EasyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class s {
    private static final String a = "EMMonitorDB";
    private static final int b = 1;
    private static final String c = "monitor.db";
    private s.b d = null;
    private static final String e = "apps";
    private static final String f = "appname";
    private String g = null;
    private static final String h = "create table apps (appname text primary key);";

    public s() {
        try {
            this.g = Environment.getExternalStorageDirectory() + "/emlibs/libs";
            this.d = new s.b(Chat.getInstance().getAppContext(), "monitor.db", 1, this.g);
        } catch (Exception var2) {
            EMLog.d("EMMonitorDB", var2.getMessage());
        }

    }

    public void a(String var1) {
        try {
            if(this.d != null) {
                SQLiteDatabase var2 = this.d.getWritableDatabase();
                ContentValues var3 = new ContentValues();
                var3.put("appname", var1);
                var2.replace("apps", (String)null, var3);
                var2.close();
            }
        } catch (Exception var4) {
            EMLog.e("EMMonitorDB", var4.toString());
        }

    }

    public void b(String var1) {
        try {
            if(this.d != null) {
                SQLiteDatabase var2 = this.d.getWritableDatabase();
                var2.delete("apps", "appname = ?", new String[]{var1});
                var2.close();
            }
        } catch (Exception var3) {
            EMLog.e("EMMonitorDB", var3.toString());
        }

    }

    public List<String> a() {
        ArrayList var1 = new ArrayList();

        try {
            if(this.d != null) {
                SQLiteDatabase var2 = this.d.getReadableDatabase();
                Cursor var3 = var2.rawQuery("select * from apps", (String[])null);
                if(var3 != null) {
                    while(var3.moveToNext()) {
                        String var4 = var3.getString(var3.getColumnIndex("appname"));
                        var1.add(var4);
                    }

                    var3.close();
                }

                var2.close();
            }
        } catch (Exception var5) {
            EMLog.e("EMMonitorDB", var5.toString());
        }

        return var1;
    }

    private static class a extends ContextWrapper {
        private String dirPath;

        public a(Context var1, String var2) {
            super(var1);
            this.dirPath = var2;
        }

        public File getDatabasePath(String var1) {
            File var2 = new File(this.dirPath + File.separator + var1);
            if(!var2.getParentFile().exists()) {
                var2.getParentFile().mkdirs();
            }

            return var2;
        }

        public SQLiteDatabase openOrCreateDatabase(String var1, int var2, SQLiteDatabase.CursorFactory var3) {
            return SQLiteDatabase.openOrCreateDatabase(this.getDatabasePath(var1), var3);
        }

        public SQLiteDatabase openOrCreateDatabase(String var1, int var2, SQLiteDatabase.CursorFactory var3, DatabaseErrorHandler var4) {
            return SQLiteDatabase.openOrCreateDatabase(this.getDatabasePath(var1).getAbsolutePath(), var3, var4);
        }
    }

    private static class b extends SQLiteOpenHelper {
        public b(Context var1, String var2, int var3, String var4) throws EaseMobException {
            super(getCustomContext(var1, var4), var2, (SQLiteDatabase.CursorFactory)null, var3);
        }

        public void onCreate(SQLiteDatabase var1) {
            var1.execSQL("create table apps (appname text primary key);");
        }

        public void onUpgrade(SQLiteDatabase var1, int var2, int var3) {
        }

        private static s.a getCustomContext(Context var0, String var1) throws EaseMobException {
            if(!EasyUtils.isSdcardExist()) {
                throw new EaseMobException("sd card not exist");
            } else {
                return new s.a(var0, var1);
            }
        }
    }
}

