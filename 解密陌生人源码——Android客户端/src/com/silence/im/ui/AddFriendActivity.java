package com.silence.im.ui;

import java.io.File;

import org.jivesoftware.smack.util.StringUtils;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.R;
import com.silence.im.provider.ContactsProvider;
import com.silence.im.service.Contact;
import com.silence.im.service.IXmppManager;
import com.silence.im.ui.view.RoundedImageView;
import com.silence.im.util.BitmapTool;

@SuppressLint({ "InlinedApi", "NewApi" })
public class AddFriendActivity extends Activity {

	public static final int ADD_FRIEND_ERROR = 0;
	public static final int ADD_FRIEND_SUCCESS = 1;
	public static final int ADD_FRIEND_EXIST = 2;
	private static final String GROUP_NAME = "friends";

	private IXmppManager xmppManager;
	private ServiceConnection serviceConnect = new XMPPServiceConnection();
	private AddFriendTask addFriendTask;

	// 控件
	private RoundedImageView avatar;
	private TextView accountEditText, nickNameTextView, realNameTextView,
			emailTextView;
	private EditText name_by_mEditText;
	private Button addFriend;
	private RadioGroup radioGroup;
	private CustomProgressDialog progressDialog;

	// 拍照文件
	private Contact stranger;

	private ImageButton titleBtn;
	private TextView titleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 实例化界面
		setContentView(R.layout.activity_add_view_examine_account);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// 自定义ActionBar布局
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				AddFriendActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("添加好友");

		// 头像
		avatar = (RoundedImageView) this
				.findViewById(R.id.activity_user_avatar);
		// 账户
		accountEditText = (TextView) this
				.findViewById(R.id.activity_user_account);

		// 昵称
		nickNameTextView = (TextView) this
				.findViewById(R.id.activity_user_nickname);
		// 邮箱
		emailTextView = (TextView) this.findViewById(R.id.activity_user_email);
		// 姓名
		realNameTextView = (TextView) this
				.findViewById(R.id.activity_user_realname);
		// 备注
		name_by_mEditText = (EditText) this
				.findViewById(R.id.activity_user_name);
		// 性别
		radioGroup = (RadioGroup) this
				.findViewById(R.id.activity_user_gendergroup);

		// 按钮
		// findViewById(R.id.activity_user_commit).setVisibility(View.GONE);
		addFriend = (Button) this.findViewById(R.id.activity_user_commit);
		addFriend.setText("添加好友");

		// 获取数据
		System.out.println("AddFriends");
		Intent intent = getIntent();
		stranger = intent.getParcelableExtra("stranger");

		// 装载数据
		accountEditText.setText(stranger.account);
		nickNameTextView.setText(stranger.nickname);
		realNameTextView.setText(stranger.realname);
		emailTextView.setText(stranger.email);
		// 头像
		if (stranger.avatar != null) {// 检测头像为空
			System.out.println("path = " + stranger.avatar + "  avatar --->>"
					+ avatar.toString());
			Bitmap bitmap = BitmapTool.decodeBitmap(stranger.avatar, 0, 0,
					false);
			Drawable drawable = new BitmapDrawable(getResources(), bitmap);
			avatar.setImageDrawable(drawable);
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
					stranger.gender.indexOf(":") + 1, stranger.gender.length());
		if (ContactsProvider.MAN.equals(gender)) {
			((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
		} else if (ContactsProvider.WOMAN.equals(gender)) {
			((RadioButton) radioGroup.getChildAt(1)).setChecked(true);
		} else if (ContactsProvider.OTHER.equals(gender)) {
			// ((RadioButton) radioGroup.getChildAt(2)).setChecked(true);
			((TextView) this.findViewById(R.id.activity_user_othergender))
					.setText(genderContent);
		}

		((TextView) this.findViewById(R.id.activity_user_othergender))
				.setFocusable(false);
		accountEditText.setFocusable(false);
		nickNameTextView.setFocusable(false);
		realNameTextView.setFocusable(false);
		emailTextView.setFocusable(false);
		for (int i = 0; i < radioGroup.getChildCount(); i++)
			((RadioButton) radioGroup.getChildAt(i)).setEnabled(false);

		// 初始化对话框
		// dialog = new ProgressDialog(AddFriendActivity.this);
		// dialog.setTitle("添加好友");
		// dialog.setMessage("正在添加好友...");
		// dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
		//
		// @Override
		// public boolean onKey(DialogInterface dialog, int keyCode,
		// KeyEvent event) {
		// if (KeyEvent.KEYCODE_BACK == keyCode) {
		// addFriendTask.cancel(true);
		// dialog.dismiss();
		// }
		// return true;
		// }
		// });
		progressDialog = CustomProgressDialog.createDialog(this);
		progressDialog.setMessage("loading....");

		// 添加好友监听
		addFriend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				addFriendTask = new AddFriendTask();
				stranger.name_by_me = name_by_mEditText.getText().toString();
				addFriendTask.execute();
			}
		});
	}

	private class AddFriendTask extends AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			System.out.println("jid = " + stranger.account + " nickname = "
					+ stranger.nickname);
			int result = ADD_FRIEND_ERROR;
			try {
				result = xmppManager.addGroupFriend(GROUP_NAME,
						stranger.account, stranger.name_by_me);
			} catch (RemoteException e) {
				e.printStackTrace();
				return ADD_FRIEND_ERROR;
			}
			if (stranger.avatar != null) {
				File file = new File(stranger.avatar);
				file.renameTo(new File(IM.AVATAR_PATH
						+ StringUtils.parseName(stranger.account)));
			}
			return result;
		}

		@Override
		protected void onPreExecute() {
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
				MyToast.makeText(AddFriendActivity.this, "TA已经是您的好友了哦", 1)
						.show();
			} else
				// 添加失败
				MyToast.makeText(AddFriendActivity.this, "添加好友失败", 1).show();
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
}
