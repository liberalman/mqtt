package com.seaofheart.app;

import java.util.Map;

import android.content.Intent;
import android.content.IntentFilter;

import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.applib.controller.HXSDKHelper;
import com.seaofheart.applib.model.HXSDKModel;
import com.seaofheart.app.CallBack;
import com.seaofheart.app.chat.ChatManager;
import com.seaofheart.app.chat.Message;
import com.seaofheart.app.chat.Message.ChatType;
import com.seaofheart.app.chat.Message.Type;
import com.seaofheart.app.chat.OnMessageNotifyListener;
import com.seaofheart.app.chat.OnNotificationClickListener;
import com.seaofheart.app.domain.User;
import com.seaofheart.app.fx.ChatActivity;
import com.seaofheart.app.fx.MainActivity;
import com.seaofheart.app.fx.others.TopUser;
import com.seaofheart.app.receiver.VoiceCallReceiver;
import com.seaofheart.app.utils.CommonUtils;

/**
 * Demo UI HX SDK helper class which subclass HXSDKHelper
 * 
 * @author easemob
 * 
 */
public class DemoHXSDKHelper extends HXSDKHelper {

    /**
     * contact list in cache
     */
    private Map<String, User> contactList;
    private Map<String, TopUser> topUserList;

    @Override
    protected void initHXOptions() {
        super.initHXOptions();
        // you can also get EMChatOptions to set related SDK options
        // EMChatOptions options = ChatManager.getInstance().getChatOptions();
    }

    @Override
    protected OnMessageNotifyListener getMessageNotifyListener() {
        // 取消注释，app在后台，有新消息来时，状态栏的消息提示换成自己写的
        return new OnMessageNotifyListener() {

            @Override
            public String onNewMessageNotify(Message message) {
                // 设置状态栏的消息提示，可以根据message的类型做相应提示
                String ticker = CommonUtils.getMessageDigest(message, appContext);
                if (message.getType() == ProtocolMessage.TYPE.TXT)
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
                String nick = message.getFrom();
                User user = MYApplication.getInstance().getContactList()
                        .get(nick);
                if (user != null && user.getNick() != null) {
                    nick = user.getNick();
                }

                return nick + ": " + ticker;
            }

            @Override
            public String onLatestMessageNotify(Message message,
                    int fromUsersNum, int messageNum) {

                return fromUsersNum + "个好友，发来了" + messageNum + "条消息";
            }

            @Override
            public String onSetNotificationTitle(Message message) {
                // 修改标题,这里使用默认
                return null;
            }

            @Override
            public int onSetSmallIcon(Message message) {
                // 设置小图标
                return 0;
            }
        };
    }

    @Override
    protected OnNotificationClickListener getNotificationClickListener() {
        return new OnNotificationClickListener() {

            @Override
            public Intent onNotificationClick(Message message) {
                Intent intent = new Intent(appContext, ChatActivity.class);
                ProtocolMessage.CHAT_TYPE chatType = message.getChatType();
                if (chatType == ProtocolMessage.CHAT_TYPE.CHAT_SINGLE) { // 单聊信息
                    intent.putExtra("userId", message.getFrom());
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                } else { // 群聊信息
                         // message.getTo()为群聊id
                    intent.putExtra("groupId", message.getTo());
                    intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                }
                return intent;
            }
        };
    }

    @Override
    protected void onConnectionConflict() {
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("conflict", true);
        appContext.startActivity(intent);
    }

    @Override
    protected void onCurrentAccountRemoved() {
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_REMOVED, true);
        appContext.startActivity(intent);
    }

    @Override
    protected void initListener() {
        super.initListener();
        IntentFilter callFilter = new IntentFilter(ChatManager.getInstance()
                .getIncomingVoiceCallBroadcastAction());
        appContext.registerReceiver(new VoiceCallReceiver(), callFilter);
    }

    @Override
    protected HXSDKModel createModel() {
        return new DemoHXSDKModel(appContext);
    }

    /**
     * get demo HX SDK Model
     */
    public DemoHXSDKModel getModel() {
        return (DemoHXSDKModel) hxModel;
    }

    /**
     * 获取内存中好友user list
     * 
     * @return
     */
    public Map<String, User> getContactList() {
        if (getHXId() != null && contactList == null) {
            contactList = ((DemoHXSDKModel) getModel()).getContactList();
        }

        return contactList;
    }

    /**
     * 获取内存中置顶好友 t
     * 
     * @return
     */
    public Map<String, TopUser> getTopUserList() {
        if (getHXId() != null && topUserList == null) {
            topUserList = ((DemoHXSDKModel) getModel()).getTopUserList();
        }

        return topUserList;
    }

    /**
     * 设置置顶好友到内存中
     * 
     * @param topUserList
     */
    public void setTopUserList(Map<String, TopUser> topUserList) {
        this.topUserList = topUserList;
    }

    /**
     * 设置好友user list到内存中
     * 
     * @param contactList
     */
    public void setContactList(Map<String, User> contactList) {
        this.contactList = contactList;
    }

    @Override
    public void logout(final CallBack callback) {
        super.logout(new CallBack() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                setContactList(null);
                getModel().closeDB();
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

        });
    }

}
