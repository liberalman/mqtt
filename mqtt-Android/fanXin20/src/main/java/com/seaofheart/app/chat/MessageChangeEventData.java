package com.seaofheart.app.chat;

/**
 * Created by Administrator on 2016/4/14.
 */

public class MessageChangeEventData extends ChangeEventData<Object> {
    public MessageChangeEventData.ChangeSource source = null;
    public Message changedMsg = null;

    public MessageChangeEventData() {
    }

    public static enum ChangeSource {
        MessageState,
        MessageId;

        private ChangeSource() {
        }
    }
}