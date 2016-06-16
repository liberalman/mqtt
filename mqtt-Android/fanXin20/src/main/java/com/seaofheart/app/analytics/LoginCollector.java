package com.seaofheart.app.analytics;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.util.EMLog;

public class LoginCollector extends Collector {
    private static final String TAG = "[Collector][Login]";
    public static final String CHAT_LOGIN_TIME = "chat login time";
    public static final String IM_LOGIN_TIME = "im login time";
    public static final String RETRIEVE_TOKEN_TIME = "retrieve token time";
    public static final String CONNECTION_ERROR_COLLECTION = "connection error collection";

    public LoginCollector() {
    }

    public static void collectLoginTime(long var0) {
        EMLog.d("[Collector][Login]" + getTagPrefix("chat login time"), "chat login time : " + timeToString(var0));
    }

    public static void collectIMLoginTime(long var0) {
        EMLog.d("[Collector][Login]" + getTagPrefix("im login time"), "im login time : " + timeToString(var0));
    }

    public static void collectRetrieveTokenTime(long var0) {
        EMLog.d("[Collector][Login]" + getTagPrefix("retrieve token time"), "retrieve token time : " + timeToString(var0));
    }

    public static void collectConnectionError(String var0) {
    }
}

