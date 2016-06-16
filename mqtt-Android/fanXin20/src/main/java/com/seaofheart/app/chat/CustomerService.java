package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.seaofheart.app.NotifierEvent;
import com.seaofheart.app.chat.core.p;
import com.seaofheart.app.chat.core.r;
import com.seaofheart.app.util.EMLog;

class CustomerService implements r {
    private static final String TAG = "CustomerService";
    private CustomerServiceConfiguration configuration = null;
    private static CustomerService instance = null;
    private PendingIntent logoutIntent = null;
    private CustomerService.HelpDeskLogoutReceiver logoutReceiver = null;
    private static final String CONFIG_EASEMOB_HELPDESK_JID = "EASEMOB_HELPDESK_JID";
    private static final String HELPDESK_LOGOUT_BROADCAST = "easemob.helpdesk.logout.";
    private static final long MAX_LOGOUT_DELAY = 57600L;
    private static final long MIN_INTERVAL_UPDATE_LOGOUT_TIME = 600L;
    private long logoutDelay = 57600L;

    private CustomerService() {
    }

    static synchronized CustomerService getInstance() {
        if(instance == null) {
            instance = new CustomerService();
        }

        return instance;
    }

    void setConfiguration(CustomerServiceConfiguration var1) {
        this.configuration = var1;
    }

    boolean isCustomServiceAgent(String var1) {
        return this.configuration == null?false:(this.configuration.getAgents() == null?false:this.configuration.getAgents().contains(var1));
    }

    public void setLogoutDelay(long var1) {
        if(var1 < 57600L) {
            this.logoutDelay = var1;
        }

    }

    private String getLogoutBroadcastAction() {
        return "easemob.helpdesk.logout." + ChatConfig.getInstance().APPKEY + Chat.getInstance().getAppContext().getPackageName();
    }

    public void scheduleLogout(CustomerService.EMScheduleLogoutReason var1) {
        EMLog.d("CustomerService", "schedule helpdesk logout");
        Long var2 = Long.valueOf(System.currentTimeMillis() + this.logoutDelay * 1000L);
        if(p.getInstance().K()) {
            long var3 = p.getInstance().L();
            boolean var5 = false;
            if(!var5 && Math.abs(var2.longValue() - var3) < 600000L) {
                return;
            }

            long var6 = System.currentTimeMillis();
            if(var1 == CustomerService.EMScheduleLogoutReason.EMNewMessage) {
                if(var3 < var6 + this.logoutDelay * 1000L) {
                    var2 = Long.valueOf(var6 + this.logoutDelay * 1000L);
                }
            } else if(var1 == CustomerService.EMScheduleLogoutReason.EMLogin) {
                if(var3 < var6) {
                    this.forceLogout();
                    return;
                }

                var2 = Long.valueOf(var3);
            }
        }

        Context var9 = Chat.getInstance().getAppContext();

        try {
            AlarmManager var4 = (AlarmManager)var9.getSystemService(Context.ALARM_SERVICE);
            if(this.logoutIntent == null) {
                Intent var10 = new Intent(this.getLogoutBroadcastAction());
                this.logoutIntent = PendingIntent.getBroadcast(var9, 0, var10, 0);
            }

            if(this.logoutReceiver == null) {
                this.logoutReceiver = new CustomerService.HelpDeskLogoutReceiver();
                IntentFilter var11 = new IntentFilter(this.getLogoutBroadcastAction());
                var9.registerReceiver(this.logoutReceiver, var11);
            }

            var4.cancel(this.logoutIntent);
            var4.set(0, var2.longValue(), this.logoutIntent);
            p.getInstance().c(var2.longValue());
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    void unregisterLogoutTimerReceiver() {
        EMLog.d("CustomerService", "unregister helpdesk logout receiver");
        if(this.logoutReceiver != null) {
            Context var1 = Chat.getInstance().getAppContext();
            if(this.logoutIntent == null) {
                Intent var2 = new Intent(this.getLogoutBroadcastAction());
                this.logoutIntent = PendingIntent.getBroadcast(var1, 0, var2, 0);
            }

            try {
                AlarmManager var4 = (AlarmManager)var1.getSystemService(Context.ALARM_SERVICE);
                var4.cancel(this.logoutIntent);
                var1.unregisterReceiver(this.logoutReceiver);
                this.logoutReceiver = null;
            } catch (Exception var3) {
                if(!var3.getMessage().contains("Receiver not registered")) {
                    var3.printStackTrace();
                }
            }

        }
    }

    public void cancelScheduledLogout() {
        EMLog.d("CustomerService", "cancel helpdesk logout");
        p.getInstance().M();
        this.unregisterLogoutTimerReceiver();
    }

    private void forceLogout() {
        ChatManager.getInstance().logout();
        p.getInstance().M();
        Notifier.getInstance(Chat.getInstance().getAppContext()).publishEvent(NotifierEvent.Event.EventLogout, (Object)null);
    }

    boolean isMessageBelongingToCusomerService(Message var1) {
        return this.isCustomServiceAgent(var1.getFrom());
    }

    public void onInit() {
    }

    public void onDestroy() {
        this.cancelScheduledLogout();
    }

    public static enum EMScheduleLogoutReason {
        EMLogin,
        EMNewMessage;

        private EMScheduleLogoutReason() {
        }
    }

    public class HelpDeskLogoutReceiver extends BroadcastReceiver {
        private static final String TAG = "HelpDeskLogoutReceiver";

        public HelpDeskLogoutReceiver() {
        }

        public void onReceive(Context var1, Intent var2) {
            EMLog.d("HelpDeskLogoutReceiver", "HelpDesk logout receiver received message");
            CustomerService.this.forceLogout();
        }
    }
}

