package com.seaofheart.app;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class NotifierEvent {
    private Object eventData = null;
    private NotifierEvent.Event event;

    public NotifierEvent() {
        this.event = NotifierEvent.Event.EventNewMessage;
    }

    public void setEvent(NotifierEvent.Event var1) {
        this.event = var1;
    }

    public NotifierEvent.Event getEvent() {
        return this.event;
    }

    public void setEventData(Object var1) {
        this.eventData = var1;
    }

    public Object getData() {
        return this.eventData;
    }

    public static enum Event {
        EventNewMessage,
        EventNewCMDMessage,
        EventReadAck,
        EventDeliveryAck,
        EventOfflineMessage,
        EventConversationListChanged,
        EventMessageChanged,
        EventLogout;

        private Event() {
        }
    }
}

