package com.silence.im.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.util.StringUtils;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.R;
import com.silence.im.provider.ContactsProvider;
import com.silence.im.service.Contact;
import com.silence.im.service.IXmppManager;
import com.silence.im.ui.adapter.AddViewAdapter;
import com.silence.im.ui.adapter.SearchAdapter;
import com.silence.im.ui.view.RoundedImageView;
import com.silence.im.util.Base64;
import com.silence.im.util.BitmapTool;

/**
 * 添加好友
 * 
 * @author JerSuen
 */
public class AddActivity extends Activity implements View.OnClickListener,
		OnItemClickListener {

	private static final String GROUP_NAME = "friends";
	public static final int ADD_FRIEND_ERROR = 0;
	public static final int ADD_FRIEND_SUCCESS = 1;
	public static final int ADD_FRIEND_EXIST = 2;
	// ViewPage
	private AddFriendTask addFriendTask;
	private ViewPager viewPager;
	private AddViewAdapter adapter;
	private View searchView, examineView;
	// ListView
	private ListView listView;
	private SearchAdapter listViewAdapter;
	private List<Map<String, Object>> list;
	// System tools
	private ServiceConnection serviceConnect = new XMPPServiceConnection();
	private IXmppManager xmppManager;

	private Contact stranger;
	private EditText name_by_mEditText;
	private byte[] avatarBytes;

	// private ProgressDialog dialog;
	private CustomProgressDialog progressDialog;

	private ImageButton titleBtn;
	private TextView titleText;

	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// 自定义ActionBar布局
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				AddActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("添加好友");
		setContentView(R.layout.activity_add);
		viewPager = (ViewPager) findViewById(R.id.activity_add_view_pager);
		// 禁止滑动
		viewPager.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View view, MotionEvent motionEvent) {
				return true;
			}
		});

		searchView = getLayoutInflater().inflate(
				R.layout.activity_add_view_search_account, null);
		listView = (ListView) searchView
				.findViewById(R.id.activity_add_view_search_account_list);
		listView.setOnItemClickListener(this);
		examineView = getLayoutInflater().inflate(
				R.layout.activity_add_view_examine_account, null);

		List<View> views = new ArrayList<View>();
		views.add(searchView);
		views.add(examineView);

		adapter = new AddViewAdapter(views);
		// 适配器内容监听器
		adapter.setOnSignViewClickListener(this);
		viewPager.setAdapter(adapter);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_add_view_search_account_commit:
			list = new ArrayList<Map<String, Object>>();
			new AsyncTask<String, Integer, Boolean>() {

				protected void onPreExecute() {
					// dialog = ProgressDialog.show(AddActivity.this, "搜索好友",
					// "正在为您搜索好友，请稍候...");
					progressDialog = CustomProgressDialog
							.createDialog(AddActivity.this);
					progressDialog.setMessage("loading....");
					progressDialog.show();
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(AddActivity.this
							.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					progressDialog.show();
				};

				@Override
				protected Boolean doInBackground(String... params) {
					String accountStr = ((EditText) searchView
							.findViewById(R.id.activity_add_view_search_account_input))
							.getText().toString().trim();
					if (TextUtils.isEmpty(accountStr) || xmppManager == null) {
						return false;
					}
					try {
						String[] jidStr = xmppManager.searchAccount(accountStr,
								true);

						for (String str : jidStr) {
							if (!TextUtils.isEmpty(str)) {
								stranger = xmppManager.getVCard(str);
								Map<String, Object> map = new HashMap<String, Object>();
								map.put("account", stranger.account);
								map.put("avatar", stranger.avatar);
								map.put("gender", stranger.gender);
								map.put("nickname", stranger.nickname);
								list.add(map);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					listViewAdapter = new SearchAdapter(AddActivity.this, list);
					return true;
				}

				protected void onPostExecute(Boolean result) {
					progressDialog.dismiss();
					listView.setAdapter(listViewAdapter);
				};
			}.execute();
			break;
		case R.id.activity_user_commit:
			// TODO 添加好友
			addFriendTask = new AddFriendTask();
			stranger.name_by_me = name_by_mEditText.getText().toString();
			addFriendTask.execute();
			break;
		}
	}

	private class AddFriendTask extends AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			System.out.println("jid = " + stranger.account + " nickname = "
					+ stranger.nickname);
			int result = AddFriendActivity.ADD_FRIEND_ERROR;
			try {
				result = xmppManager.addGroupFriend(GROUP_NAME,
						stranger.account, stranger.name_by_me);
			} catch (RemoteException e1) {
				e1.printStackTrace();
				return AddFriendActivity.ADD_FRIEND_ERROR;
			}
			if (stranger.avatar != null) {
				avatarBytes = Base64.decode(stranger.avatar);
				Bitmap bm = BitmapFactory.decodeByteArray(avatarBytes, 0,
						avatarBytes.length);
				try {
					BitmapTool.saveFile(
							bm,
							IM.AVATAR_PATH
									+ StringUtils.parseName(stranger.account));
				} catch (IOException e) {
					e.printStackTrace();
					return AddFriendActivity.ADD_FRIEND_ERROR;
				}
			}
			return result;
		}

		@Override
		protected void onPreExecute() {
			// dialog = ProgressDialog.show(AddActivity.this, "添加好友",
			// "正在添加好友...");
			progressDialog = CustomProgressDialog
					.createDialog(AddActivity.this);
			progressDialog.setMessage("loading....");
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(Integer result) {
			progressDialog.dismiss();
			if (result == ADD_FRIEND_SUCCESS) {
				// 添加成功
				// 返回好友列表AddActivity
				finish();
			} else if (result == ADD_FRIEND_EXIST) {
				// 好友已存在
				MyToast.makeText(AddActivity.this, "TA已经是您的好友了哦", 1).show();
			} else
				// 添加失败
				MyToast.makeText(AddActivity.this, "添加好友失败", 1).show();
		}
	}

	/** 连接服务 */
	private class XMPPServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName componentName,
				IBinder iBinder) {
			xmppManager = IXmppManager.Stub.asInterface(iBinder);
		}

		public void onServiceDisconnected(ComponentName componentName) {
			xmppManager = null;
		}
	}

	protected void onStart() {
		super.onStart();
		bindService(new Intent(this, IMService.class), serviceConnect,
				BIND_AUTO_CREATE);
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(IM.MESSAGE_NOTIFICATION);
	}

	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnect);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String account = (String) list.get(position).get("account");
		if (!TextUtils.isEmpty(account)) {
			viewPager.setCurrentItem(1);
			try {
				stranger = xmppManager.getVCard(account);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			// Intent intent = new Intent(AddActivity.this,
			// AddFriendActivity.class);
			// intent.putExtra("stranger", stranger);
			// startActivity(intent);
			// AddActivity.this.finish();
			// examineView.findViewById(R.id.activity_user_commit).setVisibility(
			// View.GONE);
			// 头像
			Button addFriendbButton = (Button) examineView
					.findViewById(R.id.activity_user_commit);
			addFriendbButton.setText("添加好友");
			RoundedImageView avatar = (RoundedImageView) examineView
					.findViewById(R.id.activity_user_avatar);
			// 账户
			TextView accountTextView = (TextView) examineView
					.findViewById(R.id.activity_user_account);
			// 昵称
			TextView nickNameTextView = (TextView) examineView
					.findViewById(R.id.activity_user_nickname);
			// 邮箱
			TextView emailTextView = (TextView) examineView
					.findViewById(R.id.activity_user_email);
			// 姓名
			TextView realNameTextView = (TextView) examineView
					.findViewById(R.id.activity_user_realname);
			// 备注
			name_by_mEditText = (EditText) examineView
					.findViewById(R.id.activity_user_name);
			// 性别
			RadioGroup radioGroup = (RadioGroup) examineView
					.findViewById(R.id.activity_user_gendergroup);

			// 装载数据
			accountTextView.setText(stranger.account);
			nickNameTextView.setText(stranger.nickname);
			realNameTextView.setText(stranger.realname);
			emailTextView.setText(stranger.email);
			// 头像
			if (stranger.avatar != null) {// 检测头像为空
				avatarBytes = Base64.decode(stranger.avatar);
				Bitmap bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0,
						avatarBytes.length);
				avatar.setImageBitmap(bitmap);
			} else {
				// 加载默认图片
				avatar.setBackgroundResource(R.drawable.ic_launcher);
			}
			// 性别
			System.out.println("AddFriend -->> gender = " + stranger.gender);
			String gender = stranger.gender.substring(0,
					stranger.gender.indexOf(":"));
			String genderContent = null;
			if (stranger.gender.length() - stranger.gender.indexOf(":") > 1)
				genderContent = stranger.gender.substring(
						stranger.gender.indexOf(":") + 1,
						stranger.gender.length());
			if (ContactsProvider.MAN.equals(gender)) {
				((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
			} else if (ContactsProvider.WOMAN.equals(gender)) {
				((RadioButton) radioGroup.getChildAt(1)).setChecked(true);
			} else if (ContactsProvider.OTHER.equals(gender)) {
				// ((RadioButton) radioGroup.getChildAt(2)).setChecked(true);
				radioGroup.check(0);
				((TextView) this.findViewById(R.id.activity_user_othergender))
						.setText(genderContent);
			}

			((TextView) this.findViewById(R.id.activity_user_othergender))
					.setFocusable(false);
			accountTextView.setFocusable(false);
			nickNameTextView.setFocusable(false);
			realNameTextView.setFocusable(false);
			emailTextView.setFocusable(false);
			for (int i = 0; i < radioGroup.getChildCount(); i++)
				((RadioButton) radioGroup.getChildAt(i)).setEnabled(false);
		} else {
			MyToast.makeText(AddActivity.this, "没有此用户", MyToast.LENGTH_SHORT)
					.show();
		}
	}
}