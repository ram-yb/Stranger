package com.silence.im.ui;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.R;

/**
 * 欢迎界面
 * 
 * @author JerSuen
 */
public class WelcomeActivity extends Activity {
	/* private SecretTextView hintView; */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_welcome);

		/*
		 * hintView = (SecretTextView) findViewById(R.id.activity_welcome_hint);
		 * findViewById
		 * (R.id.activity_welcome_hint).startAnimation(AnimationUtils
		 * .loadAnimation(this, R.anim.welcome_hint_bottom_in));
		 * hintView.setmDuration(1500); hintView.toggle();
		 */

		makeDirectory();// 创建APP文件目录

		// 首次打开初始化
		PackageInfo info;
		int currentVersion = 0;
		try {
			info = getPackageManager().getPackageInfo(IM.PACKAGE_NAME, 0);
			currentVersion = info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		int lastVersion = prefs.getInt(IM.VERSION_KEY, 0);
		if (currentVersion > lastVersion) {
			// 如果当前版本大于上次版本，该版本属于第一次启动
			// 将当前版本写入preference中，则下次启动的时候，据此判断，不再为首次启动
			prefs.edit().putInt(IM.VERSION_KEY, currentVersion).commit();
			firstInit();
			// 第一次启动设置IP
			showDialog();
		} else {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					startActivity(new Intent(WelcomeActivity.this,
							LoginActivity.class));
					WelcomeActivity.this.finish();
				}
			}, 1000);
		}

		IM.putString(IM.CHAT_STATUS, IM.NO_CHATTING);
	}

	private void showDialog() {
		/* 初始化普通对话框。并设置样式 */
		Dialog selectDialog = new Dialog(this, R.style.dialog);
		selectDialog.setCancelable(true);
		selectDialog.setCanceledOnTouchOutside(true);
		/* 设置普通对话框的布局 */
		selectDialog.setContentView(R.layout.dialog_text3);

		final EditText address = (EditText) selectDialog
				.findViewById(R.id.dialog_address);
		final EditText http = (EditText) selectDialog
				.findViewById(R.id.dialog_http);
		Button submitButton = (Button) selectDialog
				.findViewById(R.id.dialog_button);

		submitButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String addressNetString = address.getText().toString();
				String httpString = http.getText().toString();
				if (addressNetString != null) {
					Properties properties = new Properties();
					try {
						properties.load(IM.im.getAssets().open(
								"property.properties"));
					} catch (IOException e) {
						e.printStackTrace();
					}
					properties.put("host", addressNetString.trim());
					properties.put("httphost", httpString);
					IM.HOST = addressNetString.trim();
					new Handler().postDelayed(new Runnable() {
						public void run() {
							startActivity(new Intent(WelcomeActivity.this,
									LoginActivity.class));
							WelcomeActivity.this.finish();
						}
					}, 1000);
				}
			}
		});

		Window dialogWindow31 = selectDialog.getWindow();
		WindowManager m31 = dialogWindow31.getWindowManager();
		Display d31 = m31.getDefaultDisplay(); // 获取屏幕宽、高用
		WindowManager.LayoutParams p31 = dialogWindow31.getAttributes(); // 获取对话框当前的参数值
		p31.height = (int) (d31.getHeight() * 0.5); // 高度设置为屏幕的0.5
		p31.width = (int) (d31.getWidth() * 0.7); // 宽度设置为屏幕的0.7
		dialogWindow31.setAttributes(p31);
		selectDialog.show();// 显示对话框
	}

	private void firstInit() {
		// 判断Android版本，4.4版本是分界线
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			IM.putSetting("androidVersion", android.os.Build.VERSION.SDK_INT);// 4.4版本
			IM.putSetting("KITKAT", 1);
		} else {
			IM.putSetting("androidVersion", android.os.Build.VERSION.SDK_INT);// 4.4以下版本
			IM.putSetting("KITKAT", 0);
		}
		// 初始化设置参数
		IM.putSetting(SettingActivity.SETTING_VOICE, SettingActivity.VOICE_OPEN);
		IM.putSetting(SettingActivity.SETTING_NOTIFICATION,
				SettingActivity.NOTIFICATION_OPEN);
		IM.putSetting(IM.ONLINE_STATUS, IM.AVAILABLE);
		IM.putSetting(SettingActivity.REMEMBER_PASSWORD,
				SettingActivity.NO_REMEMBER);

		int width = getWindowManager().getDefaultDisplay().getWidth();
		int height = getWindowManager().getDefaultDisplay().getHeight();
		IM.putSetting("width", width);
		IM.putSetting("height", height);
	}

	private void makeDirectory() {

		File file_avatar = new File(IM.AVATAR_PATH);
		if (!file_avatar.exists()) {
			file_avatar.mkdirs();
		}
		File file_download = new File(IM.DOWNLOAD_PATH);
		if (!file_download.exists()) {
			file_download.mkdirs();
		}
		File file_audio = new File(IM.AUDIO_PATH);
		if (!file_audio.exists()) {
			file_audio.mkdirs();
		}
		File file_image = new File(IM.IMAGE_PATH);
		if (!file_image.exists()) {
			file_image.mkdirs();
		}
		File file_cache = new File(IM.CACHE_PATH);
		if (!file_cache.exists()) {
			file_cache.mkdirs();
		}
	}
}
