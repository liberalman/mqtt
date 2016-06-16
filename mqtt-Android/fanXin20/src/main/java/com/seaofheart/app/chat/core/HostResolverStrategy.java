package com.seaofheart.app.chat.core;

/**
 * Created by Administrator on 2016/4/16.
 */
class HostResolverStrategy implements g { // class n
    private static final String TAG = "EMHostResolverStrategy";
    private HostResolver b = null;
    private HostResolver c = null;
    private HostResolver d = null;

    public HostResolverStrategy() {
        this.b = new HostResolver();
        this.b.a(new o());
        this.b.a(this);
        this.c = new HostResolver();
        this.c.a(new w());
        this.c.a(this);
        this.d = new HostResolver();
        this.d.a(new h());
        this.d.a(this);
    }

    public j.Address b() {
        return this.b.b();
    }

    public j.Address c() {
        return this.c.b();
    }

    public j.Address d() {
        return this.d.b();
    }

    public int e() {
        return this.b.d();
    }

    public int f() {
        return this.c.d();
    }

    public int g() {
        return this.d.d();
    }

    public j.Address h() {
        return this.d.c();
    }

    public j.Address i() {
        return this.b.c();
    }

    public j.Address j() {
        return this.c.c();
    }

    public void a() {
        if(this.b != null) {
            this.b.a();
        }

        if(this.c != null) {
            this.c.a();
        }

        if(this.d != null) {
            this.d.a();
        }

    }

    void k() {
        if(this.b != null) {
            this.b.e();
        }

        if(this.c != null) {
            this.c.e();
        }

        if(this.d != null) {
            this.d.e();
        }

    }
}
