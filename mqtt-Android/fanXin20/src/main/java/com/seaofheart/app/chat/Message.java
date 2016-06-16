package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.exceptions.EaseMobException;
import com.seaofheart.app.CallBack;
import com.seaofheart.app.util.EMLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class Message implements Parcelable, Cloneable {
    private static final String TAG = "msg";
    CallBack messageStatusCallBack;
    //Message.Type type;
    ProtocolMessage.TYPE type;
    //public Message.Direct direct;
    public ProtocolMessage.DIRECT direct;
    //public Message.Status status;
    public ProtocolMessage.STATUS status;
    Contact from;
    Contact to;
    MessageBody body;
    String msgId;
    public boolean isAcked;
    public boolean isDelivered;
    long msgTime;
    //Message.ChatType chatType;
    ProtocolMessage.CHAT_TYPE chatType;
    public transient int progress;
    Hashtable<String, Object> attributes;
    transient boolean unread;
    transient boolean offline;
    boolean isListened;
    static final String ATTR_ENCRYPTED = "isencrypted";
    private int error;
    public static final Creator<Message> CREATOR = new Creator() {
        public Message createFromParcel(Parcel var1) {
            return new Message(var1);
        }

        public Message[] newArray(int var1) {
            return new Message[var1];
        }
    };

    Message(ProtocolMessage.TYPE var1) {
        this.status = ProtocolMessage.STATUS.CREATE;
        this.isAcked = false;
        this.isDelivered = false;
        this.chatType = ProtocolMessage.CHAT_TYPE.CHAT_SINGLE;
        this.progress = 0;
        this.attributes = new Hashtable();
        this.unread = true;
        this.offline = false;
        this.error = 0;
        this.type = var1;
        this.msgTime = System.currentTimeMillis();
    }

    public ProtocolMessage.TYPE getType() {
        return this.type;
    }

    public MessageBody getBody() {
        return this.body;
    }

    public long getMsgTime() {
        return this.msgTime;
    }

    public void setMsgTime(long var1) {
        this.msgTime = var1;
    }

    public static Message createSendMessage(ProtocolMessage.TYPE var0) {
        Message var1 = new Message(var0);
        var1.direct = ProtocolMessage.DIRECT.SEND;
        Contact var2 = SessionManager.getInstance().currentUser;
        if(var2 == null) {
            String var3 = SessionManager.getInstance().getLastLoginUser();
            var2 = ContactManager.getInstance().getContactByUserName(var3);
        }

        var1.from = var2;
        var1.setMsgId(MessageUtils.getUniqueMessageId());
        return var1;
    }

    public static Message createReceiveMessage(ProtocolMessage.TYPE type) {
        Message msg = new Message(type);
        msg.direct = ProtocolMessage.DIRECT.RECEIVE;
        msg.to = SessionManager.getInstance().currentUser;
        return msg;
    }

    public static Message createTxtSendMessage(String var0, String var1) {
        if(var0.length() > 0) {
            Message var2 = createSendMessage(ProtocolMessage.TYPE.TXT);
            TextMessageBody var3 = new TextMessageBody(var0);
            var2.addBody(var3);
            var2.setReceipt(var1);
            return var2;
        } else {
            EMLog.e("msg", "text content size must be greater than 10");
            return null;
        }
    }

    public static Message createVoiceSendMessage(String var0, int var1, String var2) {
        if(!(new File(var0)).exists()) {
            EMLog.e("msg", "voice file does not exsit");
            return null;
        } else {
            Message var3 = createSendMessage(ProtocolMessage.TYPE.VOICE);
            VoiceMessageBody var4 = new VoiceMessageBody(new File(var0), var1);
            var3.addBody(var4);
            var3.setReceipt(var2);
            return var3;
        }
    }

    public static Message createImageSendMessage(String var0, boolean var1, String var2) {
        if(!(new File(var0)).exists()) {
            EMLog.e("msg", "image file does not exsit");
            return null;
        } else {
            Message var3 = createSendMessage(ProtocolMessage.TYPE.IMAGE);
            var3.setReceipt(var2);
            ImageMessageBody var4 = new ImageMessageBody(new File(var0));
            var4.setSendOriginalImage(var1);
            var3.addBody(var4);
            return var3;
        }
    }

    public static Message createVideoSendMessage(String var0, String var1, int var2, String var3) {
        File var4 = new File(var0);
        if(!var4.exists()) {
            EMLog.e("msg", "video file does not exist");
            return null;
        } else {
            Message var5 = createSendMessage(ProtocolMessage.TYPE.VIDEO);
            var5.setReceipt(var3);
            VideoMessageBody var6 = new VideoMessageBody(var4, var1, var2, var4.length());
            var5.addBody(var6);
            return var5;
        }
    }

    public static Message createLocationSendMessage(double var0, double var2, String var4, String var5) {
        Message var6 = createSendMessage(ProtocolMessage.TYPE.LOCATION);
        LocationMessageBody var7 = new LocationMessageBody(var4, var0, var2);
        var6.addBody(var7);
        var6.setReceipt(var5);
        return var6;
    }

    public static Message createFileSendMessage(String var0, String var1) {
        File var2 = new File(var0);
        if(!var2.exists()) {
            EMLog.e("msg", "file does not exist");
            return null;
        } else {
            Message var3 = createSendMessage(ProtocolMessage.TYPE.FILE);
            var3.setReceipt(var1);
            NormalFileMessageBody var4 = new NormalFileMessageBody(new File(var0));
            var3.addBody(var4);
            return var3;
        }
    }

    public void addBody(MessageBody var1) {
        this.body = var1;
    }

    public String getFrom() {
        return this.from == null?null:this.from.username;
    }

    public void setFrom(String var1) {
        Contact var2 = new Contact();
        var2.setUsername(var1);
        this.from = var2;
    }

    public void setTo(String var1) {
        Contact var2 = new Contact();
        var2.setUsername(var1);
        this.to = var2;
    }

    public String getTo() {
        return this.to == null?null:this.to.username;
    }

    public String getMsgId() {
        return this.msgId;
    }

    public void setMsgId(String var1) {
        this.msgId = var1;
    }

    public void setReceipt(String var1) {
        ContactManager var2 = ContactManager.getInstance();
        Contact var3 = null;
        if(var1.contains("@")) {
            EMLog.e("msg", "error wrong uesrname format:" + var1);
        } else {
            var3 = var2.getContactByUserName(var1);
        }

        if(var3 == null) {
            var3 = new Contact(var1);
        }

        this.to = var3;
    }

    public void setMessageStatusCallback(CallBack var1) {
        this.messageStatusCallBack = var1;
    }

    public String toString() {
        StringBuffer var1 = new StringBuffer();
        var1.append("msg{from:" + this.from.username);
        var1.append(", to:" + this.to.username);
        var1.append(" body:" + this.body.toString());
        return var1.toString();
    }

    public void setAttribute(String var1, boolean var2) {
        if(this.attributes == null) {
            this.attributes = new Hashtable();
        }

        this.attributes.put(var1, Boolean.valueOf(var2));
    }

    public void setAttribute(String var1, int var2) {
        if(this.attributes == null) {
            this.attributes = new Hashtable();
        }

        this.attributes.put(var1, Integer.valueOf(var2));
    }

    public void setAttribute(String var1, JSONObject var2) {
        if(this.attributes == null) {
            this.attributes = new Hashtable();
        }

        this.attributes.put(var1, var2);
    }

    public void setAttribute(String var1, JSONArray var2) {
        if(this.attributes == null) {
            this.attributes = new Hashtable();
        }

        this.attributes.put(var1, var2);
    }

    public void setAttribute(String var1, String var2) {
        if(this.attributes == null) {
            this.attributes = new Hashtable();
        }

        this.attributes.put(var1, var2);
    }

    public boolean getBooleanAttribute(String var1) throws EaseMobException {
        Boolean var2 = null;
        if(this.attributes != null) {
            Object var3 = this.attributes.get(var1);
            if(var3 instanceof Boolean) {
                var2 = (Boolean)var3;
            } else if(var3 instanceof Integer) {
                int var4 = ((Integer)var3).intValue();
                if(var4 > 0) {
                    var2 = Boolean.valueOf(true);
                } else {
                    var2 = Boolean.valueOf(false);
                }
            }
        }

        if(var2 == null) {
            throw new EaseMobException("attribute " + var1 + " not found");
        } else {
            return var2.booleanValue();
        }
    }

    public boolean getBooleanAttribute(String var1, boolean var2) {
        if(this.attributes == null) {
            return var2;
        } else {
            Boolean var3 = null;

            try {
                var3 = Boolean.valueOf(this.getBooleanAttribute(var1));
            } catch (EaseMobException var5) {
                ;
            }

            return var3 == null?var2:var3.booleanValue();
        }
    }

    public int getIntAttribute(String var1, int var2) {
        Integer var3 = null;
        if(this.attributes != null) {
            var3 = (Integer)this.attributes.get(var1);
        }

        return var3 == null?var2:var3.intValue();
    }

    public int getIntAttribute(String var1) throws EaseMobException {
        Integer var2 = null;
        if(this.attributes != null) {
            var2 = (Integer)this.attributes.get(var1);
        }

        if(var2 == null) {
            throw new EaseMobException("attribute " + var1 + " not found");
        } else {
            return var2.intValue();
        }
    }

    public String getStringAttribute(String var1) throws EaseMobException {
        if(this.attributes != null && this.attributes.containsKey(var1)) {
            Object var2 = this.attributes.get(var1);
            if(var2 instanceof String) {
                return (String)var2;
            } else if(var2 instanceof JSONObject) {
                return ((JSONObject)var2).toString();
            } else if(var2 instanceof JSONArray) {
                return ((JSONArray)var2).toString();
            } else {
                throw new EaseMobException("attribute " + var1 + " not String type");
            }
        } else {
            throw new EaseMobException("attribute " + var1 + " not found");
        }
    }

    public String getStringAttribute(String var1, String var2) {
        if(this.attributes != null && this.attributes.containsKey(var1)) {
            Object var3 = this.attributes.get(var1);
            if(var3 instanceof String) {
                return (String)var3;
            }

            if(var3 instanceof JSONObject) {
                return ((JSONObject)var3).toString();
            }

            if(var3 instanceof JSONArray) {
                return ((JSONArray)var3).toString();
            }
        }

        return var2;
    }

    public JSONObject getJSONObjectAttribute(String var1) throws EaseMobException {
        if(this.attributes != null && this.attributes.containsKey(var1)) {
            Object var2 = this.attributes.get(var1);
            if(var2 instanceof JSONObject) {
                return (JSONObject)var2;
            } else {
                if(var2 instanceof String) {
                    try {
                        JSONObject var3 = new JSONObject((String)var2);
                        return var3;
                    } catch (JSONException var4) {
                        ;
                    }
                }

                throw new EaseMobException("attribute " + var1 + " not JSONObject type");
            }
        } else {
            throw new EaseMobException("attribute " + var1 + " not found");
        }
    }

    public JSONArray getJSONArrayAttribute(String var1) throws EaseMobException {
        if(this.attributes != null && this.attributes.containsKey(var1)) {
            Object var2 = this.attributes.get(var1);
            if(var2 instanceof JSONArray) {
                return (JSONArray)var2;
            } else {
                if(var2 instanceof String) {
                    try {
                        JSONArray var3 = new JSONArray((String)var2);
                        return var3;
                    } catch (JSONException var4) {
                        ;
                    }
                }

                throw new EaseMobException("attribute " + var1 + " not JSONArray type");
            }
        } else {
            throw new EaseMobException("attribute " + var1 + " not found");
        }
    }

    public ProtocolMessage.CHAT_TYPE getChatType() {
        return this.chatType;
    }

    public void setChatType(ProtocolMessage.CHAT_TYPE var1) {
        this.chatType = var1;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeString(this.type.name());
        var1.writeString(this.direct.name());
        var1.writeString(this.msgId);
        var1.writeLong(this.msgTime);
        ArrayList var3 = new ArrayList();
        ArrayList var4 = new ArrayList();
        Hashtable var5 = new Hashtable();
        Iterator var7 = this.attributes.entrySet().iterator();

        while(var7.hasNext()) {
            Map.Entry var6 = (Map.Entry)var7.next();
            if(var6.getValue() != null) {
                if(var6.getValue() instanceof JSONObject) {
                    var3.add(Pair.create((String)var6.getKey(), (JSONObject)var6.getValue()));
                } else if(var6.getValue() instanceof JSONArray) {
                    var4.add(Pair.create((String)var6.getKey(), (JSONArray)var6.getValue()));
                } else {
                    var5.put((String)var6.getKey(), var6.getValue());
                }
            }
        }

        var1.writeInt(var3.size());
        var7 = var3.iterator();

        Pair var8;
        while(var7.hasNext()) {
            var8 = (Pair)var7.next();
            var1.writeString((String)var8.first);
            var1.writeString(((JSONObject)var8.second).toString());
        }

        var1.writeInt(var4.size());
        var7 = var4.iterator();

        while(var7.hasNext()) {
            var8 = (Pair)var7.next();
            var1.writeString((String)var8.first);
            var1.writeString(((JSONArray)var8.second).toString());
        }

        var1.writeMap(var5);
        var1.writeParcelable(this.from, var2);
        var1.writeParcelable(this.to, var2);
        var1.writeParcelable(this.body, var2);
        var1.writeString(this.chatType.name());
    }

    private Message(Parcel var1) {
        this.status = ProtocolMessage.STATUS.CREATE;
        this.isAcked = false;
        this.isDelivered = false;
        this.chatType = ProtocolMessage.CHAT_TYPE.CHAT_SINGLE;
        this.progress = 0;
        this.attributes = new Hashtable();
        this.unread = true;
        this.offline = false;
        this.error = 0;
        this.type = ProtocolMessage.TYPE.valueOf(var1.readString());
        this.direct = ProtocolMessage.DIRECT.valueOf(var1.readString());
        this.msgId = var1.readString();
        this.msgTime = var1.readLong();
        this.attributes = new Hashtable();
        int var2 = var1.readInt();

        int var3;
        for(var3 = 0; var3 < var2; ++var3) {
            String var4 = var1.readString();
            JSONObject var5 = null;

            try {
                var5 = new JSONObject(var1.readString());
            } catch (JSONException var9) {
                var9.printStackTrace();
            }

            this.attributes.put(var4, var5);
        }

        var3 = var1.readInt();

        for(int var10 = 0; var10 < var3; ++var10) {
            String var12 = var1.readString();
            JSONArray var6 = null;

            try {
                var6 = new JSONArray(var1.readString());
            } catch (JSONException var8) {
                var8.printStackTrace();
            }

            this.attributes.put(var12, var6);
        }

        Hashtable var11 = new Hashtable();
        var1.readMap(var11, (ClassLoader)null);
        this.attributes.putAll(var11);
        this.from = (Contact)var1.readParcelable(Message.class.getClassLoader());
        this.to = (Contact)var1.readParcelable(Message.class.getClassLoader());
        this.body = (MessageBody)var1.readParcelable(Message.class.getClassLoader());
        this.chatType = ProtocolMessage.CHAT_TYPE.valueOf(var1.readString());
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean isAcked() {
        return this.isAcked;
    }

    public void setAcked(boolean var1) {
        this.isAcked = var1;
    }

    public boolean isDelivered() {
        return this.isDelivered;
    }

    public void setDelivered(boolean var1) {
        this.isDelivered = var1;
    }

    public boolean isUnread() {
        return this.unread;
    }

    public void setUnread(boolean var1) {
        this.unread = var1;
    }

    public void setType(ProtocolMessage.TYPE var1) {
        this.type = var1;
    }

    public boolean isListened() {
        return this.isListened;
    }

    public void setListened(boolean var1) {
        this.isListened = var1;
    }

    public String getUserName() {
        String var1 = "";
        if(this.getFrom() != null && this.getFrom().equals(ChatManager.getInstance().getCurrentUser())) {
            var1 = this.getTo();
        } else {
            var1 = this.getFrom();
        }

        return var1;
    }

    void setError(int var1) {
        this.error = var1;
    }

    public int getError() {
        return this.error;
    }

    public static enum ChatType {
        Chat, //单聊
        GroupChat, //群聊
        ChatRoom; //聊天室

        private ChatType() {
        }
    }

    public static enum Direct {
        SEND, // 发送方
        RECEIVE; // 接收方

        private Direct() {
        }
    }

    public static enum Status {
        SUCCESS,
        FAIL,
        INPROGRESS, // 处理中
        CREATE;

        private Status() {
        }
    }

    public static enum Type {
        TXT,
        IMAGE,
        VIDEO,
        LOCATION,
        VOICE,
        FILE,
        CMD;

        private Type() {
        }
    }
}

