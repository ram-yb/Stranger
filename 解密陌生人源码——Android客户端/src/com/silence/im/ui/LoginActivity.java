package com.silence.im.ui;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.XMPPTCPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.MainActivity;
import com.silence.im.R;
import com.silence.im.provider.ContactsProvider.ContactColumns;
import com.silence.im.util.MD5Utils;
import com.silence.im.util.PinYin;
import com.stranger.client.util.GeneralIQ;

public class LoginActivity extends Activity implements View.OnClickListener,
		OnCheckedChangeListener {
	private EditText inAccount, inPassword;
	private CheckBox remenberBox;
	private ImageView avatar;
	private TextView findPass;
	private XMPPConnection connection;
	private CustomProgressDialog progressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 自定义title
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_login);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_login_title);

		avatar = (ImageView) this.findViewById(R.id.activity_login_avatar);
		// 账户输入
		inAccount = (EditText) findViewById(R.id.activity_login_account);
		// 密码输入
		inPassword = (EditText) findViewById(R.id.activity_login_password);
		// 填充账户名
		remenberBox = (CheckBox) this
				.findViewById(R.id.activity_login_check_password);
		remenberBox.setOnCheckedChangeListener(this);
		if (IM.getSetting(SettingActivity.REMEMBER_PASSWORD) == SettingActivity.REMEMBER)
			remenberBox.setChecked(true);
		if (!TextUtils.isEmpty(StringUtils.parseName(IM
				.getString(IM.ACCOUNT_JID))))
			inAccount.setText(StringUtils.parseName(IM
					.getString(IM.ACCOUNT_JID)));
		if (!TextUtils.isEmpty(IM.getString(IM.ACCOUNT_PASSWORD))
				&& remenberBox.isChecked())
			inPassword.setText(IM.getString(IM.ACCOUNT_PASSWORD));
		// inPassword.setText("1234");

		findViewById(R.id.activity_login_btn_login).setOnClickListener(this);
		findViewById(R.id.activity_login_register).setOnClickListener(this);

		// 自动加载头像
		// inPassword.setOnClickListener(this);
		String account = inAccount.getText().toString();
		if (account != null) {
			Drawable drawable = IM.getAvatar(account);
			if (drawable != null)
				avatar.setImageDrawable(drawable);
		}
		inAccount.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String account = inAccount.getText().toString();
					if (account != null) {
						Drawable drawable = IM.getAvatar(account);
						if (drawable != null)
							avatar.setImageDrawable(drawable);
						else
							avatar.setImageResource(R.drawable.mypicture);
					}
				}
			}
		});
		// inPassword.setOnFocusChangeListener(new View.OnFocusChangeListener()
		// {
		//
		// @Override
		// public void onFocusChange(View v, boolean hasFocus) {
		// String account = inAccount.getText().toString();
		// if (account != null) {
		// Drawable drawable = IM.getAvatar(account);
		// if (drawable != null)
		// avatar.setImageDrawable(drawable);
		// else
		// avatar.setImageResource(R.drawable.mypicture);
		// }
		// }
		// });
		// 找回密码
		findPass = (TextView) this
				.findViewById(R.id.activity_login_findpassword);
		findPass.setOnClickListener(this);

		System.out.println("host = " + IM.HOST + "  port = " + IM.PORT
				+ "  imageSize = " + IM.IMAGE_MAX_SIZE);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_login_password:
			String account = inAccount.getText().toString();
			if (account != null) {
				Drawable drawable = IM.getAvatar(account);
				if (drawable != null)
					avatar.setImageDrawable(drawable);
			}
			break;
		case R.id.activity_login_register:
			startActivityForResult(new Intent(this, SignActivity.class),
					RESULT_FIRST_USER);
			break;
		case R.id.activity_login_findpassword:
			String accountsString = inAccount.getText().toString().trim();
			Intent intent = new Intent(LoginActivity.this,
					FindPasswordActivity.class);
			intent.putExtra("account", accountsString);
			startActivity(intent);
			break;
		case R.id.activity_login_btn_login:
			String accountStr = inAccount.getText().toString().trim();
			String passwordStr = inPassword.getText().toString().trim();

			if (TextUtils.isEmpty(accountStr) && TextUtils.isEmpty(passwordStr)) {
				MyToast.makeText(this, "账户和密码不能为空", MyToast.LENGTH_LONG).show();
				return;
			}

			if (TextUtils.isEmpty(accountStr)) {
				MyToast.makeText(this, "请输入账户", MyToast.LENGTH_LONG).show();
				return;
			}

			if (TextUtils.isEmpty(passwordStr)) {
				MyToast.makeText(this, "请输入密码", MyToast.LENGTH_LONG).show();
				return;
			}

			new AsyncTask<String, Void, Integer>() {

				private final int OK = 0;
				private final int ERROR_ACCOUNT = 1;
				private final int ERROR_CONNECT = 2;

				// private ProgressDialog dialog;

				protected void onPreExecute() {
					progressDialog = CustomProgressDialog
							.createDialog(LoginActivity.this);
					progressDialog.setMessage("loading....");
					progressDialog.show();

				}

				protected Integer doInBackground(String... strings) {
					ConnectionConfiguration config = new ConnectionConfiguration(
							IM.HOST, IM.PORT);
					config.setDebuggerEnabled(true);
					// 关闭安全模式
					config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
					connection = new XMPPTCPConnection(config);
					try {
						connection.connect();
						connection.login(strings[0], strings[1], getResources()
								.getString(R.string.app_name));

						IM.putString(IM.ACCOUNT_JID, StringUtils
								.parseBareAddress(connection.getUser()));
						IM.putString(IM.ACCOUNT_PASSWORD, inPassword.getText()
								.toString());
						// 1. 保存账户信息,并启动XMPP后台
						LoginActivity.this.startService(new Intent(
								LoginActivity.this, IMService.class));
						VCard me = new VCard();
						me.load(connection);
						// 2. 保存账户信息
						IM.putString(IM.ACCOUNT_NICKNAME, me.getNickName());
						IM.putString(IM.ACCOUNT_REALNAME, me.getFirstName());
						IM.putString(IM.ACCOUNT_EMAIL, me.getEmailHome());
						IM.putString(IM.ACCOUNT_GENDER, me.getLastName());
						IM.saveAvatar(me.getAvatar(),
								StringUtils.parseName(connection.getUser()));

						// 设置在线状态
						int status = IM.getSetting(IM.ONLINE_STATUS);
						Presence presence = null;
						if (status == IM.AVAILABLE) {
							presence = new Presence(Presence.Type.available);
							connection.sendPacket(presence);
						} else if (status == IM.UNAVAILABLE) {
							presence = new Presence(Presence.Type.unavailable);
							connection.sendPacket(presence);
						}

						// 保存密码
						savePassword(strings[0], strings[1], me.getEmailHome(),
								false);

						// 设置同意所有订阅请求
						Roster roster = connection.getRoster();
						roster.setSubscriptionMode(SubscriptionMode.accept_all);

						// 刷新好友列表
						Collection<RosterEntry> collection = connection
								.getRoster().getEntries();

						Uri uri = Uri
								.parse("content://com.silence.im.provider.ContactsProvider/"
										+ StringUtils.parseName(IM
												.getString(IM.ACCOUNT_JID))
										+ "____contact");
						for (RosterEntry entry : collection) {
							VCard vCard = new VCard();
							try {
								vCard.load(connection, entry.getUser());
							} catch (NoResponseException e) {
								e.printStackTrace();
							} catch (XMPPErrorException e) {
								e.printStackTrace();
							} catch (NotConnectedException e) {
								e.printStackTrace();
							}

							ContentValues values = new ContentValues();
							values.put(ContactColumns.ACCOUNT, entry.getUser());
							values.put(ContactColumns.NICKNAME,
									vCard.getNickName());
							values.put(ContactColumns.REAL_NAME,
									vCard.getFirstName());
							values.put(ContactColumns.EMAIL,
									vCard.getEmailHome());
							values.put(ContactColumns.GENDER,
									vCard.getLastName());
							values.put(ContactColumns.NAME_BY_ME,
									entry.getName());
							String sort = PinYin.getPinYin(StringUtils
									.parseName(entry.getUser()));
							values.put(ContactColumns.SORT, sort);
							values.put(
									ContactColumns.SECTION,
									sort.substring(0, 1).toUpperCase(
											Locale.ENGLISH));
							vCard = null;
							if (IM.im.getContentResolver().update(uri, values,
									ContactColumns.ACCOUNT + "=?",
									new String[] { entry.getUser() }) == 0) {
								IM.im.getContentResolver().insert(uri, values);
							}
						}

						return OK;
					} catch (XMPPException e) {
						e.printStackTrace();
						return ERROR_ACCOUNT;
					} catch (SmackException e) {
						e.printStackTrace();
						return ERROR_CONNECT;
					} catch (IOException e) {
						e.printStackTrace();
						return ERROR_CONNECT;
					} finally {
						connection.disconnect();
					}
				}

				protected void onPostExecute(Integer integer) {
					progressDialog.dismiss();
					switch (integer) {
					case OK:
						// 3. 跳转
						startActivity(new Intent(LoginActivity.this,
								MainActivity.class));
						LoginActivity.this.finish();
						break;
					case ERROR_ACCOUNT:
						MyToast.makeText(LoginActivity.this, "账户验证失败",
								MyToast.LENGTH_LONG).show();
						break;
					case ERROR_CONNECT:
						MyToast.makeText(LoginActivity.this, "网络错误",
								MyToast.LENGTH_LONG).show();
						break;
					}
				}
			}.execute(inAccount.getText().toString().trim(), inPassword
					.getText().toString().trim());
			break;
		}
	}

	// TODO 保存密码
	public boolean savePassword(String accountString, String passwordString,
			String emailString, boolean type) {

		String passwordMD5 = MD5Utils.convertMD5(passwordString);
		GeneralIQ iq = new GeneralIQ("query", "urn:xmpp:rayo:findpassword");
		DocumentFactory factory = new DocumentFactory();
		Element element = factory.createElement("findpassword");
		element.addAttribute("username", accountString);
		element.addAttribute("email", emailString);
		element.addAttribute("password", passwordMD5);
		element.addAttribute("account", IM.getString(IM.ACCOUNT_JID));

		if (type)
			element.addAttribute("type", "insert");
		else
			element.addAttribute("type", "update");

		Element childElement = factory.createElement("query",
				"urn:xmpp:rayo:findpassword");
		childElement.add(element);
		iq.setChildElement(childElement);
		iq.setFrom(IM.getString(IM.ACCOUNT_JID));
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			LoginActivity.this.finish();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked)
			IM.putSetting(SettingActivity.REMEMBER_PASSWORD,
					SettingActivity.REMEMBER);
		else
			IM.putSetting(SettingActivity.REMEMBER_PASSWORD,
					SettingActivity.NO_REMEMBER);
	}
}