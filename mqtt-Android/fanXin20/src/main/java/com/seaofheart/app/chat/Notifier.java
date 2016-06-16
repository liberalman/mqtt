package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.seaofheart.app.EventListener;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.util.EasyUtils;
import com.seaofheart.app.NotifierEvent;
import com.seaofheart.app.util.EMLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Notifier {
    private static final String TAG = "notify";
    static Ringtone ringtone = null;
    private static final String[] msg_eng = new String[]{"sent a message", "sent a picture", "sent a voice", "sent location message", "sent a video", "sent a file", "%1 contacts sent %2 messages"};
    private static final String[] msg_ch = new String[]{"发来一条消息", "发来一张图片", "发来一段语音", "发来位置信息", "发来一个视频", "发来一个文件", "%1个联系人发来%2条消息"};
    private static int notifyID = 341;
    private NotificationManager notificationManager = null;
    private HashSet<String> fromUsers = new HashSet();
    private int notificationNum = 0;
    private Context appContext;
    private String appName;
    private String packageName;
    private String[] msgs;
    private long lastNotifiyTime;
    private static Notifier instance;
    private OnMessageNotifyListener onMessageNotifyListener;
    private AudioManager audioManager;
    private Vibrator vibrator;
    private Hashtable<NotifierEvent.Event, List<EventListener>> filteredEventListeners = new Hashtable();
    private ExecutorService notifierThread = Executors.newSingleThreadExecutor();
    private ExecutorService newMsgQueue = Executors.newSingleThreadExecutor();

    private Notifier(Context var1) {
        this.appContext = var1;
        if(this.notificationManager == null) {
            this.notificationManager = (NotificationManager)var1.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        this.onMessageNotifyListener = ChatManager.getInstance().getChatOptions().onMessageNotifyListener;
        if(this.appContext.getApplicationInfo().labelRes != 0) {
            this.appName = this.appContext.getString(this.appContext.getApplicationInfo().labelRes);
        } else {
            this.appName = "";
        }

        this.packageName = this.appContext.getApplicationInfo().packageName;
        if(Locale.getDefault().getLanguage().equals("zh")) {
            this.msgs = msg_ch;
        } else {
            this.msgs = msg_eng;
        }

        this.audioManager = (AudioManager)this.appContext.getSystemService(Context.AUDIO_SERVICE);
        this.vibrator = (Vibrator)this.appContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void clear() {
        this.filteredEventListeners.clear();
    }

    public static synchronized Notifier getInstance(Context var0) {
        if(instance == null) {
            instance = new Notifier(var0);
            return instance;
        } else {
            return instance;
        }
    }

    public void stop() {
        if(ringtone != null) {
            ringtone.stop();
            ringtone = null;
        }

    }

    public void registerEventListener(EventListener var1) {
        if(var1 != null) {
            this.registerEventListener(var1, new NotifierEvent.Event[]{NotifierEvent.Event.EventNewCMDMessage, NotifierEvent.Event.EventNewMessage, NotifierEvent.Event.EventDeliveryAck, NotifierEvent.Event.EventOfflineMessage, NotifierEvent.Event.EventReadAck, NotifierEvent.Event.EventConversationListChanged});
        }
    }

    public void removeEventListener(EventListener var1) {
        if(var1 != null) {
            Hashtable var2 = this.filteredEventListeners;
            synchronized(this.filteredEventListeners) {
                Collection var3 = this.filteredEventListeners.values();
                if(var3 != null) {
                    Iterator var4 = var3.iterator();

                    while(var4.hasNext()) {
                        this.remove((List)var4.next(), var1);
                    }
                }

            }
        }
    }

    public void registerEventListener(EventListener var1, NotifierEvent.Event[] var2) {
        Hashtable var3 = this.filteredEventListeners;
        synchronized(this.filteredEventListeners) {
            NotifierEvent.Event[] var7 = var2;
            int var6 = var2.length;

            for(int var5 = 0; var5 < var6; ++var5) {
                NotifierEvent.Event var4 = var7[var5];
                this.registerEventListener(var1, var4);
            }

        }
    }

    private void remove(List<EventListener> var1, EventListener var2) {
        Iterator var3 = var1.iterator();

        while(var3.hasNext()) {
            EventListener var4 = (EventListener)var3.next();
            if(var4 == var2) {
                var3.remove();
            }
        }

    }

    boolean publishEvent(final NotifierEvent.Event var1, final Object var2) {
        EMLog.d("notify", "publish event, event type: " + var1.toString());
        if(this.containsType(var1)) {
            this.notifierThread.submit(new Runnable() {
                public void run() {
                    synchronized(Notifier.this.filteredEventListeners) {
                        if(Notifier.this.containsType(var1)) {
                            List var2x = (List)Notifier.this.filteredEventListeners.get(var1);
                            if(var2x != null) {
                                Iterator var3 = var2x.iterator();
                                Notifier.this.publishEvent(var3, var1, var2);
                            }
                        }

                    }
                }
            });
            return true;
        } else {
            return false;
        }
    }

    private void registerEventListener(EventListener var1, NotifierEvent.Event var2) {
        if(!this.filteredEventListeners.containsKey(var2)) {
            ArrayList var3 = new ArrayList();
            var3.add(var1);
            this.filteredEventListeners.put(var2, var3);
        } else {
            List var4 = (List)this.filteredEventListeners.get(var2);
            if(!var4.contains(var1)) {
                var4.add(0, var1);
            }
        }

    }

    private boolean containsType(NotifierEvent.Event var1) {
        return this.filteredEventListeners.containsKey(var1);
    }

    private void publishEvent(Iterator<EventListener> var1, NotifierEvent.Event var2, Object var3) {
        while(var1.hasNext()) {
            NotifierEvent var4 = new NotifierEvent();
            var4.setEventData(var3);
            var4.setEvent(var2);
            EventListener var5 = (EventListener)var1.next();
            if(var5 != null) {
                var5.onEvent(var4);
            }
        }

    }

    void resetNotificationCount() {
        this.notificationNum = 0;
        this.fromUsers.clear();
    }

    void notifyChatMsg(final Message var1) {
        this.newMsgQueue.submit(new Runnable() {
            public void run() {
                if(!Notifier.this.publishEvent(NotifierEvent.Event.EventNewMessage, var1)) {
                    if(var1.getBooleanAttribute("em_ignore_notification", false)) {
                        if(EasyUtils.isAppRunningForeground(Notifier.this.appContext)) {
                            Notifier.this.sendBroadcast(var1);
                        }

                    } else {
                        String var1x = null;
                        List var2 = null;
                        if(var1.chatType == ProtocolMessage.CHAT_TYPE.CHAT_SINGLE) {
                            var1x = var1.getFrom();
                            var2 = ChatManager.getInstance().getChatOptions().getUsersOfNotificationDisabled();
                        } else {
                            var1x = var1.getTo();
                            var2 = ChatManager.getInstance().getChatOptions().getGroupsOfNotificationDisabled();
                        }

                        if(ChatManager.getInstance().getChatOptions().isShowNotificationInBackgroud() && !EasyUtils.isAppRunningForeground(Notifier.this.appContext)) {
                            EMLog.d("notify", "easemob chat app is not running, sending notification");
                            if(var2 == null || !var2.contains(var1x)) {
                                Notifier.this.sendNotification(var1);
                                Notifier.this.notifyOnNewMsg();
                            }

                        } else {
                            Notifier.this.sendBroadcast(var1);
                            if(Chat.getInstance().appInited && (var2 == null || !var2.contains(var1x))) {
                                Notifier.this.notifyOnNewMsg();
                            }

                        }
                    }
                }
            }
        });
    }

    void sendReadAckMsgBroadcast(String var1, String var2) {
        if(!this.publishEvent(NotifierEvent.Event.EventReadAck, ChatManager.getInstance().getMessage(var2))) {
            Intent var3 = new Intent(ChatManager.getInstance().getAckMessageBroadcastAction());
            var3.putExtra("msgid", var2);
            var3.putExtra("from", var1);
            EMLog.d("notify", "send ack message broadcast for msg:" + var2);
            this.appContext.sendOrderedBroadcast(var3, (String)null);
        }
    }

    void sendDeliveryAckMsgBroadcast(String var1, String var2) {
        if(!this.publishEvent(NotifierEvent.Event.EventDeliveryAck, ChatManager.getInstance().getMessage(var2))) {
            Intent var3 = new Intent(ChatManager.getInstance().getDeliveryAckMessageBroadcastAction());
            var3.putExtra("msgid", var2);
            var3.putExtra("from", var1);
            EMLog.d("notify", "send delivery ack message broadcast for msg:" + var2);
            this.appContext.sendOrderedBroadcast(var3, (String)null);
        }
    }

    void sendBroadcast(Message var1) {
        Intent var2 = new Intent(ChatManager.getInstance().getNewMessageBroadcastAction());
        var2.putExtra("msgid", var1.msgId);
        var2.putExtra("from", var1.from.username);
        var2.putExtra("type", var1.type.ordinal());
        EMLog.d("notify", "send new message broadcast for msg:" + var1.msgId);
        this.appContext.sendOrderedBroadcast(var2, (String)null);
    }

    void sendCmdMsgBroadcast(Message var1) {
        if(!this.publishEvent(NotifierEvent.Event.EventNewCMDMessage, var1)) {
            Context var2 = Chat.getInstance().getAppContext();
            Intent var3 = new Intent(ChatManager.getInstance().getCmdMessageBroadcastAction());
            var3.putExtra("msgid", var1.getMsgId());
            var3.putExtra("message", var1);
            EMLog.d("notify", "received cmd message: " + var1.getMsgId());
            var2.sendOrderedBroadcast(var3, (String)null);
        }
    }

    private void sendNotification(Message var1) {
        String var2 = var1.from.getNick();

        try {
            String var3 = "";
            String var4 = var2 + " ";
            switch(var1.type.ordinal()) { // $SWITCH_TABLE$com$easemob$chat$Message$Type()[var1.type.ordinal()]
                case 1:
                    var4 = var4 + this.msgs[0];
                    TextMessageBody var5 = (TextMessageBody)var1.body;
                    var3 = var5.message;
                    break;
                case 2:
                    var4 = var4 + this.msgs[1];
                    break;
                case 3:
                    var4 = var4 + this.msgs[4];
                    break;
                case 4:
                    var4 = var4 + this.msgs[3];
                    break;
                case 5:
                    var4 = var4 + this.msgs[2];
                    break;
                case 6:
                    var4 = var4 + this.msgs[5];
                    FileMessageBody var6 = (FileMessageBody)var1.body;
                    var3 = var6.fileName;
            }

            this.onMessageNotifyListener = ChatManager.getInstance().getChatOptions().onMessageNotifyListener;
            PackageManager var17 = this.appContext.getPackageManager();
            String var18 = (String)var17.getApplicationLabel(this.appContext.getApplicationInfo());
            String var7 = var18;
            if(this.onMessageNotifyListener != null) {
                String var8 = this.onMessageNotifyListener.onNewMessageNotify(var1);
                String var9 = this.onMessageNotifyListener.onSetNotificationTitle(var1);
                if(var8 != null) {
                    var4 = var8;
                }

                if(var9 != null) {
                    var7 = var9;
                }
            }

            NotificationCompat.Builder var19 = (new NotificationCompat.Builder(this.appContext)).setSmallIcon(this.appContext.getApplicationInfo().icon).setWhen(System.currentTimeMillis()).setAutoCancel(true);
            Intent var20 = this.appContext.getPackageManager().getLaunchIntentForPackage(this.packageName);
            if(ChatManager.getInstance().getChatOptions().onNotificationClickListener != null) {
                var20 = ChatManager.getInstance().getChatOptions().onNotificationClickListener.onNotificationClick(var1);
            }

            PendingIntent var10 = PendingIntent.getActivity(this.appContext, notifyID, var20, PendingIntent.FLAG_CANCEL_CURRENT);
            ++this.notificationNum;
            this.fromUsers.add(var1.getFrom());
            int var11 = this.fromUsers.size();
            String var12 = this.msgs[6].replaceFirst("%1", Integer.toString(var11)).replaceFirst("%2", Integer.toString(this.notificationNum));
            if(this.onMessageNotifyListener != null) {
                String var13 = this.onMessageNotifyListener.onLatestMessageNotify(var1, var11, this.notificationNum);
                if(var13 != null) {
                    var12 = var13;
                }

                int var14 = this.onMessageNotifyListener.onSetSmallIcon(var1);
                if(var14 != 0) {
                    var19.setSmallIcon(var14);
                }
            }

            var19.setContentTitle(var7);
            var19.setTicker(var4);
            var19.setContentText(var12);
            var19.setContentIntent(var10);
            Notification var21 = var19.build();

            try {
                this.notificationManager.cancel(notifyID);
            } catch (Exception var15) {
                ;
            }

            this.notificationManager.notify(notifyID, var21);
        } catch (Exception var16) {
            var16.printStackTrace();
        }

    }

    public void notifyOnNewMsg() {
        if(ChatManager.getInstance().getChatOptions().getNotificationEnable() && ChatManager.getInstance().getChatOptions().getNotifyBySoundAndVibrate()) {
            try {
                if(System.currentTimeMillis() - this.lastNotifiyTime < 1000L) {
                    return;
                }

                this.lastNotifiyTime = System.currentTimeMillis();
                if(this.audioManager.getRingerMode() == 0) {
                    EMLog.e("notify", "in slient mode now");
                    return;
                }

                if(ChatManager.getInstance().getChatOptions().getNoticedByVibrate()) {
                    long[] var1 = new long[]{0L, 180L, 80L, 120L};
                    this.vibrator.vibrate(var1, -1);
                }

                if(ChatManager.getInstance().getChatOptions().getNoticedBySound()) {
                    String var4 = Build.MANUFACTURER;
                    if(ringtone == null) {
                        Uri var2 = null;
                        if(ChatManager.getInstance().getChatOptions().getNotifyRingUri() == null) {
                            var2 = RingtoneManager.getDefaultUri(2);
                        } else {
                            var2 = ChatManager.getInstance().getChatOptions().getNotifyRingUri();
                        }

                        ringtone = RingtoneManager.getRingtone(this.appContext, var2);
                        if(ringtone == null) {
                            EMLog.d("notify", "cant find ringtone at:" + var2.getPath());
                            return;
                        }
                    }

                    if(!ringtone.isPlaying()) {
                        ringtone.play();
                        if(var4 != null && var4.toLowerCase().contains("samsung")) {
                            Thread var5 = new Thread() {
                                public void run() {
                                    try {
                                        Thread.sleep(3000L);
                                        if(Notifier.ringtone.isPlaying()) {
                                            Notifier.ringtone.stop();
                                        }
                                    } catch (Exception var2) {
                                        ;
                                    }

                                }
                            };
                            var5.run();
                        }
                    }
                }
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        }
    }

    void cancelNotificaton() {
        if(this.notificationManager != null) {
            this.notificationManager.cancel(notifyID);
        }

    }

    void sendIncomingVoiceCallBroadcast(String var1, String var2) {
        String var3 = ContactManager.getUserNameFromEid(var1);
        Intent var4 = new Intent(ChatManager.getInstance().getIncomingCallBroadcastAction());
        var4.putExtra("from", var3);
        var4.putExtra("type", var2);
        EMLog.d("notify", "send incoming call broadcaset with user : " + var3);
        if(VoiceCallManager.getInstance().getActiveSession() != null) {
            if(var2 == VoiceCallManager.CallType.audio.toString()) {
                Intent var5 = new Intent(ChatManager.getInstance().getIncomingVoiceCallBroadcastAction());
                var5.putExtra("from", var3);
                this.appContext.sendBroadcast(var5, (String)null);
            }

            this.appContext.sendBroadcast(var4, (String)null);
        }
    }
}

