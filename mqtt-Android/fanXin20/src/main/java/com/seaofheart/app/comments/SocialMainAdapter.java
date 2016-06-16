package com.seaofheart.app.comments;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.facebook.drawee.view.SimpleDraweeView;
import com.seaofheart.app.Constant;
import com.seaofheart.app.MYApplication;
import com.seaofheart.app.R;
import com.seaofheart.app.comments.AddPopWindow.ClickCallBack;
import com.seaofheart.app.comments.SocialApiTask.DataCallBack;
import com.seaofheart.app.domain.User;
import com.seaofheart.app.fx.others.LocalUserInfo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SocialMainAdapter extends BaseAdapter {
    private SocialMainActivity context;
    private List<JSONObject> users;
    private LayoutInflater inflater;
    public RelativeLayout re_edittext;
    private String myuserID;
    private String myAvatar;
    private String myNick;

    public SocialMainAdapter(SocialMainActivity context1,
            List<JSONObject> jsonArray) {
        this.context = context1;

        this.users = jsonArray;
        inflater = LayoutInflater.from(context);

        // 底部评论输入框
        re_edittext = (RelativeLayout) context.findViewById(R.id.re_edittext);
        myuserID = MYApplication.getInstance().getUserName();
        myNick = LocalUserInfo.getInstance(context).getUserInfo("nick");
        myAvatar = LocalUserInfo.getInstance(context).getUserInfo("avatar");

    }

    @Override
    public int getCount() {
        return users.size() + 1;
    }

    @Override
    public JSONObject getItem(int position) {
        if (position == 0) {
            return null;
        } else {
            return users.get(position - 1);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            View view = inflater
                    .inflate(R.layout.item_header_social_main, null);
            final RelativeLayout ll_msg = (RelativeLayout) view
                    .findViewById(R.id.ll_msg);
            ll_msg.setVisibility(View.VISIBLE);
            ll_msg.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ll_msg.setVisibility(View.GONE);
                    Toast.makeText(context, "点击消息并跳转...", Toast.LENGTH_LONG)
                            .show();
                    ;
                }

            });
            return view;
        } else {

            convertView = inflater.inflate(R.layout.item_social_main, parent,
                    false);

            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (holder == null) {
                holder = new ViewHolder();
                holder.tv_nick = (TextView) convertView
                        .findViewById(R.id.tv_nick);
                holder.tv_time = (TextView) convertView
                        .findViewById(R.id.tv_time);

                holder.iv_avatar = (SimpleDraweeView) convertView
                        .findViewById(R.id.sdv_image);
                holder.image_1 = (SimpleDraweeView) convertView
                        .findViewById(R.id.image_1);
                holder.image_2 = (SimpleDraweeView) convertView
                        .findViewById(R.id.image_2);
                holder.image_3 = (SimpleDraweeView) convertView
                        .findViewById(R.id.image_3);
                holder.image_4 = (SimpleDraweeView) convertView
                        .findViewById(R.id.image_4);
                holder.image_5 = (SimpleDraweeView) convertView
                        .findViewById(R.id.image_5);
                holder.image_6 = (SimpleDraweeView) convertView
                        .findViewById(R.id.image_6);
                holder.image_7 = (SimpleDraweeView) convertView
                        .findViewById(R.id.image_7);
                holder.image_8 = (SimpleDraweeView) convertView
                        .findViewById(R.id.image_8);
                holder.image_9 = (SimpleDraweeView) convertView
                        .findViewById(R.id.image_9);
                holder.ll_one = (LinearLayout) convertView
                        .findViewById(R.id.ll_one);
                holder.ll_two = (LinearLayout) convertView
                        .findViewById(R.id.ll_two);
                holder.ll_three = (LinearLayout) convertView
                        .findViewById(R.id.ll_three);

                holder.tv_content = (TextView) convertView
                        .findViewById(R.id.tv_content);
                holder.tv_location = (TextView) convertView
                        .findViewById(R.id.tv_location);
                holder.iv_pop = (ImageView) convertView
                        .findViewById(R.id.iv_pop);

                holder.tv_goodmembers = (TextView) convertView
                        .findViewById(R.id.tv_goodmembers);
                holder.ll_goodmembers = (LinearLayout) convertView
                        .findViewById(R.id.ll_goodmembers);
                holder.tv_commentmembers = (TextView) convertView
                        .findViewById(R.id.tv_commentmembers);
                holder.view_pop = (View) convertView
                        .findViewById(R.id.view_pop);
                holder.tv_delete = (TextView) convertView
                        .findViewById(R.id.tv_delete);
                convertView.setTag(holder);
            }
            final View view_pop = holder.view_pop;
            JSONObject json = users.get(position - 1);
            // 如果数据出错....

            if (json == null || json.size() == 0) {
                users.remove(position - 1);
                this.notifyDataSetChanged();
            }
            final String userID = json.getString("userID");
            String content = json.getString("content");
            String imageStr = json.getString("imageStr");
            String location = json.getString("location");
            final String sID = json.getString("sID");
            // String token = json.getString("token");
            String rel_time = json.getString("time");
            // 设置删除键
            if (userID.equals(myuserID)) {

                holder.tv_delete.setVisibility(View.VISIBLE);
                holder.tv_delete.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        showPhotoDialog(position - 1, sID);
                        // users.remove(position - 1);
                        // this.notifyDataSetChanged();
                    }

                });
            } else {
                holder.tv_delete.setVisibility(View.GONE);
            }

            // 设置昵称。

            String nick = userID;
            String avatar = "";
            if (userID.equals(myuserID)) {
                nick = myNick;
                avatar = myAvatar;

            } else {

                User user = MYApplication.getInstance().getContactList()
                        .get(userID);
                if (user != null) {

                    nick = user.getNick();
                    avatar = user.getAvatar();

                }

            }

            holder.tv_nick.setText(nick);
            holder.iv_avatar.setImageURI(Uri
                    .parse(Constant.URL_Avatar + avatar));
            holder.tv_nick.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context,
                            SocialFriendActivity.class).putExtra("friendID",
                            userID));
                }

            });

            // 设置头像.....

            // 设置文章中的图片
           System.out.print("imageStr--->>"+imageStr);
            if (!imageStr.equals("0")) {
                String[] images = imageStr.split("split");
                int imNumb = images.length;
                holder.image_1.setVisibility(View.VISIBLE);
                holder.image_1.setImageURI(Uri.parse(Constant.URL_SOCIAL_PHOTO
                        + images[0]));
                holder.image_1.setOnClickListener(new ImageListener(images, 0));

                Log.e("imNumb--->>", String.valueOf(imNumb));
                // 四张图的时间情况比较特殊
                if (imNumb == 4) {
                    holder.image_2.setVisibility(View.VISIBLE);
                    holder.image_2.setImageURI(Uri
                            .parse(Constant.URL_SOCIAL_PHOTO + images[1]));
                    holder.image_2.setOnClickListener(new ImageListener(images,
                            1));
                    holder.image_4.setVisibility(View.VISIBLE);
                    holder.image_4.setImageURI(Uri
                            .parse(Constant.URL_SOCIAL_PHOTO + images[2]));
                    holder.image_4.setOnClickListener(new ImageListener(images,
                            2));
                    holder.image_5.setVisibility(View.VISIBLE);
                    holder.image_5.setImageURI(Uri
                            .parse(Constant.URL_SOCIAL_PHOTO + images[3]));
                    holder.image_5.setOnClickListener(new ImageListener(images,
                            3));
                } else {
                    if (imNumb > 1) {
                        holder.image_2.setVisibility(View.VISIBLE);
                        holder.image_2.setImageURI(Uri
                                .parse(Constant.URL_SOCIAL_PHOTO + images[1]));
                        holder.image_2.setOnClickListener(new ImageListener(
                                images, 1));
                        if (imNumb > 2) {
                            holder.image_3.setVisibility(View.VISIBLE);
                            holder.image_3.setImageURI(Uri
                                    .parse(Constant.URL_SOCIAL_PHOTO
                                            + images[2]));
                            holder.image_3
                                    .setOnClickListener(new ImageListener(
                                            images, 2));
                            if (imNumb > 3) {
                                holder.image_4.setVisibility(View.VISIBLE);
                                holder.image_4.setImageURI(Uri
                                        .parse(Constant.URL_SOCIAL_PHOTO
                                                + images[3]));
                                holder.image_4
                                        .setOnClickListener(new ImageListener(
                                                images, 3));
                                if (imNumb > 4) {
                                    holder.image_5.setVisibility(View.VISIBLE);
                                    holder.image_5.setImageURI(Uri
                                            .parse(Constant.URL_SOCIAL_PHOTO
                                                    + images[4]));
                                    holder.image_5
                                            .setOnClickListener(new ImageListener(
                                                    images, 4));
                                    if (imNumb > 5) {
                                        holder.image_6
                                                .setVisibility(View.VISIBLE);
                                        holder.image_6
                                                .setImageURI(Uri
                                                        .parse(Constant.URL_SOCIAL_PHOTO
                                                                + images[5]));
                                        holder.image_6
                                                .setOnClickListener(new ImageListener(
                                                        images, 5));
                                        if (imNumb > 6) {
                                            holder.image_7
                                                    .setVisibility(View.VISIBLE);
                                            holder.image_7
                                                    .setImageURI(Uri
                                                            .parse(Constant.URL_SOCIAL_PHOTO
                                                                    + images[6]));
                                            holder.image_7
                                                    .setOnClickListener(new ImageListener(
                                                            images, 6));
                                            if (imNumb > 7) {
                                                holder.image_8
                                                        .setVisibility(View.VISIBLE);
                                                holder.image_8
                                                        .setImageURI(Uri
                                                                .parse(Constant.URL_SOCIAL_PHOTO
                                                                        + images[7]));
                                                holder.image_8
                                                        .setOnClickListener(new ImageListener(
                                                                images, 7));
                                                if (imNumb > 8) {
                                                    holder.image_9
                                                            .setVisibility(View.VISIBLE);
                                                    holder.image_9
                                                            .setImageURI(Uri
                                                                    .parse(Constant.URL_SOCIAL_PHOTO
                                                                            + images[8]));
                                                    holder.image_9
                                                            .setOnClickListener(new ImageListener(
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
                holder.tv_location.setVisibility(View.VISIBLE);
                holder.tv_location.setText(location);
            }
            // 显示文章内容
            // .setText(content);
            setUrlTextView(content, holder.tv_content);
            final ImageView iv_temp = holder.iv_pop;
            final LinearLayout ll_goodmembers_temp = holder.ll_goodmembers;

            // 点赞评论的数据
            final JSONArray goodArray = json.getJSONArray("good");
            final JSONArray commentArray = json.getJSONArray("comment");

            // 点赞

            setGoodTextClick(holder.tv_goodmembers, goodArray,
                    ll_goodmembers_temp, view_pop, commentArray.size());

            boolean is_good_temp = true;
            for (int i = 0; i < goodArray.size(); i++) {
                JSONObject json_good = goodArray.getJSONObject(i);
                if (json_good.getString("userID").equals(myuserID)) {
                    is_good_temp = false;
                }
            }
            // 评论

            if (commentArray != null && commentArray.size() != 0) {
                holder.tv_commentmembers.setVisibility(View.VISIBLE);
                setCommentTextClick(holder.tv_commentmembers, commentArray,
                        view_pop, goodArray.size());

            }

            final boolean is_good = is_good_temp;
            String goodStr = "赞";
            if (!is_good) {
                goodStr = "取消";

            }
            iv_temp.setTag(goodStr);

            final TextView tv_commentmembers_temp = holder.tv_commentmembers;
            final TextView tv_good_temp = holder.tv_goodmembers;
            iv_temp.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    AddPopWindow addPopWindow = new AddPopWindow(
                            (SocialMainActivity) context, iv_temp,
                            new ClickCallBack() {

                                @Override
                                public void clicked(int type) {
                                    // 点击取消
                                    if (type == 1) {
                                        if (((String) iv_temp.getTag())
                                                .equals("赞")) {
                                            setGood(sID, tv_good_temp,
                                                    goodArray,
                                                    ll_goodmembers_temp,
                                                    view_pop,
                                                    commentArray.size());
                                            iv_temp.setTag("取消");

                                        } else {
                                            cancelGood(sID, tv_good_temp,
                                                    goodArray,
                                                    ll_goodmembers_temp,
                                                    view_pop,
                                                    commentArray.size());
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

            holder.tv_time.setText(getTime(rel_time, MYApplication
                    .getInstance().getTime()));

            return convertView;
        }

    }

    public static class ViewHolder {
        SimpleDraweeView iv_avatar;
        // 昵称
        TextView tv_nick;
        // 时间
        TextView tv_time;
        // 三行图片
        LinearLayout ll_one;
        LinearLayout ll_two;
        LinearLayout ll_three;
        SimpleDraweeView image_1;
        SimpleDraweeView image_2;

        SimpleDraweeView image_3;
        SimpleDraweeView image_4;
        SimpleDraweeView image_5;
        SimpleDraweeView image_6;
        SimpleDraweeView image_8;
        SimpleDraweeView image_9;
        SimpleDraweeView image_7;
        // 动态内容
        TextView tv_content;
        // 删除

        TextView tv_delete;
        // 位置
        TextView tv_location;
        // 评论点赞
        ImageView iv_pop;
        LinearLayout ll_goodmembers;
        TextView tv_goodmembers;
        TextView tv_commentmembers;
        View view_pop;
    }

    @SuppressLint("SimpleDateFormat")
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
            intent.setClass(context, BigImageActivity.class);
            intent.putExtra("images", images);
            intent.putExtra("page", page);
            context.startActivity(intent);

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
            String content = json.getString("content");
            String scID = json.getString("scID");

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
                ds.setColor(context.getResources().getColor(R.color.text_color));

            }
            ds.setUnderlineText(false); // 去掉下划线
        }

        @Override
        public void onClick(final View widget) {

            if (widget instanceof TextView) {
                ((TextView) widget).setHighlightColor(context.getResources()
                        .getColor(android.R.color.darker_gray));
                new Handler().postDelayed(new Runnable() {

                    public void run() {

                        ((TextView) widget).setHighlightColor(context
                                .getResources().getColor(
                                        android.R.color.transparent));

                    }

                }, 1000);

            }

            if (type == 2) {
                showDeleteDialog(userID, postion, scID, type, ctextView,
                        cjsons, view, goodSize);

            } else {

                context.startActivity(new Intent(context,
                        SocialFriendActivity.class)
                        .putExtra("friendID", userID));
            }
        }

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
        if (re_edittext == null || re_edittext.getVisibility() != View.VISIBLE) {
            re_edittext = (RelativeLayout) context
                    .findViewById(R.id.re_edittext);
            re_edittext.setVisibility(View.VISIBLE);
            final EditText et_comment = (EditText) re_edittext
                    .findViewById(R.id.et_comment);
            Button btn_send = (Button) re_edittext.findViewById(R.id.btn_send);
            btn_send.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String comment = et_comment.getText().toString().trim();
                    if (TextUtils.isEmpty(comment)) {
                        Toast.makeText(context, "请输入评论", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    submitComment(sID, comment, tv_comment, jsons, view,
                            goodSize);
                    et_comment.setText("");
                    hideCommentEditText();
                }

            });
        }

    }

    /**
     * 
     * 隐藏发表评论的输入框
     * 
     * 
     * 
     */
    public void hideCommentEditText() {
        if (re_edittext != null && re_edittext.getVisibility() == View.VISIBLE)
            re_edittext.setVisibility(View.GONE);
    }

    /**
     * 
     * 提交评论
     * 
     */
    private void submitComment(String sID, String comment, TextView tv_comment,
            JSONArray jsons, View view, int goodSize) {
        String tag = String.valueOf(System.currentTimeMillis());

        // 即时改变当前UI
        JSONObject json = new JSONObject();
        json.put("userID", myuserID);
        json.put("content", comment);
        // 本地标记，方便本地定位删除，服务器端用不到这个字段
        json.put("tag", tag);
        jsons.add(json);
        setCommentTextClick(tv_comment, jsons, view, goodSize);
        //
        // 更新后台
        Map<String, String> map = new HashMap<String, String>();
        map.put("sID", sID);
        map.put("content", comment);
        map.put("userID", myuserID);
        map.put("tag", tag);
        SocialApiTask task = new SocialApiTask(context,
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

        SocialApiTask task = new SocialApiTask(context,
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

        SocialApiTask task = new SocialApiTask(context,
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

    private void showDeleteDialog(final String userID, final int postion,
            final String scID, final int type, final TextView ctextView,
            final JSONArray cjsons, final View view, final int goodSize) {
        final AlertDialog dlg = new AlertDialog.Builder(context).create();
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.dialog_social_main);
        TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
        tv_paizhao.setText("复制");
        tv_paizhao.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
                ClipboardManager cmb = (ClipboardManager) context
                        .getSystemService(Context.CLIPBOARD_SERVICE);
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
        SocialApiTask task = new SocialApiTask(context,
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
                ((TextView) widget).setHighlightColor(context.getResources()
                        .getColor(android.R.color.darker_gray));
                new Handler().postDelayed(new Runnable() {

                    public void run() {

                        ((TextView) widget).setHighlightColor(context
                                .getResources().getColor(
                                        android.R.color.transparent));

                    }

                }, 1000);

            }
            context.startActivity(new Intent(context, MyWebViewActivity.class)
                    .putExtra("url", url));

        }

    }

    public static boolean checkURL(String url) {
        boolean value = false;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url)
                    .openConnection();
            int code = conn.getResponseCode();
            System.out.println(">>>>>>>>>>>>>>>> " + code
                    + " <<<<<<<<<<<<<<<<<<");
            if (code != 200) {
                value = false;
            } else {
                value = true;
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }

    private void showPhotoDialog(final int index, final String sID) {
        final AlertDialog dlg = new AlertDialog.Builder(context).create();
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

                users.remove(index);
                notifyDataSetChanged();
                // 更新服务器
                Map<String, String> map = new HashMap<String, String>();
                map.put("sID", sID);
                SocialApiTask task = new SocialApiTask(context,
                        Constant.URL_SOCIAL_DELETE, map);
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
                dlg.cancel();
            }
        });

    }
}
