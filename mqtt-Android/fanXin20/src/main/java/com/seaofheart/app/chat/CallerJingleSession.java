package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import com.seaofheart.app.exceptions.NetworkUnconnectedException;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.media.EIce;
import com.seaofheart.app.util.EMLog;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JinglePacketFactory;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.Reason;

import org.jivesoftware.smack.XMPPConnection;
import org.json.JSONObject;

import java.util.List;

class CallerJingleSession extends VoiceCallSession {
    private static final String TAG = CallerJingleSession.class.getSimpleName();
    private boolean callAccepted = false;
    private EIce caller;

    public CallerJingleSession(SessionHandler var1, String var2, XMPPConnection var3) {
        super(var1, var2, var3);
        this.callDirection = CallDirection.OUTGOING;
    }

    public CallerJingleSession buildPeer(String var1) {
        this.peerJid = var1;
        return this;
    }

    public void makeCall() throws EaseMobException {
        EMLog.d(TAG, "callConfig is " + this.callConfig);

        try {
            this.streamParams = new StreamParams();
            JSONObject var1 = new JSONObject(this.callConfig);
            String var2 = var1.getJSONObject("relayMS").getJSONObject("caller").getString("conferenceId");
            this.streamParams.conferenceId = var2;
            this.jingleStreamManager.initStreamParams(this.streamParams);
        } catch (Exception var6) {
            ;
        }

        this.caller = EIce.newCaller(this.callConfig);
        String var7 = this.caller.getLocalContent();
        List var8 = this.jingleStreamManager.createcontentList(var7);
        if(var8 == null) {
            this.closeSession((Reason)null);
        } else {
            JingleIQ var3 = JinglePacketFactory.createSessionInitiate(this.connection.getUser(), this.peerJid, this.sessionId, var7, var8);
            var3.setStatistic(this.getCallStatistic(0));
            EMLog.i(TAG, "CALLER: sending jingle request: " + var3.toXML());

            try {
                this.connection.sendPacket(var3);
            } catch (IllegalStateException var5) {
                var5.printStackTrace();
                throw new NetworkUnconnectedException(var5.toString());
            }

            this.changeState(CallStateChangeListener.CallState.CONNECTING, CallStateChangeListener.CallError.ERROR_NONE);
        }
    }

    protected void closeSession(Reason var1) {
        super.closeSession(var1);
        this.connection.removePacketListener(this.jinglePacketHandler);
    }

    public void handleSessionInitiate(JingleIQ var1) {
    }

    public synchronized void handleSessionAccept(JingleIQ var1) {
        if(this.caller == null) {
            EMLog.d(TAG, "caller=null when handleSessionAccept");
        } else if(!this.checkAndAck(var1)) {
            EMLog.d(TAG, "!checkAndAck(jiq) when handleSessionAccept");
        } else {
            this.state = SessionState.NEGOTIATING_TRANSPORT;

            try {
                String var2 = var1.getSdpJsonString();
                if(var2 == null) {
                    List var3 = var1.getContentList();
                    var2 = this.contentListToJson(var3);
                }

                this.caller.callerNego(var2, (EIce.EIceListener)null);
                String var5 = this.caller.waitforNegoResult();
                this.freeIce();
                this.handleNegoResult(var5);
            } catch (Exception var4) {
                var4.printStackTrace();
                this.closeSession(Reason.FAILED_APPLICATION);
                this.changeState(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_TRANSPORT);
            }

        }
    }

    protected synchronized void freeIce() {
        if(this.caller != null) {
            EMLog.d(TAG, "ice free");
            this.caller.freeCall();
            this.caller = null;
        }

    }

    public void handleSessionTerminate(JingleIQ var1) {
        if(var1.getReason().getReason() == Reason.BUSY) {
            this.noNeedToCloseSesson = true;
            this.free();
            this.state = SessionState.CLOSED;
            this.jinglePacketHandler.removeJingleSession(this);
            this.changeState(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_BUSY);
        } else {
            super.handleSessionTerminate(var1);
            this.callState = CallStateChangeListener.CallState.DISCONNNECTED;
            if(var1.getReason().getReason() == Reason.DECLINE) {
                this.free();
                this.changeState(this.callState, CallStateChangeListener.CallError.REJECTED);
            } else if(var1.getReason().getReason() == Reason.SUCCESS) {
                this.changeState(this.callState, CallStateChangeListener.CallError.ERROR_NONE);
            } else {
                this.free();
                this.changeState(this.callState, CallStateChangeListener.CallError.ERROR_TRANSPORT);
            }

        }
    }

    public void handleCallAccept(JingleIQ var1) {
        EMLog.d(TAG, "the call has been accepted by remote peer!");
        this.callAccepted = true;
        if(this.checkAndAck(var1)) {
            if(this.callState == CallStateChangeListener.CallState.CONNECTED) {
                this.jingleStreamManager.startStream(this.streamParams);
                this.changeState(CallStateChangeListener.CallState.ACCEPTED, CallStateChangeListener.CallError.ERROR_NONE);
            }
        }
    }

    protected void onConnectionConnected() {
        this.changeState(CallStateChangeListener.CallState.CONNECTED, CallStateChangeListener.CallError.ERROR_NONE);
        if(this.callAccepted && !this.jingleStreamManager.streamStarted()) {
            this.jingleStreamManager.startStream(this.streamParams);
            this.changeState(CallStateChangeListener.CallState.ACCEPTED, CallStateChangeListener.CallError.ERROR_NONE);
        }

    }
}

