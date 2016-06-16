package com.seaofheart.app.fx;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.seaofheart.app.CallBack;
import com.seaofheart.app.Constant;
import com.seaofheart.app.MYApplication;
import com.seaofheart.app.R;
import com.seaofheart.app.activity.BaseActivity;
import com.seaofheart.app.activity.FXAlertDialog;
import com.seaofheart.app.chat.ChatManager;
import com.seaofheart.app.chat.CmdMessageBody;
import com.seaofheart.app.chat.Message;
import com.seaofheart.app.chat.protocol.ProtocolMessage;
import com.seaofheart.app.fx.others.LocalUserInfo;

public class AddFriendsFinalActivity extends BaseActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriends_final);
                
        final String hxid =this.getIntent().getStringExtra("hxid");
        TextView  tv_send= (TextView) this.findViewById(R.id.tv_send);
        final EditText et_reason= (EditText) this.findViewById(R.id.et_reason);
        
        tv_send.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                addContact(hxid,et_reason.getText().toString().trim());
            }
            
        });
    }
    
    /**
     * 添加contact
     * 
     * @param glufine_id
     */
    @SuppressLint("ShowToast")
    public void addContact(final String glufine_id,final String myreason) {
        if (glufine_id == null || glufine_id.equals("")) {
            return;
        }

        if (MYApplication.getInstance().getUserName().equals(glufine_id)) {
            startActivity(new Intent(this, FXAlertDialog.class).putExtra("msg",
                    "不能添加自己"));
            return;
        }

        if (MYApplication.getInstance().getContactList()
                .containsKey(glufine_id)) {
            startActivity(new Intent(this, FXAlertDialog.class).putExtra("msg",
                    "此用户已是你的好友"));
            return;
        }
        
        
        
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在发送请求...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        
        
        
        //支持单聊和群聊，默认单聊，如果是群聊添加下面这行
      //  cmdMsg.setChatType(ChatType.GroupChat);
        Message cmdMsg = Message.createSendMessage(ProtocolMessage.TYPE.CMD);
        String action =Constant.CMD_ADD_FRIEND;//action可以自定义，在广播接收时可以收到
        CmdMessageBody cmdBody=new CmdMessageBody(action);
        
        String name = LocalUserInfo.getInstance(
                AddFriendsFinalActivity.this).getUserInfo("nick");
        String avatar = LocalUserInfo.getInstance(
                AddFriendsFinalActivity.this).getUserInfo("avatar");
        long time = System.currentTimeMillis();
        String myreason_temp=myreason;
        if(myreason==null||myreason.equals("")){
            myreason_temp = "请求加你为好友";
        }
        String reason = name + "66split88" + avatar + "66split88"
                + String.valueOf(time)+"66split88"+myreason_temp;

        
        
        cmdMsg.setReceipt(glufine_id);
        cmdMsg.setAttribute("reason", reason);//支持自定义扩展
        cmdMsg.addBody(cmdBody); 
        ChatManager.getInstance().sendMessage(cmdMsg, new CallBack(){

          @Override
          public void onError(int arg0, final String arg1) {
              runOnUiThread(new Runnable() {
                  public void run() {
                      progressDialog.dismiss();
                      Toast.makeText(getApplicationContext(),
                              "请求添加好友失败:" + arg1, Toast.LENGTH_SHORT ).show();
                  }
              });              
          }

          @Override
          public void onProgress(int arg0, String arg1) {
               
          }

          @Override
          public void onSuccess() {
              runOnUiThread(new Runnable() {
                  @SuppressLint("ShowToast")
                  public void run() {
                      progressDialog.dismiss();
                      Toast.makeText(getApplicationContext(),
                              "发送请求成功,等待对方验证", Toast.LENGTH_SHORT).show();
                      
                      finish();
                  }
              });
          }
            
        });
    }
    
    public void back(View view ){
        
        finish();
    }
}
