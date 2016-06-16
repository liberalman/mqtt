package com.seaofheart.app.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.annotation.SuppressLint;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Process;

@SuppressLint({"NewApi"})
public class net {
    static final String TAG = "net";
    protected static net instance = null;
    static long c = 0L;
    static long d = 0L;
    static long e = 0L;
    static long f = 0L;
    static long g = 0L;
    static long h = 0L;
    static long i = 0L;
    static long j = 0L;
    static long k;
    static long l;
    static long m = 0L;
    static long n = 0L;
    static long o = 0L;
    static long p = 0L;
    static long q = 0L;
    static long r = 0L;
    static int s;
    static long t = 0L;
    static long u = 0L;
    static boolean v = false;

    public net() {
    }

    public static void a() {
        s = Process.myUid();
        b();
        v = true;
    }

    public static void b() {
        c = TrafficStats.getUidRxBytes(s);
        d = TrafficStats.getUidTxBytes(s);
        if(Build.VERSION.SDK_INT >= 12) {
            e = TrafficStats.getUidRxPackets(s);
            f = TrafficStats.getUidTxPackets(s);
        } else {
            e = 0L;
            f = 0L;
        }

        k = 0L;
        l = 0L;
        m = 0L;
        n = 0L;
        o = 0L;
        p = 0L;
        q = 0L;
        r = 0L;
        u = System.currentTimeMillis();
        t = System.currentTimeMillis();
    }

    public static void c() {
        v = false;
        b();
    }

    public static void d() {
        if(v) {
            Long var0 = Long.valueOf(System.currentTimeMillis());
            long var1 = (var0.longValue() - t) / 1000L;
            if(var1 == 0L) {
                var1 = 1L;
            }

            o = TrafficStats.getUidRxBytes(s);
            p = TrafficStats.getUidTxBytes(s);
            k = o - c;
            l = p - d;
            g += k;
            h += l;
            if(Build.VERSION.SDK_INT >= 12) {
                q = TrafficStats.getUidRxPackets(s);
                r = TrafficStats.getUidTxPackets(s);
                m = q - e;
                n = r - f;
                i += m;
                j += n;
            }

            if(k == 0L && l == 0L) {
                EMLog.d("net", "no network traffice");
            } else {
                EMLog.d("net", l + " bytes send; " + k + " bytes received in " + var1 + " sec");
                if(Build.VERSION.SDK_INT >= 12 && n > 0L) {
                    EMLog.d("net", n + " packets send; " + m + " packets received in " + var1 + " sec");
                }

                EMLog.d("net", "total:" + h + " bytes send; " + g + " bytes received");
                if(Build.VERSION.SDK_INT >= 12 && j > 0L) {
                    long var3 = (System.currentTimeMillis() - u) / 1000L;
                    EMLog.d("net", "total:" + j + " packets send; " + i + " packets received in " + var3);
                }

                c = o;
                d = p;
                e = q;
                f = r;
                t = var0.longValue();
            }

        }
    }
}

