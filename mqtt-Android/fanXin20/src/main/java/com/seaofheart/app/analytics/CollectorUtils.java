package com.seaofheart.app.analytics;

/**
 * Created by Administrator on 2016/4/14.
 */
import java.text.SimpleDateFormat;
import java.util.Date;

public class CollectorUtils {
    public CollectorUtils() {
    }

    public static String timeToString(long var0) {
        Date var2 = new Date(var0);
        SimpleDateFormat var3 = new SimpleDateFormat("mm:ss:SSS");
        String var4 = var3.format(var2);
        return var4;
    }
}
