package com.seaofheart.app.chat.core;

/**
 * Created by Administrator on 2016/4/16.
 */
import org.jivesoftware.smack.packet.PacketExtension;

public class k implements PacketExtension {
    public static final String a = "encrypt";
    public static final String b = "jabber:client";

    public k() {
    }

    public String getElementName() {
        return "encrypt";
    }

    public String getNamespace() {
        return "jabber:client";
    }

    public String toXML() {
        return "<encrypt/>";
    }
}
