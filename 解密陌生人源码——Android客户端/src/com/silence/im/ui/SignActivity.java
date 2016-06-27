package com.silence.im.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPTCPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.MainActivity;
import com.silence.im.R;
import com.silence.im.provider.ContactsProvider;
import com.silence.im.ui.adapter.SignViewAdapter;
import com.silence.im.ui.view.RoundedImageView;
import com.silence.im.util.MD5Utils;
import com.silence.im.util.UriUtils;
import com.stranger.client.util.GeneralIQ;

/**
 * 注册
 * 
 * @author JerSuen
 */
// TODO标记处添加Email和真实姓名
public class SignActivity extends Activity implements View.OnClickListener {
	/**
	 * 选择照片返回码
	 */
	private static final int selectCode = 123;

	/**
	 * 拍照返回码
	 */
	private static final int cameraCode = 124;
	/**
	 * 系统裁剪返回码
	 */
	private static final int picCode = 125;

	// 拍照文件
	private File tempFile;
	private ViewPager viewPager;
	private SignViewAdapter adapter;
	private View createAccount, perfectAccount, uploadAvatar;
	private RoundedImageView avatar;
	private String accountJid, accountPassword, accountNickName, accountSex,
			accountEmail;
	private XMPPConnection connection;
	private AccountManager accountManager;
	private byte[] avatarBytes;
	private TextView titleText;
	private ImageButton titleBtn;

	private EditText sexEditText;
	private RadioGroup radioGroup;

	private String accountStr = null;
	private String passwordStr = null;

	Dialog selectDialog;

	private CustomProgressDialog progressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getActionBar().setHomeButtonEnabled(true);

