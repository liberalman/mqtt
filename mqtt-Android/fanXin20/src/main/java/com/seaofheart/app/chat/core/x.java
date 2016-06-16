//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.seaofheart.app.chat.core;

import org.jivesoftware.smack.packet.PacketExtension;

public class x implements PacketExtension {
    private x.a c;
    //public static final String a = "roomtype";
    public static final String b = "easemob:x:roomtype";

    public x() {
        this.c = x.a.a;
    }

    public String getElementName() {
        return "roomtype";
    }

    public String getNamespace() {
        return "easemob:x:roomtype";
    }

    public String toXML() {
        StringBuilder var1 = new StringBuilder();
        var1.append("<").append(this.getElementName()).append(" xmlns=\"").append(this.getNamespace()).append("\"");
        var1.append(" type=\"").append(this.c.toString()).append("\"/>");
        return var1.toString();
    }

    public x.a a() {
        return this.c;
    }

    public void a(x.a var1) {
        this.c = var1;
    }

    public static enum a {
        a;

        private a() {
        }
    }
}
