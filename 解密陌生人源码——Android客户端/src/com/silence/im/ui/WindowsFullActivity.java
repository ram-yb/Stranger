package com.silence.im.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.app.NotificationManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import com.silence.im.IM;
import com.silence.im.R;
import com.silence.im.imageutils.ImageFragmentAdapter;
import com.silence.im.imageutils.MyImageView;
import com.silence.im.imageutils.ViewImageFragment;
import com.silence.im.provider.SMSProvider.SMSColumns;
import com.stranger.client.util.FileMessage;

//���ͼƬȫ����Activity
public class WindowsFullActivity extends FragmentActivity {

	public static final int DIR_READ = 0;
	public static final int CHAT_READ = 1;

	// �ؼ�
	private ViewPager viewPager;
	private ImageFragmentAdapter adapter;
	// ����
	private int position;
	private List<String> fileNames;

	// �˵�
	private PopupWindow popupWindowGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ȫ�����ã����ش�������װ�� F
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// ����������View�ģ����Դ������е����β��ֱ����غ������Ȼ��Ч
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// �ؼ�ʵ����
		setContentView(R.layout.activity_windows_full);
		viewPager = (ViewPager) this
				.findViewById(R.id.activity_windows_full_viewpager);

		fileNames = new ArrayList<String>();
		// ��������
		int type = getIntent().getIntExtra("type", -1);
		// ��ȡ����
		initData(type);

		// ���ؿؼ�����
		adapter = new ImageFragmentAdapter(getSupportFragmentManager(),
				fileNames);
		viewPager.setAdapter(adapter);
		// ������ͼ
		viewPager.setCurrentItem(position);
		// ��ҳ����
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// ��ҳ������ͼƬ�ķŴ�״̬
				try {
					ViewImageFragment fragment = adapter.getFragment(arg0);
					if (fragment != null) {
						MyImageView imageView = fragment.getImageView();
						imageView.zoomTo(imageView.getMiniZoom());
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		});
	}

	private void initData(int type) {
		if (type == DIR_READ) {
			position = getIntent().getIntExtra("position", 0);
			File[] files = new File(IM.IMAGE_PATH).listFiles();
			for (File file : files)
				fileNames.add(file.getPath());
		} else if (type == CHAT_READ) {
			Uri uri = Uri
					.parse("content://com.silence.im.provider.SMSProvider/"
							+ StringUtils.parseName(IM
									.getString(IM.ACCOUNT_JID)) + "____sms");
			String sessionID = getIntent().getStringExtra("sessionID");
			String path = getIntent().getStringExtra("path");
			Cursor cursor = this.getContentResolver().query(
					uri,
					null,
					SMSColumns.SESSION_ID + "=? AND " + SMSColumns.STATUS
							+ "=?",
					new String[] { sessionID, FileMessage.COMPLETE },
					SMSColumns.TIME);
			int i = 0;
			while (cursor.moveToNext()) {
				String filePath = cursor.getString(cursor
						.getColumnIndex(SMSColumns.FILEPATH));
				String mimetype = cursor.getString(cursor
						.getColumnIndex(SMSColumns.TYPE));
				if (mimetype.contains("image")) {
					fileNames.add(filePath);
					if (path.equals(filePath))
						position = i;
					i++;
				}
			}
			cursor.close();
		}
	}

	private void initMenu() {
		View popupView = getLayoutInflater().inflate(R.layout.windows_menu,
				null);

		popupWindowGroup = new PopupWindow(popupView,
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		popupWindowGroup.setTouchable(true);
		popupWindowGroup.setOutsideTouchable(true);
		popupWindowGroup.setBackgroundDrawable(new BitmapDrawable(
				getResources(), (Bitmap) null));
		popupWindowGroup.getContentView().setFocusableInTouchMode(true);
		popupWindowGroup.getContentView().setFocusable(true);

		// ����
		Button btnCancel = (Button) popupView.findViewById(R.id.windows_cancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				popupWindowGroup.dismiss();
			}
		});

		Button btnDelete = (Button) popupView.findViewById(R.id.windows_save);
		btnDelete.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				String path = fileNames.get(position);
				String fileName = path.substring(path.lastIndexOf("/") + 1);
				new File(path).renameTo(new File(IM.DOWNLOAD_PATH + fileName));
				popupWindowGroup.dismiss();
				MyToast.makeText(WindowsFullActivity.this,
						"ͼƬ�ѱ��浽" + IM.DOWNLOAD_PATH + fileName,
						MyToast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");// ���봴��һ��
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (popupWindowGroup == null)
			initMenu();
		popupWindowGroup.showAtLocation(
				LayoutInflater.from(this).inflate(
						R.layout.activity_windows_full, null), Gravity.BOTTOM,
				0, 0);
		return false;// ����Ϊtrue ����ʾϵͳmenu
	}

	@Override
	protected void onStart() {
		super.onStart();
		// ����֪ͨ��
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(IM.MESSAGE_NOTIFICATION);
	}
}
