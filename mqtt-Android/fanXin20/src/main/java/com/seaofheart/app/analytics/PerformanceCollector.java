package com.seaofheart.app.analytics;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.chat.Group;
import com.seaofheart.app.util.EMLog;


public class PerformanceCollector extends Collector {
    public static final String TAG = "[Collector][Perf]";
    public static final String RETRIEVE_GROUPS = "retrieve groups from server time";
    public static final String LOADING_ALL_CONVERSATIONS = "load all conversations time";
    public static final String LOAD_ALL_LOCAL_GROUPS = "load all local groups";
    public static final String LOAD_ALL_LOCAL_CHAT_ROOMS = "load all local chat rooms";
    public static final String RETRIEVE_ROSTER = "retrieve roster";
    public static final String DOWNLOAD_FILE = "download file time";
    public static final String UPLOAD_FILE = "upload file time";
    public static final String SYNC_GROUPS = "sync groups time";
    public static final String RETRIEVE_GROUP = "retrieve group detail";

    public PerformanceCollector() {
    }

    public static void collectRetrieveGroupsFromServerTime(int var0, long var1) {
        EMLog.d("[Collector][Perf]" + getTagPrefix("retrieve groups from server time"), "time spent on loading groups size : " + var0 + " with time spent : " + timeToString(var1));
    }

    public static void collectLoadingAllConversations(int var0, int var1, long var2) {
        EMLog.d("[Collector][Perf]" + getTagPrefix("load all conversations time"), "time spent on loading all conversations : conversation size " + var0 + " messages count : " + var1 + " with time spent : " + timeToString(var2));
    }

    public static void collectLoadAllLocalGroups(int var0, long var1) {
        EMLog.d("[Collector][Perf]" + getTagPrefix("load all local groups"), "load all local group with size : " + var0 + " timeSpent : " + timeToString(var1));
    }

    public static void collectLoadAllLocalChatRooms(int var0, long var1) {
        EMLog.d("[Collector][Perf]" + getTagPrefix("load all local chat rooms"), "load all local chat rooms with size : " + var0 + " timeSpent : " + timeToString(var1));
    }

    public static void collectRetrieveRoster(int var0, long var1) {
        EMLog.d("[Collector][Perf]" + getTagPrefix("retrieve roster"), "retrieve roster with size : " + var0 + " timeSpent : " + timeToString(var1));
    }

    public static void collectDownloadFileTime(TimeTag var0, long var1, String var3) {
        EMLog.d("[Collector][Perf]" + getTagPrefix("download file time"), "download file : " + var3 + " size : " + var1 + " time spent : " + var0.timeStr() + " speed(bytes/s) : " + var1 / var0.timeSpent() * 1000L);
    }

    public static void collectUploadFileTime(TimeTag var0, long var1, String var3) {
        EMLog.d("[Collector][Perf]" + getTagPrefix("upload file time"), "upload file : " + var3 + " size : " + var1 + " time spent : " + var0.timeStr() + " speed(bytes/s) : " + var1 / var0.timeSpent() * 1000L);
    }

    public static void collectSyncWithServerGroups(int var0, long var1) {
        EMLog.d("[Collector][Perf]" + getTagPrefix("sync groups time"), "sync groups with server with group size : " + var0 + " timeSpent : " + timeToString(var1));
    }

    public static void collectRetrieveGroupFromServer(Group var0, long var1) {
        if(var0 != null) {
            EMLog.d("[Collector][Perf]" + getTagPrefix("retrieve group detail"), "retrieve group details from server with group id : " + var0.getId() + " group name : " + var0.getName() + " members : " + var0.getAffiliationsCount() + " time spent : " + timeToString(var1));
        }
    }
}

