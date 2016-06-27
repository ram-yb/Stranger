package com.silence.im.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.R;
import com.silence.im.service.IXmppManager;

public class ChangePasswordActivity extends Activity {

	private IXmppManager xmppManager;// 连接管理器
	private ServiceConnection serviceConnect = new XMPPServiceConnection();

	private EditText password, passwordConfir, codeEditText;
	private Button commit;
	private String account;
	private String password1;
	private CustomProgressDialog progressDialog;

	private ImageButton titleBtn;
	private TextView titleText;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_changepassword);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// 自定义ActionBar布局
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				ChangePasswordActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("修改密码");

		password = (EditText) this
				.findViewById(R.id.activity_changepassword__password);
		passwordConfir = (EditText) this
				.findViewById(R.id.activity_changepassword__password_again);
		codeEditText = (EditText) this
				.findViewById(R.id.activity_changepassword_code);
		commit = (Button) this
				.findViewById(R.id.activity_changepassword__commit);
		setTitle("修改密码");

		account = getIntent().getStringExtra("account");

		commit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				password1 = password.getText().toString().trim();
				String password2 = passwordConfir.getText().toString().trim();
				String code = codeEditText.getText().toString();
				if (TextUtils.isEmpty(password.getText().toString())) {
					MyToast.makeText(ChangePasswordActivity.this, "密码不能为空哦",
							MyToast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(passwordConfir.getText().toString())) {
					MyToast.makeText(ChangePasswordActivity.this, "确认密码不能为空哦",
							MyToast.LENGTH_SHORT).show();
					return;
				}
				if (!password1.equals(password2)) {
					MyToast.makeText(ChangePasswordActivity.this, "两次密码要一致哦",
							MyToast.LENGTH_SHORT).show();
					return;
				}
				if (code != null)
					code = code.trim();
				new ChangePasswordTask().execute(code, password1, password2);
			}
		});
		//
		// dialog = new ProgressDialog(ChangePasswordActivity.this);
		// dialog.setTitle("修改密码");
		// dialog.setMessage("正在修改密码，请稍候...");
		//
		progressDialog = CustomProgressDialog.createDialog(this);
		progressDialog.setMessage("loading....");

	}

	private class ChangePasswordTask extends
			AsyncTask<String, Integer, Integer> {
		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(String... params) {
			int result = FindPasswordActivity.SERVER_ERROR;
			try {
				result = xmppManager.findPasswordByEmail(account, null,
						params[0], true);
				if (result == FindPasswordActivity.SUCCESS) {
					String password3 = IM.getString(IM.TEMP);
					IM.putString(IM.ACCOUNT_PASSWORD, password3);
					xmppManager.connect();
					xmppManager.login();
					boolean res = xmppManager.changePassword(password1);
					if (res) {
						xmppManager.logout();
						// IM.putString(IM.ACCOUNT_PASSWORD, password1);
						// startService(new Intent(ChangePasswordActivity.this,
						// IMService.class));
					} else
						return FindPasswordActivity.SERVER_ERROR;
				}
			} catch (RemoteException e) {
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {// 刷新UI操作
			progressDialog.dismiss();
			switch (result) {
			case FindPasswordActivity.SUCCESS:
				// startActivity(new Intent(ChangePasswordActivity.this,
				// LoginActivity.class));
				ChangePasswordActivity.this.finish();
				break;
			case FindPasswordActivity.FAIL:
				MyToast.makeText(ChangePasswordActivity.this, "验证码错误，请重试...",
						MyToast.LENGTH_SHORT).show();
				break;
			default:
				MyToast.makeText(ChangePasswordActivity.this, "服务器异常，请重试...",
						MyToast.LENGTH_SHORT).show();
				break;
			}
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
		unbindService(serviceConnect);
		super.onDestroy();
	}
}
