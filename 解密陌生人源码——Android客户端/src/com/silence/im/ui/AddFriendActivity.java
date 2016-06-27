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

	// �ؼ�
	private RoundedImageView avatar;
	private TextView accountEditText, nickNameTextView, realNameTextView,
			emailTextView;
	private EditText name_by_mEditText;
	private Button addFriend;
	private RadioGroup radioGroup;
	private CustomProgressDialog progressDialog;

	// �����ļ�
	private Contact stranger;

	private ImageButton titleBtn;
	private TextView titleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ʵ��������
		setContentView(R.layout.activity_add_view_examine_account);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// �Զ���ActionBar����
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				AddFriendActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("��Ӻ���");

		// ͷ��
		avatar = (RoundedImageView) this
				.findViewById(R.id.activity_user_avatar);
		// �˻�
		accountEditText = (TextView) this
				.findViewById(R.id.activity_user_account);

		// �ǳ�
		nickNameTextView = (TextView) this
				.findViewById(R.id.activity_user_nickname);
		// ����
		emailTextView = (TextView) this.findViewById(R.id.activity_user_email);
		// ����
		realNameTextView = (TextView) this
				.findViewById(R.id.activity_user_realname);
		// ��ע
		name_by_mEditText = (EditText) this
				.findViewById(R.id.activity_user_name);
		// �Ա�
		radioGroup = (RadioGroup) this
				.findViewById(R.id.activity_user_gendergroup);

		// ��ť
		// findViewById(R.id.activity_user_commit).setVisibility(View.GONE);
		addFriend = (Button) this.findViewById(R.id.activity_user_commit);
		addFriend.setText("��Ӻ���");

		// ��ȡ����
		System.out.println("AddFriends");
		Intent intent = getIntent();
		stranger = intent.getParcelableExtra("stranger");

		// װ������
		accountEditText.setText(stranger.account);
		nickNameTextView.setText(stranger.nickname);
		realNameTextView.setText(stranger.realname);
		emailTextView.setText(stranger.email);
		// ͷ��
		if (stranger.avatar != null) {// ���ͷ��Ϊ��
			System.out.println("path = " + stranger.avatar + "  avatar --->>"
					+ avatar.toString());
			Bitmap bitmap = BitmapTool.decodeBitmap(stranger.avatar, 0, 0,
					false);
			Drawable drawable = new BitmapDrawable(getResources(), bitmap);
			avatar.setImageDrawable(drawable);
		} else {
			// ����Ĭ��ͼƬ
			avatar.setBackgroundResource(R.drawable.ic_launcher);
		}
		// �Ա�
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

		// ��ʼ���Ի���
		// dialog = new ProgressDialog(AddFriendActivity.this);
		// dialog.setTitle("��Ӻ���");
		// dialog.setMessage("������Ӻ���...");
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

		// ��Ӻ��Ѽ���
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
				// ��ӳɹ�
				// ���غ����б�AddActivity
				finish();
			} else if (result == ADD_FRIEND_EXIST) {
				// �����Ѵ���
				MyToast.makeText(AddFriendActivity.this, "TA�Ѿ������ĺ�����Ŷ", 1)
						.show();
			} else
				// ���ʧ��
				MyToast.makeText(AddFriendActivity.this, "��Ӻ���ʧ��", 1).show();
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

	/** ���ӷ��� */
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
