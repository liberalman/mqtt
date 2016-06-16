package com.seaofheart.app.comments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.facebook.drawee.view.SimpleDraweeView;
import com.seaofheart.app.Constant;
import com.seaofheart.app.MYApplication;
import com.seaofheart.app.R;
import com.seaofheart.app.activity.BaseActivity;
import com.seaofheart.app.comments.AddPopWindow.ClickCallBack;
import com.seaofheart.app.comments.SocialApiTask.DataCallBack;
import com.seaofheart.app.domain.User;
import com.seaofheart.app.fx.others.LocalUserInfo;

public class SocialDetailActivity extends BaseActivity {
    public RelativeLayout re_edittext;
    private String myuserID;
    EditText et_comment;
    JSONObject json = null;
    Button btn_send;
    TextView tv_comment_tag;
    JSONArray jsons_tag;
    View view_tag;
    int goodSize_tag;
    String sID_tag;

    private String myAvatar;
    private String myNick;
    private Context context;

    // private TextView tv_nick;
    // SimpleDraweeView sdv_image;
    protected void onCreate(Bundle arg0) {

        super.onCreate(arg0);
        setContentView(R.layout.activity_detail);
        myuserID = MYApplication.getInstance().getUserName();
        myNick = LocalUserInfo.getInstance(context).getUserInfo("nick");
        myAvatar = LocalUserInfo.getInstance(context).getUserInfo("avatar");
        String jsonStr = this.getIntent().getStringExtra("json");
        if (jsonStr == null) {

            finish();
            return;
        }
        json = JSONObject.parseObject(jsonStr);
        initView();

    }

    private void initView() {
        et_comment = (EditText) findViewById(R.id.et_comment);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String comment = et_comment.getText().toString().trim();
                if (TextUtils.isEmpty(comment)) {
                    Toast.makeText(SocialDetailActivity.this, "请输入评论",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                submitComment();
                et_comment.setText("");
                closeInputMethod();

            }

        });
        myuserID = MYApplication.getInstance().getUserName();
        // 底部评论输入框
        re_edittext = (RelativeLayout) findViewById(R.id.re_edittext);
        TextView tv_nick = (TextView) findViewById(R.id.tv_nick);
        TextView tv_time = (TextView) findViewById(R.id.tv_time);

        SimpleDraweeView iv_avatar = (SimpleDraweeView) findViewById(R.id.sdv_image);
        SimpleDraweeView image_1 = (SimpleDraweeView) findViewById(R.id.image_1);
        SimpleDraweeView image_2 = (SimpleDraweeView) findViewById(R.id.image_2);
        SimpleDraweeView image_3 = (SimpleDraweeView) findViewById(R.id.image_3);
        SimpleDraweeView image_4 = (SimpleDraweeView) this
                .findViewById(R.id.image_4);
        SimpleDraweeView image_5 = (SimpleDraweeView) this
                .findViewById(R.id.image_5);
        SimpleDraweeView image_6 = (SimpleDraweeView) this
                .findViewById(R.id.image_6);
        SimpleDraweeView image_7 = (SimpleDraweeView) this
                .findViewById(R.id.image_7);
        SimpleDraweeView image_8 = (SimpleDraweeView) this
                .findViewById(R.id.image_8);
        SimpleDraweeView image_9 = (SimpleDraweeView) this
                .findViewById(R.id.image_9);
        LinearLayout ll_one = (LinearLayout) this.findViewById(R.id.ll_one);
        LinearLayout ll_two = (LinearLayout) this.findViewById(R.id.ll_two);
        LinearLayout ll_three = (LinearLayout) this.findViewById(R.id.ll_three);

        TextView tv_content = (TextView) this.findViewById(R.id.tv_content);
        TextView tv_location = (TextView) this.findViewById(R.id.tv_location);
        ImageView iv_pop = (ImageView) this.findViewById(R.id.iv_pop);

