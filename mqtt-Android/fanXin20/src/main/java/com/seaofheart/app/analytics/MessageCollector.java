package com.seaofheart.app.analytics;

/**
 * Created by Administrator on 2016/4/14.
 */
import com.seaofheart.app.util.EMLog;
import com.seaofheart.app.chat.Message;

public class MessageCollector extends Collector {
    private static final String TAG = "[Collector][Message]";
    public static final String SENT_MESSAGE = "sent message time";

    public MessageCollector() {
    }

    public static void collectSendMsgTime(long var0, Message var2) {
        EMLog.d("[Collector][Message]" + getTagPrefix("sent message time"), "send message with type : " + var2.getType() + " status : " + var2.status + " time spent : " + timeToString(var0));
    }
}
