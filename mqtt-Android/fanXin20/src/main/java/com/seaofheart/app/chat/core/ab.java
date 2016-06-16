package com.seaofheart.app.chat.core;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;

public class ab extends IQ {
    private static final String b = "jabber:iq:version";
    String a;

    public ab(String var1) {
        this.a = var1;
        this.setType(Type.RESULT);
    }

    public String getChildElementXML() {
        StringBuilder var1 = new StringBuilder();
        var1.append("<query xmlns=\"").append("jabber:iq:version").append("\">");
        var1.append("<name>easemob</name>");
        var1.append("<version>" + this.a + "</version>");
        var1.append("<os>android</os>");
        var1.append("</query>");
        return var1.toString();
    }
}

