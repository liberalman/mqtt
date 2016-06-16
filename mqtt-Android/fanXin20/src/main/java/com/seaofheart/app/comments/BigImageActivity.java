package com.seaofheart.app.comments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONArray;
 
import com.seaofheart.app.Constant;
import com.seaofheart.app.R;
import com.seaofheart.app.activity.BaseActivity;
import com.seaofheart.app.comments.ImageCycleView.ImageCycleViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;
 

public class BigImageActivity extends BaseActivity {
  //  private JSONArray json = null;

    private ImageCycleView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.social_activity_bigimage);
      //  String jsonStr = getIntent().getStringExtra("jsonStr");
        
        String[] images=getIntent().getStringArrayExtra("images");
     //   json = JSONArray.parseArray(jsonStr);
//        if (json == null) {
//            finish();
//            return;
//        }

        int page = getIntent().getIntExtra("page", 0);

        mAdView = (ImageCycleView) this.findViewById(R.id.ad_view);
        mAdView.setImageResources(images, page, mAdCycleViewListener);

    }

     
       
     

    private ImageCycleViewListener mAdCycleViewListener = new ImageCycleViewListener() {
        @Override
        public void onImageClick(int position, View imageView) {

           // finish();
        }

        @Override
        public void displayImage(String imageURL, ImageView imageView) {
            ImageLoader.getInstance().displayImage(imageURL, imageView);
        }
    };

}
