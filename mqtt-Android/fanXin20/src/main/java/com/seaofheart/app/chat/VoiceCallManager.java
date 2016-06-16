package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.EMConnectionListener;
import com.seaofheart.app.a.a;
import com.seaofheart.app.chat.core.fDefaultPacketExtension;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.chat.core.tIQ;
import com.seaofheart.app.exceptions.NetworkUnconnectedException;
import com.seaofheart.app.exceptions.NoActiveCallException;
import com.seaofheart.app.exceptions.ServiceNotReadyException;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.media.EIce;
import com.seaofheart.app.util.EMLog;
import com.xonami.javaBells.JingleSession;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleAction;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.SessionInfoType;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class VoiceCallManager {
    private static final String TAG = VoiceCallManager.class.getSimpleName();
    private static VoiceCallManager instance = null;
    private SessionHandler incomingCallListener = null;
    private SessionHandler outgoingCallHandler = null;
    private CallStateChangeListener stateChangeListener = null;
    //private VoiceCallManager.CallStateChangeListenerDelegate stateChangeListenerDelegate = new VoiceCallManager.CallStateChangeListenerDelegate((VoiceCallManager.CallStateChangeListenerDelegate)null);
    private VoiceCallManager.CallStateChangeListenerDelegate stateChangeListenerDelegate = new VoiceCallManager.CallStateChangeListenerDelegate();
    private boolean inited = false;
    private VoiceCallSession activeSession = null;
    private Thread makingCallThread = null;
    private Timer callingTimer = new Timer();
    private boolean callWasEnded = false;
    private static final long CALLING_TIMEROUT = 50000L;
    private JingleStreamManager jsm;

    private VoiceCallManager() {
    }

    synchronized void init() {
        if(!this.inited) {
            this.registerConnectionListener();
            this.registerIceLogListener();
            this.inited = true;
        }
    }

    private void registerIceLogListener() {
        EIce.registerLogListener(new EIce.LogListener() {
            public void onLog(int var1, String var2) {
                EMLog.d("EICE", var2);
            }
        });
    }

    static synchronized VoiceCallManager getInstance() {
        if(instance == null) {
            instance = new VoiceCallManager();
        }

        return instance;
    }

    boolean isActiveCallOngoing() {
        return this.activeSession != null;
    }

    VoiceCallSession getActiveSession() {
        return this.activeSession;
    }

    public boolean isDirectCall() {
        return this.getActiveSession() != null?!this.getActiveSession().isRelayCall:true;
    }

    CallDirection getCallDirection() {
        return this.activeSession == null?this.activeSession.getCallDirection():CallDirection.NONE;
    }

    void addStateChangeListener(CallStateChangeListener var1) {
        this.stateChangeListener = var1;
    }

    void removeStateChangeListener(CallStateChangeListener var1) {
        this.stateChangeListener = null;
    }

    private void startListeningCall() throws ServiceNotReadyException {
        if(this.incomingCallListener == null) {
            /*final XMPPConnection var1 = SessionManager.getInstance().getConnection();
            if(var1 != null && var1.isConnected()) {
                this.incomingCallListener = new SessionHandler(var1) {
                    protected boolean jiqAccepted(JingleIQ var1) {
                        JingleAction var2 = var1.getAction();
                        return var2 == JingleAction.SESSION_INITIATE || var2 == JingleAction.CALLER_RELAY;
                    }

                    public JingleSession createJingleSession(String var1, JingleIQ var2) {
                        if(VoiceCallManager.this.activeSession != null && VoiceCallManager.this.activeSession.callDirection == CallDirection.OUTGOING) {
                            return null;
                        } else {
                            ReceiverJingleSession var3 = (ReceiverJingleSession)(new ReceiverJingleSession(this, var1, this.connection)).registerCallStateListener(VoiceCallManager.this.stateChangeListenerDelegate);
                            return var3;
                        }
                    }
                };
            } else {
                throw new ServiceNotReadyException("no connection is initialized!");
            }*/
        }
    }

    void makeCall(final String var1, final VoiceCallManager.CallType var2) throws ServiceNotReadyException {
        /*XMPPConnection var3 = SessionManager.getInstance().getConnection();
        if(var3 != null && var3.isConnected()) {
            if(this.activeSession != null && this.activeSession.getCallDirection() == CallDirection.OUTGOING) {
                this.activeSession.endCall();
            }

            if(this.activeSession != null && this.activeSession.getCallDirection() == CallDirection.INCOMING) {
                this.activeSession.onBusy();
            }

            if(this.outgoingCallHandler != null) {
                var3.removePacketListener(this.outgoingCallHandler);
            }

            this.makingCallThread = new Thread() {
                public void run() {
                    VoiceCallManager.this.syncMakeCall(var1, var2);
                }
            };
            this.makingCallThread.start();
        } else {
            throw new ServiceNotReadyException("no connection is initialized!");
        }*/
    }

    private synchronized void syncMakeCall(String var1, VoiceCallManager.CallType var2) {
        /*final XMPPConnection var3 = SessionManager.getInstance().getConnection();
        if(var3 != null && var3.isConnected()) {
            if(this.activeSession != null && this.activeSession.getCallDirection() == CallDirection.INCOMING) {
                this.activeSession.onBusy();
            }

            final String var4 = ContactManager.getEidFromUserName(var1) + "/mobile";
            this.jsm = new JingleStreamManager(ContentPacketExtension.CreatorEnum.initiator, var2);
            List var5 = null;
            List var6 = null;

            try {
                tIQ var7 = this.joinP2PConference(var2, var1);
                var5 = var7.a();
                var6 = var7.b();
            } catch (EaseMobException var11) {
                var11.printStackTrace();
            }

            final String var12 = this.getCallConfig(var2, true, var5, var6);
            this.outgoingCallHandler = new SessionHandler(var3) {
                protected boolean jiqAccepted(JingleIQ var1) {
                    JingleAction var2 = var1.getAction();
                    return var2 == JingleAction.CALL_ACCEPT || var2 == JingleAction.SESSION_ACCEPT;
                }

                public JingleSession createJingleSession(String var1, JingleIQ var2) {
                    return (new CallerJingleSession(this, var1, this.connection)).buildPeer(var4).build(VoiceCallManager.this.jsm).buildCallConfig(var12).registerCallStateListener(VoiceCallManager.this.stateChangeListenerDelegate);
                }
            };
            CallerJingleSession var8 = (CallerJingleSession)this.outgoingCallHandler.createInitateJingleSession(JingleIQ.generateSID());
            this.activeSession = var8;

            try {
                var8.makeCall();
            } catch (EaseMobException var10) {
                this.stateChangeListenerDelegate.onCallStateChanged(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_TRANSPORT);
            }

            this.makingCallThread = null;
            this.startCallingTimer();
        } else {
            this.stateChangeListenerDelegate.onCallStateChanged(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_TRANSPORT);
        }*/
    }

    public void pauseVoiceTransfer() {
        if(this.jsm != null) {
            this.jsm.pauseVoiceStream();
            VoiceCallSession var1 = this.getActiveSession();
            var1.sendSessionChangeInfo(SessionInfoType.mute);
        }

    }

    public void resumeVoiceTransfer() {
        if(this.jsm != null) {
            this.jsm.resumeVoiceStream();
            VoiceCallSession var1 = this.getActiveSession();
            var1.sendSessionChangeInfo(SessionInfoType.unmute);
        }

    }

    public void pauseVideoTransfer() {
        if(this.jsm != null) {
            this.jsm.pauseVideoStream();
            VoiceCallSession var1 = this.getActiveSession();
            if(var1 != null) {
                var1.sendSessionChangeInfo(SessionInfoType.videoPause);
            }
        }

    }

    public void resumeVideoTransfer() {
        if(this.jsm != null) {
            this.jsm.resumeVideoStream();
            VoiceCallSession var1 = this.getActiveSession();
            if(var1 != null) {
                var1.sendSessionChangeInfo(SessionInfoType.videoResume);
            }
        }

    }

    public int getVoiceInputLevel() {
        return this.jsm != null?this.jsm.getVoiceInputLevel():0;
    }

    public int getVoiceRemoteBitrate() {
        return this.jsm != null?this.jsm.getVoiceRemoteBitrate():0;
    }

    public String getCallConfig(VoiceCallManager.CallType var1, boolean var2, List<fDefaultPacketExtension> var3, List<p.classc> var4) {
        List var5 = p.getInstance().P();
        if(var2) {
            var5 = var4;
        }

        JSONObject var6 = new JSONObject();
        JSONArray var7 = new JSONArray();

        JSONObject var9;
        try {
            if(var1 == VoiceCallManager.CallType.video) {
                var6.put("compCount", 4);
            } else {
                var6.put("compCount", 2);
            }

            if(var5 != null) {
                for(int var8 = 0; var8 < var5.size(); ++var8) {
                    var9 = new JSONObject();
                    var9.put("host", ((p.classc)var5.get(var8)).a);
                    var9.put("port", ((p.classc)var5.get(var8)).b);
                    var7.put(var9);
                }

                var6.put("turnAddrs", var7);
            }
        } catch (JSONException var14) {
            var14.printStackTrace();
            EMLog.e(TAG, "get turn server config fail");
            return "{}";
        }

        if(var3 == null) {
            return var6.toString();
        } else {
            JSONObject var15 = new JSONObject();
            var9 = new JSONObject();
            JSONObject var10 = new JSONObject();

            try {
                Iterator var12 = var3.iterator();

                while(var12.hasNext()) {
                    fDefaultPacketExtension var11 = (fDefaultPacketExtension)var12.next();
                    if(var11.a().toLowerCase().equals(ChatManager.getInstance().getCurrentUser().toLowerCase())) {
                        this.putConferenceConfig(var9, var11);
                        var15.put("caller", var9);
                    } else {
                        this.putConferenceConfig(var10, var11);
                        var15.put("callee", var10);
                    }
                }

                var6.put("relayMS", var15);
            } catch (JSONException var13) {
                var13.printStackTrace();
            }

            return var6.toString();
        }
    }

    private void putConferenceConfig(JSONObject var1, fDefaultPacketExtension var2) throws JSONException {
        var1.put("conferenceId", var2.c()).put("serverIp", var2.d()).put("rcode", var2.e()).put("serverPort", Integer.parseInt(var2.f())).put("channelId", Integer.parseInt(var2.g()));
        if(var2.b() != null) {
            var1.put("vchannelId", Integer.parseInt(var2.b()));
        }

    }

    private void startCallingTimer() {
        this.callingTimer = new Timer();
        this.callingTimer.schedule(new TimerTask() {
            public void run() {
                if(VoiceCallManager.this.activeSession != null) {
                    CallStateChangeListener.CallState var1 = VoiceCallManager.this.activeSession.getCallState();
                    if(var1 != CallStateChangeListener.CallState.ACCEPTED || var1 != CallStateChangeListener.CallState.DISCONNNECTED) {
                        VoiceCallManager.this.activeSession.onTimerOut();
                    }
                }

            }
        }, 50000L);
    }

    void answerCall() throws NoActiveCallException, NetworkUnconnectedException {
        final ReceiverJingleSession var1 = (ReceiverJingleSession)this.activeSession;

        try {
            this.checkConnection();
        } catch (NetworkUnconnectedException var3) {
            var3.printStackTrace();
            if(var1 != null) {
                var1.endCall();
            }

            throw new NetworkUnconnectedException("Please check your connection!");
        }

        if(var1 != null) {
            (new Thread() {
                public void run() {
                    if(var1 != null) {
                        VoiceCallManager.this.jsm = var1.jingleStreamManager;
                        var1.answerCall();
                    }

                }
            }).start();
        } else {
            EMLog.e(TAG, "no imcoming active call");
            throw new NoActiveCallException("no imcoming active call");
        }
    }

    void rejectCall() throws NoActiveCallException {
        final ReceiverJingleSession var1 = (ReceiverJingleSession)this.activeSession;
        if(var1 != null) {
            (new Thread() {
                public void run() {
                    if(var1 != null) {
                        var1.rejectCall();
                    }

                }
            }).start();
        } else {
            EMLog.e(TAG, "no imcoming active call");
            throw new NoActiveCallException("no imcoming active call");
        }
    }

    void endCall() {
        this.callingTimer.cancel();
        (new Thread(new Runnable() {
            public void run() {
                if(VoiceCallManager.this.makingCallThread != null && VoiceCallManager.this.makingCallThread.isAlive()) {
                    EMLog.d(VoiceCallManager.TAG, "endcall and wait for call thread end");
                    long var1 = System.currentTimeMillis();

                    try {
                        VoiceCallManager.this.makingCallThread.join();
                    } catch (InterruptedException var4) {
                        var4.printStackTrace();
                    }

                    EMLog.d(VoiceCallManager.TAG, "wait for call thread cost time : " + (System.currentTimeMillis() - var1));
                }

                if(VoiceCallManager.this.activeSession == null) {
                    EMLog.w(VoiceCallManager.TAG, "no active call!");
                    VoiceCallManager.this.stateChangeListenerDelegate.onCallStateChanged(CallStateChangeListener.CallState.DISCONNNECTED, CallStateChangeListener.CallError.ERROR_NONE);
                } else {
                    if(VoiceCallManager.this.activeSession != null) {
                        EMLog.d(VoiceCallManager.TAG, "end an active call with call direction = " + VoiceCallManager.this.activeSession.getCallDirection());
                        if(VoiceCallManager.this.activeSession != null) {
                            VoiceCallManager.this.activeSession.endCall();
                        }
                    }

                }
            }
        })).start();
    }

    synchronized void onCallRinging(VoiceCallSession var1) {
        if(this.activeSession != null && this.activeSession != var1) {
            var1.onBusy();
        } else {
            if(this.activeSession == null) {
                this.activeSession = var1;
            }

            if(var1 != null && var1.isVideoCall()) {
                ChatManager.getInstance().notifyIncomingCall(this.activeSession.getPeerJid(), VoiceCallManager.CallType.video);
            } else {
                ChatManager.getInstance().notifyIncomingCall(this.activeSession.getPeerJid(), VoiceCallManager.CallType.audio);
            }

        }
    }

    void onJingleInitiateAction(VoiceCallSession var1) {
        ReceiverJingleSession var2 = (ReceiverJingleSession)var1;
        if(this.activeSession != null) {
            var2.rejectSessionInitiate();
        } else {
            this.activeSession = var1;
            var2.acceptSessionInitiate();
        }

    }

    void registerConnectionListener() {
        ChatManager.getInstance().addConnectionListener(new EMConnectionListener() {
            public void onConnected() {
                try {
                    /*XMPPConnection var1 = SessionManager.getInstance().getConnection();
                    if(var1 != null && VoiceCallManager.this.incomingCallListener != null) {
                        var1.removePacketListener(VoiceCallManager.this.incomingCallListener);
                    }*/

                    VoiceCallManager.this.incomingCallListener = null;
                    VoiceCallManager.this.startListeningCall();
                } catch (ServiceNotReadyException var2) {
                    var2.printStackTrace();
                    EMLog.w(VoiceCallManager.TAG, var2.getMessage());
                }

            }

            public void onDisconnected(int var1) {
            }
        });
    }

    /*public tIQ joinP2PConference(VoiceCallManager.CallType var1, String var2) throws EaseMobException {
        XMPPConnection var3 = SessionManager.getInstance().getConnection();
        tIQ var4 = tIQ.a(var1 == VoiceCallManager.CallType.video, var2);
        AndFilter var5 = new AndFilter(new PacketFilter[]{new PacketIDFilter(var4.getPacketID()), new PacketTypeFilter(IQ.class)});
        PacketCollector var6 = var3.createPacketCollector(var5);
        var3.sendPacket(var4);
        tIQ var7 = (tIQ)var6.nextResult(6000L);
        var6.cancel();
        if(var7 == null) {
            throw new EaseMobException(-1001, "No response from server.");
        } else if(var7.getType() == IQ.Type.ERROR) {
            throw new EaseMobException(-998, var7.getError().toString());
        } else {
            return var7;
        }
    }*/

    public void removeP2PConference(String var1) {
        try {
            /*XMPPConnection var2 = SessionManager.getInstance().getConnection();
            tIQ var3 = tIQ.a(var1);
            var2.sendPacket(var3);*/
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    private void checkConnection() throws NetworkUnconnectedException {
        /*XMPPConnection var1 = SessionManager.getInstance().getConnection();
        if(var1 == null || !var1.isConnected()) {
            throw new NetworkUnconnectedException("no connection is initialized!");
        }*/
    }

    private class CallStateChangeListenerDelegate implements CallStateChangeListener {
        boolean needMonitor;
        int curBitrate;
        long badNetworkStartTime;
        long noNetworkStartTime;
        CallState callState;
        static final int ratedInternal = 6000;
        boolean isFirstTime;

        private CallStateChangeListenerDelegate() {
            this.needMonitor = false;
            this.curBitrate = 0;
            this.badNetworkStartTime = 0L;
            this.noNetworkStartTime = 0L;
            this.isFirstTime = true;
        }

        public void onCallStateChanged(CallState var1, CallError var2) {
            EMLog.d(VoiceCallManager.TAG, "onCallStateChanged with callState = " + var1 + " callError = " + var2);
            this.callState = var1;
            if(var1 == CallState.ACCEPTED) {
                VoiceCallManager.this.callingTimer.cancel();
                this.needMonitor = true;
                this.startMonitor();
            }

            if(var1 == CallState.DISCONNNECTED) {
                VoiceCallManager.this.activeSession = null;
                VoiceCallManager.this.callingTimer.cancel();
                this.stopMonitor();
            }

            if(VoiceCallManager.this.stateChangeListener != null) {
                VoiceCallManager.this.stateChangeListener.onCallStateChanged(var1, var2);
            }

        }

        void startMonitor() {
            synchronized(this) {
                if(this.needMonitor) {
                    (new Thread(new Runnable() {
                        public void run() {
                            while(CallStateChangeListenerDelegate.this.needMonitor) {
                                try {
                                    Thread.sleep(1500L);
                                } catch (InterruptedException var4) {
                                    var4.printStackTrace();
                                }

                                if(VoiceCallManager.getInstance().getActiveSession() == null) {
                                    return;
                                }

                                boolean var1 = VoiceCallManager.getInstance().getActiveSession().isVideoCall();
                                if(var1) {
                                    CallStateChangeListenerDelegate.this.curBitrate = a.a().n();
                                } else {
                                    CallStateChangeListenerDelegate.this.curBitrate = VoiceCallManager.getInstance().getVoiceRemoteBitrate();
                                }

                                long var2 = System.currentTimeMillis();
                                if(CallStateChangeListenerDelegate.this.isFirstTime) {
                                    var2 += 2000L;
                                }

                                if(var1 && CallStateChangeListenerDelegate.this.curBitrate <= 90) {
                                    if(CallStateChangeListenerDelegate.this.curBitrate > 0 || CallStateChangeListenerDelegate.this.callState != CallState.VIDEO_PAUSE) {
                                        CallStateChangeListenerDelegate.this.isFirstTime = false;
                                        if(CallStateChangeListenerDelegate.this.badNetworkStartTime == 0L) {
                                            CallStateChangeListenerDelegate.this.badNetworkStartTime = var2;
                                        }

                                        if(CallStateChangeListenerDelegate.this.noNetworkStartTime == 0L && CallStateChangeListenerDelegate.this.curBitrate <= 0) {
                                            CallStateChangeListenerDelegate.this.noNetworkStartTime = var2;
                                        }

                                        if(CallStateChangeListenerDelegate.this.curBitrate <= 0) {
                                            if(var2 - CallStateChangeListenerDelegate.this.noNetworkStartTime >= 6000L && VoiceCallManager.this.stateChangeListener != null) {
                                                VoiceCallManager.this.stateChangeListener.onCallStateChanged(CallState.NETWORK_UNSTABLE, CallError.ERROR_NO_DATA);
                                            }
                                        } else if(var2 - CallStateChangeListenerDelegate.this.badNetworkStartTime >= 6000L && VoiceCallManager.this.stateChangeListener != null) {
                                            VoiceCallManager.this.stateChangeListener.onCallStateChanged(CallState.NETWORK_UNSTABLE, CallError.ERROR_NONE);
                                        }
                                    }
                                } else if(!var1 && CallStateChangeListenerDelegate.this.curBitrate <= 3) {
                                    if(CallStateChangeListenerDelegate.this.curBitrate > 0 || CallStateChangeListenerDelegate.this.callState != CallState.VOICE_PAUSE) {
                                        CallStateChangeListenerDelegate.this.isFirstTime = false;
                                        if(CallStateChangeListenerDelegate.this.badNetworkStartTime == 0L) {
                                            CallStateChangeListenerDelegate.this.badNetworkStartTime = var2;
                                        }

                                        if(CallStateChangeListenerDelegate.this.noNetworkStartTime == 0L && CallStateChangeListenerDelegate.this.curBitrate <= 0) {
                                            CallStateChangeListenerDelegate.this.noNetworkStartTime = var2;
                                        }

                                        if(CallStateChangeListenerDelegate.this.curBitrate <= 0) {
                                            if(var2 - CallStateChangeListenerDelegate.this.noNetworkStartTime >= 6000L && VoiceCallManager.this.stateChangeListener != null) {
                                                VoiceCallManager.this.stateChangeListener.onCallStateChanged(CallState.NETWORK_UNSTABLE, CallError.ERROR_NO_DATA);
                                            }
                                        } else if(var2 - CallStateChangeListenerDelegate.this.badNetworkStartTime >= 6000L && VoiceCallManager.this.stateChangeListener != null) {
                                            VoiceCallManager.this.stateChangeListener.onCallStateChanged(CallState.NETWORK_UNSTABLE, CallError.ERROR_NONE);
                                        }
                                    }
                                } else {
                                    CallStateChangeListenerDelegate.this.badNetworkStartTime = 0L;
                                    CallStateChangeListenerDelegate.this.noNetworkStartTime = 0L;
                                    if(VoiceCallManager.this.stateChangeListener != null) {
                                        VoiceCallManager.this.stateChangeListener.onCallStateChanged(CallState.NETWORK_NORMAL, CallError.ERROR_NONE);
                                    }
                                }
                            }

                        }
                    })).start();
                }
            }
        }

        void stopMonitor() {
            synchronized(this) {
                this.needMonitor = false;
                this.badNetworkStartTime = 0L;
                this.noNetworkStartTime = 0L;
                this.isFirstTime = true;
            }
        }
    }

    public static enum CallType {
        audio,
        video;

        private CallType() {
        }
    }
}

