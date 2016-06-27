package com.silence.im.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.R;

@SuppressLint("NewApi")
public class AboutActivity extends Activity {

	private TextView content;
	private ImageButton titleBtn;
	private TextView titleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// 自定义ActionBar布局
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				AboutActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("关  于");

	}

	@Override
	protected void onStart() {
		super.onStart();
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(IM.MESSAGE_NOTIFICATION);
	}
}
