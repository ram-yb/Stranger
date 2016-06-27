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
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.R;
import com.silence.im.service.IXmppManager;
import com.silence.im.ui.adapter.SettingAdapter;

public class SettingActivity extends Activity implements OnItemClickListener,
		View.OnClickListener {

	private static final int USER_INFO = 1;
	private static final int LINE_STATUS = 2;
	private static final int NOTIFICATION = 5;
	private static final int VOICE = 4;
	private static final int DELETE_CHAT = 7;
	private static final int DELETE_IMAGE = 8;
	private static final int DELETE_CACHE = 9;
	private static final int CLEAR_SETTING = 11;
	private static final int ABOUT = 13;
	// ��������
	public static final String SETTING_VOICE = "setting_voice";
	public static final int VOICE_OPEN = 0;
	public static final int VOICE_CLOSE = 1;

	// ֪ͨ����
	public static final String SETTING_NOTIFICATION = "setting_notification";
	public static final int NOTIFICATION_OPEN = 0;
	public static final int NOTIFICATION_CLOSE = 1;
	// ��ס����
	public static final String REMEMBER_PASSWORD = "remember_password";
	public static final int REMEMBER = 1;
	public static final int NO_REMEMBER = 0;

	private ListView setting;
	private SettingAdapter adapter;
	private CustomProgressDialog progressDialog1;

	private Dialog selectDialog;

	private ServiceConnection serviceConnect = new SettingServiceConnect();
	private IXmppManager xmppManager;

	private ImageButton titleBtn;
	private TextView titleText;

	public LayoutInflater mInflater;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// �Զ���ActionBar����
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				SettingActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("��  ��");
		setting = (ListView) this.findViewById(R.id.activity_setting_listview);
		adapter = new SettingAdapter(SettingActivity.this);
		adapter.setOnClickListener(this);
		setting.setAdapter(adapter);
		setting.setOnItemClickListener(this);

		mInflater = LayoutInflater.from(this);
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case USER_INFO:// ɾ���˻�
			startActivity(new Intent(SettingActivity.this, UserActivity.class)
					.putExtra(UserActivity.EXTRA_ID,
							IM.getString(IM.ACCOUNT_JID)));
			break;
		case LINE_STATUS:// ����״̬

			/* ��ʼ����ͨ�Ի��򡣲�������ʽ */
			selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* ������ͨ�Ի���Ĳ��� */
			selectDialog.setContentView(R.layout.dialog_text2);

			TextView textView34 = (TextView) selectDialog
					.findViewById(R.id.title_choices);
			TextView textView31 = (TextView) selectDialog
					.findViewById(R.id.choice_one_text);
			TextView textView32 = (TextView) selectDialog
					.findViewById(R.id.choice_two_text);
			TextView textView33 = (TextView) selectDialog
					.findViewById(R.id.choice_three_text);

			textView33.setText("����״̬");
			textView31.setText("����~@^_^@~");
			textView32.setText("����o(��_��)o");
			textView33.setText("ע��~@^_^@~");
			if (IM.getSetting(IM.ONLINE_STATUS) == IM.AVAILABLE) {
				textView31.setTextColor(Color.WHITE);
			} else if (IM.getSetting(IM.ONLINE_STATUS) == IM.UNAVAILABLE) {
				textView32.setTextColor(Color.WHITE);
			} else {
				textView33.setTextColor(Color.WHITE);
			}
			textView31.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					new SettingHandlerTask().execute(LINE_STATUS, 0);
					selectDialog.dismiss();
				}
			});
			textView32.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					new SettingHandlerTask().execute(LINE_STATUS, 1);
					selectDialog.dismiss();
				}
			});
			textView33.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					new SettingHandlerTask().execute(LINE_STATUS, 2);
					selectDialog.dismiss();
				}
			});

			Window dialogWindow31 = selectDialog.getWindow();
			WindowManager m31 = dialogWindow31.getWindowManager();
			Display d31 = m31.getDefaultDisplay(); // ��ȡ��Ļ������
			WindowManager.LayoutParams p31 = dialogWindow31.getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
			p31.height = (int) (d31.getHeight() * 0.34); // �߶�����Ϊ��Ļ��0.1
			p31.width = (int) (d31.getWidth() * 0.65); // �������Ϊ��Ļ��0.40
			dialogWindow31.setAttributes(p31);
			selectDialog.show();// ��ʾ�Ի���
			//
			// CharSequence[] status = { "����", "����", "ע��" };
			// builder = new AlertDialog.Builder(SettingActivity.this);
			// builder.setTitle("����״̬");
			// builder.setSingleChoiceItems(status,
			// IM.getSetting(IM.ONLINE_STATUS),
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// new SettingHandlerTask()
			// .execute(LINE_STATUS, which);
			// dialog.dismiss();
			// }
			// });
			// dialog = builder.create();
			// dialog.show();
			break;
		case VOICE:// ����

			/* ��ʼ����ͨ�Ի��򡣲�������ʽ */
			selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* ������ͨ�Ի���Ĳ��� */
			selectDialog.setContentView(R.layout.dialog_text);

			TextView textView23 = (TextView) selectDialog
					.findViewById(R.id.title_choices);
			TextView textView21 = (TextView) selectDialog
					.findViewById(R.id.choice_one_text);
			TextView textView22 = (TextView) selectDialog
					.findViewById(R.id.choice_two_text);

			textView23.setText("������������");
			textView21.setText("����o(��_��)o");
			textView22.setText("�ر�^_^```");
			if (IM.getSetting(SETTING_VOICE) == VOICE_OPEN) {
				textView21.setTextColor(Color.WHITE);
			} else {
				textView22.setTextColor(Color.WHITE);
			}
			textView21.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					IM.putSetting(SETTING_VOICE, VOICE_OPEN);
					selectDialog.dismiss();
				}
			});
			textView22.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					IM.putSetting(SETTING_VOICE, VOICE_CLOSE);
					selectDialog.dismiss();
				}
			});

			Window dialogWindow21 = selectDialog.getWindow();
			WindowManager m21 = dialogWindow21.getWindowManager();
			Display d21 = m21.getDefaultDisplay(); // ��ȡ��Ļ������
			WindowManager.LayoutParams p21 = dialogWindow21.getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
			p21.height = (int) (d21.getHeight() * 0.25); // �߶�����Ϊ��Ļ��0.1
			p21.width = (int) (d21.getWidth() * 0.65); // �������Ϊ��Ļ��0.40
			dialogWindow21.setAttributes(p21);
			selectDialog.show();// ��ʾ�Ի���

			// CharSequence[] voice = { "����", "�ر�" };
			// builder = new AlertDialog.Builder(SettingActivity.this);
			// builder.setTitle("��������");
			// builder.setSingleChoiceItems(voice, IM.getSetting(SETTING_VOICE),
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// if (which == 0)
			// IM.putSetting(SETTING_VOICE, VOICE_OPEN);
			// else if (which == 1)
			// IM.putSetting(SETTING_VOICE, VOICE_CLOSE);
			// dialog.dismiss();
			// }
			// });
			// dialog = builder.create();
			//
			// dialog.show();
			break;
		case NOTIFICATION:// ֪ͨ
			/* ��ʼ����ͨ�Ի��򡣲�������ʽ */
			selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* ������ͨ�Ի���Ĳ��� */
			selectDialog.setContentView(R.layout.dialog_text);

			TextView textView13 = (TextView) selectDialog
					.findViewById(R.id.title_choices);
			TextView textView11 = (TextView) selectDialog
					.findViewById(R.id.choice_one_text);
			TextView textView12 = (TextView) selectDialog
					.findViewById(R.id.choice_two_text);

			textView13.setText("֪ͨ������");
			textView11.setText("����o(��_��)o");
			textView12.setText("�ر�^_^```");
			if (IM.getSetting(SETTING_NOTIFICATION) == NOTIFICATION_OPEN) {
				textView11.setTextColor(Color.WHITE);
			} else {
				textView12.setTextColor(Color.WHITE);
			}
			textView11.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					IM.putSetting(SETTING_NOTIFICATION, NOTIFICATION_OPEN);
					selectDialog.dismiss();
				}
			});
			textView12.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {
					IM.putSetting(SETTING_NOTIFICATION, NOTIFICATION_CLOSE);
					selectDialog.dismiss();
				}
			});

			Window dialogWindow11 = selectDialog.getWindow();
			WindowManager m11 = dialogWindow11.getWindowManager();
			Display d11 = m11.getDefaultDisplay(); // ��ȡ��Ļ������
			WindowManager.LayoutParams p11 = dialogWindow11.getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
			p11.height = (int) (d11.getHeight() * 0.25); // �߶�����Ϊ��Ļ��0.1
			p11.width = (int) (d11.getWidth() * 0.65); // �������Ϊ��Ļ��0.40
			dialogWindow11.setAttributes(p11);
			selectDialog.show();// ��ʾ�Ի���

			// CharSequence[] Notification = { "����", "�ر�" };
			// builder = new AlertDialog.Builder(SettingActivity.this);
			// builder.setTitle("֪ͨ������");
			// builder.setSingleChoiceItems(Notification,
			// IM.getSetting(SETTING_NOTIFICATION),
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// if (which == 0)
			// IM.putSetting(SETTING_NOTIFICATION,
			// NOTIFICATION_OPEN);
			// else if (which == 1)
			// IM.putSetting(SETTING_NOTIFICATION,
			// NOTIFICATION_CLOSE);
			// dialog.dismiss();
			// }
			// });
			// dialog = builder.create();
			// dialog.show();
			break;
		case DELETE_CHAT:// ��������¼

			/* ��ʼ����ͨ�Ի��򡣲�������ʽ */
			selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* ������ͨ�Ի���Ĳ��� */
			selectDialog.setContentView(R.layout.dialog_delete);

			Button cacel = (Button) selectDialog
					.findViewById(R.id.dialog_cacel);
			Button delete = (Button) selectDialog
					.findViewById(R.id.dialog_delete);
			TextView textView = (TextView) selectDialog
					.findViewById(R.id.dialog_text);

			textView.setText("ȷ����������¼��");
			delete.setText("ȷ��");

			delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					selectDialog.dismiss();
					new SettingHandlerTask().execute(DELETE_CHAT, 2);
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

			// builder = new AlertDialog.Builder(SettingActivity.this);
			// builder.setTitle("��ʾ");
			// builder.setMessage("ȷ����������¼��");
			// builder.setPositiveButton("ȷ��",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// new SettingHandlerTask()
			// .execute(DELETE_CHAT, which);
			// }
			// })
			// .setNegativeButton("ȡ��",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog,
			// int which) {
			// dialog.dismiss();
			// }
			// })
			// .setOnKeyListener(new DialogInterface.OnKeyListener() {
			//
			// @Override
			// public boolean onKey(DialogInterface dialog,
			// int keyCode, KeyEvent event) {
			// if (keyCode == KeyEvent.KEYCODE_BACK)
			// dialog.dismiss();
			// return false;
			// }
			// });
			// dialog = builder.create();
			// dialog.show();
			break;
		case DELETE_IMAGE:// �������ͼƬ

			/* ��ʼ����ͨ�Ի��򡣲�������ʽ */
			selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* ������ͨ�Ի���Ĳ��� */
			selectDialog.setContentView(R.layout.dialog_delete);

			Button cacel4 = (Button) selectDialog
					.findViewById(R.id.dialog_cacel);
			Button delete4 = (Button) selectDialog
					.findViewById(R.id.dialog_delete);
			TextView textView4 = (TextView) selectDialog
					.findViewById(R.id.dialog_text);

			textView4.setText("ȷ���������ͼƬ��");
			delete4.setText("ȷ��");

			delete4.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					selectDialog.dismiss();
					new SettingHandlerTask().execute(DELETE_IMAGE, 2);
				}

			});

			cacel4.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectDialog.dismiss();// ���ضԻ���
				}
			});
			Window dialogWindow4 = selectDialog.getWindow();
			WindowManager m4 = dialogWindow4.getWindowManager();
			Display d4 = m4.getDefaultDisplay(); // ��ȡ��Ļ������
			WindowManager.LayoutParams p4 = dialogWindow4.getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
			p4.height = (int) (d4.getHeight() * 0.3); // �߶�����Ϊ��Ļ��0.3
			p4.width = (int) (d4.getWidth() * 0.65); // �������Ϊ��Ļ��0.65
			dialogWindow4.setAttributes(p4);
			selectDialog.show();// ��ʾ�Ի���

			// builder = new AlertDialog.Builder(SettingActivity.this);
			// builder.setTitle("��ʾ");
			// builder.setMessage("ȷ���������ͼƬ��");
			// builder.setPositiveButton("ȷ��",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// new SettingHandlerTask().execute(DELETE_IMAGE,
			// which);
			// }
			// })
			// .setNegativeButton("ȡ��",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog,
			// int which) {
			// dialog.dismiss();
			// }
			// })
			// .setOnKeyListener(new DialogInterface.OnKeyListener() {
			//
			// @Override
			// public boolean onKey(DialogInterface dialog,
			// int keyCode, KeyEvent event) {
			// if (keyCode == KeyEvent.KEYCODE_BACK)
			// dialog.dismiss();
			// return false;
			// }
			// });
			// dialog = builder.create();
			// dialog.show();
			break;
		case DELETE_CACHE:// ��ջ���
		{
			/* ��ʼ����ͨ�Ի��򡣲�������ʽ */

			selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* ������ͨ�Ի���Ĳ��� */
			selectDialog.setContentView(R.layout.dialog_delete);

			Button cacel1 = (Button) selectDialog
					.findViewById(R.id.dialog_cacel);
			Button delete1 = (Button) selectDialog
					.findViewById(R.id.dialog_delete);
			TextView textView1 = (TextView) selectDialog
					.findViewById(R.id.dialog_text);

			textView1.setText("ȷ����ջ�����");
			delete1.setText("ȷ��");

			delete1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					selectDialog.dismiss();
					new SettingHandlerTask().execute(DELETE_CACHE, 2);

				}

			});

			cacel1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectDialog.dismiss();// ���ضԻ���
				}
			});
			Window dialogWindow1 = selectDialog.getWindow();
			WindowManager m1 = dialogWindow1.getWindowManager();
			Display d1 = m1.getDefaultDisplay(); // ��ȡ��Ļ������
			WindowManager.LayoutParams p1 = dialogWindow1.getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
			p1.height = (int) (d1.getHeight() * 0.3); // �߶�����Ϊ��Ļ��0.3
			p1.width = (int) (d1.getWidth() * 0.65); // �������Ϊ��Ļ��0.65
			dialogWindow1.setAttributes(p1);
			selectDialog.show();// ��ʾ�Ի���
		}
			// builder = new AlertDialog.Builder(SettingActivity.this);
			// builder.setTitle("��ʾ");
			// builder.setMessage("ȷ����ջ�����");
			// builder.setPositiveButton("ȷ��",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// new SettingHandlerTask().execute(DELETE_CACHE, 2);
			// }
			// })
			// .setNegativeButton("ȡ��",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog,
			// int which) {
			// dialog.dismiss();
			// }
			// })
			// .setOnKeyListener(new DialogInterface.OnKeyListener() {
			//
			// @Override
			// public boolean onKey(DialogInterface dialog,
			// int keyCode, KeyEvent event) {
			// if (keyCode == KeyEvent.KEYCODE_BACK)
			// dialog.dismiss();
			// return false;
			// }
			// });
			// dialog = builder.create();
			// dialog.show();
			break;

		case CLEAR_SETTING:// �ָ���ʼ����

			/* ��ʼ����ͨ�Ի��򡣲�������ʽ */
			selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* ������ͨ�Ի���Ĳ��� */
			selectDialog.setContentView(R.layout.dialog_delete);

			Button cacel3 = (Button) selectDialog
					.findViewById(R.id.dialog_cacel);
			Button delete3 = (Button) selectDialog
					.findViewById(R.id.dialog_delete);
			TextView textView3 = (TextView) selectDialog
					.findViewById(R.id.dialog_text);

			textView3.setText("ȷ��Ҫ�ָ���ʼ������");
			delete3.setText("ȷ��");

			delete3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					// �ж�Android�汾��4.4�汾�Ƿֽ���
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
						IM.putSetting("androidVersion",
								android.os.Build.VERSION.SDK_INT);// 4.4�汾
						IM.putSetting("KITKAT", 1);
					} else {
						IM.putSetting("androidVersion",
								android.os.Build.VERSION.SDK_INT);// 4.4���°汾
						IM.putSetting("KITKAT", 0);
					}
					// ��ʼ�����ò���
					IM.putSetting(SettingActivity.SETTING_VOICE,
							SettingActivity.VOICE_OPEN);
					IM.putSetting(SettingActivity.SETTING_NOTIFICATION,
							SettingActivity.NOTIFICATION_OPEN);
					IM.putSetting(SettingActivity.REMEMBER_PASSWORD,
							SettingActivity.NO_REMEMBER);
					selectDialog.dismiss();
					MyToast.makeText(SettingActivity.this, "�ɹ��ָ���ʼ����",
							MyToast.LENGTH_SHORT).show();
				}

			});

			cacel3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectDialog.dismiss();// ���ضԻ���
				}
			});
			Window dialogWindow3 = selectDialog.getWindow();
			WindowManager m3 = dialogWindow3.getWindowManager();
			Display d3 = m3.getDefaultDisplay(); // ��ȡ��Ļ������
			WindowManager.LayoutParams p3 = dialogWindow3.getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
			p3.height = (int) (d3.getHeight() * 0.3); // �߶�����Ϊ��Ļ��0.3
			p3.width = (int) (d3.getWidth() * 0.65); // �������Ϊ��Ļ��0.65
			dialogWindow3.setAttributes(p3);
			selectDialog.show();// ��ʾ�Ի���

			// builder = new AlertDialog.Builder(SettingActivity.this);
			// builder.setTitle("��ʾ");
			// builder.setMessage("ȷ��Ҫ�ָ���ʼ������");
			// builder.setNegativeButton("ȡ��",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// dialog.dismiss();
			// }
			// })
			// .setPositiveButton("ȷ��",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog,
			// int which) {
			// // �ж�Android�汾��4.4�汾�Ƿֽ���
			// if (android.os.Build.VERSION.SDK_INT >=
			// android.os.Build.VERSION_CODES.KITKAT) {
			// IM.putSetting(
			// "androidVersion",
			// android.os.Build.VERSION.SDK_INT);// 4.4�汾
			// IM.putSetting("KITKAT", 1);
			// } else {
			// IM.putSetting(
			// "androidVersion",
			// android.os.Build.VERSION.SDK_INT);// 4.4���°汾
			// IM.putSetting("KITKAT", 0);
			// }
			// // ��ʼ�����ò���
			// IM.putSetting(
			// SettingActivity.SETTING_VOICE,
			// SettingActivity.VOICE_OPEN);
			// IM.putSetting(
			// SettingActivity.SETTING_NOTIFICATION,
			// SettingActivity.NOTIFICATION_OPEN);
			// IM.putSetting(
			// SettingActivity.REMEMBER_PASSWORD,
			// SettingActivity.NO_REMEMBER);
			// dialog.dismiss();
			// MyToast.makeText(SettingActivity.this,
			// "�ɹ��ָ���ʼ����", MyToast.LENGTH_SHORT)
			// .show();
			// }
			// })
			// .setOnKeyListener(new DialogInterface.OnKeyListener() {
			//
			// @Override
			// public boolean onKey(DialogInterface dialog,
			// int keyCode, KeyEvent event) {
			// if (keyCode == KeyEvent.KEYCODE_BACK)
			// dialog.dismiss();
			// return false;
			// }
			// });
			// dialog = builder.create();
			// dialog.show();
			break;
		case ABOUT:// ����
			startActivity(new Intent(SettingActivity.this, AboutActivity.class));
			break;
		default:
			break;
		}
	}

	private class SettingHandlerTask extends
			AsyncTask<Integer, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... params) {
			switch (params[0]) {
			case LINE_STATUS:
				try {
					if (params[1] == 0) {
						xmppManager.setOnlineStatus(IM.AVAILABLE);
					} else if (params[1] == 1) {
						xmppManager.setOnlineStatus(IM.UNAVAILABLE);
					} else if (params[1] == 2) {
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									xmppManager.logout();
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
						}).start();
						startActivity(new Intent(SettingActivity.this,
								LoginActivity.class));
						SettingActivity.this.finish();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case DELETE_CHAT:
				Uri uri = Uri
						.parse("content://com.silence.im.provider.SMSProvider/"
								+ StringUtils.parseName(IM
										.getString(IM.ACCOUNT_JID)) + "____sms");
				SettingActivity.this.getContentResolver().delete(uri, null,
						null);
				deleteDirectory(IM.IMAGE_PATH);
				deleteDirectory(IM.AUDIO_PATH);
				break;
			case DELETE_IMAGE:
				deleteDirectory(IM.IMAGE_PATH);
				break;
			case DELETE_CACHE:
				deleteDirectory(IM.CACHE_PATH);
				break;
			default:
				break;
			}
			return null;
		}

		private void deleteDirectory(String path) {
			File dir = new File(path);
			File[] files = null;
			if (dir.isDirectory()) {
				files = dir.listFiles();
				for (File temp : files) {
					if (temp.exists())
						temp.delete();
				}
			}
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

	/**
	 * ��������
	 */
	private class SettingServiceConnect implements ServiceConnection {

		public void onServiceConnected(ComponentName componentName,
				IBinder iBinder) {
			xmppManager = IXmppManager.Stub.asInterface(iBinder);
		}

		public void onServiceDisconnected(ComponentName componentName) {
			xmppManager = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_setting_account_delete:

			/* ��ʼ����ͨ�Ի��򡣲�������ʽ */
			selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* ������ͨ�Ի���Ĳ��� */
			selectDialog.setContentView(R.layout.dialog_delete);

			Button cacel2 = (Button) selectDialog
					.findViewById(R.id.dialog_cacel);
			Button delete2 = (Button) selectDialog
					.findViewById(R.id.dialog_delete);
			TextView textView2 = (TextView) selectDialog
					.findViewById(R.id.dialog_text);

			textView2.setText("ȷʵҪɾ�����˻�������ʻ�������");
			delete2.setText("ȷ��");

			delete2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectDialog.dismiss();// ���ضԻ���

					new AsyncTask<String, Integer, Boolean>() {
						protected void onPreExecute() {
							selectDialog.dismiss();// ���ضԻ���
							// progressDialog = ProgressDialog
							// .show(SettingActivity.this, "��ʾ",
							// "����ɾ���˻������Ժ�...");
							// progressDialog.setCancelable(false);
							// progressDialog.show();

							progressDialog1 = CustomProgressDialog
									.createDialog(SettingActivity.this);
							progressDialog1.setMessage("loading....");
							progressDialog1.setCancelable(false);
							progressDialog1.show();
						};

						@Override
						protected Boolean doInBackground(String... params) {
							Uri uri = Uri
									.parse("content://com.silence.im.provider.SMSProvider/"
											+ StringUtils.parseName(IM
													.getString(IM.ACCOUNT_JID))
											+ "____sms");
							getContentResolver().delete(uri, null, null);

							Uri uri1 = Uri
									.parse("content://com.silence.im.provider.ContactsProvider/"
											+ StringUtils.parseName(IM
													.getString(IM.ACCOUNT_JID))
											+ "____contact");
							getContentResolver().delete(uri1, null, null);
							try {
								xmppManager.logout();
							} catch (RemoteException e) {
								e.printStackTrace();
							}
							IM.putString(IM.ACCOUNT_JID, "contact");
							IM.putString(IM.ACCOUNT_PASSWORD, null);
							IM.putString(IM.ACCOUNT_NICKNAME, null);
							IM.putString(IM.ACCOUNT_REALNAME, null);
							IM.putString(IM.ACCOUNT_EMAIL, null);
							IM.putString(IM.ACCOUNT_GENDER, null);
							IM.putSetting(SettingActivity.REMEMBER_PASSWORD,
									SettingActivity.NO_REMEMBER);
							return true;
						}

						protected void onPostExecute(Boolean result) {
							progressDialog1.dismiss();
							startActivity(new Intent(SettingActivity.this,
									LoginActivity.class));
						};
					}.execute();

				}
			});

			cacel2.setOnClickListener(new OnClickListener() {

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

			selectDialog.show();// ��ʾ�Ի���

			// builder = new AlertDialog.Builder(SettingActivity.this);
			// builder.setTitle("��ʾ");
			// builder.setMessage("ȷʵҪɾ�����˻�������ʻ�������");
			// builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			//
			// @Override
			// public boolean onKey(DialogInterface dialog, int keyCode,
			// KeyEvent event) {
			// if (keyCode == KeyEvent.KEYCODE_BACK)
			// dialog.dismiss();
			// return false;
			// }
			// });
			// builder.setPositiveButton("ȷ��",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			//
			// new AsyncTask<String, Integer, Boolean>() {
			// protected void onPreExecute() {
			// SettingActivity.this.dialog.dismiss();
			// progressDialog = ProgressDialog.show(
			// SettingActivity.this, "��ʾ",
			// "����ɾ���˻������Ժ�...");
			// progressDialog.setCancelable(false);
			// progressDialog.show();
			// };
			//
			// @Override
			// protected Boolean doInBackground(
			// String... params) {
			// Uri uri =
			// Uri.parse("content://com.jersuen.im.provider.SMSProvider/"
			// + StringUtils.parseName(IM
			// .getString(IM.ACCOUNT_JID))
			// + "____sms");
			// getContentResolver()
			// .delete(uri, null, null);
			//
			// Uri uri1 =
			// Uri.parse("content://com.jersuen.im.provider.ContactsProvider/"
			// + StringUtils.parseName(IM
			// .getString(IM.ACCOUNT_JID))
			// + "____contact");
			// getContentResolver().delete(uri1, null,
			// null);
			// try {
			// xmppManager.logout();
			// } catch (RemoteException e) {
			// e.printStackTrace();
			// }
			// IM.putString(IM.ACCOUNT_JID, "contact");
			// IM.putString(IM.ACCOUNT_PASSWORD, null);
			// IM.putString(IM.ACCOUNT_NICKNAME, null);
			// IM.putString(IM.ACCOUNT_REALNAME, null);
			// IM.putString(IM.ACCOUNT_EMAIL, null);
			// IM.putString(IM.ACCOUNT_GENDER, null);
			// IM.putSetting(
			// SettingActivity.REMEMBER_PASSWORD,
			// SettingActivity.NO_REMEMBER);
			// return true;
			// }
			//
			// protected void onPostExecute(Boolean result) {
			// progressDialog.dismiss();
			// startActivity(new Intent(
			// SettingActivity.this,
			// LoginActivity.class));
			// };
			// }.execute();
			// }
			// });
			// builder.setNegativeButton("ȡ��",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// dialog.dismiss();
			// }
			// });
			// dialog = builder.create();
			// dialog.show();
			break;
		case R.id.activity_setting_account_avatar:
			startActivity(new Intent(SettingActivity.this, UserActivity.class)
					.putExtra(UserActivity.EXTRA_ID,
							IM.getString(IM.ACCOUNT_JID)));
			break;
		default:
			break;
		}
	}

}
