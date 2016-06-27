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

	private IXmppManager xmppManager;// ���ӹ�����
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
		actionBar.setCustomView(R.layout.activity_common_title);// �Զ���ActionBar����
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				ChangePasswordActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("�޸�����");

		password = (EditText) this
				.findViewById(R.id.activity_changepassword__password);
		passwordConfir = (EditText) this
				.findViewById(R.id.activity_changepassword__password_again);
		codeEditText = (EditText) this
				.findViewById(R.id.activity_changepassword_code);
		commit = (Button) this
				.findViewById(R.id.activity_changepassword__commit);
		setTitle("�޸�����");

		account = getIntent().getStringExtra("account");

		commit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				password1 = password.getText().toString().trim();
				String password2 = passwordConfir.getText().toString().trim();
				String code = codeEditText.getText().toString();
				if (TextUtils.isEmpty(password.getText().toString())) {
					MyToast.makeText(ChangePasswordActivity.this, "���벻��Ϊ��Ŷ",
							MyToast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(passwordConfir.getText().toString())) {
					MyToast.makeText(ChangePasswordActivity.this, "ȷ�����벻��Ϊ��Ŷ",
							MyToast.LENGTH_SHORT).show();
					return;
				}
				if (!password1.equals(password2)) {
					MyToast.makeText(ChangePasswordActivity.this, "��������Ҫһ��Ŷ",
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
		// dialog.setTitle("�޸�����");
		// dialog.setMessage("�����޸����룬���Ժ�...");
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
		protected void onPostExecute(Integer result) {// ˢ��UI����
			progressDialog.dismiss();
			switch (result) {
			case FindPasswordActivity.SUCCESS:
				// startActivity(new Intent(ChangePasswordActivity.this,
				// LoginActivity.class));
				ChangePasswordActivity.this.finish();
				break;
			case FindPasswordActivity.FAIL:
				MyToast.makeText(ChangePasswordActivity.this, "��֤�����������...",
						MyToast.LENGTH_SHORT).show();
				break;
			default:
				MyToast.makeText(ChangePasswordActivity.this, "�������쳣��������...",
						MyToast.LENGTH_SHORT).show();
				break;
			}
		}
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
