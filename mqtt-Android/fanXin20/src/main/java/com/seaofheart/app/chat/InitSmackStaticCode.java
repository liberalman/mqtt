package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/16.
 */
import android.content.Context;
import org.jivesoftware.smack.PrivacyListManager;
import org.jivesoftware.smackx.LastActivityManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.entitycaps.packet.CapsExtension;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class InitSmackStaticCode {
    public InitSmackStaticCode() {
    }

    public static void initStaticCode(Context var0) {
        ClassLoader var1 = var0.getClassLoader();

        try {
            Class.forName(ServiceDiscoveryManager.class.getName(), true, var1);
            Class.forName(PrivacyListManager.class.getName(), true, var1);
            Class.forName(MultiUserChat.class.getName(), true, var1);
            Class.forName(LastActivityManager.class.getName(), true, var1);
            Class.forName(CapsExtension.class.getName(), true, var1);
        } catch (ClassNotFoundException var3) {
            throw new IllegalStateException("Could not init static class blocks", var3);
        }
    }
}
