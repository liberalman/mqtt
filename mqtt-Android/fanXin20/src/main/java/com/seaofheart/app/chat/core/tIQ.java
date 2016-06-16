package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.IQ;

import java.util.ArrayList;
import java.util.List;

public class tIQ extends IQ { // t
    //public static final String a = "urn:xmpp:media-conference";
    public static final String b = "query";
    private final List<fDefaultPacketExtension> c = new ArrayList();
    private List<p.classc> d = null;

    public tIQ() {
    }

    public String getChildElementXML() {
        StringBuilder var1 = new StringBuilder();
        var1.append("<query xmlns=\"urn:xmpp:media-conference\">");
        var1.append(this.getExtensionsXML());
        var1.append("</query>");
        return var1.toString();
    }

    public List<fDefaultPacketExtension> a() {
        return this.c;
    }

    public void a(fDefaultPacketExtension var1) {
        this.c.add(var1);
    }

    public void a(List<p.classc> var1) {
        this.d = var1;
    }

    public List<p.classc> b() {
        return this.d;
    }

    public static tIQ a(boolean var0, String var1) {
        tIQ var2 = new tIQ();
        fDefaultPacketExtension var3 = new fDefaultPacketExtension();
        var3.a(var1);
        if(!var0) {
            var3.h(tIQ.a.a.a());
        } else {
            var3.h(tIQ.a.b.a());
        }

        var2.addExtension(var3);
        var2.setType(Type.SET);
        return var2;
    }

    public static tIQ a(String var0) {
        tIQ var1 = new tIQ();
        fDefaultPacketExtension var2 = new fDefaultPacketExtension();
        var2.c(var0);
        var2.h(tIQ.a.c.a());
        var1.addExtension(var2);
        var1.setType(Type.SET);
        return var1;
    }

    private static enum a {
        a("join-p2p-voice"),
        b("join-p2p-video"),
        c("remove-p2p");

        private final String d;

        private a(String var3) {
            this.d = var3;
        }

        public String a() {
            return this.d;
        }
    }

    class b extends DefaultPacketExtension {
        public static final String a = "turnServerList";
        static final String b = "turnServer";

        public b() {
            super("turnServerList", "urn:xmpp:media-conference");
        }
    }
}

