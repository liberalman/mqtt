package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.chat.ChatManager;
import com.seaofheart.app.util.EMLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cleaner implements r { // class d
    private static final String TAG = "EMCleaner";
    private List<e> b = Collections.synchronizedList(new ArrayList());
    private ExecutorService c = null;
    private int d = 1;

    protected Cleaner() {
    }

    public static Cleaner getInstance(int var0) {
        Cleaner var1 = new Cleaner();
        var1.d = var0;
        return var1;
    }

    public void a(e var1) {
        if(var1 != null) {
            if(!this.c(var1)) {
                this.b.add(var1);
                this.a();
            }
        }
    }

    public void a(List<e> var1) {
        this.b.clear();
        this.b.addAll(var1);
        this.a();
    }

    public void b(e var1) {
        List var2 = this.b;
        synchronized(this.b) {
            Iterator var3 = this.b.iterator();

            while(var3.hasNext()) {
                if(((e)var3.next()).equals(var1)) {
                    var3.remove();
                }
            }

        }
    }

    public boolean c(e var1) {
        List var2 = this.b;
        synchronized(this.b) {
            Iterator var3 = this.b.iterator();

            while(var3.hasNext()) {
                if(((e)var3.next()).equals(var1)) {
                    return true;
                }
            }

            return false;
        }
    }

    private synchronized void a() {
        this.c.submit(new Runnable() {
            public void run() {
                if(Cleaner.this.b.size() > 0) {
                    while(true) {
                        if(ChatManager.getInstance().isConnected()) {
                            e var1 = null;

                            try {
                                if(Cleaner.this.b.size() <= 0) {
                                    return;
                                }

                                var1 = (e)Cleaner.this.b.get(0);
                                var1.run();
                                Cleaner.this.b(var1);
                                if(Cleaner.this.b.size() > 0) {
                                    Cleaner.this.a();
                                }
                            } catch (Exception var3) {
                                EMLog.d("EMCleaner", "cmd : " + var1.toString() + " with exception : " + var3.toString());
                                Cleaner.this.a();
                            }
                            break;
                        }

                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException var4) {
                            EMLog.d("EMCleaner", "should logout happend since cleaner\'s interrupted");
                            return;
                        }

                        EMLog.d("EMCleaner", "try checking connection again after waiting 1 second.");
                    }
                }

            }
        });
    }

    void d(final e var1) {
        this.c.submit(new Runnable() {
            public void run() {
                for(; !ChatManager.getInstance().isConnected(); EMLog.d("EMCleaner", "try checking connection again after waiting 1 second.")) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException var3) {
                        EMLog.d("EMCleaner", "should logout happend since cleaner\'s interrupted");
                        return;
                    }
                }

                if(var1 != null) {
                    try {
                        var1.run();
                    } catch (Exception var2) {
                        Cleaner.this.d(var1);
                    }
                }

            }
        });
    }

    public void onInit() {
        this.c = Executors.newFixedThreadPool(this.d);
    }

    public void onDestroy() {
        if(this.c != null) {
            this.c.shutdownNow();
        }

        this.b.clear();
    }
}

