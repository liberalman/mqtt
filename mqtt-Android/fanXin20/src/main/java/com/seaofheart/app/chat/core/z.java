package com.seaofheart.app.chat.core;

/**
 * Created by Administrator on 2016/4/16.
 */
import java.util.Date;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;

public class z implements PacketExtension {
    private Date a;

    public z(Date var1) {
        this.a = var1;
    }

    public Date a() {
        return this.a;
    }

    public String getElementName() {
        return "ts";
    }

    public String getNamespace() {
        return "urn:xmpp:timestamp";
    }

    public String toXML() {
        StringBuilder var1 = new StringBuilder();
        var1.append("<").append(this.getElementName()).append(" xmlns=\"").append(this.getNamespace()).append("\"");
        var1.append(" stamp=\"");
        var1.append(StringUtils.formatXEP0082Date(this.a()));
        var1.append("\"");
        var1.append(">");
        var1.append("</").append(this.getElementName()).append(">");
        return var1.toString();
    }
}
