//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.seaofheart.app.chat.core;

import org.jivesoftware.smack.packet.DefaultPacketExtension;

public class aDefaultPacketExtension extends DefaultPacketExtension { // class a
    public static final String a = "received";
    public static final String b = "acked";
    public static final String c = "delivery";
    public static final String d = "request";
    public static final String e = "urn:xmpp:receipts";
    public static final String f = "id";
    private String g = "";

    public aDefaultPacketExtension(String var1) {
        super(var1, "urn:xmpp:receipts");
    }

    public String toXML() {
        StringBuilder var1 = new StringBuilder();
        var1.append("<").append(this.getElementName()).append(" xmlns=\"").append(this.getNamespace()).append("\" ");
        var1.append("id=\"").append(this.getValue("id")).append("\"/>");
        return var1.toString();
    }

    public String a() {
        return this.g;
    }

    public void a(String var1) {
        this.g = var1;
    }
}
