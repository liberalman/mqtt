package com.seaofheart.app.chat.core;

import com.seaofheart.app.util.EMLog;

import java.util.List;

/**
 * Created by Administrator on 2016/4/16.
 */
public class HostResolver { // class l
    private static final String TAG = "EMHostResolver"; // a
    private j.Jsalahe b = null;
    private int c = -1;
    private int d = -1;
    private boolean e = true;
    private j.Address f = null;
    private m g = null;
    private static final int h = 1;
    private int i = 1;
    private g jjj = null; // j

    HostResolver() {
    }

    void a(g var1) {
        this.jjj = var1;
    }

    synchronized j.Address b() {
        return this.f == null?this.c():this.f;
    }

    synchronized j.Address c() {
        if(!this.g.c()) {
            if(this.f == null && this.g != null) {
                this.f = this.g.a();
            }

            return this.f;
        } else {
            if(this.b == null) {
                this.b = j.getInstance().i();
            }

            if(this.b == null) {
                EMLog.e("EMHostResolver", "failed to get dns config");
                if(this.f == null) {
                    this.f = new j.Address();
                    this.f = this.g.a();
                }

                return this.f;
            } else {
                j.DnsHost var1 = null;
                if(this.f == null) {
                    var1 = this.f();
                    this.f = new j.Address();
                    if(var1 == null) {
                        EMLog.e("EMHostResolver", "dns config did not return the ip list : " + this.b.a());
                        this.e = false;
                        var1 = this.g();
                    }
                } else {
                    if(this.e) {
                        var1 = this.f();
                    }

                    if(var1 == null) {
                        j.getInstance().o();
                        this.e = false;
                        var1 = this.g();
                    }

                    if(var1 == null) {
                        if(this.i <= 0) {
                            this.f = this.g.a();
                            this.i = 1;
                        } else {
                            --this.i;
                            if(this.g.d()) {
                                j.Jsalahe var2 = j.getInstance().n();
                                if(var2 != null) {
                                    if(this.b != var2) {
                                        this.b = var2;
                                        if(this.jjj != null) {
                                            this.jjj.a();
                                        }
                                    }

                                    this.e = true;
                                    this.c = -1;
                                    this.d = -1;
                                    var1 = this.f();
                                    if(var1 == null) {
                                        var1 = this.g();
                                    }
                                }
                            }
                        }
                    }
                }

                if(var1 != null) {
                    if(this.f == null) {
                        this.f = new j.Address();
                    }

                    this.f.host = this.e?var1.ip:var1.domain;
                    this.f.port = var1.port;
                    this.f.protocol = var1.protocol;
                    this.f.dnsHost = var1;
                } else {
                    this.e = true;
                    this.c = -1;
                    this.d = -1;
                    this.f = this.g.a();
                }

                if(this.f != null) {
                    EMLog.d("EMHostResolver", "the next availabe host : " + this.f.a());
                } else {
                    EMLog.e("EMHostResolver", "no availabe host is selected, we should not reach here!!");
                }

                return this.f;
            }
        }
    }

    int d() {
        List var1 = this.g.b();
        return var1 != null && var1.size() > 0?var1.size() * 2 - (this.c + 1) - (this.d + 1):0;
    }

    void e() {
        this.b = null;
        this.f = null;
        this.c = -1;
        this.d = -1;
        this.e = true;
    }

    private void a(j.Jsalahe var1) {
        if(var1 != null && var1 != this.b) {
            this.b = var1;
        }

    }

    private j.DnsHost f() {
        if(this.b != null) {
            List var1 = this.g.b();
            ++this.c;
            if(var1 != null && this.c < var1.size()) {
                while(this.c < var1.size()) {
                    j.DnsHost var2 = this.a(this.c);
                    if(var2 != null && var2.ip != null && !var2.ip.trim().equals("")) {
                        return var2;
                    }

                    ++this.c;
                }
            }
        }

        this.c = -1;
        return null;
    }

    private j.DnsHost g() {
        if(this.b != null) {
            List var1 = this.g.b();
            ++this.d;
            if(var1 != null && this.d < var1.size()) {
                while(this.d < var1.size()) {
                    j.DnsHost var2 = this.a(this.d);
                    if(var2 != null && var2.domain != null && !var2.domain.trim().equals("")) {
                        return var2;
                    }

                    ++this.d;
                }
            }
        }

        this.d = -1;
        return null;
    }

    private j.DnsHost a(int var1) {
        if(this.b != null) {
            List var2 = this.g.b();
            if(var2 == null) {
                return null;
            }

            int var3 = var2.size();
            if(var3 > 0 && var1 < var3) {
                return (j.DnsHost)var2.get(var1);
            }
        }

        return null;
    }

    void a(m var1) {
        this.g = var1;
    }

    public void a() {
        if(this.b != null && this.b != j.getInstance().i()) {
            this.e();
            this.a(j.getInstance().i());
        }

    }
}
