package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.util.NetUtils;
import com.xonami.javaBells.DefaultJingleSession;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidatePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JinglePacketFactory;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.Reason;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.SessionInfoType;

import org.jivesoftware.smack.XMPPConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

class VoiceCallSession extends DefaultJingleSession {
    private static final String TAG = VoiceCallSession.class.getSimpleName();
    protected JingleStreamManager jingleStreamManager;
    protected CallStateChangeListener stateChangeListener = null;
    protected JingleIQ jingleIQ = null;
    protected CallStateChangeListener.CallState callState;
    protected StreamParams streamParams;
    protected boolean isLocalHostCandiate;
    protected boolean noNeedToCloseSesson;
    CallDirection callDirection;
    protected String callConfig;
    protected boolean isRelayCall;

    String getPeerJid() {
        return this.peerJid;
    }

    VoiceCallSession build(JingleStreamManager var1) {
        this.jingleStreamManager = var1;
        return this;
    }

    VoiceCallSession buildCallConfig(String var1) {
        this.callConfig = var1;
        return this;
    }

    VoiceCallSession registerCallStateListener(CallStateChangeListener var1) {
        this.stateChangeListener = var1;
        return this;
    }

    protected VoiceCallSession(SessionHandler var1, String var2, XMPPConnection var3) {
        super(var1, var2, var3);
        this.callState = CallStateChangeListener.CallState.IDLE;
        this.streamParams = null;
        this.isLocalHostCandiate = true;
        this.noNeedToCloseSesson = false;
        this.callDirection = null;
        this.isRelayCall = false;
        if(!VoiceCallManager.getInstance().isActiveCallOngoing()) {
            var1.setActiveSession(this);
        }

    }

    protected void freeIce() {
    }

    protected String contentListToJson(List<ContentPacketExtension> var1) {
        try {
            JSONObject var2 = new JSONObject();
            JSONArray var3 = new JSONArray();
            Iterator var5 = var1.iterator();

            while(var5.hasNext()) {
                ContentPacketExtension var4 = (ContentPacketExtension)var5.next();
                String var6 = var4.getName();
                List var7 = var4.getChildExtensionsOfType(IceUdpTransportPacketExtension.class);
                IceUdpTransportPacketExtension var8 = (IceUdpTransportPacketExtension)var7.get(0);
                List var9 = var8.getCandidateList();
                Iterator var11 = var9.iterator();

                while(var11.hasNext()) {
                    CandidatePacketExtension var10 = (CandidatePacketExtension)var11.next();
                    JSONObject var12 = new JSONObject();
                    if(var6.equals(VoiceCallManager.CallType.video.toString())) {
                        var12.put("component", var10.getComponent() + 2);
                    } else {
                        var12.put("component", var10.getComponent());
                    }

                    var12.put("foundation", var10.getFoundation());
                    var12.put("generation", var10.getGeneration());
                    var12.put("id", var10.getID());
                    var12.put("ip", var10.getIP());
                    var12.put("network", var10.getNetwork());
                    var12.put("port", var10.getPort());
                    var12.put("priority", var10.getPriority());
                    var12.put("protocol", var10.getProtocol());
                    var12.put("type", var10.getType());
                    var3.put(var12);
                }

                if(!var2.has("pwd")) {
                    var2.put("pwd", var8.getPassword());
                }

                if(!var2.has("ufrag")) {
                    var2.put("ufrag", var8.getUfrag());
                }
            }

            var2.put("candidates", var3);
            return var2.toString();
        } catch (JSONException var13) {
            var13.printStackTrace();
            return null;
        }
    }

    CallDirection getCallDirection() {
        return this.callDirection;
    }

    CallStateChangeListener.CallState getCallState() {
        return this.callState;
    }

    public void handleCallAccept(JingleIQ var1) {
    }

    public void handleCallerRelay(JingleIQ var1) {
    }

    public void onBusy() {
        this.connection.sendPacket(JinglePacketFactory.createSessionTerminate(this.myJid, this.peerJid, this.sessionId, Reason.BUSY, (String)null));
        this.state = SessionState.CLOSED;
        this.jinglePacketHandler.removeJingleSession(this);
    }

