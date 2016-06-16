package com.seaofheart.app.chat.core;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.seaofheart.app.analytics.LoginCollector;
import com.seaofheart.app.analytics.TimeTag;
import com.seaofheart.app.chat.Chat;
import com.seaofheart.app.chat.ChatConfig;
import com.seaofheart.app.chat.ChatManager;
import com.seaofheart.app.chat.ContactManager;
import com.seaofheart.app.chat.ReceiveMessageRunnable;
import com.seaofheart.app.exceptions.AuthenticationException;
import com.seaofheart.app.exceptions.NetworkUnconnectedException;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.util.NetUtils;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.net;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.jivesoftware.smack.ConnectionConfiguration;
//import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager {
    private static final String TAG = ConnectionManager.class.getSimpleName();
    private static final String PERF = "perf";
    private static final String RESOURCE = "mobile";
    //private static String xmppResource = null;
    private String bareJid; // 完整的Jid包括 Username@Domain/resource
    private String password;
    private Context context;
    private final ConnectionManager.PingListener pingListener = new ConnectionManager.PingListener();

    //private XMPPConnection connection;
    private MQTT mqtt;
    private FutureConnection futureConnection; // 非阻塞链接
    private BlockingConnection blockingConnection; // 阻塞链接
    private CallbackConnection callbackConnection; // 回调式连接
    ExecutorService receiveThreadPool = Executors.newCachedThreadPool();

    private ConnectionConfiguration connectionConfig;
