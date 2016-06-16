package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.util.EMLog;
import com.xonami.javaBells.JinglePacketHandler;
import com.xonami.javaBells.JingleSession;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleAction;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.Reason;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

class SessionHandler extends JinglePacketHandler {
    private static final String TAG = SessionHandler.class.getSimpleName();
    protected VoiceCallSession activeSession = null;

    public void processPacket(Packet var1) {
        JingleIQ var2 = (JingleIQ)var1;
        if(var2.getType() == IQ.Type.ERROR) {
            EMLog.e(TAG, "error is received with error code = " + var2.getError().getCode());
            if(var2.getError().getCode() == 503 && this.activeSession != null) {
                this.activeSession.changeState(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_INAVAILABLE);
                this.activeSession.closeSession((Reason)null);
            }

        } else if(this.isGeneralJiq(var2)) {
            super.processPacket(var1);
        } else {
            JingleSession var3 = (JingleSession)this.jingleSessions.get(var2.getSID());
            if(this.jiqAccepted(var2)) {
                VoiceCallSession var4;
                if(var2.getAction() == JingleAction.CALL_ACCEPT) {
                    if(var3 == null) {
                        return;
                    }

                    var4 = (VoiceCallSession)var3;
                    var4.handleCallAccept(var2);
                } else if(var2.getAction() == JingleAction.CALLER_RELAY) {
                    if(var3 == null) {
                        return;
                    }

                    var4 = (VoiceCallSession)var3;
                    var4.handleCallerRelay(var2);
                } else {
                    super.processPacket(var1);
                }
            }

        }
    }

    VoiceCallSession getActiveSession() {
        return this.activeSession;
    }

    void setActiveSession(VoiceCallSession var1) {
        this.activeSession = var1;
    }

    SessionHandler(XMPPConnection var1) {
        super(var1);
    }

    protected boolean isGeneralJiq(JingleIQ var1) {
        JingleAction var2 = var1.getAction();
        return var2 == JingleAction.CONTENT_ADD || var2 == JingleAction.CONTENT_MODIFY || var2 == JingleAction.CONTENT_ACCEPT || var2 == JingleAction.CONTENT_REJECT || var2 == JingleAction.CONTENT_REMOVE || var2 == JingleAction.DESCRIPTION_INFO || var2 == JingleAction.SECURITY_INFO || var2 == JingleAction.SESSION_INFO || var2 == JingleAction.SESSION_TERMINATE;
    }

    protected boolean jiqAccepted(JingleIQ var1) {
        return false;
    }
}