    void endCall() {
        if(this.callState != CallStateChangeListener.CallState.CONNECTED && this.callState != CallStateChangeListener.CallState.ACCEPTED) {
            this.closeSession(Reason.DECLINE);
            this.changeState(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_NONE);
        } else {
            this.closeSession(Reason.SUCCESS);
            this.changeState(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_NONE);
        }

    }

    public boolean isVideoCall() {
        return this.jingleStreamManager != null?this.jingleStreamManager.isVideoCall():false;
    }

    protected void handleNegoResult(String var1) {
        try {
            JSONObject var2 = new JSONObject(var1);
            com.easemob.util.EMLog.d(TAG, "negotiation complete, result: " + var1);
            int var3 = var2.getInt("result");
            if(var3 != 0) {
                this.onNegotiationFailed();
            } else {
                this.onNegotiationSuccessed(var2);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
            this.onNegotiationFailed();
        }

    }

    protected void onNegotiationFailed() {
        com.easemob.util.EMLog.e(TAG, "negotiation fails");
        String var1 = this.getConferenceId();
        if(var1 != null) {
            VoiceCallManager.getInstance().removeP2PConference(var1);
        }

        this.closeSession(Reason.CONNECTIVITY_ERROR);
        this.changeState(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_TRANSPORT);
    }

    protected String getConferenceId() {
        String var1 = null;
        if(this.callConfig != null && !"{}".equals(this.callConfig)) {
            try {
                JSONObject var2 = new JSONObject(this.callConfig);
                var1 = var2.getJSONObject("relayMS").getJSONObject("caller").getString("conferenceId");
            } catch (JSONException var3) {
                ;
            }
        }

        return var1;
    }

    protected void onNegotiationSuccessed(JSONObject var1) {
        com.easemob.util.EMLog.e(TAG, "negotiation success");
        JingleIQ var2 = JinglePacketFactory.createCallerRelay(this.myJid, this.peerJid, this.sessionId, "disabled");
        this.connection.sendPacket(var2);
        if(this.streamParams == null) {
            this.streamParams = new StreamParams();
        }

        try {
            String var3 = null;
            this.isRelayCall = false;
            if(var1.has("pairs")) {
                var3 = var1.getString("pairs");
            } else {
                var3 = var1.getString("relay_pairs");
                this.isRelayCall = true;
            }

            this.streamParams.isRelayCall = this.isRelayCall;
            JSONArray var4 = new JSONArray(var3);

            for(int var5 = 0; var5 < var4.length(); ++var5) {
                JSONObject var6 = var4.getJSONObject(var5);
                int var7 = var6.getInt("comp_id");
                JSONObject var8 = null;
                JSONObject var9 = null;
                if(this.isVoiceCallCandidate(var7)) {
                    var8 = var6.getJSONObject("local");
                    this.streamParams.localPort = var8.getInt("port");
                    this.streamParams.localAddress = var8.getString("ip");
                    var9 = var6.getJSONObject("remote");
                    this.streamParams.remotePort = var9.getInt("port");
                    this.streamParams.remoteAddress = var9.getString("ip");
                } else if(this.isVideoCallCandidate(var7, this.isRelayCall)) {
                    var8 = var6.getJSONObject("local");
                    this.streamParams.videoLocalPort = var8.getInt("port");
                    var9 = var6.getJSONObject("remote");
                    this.streamParams.videoRemotePort = var9.getInt("port");
                    this.streamParams.videoRemoteAddress = var9.getString("ip");
                }

                if(this.isRelayCall) {
                    if(var7 == 2) {
                        this.streamParams.videoChannelId = var9.getInt("channelId");
                    } else {
                        this.streamParams.channelId = var9.getInt("channelId");
                    }

                    this.streamParams.conferenceId = var9.getString("conferenceId");
                    this.streamParams.rcode = var9.getString("rcode");
                }
            }

            if(this.streamParams.conferenceId == null) {
                this.streamParams.conferenceId = this.sessionId;
            }

            if(this.streamParams.rcode == null) {
                this.streamParams.rcode = "-1";
            }
        } catch (JSONException var10) {
            var10.printStackTrace();
            com.easemob.util.EMLog.d(TAG, "parse nogetiation result fail : " + var10.getMessage());
            this.onNegotiationFailed();
            return;
        }

        if(this.callState == CallStateChangeListener.CallState.DISCONNNECTED) {
            com.easemob.util.EMLog.d(TAG, "call state is DISCONNNECTED");
        } else {
            this.onConnectionConnected();
        }
    }

    private boolean isVideoCallCandidate(int var1, boolean var2) {
        return var1 == 3 || var2 && var1 == 2;
    }

    private boolean isVoiceCallCandidate(int var1) {
        return var1 == 1;
    }

    protected void onConnectionConnected() {
        this.changeState(CallStateChangeListener.CallState.CONNECTED, CallStateChangeListener.CallError.ERROR_NONE);
        this.jingleStreamManager.startStream(this.streamParams);
    }

    protected void closeSession(final Reason var1) {
        this.free();
        if(this.state != SessionState.CLOSED) {
            if(var1 != null) {
                (new Thread(new Runnable() {
                    public void run() {
                        try {
                            JingleIQ var1x = JinglePacketFactory.createSessionTerminate(VoiceCallSession.this.myJid, VoiceCallSession.this.peerJid, VoiceCallSession.this.sessionId, var1, (String)null);
                            var1x.setStatistic(VoiceCallSession.this.getCallStatistic(VoiceCallSession.this.jingleStreamManager.callCostTime));
                            VoiceCallSession.this.connection.sendPacket(var1x);
                        } catch (IllegalStateException var2) {
                            var2.printStackTrace();
                            com.easemob.util.EMLog.e("DefaultJingleSession", "no connection!");
                        }

                    }
                })).start();
            }

            com.easemob.util.EMLog.d(TAG, "close sesstion, state: " + this.state);
            this.state = SessionState.CLOSED;
            this.jinglePacketHandler.removeJingleSession(this);
            this.callState = CallStateChangeListener.CallState.DISCONNNECTED;
        }
    }

    public void sendSessionChangeInfo(SessionInfoType var1) {
        JingleIQ var2 = JinglePacketFactory.createSessionInfo(this.myJid, this.peerJid, this.sessionId, var1);

        try {
            this.connection.sendPacket(var2);
        } catch (IllegalStateException var4) {
            var4.printStackTrace();
        }

    }

    protected String getCallStatistic(int var1) {
        String var2 = NetUtils.getNetworkType(Chat.getInstance().getAppContext());
        String var3 = "android," + Chat.getInstance().getVersion();
        String var4 = this.getCallDirection() == CallDirection.OUTGOING?"caller":"callee";
        String var5 = this.jingleStreamManager.isVideoCall()?"video":"audio";
        StringBuilder var6 = new StringBuilder();
        var6.append("<statistics network=\"" + var2 + "\"").append(" version=\"" + var3 + "\"").append(" identity=\"" + var4 + "\"").append(" type=\"" + var5 + "\"");
        if(var1 > 0) {
            var6.append(" duration=\"" + var1 + "\"");
        }

        var6.append("/>");
        return var6.toString();
    }

    protected void changeState(CallStateChangeListener.CallState var1, CallStateChangeListener.CallError var2) {
        if(VoiceCallManager.getInstance().getActiveSession() != null && this.getSessionId() != null) {
            if(this.getSessionId().equals(VoiceCallManager.getInstance().getActiveSession().getSessionId())) {
                this.callState = var1;
                if(this.stateChangeListener != null) {
                    this.stateChangeListener.onCallStateChanged(var1, var2);
                }

            }
        }
    }

    protected void free() {
        this.freeIce();
        if(this.jingleStreamManager != null) {
            this.jingleStreamManager.stopStream();
        }

        SessionHandler var1 = (SessionHandler)this.jinglePacketHandler;
        var1.setActiveSession((VoiceCallSession)null);
    }

    void onTimerOut() {
        this.closeSession(Reason.TIMEOUT);
        this.changeState(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_NORESPONSE);
    }
}

