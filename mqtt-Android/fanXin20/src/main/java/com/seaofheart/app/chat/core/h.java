package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.chat.ChatConfig;

import java.util.List;

class h implements m {
    h() {
    }

    public j.Address a() {
        j.Address var1 = new j.Address();
        var1.host = p.getInstance().p();
        var1.port = 80;
        var1.protocol = p.getInstance().l()?"https":"http";
        return var1;
    }

    public List<j.DnsHost> b() {
        j.Jsalahe var1 = j.getInstance().i();
        return var1 != null?var1.f:null;
    }

    public boolean c() {
        return p.getInstance().j() && p.getInstance().w() == ChatConfig.EnvMode.EMProductMode;
    }

    public boolean d() {
        return false;
    }
}
