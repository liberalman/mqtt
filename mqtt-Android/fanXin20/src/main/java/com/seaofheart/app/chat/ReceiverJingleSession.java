package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.media.EIce;
import com.seaofheart.app.util.EMLog;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JinglePacketFactory;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.Reason;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.SessionInfoType;

import org.jivesoftware.smack.XMPPConnection;

import java.util.Iterator;
import java.util.List;

class ReceiverJingleSession extends VoiceCallSession {
    private static final String TAG = ReceiverJingleSession.class.getSimpleName();
    private List<ContentPacketExtension> acceptedContent = null;
    private String acceptedContentJsonStr = null;
    private EIce callee;

    public ReceiverJingleSession(SessionHandler var1, String var2, XMPPConnection var3) {
        super(var1, var2, var3);
        this.callDirection = CallDirection.INCOMING;
    }

    protected void closeSession(Reason var1) {
        super.closeSession(var1);
    }

    public void handleCallerRelay(JingleIQ var1) {
        EMLog.d(TAG, "handleCallerRelay : " + var1.getReason().getText());
        this.jingleIQ = var1;
        this.ack(this.jingleIQ);
    }

    public void handleSessionAccept(JingleIQ var1) {
    }

    public void answerCall() {
        EMLog.d(TAG, "start answer call");
        if(this.callState != CallStateChangeListener.CallState.CONNECTED && this.callState != CallStateChangeListener.CallState.ACCEPTED) {
            EMLog.i(TAG, "Accepting incomig call!");
            JingleIQ var1 = JinglePacketFactory.createCallAccept(this.myJid, this.peerJid, this.sessionId);
            var1.setStatistic(this.getCallStatistic(0));
            this.connection.sendPacket(var1);
            this.jingleStreamManager.startStream(this.streamParams);
            this.changeState(CallStateChangeListener.CallState.CONNECTED, CallStateChangeListener.CallError.ERROR_NONE);
            this.changeState(CallStateChangeListener.CallState.ACCEPTED, CallStateChangeListener.CallError.ERROR_NONE);
        }
    }

    private void acceptContent(final boolean var1) {
        (new Thread(new Runnable() {
            public void run() {
                try {
                    boolean var1x = true;
                    if(ReceiverJingleSession.this.jingleIQ.getSdpJsonString() != null) {
                        ReceiverJingleSession.this.acceptedContentJsonStr = ReceiverJingleSession.this.jingleIQ.getSdpJsonString();
                    } else {
                        var1x = false;
                        ReceiverJingleSession.this.acceptedContent = ReceiverJingleSession.this.jingleIQ.getContentList();
                        ReceiverJingleSession.this.acceptedContentJsonStr = ReceiverJingleSession.this.contentListToJson(ReceiverJingleSession.this.acceptedContent);
                    }

                    EMLog.i(ReceiverJingleSession.TAG, "Accepting incomig jingle call!");
                    VoiceCallManager.CallType var7 = var1? VoiceCallManager.CallType.video: VoiceCallManager.CallType.audio;
                    ReceiverJingleSession.this.buildCallConfig(VoiceCallManager.getInstance().getCallConfig(var7, false, (List)null, (List)null));
                    ReceiverJingleSession.this.callee = EIce.newCallee(ReceiverJingleSession.this.callConfig, ReceiverJingleSession.this.acceptedContentJsonStr);
                    String var3 = ReceiverJingleSession.this.callee.getLocalContent();
                    JingleIQ var4 = null;
                    if(var1x) {
                        var4 = JinglePacketFactory.createSessionAccept(ReceiverJingleSession.this.myJid, ReceiverJingleSession.this.peerJid, ReceiverJingleSession.this.sessionId, var3);
                    } else {
                        List var5 = ReceiverJingleSession.this.jingleStreamManager.createcontentList(var3);
                        var4 = JinglePacketFactory.createSessionAccept(ReceiverJingleSession.this.myJid, ReceiverJingleSession.this.peerJid, ReceiverJingleSession.this.sessionId, var5);
                    }

                    var4.setStatistic(ReceiverJingleSession.this.getCallStatistic(0));
                    ReceiverJingleSession.this.connection.sendPacket(var4);
                    ReceiverJingleSession.this.state = SessionState.NEGOTIATING_TRANSPORT;
                    ReceiverJingleSession.this.callee.calleeNego((EIce.EIceListener)null);
                    ReceiverJingleSession.this.changeState(CallStateChangeListener.CallState.CONNECTING, CallStateChangeListener.CallError.ERROR_NONE);
                    String var8 = ReceiverJingleSession.this.callee.waitforNegoResult();
                    ReceiverJingleSession.this.freeIce();
                    ReceiverJingleSession.this.handleNegoResult(var8);
                } catch (Exception var6) {
                    var6.printStackTrace();
                    EMLog.w(ReceiverJingleSession.TAG, "An error occured. Rejecting call!");
                    JingleIQ var2 = JinglePacketFactory.createCancel(ReceiverJingleSession.this.myJid, ReceiverJingleSession.this.peerJid, ReceiverJingleSession.this.sessionId);
                    ReceiverJingleSession.this.connection.sendPacket(var2);
                    ReceiverJingleSession.this.closeSession(Reason.FAILED_APPLICATION);
                    ReceiverJingleSession.this.changeState(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_TRANSPORT);
                }

            }
        })).start();
    }

