package com.seaofheart.app.chat.core;

import com.seaofheart.app.chat.ChatConfig.EnvMode;
import com.seaofheart.app.chat.core.j.Jsalahe;
import com.seaofheart.app.chat.core.j.DnsHost;
import com.seaofheart.app.chat.core.j.Address;
import java.util.List;

class o implements m {
    o() {
    }

    public Address a() {
        Address var1 = new Address();
        var1.host = p.getInstance().k();
        var1.port = p.a;
        return var1;
    }

    public List<DnsHost> b() {
        Jsalahe var1 = j.getInstance().i();
        return var1 != null?var1.g:null;
    }

    public boolean c() {
        return p.getInstance().h() && p.getInstance().w() == EnvMode.EMProductMode;
    }

    public boolean d() {
        return true;
    }
}

