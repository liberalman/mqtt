package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import com.seaofheart.app.EMConnectionListener;
import com.seaofheart.app.analytics.Collector;
import com.seaofheart.app.util.net;
import com.seaofheart.app.chat.core.ac;
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.util.NetUtils;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ping.packet.Ping;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class SmartHeartBeat {
    private static final String TAG = "smart ping";
    private static final int MOBILE_INTERVAL = 180000;
    private static final int WIFI_INTERVAL = 120000;
    private static final int PING_PONG_TIMEOUT = 15000;
    private static final int MIN_INTERVAL = 30000;
    private static final int MAX_INTERVAL = 270000;
    private static final int MAX_MIN_INTERVAL_COUNTER = 3;
    private int heartbeatStep = 30000;
    private int failTries = 4;
    private int successedInterval;
    private int currentInterval;
    private boolean dataReceivedDuringInterval = false;
    private long lastSuccessPingpongTime;
    private static final int PING_PONG_CHECK_INTERVAL = 900000;
    private int minIntervalCounter = 3;
    private String heartbeatId;
    private HeartBeatReceiver alarmIntentReceiver = null;
    private PendingIntent alarmIntent = null;
    private Context appContext;
    private XMPPConnection connection;
    private EMConnectionListener cnnListener = null;
    private PowerManager.WakeLock wakeLock;
    private Object stateLock = new Object();
    private SmartHeartBeat.EMSmartPingState pingState;
    ac whitePingPacket;
    Ping pingPacket;
    ExecutorService threadPool;
    long lastPacketReceivedTime;
    private PacketListener pktListener;
    private Runnable heartBeatRunnable;

    private SmartHeartBeat() {
        this.pingState = SmartHeartBeat.EMSmartPingState.EMReady;
        this.whitePingPacket = new ac();
        this.pingPacket = new Ping();
        this.threadPool = null;
        this.lastPacketReceivedTime = 0L;
        this.pktListener = new PacketListener() {
            public void processPacket(Packet var1) {
                SmartHeartBeat.this.dataReceivedDuringInterval = true;
                SmartHeartBeat.this.lastPacketReceivedTime = System.currentTimeMillis();
            }
        };
        this.heartBeatRunnable = new Runnable() {
            public void run() {
                EMLog.d("smart ping", "has network connection:" + NetUtils.hasNetwork(SmartHeartBeat.this.appContext) + " has data conn:" + NetUtils.hasDataConnection(SmartHeartBeat.this.appContext) + " isConnected to easemob server : " + ChatManager.getInstance().isConnected());
                if(SmartHeartBeat.this.hasDataConnection()) {
                    if(SmartHeartBeat.this.pingState == SmartHeartBeat.EMSmartPingState.EMHitted) {
                        if(System.currentTimeMillis() - SmartHeartBeat.this.lastSuccessPingpongTime >= (long)(900000 + (new Random()).nextInt(5000))) {
                            EMLog.d("smart ping", "Final candiate hitted, but the interval is bigger than PING_PONG_CHECK_INTERVAL");
                            SmartHeartBeat.this.checkPingPong();
                        } else {
                            SmartHeartBeat.this.sendPing();
                        }
                    } else {
                        SmartHeartBeat.this.checkPingPong();
                    }
                } else {
                    EMLog.d("smart ping", "....no connection to server");
                }

                net.d();
                SmartHeartBeat.this.scheduleNextAlarm();
            }
        };
    }

    public static SmartHeartBeat create() {
        return new SmartHeartBeat();
    }

    public void onInit(XMPPConnection var1) {
        this.changeState(SmartHeartBeat.EMSmartPingState.EMEvaluating);
        this.threadPool = Executors.newSingleThreadExecutor();
        this.reset();
        this.appContext = Chat.getInstance().getAppContext();
        this.connection = var1;
        var1.addPacketListener(this.pktListener, new PacketFilter() {
            public boolean accept(Packet var1) {
                return SmartHeartBeat.this.heartbeatId != null && var1.getPacketID() != null?!var1.getPacketID().equals(SmartHeartBeat.this.heartbeatId):true;
            }
        });
        if(this.cnnListener == null) {
            this.cnnListener = new EMConnectionListener() {
                public void onDisconnected(int var1) {
                    EMLog.d("smart ping", " onDisconnected ...");
                    SmartHeartBeat.this.reset();
                }

                public void onConnected() {
                    EMLog.d("smart ping", " onConnectred ...");
                    SmartHeartBeat.this.scheduleNextAlarm();
                }
            };
        }

        ChatManager.getInstance().removeConnectionListener(this.cnnListener);
        ChatManager.getInstance().addConnectionListener(this.cnnListener);
        PowerManager var2 = (PowerManager)this.appContext.getSystemService(Context.POWER_SERVICE);
        if(this.wakeLock == null) {
            this.wakeLock = var2.newWakeLock(1, "heartbeatlock");
        }

    }

    private void sendPing() {
        EMLog.d("smart ping", "send white heartbeat");

        try {
            this.heartbeatId = this.whitePingPacket.getPacketID();
            this.connection.sendPacket(this.whitePingPacket);
        } catch (Exception var2) {
            EMLog.e("smart ping", var2.toString());
        }

    }

    private void checkPingPong() {
        EMLog.d("smart ping", "check pingpong ...");
        boolean var1 = false;
        int var2 = 0;

        while(true) {
            label101: {
                if(var2 < this.failTries) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException var4) {
                        EMLog.e("smart ping", "heartbeat thread be interrupt");
                        return;
                    }

                    try {
                        if(this.dataReceivedDuringInterval) {
                            return;
                        }

                        var1 = this.sendPingPong();
                        if(this.dataReceivedDuringInterval) {
                            return;
                        }

                        if(!var1) {
                            break label101;
                        }

                        EMLog.d("smart ping", "success to send ping pong ... with current heartbeat interval : " + Collector.timeToString((long)this.currentInterval));
                        this.successedInterval = this.currentInterval;
                        this.lastSuccessPingpongTime = System.currentTimeMillis();
                        EMLog.d("smart ping", "send ping-pong successed");
                        if(this.pingState == SmartHeartBeat.EMSmartPingState.EMHitted) {
                            EMLog.d("smart ping", "that\'s already in the EMHitted state, just return...");
                            return;
                        }

                        if(this.successedInterval == 270000 || this.pingState == SmartHeartBeat.EMSmartPingState.EMReevaluating) {
                            if(this.successedInterval == 270000) {
                                EMLog.d("smart ping", "Find the best interval, interval is the max interval");
                            }

                            if(this.pingState == SmartHeartBeat.EMSmartPingState.EMReevaluating) {
                                EMLog.d("smart ping", "success to pingping and current state is EMSmartPingState.EMReevaluating, so use current interval as final interval");
                            }

                            EMLog.d("smart ping", "enter the ping state : " + this.pingState);
                            this.changeState(SmartHeartBeat.EMSmartPingState.EMHitted);
                            return;
                        }

                        this.currentInterval += this.heartbeatStep;
                        if(this.currentInterval >= 270000) {
                            this.currentInterval = 270000;
                        }
                    } catch (Exception var5) {
                        return;
                    }
                }

                if(!var1) {
                    EMLog.d("smart ping", "failed to send ping pong ... with current heartbeat interval : " + Collector.timeToString((long)this.currentInterval));
                    if(this.hasDataConnection()) {
                        if(this.successedInterval != 0) {
                            this.currentInterval = this.successedInterval;
                            if(this.pingState == SmartHeartBeat.EMSmartPingState.EMEvaluating || this.pingState == SmartHeartBeat.EMSmartPingState.EMHitted) {
                                EMLog.d("smart ping", "send ping-pong failed, but has success interval candiate with ping state : " + this.pingState + " enter EMSmartPingState.EMReevaluating");
                                this.changeState(SmartHeartBeat.EMSmartPingState.EMReevaluating);
                            }

                            this.successedInterval = 0;
                            ChatManager.getInstance().forceReconnect();
                        } else {
                            if(this.pingState == SmartHeartBeat.EMSmartPingState.EMReevaluating) {
                                this.pingState = SmartHeartBeat.EMSmartPingState.EMEvaluating;
                            }

                            if(this.currentInterval > 30000) {
                                this.currentInterval -= this.heartbeatStep;
                                if(this.currentInterval <= 30000) {
                                    this.currentInterval = 30000;
                                }

                                ChatManager.getInstance().forceReconnect();
                            } else if(this.minIntervalCounter <= 0) {
                                ChatManager.getInstance().forceReconnect();
                                this.reset();
                            } else {
                                --this.minIntervalCounter;
                            }
                        }
                    }
                }

                return;
            }

            ++var2;
        }
    }

    private boolean hasDataConnection() {
        return NetUtils.hasDataConnection(this.appContext) && ChatManager.getInstance().isConnected();
    }

    private void releaseWakelock() {
        if(this.wakeLock.isHeld()) {
            this.wakeLock.release();
            EMLog.d("smart ping", "released the wake lock");
        }

    }

    private boolean sendPingPong() {
        EMLog.d("smart ping", "send ping-pong type heartbeat");
        if(this.connection != null && this.connection.isConnected()) {
            PacketCollector var1 = this.connection.createPacketCollector(new PacketIDFilter(this.pingPacket.getPacketID()));
            this.heartbeatId = this.pingPacket.getPacketID();
            this.connection.sendPacket(this.pingPacket);
            IQ var2 = (IQ)var1.nextResult(15000L);
            var1.cancel();
            if(var2 == null) {
                EMLog.e("smart ping", "no result while send ping-pong");
                return false;
            } else if(var2.getError() != null) {
                EMLog.e("smart ping", "received a error pong: " + var2.getError());
                return false;
            } else {
                return true;
            }
        } else {
            EMLog.d("smart ping", "connection is null or not connected");
            return false;
        }
    }

    public void start() {
        if(this.pingState != SmartHeartBeat.EMSmartPingState.EMStopped) {
            if(ChatManager.getInstance().isConnected() && NetUtils.hasNetwork(this.appContext)) {
                if(this.dataReceivedDuringInterval) {
                    this.dataReceivedDuringInterval = false;
                    long var1 = System.currentTimeMillis() - this.lastPacketReceivedTime;
                    if(var1 - (long)this.currentInterval < 100000L) {
                        this.scheduleNextAlarm();
                        return;
                    }
                }

                EMLog.d("smart ping", "post heartbeat runnable");
                this.threadPool.execute(this.heartBeatRunnable);
            } else {
                if(this.dataReceivedDuringInterval) {
                    this.dataReceivedDuringInterval = false;
                }

                this.scheduleNextAlarm();
            }
        }
    }

    public void scheduleNextAlarm() {
        try {
            EMLog.d("smart ping", "schedule next alarm");
            EMLog.d("smart ping", "current heartbeat interval : " + Collector.timeToString((long)this.currentInterval) + " smart ping state : " + this.pingState);
            this.dataReceivedDuringInterval = false;
            AlarmManager var1 = (AlarmManager)this.appContext.getSystemService(Context.ALARM_SERVICE);
            if(this.alarmIntent == null) {
                Intent var2 = new Intent("easemob.chat.heatbeat." + ChatConfig.getInstance().APPKEY);
                this.alarmIntent = PendingIntent.getBroadcast(this.appContext, 0, var2, 0);
            }

            if(this.alarmIntentReceiver == null) {
                this.alarmIntentReceiver = new HeartBeatReceiver(this);
                IntentFilter var4 = new IntentFilter("easemob.chat.heatbeat." + ChatConfig.getInstance().APPKEY);
                this.appContext.registerReceiver(this.alarmIntentReceiver, var4);
            }

            Long var5 = Long.valueOf(System.currentTimeMillis() + 180000L);
            if(this.hasDataConnection()) {
                if(this.currentInterval <= 0) {
                    this.currentInterval = this.getDefaultInterval();
                    EMLog.d("smart ping", "current heartbeat interval is not set, use default interval : " + Collector.timeToString((long)this.currentInterval));
                }

                var5 = Long.valueOf(System.currentTimeMillis() + (long)this.currentInterval);
            } else {
                var5 = Long.valueOf(System.currentTimeMillis() + 180000L);
                EMLog.d("smart ping", "is not connected to server, so use idle interval : 3 mins");
            }

            if(Build.VERSION.SDK_INT >= 19) {
                var1.setExact(0, var5.longValue(), this.alarmIntent);
            } else {
                var1.set(0, var5.longValue(), this.alarmIntent);
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public void stop() {
        EMLog.d("smart ping", "stop heart beat timer");
        this.changeState(SmartHeartBeat.EMSmartPingState.EMStopped);
        this.threadPool.shutdownNow();
        this.reset();
        this.releaseWakelock();
        if(this.connection != null) {
            this.connection.removePacketListener(this.pktListener);
        }

        if(this.cnnListener != null) {
            ChatManager.getInstance().removeConnectionListener(this.cnnListener);
        }

        try {
            AlarmManager var1 = (AlarmManager)this.appContext.getSystemService(Context.ALARM_SERVICE);
            var1.cancel(this.alarmIntent);
            this.appContext.unregisterReceiver(this.alarmIntentReceiver);
            this.alarmIntentReceiver = null;
        } catch (Exception var2) {
            if(!var2.getMessage().contains("Receiver not registered")) {
                var2.printStackTrace();
            }
        }

    }

    private void changeState(SmartHeartBeat.EMSmartPingState var1) {
        EMLog.d("smart ping", "change smart ping state from : " + this.pingState + " to : " + var1);
        Object var2 = this.stateLock;
        synchronized(this.stateLock) {
            this.pingState = var1;
        }
    }

    private void reset() {
        EMLog.d("smart ping", "reset interval...");
        this.currentInterval = 0;
        this.successedInterval = 0;
        this.dataReceivedDuringInterval = false;
        this.minIntervalCounter = 3;
        this.changeState(SmartHeartBeat.EMSmartPingState.EMEvaluating);
    }

    private int getDefaultInterval() {
        boolean var1 = false;
        int var2;
        if(NetUtils.isWifiConnection(this.appContext)) {
            var2 = 120000;
        } else {
            var2 = 180000;
        }

        return var2;
    }

    private static enum EMSmartPingState {
        EMReady,
        EMEvaluating,
        EMReevaluating,
        EMHitted,
        EMStopped;

        private EMSmartPingState() {
        }
    }
}

