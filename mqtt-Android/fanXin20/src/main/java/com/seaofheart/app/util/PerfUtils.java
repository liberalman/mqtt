package com.seaofheart.app.util;

/**
 * Created by Administrator on 2016/4/14.
 */
public class PerfUtils {
    public PerfUtils() {
    }

    public static int getTimeSpendSecond(long var0) {
        int var2 = (int)(System.currentTimeMillis() - var0);
        if(var2 == 0) {
            var2 = 1;
        }

        return var2;
    }

    public static int getSpeed(long var0, long var2) {
        return (int)((float)var0 / (float)(var2 / 1000L));
    }
}