		// 自定义title
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_sign);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_sign_title);
		titleBtn = (ImageButton) findViewById(R.id.activity_sign_title_btn_back);
		titleBtn.setOnClickListener(this);
		titleText = (TextView) findViewById(R.id.activity_sign_title_text);

		viewPager = (ViewPager) findViewById(R.id.activity_sign_view_pager);
		// 禁止滑动
		viewPager.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View view, MotionEvent motionEvent) {
				return true;
			}
		});

		createAccount = getLayoutInflater().inflate(
				R.layout.activity_sign_view_create_account, null);
		uploadAvatar = getLayoutInflater().inflate(
				R.layout.activity_sign_view_upload_avatar, null);
		perfectAccount = getLayoutInflater().inflate(
				R.layout.activity_sign_view_perfect_account, null);
		avatar = (RoundedImageView) uploadAvatar
				.findViewById(R.id.activity_sign_view_upload_avatar_avatar);

		// 性别监听
		sexEditText = (EditText) perfectAccount
				.findViewById(R.id.activity_sign_view_perfect_account_radioother_content);
		radioGroup = (RadioGroup) perfectAccount
				.findViewById(R.id.activity_sign_view_perfect_account_radiogroup);
		radioGroup.check(0);

		sexEditText.addTextChangedListener(new TextWatcher() {

			@SuppressLint("NewApi")
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {

				if (!sexEditText.getText().toString().trim().isEmpty()) {
					radioGroup.check(0);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		});

		for (int i = 0; i < radioGroup.getChildCount(); i++) {
			((RadioButton) radioGroup.getChildAt(i))
					.setOnCheckedChangeListener(new GenderCheckChangeListener());
		}

		List<View> views = new ArrayList<View>();
		views.add(createAccount);
		views.add(perfectAccount);
		views.add(uploadAvatar);

		adapter = new SignViewAdapter(views);
		// 适配器内容监听器
		adapter.setOnSignViewClickListener(this);
		viewPager.setAdapter(adapter);
	}

	@SuppressLint("NewApi")
	public void onClick(final View v) {
		switch (v.getId()) {

		case R.id.activity_sign_title_btn_back:
			onBackPressed();
			break;
		// 创建账户布局监听
		case R.id.activity_sign_view_create_account_commit:
			TextView account = (TextView) createAccount
					.findViewById(R.id.activity_sign_view_create_account_account);
			TextView password = (TextView) createAccount
					.findViewById(R.id.activity_sign_view_create_account_password);
			TextView passwordagin = (TextView) createAccount
					.findViewById(R.id.activity_sign_view_create_account_password_agin);
			accountStr = account.getText().toString().trim();
			passwordStr = password.getText().toString().trim();
			String passwordaginStr = passwordagin.getText().toString().trim();

			if (TextUtils.isEmpty(accountStr)) {
				MyToast.makeText(SignActivity.this, "请检查账户o(∩_∩)o",
						MyToast.LENGTH_LONG).show();
				return;
			}

			if (TextUtils.isEmpty(passwordStr) || passwordStr.length() < 6) {
				MyToast.makeText(SignActivity.this, "密码不能为空，并且长度大于6位~@^_^@~",
						MyToast.LENGTH_LONG).show();
				return;
			}
			if (!passwordStr.equals(passwordaginStr)) {
				MyToast.makeText(SignActivity.this, "两次输入的密码不一样哦~~",
						MyToast.LENGTH_LONG).show();
				return;
			}

			// 创建账户任务
			new AsyncTask<String, Void, Boolean>() {
				// private ProgressDialog dialog;

				protected void onPreExecute() {
					progressDialog = CustomProgressDialog
							.createDialog(SignActivity.this);
					progressDialog.setMessage("loading....");
					progressDialog.show();
				}

				protected Boolean doInBackground(String... strings) {
					connection = ConfigConnection();
					try {
						connection.connect();
						accountManager = AccountManager.getInstance(connection);
						Map<String, String> map = new HashMap<String, String>();
						map.put("name", strings[0]);
						accountManager.createAccount(strings[0], strings[1],
								map);
						connection.login(strings[0], strings[1]);
						accountJid = StringUtils.parseBareAddress(connection
								.getUser());
						accountPassword = strings[1];

						// 设置在线
						Presence presence = new Presence(
								Presence.Type.available);
						connection.sendPacket(presence);

						// 设置同意所有订阅请求
						Roster roster = connection.getRoster();
						roster.setSubscriptionMode(SubscriptionMode.accept_all);

						return true;
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				}

				protected void onPostExecute(Boolean aBoolean) {
					progressDialog.dismiss();
					if (aBoolean) {
						viewPager.setCurrentItem(1);
						titleText.setText("请完善资料");
						InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						inputMethodManager.hideSoftInputFromWindow(
								SignActivity.this.getCurrentFocus()
										.getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
					} else {
						MyToast.makeText(SignActivity.this, "服务器好像不愿意哦",
								MyToast.LENGTH_LONG).show();
					}
				}
			}.execute(accountStr, passwordStr);

			break;
		// 完善资料布局监听
		case R.id.activity_sign_view_perfect_account_commit:
			TextView nickname = (TextView) perfectAccount
					.findViewById(R.id.activity_sign_view_perfect_account_nickname);
			accountNickName = nickname.getText().toString().trim();
			TextView email = (TextView) perfectAccount
					.findViewById(R.id.activity_sign_view_perfect_account_email);
			accountEmail = email.getText().toString().trim();

			if (TextUtils.isEmpty(accountNickName)) {
				MyToast.makeText(SignActivity.this, "昵称不能胡来",
						MyToast.LENGTH_LONG).show();
				return;
			}

			if (radioGroup.getCheckedRadioButtonId() == R.id.activity_sign_view_perfect_account_radioman) {

				accountSex = ContactsProvider.MAN + ":";
			} else if (radioGroup.getCheckedRadioButtonId() == R.id.activity_sign_view_perfect_account_radiowoman) {

				accountSex = ContactsProvider.WOMAN + ":";
			} else if (!sexEditText.getText().toString().trim().isEmpty()) {
				accountSex = ContactsProvider.OTHER
						+ ":"
						+ ((EditText) findViewById(R.id.activity_sign_view_perfect_account_radioother_content))
								.getText().toString();
			} else if (sexEditText.getText().toString().trim().isEmpty()
					&& radioGroup.getCheckedRadioButtonId() == 0) {
				MyToast.makeText(SignActivity.this, "性别不可不选哟~~",
						MyToast.LENGTH_LONG).show();
				return;
			}

			viewPager.setCurrentItem(2);
			titleText.setText("请上传头像");
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(SignActivity.this
					.getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
			break;
		case R.id.activity_sign_view_upload_avatar_avatar:
		case R.id.activity_sign_view_upload_avatar_layout:
			showDialog();
			break;
		case R.id.activity_sign_view_upload_avatar_commit:
			if (avatarBytes == null) {
				showDialog();
			} else {
				// 上传头像，完成注册任务
				new AsyncTask<Void, Void, Boolean>() {
					// private ProgressDialog dialog;

					protected void onPreExecute() {
						progressDialog = CustomProgressDialog
								.createDialog(SignActivity.this);
						progressDialog.setMessage("loading....");
						progressDialog.show();
					}

					// TODO
					protected Boolean doInBackground(Void... voids) {
						try {
							VCard vCard = new VCard();
							vCard.load(connection);
							vCard.setNickName(accountNickName);
							vCard.setLastName(accountSex);
							vCard.setAvatar(avatarBytes);
							vCard.setEmailHome(accountEmail);
							vCard.save(connection);

							return true;
						} catch (Exception e) {
							e.printStackTrace();
						}
						return false;
					}

					// TODO
					protected void onPostExecute(Boolean aBoolean) {
						progressDialog.dismiss();
						if (aBoolean) {
							// 1. 保存账户
							IM.putString(IM.ACCOUNT_JID, accountJid);
							IM.putString(IM.ACCOUNT_PASSWORD, accountPassword);
							IM.putString(IM.ACCOUNT_NICKNAME, accountNickName);
							IM.putString(IM.ACCOUNT_GENDER, accountSex);
							IM.putString(IM.ACCOUNT_EMAIL, accountEmail);
							IM.saveAvatar(avatarBytes,
									StringUtils.parseName(accountJid));

							// 2. 启动XMPP后台
							startService(new Intent(SignActivity.this,
									IMService.class));
							// 3. 跳转
							startActivity(new Intent(SignActivity.this,
									MainActivity.class));
							// 4.保存密码
							savePassword(accountStr, passwordStr, accountEmail,
									true);
							// 5. 销毁登陆页面
							setResult(RESULT_OK);
							finish();
						} else {
							MyToast.makeText(SignActivity.this,
									"额,就差这一步了,再试一次", MyToast.LENGTH_LONG)
									.show();
						}
					}
				}.execute();
			}
			break;
		}
	}

	// TODO
	public boolean savePassword(String accountString, String passwordString,
			String emailString, boolean type) {

		String passwordMD5 = MD5Utils.convertMD5(passwordString);
		GeneralIQ iq = new GeneralIQ("query", "urn:xmpp:rayo:findpassword");
		DocumentFactory factory = new DocumentFactory();
		Element element = factory.createElement("findpassword");
		element.addAttribute("username", accountString);
		element.addAttribute("email", emailString);
		element.addAttribute("password", passwordMD5);
		element.addAttribute("account", accountJid);

		if (type)
			element.addAttribute("type", "insert");
		else
			element.addAttribute("type", "update");

		Element childElement = factory.createElement("query",
				"urn:xmpp:rayo:findpassword");
		childElement.add(element);
		iq.setChildElement(childElement);
		iq.setFrom(accountJid);
		iq.setType(IQ.Type.SET);

		try {
			connection.sendPacket(iq);
		} catch (NotConnectedException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println("iq -->> " + iq.toXML());

		PacketCollector collector = connection
				.createPacketCollector(new AndFilter(new PacketIDFilter(iq
						.getPacketID()), new PacketTypeFilter(IQ.class)));
		// /获取服务器发回的附近的人信息
		GeneralIQ result = (GeneralIQ) collector.nextResult(SmackConfiguration
				.getDefaultPacketReplyTimeout() * 2);
		collector.cancel();
		System.out.println("XMPPManager -->>result" + result.toXML());

		Element resultElement = result.getChildElement()
				.element("findpassword");
		int status = Integer.parseInt(resultElement.attributeValue("status"));
		System.out.println("status = " + status);

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("onActivityResult");
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
		// 拍照
		case cameraCode:
			// 获取照片,开始裁剪
			IM.doCropPhoto(SignActivity.this, Uri.fromFile(tempFile), picCode);
			break;
		// 图库
		case selectCode:
			Uri uri = data.getData();
			String path = UriUtils.getPath(SignActivity.this, uri);
			System.out.println("path = " + path);
			if (!TextUtils.isEmpty(path)) {
				// 文件后缀判断
				if (path.endsWith("jpg") || path.endsWith("png")
						|| path.endsWith("gif") || path.endsWith("jpeg")
						|| path.endsWith("bmp") || path.endsWith("ico")) {
					// 获取照片,开始裁剪
					Uri newUri = Uri.parse("file:///" + path); // 将绝对路径转换为URL
					IM.doCropPhoto(SignActivity.this, newUri, picCode);
				}
			}
			break;
		// 裁剪
		case picCode:
			System.out.println("UserActivity-->>剪裁");
			if (data != null) {
				Bitmap photoPic = data.getParcelableExtra("data");
				if (photoPic != null) {
					avatar.setImageDrawable(IM.Bitmap2Drawable(photoPic));
					avatarBytes = IM.Bitmap2Bytes(photoPic);
					// 保存头像
					// IM.saveAvatar(avatarBytes,
					// StringUtils.parseName(accountJid));
				}
			}
			break;
		}
	}

	private void showDialog() {
		if (selectDialog == null) {
			// dialog = new AlertDialog.Builder(this)
			// .setTitle("选择照片")
			// .setItems(R.array.select_photo_items,
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int which) {
			// switch (which) {
			// case 0:
			// tempFile = IM.getCameraFile();
			// // 进入拍照
			// Intent intentCamera = new Intent(
			// MediaStore.ACTION_IMAGE_CAPTURE);
			// intentCamera.putExtra(
			// MediaStore.EXTRA_OUTPUT,
			// Uri.fromFile(tempFile));
			// startActivityForResult(intentCamera,
			// cameraCode);
			// break;
			// case 1:
			// // 浏览图库
			// // Intent intentSelect = new Intent();
			// // intentSelect.setType("image/*");
			// // intentSelect
			// // .setAction(Intent.ACTION_GET_CONTENT);
			// // startActivityForResult(intentSelect,
			// // selectCode);
			//
			// int version = IM.getSetting("KITKAT");
			// Intent intent = null;
			// if (version == 1) {
			// intent = new Intent(
			// Intent.ACTION_OPEN_DOCUMENT); // 4.4推荐用此方式，4.4以下的API需要再兼容
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
			//
			//

			/* 初始化普通对话框。并设置样式 */
			final Dialog selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* 设置普通对话框的布局 */
			selectDialog.setContentView(R.layout.dialog_upavater);

			TextView take = (TextView) selectDialog
					.findViewById(R.id.dialog_upavater_take);
			TextView file = (TextView) selectDialog
					.findViewById(R.id.dialog_upavater_file);

			take.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					tempFile = IM.getCameraFile();
					// 进入拍照
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

					// 浏览图库
					// Intent intentSelect = new Intent();
					// intentSelect.setType("image/*");
					// intentSelect
					// .setAction(Intent.ACTION_GET_CONTENT);
					// startActivityForResult(intentSelect,
					// selectCode);
					int version = IM.getSetting("KITKAT");
					Intent intent = null;
					if (version == 1) {
						intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // 4.4推荐用此方式，4.4以下的API需要再兼容
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
			Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
			WindowManager.LayoutParams p = dialogWindow.getAttributes(); //
			// 获取对话框当前的参数值
			p.height = (int) (d.getHeight() * 0.25); // 高度设置为屏幕的0.25
			p.width = (int) (d.getWidth() * 0.65); // 宽度设置为屏幕的0.65
			dialogWindow.setAttributes(p);

			selectDialog.show();// 显示对话框
		}
	}

	private XMPPConnection ConfigConnection() {
		ConnectionConfiguration configuration = new ConnectionConfiguration(
				IM.HOST, IM.PORT);
		configuration.setDebuggerEnabled(true);
		// 关闭安全模式
		configuration
				.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		return new XMPPTCPConnection(configuration);
	}

	public void onBackPressed() {
		// 退出注册判断
		if (TextUtils.isEmpty(accountJid)) {
			finish();
		} else {
			/* 初始化普通对话框。并设置样式 */
			selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* 设置普通对话框的布局 */
			selectDialog.setContentView(R.layout.dialog_delete);

			Button cacel = (Button) selectDialog
					.findViewById(R.id.dialog_cacel);
			Button delete = (Button) selectDialog
					.findViewById(R.id.dialog_delete);
			delete.setText("确定");
			TextView textView = (TextView) selectDialog
					.findViewById(R.id.dialog_text);

			textView.setText("确定放弃注册吗？");

			delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectDialog.dismiss();// 隐藏对话框
					try {
						// 删除账号，必须要登录注册账号
						accountManager.deleteAccount();
					} catch (Exception e) {
						e.printStackTrace();
					}
					// 关闭链接
					connection.disconnect();
					createAccount = null;
					connection = null;
					SignActivity.this.finish();
				}
			});

			cacel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectDialog.dismiss();// 隐藏对话框
				}
			});
			Window dialogWindow = selectDialog.getWindow();
			WindowManager m = dialogWindow.getWindowManager();
			Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
			WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
			p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3
			p.width = (int) (d.getWidth() * 0.65); // 宽度设置为屏幕的0.65
			dialogWindow.setAttributes(p);

			selectDialog.show();// 显示对话框

			// new AlertDialog.Builder(SignActivity.this,
			// AlertDialog.THEME_HOLO_LIGHT)
			// .setTitle("确定放弃注册")
			// .setPositiveButton("确定",
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			// try {
			// // 删除账号，必须要登录注册账号
			// accountManager.deleteAccount();
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// // 关闭链接
			// connection.disconnect();
			// createAccount = null;
			// connection = null;
			// SignActivity.this.finish();
			// }
			// })
			// .setNegativeButton("取消",
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			// }
			// }).create().show();
		}
	}

	private class GenderCheckChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {

				sexEditText.setText("");

			}
		}

	}
}