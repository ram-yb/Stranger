package com.silence.im.ui;

import java.io.File;
import org.jivesoftware.smack.util.StringUtils;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.R;
import com.silence.im.provider.ContactsProvider;
import com.silence.im.provider.ContactsProvider.ContactColumns;
import com.silence.im.service.Contact;
import com.silence.im.service.IXmppManager;
import com.silence.im.ui.view.RoundedImageView;
import com.silence.im.util.Base64;
import com.silence.im.util.UriUtils;

public class UserActivity extends Activity implements View.OnClickListener {
	// ѡ����Ƭ������
	private static final int selectCode = 123;
	// ���շ�����
	private static final int cameraCode = 124;
	// ϵͳ�ü�������
	private static final int picCode = 125;
	// Intent��ֵkey���˻�
	public static final String EXTRA_ID = "account";

	// ϵͳ����
	private ServiceConnection serviceConnect = new XMPPServiceConnection();
	private IXmppManager xmppManager;
	private boolean isMe;

	// �ؼ�
	private RoundedImageView avatar;
	private EditText accountEditText, nickNameEditText, genderEditText,
			realNameEditText, emailEditText, name_by_mEditText;
	private RadioGroup radioGroup;
	private AlertDialog dialog;

	// �����ļ�
	private File tempFile;
	private byte[] avatarBytes;
	private String account, nickname, gender, genderContent, realname, email,
			name_by_me;

	private ImageButton titleBtn;
	private TextView titleText;

	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// �Զ���ActionBar����
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				UserActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("������Ϣ");

		avatar = (RoundedImageView) findViewById(R.id.activity_user_avatar);
		// �ǳ�
		nickNameEditText = (EditText) findViewById(R.id.activity_user_nickname);
		// ����
		realNameEditText = (EditText) findViewById(R.id.activity_user_realname);
		// ����
		emailEditText = (EditText) findViewById(R.id.activity_user_email);
		// �˻�
		accountEditText = (EditText) findViewById(R.id.activity_user_account);
		// ��ע
		name_by_mEditText = (EditText) this
				.findViewById(R.id.activity_user_name);
		// �Ա�
		genderEditText = (EditText) this
				.findViewById(R.id.activity_user_othergender);
		radioGroup = (RadioGroup) this
				.findViewById(R.id.activity_user_gendergroup);

		genderEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				if (!genderEditText.getText().toString().trim().isEmpty()) {
					radioGroup.check(0);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		});

		for (int i = 0; i < radioGroup.getChildCount(); i++) {
			((RadioButton) radioGroup.getChildAt(i))
					.setOnCheckedChangeListener(new GenderCheckChangeListener());
		}

		account = getIntent().getStringExtra(EXTRA_ID);

		findViewById(R.id.activity_user_commit).setOnClickListener(this);
		avatar.setImageDrawable(IM.getAvatar(StringUtils.parseName(account)));

		if (!TextUtils.isEmpty(account)) {
			if (account.equals(IM.getString(IM.ACCOUNT_JID))) {
				// �Լ�ͷ�����
				accountEditText.setFocusable(false);
				avatar.setOnClickListener(this);
				findViewById(R.id.activity_user_avatar_layout)
						.setOnClickListener(this);
				// �Լ�û�б�ע
				findViewById(R.id.activity_user_name_layout).setVisibility(
						View.GONE);
				nickNameEditText.setText(IM.getString(IM.ACCOUNT_NICKNAME));
				realNameEditText.setText(IM.getString(IM.ACCOUNT_REALNAME));
				emailEditText.setText(IM.getString(IM.ACCOUNT_EMAIL));
				String genderTemp = IM.getString(IM.ACCOUNT_GENDER);
				setGender(genderTemp);
				isMe = true;
			} else {
				// �������޸ĺ����ǳ�
				nickNameEditText.setFocusable(false);
				realNameEditText.setFocusable(false);
				emailEditText.setFocusable(false);
				genderEditText.setFocusable(false);
				accountEditText.setFocusable(false);
				for (int i = 0; i < radioGroup.getChildCount(); i++) {
					((RadioButton) radioGroup.getChildAt(i)).setEnabled(false);
				}

				Uri uri = Uri
						.parse("content://com.jersuen.im.provider.ContactsProvider/"
								+ StringUtils.parseName(IM
										.getString(IM.ACCOUNT_JID))
								+ "____contact");
				Cursor cursor = getContentResolver().query(uri, null,
						ContactsProvider.ContactColumns.ACCOUNT + " = ?",
						new String[] { account }, null);

				if (cursor != null && cursor.moveToFirst()) {
					name_by_me = cursor.getString(cursor
							.getColumnIndex(ContactColumns.NAME_BY_ME));
					// ����ͨѶ¼��ı�ע
					name_by_mEditText.setText(name_by_me);
				}
				isMe = false;
			}
		}
		accountEditText.setText(StringUtils.parseName(account));
		avatarBytes = null;
	}

	private void setGender(String genderTemp) {
		if (genderTemp.contains(":")) {
			int index = genderTemp.indexOf(":");
			gender = genderTemp.substring(0, index);
			if (genderTemp.length() - index > 1)
				genderContent = genderTemp.substring(index + 1,
						genderTemp.length());
			System.out.println("gender = " + gender + "  genderContent = "
					+ genderContent);
			if (ContactsProvider.MAN.equals(gender)) {
				((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
				// genderEditText.setFocusable(false);
			} else if (ContactsProvider.WOMAN.equals(gender)) {
				((RadioButton) radioGroup.getChildAt(1)).setChecked(true);
				// genderEditText.setFocusable(false);
			} else if (ContactsProvider.OTHER.equals(gender)) {
				radioGroup.check(0);
				genderEditText.setText(genderContent);
			}
		} else {
			gender = genderTemp;
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		// ͷ���¼�
		case R.id.activity_user_avatar:
		case R.id.activity_user_avatar_layout:
			avatarBytes = null;
			showDialog();
			break;
		// �޸��¼�
		case R.id.activity_user_commit:
			if (isMe) {
				nickname = nickNameEditText.getText().toString().trim();
				realname = realNameEditText.getText().toString().trim();
				email = emailEditText.getText().toString().trim();

				genderContent = genderEditText.getText().toString().trim();
				String tempGender = gender + ":" + genderContent;
				if (TextUtils.isEmpty(nickname)) {
					MyToast.makeText(UserActivity.this, "�ǳƲ���Ϊ��Ŷ��",
							MyToast.LENGTH_SHORT).show();
					return;
				}
				// �޸��Լ�����Ƭ
				boolean result;
				Contact contact = new Contact();
				contact.nickname = nickname;
				contact.account = account;
				contact.email = email;
				contact.realname = realname;
				contact.gender = tempGender;
				if (avatarBytes != null)
					contact.avatar = Base64.encodeBytes(avatarBytes);

				try {
					result = xmppManager.setVCard(contact);
					System.out.println("result = " + result + "  account = "
							+ contact.account + "  name = " + contact.nickname);
				} catch (RemoteException e) {
					e.printStackTrace();
					result = false;
				}
				if (result) {
					// �޸ĳɹ���ˢ�±���
					IM.putString(IM.ACCOUNT_EMAIL, email);
					IM.putString(IM.ACCOUNT_NICKNAME, nickname);
					IM.putString(IM.ACCOUNT_REALNAME, realname);
					IM.putString(IM.ACCOUNT_GENDER, tempGender);
					if (avatarBytes != null)
						IM.saveAvatar(avatarBytes,
								StringUtils.parseName(account));
				}
				MyToast.makeText(UserActivity.this,
						(result) ? "�޸���Ƭ�ɹ�" : "�޸���Ƭʧ��", MyToast.LENGTH_LONG)
						.show();
			} else {
				name_by_me = name_by_mEditText.getText().toString().trim();
				// �޸ĺ��ѵı�ע
				boolean result;
				try {
					result = xmppManager
							.setRosterEntryName(account, name_by_me);
				} catch (RemoteException e) {
					e.printStackTrace();
					result = false;
				}

				if (result) {
					Uri uri = Uri
							.parse("content://com.jersuen.im.provider.ContactsProvider/"
									+ StringUtils.parseName(IM
											.getString(IM.ACCOUNT_JID))
									+ "____contact");
					ContentValues values = new ContentValues();
					values.put(ContactColumns.NAME_BY_ME, name_by_me);
					int count = getContentResolver().update(uri, values,
							ContactColumns.ACCOUNT + " = ?",
							new String[] { account });
					MyToast.makeText(UserActivity.this,
							(count > 0) ? "�޸ı�ע�ɹ�" : "�޸ı�עʧ��",
							MyToast.LENGTH_LONG).show();
				}
			}
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
		// ����
		case cameraCode:
			// ��ȡ��Ƭ,��ʼ�ü�
			IM.doCropPhoto(UserActivity.this, Uri.fromFile(tempFile), picCode);
			break;
		// ͼ��
		case selectCode:
			Uri uri = data.getData();
			String path = UriUtils.getPath(UserActivity.this, uri);
			System.out.println("path = " + path);
			if (!TextUtils.isEmpty(path)) {
				// �ļ���׺�ж�
				if (path.endsWith("jpg") || path.endsWith("png")
						|| path.endsWith("gif") || path.endsWith("jpeg")
						|| path.endsWith("bmp") || path.endsWith("ico")) {
					// ��ȡ��Ƭ,��ʼ�ü�
					Uri newUri = Uri.parse("file:///" + path); // ������·��ת��ΪURL
					IM.doCropPhoto(UserActivity.this, newUri, picCode);
				}
			}
			break;
		// �ü�
		case picCode:
			System.out.println("UserActivity-->>����");
			if (data != null) {
				Bitmap photoPic = data.getParcelableExtra("data");
				if (photoPic != null) {
					avatar.setImageDrawable(IM.Bitmap2Drawable(photoPic));
					avatarBytes = IM.Bitmap2Bytes(photoPic);
					// ����ͷ��
					// IM.saveAvatar(avatarBytes,
					// StringUtils.parseName(account));
				}
			}
			break;
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
			if (!isMe) {
				try {
					Contact contact = xmppManager.getVCard(account);
					if (contact != null) {
						nickname = contact.nickname;
						if (!TextUtils.isEmpty(nickname)) {
							nickNameEditText.setText(nickname);
						}
						realNameEditText.setText(contact.realname);
						emailEditText.setText(contact.email);
						String tempGender = contact.gender;
						setGender(tempGender);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		public void onServiceDisconnected(ComponentName componentName) {
			xmppManager = null;
		}
	}

	// private void showDialog() {
	// if (dialog == null) {
	// dialog = new AlertDialog.Builder(this)
	// .setTitle("ѡ����Ƭ")
	// .setItems(R.array.select_photo_items,
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int which) {
	// switch (which) {
	// case 0:
	// tempFile = IM.getCameraFile();
	// // ��������
	// Intent intentCamera = new Intent(
	// MediaStore.ACTION_IMAGE_CAPTURE);
	// intentCamera.putExtra(
	// MediaStore.EXTRA_OUTPUT,
	// Uri.fromFile(tempFile));
	// startActivityForResult(intentCamera,
	// cameraCode);
	// break;
	// case 1:
	// // ���ͼ��
	// // Intent intentSelect = new Intent();
	// // intentSelect.setType("image/*");
	// // intentSelect
	// // .setAction(Intent.ACTION_GET_CONTENT);
	// // startActivityForResult(intentSelect,
	// // selectCode);
	// int version = IM.getSetting("KITKAT");
	// Intent intent = null;
	// if (version == 1) {
	// intent = new Intent(
	// Intent.ACTION_OPEN_DOCUMENT); // 4.4�Ƽ��ô˷�ʽ��4.4���µ�API��Ҫ�ټ���
	// intent.addCategory(Intent.CATEGORY_OPENABLE);
	// intent.setType("image/*");
	// } else
	// intent = new Intent(
	// Intent.ACTION_PICK,
	// android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	// startActivityForResult(intent,
	// selectCode);
	// break;
	// }
	// }
	// }).create();
	// }
	// dialog.show();
	// }

	private void showDialog() {
		/* ��ʼ����ͨ�Ի��򡣲�������ʽ */
		final Dialog selectDialog = new Dialog(this, R.style.dialog);
		selectDialog.setCancelable(true);
		selectDialog.setCanceledOnTouchOutside(true);
		/* ������ͨ�Ի���Ĳ��� */
		selectDialog.setContentView(R.layout.dialog_upavater);

		TextView take = (TextView) selectDialog
				.findViewById(R.id.dialog_upavater_take);
		TextView file = (TextView) selectDialog
				.findViewById(R.id.dialog_upavater_file);

		take.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				tempFile = IM.getCameraFile();
				// ��������
				Intent intentCamera = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(tempFile));
				startActivityForResult(intentCamera, cameraCode);
				selectDialog.dismiss();
			}
		});

		file.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// ���ͼ��
				// Intent intentSelect = new Intent();
				// intentSelect.setType("image/*");
				// intentSelect
				// .setAction(Intent.ACTION_GET_CONTENT);
				// startActivityForResult(intentSelect,
				// selectCode);
				int version = IM.getSetting("KITKAT");
				Intent intent = null;
				if (version == 1) {
					intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // 4.4�Ƽ��ô˷�ʽ��4.4���µ�API��Ҫ�ټ���
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("image/*");
				} else
					intent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, selectCode);
				selectDialog.dismiss();
			}
		});
		Window dialogWindow = selectDialog.getWindow();
		WindowManager m = dialogWindow.getWindowManager();
		Display d = m.getDefaultDisplay(); // ��ȡ��Ļ������
		WindowManager.LayoutParams p = dialogWindow.getAttributes(); //
		// ��ȡ�Ի���ǰ�Ĳ���ֵ
		p.height = (int) (d.getHeight() * 0.25); // �߶�����Ϊ��Ļ��0.25
		p.width = (int) (d.getWidth() * 0.65); // �������Ϊ��Ļ��0.65
		dialogWindow.setAttributes(p);

		selectDialog.show();// ��ʾ�Ի���

	}

	private class GenderCheckChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				switch (buttonView.getId()) {
				case R.id.activity_user_gendergroup_man:
					gender = "man";
					break;
				case R.id.activity_user_gendergroup_woman:
					gender = "woman";
					break;
				default:
					gender = "other";
					break;
				}
				genderEditText.setText("");
				System.out.println("GenderCheckChangeListener -->>gender = "
						+ gender);
			}
		}

	}
}