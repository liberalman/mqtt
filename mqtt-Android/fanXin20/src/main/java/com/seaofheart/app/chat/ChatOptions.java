package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.net.Uri;

import java.util.List;

public class ChatOptions {
    private boolean acceptInvitationAlways = true;
    private boolean useEncryption = false;
    private boolean useRoster = false;
    private boolean noticedBySound = true;
    private boolean noticedByVibrate = true;
    private boolean notificationEnable = true;
    private boolean useSpeaker = true;
    private int numberOfMessagesLoaded = 20;
    private boolean requireReadAck = true;
    private boolean requireDeliveryAck = false;
    private boolean requireServerAck = true;
    private boolean audioFileWithExt = false;
    OnMessageNotifyListener onMessageNotifyListener;
    OnNotificationClickListener onNotificationClickListener;
    private boolean showNotification = true;
    private Uri ringUri;
    private List<String> groupsOfNotificationDisabled = null;
    private List<String> usersOfNotificationDisabled = null;
    private boolean autoConversationsLoaded = true;
    private boolean deleteMessagesAsExitGroup = true;
    private boolean isChatroomOwnerLeaveAllowed = true;
    private long offlineInterval = 3000L;
    private boolean disableAutoLeaveChatroom = false;

    public ChatOptions() {
    }

    public boolean getRequireAck() {
        return this.requireReadAck;
    }

    public void setRequireAck(boolean var1) {
        this.requireReadAck = var1;
    }

    public boolean getRequireDeliveryAck() {
        return this.requireDeliveryAck;
    }

    public void setRequireDeliveryAck(boolean var1) {
        this.requireDeliveryAck = var1;
    }

    public boolean getNoticedBySound() {
        return this.noticedBySound;
    }

    public void setNoticeBySound(boolean var1) {
        this.noticedBySound = var1;
    }

    public boolean getNoticedByVibrate() {
        return this.noticedByVibrate;
    }

    public void setNoticedByVibrate(boolean var1) {
        this.noticedByVibrate = var1;
    }

    public boolean getNotificationEnable() {
        return this.notificationEnable;
    }

    public void setNotificationEnable(boolean var1) {
        this.notificationEnable = var1;
    }

    public boolean getNotifyBySoundAndVibrate() {
        return this.notificationEnable;
    }

    public void setNotifyBySoundAndVibrate(boolean var1) {
        this.notificationEnable = var1;
    }

    public boolean getUseSpeaker() {
        return this.useSpeaker;
    }

    public void setUseSpeaker(boolean var1) {
        this.useSpeaker = var1;
    }

    public void setUseEncryption(boolean var1) {
        this.useEncryption = var1;
    }

    public boolean getUseEncryption() {
        return this.useEncryption;
    }

    public boolean getUseRoster() {
        return this.useRoster;
    }

    public void setUseRoster(boolean var1) {
        this.useRoster = var1;
    }

    public boolean getAcceptInvitationAlways() {
        return this.acceptInvitationAlways;
    }

    public void setAcceptInvitationAlways(boolean var1) {
        this.acceptInvitationAlways = var1;
    }

    public boolean getRequireServerAck() {
        return this.requireServerAck;
    }

    protected boolean getAudioFileWithExt() {
        return this.audioFileWithExt;
    }

    public void setAudioFileWithExt(boolean var1) {
        this.audioFileWithExt = var1;
    }

    public void setRequireServerAck(boolean var1) {
        this.requireServerAck = var1;
    }

    public void setNotifyText(OnMessageNotifyListener var1) {
        this.onMessageNotifyListener = var1;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener var1) {
        this.onNotificationClickListener = var1;
    }

    public void setShowNotificationInBackgroud(boolean var1) {
        this.showNotification = var1;
    }

    public boolean isShowNotificationInBackgroud() {
        return this.showNotification;
    }

    public void setNotifyRingUri(Uri var1) {
        this.ringUri = var1;
    }

    public Uri getNotifyRingUri() {
        return this.ringUri;
    }

    public int getNumberOfMessagesLoaded() {
        return this.numberOfMessagesLoaded;
    }

    public void setNumberOfMessagesLoaded(int var1) {
        if(var1 > 0) {
            this.numberOfMessagesLoaded = var1;
        }

    }

    public void setReceiveNotNoifyGroup(List<String> var1) {
        this.setGroupsOfNotificationDisabled(var1);
    }

    public List<String> getReceiveNoNotifyGroup() {
        return this.getGroupsOfNotificationDisabled();
    }

    public void setGroupsOfNotificationDisabled(List<String> var1) {
        this.groupsOfNotificationDisabled = var1;
    }

    public List<String> getGroupsOfNotificationDisabled() {
        return this.groupsOfNotificationDisabled;
    }

    public void setUsersOfNotificationDisabled(List<String> var1) {
        this.usersOfNotificationDisabled = var1;
    }

    public List<String> getUsersOfNotificationDisabled() {
        return this.usersOfNotificationDisabled;
    }

    public void setDeleteMessagesAsExitGroup(boolean var1) {
        this.deleteMessagesAsExitGroup = var1;
    }

    public boolean isDeleteMessagesAsExitGroup() {
        return this.deleteMessagesAsExitGroup;
    }

    public void setAutoConversatonsLoaded(boolean var1) {
        this.autoConversationsLoaded = var1;
    }

    public boolean getAutoConversationsLoaded() {
        return this.autoConversationsLoaded;
    }

    public void allowChatroomOwnerLeave(boolean var1) {
        this.isChatroomOwnerLeaveAllowed = var1;
    }

    public boolean isChatroomOwnerLeaveAllowed() {
        return this.isChatroomOwnerLeaveAllowed;
    }

    public void setOfflineInterval(long var1) {
        if(var1 < 0L) {
            var1 = 3000L;
        }

        this.offlineInterval = var1;
    }

    public long getOfflineInterval() {
        return this.offlineInterval;
    }

    public void disableAutomaticallyLeaveChatroomOnLogin(boolean var1) {
        this.disableAutoLeaveChatroom = var1;
    }

    public boolean isAutomaticallyLeaveChatroomDisabledOnLogin() {
        return this.disableAutoLeaveChatroom;
    }
}

