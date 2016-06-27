package com.silence.im.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.R;
import com.silence.im.service.IXmppManager;

@SuppressLint("NewApi")
public class FindPasswordActivity extends Activity implements OnClickListener {

	public static final int SUCCESS = 1;
	public static final int NO_USER = 2;
	public static final int EMAIL_UNAVAILABLE = 3;
	public static final int SERVER_ERROR = 4;
	public static final int FAIL = 5;

	private IXmppManager xmppManager;// ���ӹ�����
	private ServiceConnection serviceConnect = new XMPPServiceConnection();

	private EditText account, email;
	private Button commit;
	// private ProgressDialog dialog;
	private CustomProgressDialog progressDialog;

	private int emailResult;

	private Dialog selectDialog;

	private String accountString, emailString;

	private ImageButton titleBtn;
	private TextView titleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_findpassword);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// �Զ���ActionBar����
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				FindPasswordActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("�һ�����");

		account = (EditText) this
				.findViewById(R.id.activity_findpassword_account);
		email = (EditText) this.findViewById(R.id.activity_findpassword_email);
		commit = (Button) this.findViewById(R.id.activity_findpassword_commit);

		String accountString = getIntent().getStringExtra("account");
		account.setText(accountString);
		commit.setOnClickListener(this);
		//
		// dialog = new ProgressDialog(FindPasswordActivity.this);
		// dialog.setTitle("�һ�����");
		// dialog.setMessage("������ϵ�����������Ժ�...");
		// dialog.setCancelable(false);
		//
		progressDialog = CustomProgressDialog.createDialog(this);
		progressDialog.setMessage("loading....");
		progressDialog.setCancelable(false);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_findpassword_commit:
			progressDialog.show();
			accountString = account.getText().toString().trim();
			emailString = email.getText().toString().trim();
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						emailResult = xmppManager.findPasswordByEmail(
								accountString, emailString, null, false);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					// ֪ͨˢ��UI
					handler.sendEmptyMessage(0);
				}
			}).start();
			break;
		default:
			break;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			progressDialog.dismiss();
			switch (msg.what) {
			case 0:
				if (emailResult == SERVER_ERROR)
					MyToast.makeText(FindPasswordActivity.this, "����������",
							MyToast.LENGTH_SHORT).show();
				else if (emailResult == SUCCESS) {
					MyToast.makeText(FindPasswordActivity.this, "�ɹ�",
							MyToast.LENGTH_SHORT).show();
					showDialog();
				} else if (emailResult == NO_USER)
					MyToast.makeText(FindPasswordActivity.this, "û�д��û�",
							MyToast.LENGTH_SHORT).show();
				else if (emailResult == EMAIL_UNAVAILABLE)
					MyToast.makeText(FindPasswordActivity.this, "����ƥ�䲻��ȷ",
							MyToast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};

	// private void showSuccessDialog() {
	// AlertDialog.Builder builder = new AlertDialog.Builder(
	// FindPasswordActivity.this);
	// builder.setTitle("�һ�����");
	// builder.setMessage("��֤���ѷ������䣬��ǰ��������鿴���޸�����");
	// builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// startActivity(new Intent(FindPasswordActivity.this,
	// ChangePasswordActivity.class).putExtra("account",
	// accountString));
	// FindPasswordActivity.this.finish();
	// }
	// }).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// }
	// });
	// alertDialog = builder.create();
	// alertDialog.show();
	// }

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

	private void showDialog() {

		/* ��ʼ����ͨ�Ի��򡣲�������ʽ */
		selectDialog = new Dialog(this, R.style.dialog);
		selectDialog.setCancelable(true);
		selectDialog.setCanceledOnTouchOutside(true);
		/* ������ͨ�Ի���Ĳ��� */
		selectDialog.setContentView(R.layout.dialog_delete);

		Button cacel = (Button) selectDialog.findViewById(R.id.dialog_cacel);
		Button delete = (Button) selectDialog.findViewById(R.id.dialog_delete);
		delete.setText("ȷ��");
		TextView textView = (TextView) selectDialog
				.findViewById(R.id.dialog_text);

		textView.setText("��֤���ѷ������䣬��ǰ��������鿴���޸�����");

		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ɾ������
				selectDialog.dismiss();// ���ضԻ���
				startActivity(new Intent(FindPasswordActivity.this,
						ChangePasswordActivity.class).putExtra("account",
						accountString));
				FindPasswordActivity.this.finish();
			}
		});

		cacel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				selectDialog.dismiss();// ���ضԻ���
			}
		});
		Window dialogWindow = selectDialog.getWindow();
		WindowManager m = dialogWindow.getWindowManager();
		Display d = m.getDefaultDisplay(); // ��ȡ��Ļ������
		WindowManager.LayoutParams p = dialogWindow.getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
		p.height = (int) (d.getHeight() * 0.3); // �߶�����Ϊ��Ļ��0.3
		p.width = (int) (d.getWidth() * 0.65); // �������Ϊ��Ļ��0.65
		dialogWindow.setAttributes(p);

		selectDialog.show();// ��ʾ�Ի���
	}
}
