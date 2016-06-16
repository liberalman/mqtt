package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/15.
 */
class StreamParams {
    public String remoteAddress = null;
    public String localAddress = null;
    public String videoRemoteAddress = null;
    public int remotePort = -1;
    public int videoRemotePort = -1;
    public int localPort = -1;
    public int videoLocalPort = -1;
    public int channelId = -1;
    public int videoChannelId = -1;
    public String conferenceId = null;
    public String rcode = null;
    public boolean isRelayCall = false;

    StreamParams() {
    }
}