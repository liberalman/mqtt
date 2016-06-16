package com.seaofheart.app.analytics;

/**
 * Created by Administrator on 2016/4/14.
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Collector {
    static boolean collectorEnabled = false;
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    public Collector() {
    }

    public void enableCollector(boolean var1) {
        collectorEnabled = var1;
    }

    public static String timeToString(long var0) {
        Date var2 = new Date(var0);
        SimpleDateFormat var3 = new SimpleDateFormat("mm:ss:SSS");
        String var4 = var3.format(var2);
        return var4;
    }

    public static String getTagPrefix(String var0) {
        return "[" + var0 + "]";
    }
}
