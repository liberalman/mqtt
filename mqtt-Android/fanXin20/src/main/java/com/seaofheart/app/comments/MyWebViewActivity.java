package com.seaofheart.app.comments;

 

import com.seaofheart.app.R;
import com.seaofheart.app.activity.BaseActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

 

public class MyWebViewActivity extends BaseActivity{
	WebView webView;
	ProgressBar progressBar1;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		initView();
	   
	}
	
	private void initView(){
		progressBar1=(ProgressBar) this.findViewById(R.id.progressBar1);
		
		String url=this.getIntent().getStringExtra("url");
		webView=(WebView) this.findViewById(R.id.webView);
		 
	 
		webView.loadUrl(url);
		webView.setWebViewClient(new WebViewClient(){
			  @Override
			public void onPageFinished(WebView view, String url) {
 				super.onPageFinished(view, url);
 				progressBar1.setVisibility(View.GONE);
 				
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			 
			       { //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
			 
			             view.loadUrl(url);
			               
			             return true;
			  
			       }
			  
			 });

		 
		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK
                 && event.getRepeatCount() == 0) {
        	webView.goBack();
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }

}
