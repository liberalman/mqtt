//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.seaofheart.app.chat.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.seaofheart.app.chat.Chat;

final class v {
    private static final String a = "easemosharedPreferences.sdk.pref";
    private static SharedPreferences sharedPreferences; // b
    private static Editor editor; // c
    private static v instance; // d
    private static String e = "shared_key_ddversion";
    private static String f = "shared_key_ddxml";
    private static String g = "shared_key_ddtime";
    private static String h = "valid_before";
    private static String i = "scheduled_logout_time";
    private static String j = "shared_key_gcm_id";
    private long k = 0L;

    private v(Context var1) {
        sharedPreferences = var1.getSharedPreferences("easemosharedPreferences.sdk.pref", 0);
        editor = sharedPreferences.edit();
    }

    static synchronized v a() {
        if(instance == null) {
            instance = new v(Chat.getInstance().getAppContext());
        }

        return instance;
    }

    public void a(String var1) {
        editor.putString(e, var1);
        editor.commit();
    }

    public void b(String var1) {
        editor.putString(f, var1);
        editor.commit();
    }

    public void a(long var1) {
        editor.putLong(g, var1);
        editor.commit();
    }

    public void b(long var1) {
        editor.putLong(h, var1);
        editor.commit();
    }

    public long b() {
        return sharedPreferences.getLong(h, -1L);
    }

    public String c() {
        return sharedPreferences.getString(e, "");
    }

    public String d() {
        return sharedPreferences.getString(f, "");
    }

    public long e() {
        return sharedPreferences.getLong(g, -1L);
    }

    public boolean f() {
        return this.k != 0L?true:sharedPreferences.contains(i);
    }

    public long g() {
        if(this.k != 0L) {
            return this.k;
        } else {
            this.k = sharedPreferences.getLong(i, -1L);
            return this.k;
        }
    }

    public void c(long var1) {
        this.k = var1;
        editor.putLong(i, var1);
        editor.commit();
    }

    public void h() {
        if(this.f()) {
            this.k = 0L;
            editor.remove(i);
            editor.commit();
        }

    }

    public void a(String var1, String var2) {
        if(var1 == null && var2 == null) {
            editor.remove("debugIM");
            editor.remove("debugRest");
        } else {
            editor.putString("debugIM", var1);
            editor.putString("debugRest", var2);
        }

        editor.commit();
    }

    public String i() {
        return sharedPreferences.getString("debugIM", (String)null);
    }

    public String j() {
        return sharedPreferences.getString("debugRest", (String)null);
    }

    public void c(String var1) {
        editor.putString("debugAppkey", var1);
        editor.commit();
    }

    public String k() {
        return sharedPreferences.getString("debugAppkey", (String)null);
    }

    public void a(boolean var1) {
        editor.putString("debugMode", String.valueOf(var1));
        editor.commit();
    }

    public String l() {
        return sharedPreferences.getString("debugMode", (String)null);
    }

    public void d(String var1) {
        editor.putString(j, var1);
        editor.commit();
    }

    public String m() {
        return sharedPreferences.getString(j, (String)null);
    }
}
