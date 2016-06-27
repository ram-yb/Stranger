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

	private IXmppManager xmppManager;// 连接管理器
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
		actionBar.setCustomView(R.layout.activity_common_title);// 自定义ActionBar布局
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				FindPasswordActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("找回密码");

		account = (EditText) this
				.findViewById(R.id.activity_findpassword_account);
		email = (EditText) this.findViewById(R.id.activity_findpassword_email);
		commit = (Button) this.findViewById(R.id.activity_findpassword_commit);

		String accountString = getIntent().getStringExtra("account");
		account.setText(accountString);
		commit.setOnClickListener(this);
		//
		// dialog = new ProgressDialog(FindPasswordActivity.this);
		// dialog.setTitle("找回密码");
		// dialog.setMessage("正在联系服务器，请稍候...");
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
					// 通知刷新UI
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
					MyToast.makeText(FindPasswordActivity.this, "服务器出错",
							MyToast.LENGTH_SHORT).show();
				else if (emailResult == SUCCESS) {
					MyToast.makeText(FindPasswordActivity.this, "成功",
							MyToast.LENGTH_SHORT).show();
					showDialog();
				} else if (emailResult == NO_USER)
					MyToast.makeText(FindPasswordActivity.this, "没有此用户",
							MyToast.LENGTH_SHORT).show();
				else if (emailResult == EMAIL_UNAVAILABLE)
					MyToast.makeText(FindPasswordActivity.this, "邮箱匹配不正确",
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
	// builder.setTitle("找回密码");
	// builder.setMessage("验证码已发到邮箱，请前往绑定邮箱查看后修改密码");
	// builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// startActivity(new Intent(FindPasswordActivity.this,
	// ChangePasswordActivity.class).putExtra("account",
	// accountString));
	// FindPasswordActivity.this.finish();
	// }
	// }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// }
	// });
	// alertDialog = builder.create();
	// alertDialog.show();
	// }

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

	private void showDialog() {

		/* 初始化普通对话框。并设置样式 */
		selectDialog = new Dialog(this, R.style.dialog);
		selectDialog.setCancelable(true);
		selectDialog.setCanceledOnTouchOutside(true);
		/* 设置普通对话框的布局 */
		selectDialog.setContentView(R.layout.dialog_delete);

		Button cacel = (Button) selectDialog.findViewById(R.id.dialog_cacel);
		Button delete = (Button) selectDialog.findViewById(R.id.dialog_delete);
		delete.setText("确定");
		TextView textView = (TextView) selectDialog
				.findViewById(R.id.dialog_text);

		textView.setText("验证码已发到邮箱，请前往绑定邮箱查看后修改密码");

		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 删除好友
				selectDialog.dismiss();// 隐藏对话框
				startActivity(new Intent(FindPasswordActivity.this,
						ChangePasswordActivity.class).putExtra("account",
						accountString));
				FindPasswordActivity.this.finish();
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
	}
}