        TextView tv_goodmembers = (TextView) this
                .findViewById(R.id.tv_goodmembers);
        LinearLayout ll_goodmembers = (LinearLayout) this
                .findViewById(R.id.ll_goodmembers);
        TextView tv_commentmembers = (TextView) this
                .findViewById(R.id.tv_commentmembers);
        tv_comment_tag = tv_commentmembers;
        final View view_pop = (View) this.findViewById(R.id.view_pop);
        TextView tv_delete = (TextView) this.findViewById(R.id.tv_delete);
        view_tag = view_pop;
        final String userID = json.getString("userID");
        String content = json.getString("content");
        String imageStr = json.getString("imageStr");
        String location = json.getString("location");
        final String sID = json.getString("sID");
        sID_tag = sID;
        // String token = json.getString("token");
        String rel_time = json.getString("time");
        // 设置删除键
        if (userID.equals(myuserID)) {

            tv_delete.setVisibility(View.VISIBLE);
            tv_delete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    showPhotoDialog(sID);
                    // users.remove(position - 1);
                    // this.notifyDataSetChanged();
                }

            });
        } else {
            tv_delete.setVisibility(View.GONE);
        }

        // 设置昵称。暂时用id代替

        String nick_temp = userID;
        String avatar_temp = userID;
        if (userID.equals(myuserID)) {

            nick_temp = myNick;
            avatar_temp = myAvatar;

        } else {
            User user = MYApplication.getInstance().getContactList()
                    .get(userID);
            if (user != null && user.getNick() != null
                    & user.getAvatar() != null) {
                nick_temp = user.getNick();
                avatar_temp = user.getAvatar();

            }
        }
        tv_nick.setText(nick_temp);

        tv_nick.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });

        // 设置头像.....
        iv_avatar.setImageURI(Uri.parse(Constant.URL_Avatar + avatar_temp));
        // 设置文章中的图片
        Log.e("imageStr--->>", imageStr);
        if (!imageStr.equals("0")) {
            String[] images = imageStr.split("split");
            int imNumb = images.length;
            image_1.setVisibility(View.VISIBLE);
            image_1.setImageURI(Uri
                    .parse(Constant.URL_SOCIAL_PHOTO + images[0]));
            image_1.setOnClickListener(new ImageListener(images, 0));

            Log.e("imNumb--->>", String.valueOf(imNumb));
            // 四张图的时间情况比较特殊
            if (imNumb == 4) {
                image_2.setVisibility(View.VISIBLE);
                image_2.setImageURI(Uri.parse(Constant.URL_SOCIAL_PHOTO
                        + images[1]));
                image_2.setOnClickListener(new ImageListener(images, 1));
                image_4.setVisibility(View.VISIBLE);
                image_4.setImageURI(Uri.parse(Constant.URL_SOCIAL_PHOTO
                        + images[2]));
                image_4.setOnClickListener(new ImageListener(images, 2));
                image_5.setVisibility(View.VISIBLE);
                image_5.setImageURI(Uri.parse(Constant.URL_SOCIAL_PHOTO
                        + images[3]));
                image_5.setOnClickListener(new ImageListener(images, 3));
            } else {
                if (imNumb > 1) {
                    image_2.setVisibility(View.VISIBLE);
                    image_2.setImageURI(Uri.parse(Constant.URL_SOCIAL_PHOTO
                            + images[1]));
                    image_2.setOnClickListener(new ImageListener(images, 1));
                    if (imNumb > 2) {
                        image_3.setVisibility(View.VISIBLE);
                        image_3.setImageURI(Uri.parse(Constant.URL_SOCIAL_PHOTO
                                + images[2]));
                        image_3.setOnClickListener(new ImageListener(images, 2));
                        if (imNumb > 3) {
                            image_4.setVisibility(View.VISIBLE);
                            image_4.setImageURI(Uri
                                    .parse(Constant.URL_SOCIAL_PHOTO
                                            + images[3]));
                            image_4.setOnClickListener(new ImageListener(
                                    images, 3));
                            if (imNumb > 4) {
                                image_5.setVisibility(View.VISIBLE);
                                image_5.setImageURI(Uri
                                        .parse(Constant.URL_SOCIAL_PHOTO
                                                + images[4]));
                                image_5.setOnClickListener(new ImageListener(
                                        images, 4));
                                if (imNumb > 5) {
                                    image_6.setVisibility(View.VISIBLE);
                                    image_6.setImageURI(Uri
                                            .parse(Constant.URL_SOCIAL_PHOTO
                                                    + images[5]));
                                    image_6.setOnClickListener(new ImageListener(
                                            images, 5));
                                    if (imNumb > 6) {
                                        image_7.setVisibility(View.VISIBLE);
                                        image_7.setImageURI(Uri
                                                .parse(Constant.URL_SOCIAL_PHOTO
                                                        + images[6]));
                                        image_7.setOnClickListener(new ImageListener(
                                                images, 6));
                                        if (imNumb > 7) {
                                            image_8.setVisibility(View.VISIBLE);
                                            image_8.setImageURI(Uri
                                                    .parse(Constant.URL_SOCIAL_PHOTO
                                                            + images[7]));
                                            image_8.setOnClickListener(new ImageListener(
                                                    images, 7));
                                            if (imNumb > 8) {
                                                image_9.setVisibility(View.VISIBLE);
                                                image_9.setImageURI(Uri
                                                        .parse(Constant.URL_SOCIAL_PHOTO
                                                                + images[8]));
                                                image_9.setOnClickListener(new ImageListener(
                                                        images, 8));

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // 显示位置
        if (location != null && !location.equals("0")) {
            tv_location.setVisibility(View.VISIBLE);
            tv_location.setText(location);
        }
        // 显示文章内容
        // .setText(content);
        setUrlTextView(content, tv_content);
        final ImageView iv_temp = iv_pop;
        final LinearLayout ll_goodmembers_temp = ll_goodmembers;

        // 点赞评论的数据
        final JSONArray goodArray = json.getJSONArray("good");
        final JSONArray commentArray = json.getJSONArray("comment");
        jsons_tag = commentArray;
        goodSize_tag = goodArray.size();
        // 点赞

        setGoodTextClick(tv_goodmembers, goodArray, ll_goodmembers_temp,
                view_pop, commentArray.size());

        boolean is_good_temp = true;
        for (int i = 0; i < goodArray.size(); i++) {
            JSONObject json_good = goodArray.getJSONObject(i);
            if (json_good.getString("userID").equals(myuserID)) {
                is_good_temp = false;
            }
        }
        // 评论

        if (commentArray != null && commentArray.size() != 0) {
            tv_commentmembers.setVisibility(View.VISIBLE);
            setCommentTextClick(tv_commentmembers, commentArray, view_pop,
                    goodArray.size());

        }

        final boolean is_good = is_good_temp;
        String goodStr = "赞";
        if (!is_good) {
            goodStr = "取消";

        }
        iv_temp.setTag(goodStr);

        final TextView tv_commentmembers_temp = tv_commentmembers;
        final TextView tv_good_temp = tv_goodmembers;
        iv_temp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AddPopWindow addPopWindow = new AddPopWindow(
                        SocialDetailActivity.this, iv_temp,
                        new ClickCallBack() {

                            @Override
                            public void clicked(int type) {
                                // 点击取消
                                if (type == 1) {
                                    if (((String) iv_temp.getTag()).equals("赞")) {
                                        setGood(sID, tv_good_temp, goodArray,
                                                ll_goodmembers_temp, view_pop,
                                                commentArray.size());
                                        iv_temp.setTag("取消");

                                    } else {
                                        cancelGood(sID, tv_good_temp,
                                                goodArray, ll_goodmembers_temp,
                                                view_pop, commentArray.size());
                                        iv_temp.setTag("赞");
                                    }

                                } else {
                                    // 点击评论
                                    showCommentEditText(sID,
                                            tv_commentmembers_temp,
                                            commentArray, view_pop,
                                            goodArray.size());
                                }
                            }

                        });
                addPopWindow.showPopupWindow(iv_temp);

            }
        });

        // 显示时间

        tv_time.setText(getTime(rel_time, MYApplication.getInstance().getTime()));
    }

    /**
     * 
     * 显示发表评论的输入框
     * 
     * 
     * 
     */

    public void showCommentEditText(final String sID,
            final TextView tv_comment, final JSONArray jsons, final View view,
            final int goodSize) {

        openInputMethod(re_edittext);

    }

    private void showPhotoDialog(final String sID) {
        final AlertDialog dlg = new AlertDialog.Builder(
                SocialDetailActivity.this).create();
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.dialog_social_delete);
        TextView tv_cancel = (TextView) window.findViewById(R.id.tv_cancel);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
                dlg.cancel();
            }
        });
        TextView tv_ok = (TextView) window.findViewById(R.id.tv_ok);
        tv_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // 更新服务器
                Map<String, String> map = new HashMap<String, String>();
                map.put("sID", sID);
                SocialApiTask task = new SocialApiTask(
                        SocialDetailActivity.this, Constant.URL_SOCIAL_DELETE,
                        map);
                task.getData(new DataCallBack() {

                    @Override
                    public void onDataCallBack(JSONObject data) {
                        // dialog.dismiss();
                        if (data == null) {

                            Log.e("hideCommentEditText()-->>>>",
                                    "hideCommentEditText()-----ERROR");
                            return;
                        }
                        int code = data.getInteger("code");
                        if (code == 1000) {

                            finish();
                            Log.e("hideCommentEditText()-->>>>",
                                    "hideCommentEditText()-----SUCCESS");
                        } else {
                            Log.e("hideCommentEditText()-->>>>",
                                    "hideCommentEditText()-----ERROR_2");

                        }
                    }
                });
                dlg.cancel();
            }
        });

    }

    private void setUrlTextView(String test_temp, TextView tv_content) {

        String test = test_temp;

        if ((test_temp != null)
                && (test_temp.contains("http://")
                        || test_temp.contains("https://") || test_temp
                            .contains("www."))) {
            int start = 0;
            while (test != null
                    && !(test.startsWith("http://")
                            || test.startsWith("https://") || test
                                .startsWith("www."))) {

                test = test.substring(1);
                start++;

            }
            int end = 0;

            for (int i = 0; i < test.length(); i++) {
                char item = test.charAt(i);
                if (isChinese(item) || item == ' ') {

                    break;
                }
                end = i;

            }

            String result = (String) test_temp
                    .substring(start, start + end + 1);
            // 可以检验是否有效连接，但是影响效率
            // if(result!=nullcheckURL(result)){
            //
            // }
            if (result != null) {

                SpannableStringBuilder ssb = new SpannableStringBuilder();
                ssb.append(test_temp);

                ssb.setSpan(new ContentURLSpan(result), start, start + end + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                tv_content.setText(ssb);
                tv_content.setMovementMethod(LinkMovementMethod.getInstance());
            }

        } else {
            tv_content.setText(test_temp);
        }

    }

    // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    private class ContentURLSpan extends ClickableSpan {
        private String url;

        public ContentURLSpan(String url) {
            this.url = url;

        }

        @Override
        public void updateDrawState(TextPaint ds) {

            ds.setUnderlineText(false); // 去掉下划线
        }

        @Override
        public void onClick(final View widget) {

            if (widget instanceof TextView) {
                ((TextView) widget).setHighlightColor(getResources().getColor(
                        android.R.color.darker_gray));
                new Handler().postDelayed(new Runnable() {

                    public void run() {

                        ((TextView) widget).setHighlightColor(getResources()
                                .getColor(android.R.color.transparent));

                    }

                }, 1000);

            }
            startActivity(new Intent(SocialDetailActivity.this,
                    MyWebViewActivity.class).putExtra("url", url));

        }

    }

    // 设置点赞的
    private void setGoodTextClick(TextView mTextView2, JSONArray data,
            LinearLayout ll_goodmembers, View view, int cSize) {
        if (data == null || data.size() == 0) {
            ll_goodmembers.setVisibility(View.GONE);
        } else {

            ll_goodmembers.setVisibility(View.VISIBLE);
        }
        if (cSize > 0 && data.size() > 0) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);

        }
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        int start = 0;
        for (int i = 0; i < data.size(); i++) {

            JSONObject json_good = data.getJSONObject(i);
            String userID_temp = json_good.getString("userID");
            String nick = userID_temp;

            if (userID_temp.equals(myuserID)) {
                nick = myNick;

            } else {

                User user = MYApplication.getInstance().getContactList()
                        .get(userID_temp);
                if (user != null) {

                    nick = user.getNick();

                }

            }

            if (i != (data.size() - 1) && data.size() > 1) {
                ssb.append(nick + ",");
            } else {
                ssb.append(nick);

            }

            ssb.setSpan(new TextViewURLSpan(nick, userID_temp, 0), start, start
                    + nick.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = ssb.length();
        }
        mTextView2.setText(ssb);
        mTextView2.setMovementMethod(LinkMovementMethod.getInstance());

        // SpannableStringBuilder newString = new SpannableStringBuilder();
        // SpannableString temp = (SpannableString) mTextView2.getText();
        // newString.append("000000");
        // newString.append(temp);
        // mTextView2.setText(newString);
    }

    private class TextViewURLSpan extends ClickableSpan {
        private String userID;
        // 0是点赞里面的名字。1是评论里面的名字；2是评论中的删除
        private int type = 0;
        private TextView ctextView;
        private JSONArray cjsons;
        private View view;
        private int goodSize;
        private String scID;
        private int postion;

        public TextViewURLSpan(String nick, String userID, int postion,
                String scID, int type, TextView ctextView, JSONArray cjsons,
                View view, int goodSize) {
            this.userID = userID;
            this.type = type;
            this.ctextView = ctextView;
            this.cjsons = cjsons;
            this.view = view;
            this.goodSize = goodSize;
            this.scID = scID;
            this.postion = postion;
        }

        public TextViewURLSpan(String nick, String userID, int type) {
            this.userID = userID;
            this.type = type;

        }

        @Override
        public void updateDrawState(TextPaint ds) {
            if (type != 2) {
                ds.setColor(getResources().getColor(R.color.text_color));

            }
            ds.setUnderlineText(false); // 去掉下划线
        }

        @Override
        public void onClick(final View widget) {

            if (widget instanceof TextView) {
                ((TextView) widget).setHighlightColor(getResources().getColor(
                        android.R.color.darker_gray));
                new Handler().postDelayed(new Runnable() {

                    public void run() {

                        ((TextView) widget).setHighlightColor(getResources()
                                .getColor(android.R.color.transparent));

                    }

                }, 1000);

            }

            if (type == 2) {
                showDeleteDialog(userID, postion, scID, type, ctextView,
                        cjsons, view, goodSize);

            } else {

                startActivity(new Intent(SocialDetailActivity.this,
                        SocialFriendActivity.class)
                        .putExtra("friendID", userID));
            }
        }

    }

    private void showDeleteDialog(final String userID, final int postion,
            final String scID, final int type, final TextView ctextView,
            final JSONArray cjsons, final View view, final int goodSize) {
        final AlertDialog dlg = new AlertDialog.Builder(
                SocialDetailActivity.this).create();
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.dialog_social_main);
        TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
        tv_paizhao.setText("复制");
        tv_paizhao.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
                ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(cjsons.getJSONObject(postion).getString("content")
                        .trim());

                // cmb.setPrimaryClip(ClipData clip)

                dlg.cancel();
            }
        });
        TextView tv_xiangce = (TextView) window.findViewById(R.id.tv_content2);
        tv_xiangce.setText("删除");
        tv_xiangce.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteComment(userID, postion, scID, type, ctextView, cjsons,
                        view, goodSize);

                dlg.cancel();
            }
        });

    }

    // 删除评论
    private void deleteComment(String userID, final int postion, String scID,
            int type, TextView ctextView, final JSONArray cjsons, View view,
            int goodSize) {

        if (scID == null) {
            scID = "LOCAL";
        }
        ;
        String tag = cjsons.getJSONObject(postion).getString("tag");
        if (tag == null) {
            tag = String.valueOf(System.currentTimeMillis());
        }
        // 更新UI
        cjsons.remove(postion);
        setCommentTextClick(ctextView, cjsons, view, goodSize);
        // 更新服务器
        Map<String, String> map = new HashMap<String, String>();
        map.put("scID", scID);
        map.put("userID", myuserID);
        map.put("tag", tag);
        SocialApiTask task = new SocialApiTask(SocialDetailActivity.this,
                Constant.URL_SOCIAL_DELETE_COMMENT, map);
        task.getData(new DataCallBack() {

            @Override
            public void onDataCallBack(JSONObject data) {
                // dialog.dismiss();
                if (data == null) {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----ERROR");
                    return;
                }
                int code = data.getInteger("code");
                if (code == 1000) {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----SUCCESS");
                } else {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----ERROR_2");

                }
            }
        });

    }

    // 设置点赞的
    private void setCommentTextClick(TextView mTextView2, JSONArray data,
            View view, int goodSize) {
        if (goodSize > 0 && data.size() > 0) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
        if (data.size() == 0) {
            mTextView2.setVisibility(View.GONE);
        } else {
            mTextView2.setVisibility(View.VISIBLE);

        }
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        int start = 0;

        for (int i = 0; i < data.size(); i++) {

            JSONObject json = data.getJSONObject(i);
            String content = json.getString("content");
            String scID = json.getString("scID");
            String userID_temp = json.getString("userID");

            String nick = userID_temp;

            if (userID_temp.equals(myuserID)) {
                nick = myNick;

            } else {

                User user = MYApplication.getInstance().getContactList()
                        .get(userID_temp);
                if (user != null) {

                    nick = user.getNick();

                }

            }
            String content_0 = "";
            String content_1 = ": " + content;
            String content_2 = ": " + content + "\n";
            if (i == (data.size() - 1) || (data.size() == 1 && i == 0)) {
                ssb.append(nick + content_1);
                content_0 = content_1;
            } else {

                ssb.append(nick + content_2);
                content_0 = content_2;
            }

            ssb.setSpan(new TextViewURLSpan(nick, userID_temp, 1), start, start
                    + nick.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (userID_temp.equals(myuserID)) {

                ssb.setSpan(new TextViewURLSpan(nick, userID_temp, i, scID, 2,
                        mTextView2, data, view, goodSize), start,
                        start + nick.length() + content_0.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            start = ssb.length();

        }

        mTextView2.setText(ssb);
        mTextView2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * 
     * 点赞
     * 
     */
    public void setGood(String sID, TextView tv_good, JSONArray jsons,
            LinearLayout ll_goodmembers_temp, View view, int cSize) {
        // 即时改变当前UI
        JSONObject json = new JSONObject();
        json.put("userID", myuserID);
        jsons.add(json);
        setGoodTextClick(tv_good, jsons, ll_goodmembers_temp, view, cSize);
        // 更新后台
        Map<String, String> map = new HashMap<String, String>();
        map.put("sID", sID);

        map.put("userID", myuserID);

        SocialApiTask task = new SocialApiTask(SocialDetailActivity.this,
                Constant.URL_SOCIAL_GOOD, map);
        task.getData(new DataCallBack() {

            @Override
            public void onDataCallBack(JSONObject data) {
                // dialog.dismiss();
                if (data == null) {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----ERROR");
                    return;
                }
                int code = data.getInteger("code");
                if (code == 1000) {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----SUCCESS");
                } else {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----ERROR_2");

                }
            }
        });

    }

    /**
     * 
     * 取消点赞
     * 
     */
    public void cancelGood(String sID, TextView tv_good, JSONArray jsons,
            LinearLayout ll_goodmembers_temp, View view, int cSize) {

        // 即时改变当前UI
        for (int i = 0; i < jsons.size(); i++) {
            JSONObject json = jsons.getJSONObject(i);
            if (json.getString("userID").equals(myuserID)) {
                jsons.remove(i);
            }
        }
        setGoodTextClick(tv_good, jsons, ll_goodmembers_temp, view, cSize);
        Map<String, String> map = new HashMap<String, String>();
        map.put("sID", sID);
        map.put("userID", myuserID);

        SocialApiTask task = new SocialApiTask(SocialDetailActivity.this,
                Constant.URL_SOCIAL_GOOD_CANCEL, map);
        task.getData(new DataCallBack() {

            @Override
            public void onDataCallBack(JSONObject data) {
                // dialog.dismiss();
                if (data == null) {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----ERROR");
                    return;
                }
                int code = data.getInteger("code");
                if (code == 1000) {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----SUCCESS");
                } else {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----ERROR_2");

                }
            }
        });

    }

    private String getTime(String rel_time, String now_time) {
        String backStr = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(rel_time);
            d2 = format.parse(now_time);

            // 毫秒ms
            long diff = d2.getTime() - d1.getTime();

            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffDays != 0) {
                if (diffDays < 30) {
                    if (1 < diffDays && diffDays < 2) {
                        backStr = "昨天";
                    } else if (1 < diffDays && diffDays < 2) {
                        backStr = "前天";

                    } else {

                        backStr = String.valueOf(diffDays) + "天前";
                    }
                } else {
                    backStr = "很久以前";
                }

            } else if (diffHours != 0) {
                backStr = String.valueOf(diffHours) + "小时前";

            } else if (diffMinutes != 0) {
                backStr = String.valueOf(diffMinutes) + "分钟前";

            } else {

                backStr = "刚刚";

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return backStr;

    }

    class ImageListener implements View.OnClickListener {
        String[] images;
        int page;

        public ImageListener(String[] images, int page) {

            this.images = images;
            this.page = page;
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent();
            intent.setClass(SocialDetailActivity.this, BigImageActivity.class);
            intent.putExtra("images", images);
            intent.putExtra("page", page);
            startActivity(intent);

        }

    }

    /**
     * 
     * 提交评论
     * 
     */

    private void submitComment() {
        String tag = String.valueOf(System.currentTimeMillis());
        String comment = et_comment.getText().toString().trim();
        // 即时改变当前UI
        JSONObject json = new JSONObject();
        json.put("userID", myuserID);
        json.put("content", comment);
        // 本地标记，方便本地定位删除，服务器端用不到这个字段
        json.put("tag", tag);
        jsons_tag.add(json);
        setCommentTextClick(tv_comment_tag, jsons_tag, view_tag, goodSize_tag);
        //
        // 更新后台
        Map<String, String> map = new HashMap<String, String>();
        map.put("sID", sID_tag);
        map.put("content", comment);
        map.put("userID", myuserID);
        map.put("tag", tag);
        SocialApiTask task = new SocialApiTask(SocialDetailActivity.this,
                Constant.URL_SOCIAL_COMMENT, map);
        task.getData(new DataCallBack() {

            @Override
            public void onDataCallBack(JSONObject data) {
                // dialog.dismiss();
                if (data == null) {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----ERROR");
                    return;
                }
                int code = data.getInteger("code");
                if (code == 1000) {
                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----SUCCESS");
                } else {

                    Log.e("hideCommentEditText()-->>>>",
                            "hideCommentEditText()-----ERROR_2");
                }
            }
        });

    }

    /**
     * 显示键盘
     * 
     * @param view
     */
    public void openInputMethod(final View editText) {

        InputMethodManager inputManager = (InputMethodManager) editText

        .getContext().getSystemService(

        Context.INPUT_METHOD_SERVICE);

        inputManager.showSoftInput(editText, 0);

    }

    /**
     * 关闭
     * 
     * @param view
     */

    public void closeInputMethod() {

        try {

            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))

            .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),

            InputMethodManager.HIDE_NOT_ALWAYS);

        } catch (Exception e) {
        } finally {
        }

    }
}