//    private final ConnectionManager.MTConnectionListener connectionListener = new ConnectionManager.MTConnectionListener();
    private ChatConnectionListener chatConnectionListener = null;
    private int randomBase = -1;
    private int attempts = 0;
    private Thread reconnectionThread = null;
    j.Address currentHost = null;
    boolean enableWakeLock = true;
    PowerManager.WakeLock wakeLock = null; // 电源唤醒锁，WakeLock的设置是 Activiy 级别的，不是针对整个Application应用的
    private static final String LOCK = "easemoblock";
    boolean isDone = false; // 已经释放了连接
    boolean isConnectivityRegistered = false;
    private TimeTag chatTag;
    private TimeTag imTag;
    boolean connectDisabled = false;
    static final int MAX_RETRIES_TIMES_IP = 3;
    int ipRetryCnt = 3;
    private PowerManager powerManager;

    //接收来自系统和应用中的广播,监听判断网络连接状态
    private BroadcastReceiver connectivityBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent var2) {
            String var3 = var2.getAction();
            if(!var3.equals("android.net.conn.CONNECTIVITY_CHANGE")) { // 网罗连接改变
                EMLog.d(ConnectionManager.TAG, "skip no connectivity action");
            } else {
                EMLog.d(ConnectionManager.TAG, "connectivity receiver onReceiver");
                boolean isIntenert = !NetUtils.hasDataConnection(context);
                if(isIntenert) { // 能连上互联网
                    if((ConnectionManager.this.reconnectionThread == null || !ConnectionManager.this.reconnectionThread.isAlive()) && ConnectionManager.this.isConnected()) {
                        (new Thread() { // 启新线程重连服务器
                            public void run() {
                                ConnectionManager.this.reConnect();
                            }
                        }).start();
                    }
                } else { // 不能联网
                    if(context == null) {
                        return;
                    }

                    if(!NetUtils.hasDataConnection(context)) { // 再次检查，还是没有网络连接
                        EMLog.d(ConnectionManager.TAG, "in connectivity broadcast, skip since no data connection");
                        return;
                    }

                    if(!ConnectionManager.this.isConnected()) {
                        ConnectionManager.this.resetAttempts();
                        if(ConnectionManager.this.reconnectionThread != null) {
                            ConnectionManager.this.reconnectionThread.interrupt();
                            (new Thread() { //起新线程连接服务器
                                public void run() {
                                    ConnectionManager.this.startReconnectionThread();
                                }
                            }).start();
                        }
                    }
                }

            }
        }
    };

    public ConnectionManager() {
    }

    public void onInit() {
        this.context = Chat.getInstance().getAppContext();
        this.powerManager = (PowerManager)this.context.getSystemService(Context.POWER_SERVICE);
        this.initConnectionConfig();

        this.isDone = false;
    }

    public void onInit(String var1, String password) {
        this.onInit();
        this.bareJid = var1;
        this.password = password;
    }

    public void setChatTag(TimeTag var1) {
        this.chatTag = var1;
    }

    public String getCurrentUser() {
        return ContactManager.getUserNameFromEid(this.bareJid);
    }

    public String getCurrentPwd() {
        return this.password;
    }

    private boolean hasConnected = false;
    private void initConnectionConfig() {
        if(!hasConnected){
            this.mqtt = new MQTT();
            try {
                mqtt.setHost("120.26.227.155", 1883);
//            mqtt.setClientId(UTF8Buffer.utf8("mosqsub"));
//            MYApplication myApplication = MYApplication.getInstance();
//            String username = myApplication.getUserName();
//            mqtt.setUserName("11240732");
                this.futureConnection = mqtt.futureConnection();
                // 链接服务器
                Future<Void> f1 = futureConnection.connect();
                f1.await();
//            MYApplication myApplication = MYApplication.getInstance();
//            String username = myApplication.getUserName();
//            this.futureConnection.publish(MYApplication.getInstance().getUserName(), "Connection".getBytes(), QoS.AT_LEAST_ONCE, false).await(); // mytopic

                ReceiveMessageRunnable threadClass = new ReceiveMessageRunnable(this.futureConnection);
                this.receiveThreadPool.execute(threadClass);

                hasConnected = true;

            } catch (Exception e){
                e.printStackTrace();
            }
        }


        //this.configure(ProviderManager.getInstance());
//        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
//        SASLAuthentication.supportSASLMechanism("PLAIN");
//        XMPPConnection.DEBUG_ENABLED = p.getInstance().m();
//        SmackConfiguration.setPacketReplyTimeout('鱀');
//        this.currentHost = j.getInstance().b();
//        this.connectionConfig = new ConnectionConfiguration(this.currentHost.host, this.currentHost.port, ChatConfig.getInstance().getDomain());
//        this.connectionConfig.setRosterLoadedAtLogin(false);
//        this.connectionConfig.setSendPresence(false);
//        this.connectionConfig.setReconnectionAllowed(false);
//        this.connectionConfig.setCompressionEnabled(true);
//        if(Build.VERSION.SDK_INT >= 14) {
//            this.connectionConfig.setTruststoreType("AndroidCAStore");
//            this.connectionConfig.setTruststorePassword((String)null);
//            this.connectionConfig.setTruststorePath((String)null);
//        } else {
//            this.connectionConfig.setTruststoreType("BKS");
//            String var1 = System.getProperty("javax.net.ssl.trustStore");
//            if(var1 == null) {
//                var1 = System.getProperty("java.home") + File.separator + "etc" + File.separator + "security" + File.separator + "cacerts.bks";
//            }
//
//            this.connectionConfig.setTruststorePath(var1);
//        }

    }

    public void setChatConnectionListener(ChatConnectionListener var1) {
        this.chatConnectionListener = var1;
    }

    public void connect() throws NetworkUnconnectedException {
        EMLog.d(TAG, "connection manager:connect");
        if(this.futureConnection == null) {
            EMLog.e(TAG, "fail to setup connection");
            throw new NetworkUnconnectedException("fail to setup connection");
        } else if(this.futureConnection.isConnected()) {
            EMLog.d(TAG, "connection is connected, skip reconnect");
        } else {
            String var2;
            j.Address var3;
            try {
                EMLog.d(TAG, "before connect");
                this.futureConnection.connect();
                EMLog.d(TAG, "after connect");
            }/* catch (UnknownHostException var4) {
                EMLog.e(TAG, "unknow host exception:" + var4.toString());
                if(!NetUtils.hasNetwork(this.context)) {
                    throw new NetworkUnconnectedException("no network available");
                } else {
                    throw new NetworkUnconnectedException(var4.getMessage());
                }
            } catch (NoRouteToHostException var5) {
                EMLog.e(TAG, "NoRouteToHostException:" + var5.toString());
                throw new NetworkUnconnectedException(var5.getMessage());
            } catch (ConnectException var6) {
                var2 = var6.toString();
                EMLog.e(TAG, "ConnectException:" + var2);
                if(p.getInstance().h() && var2 != null && j.getInstance().h() && var2.toLowerCase().contains("refused")) {
                    var3 = j.getInstance().f();
                    if(var3 != null) {
                        this.currentHost = var3;
                    }

                    //this.futureConnection.getConfiguration().initHostAddresses(this.currentHost.host, this.currentHost.port);
                }

                throw new NetworkUnconnectedException(var2);
            } catch (SocketException var7) {
                EMLog.e(TAG, "SocketException:" + var7.toString());
                throw new NetworkUnconnectedException(var7.getMessage());
            } catch (SocketTimeoutException var8) {
                if(p.getInstance().h() && j.getInstance().h() && !j.a(this.currentHost)) {
                    j.Address var10 = j.getInstance().f();
                    if(var10 != null) {
                        this.currentHost = var10;
                    }

                    //this.futureConnection.getConfiguration().initHostAddresses(this.currentHost.host, this.currentHost.port);
                }

                EMLog.e(TAG, "SocketTimeoutException:" + var8.toString());
                NetworkUnconnectedException var11 = new NetworkUnconnectedException("SocketTimeoutException " + var8.getMessage());
                var11.setErrorCode(-1026);
                throw var11;
            }*/ catch (Exception var9) {
                var9.printStackTrace();
                var2 = null;
                if(!"".equals(var9.getMessage())) {
                    var2 = var9.getMessage();
                } else {
                    var2 = var9.toString();
                }

                if(p.getInstance().h() && var2 != null && j.getInstance().h() && var2.toLowerCase().contains("refused") && NetUtils.hasNetwork(this.context)) {
                    var3 = j.getInstance().f();
                    if(var3 != null) {
                        this.currentHost = var3;
                    }

                    //this.futureConnection.getConfiguration().initHostAddresses(this.currentHost.host, this.currentHost.port);
                }

                EMLog.e(TAG, "connection.connect() failed: " + var2);
                throw new NetworkUnconnectedException(var2);
            }
        }
    }

    private void initConnection() throws NetworkUnconnectedException {
        EMLog.d(TAG, "enter initConnection()");
        if(!this.futureConnection.isConnected()) {
            EMLog.e(TAG, "Connection is not connected as expected");
            throw new NetworkUnconnectedException("Connection is not connected as expected");
        } else {
            //this.futureConnection.addConnectionListener(this.connectionListener);
            //this.initFeatures();
            //PacketTypeFilter var1 = new PacketTypeFilter(aIQ.class);
            //this.futureConnection.addPacketListener(this.pingListener, var1);
        }
    }

    private synchronized void login() throws EaseMobException {
        long var1 = System.currentTimeMillis();

        try {
//            if(this.futureConnection.isAuthenticated()) {
//                EMLog.d(TAG, "already login. skip");
//                return;
//            }

            if(!this.futureConnection.isConnected()) {
                EMLog.e(TAG, "Connection is not connected as expected");
                throw new NetworkUnconnectedException("Connection is not connected as expected");
            }

            EMLog.d(TAG, "try to login with barejid" + this.bareJid);
            //this.futureConnection.login(this.bareJid, this.password, getXmppResource(this.context));
            EMLog.d(TAG, "login successfully");
        } catch (IllegalStateException var9) {
            EMLog.d(TAG, "illegalState in connection.login:" + var9.toString());
            if(var9.toString().indexOf(" Already logged in to server") < 0) {
                throw new EaseMobException(var9.toString());
            }
        } catch (Exception var10) {
            EMLog.e(TAG, "Failed to login to xmpp server. Caused by: " + var10.getMessage());
            String var4 = "401";
            String var5 = "not-authorized";
            String var6 = "SASL authentication failed using mechanism PLAIN";
            String var7 = var10.getMessage();
            if(var7 != null && var7.contains(var4)) {
                throw new AuthenticationException(var4);
            }

            if(var7 != null && var7.contains(var5)) {
                throw new AuthenticationException(var5);
            }

            if(var7 != null && var7.contains(var6)) {
                throw new AuthenticationException(var6);
            }

            throw new EaseMobException(var7);
        }

        try {
            this.sendVersionIQ();
            Presence var3 = new Presence(Presence.Type.available);
            if(this.imTag != null) {
                var3.setImLoginTime(this.imTag.stop());
                this.imTag = null;
            }

            if(this.chatTag != null) {
                var3.setChatLoginTime(this.chatTag.stop());
                this.chatTag = null;
            }

            //this.futureConnection.sendPacket(var3);
            EMLog.d("perf", "[perf] login time(ms)" + (System.currentTimeMillis() - var1));
            if(ChatConfig.isDebugTrafficMode()) {
                net.d();
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    private void sendVersionIQ() {
        EMLog.d(TAG, "send version iq");
        ab var1 = new ab(Chat.getInstance().getVersion());
        var1.setTo(ChatConfig.getInstance().getDomain());
        String var2 = ChatConfig.getInstance().APPKEY + "_" + ChatManager.getInstance().getCurrentUser() + "@" + ChatConfig.getInstance().getDomain();
        var1.setFrom(var2);
        //this.futureConnection.sendPacket(var1);
    }

    public boolean reuse() {
        if(this.futureConnection == null) {
            return false;
        } else {
            this.isDone = false;
            //this.futureConnection.addConnectionListener(this.connectionListener);
            return true;
        }
    }

    public synchronized void connectSync(boolean var1) throws EaseMobException {
        if(!this.connectDisabled) {
            if(!this.isDone) {
                EMLog.d(TAG, "enter connectSync");
                //if(!this.connection.isConnected() || !this.connection.isAuthenticated()) {
                if(null == this.blockingConnection || !this.blockingConnection.isConnected()) {
                    try {
                        if(this.wakeLock == null && this.enableWakeLock) {
                            this.wakeLock = this.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "easemoblock"); // PARTIAL_WAKE_LOCK 保持CPU 运转，屏幕和键盘灯有可能是关闭的。
                            this.wakeLock.acquire(); // 获取WakeLock实例后获取相应的锁
                            EMLog.d(TAG, "acquire lock");
                        }

                        TimeTag var2 = new TimeTag();
                        var2.start();
                        this.imTag = var2;
                        if(!var1) {
                            this.connect();
                        } else {
                            for(int var7 = 0; var7 < 3; ++var7) {
                                try {
                                    this.connect();
                                } catch (EaseMobException var5) {
                                    if(var5.getErrorCode() != -1026) {
                                        throw var5;
                                    }
                                }
                            }
                        }

                        this.initConnection();
                        this.login();
                        this.releaseWakelock();
                        LoginCollector.collectIMLoginTime(var2.stop());
                        if(this.chatConnectionListener != null) {
                            this.chatConnectionListener.onConnectionSuccessful();
                        }

                        this.resetAttempts();
                        this.ipRetryCnt = 3;
                    } catch (EaseMobException var6) {
                        String var3 = var6.getMessage();
                        this.releaseWakelock();
                        EMLog.e(TAG, "connectSync with error = " + var3);
                        if(!var1 && !(var6 instanceof AuthenticationException)) {
                            this.reConnect();
                        } else {
                            j.getInstance().l();
                            this.disconnect();
                        }

                        LoginCollector.collectConnectionError(var6.getMessage());
                        throw var6;
                    }
                }
            }
        }
    }

    /**
     * 释放唤醒锁
     */
    void releaseWakelock() {
        if(this.wakeLock != null && this.wakeLock.isHeld()) { // 锁非空并且已经加锁了，释放
            this.wakeLock.release();
        }

    }

    public PowerManager.WakeLock getWakeLock() {
        return this.wakeLock;
    }

    void onDnsConfigChanged() {
        this.futureConnection.disconnect();
        j.Address var1 = j.getInstance().f();
        if(var1 != null) {
            this.currentHost = var1;
        }

        //this.futureConnection.getConfiguration().initHostAddresses(this.currentHost.host, this.currentHost.port);
        this.reConnect();
    }

    private synchronized void reConnect() {
        if(!this.connectDisabled) {
            EMLog.d(TAG, "enter reConnect");
            this.futureConnection.disconnect();
            if(!this.isDone) {
                this.registerConnectivityReceiver();
                this.startReconnectionThread();
            }

        }
    }

    public void forceReconnect() {
        this.reConnect();
    }

    public void reconnectSync() throws EaseMobException {
        if(!this.isDone) {
            EMLog.d(TAG, "try to reconnectSync");
            this.connectSync(false);
        }
    }

    public void reconnectASync() {
        if(!this.isDone) {
            EMLog.d(TAG, "try to reconnectASync");
            Thread var1 = new Thread() {
                public void run() {
                    try {
                        ConnectionManager.this.reconnectSync();
                    } catch (Exception var2) {
                        var2.printStackTrace();
                    }

                }
            };
            var1.start();
        }
    }

    public void tempDisconnect() {
    }

    public boolean disconnect() {
        if(this.isDone) { // 已经释放
            return true;
        } else {
            try {
                this.releaseWakelock();
                EMLog.d(TAG, this.hashCode() + " : enter disconnect()");
                this.isDone = true; // 设置连接断开标志为true
                this.ipRetryCnt = 3; // 设置重试次数为3次
                if(this.reconnectionThread != null) {
                    this.reconnectionThread.interrupt(); // 中断连接线程
                }

                this.unregisterConnectivityReceiver();
                if(this.futureConnection != null) {
//                    if(this.connectionListener != null) {
//                        this.futureConnection.removeConnectionListener(this.connectionListener); // 移除连接监听
//                    }

                    EMLog.d(TAG, "trying to disconnect connection （" + this.futureConnection.hashCode() + ")");
                    Future<Void> f4 = this.futureConnection.disconnect();
                    f4.await();
                }

                return true;
            } catch (Exception var2) {
                var2.printStackTrace();
                return false;
            }
        }
    }

    /*public XMPPConnection getConnection() {
        return this.connection;
    }*/
    public FutureConnection getConnection() {
        return this.futureConnection;
    }

    /*public boolean isAuthentificated() {
        return this.futureConnection == null?false:this.futureConnection.isAuthenticated();
    }*/

    public boolean isConnected() {
        return this.futureConnection == null?false:this.futureConnection.isConnected();
    }

    /*
    private void initFeatures() {
        ServiceDiscoveryManager var1 = ServiceDiscoveryManager.getInstanceFor(this.futureConnection);
        if(var1 == null) {
            var1 = new ServiceDiscoveryManager(this.futureConnection);
        }

        var1.setIdentityName("EaseMob");
        var1.setIdentityType("phone");
        var1.addFeature("http://jabber.org/protocol/disco#info");
        var1.addFeature("jabber:iq:privacy");
        var1.addFeature("urn:xmpp:avatar:metadata");
        var1.addFeature("urn:xmpp:avatar:metadata+notify");
        var1.addFeature("urn:xmpp:avatar:data");
        var1.addFeature("http://jabber.org/protocol/nick");
        var1.addFeature("http://jabber.org/protocol/nick+notify");
        var1.addFeature("http://jabber.org/protocol/muc");
        var1.addFeature("http://jabber.org/protocol/muc#rooms");
        var1.addFeature("urn:xmpp:ping");
        var1.addFeature("easemob:x:roomtype");
        var1.addFeature("http://jabber.org/protocol/disco#info");
        var1.addFeature("urn:xmpp:jingle:1");
        var1.addFeature("urn:xmpp:jingle:transports:ice-udp:1");
        var1.addFeature("urn:xmpp:jingle:apps:rtp:1");
        var1.addFeature("urn:xmpp:jingle:apps:rtp:audio");
        var1.addFeature("urn:xmpp:jingle:apps:rtp:video");
    }

    private void discoverServerFeatures() {
        try {
            ServiceDiscoveryManager var1 = ServiceDiscoveryManager.getInstanceFor(this.futureConnection);
            var1.discoverInfo(this.futureConnection.getServiceName());
        } catch (XMPPException var3) {
            EMLog.w(TAG, "Unable to discover server features", var3);
        }

    }

    private void configure(ProviderManager var1) {
        EMLog.d(TAG, "configure");

        try {
            if(Class.forName("com.xonami.javaBells.JingleManager") != null) {
                JingleManager.enableJingle();
            }
        } catch (Throwable var3) {
            ;
        }

        var1.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        var1.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        var1.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        var1.addExtensionProvider("delay", "urn:xmpp:delay", new DelayInfoProvider());
        var1.addExtensionProvider("ts", "urn:xmpp:timestamp", new aa());
        var1.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        var1.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        ChatStateExtension.Provider var2 = new ChatStateExtension.Provider();
        var1.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", var2);
        var1.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates", var2);
        var1.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", var2);
        var1.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", var2);
        var1.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", var2);
        var1.addIQProvider("ping", "urn:xmpp:ping", aIQ.class);
        var1.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
        var1.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
        var1.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
        var1.addExtensionProvider("x", "jabber:x:conference", new org.jivesoftware.smackx.GroupChatInvitation.Provider());
        var1.addExtensionProvider("roomtype", "easemob:x:roomtype", new y());
        var1.addIQProvider("offline", "http://jabber.org/protocol/offline", new org.jivesoftware.smackx.packet.OfflineMessageRequest.Provider());
        var1.addExtensionProvider("offline", "http://jabber.org/protocol/offline", new org.jivesoftware.smackx.packet.OfflineMessageInfo.Provider());
        var1.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
        var1.addExtensionProvider("received", "urn:xmpp:receipts", new bPacketExtensionProvider());
        var1.addIQProvider("query", "urn:xmpp:media-conference", new uIQProvider());
    } */

    private void resetAttempts() {
        this.attempts = 0;
        this.randomBase = -1;
    }

    private int timeDelay() {
        if(this.randomBase == -1) {
            this.randomBase = (new Random()).nextInt(5) + 5;
        }

        ++this.attempts;
        return this.attempts > 3 && this.attempts <= 9?this.randomBase + (new Random()).nextInt(5):(this.attempts > 9?(this.randomBase * 3 > 30?25 + (new Random()).nextInt(5):this.randomBase * 3):this.randomBase);
    }

    /**
     * 启动重连线程
     */
    private synchronized void startReconnectionThread() {
        if(!this.isDone) {
            EMLog.d(TAG, this.hashCode() + " : enter startReconnectionThread()");
            if(this.reconnectionThread == null || !this.reconnectionThread.isAlive()) {
                EMLog.d(TAG, "start reconnectionThread()");
                this.resetAttempts();
                this.reconnectionThread = new Thread() {
                    public void run() {
                        EMLog.d(ConnectionManager.TAG, "run in reconnectionThread");

                        try {
                            sleep((long)(new Random()).nextInt(2000));
                        } catch (InterruptedException var5) {
                            var5.printStackTrace();
                            if(ConnectionManager.this.isDone) {
                                return;
                            }
                        }

                        while(!ConnectionManager.this.isConnected() && !ConnectionManager.this.isDone) {
                            try {
                                EMLog.d(ConnectionManager.TAG, "run in reconnectionThread with connection " + ConnectionManager.this.futureConnection.hashCode());
                                if(NetUtils.hasDataConnection(ConnectionManager.this.context)) {
                                    ConnectionManager.this.reconnectSync();
                                } else {
                                    EMLog.d(ConnectionManager.TAG, "skip the reconnection since there is no data connection!");
                                }
                            } catch (EaseMobException var3) {
                                var3.printStackTrace();
                            }

                            int var1 = ConnectionManager.this.timeDelay();

                            while(!ConnectionManager.this.isConnected() && !ConnectionManager.this.isDone && var1 > 0) {
                                try {
                                    sleep(1000L);
                                    --var1;
//                                    ConnectionManager.this.connectionListener.reconnectingIn(var1);
                                } catch (InterruptedException var4) {
                                    var4.printStackTrace();
                                    if(!ConnectionManager.this.isDone) {
                                        break;
                                    }

                                    return;
                                }
                            }
                        }

                    }
                };
                this.reconnectionThread.setName("EASEMOB Reconnection Thread");
                this.reconnectionThread.setDaemon(true);
                this.reconnectionThread.start();
            }
        }
    }

    private void onDisconnected() {
        EMLog.d(TAG, "on disconnected");
        if(this.wakeLock != null && this.wakeLock.isHeld()) {
            this.wakeLock.release();
            EMLog.d(TAG, "lock release");
        }

    }

    /*public static String getXmppResource(Context var0) {
        if(xmppResource == null) {
            xmppResource = "mobile";
        }

        return xmppResource;
    }*/

    public boolean isFinished() {
        return this.isDone;
    }

    /*void setBlackListActive() throws XMPPException {
        PrivacyListManager var1 = PrivacyListManager.getInstanceFor(this.futureConnection);
        var1.setActiveListName("special");
    }*/

    void enableConnect(boolean var1) {
        this.connectDisabled = var1;
    }

    private void registerConnectivityReceiver() {
        if(this.context == null) {
            EMLog.e(TAG, "context is null!......");
        } else if(!this.isConnectivityRegistered) {
            try {
                IntentFilter var1 = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
                EMLog.d(TAG, "register connectivity receiver.");
                this.context.registerReceiver(this.connectivityBroadcastReceiver, var1);
                this.isConnectivityRegistered = true;
            } catch (Exception var2) {
                var2.printStackTrace();
            }

        }
    }

    private void unregisterConnectivityReceiver() {
        if(this.context == null) {
            EMLog.e(TAG, "context is null!......");
        } else {
            EMLog.d(TAG, "unregisterConnectivityReceiver()");

            try {
                this.isConnectivityRegistered = false;
                this.context.unregisterReceiver(this.connectivityBroadcastReceiver);
            } catch (Exception var2) {
                ;
            }

        }
    }

    /**
     * Ping包监听，发送心跳包
     */
    private class PingListener implements PacketListener {
        private PingListener() {
        }

        public void processPacket(Packet packet) {
            EMLog.d(ConnectionManager.TAG, "received ping packet from :" + packet.getFrom());
            if(packet instanceof aIQ) {
                aIQ var2 = (aIQ)packet;
                if(var2.getType() == org.jivesoftware.smack.packet.IQ.Type.GET) {
                    aIQ var3 = new aIQ();
                    var3.setType(org.jivesoftware.smack.packet.IQ.Type.RESULT);
                    var3.setTo(var2.getFrom());
                    var3.setPacketID(var2.getPacketID());
                    //ConnectionManager.this.futureConnection.sendPacket(var3);
                }

            }
        }
    }

    /*private class MTConnectionListener implements ConnectionListener {
        private MTConnectionListener() {
        }

        public void connectionClosed() {
            EMLog.e(ConnectionManager.TAG, "connectionClosed");
            ConnectionManager.this.onDisconnected();
        }

        public void connectionClosedOnError(Exception var1) {
            EMLog.e(ConnectionManager.TAG, "connectionClosedOnError in " + var1);
            if(var1 != null && var1.getMessage() != null && var1.getMessage().contains("conflict")) {
                EMLog.e(ConnectionManager.TAG, "connection closed caused by conflict. set autoreconnect to false");
            } else {
                ConnectionManager.this.registerConnectivityReceiver();
                ConnectionManager.this.startReconnectionThread();
            }

            ConnectionManager.this.onDisconnected();
            if(ConnectionManager.this.chatConnectionListener != null) {
                ConnectionManager.this.chatConnectionListener.connectionClosedOnError(var1);
            }

        }

        public void reconnectingIn(int var1) {
            if(ConnectionManager.this.chatConnectionListener != null) {
                ConnectionManager.this.chatConnectionListener.reconnectingIn(var1);
            }

        }

        public void reconnectionFailed(Exception var1) {
            EMLog.e(ConnectionManager.TAG, "xmpp con mgr reconnectionFailed:" + var1);
            ConnectionManager.this.onDisconnected();
            if(ConnectionManager.this.chatConnectionListener != null) {
                ConnectionManager.this.chatConnectionListener.reconnectionFailed(var1);
            }

        }

        public void reconnectionSuccessful() {
            EMLog.d(ConnectionManager.TAG, "reconnectionSuccessful");
            ConnectionManager.this.sendVersionIQ();
            EMLog.d(ConnectionManager.TAG, "send available presence after reconnected");
            Presence var1 = new Presence(Presence.Type.available);
            //ConnectionManager.this.futureConnection.sendPacket(var1); // 发一个ping包
            if(ConnectionManager.this.chatConnectionListener != null) {
                ConnectionManager.this.chatConnectionListener.reconnectionSuccessful();
            }

        }
    }*/
}

