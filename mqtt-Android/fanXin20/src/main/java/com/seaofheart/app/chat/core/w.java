package com.seaofheart.app.chat.core;

/**
 * Created by Administrator on 2016/4/15.
 */
import com.seaofheart.app.chat.ChatConfig.EnvMode;
import com.seaofheart.app.chat.core.j.Jsalahe;
import com.seaofheart.app.chat.core.j.DnsHost;
import com.seaofheart.app.chat.core.j.Address;
import java.util.List;

class w implements m {
    w() {
    }

    public Address a() {
        Address var1 = new Address();
        var1.host = p.getInstance().o();
        var1.port = 80;
        var1.protocol = p.getInstance().l()?"https":"http";
        return var1;
    }

    public List<DnsHost> b() {
        Jsalahe var1 = j.getInstance().i();
        return var1 != null?var1.h:null;
    }

    public boolean c() {
        return p.getInstance().i() && p.getInstance().w() == EnvMode.EMProductMode;
    }

    public boolean d() {
        return true;
    }
}