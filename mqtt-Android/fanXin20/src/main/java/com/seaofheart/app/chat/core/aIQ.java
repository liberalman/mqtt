package com.seaofheart.app.chat.core;

import org.jivesoftware.smack.packet.IQ;

public class aIQ extends IQ {
    public static final String a = "urn:xmpp:ping";
    public static final String b = "ping";

    public aIQ() {
    }

    public String getChildElementXML() {
        return this.getType() == Type.RESULT?null:"<ping xmlns=\"urn:xmpp:ping\" />";
    }
}
