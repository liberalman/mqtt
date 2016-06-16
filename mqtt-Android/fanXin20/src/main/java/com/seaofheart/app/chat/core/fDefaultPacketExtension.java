package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.jivesoftware.smack.packet.DefaultPacketExtension;

public class fDefaultPacketExtension extends DefaultPacketExtension { // f
    public static final String a = "ConferencePacketExtension";
    public static final String b = "urn:xmpp:media-conference";
    public static final String c = "channelId";
    public static final String d = "server-port";
    public static final String e = "serverIp";
    public static final String f = "cspeaker";
    public static final String g = "rcode";
    public static final String h = "result";
    public static final String i = "action";
    public static final String j = "username";
    public static final String k = "vchannelId";
    public static final String l = "conferenceId";
    public static final String m = "serverPort";

    public fDefaultPacketExtension() {
        super("ConferencePacketExtension", "urn:xmpp:media-conference");
    }

    public fDefaultPacketExtension(String var1) {
        super(var1, "urn:xmpp:media-conference");
    }

    public void a(String var1) {
        this.setValue("username", var1);
    }

    public String a() {
        return this.getValue("username");
    }

    public void b(String var1) {
        this.setValue("vchannelId", var1);
    }

    public String b() {
        return this.getValue("vchannelId");
    }

    public void c(String var1) {
        this.setValue("conferenceId", var1);
    }

    public String c() {
        return this.getValue("conferenceId");
    }

    public void d(String var1) {
        this.setValue("serverIp", var1);
    }

    public String d() {
        return this.getValue("serverIp");
    }

    public void e(String var1) {
        this.setValue("rcode", var1);
    }

    public String e() {
        return this.getValue("rcode");
    }

    public void f(String var1) {
        this.setValue("serverPort", var1);
    }

    public String f() {
        return this.getValue("serverPort");
    }

    public void g(String var1) {
        this.setValue("channelId", var1);
    }

    public String g() {
        return this.getValue("channelId");
    }

    public void h(String var1) {
        this.setValue("action", var1);
    }

    public String h() {
        return this.getValue("action");
    }

    public void i(String var1) {
        this.setValue("result", var1);
    }

    public String i() {
        return this.getValue("result");
    }
}
