package com.seaofheart.app.chat.core;

import com.seaofheart.app.chat.core.x.a;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class y implements PacketExtensionProvider {
    public y() {
    }

    public PacketExtension parseExtension(XmlPullParser var1) throws Exception {
        x var2 = new x();
        String var3 = var1.getAttributeValue("", "type");
        var2.a(a.valueOf(var3));
        return var2;
    }
}