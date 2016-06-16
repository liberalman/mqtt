package com.seaofheart.app.activity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.seaofheart.app.chat.ChatConfig;
import com.seaofheart.app.chat.FileMessageBody;
import com.seaofheart.app.R;
import com.seaofheart.app.cloud.CloudOperationCallback;
import com.seaofheart.app.cloud.HttpFileManager;
import com.seaofheart.app.util.FileUtils;

public class ShowNormalFileActivity extends BaseActivity {
	private ProgressBar progressBar;
	private File file;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_file);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		final FileMessageBody messageBody = getIntent().getParcelableExtra("msgbody");
		file = new File(messageBody.getLocalUrl());
		//set head map
		final Map<String, String> maps = new HashMap<String, String>();
		if (!TextUtils.isEmpty(messageBody.getSecret())) {
			maps.put("share-secret", messageBody.getSecret());
		}
		//下载文件
		new Thread(new Runnable() {
			public void run() {
				HttpFileManager fileManager = new HttpFileManager(ShowNormalFileActivity.this, ChatConfig.getInstance().getStorageUrl());
				fileManager.downloadFile(messageBody.getRemoteUrl(), messageBody.getLocalUrl(), maps,
						new CloudOperationCallback() {
							
							@Override
							public void onSuccess(String result) {
								runOnUiThread(new Runnable() {
									public void run() {
										FileUtils.openFile(file, ShowNormalFileActivity.this);
										finish();
									}
								});
							}
							
							@Override
							public void onProgress(final int progress) {
								runOnUiThread(new Runnable() {
									public void run() {
										progressBar.setProgress(progress);
									}
								});
							}
							
							@Override
							public void onError(final String msg) {
								runOnUiThread(new Runnable() {
									public void run() {
										if(file != null && file.exists()&&file.isFile())
											file.delete();
										Toast.makeText(ShowNormalFileActivity.this, "下载文件失败: "+msg, Toast.LENGTH_SHORT).show();
										finish();
									}
								});
							}
						});

			}
		}).start();
		
	}
}
