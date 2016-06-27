package com.silence.im;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.jivesoftware.smack.util.StringUtils;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.silence.im.service.IXmppManager;
import com.silence.im.service.LoginAsyncTask;
import com.silence.im.ui.AddActivity;
import com.silence.im.ui.ChatImageActivity;
import com.silence.im.ui.MyToast;
import com.silence.im.ui.SettingActivity;
import com.silence.im.ui.ShakeActivity;
import com.silence.im.ui.UserActivity;
import com.silence.im.ui.adapter.FragmentAdapter;
import com.silence.im.ui.adapter.MenuGroupAdapter;

/**
 * ������
 * 
 * @author JerSuen
 */
public class MainActivity extends FragmentActivity implements
		View.OnClickListener {

	private ViewPager viewPager;
	private FragmentPagerAdapter adapter;
	// private FragmentPagerAdapter adapter;
	private RadioGroup radioGroup;
	private ServiceConnection serviceConnect = new LoginServiceConnect();
	private static IXmppManager xmppManager;
	private LoginAsyncTask loginTask = new LoginTask();

	// title ������
	private TextView titleText;
	private Button titleBtn;

	// Menu���
	private PopupWindow popupWindow;
	private ListView lv_group;
	private View view;
	private ArrayList<String> groups;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		forceShowOverflowMenu();

		// �Զ���title
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_main_title);

		// title���ʵ����
		titleBtn = (Button) findViewById(R.id.activity_main_title_btn);
		titleText = (TextView) findViewById(R.id.activity_main_title_text);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				showWindow(arg0);
			}
		});

		radioGroup = (RadioGroup) findViewById(R.id.group);

		viewPager = (ViewPager) findViewById(R.id.activity_main_pager);
		adapter = new FragmentAdapter(getSupportFragmentManager());
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(1);
		radioGroup.check(R.id.activity_main_btn_contact);
		viewPager.setOnPageChangeListener(new PageChangeListener());

		findViewById(R.id.activity_main_btn_contact).setOnClickListener(this);
		findViewById(R.id.activity_main_btn_session).setOnClickListener(this);
		findViewById(R.id.activity_main_btntwo).setOnClickListener(this);

		// initMenu();

	}

	// private void initMenu() {
	// menuView = View.inflate(this, R.layout.main_menu, null);
	// // ����AlertDialog
	// menuDialog = new AlertDialog.Builder(this).create();
	// menuDialog.setView(menuView);
	// menuDialog.setOnKeyListener(new OnKeyListener() {
	// public boolean onKey(DialogInterface dialog, int keyCode,
	// KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_MENU)// ��������
	// dialog.dismiss();
	// return false;
	// }
	// });
	//
	// menuGrid = (GridView) menuView.findViewById(R.id.main_mune_gridview);
	// menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
	// /** ����menuѡ�� **/
	// menuGrid.setOnItemClickListener(new OnItemClickListener() {
	// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
	// long arg3) {
	// switch (arg2) {
	// case ITEM_SEARCH:// ����
	// startActivity(new Intent(MainActivity.this,
	// AddActivity.class));
	// menuDialog.dismiss();
	// break;
	// case ITEM_USER_INFO:// �û���Ϣ
	// startActivity(new Intent(MainActivity.this,
	// UserActivity.class).putExtra(UserActivity.EXTRA_ID,
	// IM.getString(IM.ACCOUNT_JID)));
	// menuDialog.dismiss();
	// break;
	// case ITEM_REFRESH:// ˢ��
	// menuDialog.dismiss();
	// final ProgressDialog dialog = ProgressDialog.show(
	// MainActivity.this, "��ʾ", "����ˢ��...");
	// dialog.show();
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// refreshContact();
	// dialog.dismiss();
	// }
	// }).start();
	// break;
	// case ITEM_IMAGE:// ����ͼƬ
	// startActivity(new Intent(MainActivity.this,
	// ChatImageActivity.class));
	// menuDialog.dismiss();
	// break;
	// case ITEM_DOWN_MANAGER:// ����
	// startActivity(new Intent(MainActivity.this,
	// DownFileActivity.class));
	// menuDialog.dismiss();
	// break;
	// case ITEM_SETTING:// ����
	// startActivity(new Intent(MainActivity.this,
	// SettingActivity.class));
	// menuDialog.dismiss();
	// break;
	// }
	// }
	// });
	// }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");// ���봴��һ��
		return super.onCreateOptionsMenu(menu);
	}

	private SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageResourceArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				R.layout.main_menu_item,
				new String[] { "itemImage", "itemText" }, new int[] {
						R.id.item_image, R.id.item_text });
		return simperAdapter;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// if (menuDialog == null) {
		// menuDialog = new AlertDialog.Builder(this).setView(menuView).show();
		// } else {
		// menuDialog.show();
		// }
		showWindow(findViewById(R.id.activity_main_title_btn));
		return false;// ����Ϊtrue ����ʾϵͳmenu
	}

	private void refreshContact() {
		Uri uri = Uri
				.parse("content://com.silence.im.provider.ContactsProvider/"
						+ StringUtils.parseName(IM.getString(IM.ACCOUNT_JID))
						+ "____contact");
		MainActivity.this.getContentResolver().notifyChange(uri, null);
		uri = Uri.parse("content://com.silence.im.provider.SMSProvider/"
				+ StringUtils.parseName(IM.getString(IM.ACCOUNT_JID))
				+ "____sms");
		MainActivity.this.getContentResolver().notifyChange(uri, null);
	}

	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }
	//
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.action_search:// ����
	// return true;
	// case R.id.action_add:// ���Һ���
	// startActivity(new Intent(this, AddActivity.class));
	// return true;
	// case R.id.action_account:// �����û���Ϣ
	// startActivity(new Intent(this, UserActivity.class).putExtra(
	// UserActivity.EXTRA_ID, IM.getString(IM.ACCOUNT_JID)));
	// return true;
	// case R.id.action_setting:// �������
	//
	// default:
	// return super.onOptionsItemSelected(item);
	// }
	// }

	@Override
	protected void onRestart() {
		super.onRestart();
		try {
			if (xmppManager != null && !xmppManager.isConnected())
				xmppManager.connect();
			if (xmppManager != null && !xmppManager.isLogin())
				xmppManager.login();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			if (xmppManager != null && !xmppManager.isConnected())
				xmppManager.connect();
			if (xmppManager != null && !xmppManager.isLogin())
				xmppManager.login();
		} catch (RemoteException e1) {
			e1.printStackTrace();
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// ����home action
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// ע��
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.activity_main_btntwo:
			Intent intent = new Intent(MainActivity.this, ShakeActivity.class);
			startActivity(intent);
			break;
		case R.id.activity_main_btn_session:
			viewPager.setCurrentItem(0);
			break;
		case R.id.activity_main_btn_contact:
			viewPager.setCurrentItem(1);
			break;
		}
	}

	private void forceShowOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				String accountString = (String) msg.obj;
				boolean result = false;
				try {
					result = xmppManager.removeFriend(accountString);
				} catch (RemoteException e) {
					result = false;
				}

				System.out.println("ɾ������ �� result = " + result);
				// if (result)
				// MyToast.makeText(MainActivity.this, "ɾ�����ѳɹ�",
				// MyToast.LENGTH_SHORT).show();
				// else
				// MyToast.makeText(MainActivity.this, "ɾ������ʧ��",
				// MyToast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};

	/**
	 * ��������
	 */
	private class LoginServiceConnect implements ServiceConnection {

		public void onServiceConnected(ComponentName componentName,
				IBinder iBinder) {
			xmppManager = IXmppManager.Stub.asInterface(iBinder);
			if (!running)
				loginTask.execute(xmppManager);
		}

		public void onServiceDisconnected(ComponentName componentName) {
			xmppManager = null;
		}
	}

	private boolean running = false;;

	private class LoginTask extends LoginAsyncTask {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			running = true;
		}

		protected void onPostExecute(Integer integer) {
			running = false;
			switch (integer) {
			case LoginAsyncTask.LOGIN_OK:
				MyToast.makeText(MainActivity.this, "��½�ɹ�", MyToast.LENGTH_LONG)
						.show();
				break;
			case LoginAsyncTask.LOGIN_ERROR:
				MyToast.makeText(MainActivity.this, "��¼ʧ��", MyToast.LENGTH_LONG)
						.show();
				break;
			case LoginAsyncTask.CONNECTION_ERROR:
				MyToast.makeText(MainActivity.this, "����ʧ��", MyToast.LENGTH_LONG)
						.show();
				break;
			}
		}
	}

	private class PageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int position) {
			switch (position) {
			case 0:
				radioGroup.check(R.id.activity_main_btn_session);
				break;
			case 1:
				radioGroup.check(R.id.activity_main_btn_contact);
				break;

			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
	}

	private void showWindow(View parent) {

		if (popupWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			view = layoutInflater.inflate(
					R.layout.activity_main_menu_group_list, null);

			lv_group = (ListView) view
					.findViewById(R.id.activity_main_menu_group_list);
			// ��������
			groups = new ArrayList<String>();
			groups.add("�ҵ�����");
			groups.add("�ҵ�ͼƬ");
			groups.add("��Ӻ���");
			groups.add("��������");

			MenuGroupAdapter groupAdapter = new MenuGroupAdapter(this, groups);
			lv_group.setAdapter(groupAdapter);
			// ����һ��PopuWidow����
			popupWindow = new PopupWindow(view, 300, 450);
		}

		// ʹ��ۼ�
		popupWindow.setFocusable(true);
		// ����������������ʧ
		popupWindow.setOutsideTouchable(true);

		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		// ��ʾ��λ��Ϊ:��Ļ�Ŀ�ȵ�һ��-PopupWindow�ĸ߶ȵ�һ��
		int xPos = windowManager.getDefaultDisplay().getWidth() / 2
				- popupWindow.getWidth() / 2;
		Log.i("coder", "xPos:" + xPos);

		popupWindow.showAsDropDown(parent, xPos, 0);

		lv_group.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {

				if (position == 0) {
					startActivity(new Intent(MainActivity.this,
							UserActivity.class).putExtra(UserActivity.EXTRA_ID,
							IM.getString(IM.ACCOUNT_JID)));

				} else if (position == 1) {
					startActivity(new Intent(MainActivity.this,
							ChatImageActivity.class));
				} else if (position == 2) {
					startActivity(new Intent(MainActivity.this,
							AddActivity.class));
				} else {
					startActivity(new Intent(MainActivity.this,
							SettingActivity.class));
				}

				if (popupWindow != null) {
					popupWindow.dismiss();
				}
			}
		});
	}

}