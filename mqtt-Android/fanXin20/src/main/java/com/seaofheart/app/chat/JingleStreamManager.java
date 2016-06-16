package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import com.seaofheart.app.a.a;
import com.seaofheart.app.media.AVNative;
import com.seaofheart.app.media.IGxStatusCallback;
import com.seaofheart.app.media.VideoCallBridge;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.EasyUtils;
import com.xonami.javaBells.MediaType;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidatePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidateType;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class JingleStreamManager {
    private static final String TAG = JingleStreamManager.class.getSimpleName();
    private final ContentPacketExtension.CreatorEnum creator;
    private ContentPacketExtension.SendersEnum senders;
    private List<String> mediaNames;
    private AVNative avNative;
    private VideoCallBridge videoBridge;
    private boolean isVideoCall;
    private StreamParams streamParams;
    private long callStartTime;
    int callCostTime;

    JingleStreamManager(ContentPacketExtension.CreatorEnum var1) {
        this.mediaNames = new ArrayList();
        this.avNative = null;
        this.videoBridge = null;
        this.isVideoCall = false;
        this.videoBridge = VideoCallBridge.getInstance();
        this.creator = var1;
        this.mediaNames.add(VoiceCallManager.CallType.audio.toString());
        this.isVideoCall = false;
    }

    JingleStreamManager(ContentPacketExtension.CreatorEnum var1, VoiceCallManager.CallType var2) {
        this(var1);
        if(VoiceCallManager.CallType.video == var2) {
            this.mediaNames.add(var2.toString());
            this.isVideoCall = true;
        }

    }

    List<String> getMediaNames() {
        return Collections.unmodifiableList(this.mediaNames);
    }

    boolean addDefaultMedia(MediaType var1, String var2) {
        return true;
    }

    List<ContentPacketExtension> createContentList(ContentPacketExtension.SendersEnum var1) {
        this.senders = var1;
        ArrayList var2 = new ArrayList();
        Iterator var4 = this.mediaNames.iterator();

        while(var4.hasNext()) {
            String var3 = (String)var4.next();
            var2.add(this.createContentPacketExtention(var1, var3));
        }

        return var2;
    }

    List<ContentPacketExtension> createcontentList(String var1) {
        try {
            List var2 = this.createContentList(ContentPacketExtension.SendersEnum.both);
            Iterator var4 = var2.iterator();

            while(var4.hasNext()) {
                ContentPacketExtension var3 = (ContentPacketExtension)var4.next();
                String var5 = var3.getName();
                IceUdpTransportPacketExtension var6 = this.getLocalCandidatePacketExtensionForStream(var5, var1);
                var3.addChildExtension(var6);
            }

            return var2;
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }

    IceUdpTransportPacketExtension getLocalCandidatePacketExtensionForStream(String var1, String var2) throws Exception {
        IceUdpTransportPacketExtension var3 = null;

        try {
            var3 = new IceUdpTransportPacketExtension();
            JSONObject var4 = new JSONObject(var2);
            var3.setPassword(var4.getString("pwd"));
            var3.setUfrag(var4.getString("ufrag"));
            JSONArray var5 = var4.getJSONArray("candidates");

            for(int var6 = 0; var6 < var5.length(); ++var6) {
                CandidatePacketExtension var7 = new CandidatePacketExtension();
                JSONObject var8 = var5.getJSONObject(var6);
                int var9 = var8.getInt("component");
                if((!var1.equals(VoiceCallManager.CallType.audio.toString()) || var9 <= 2) && (!var1.equals(VoiceCallManager.CallType.video.toString()) || var9 >= 3)) {
                    if(var9 > 2) {
                        var9 -= 2;
                    }

                    var7.setComponent(var9);
                    var7.setFoundation(var8.getString("foundation"));
                    var7.setGeneration(Integer.parseInt(var8.getString("generation")));
                    var7.setID(String.valueOf(var8.getInt("id")));
                    var7.setIP(var8.getString("ip"));
                    var7.setNetwork(var8.getInt("network"));
                    var7.setPort(var8.getInt("port"));
                    var7.setPriority(var8.getLong("priority"));
                    var7.setProtocol(var8.getString("protocol"));
                    if(var8.has("rel-addr")) {
                        var7.setRelAddr(var8.getString("rel-addr"));
                    }

                    if(var8.has("rel-port")) {
                        var7.setRelPort(var8.getInt("rel-port"));
                    }

                    var7.setType(CandidateType.valueOf(var8.getString("type")));
                    var3.addCandidate(var7);
                }
            }

            return var3;
        } catch (Exception var10) {
            throw var10;
        }
    }

    boolean isVideoCall() {
        return this.isVideoCall;
    }

    private ContentPacketExtension createContentPacketExtention(ContentPacketExtension.SendersEnum var1, String var2) {
        this.senders = var1;
        ContentPacketExtension var3 = new ContentPacketExtension();
        RtpDescriptionPacketExtension var4 = new RtpDescriptionPacketExtension();
        var3.setCreator(this.creator);
        var3.setName(var2);
        if(var1 != null && var1 != ContentPacketExtension.SendersEnum.both) {
            var3.setSenders(var1);
        }

        var3.addChildExtension(var4);
        var4.setMedia(var2);
        return var3;
    }

    List<ContentPacketExtension> parseIncomingAndBuildMedia(JingleIQ var1, ContentPacketExtension.SendersEnum var2) throws IOException {
        return this.parseIncomingAndBuildMedia(var1.getContentList(), var2);
    }

    List<ContentPacketExtension> parseIncomingAndBuildMedia(List<ContentPacketExtension> var1, ContentPacketExtension.SendersEnum var2) throws IOException {
        this.senders = var2;
        String var3 = null;
        Object var4 = null;
        ArrayList var5 = new ArrayList();
        Iterator var7 = var1.iterator();

        String var8;
        do {
            if(!var7.hasNext()) {
                if(var5.size() == 0) {
                    return null;
                }

                return var5;
            }

            ContentPacketExtension var6 = (ContentPacketExtension)var7.next();
            var3 = var6.getName();
            if(var3 == null) {
                throw new IOException();
            }

            var8 = null;
            List var9 = var6.getChildExtensionsOfType(RtpDescriptionPacketExtension.class);
            Iterator var11 = var9.iterator();

            while(var11.hasNext()) {
                RtpDescriptionPacketExtension var10 = (RtpDescriptionPacketExtension)var11.next();
                EMLog.d(TAG, "SIP description : " + var10);
                var8 = var10.getMedia();
                if("audio".equals(var8)) {
                    var5.add(this.createContentPacketExtention(var2, var8));
                } else {
                    if(!"video".equals(var8)) {
                        throw new IOException("Unknown media type: " + var8);
                    }

                    this.isVideoCall = true;
                    var5.add(this.createContentPacketExtention(var2, var8));
                }
            }
        } while(var8 != null);

        throw new IOException();
    }

    void initStreamParams(StreamParams var1) {
        this.streamParams = var1;
    }

    void startStream(final StreamParams var1) {
        if(var1 != null) {
            this.streamParams = var1;
            if(this.avNative == null) {
                this.avNative = new AVNative();
                final IGxStatusCallback var2 = new IGxStatusCallback() {
                    public void updateStatus(int var1) {
                        EMLog.i(JingleStreamManager.TAG, "call back status : " + String.valueOf(var1));
                    }
                };
                EMLog.i(TAG, "local port : " + var1.localPort + ", video local port : " + var1.videoLocalPort + ", local address : " + var1.localAddress + ", server port : " + var1.remotePort + ", video server port : " + var1.videoRemotePort + ", server address : " + var1.remoteAddress + ", video server address : " + var1.videoRemoteAddress + ", channel number : " + var1.channelId + ", video channel number : " + var1.videoChannelId + ", conference id : " + var1.conferenceId + ", rcode : " + var1.rcode);
                long var3 = System.currentTimeMillis();
                int var5 = this.avNative.register(var2, Chat.getInstance().getAppContext(), var1.localPort, var1.localAddress, var1.remotePort, var1.remoteAddress, var1.conferenceId, var1.channelId, var1.rcode, "12345678", var1.isRelayCall, 0);
                EMLog.d(TAG, "call rigister time: " + (System.currentTimeMillis() - var3));
                if(var5 == -1) {
                    EMLog.e(TAG, "call rigister fail");
                }

                this.avNative.setFullDuplexSpeech(var1.conferenceId);
                if(this.isVideoCall) {
                    (new Thread(new Runnable() {
                        public void run() {
                            boolean var1x = false;
                            boolean var2x = false;
                            JingleStreamManager var3 = JingleStreamManager.this;
                            int var9;
                            int var10;
                            synchronized(JingleStreamManager.this) {
                                if(JingleStreamManager.this.avNative == null) {
                                    return;
                                }

                                JingleStreamManager.this.avNative.nativeInit(JingleStreamManager.this.videoBridge);
                                EMLog.d(JingleStreamManager.TAG, "start native video");
                                if(VideoCallHelper.getInstance().getVideoOrientation() == VideoCallHelper.EMVideoOrientation.EMPortrait) {
                                    var9 = a.a().g();
                                    var10 = a.a().f();
                                } else {
                                    var10 = a.a().g();
                                    var9 = a.a().f();
                                }
                            }

                            boolean var11 = true;
                            if(!EasyUtils.isAppRunningForeground(Chat.getInstance().getAppContext())) {
                                var11 = false;
                            }

                            JingleStreamManager.this.avNative.nativeStartVideo(var2, var1.videoLocalPort, var1.videoRemotePort, var1.videoRemoteAddress, var1.conferenceId, var1.videoChannelId, var1.rcode, "12345678", var9, var10, a.a().h(), var1.isRelayCall, var11);
                            JingleStreamManager var4 = JingleStreamManager.this;
                            synchronized(JingleStreamManager.this) {
                                if(JingleStreamManager.this.avNative != null) {
                                    EMLog.d(JingleStreamManager.TAG, "quit video call");

                                    try {
                                        JingleStreamManager.this.avNative.nativeQuit();
                                    } catch (Exception var6) {
                                        var6.printStackTrace();
                                    }
                                }

                                a.a().d();
                            }
                        }
                    })).start();
                }

                this.callStartTime = System.currentTimeMillis();
            }
        }
    }

    synchronized void stopStream() {
        if(this.streamParams != null && this.streamParams.conferenceId != null && this.creator == ContentPacketExtension.CreatorEnum.initiator) {
            (new Thread(new Runnable() {
                public void run() {
                    VoiceCallManager.getInstance().removeP2PConference(JingleStreamManager.this.streamParams.conferenceId);
                }
            })).start();
        }

        if(this.avNative != null) {
            EMLog.d(TAG, "try to stop the stream");
            this.callCostTime = (int)((System.currentTimeMillis() - this.callStartTime) / 1000L);
            this.avNative.stop(this.streamParams.conferenceId);
            this.avNative.unregister(this.streamParams.conferenceId);
            if(this.mediaNames.contains(VoiceCallManager.CallType.video.toString())) {
                EMLog.e(TAG, "to stop video");
                this.avNative.nativeStopVideo();
            }

            this.avNative = null;
            EMLog.d(TAG, "the stream was stopped");
        }

        this.isVideoCall = false;
    }

    void pauseVoiceStream() {
        if(this.avNative != null && this.streamParams.conferenceId != null) {
            this.avNative.stop(this.streamParams.conferenceId);
        }

    }

    void resumeVoiceStream() {
        if(this.avNative != null && this.streamParams.conferenceId != null) {
            this.avNative.setFullDuplexSpeech(this.streamParams.conferenceId);
        }

    }

    void pauseVideoStream() {
        if(this.avNative != null && this.streamParams.conferenceId != null) {
            this.avNative.nativeSetVideoEncodeFlag(false);
        }

    }

    void resumeVideoStream() {
        if(this.avNative != null && this.streamParams.conferenceId != null) {
            this.avNative.nativeSetVideoEncodeFlag(true);
        }

    }

    int getVoiceInputLevel() {
        return this.avNative != null && this.streamParams.conferenceId != null?this.avNative.GetAudioInputLevel(this.streamParams.conferenceId):0;
    }

    int getVoiceRemoteBitrate() {
        return this.avNative != null && this.streamParams.conferenceId != null?this.avNative.nativeVoeClient_GetRemoteBitrate(this.streamParams.conferenceId):0;
    }

    boolean streamStarted() {
        return this.avNative != null;
    }
}

