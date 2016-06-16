package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.seaofheart.app.chat.core.x;
import com.seaofheart.app.chat.core.z;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.util.EMLog;

import org.jivesoftware.smackx.packet.DelayInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class MessageEncoder {
    private static final String TAG = "encoder";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_TO = "to";
    public static final String ATTR_FROM = "from";
    public static final String ATTR_MSG = "msg";
    public static final String ATTR_URL = "url";
    public static final String ATTR_LOCALURL = "localurl";
    public static final String ATTR_THUMB_LOCALURL = "thumblocalurl";
    public static final String ATTR_FILENAME = "filename";
    public static final String ATTR_THUMBNAIL = "thumb";
    public static final String ATTR_SECRET = "secret";
    public static final String ATTR_SIZE = "size";
    public static final String ATTR_IMG_WIDTH = "width";
    public static final String ATTR_IMG_HEIGHT = "height";
    public static final String ATTR_THUMBNAIL_SECRET = "thumb_secret";
    public static final String ATTR_LENGTH = "length";
    public static final String ATTR_ADDRESS = "addr";
    public static final String ATTR_LATITUDE = "lat";
    public static final String ATTR_LONGITUDE = "lng";
    public static final String ATTR_ACTION = "action";
    public static final String ATTR_PARAM = "param";
    public static final String ATTR_FILE_LENGTH = "file_length";
    public static final String ATTR_EXT = "ext";
    private static final String ATTR_TYPE_TXT = "txt";
    private static final String ATTR_TYPE_IMG = "img";
    private static final String ATTR_TYPE_VOICE = "audio";
    private static final String ATTR_TYPE_VIDEO = "video";
    private static final String ATTR_TYPE_LOCATION = "loc";
    private static final String ATTR_TYPE_CMD = "cmd";
    private static final String ATTR_TYPE_file = "file";

    public MessageEncoder() {
    }

    public static String getJSONMsg(Message var0, boolean var1) {
        StringBuffer var2 = new StringBuffer();
        var2.append("{");
        var2.append("\"from\":\"" + var0.from.username + "\",");
        var2.append("\"to\":\"" + var0.to.username + "\",");
        var2.append("\"bodies\":[{");
        if(var0.type == ProtocolMessage.TYPE.TXT) {
            addTxtBody(var2, var0);
        } else if(var0.type == ProtocolMessage.TYPE.IMAGE) {
            addImageBody(var2, var0, var1);
        } else if(var0.type == ProtocolMessage.TYPE.VOICE) {
            addVoiceBody(var2, var0, var1);
        } else if(var0.type == ProtocolMessage.TYPE.LOCATION) {
            addLocationBody(var2, var0);
        } else if(var0.type == ProtocolMessage.TYPE.CMD) {
            addCmdBody(var2, var0);
        } else if(var0.type == ProtocolMessage.TYPE.VIDEO) {
            addVideoBody(var2, var0, var1);
        } else if(var0.type == ProtocolMessage.TYPE.FILE) {
            addFileBody(var2, var0, var1);
        }

        var2.append("}]");
        if(var0.attributes != null) {
            addExtAttr(var2, var0);
        }

        var2.append("}");
        var2.toString();
        return var2.toString();
    }

    private static void addExtAttr(StringBuffer var0, Message var1) {
        var0.append(",");
        var0.append("\"ext\":{");
        int var2 = 1;
        Hashtable var3 = var1.attributes;
        synchronized(var1.attributes) {
            Iterator var5 = var1.attributes.keySet().iterator();

            while(true) {
                if(!var5.hasNext()) {
                    break;
                }

                String var4 = (String)var5.next();
                var0.append("\"" + var4 + "\":");
                Object var6 = var1.attributes.get(var4);
                if(var6 instanceof JSONObject) {
                    var0.append(var6.toString());
                } else if(var6 instanceof JSONArray) {
                    var0.append(var6.toString());
                } else if(var6 instanceof String) {
                    String var7 = (String)var6;
                    if((!var7.startsWith("{") || !var7.endsWith("}") || !var7.contains(":")) && (!var7.startsWith("[{") || !var7.endsWith("}]") || !var7.contains(":"))) {
                        var0.append("\"" + var6 + "\"");
                    } else {
                        var0.append(var6);
                    }
                } else if(var6 instanceof Boolean) {
                    Boolean var9 = (Boolean)var6;
                    if(var9.booleanValue()) {
                        var0.append("true");
                    } else {
                        var0.append("false");
                    }
                } else {
                    Integer var10 = (Integer)var6;
                    var0.append(var10.toString());
                }

                if(var2 < var1.attributes.size()) {
                    var0.append(",");
                }

                ++var2;
            }
        }

        var0.append("}");
    }

    private static void addImageBody(StringBuffer var0, Message var1, boolean var2) {
        var0.append("\"type\":\"img\",");
        ImageMessageBody var3 = (ImageMessageBody) var1.body;
        var0.append("\"url\":\"" + var3.remoteUrl + "\",");
        if(var2) {
            var0.append("\"localurl\":\"" + var3.localUrl + "\",");
        }

        var0.append("\"filename\":\"" + var3.fileName + "\",");
        if(var3.thumbnailUrl != null) {
            var0.append("\"thumb\":\"" + var3.thumbnailUrl + "\",");
        }

        var0.append("\"secret\":\"" + var3.secret + "\",");
        var0.append("\"size\":{\"width\":" + var3.width + ",\"" + "height" + "\":" + var3.height + "}");
        if(var3.thumbnailSecret != null) {
            var0.append(",\"thumb_secret\":\"" + var3.thumbnailSecret + "\"");
        }

    }

    private static void addVideoBody(StringBuffer var0, Message var1, boolean var2) {
        var0.append("\"type\":\"video\",");
        VideoMessageBody var3 = (VideoMessageBody)var1.body;
        var0.append("\"url\":\"" + var3.remoteUrl + "\",");
        if(var2) {
            var0.append("\"localurl\":\"" + var3.localUrl + "\",");
            var0.append("\"thumblocalurl\":\"" + var3.localThumb + "\",");
        }

        var0.append("\"filename\":\"" + var3.fileName + "\",");
        var0.append("\"thumb\":\"" + var3.thumbnailUrl + "\",");
        var0.append("\"length\":" + var3.length + ",");
        var0.append("\"file_length\":" + var3.file_length + ",");
        var0.append("\"secret\":\"" + var3.secret + "\"");
        if(var3.thumbnailSecret != null) {
            var0.append(",\"thumb_secret\":\"" + var3.thumbnailSecret + "\"");
        }

    }

    private static void addTxtBody(StringBuffer var0, Message var1) {
        var0.append("\"type\":\"txt\",");
        TextMessageBody var2 = (TextMessageBody)var1.body;
        String var3 = var2.message;
        var3 = JSONObject.quote(var3);
        if(var3.startsWith("{") && var3.endsWith("}") || var3.startsWith("[") && var3.endsWith("]")) {
            var3 = var3.replaceAll("\"", "%22");
            var0.append("\"msg\":\"" + var3 + "\"");
        }

        var0.append("\"msg\":" + var3);
    }

    private static void addCmdBody(StringBuffer var0, Message var1) {
        var0.append("\"type\":\"cmd\",");
        CmdMessageBody var2 = (CmdMessageBody)var1.body;
        var0.append("\"action\":\"" + var2.action + "\",");
        var0.append("\"param\":[");
        if(var2.params != null && var2.params.size() != 0) {
            Iterator var3 = var2.params.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry var4 = (Map.Entry)var3.next();
                var0.append("{\"");
                var0.append(var4.getKey());
                var0.append("\":\"");
                var0.append(var4.getValue());
                var0.append("\"},");
            }

            var0.deleteCharAt(var0.lastIndexOf(","));
        }

        var0.append("]");
    }

    private static void addVoiceBody(StringBuffer var0, Message var1, boolean var2) {
        var0.append("\"type\":\"audio\",");
        VoiceMessageBody var3 = (VoiceMessageBody)var1.body;
        var0.append("\"url\":\"" + var3.remoteUrl + "\",");
        if(var2) {
            var0.append("\"localurl\":\"" + var3.localUrl + "\",");
        }

        var0.append("\"filename\":\"" + var3.fileName + "\",");
        var0.append("\"length\":" + var3.length + ",");
        var0.append("\"secret\":\"" + var3.secret + "\"");
    }

    private static void addFileBody(StringBuffer var0, Message var1, boolean var2) {
        var0.append("\"type\":\"file\",");
        NormalFileMessageBody var3 = (NormalFileMessageBody)var1.body;
        var0.append("\"url\":\"" + var3.remoteUrl + "\",");
        if(var2) {
            var0.append("\"localurl\":\"" + var3.localUrl + "\",");
        }

        var0.append("\"filename\":\"" + var3.fileName + "\",");
        var0.append("\"file_length\":" + var3.fileSize + ",");
        var0.append("\"secret\":\"" + var3.secret + "\"");
    }

    private static void addLocationBody(StringBuffer var0, Message var1) {
        var0.append("\"type\":\"loc\",");
        LocationMessageBody var2 = (LocationMessageBody)var1.body;
        var0.append("\"addr\":\"" + var2.address + "\",");
        var0.append("\"lat\":" + var2.latitude + ",");
        var0.append("\"lng\":" + var2.longitude);
    }

    static Message parseXmppMsg(org.jivesoftware.smack.packet.Message var0) {
        String var1;
        if(var0.getExtension("encrypt", "jabber:client") != null) {
            EMLog.d("encoder", "it is encrypted message, decripting");

            try {
                var1 = var0.getBody();
                String var2 = ContactManager.getUserNameFromEid(var0.getFrom());
                String var3 = EncryptUtils.decryptMessage(var1, var2);
                Iterator var5 = var0.getBodies().iterator();

                while(var5.hasNext()) {
                    org.jivesoftware.smack.packet.Message.Body var4 = (org.jivesoftware.smack.packet.Message.Body)var5.next();
                    var0.removeBody(var4);
                }

                var0.setBody(var3);
            } catch (Exception var7) {
                var7.printStackTrace();
            }
        }

        var1 = var0.getBody();
        if(var1.startsWith("{") && var1.endsWith("}")) {
            Message var8 = getMsgFromJson(var1);
            if(var8 == null) {
                EMLog.e("encoder", "wrong message format:" + var0.toXML());
                return null;
            } else {
                try {
                    DelayInfo var9 = (DelayInfo)var0.getExtension("delay", "urn:xmpp:delay");
                    if(var9 != null) {
                        var8.msgTime = var9.getStamp().getTime();
                        var8.offline = true;
                    } else {
                        z var11 = (z)var0.getExtension("ts", "urn:xmpp:timestamp");
                        if(var11 != null) {
                            var8.msgTime = var11.a().getTime();
                        }
                    }
                } catch (Exception var6) {
                    ;
                }

                x var10 = new x();
                var10 = (x)var0.getExtension(var10.getElementName(), var10.getNamespace());
                if(var10 != null && var10.a() == x.a.a) {
                    var8.setChatType(ProtocolMessage.CHAT_TYPE.CHAT_ROOM);
                }

                var8.direct = ProtocolMessage.DIRECT.RECEIVE;
                var8.msgId = var0.getPacketID();
                var8.status = ProtocolMessage.STATUS.CREATE;
                var8.unread = true;
                return var8;
            }
        } else {
            EMLog.d("encoder", "msg not in json format, ignore");
            return null;
        }
    }

    public static Message getMsgFromJson(String var0) {
        try {
            JSONObject var1 = new JSONObject(var0);
            Contact var2 = new Contact(var1.getString("from"));
            Contact var3 = new Contact(var1.getString("to"));
            JSONArray var4 = var1.getJSONArray("bodies");
            if(var4.length() < 1) {
                EMLog.d("encoder", "wrong msg without body");
                return null;
            } else {
                JSONObject var5 = var4.getJSONObject(0);
                String var6 = var5.getString("type");
                Message var7 = null;
                String var8;
                String var9;
                String var21;
                if(var6.equals("txt")) {
                    var7 = new Message(ProtocolMessage.TYPE.TXT);
                    var8 = var5.getString("msg");
                    var9 = var8.replaceAll("%22", "\"");
                    TextMessageBody var10 = new TextMessageBody(var9);
                    var7.addBody(var10);
                } else if(var6.equals("img")) {
                    var7 = new Message(ProtocolMessage.TYPE.IMAGE);
                    var8 = var5.getString("url");
                    var9 = var5.getString("filename");
                    var21 = var8;
                    if(var5.has("thumb")) {
                        var21 = var5.getString("thumb");
                    }

                    ImageMessageBody var11 = new ImageMessageBody(var9, var8, var21);
                    if(var5.has("localurl")) {
                        var11.localUrl = var5.getString("localurl");
                    }

                    if(var5.has("secret")) {
                        var11.setSecret(var5.getString("secret"));
                    }

                    if(var5.has("thumb_secret")) {
                        var11.setThumbnailSecret(var5.getString("thumb_secret"));
                    }

                    if(var5.has("size")) {
                        JSONObject var12 = var5.getJSONObject("size");
                        var11.width = var12.getInt("width");
                        var11.height = var12.getInt("height");
                    }

                    var7.addBody(var11);
                } else if(var6.equals("file")) {
                    var7 = new Message(ProtocolMessage.TYPE.FILE);
                    var8 = var5.getString("url");
                    var9 = var5.getString("filename");
                    NormalFileMessageBody var18 = new NormalFileMessageBody(var9, var8);
                    var18.fileSize = (long)Integer.parseInt(var5.getString("file_length"));
                    if(var5.has("localurl")) {
                        var18.localUrl = var5.getString("localurl");
                    }

                    if(var5.has("secret")) {
                        var18.setSecret(var5.getString("secret"));
                    }

                    var7.addBody(var18);
                } else if(var6.equals("video")) {
                    var7 = new Message(ProtocolMessage.TYPE.VIDEO);
                    var8 = var5.getString("url");
                    var9 = var5.getString("filename");
                    var21 = var5.getString("thumb");
                    int var17 = var5.getInt("length");
                    VideoMessageBody var23 = new VideoMessageBody(var9, var8, var21, var17);
                    if(var5.has("localurl")) {
                        var23.localUrl = var5.getString("localurl");
                    }

                    if(var5.has("file_length")) {
                        var23.file_length = var5.getLong("file_length");
                    }

                    if(var5.has("thumblocalurl")) {
                        var23.localThumb = var5.getString("thumblocalurl");
                    }

                    if(var5.has("secret")) {
                        var23.setSecret(var5.getString("secret"));
                    }

                    if(var5.has("thumb_secret")) {
                        var23.setThumbnailSecret(var5.getString("thumb_secret"));
                    }

                    var7.addBody(var23);
                } else {
                    int var29;
                    if(var6.equals("audio")) {
                        var7 = new Message(ProtocolMessage.TYPE.VOICE);
                        var8 = var5.getString("url");
                        var9 = var5.getString("filename");
                        var29 = var5.getInt("length");
                        VoiceMessageBody var27 = new VoiceMessageBody(var9, var8, var29);
                        if(var5.has("localurl")) {
                            var27.localUrl = var5.getString("localurl");
                        }

                        if(var5.has("secret")) {
                            var27.setSecret(var5.getString("secret"));
                        }

                        var7.addBody(var27);
                    } else if(var6.equals("loc")) {
                        var7 = new Message(ProtocolMessage.TYPE.LOCATION);
                        var8 = var5.getString("addr");
                        double var22 = var5.getDouble("lat");
                        double var30 = var5.getDouble("lng");
                        LocationMessageBody var13 = new LocationMessageBody(var8, var22, var30);
                        var7.addBody(var13);
                    } else if(var6.equals("cmd")) {
                        var7 = new Message(ProtocolMessage.TYPE.CMD);
                        HashMap var15 = new HashMap();
                        if(var5.has("param")) {
                            JSONArray var16 = var5.getJSONArray("param");

                            for(var29 = 0; var29 < var16.length(); ++var29) {
                                JSONObject var24 = var16.getJSONObject(var29);
                                String var26 = (String)var24.keys().next();
                                String var31 = (String)var24.get(var26);
                                var15.put(var26, var31);
                            }
                        }

                        CmdMessageBody var19 = new CmdMessageBody(var5.getString("action"), var15);
                        var7.addBody(var19);
                    }
                }

                if(var7 != null) {
                    var7.from = var2;
                    var7.to = var3;
                }

                if(var1.has("ext")) {
                    JSONObject var20 = var1.getJSONObject("ext");
                    Iterator var28 = var20.keys();

                    while(var28.hasNext()) {
                        var21 = (String)var28.next();
                        Object var25 = var20.get(var21);
                        if(var25 instanceof String) {
                            var7.setAttribute(var21, (String)var25);
                        } else if(var25 instanceof Integer) {
                            var7.setAttribute(var21, ((Integer)var25).intValue());
                        } else if(var25 instanceof Boolean) {
                            var7.setAttribute(var21, ((Boolean)var25).booleanValue());
                        } else if(var25 instanceof JSONObject) {
                            var7.setAttribute(var21, (JSONObject)var25);
                        } else if(var25 instanceof JSONArray) {
                            var7.setAttribute(var21, (JSONArray)var25);
                        } else {
                            EMLog.e("msg", "unknow additonal msg attr:" + var25.getClass().getName());
                        }
                    }
                }

                return var7;
            }
        } catch (Exception var14) {
            var14.printStackTrace();
            return null;
        }
    }
}
