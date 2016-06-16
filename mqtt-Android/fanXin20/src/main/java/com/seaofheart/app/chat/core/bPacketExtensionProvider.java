package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class bPacketExtensionProvider implements PacketExtensionProvider { // class b
    public bPacketExtensionProvider() {
    }

    public PacketExtension parseExtension(XmlPullParser var1) throws Exception {
        aDefaultPacketExtension var2 = new aDefaultPacketExtension("received");
        boolean var3 = false;
        String var4 = var1.getAttributeValue("", "mid");
        var2.a(var4);

        while(!var3) {
            int var5 = var1.next();
            if(var5 == 2) {
                String var6 = var1.getName();
                if(var1.isEmptyElementTag()) {
                    var2.setValue(var6, "");
                } else {
                    var5 = var1.next();
                    if(var5 == 4) {
                        String var7 = var1.getText();
                        var2.setValue(var6, var7);
                    }
                }
            } else if(var5 == 3 && var1.getName().equals("received")) {
                var3 = true;
            }
        }

        return var2;
    }
}

