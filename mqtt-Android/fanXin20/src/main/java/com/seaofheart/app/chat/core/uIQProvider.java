package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

public class uIQProvider implements IQProvider { // u
    public uIQProvider() {
    }

    public IQ parseIQ(XmlPullParser var1) throws Exception {
        tIQ var2 = new tIQ();
        boolean var3 = false;

        while(!var3) {
            int var4 = var1.next();
            if(var4 == 2) {
                if(var1.getName().equals("ConferencePacketExtension")) {
                    var2.a(this.a(var1));
                } else if(var1.getName().equals("turnServerList")) {
                    var2.a(this.b(var1));
                }
            } else if(var4 == 3 && var1.getName().equals("query")) {
                var3 = true;
            }
        }

        return var2;
    }

    private fDefaultPacketExtension a(XmlPullParser var1) throws Exception {
        boolean var2 = false;
        fDefaultPacketExtension var3 = new fDefaultPacketExtension();

        while(!var2) {
            int var4 = var1.next();
            if(var4 == 2) {
                if(var1.getName().equals("username")) {
                    var3.a(var1.nextText());
                } else if(var1.getName().equals("vchannelId")) {
                    var3.b(var1.nextText());
                } else if(var1.getName().equals("conferenceId")) {
                    var3.c(var1.nextText());
                } else if(var1.getName().equals("serverIp")) {
                    var3.d(var1.nextText());
                } else if(var1.getName().equals("rcode")) {
                    var3.e(var1.nextText());
                } else if(var1.getName().equals("serverPort")) {
                    var3.f(var1.nextText());
                } else if(var1.getName().equals("channelId")) {
                    var3.g(var1.nextText());
                } else if(var1.getName().equals("result")) {
                    var3.i(var1.nextText());
                }
            } else if(var4 == 3 && var1.getName().equals("ConferencePacketExtension")) {
                var2 = true;
            }
        }

        return var3;
    }

    private List<p.classc> b(XmlPullParser var1) throws Exception {
        ArrayList var2 = new ArrayList();
        boolean var3 = false;

        while(!var3) {
            int var4 = var1.next();
            if(var4 == 2) {
                if(var1.getName().equals("turnServer")) {
                    String var5 = var1.nextText();
                    String var6 = var5.substring(0, var5.lastIndexOf(":"));
                    String var7 = var5.substring(var5.indexOf(":") + 1);
                    p.classc var8 = new p.classc();
                    var8.a = var6;
                    var8.b = Integer.parseInt(var7);
                    var2.add(var8);
                }
            } else if(var4 == 3 && var1.getName().equals("turnServerList")) {
                var3 = true;
            }
        }

        return var2.size() == 0?null:var2;
    }
}