    protected synchronized void freeIce() {
        if(this.callee != null) {
            EMLog.d(TAG, "callee free ice");
            this.callee.freeCall();
            this.callee = null;
        } else {
            EMLog.d(TAG, "callee is null when free ice");
        }

    }

    public void rejectCall() {
        EMLog.i(TAG, "Rejecting call!");

        try {
            this.closeSession(Reason.DECLINE);
        } catch (Exception var2) {
            var2.printStackTrace();
            EMLog.e(TAG, var2.getMessage());
        }

        this.changeState(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_NONE);
    }

    void rejectSessionInitiate() {
        EMLog.d(TAG, "try to reject an incoming session initiate IQ request : from peer " + this.peerJid + " session id = " + this.sessionId);
        this.onBusy();
    }

    void acceptSessionInitiate() {
        EMLog.d(TAG, "accept an incoming session initiate request : from peer " + this.peerJid + " session id = " + this.sessionId);
        List var1 = this.jingleIQ.getContentList();
        boolean var2 = false;
        Iterator var4 = var1.iterator();

        while(var4.hasNext()) {
            ContentPacketExtension var3 = (ContentPacketExtension)var4.next();
            String var5 = var3.getAttributeAsString("name");
            if(VoiceCallManager.CallType.video.toString().equals(var5)) {
                var2 = true;
                break;
            }
        }

        if(!var2) {
            this.jingleStreamManager = new JingleStreamManager(ContentPacketExtension.CreatorEnum.responder);
        } else {
            this.jingleStreamManager = new JingleStreamManager(ContentPacketExtension.CreatorEnum.responder, VoiceCallManager.CallType.video);
        }

        try {
            this.acceptContent(var2);
        } catch (Exception var6) {
            var6.printStackTrace();
            EMLog.d(TAG, var6.getMessage());
        }

    }

    public void handleSessionInitiate(JingleIQ var1) {
        EMLog.d(TAG, "call from : " + var1 + " is ringing!");
        this.jingleIQ = var1;
        this.ack(this.jingleIQ);
        this.peerJid = var1.getFrom();
        VoiceCallManager.getInstance().onJingleInitiateAction(this);
    }

    public void handleSessionTerminate(JingleIQ var1) {
        this.callState = CallStateChangeListener.CallState.DISCONNNECTED;
        this.freeIce();
        super.handleSessionTerminate(var1);
        this.changeState(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_NONE);
    }

    public void handleSessionInfo(JingleIQ var1) {
        if(this.check(var1)) {
            SessionInfoType var2 = var1.getSessionInfo().getType();
            switch(var2.ordinal()) { // $SWITCH_TABLE$net$java$sip$communicator$impl$protocol$jabber$extensions$jingle$SessionInfoType()[var2.ordinal()]
                case 3:
                    this.changeState(CallStateChangeListener.CallState.VOICE_PAUSE, CallStateChangeListener.CallError.ERROR_NONE);
                case 4:
                case 5:
                default:
                    break;
                case 6:
                    this.changeState(CallStateChangeListener.CallState.VOICE_RESUME, CallStateChangeListener.CallError.ERROR_NONE);
                    break;
                case 7:
                    this.changeState(CallStateChangeListener.CallState.VIDEO_PAUSE, CallStateChangeListener.CallError.ERROR_NONE);
                    break;
                case 8:
                    this.changeState(CallStateChangeListener.CallState.VIDEO_RESUME, CallStateChangeListener.CallError.ERROR_NONE);
            }
        }

    }

    public void onConnectionConnected() {
        VoiceCallManager.getInstance().onCallRinging(this);
    }

    private boolean acceptCallFrom(String var1) {
        return true;
    }
}

