package com.seaofheart.app.chat.core;

import java.text.ParseException;
import java.util.Date;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;

public class aa implements PacketExtensionProvider {
    public aa() {
    }

    public PacketExtension parseExtension(XmlPullParser var1) throws Exception {
        String var2 = var1.getAttributeValue("", "stamp");
        Date var3 = null;

        try {
            var3 = StringUtils.parseDate(var2);
        } catch (ParseException var5) {
            if(var3 == null) {
                var3 = new Date(0L);
            }
        }

        z var4 = new z(var3);
        return var4;
    }
}
