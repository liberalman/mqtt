package com.seaofheart.app;

/**
 * Created by Administrator on 2016/4/14.
 */
import com.seaofheart.app.chat.Message;

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
