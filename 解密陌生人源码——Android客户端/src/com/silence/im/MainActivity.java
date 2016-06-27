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
 * 主界面
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

	// title 相关组件
	private TextView titleText;
	private Button titleBtn;

	// Menu相关
	private PopupWindow popupWindow;
	private ListView lv_group;
	private View view;
	private ArrayList<String> groups;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		forceShowOverflowMenu();

		// 自定义title
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.activity_main_title);

		// title组件实例化
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
	// // 创建AlertDialog
	// menuDialog = new AlertDialog.Builder(this).create();
	// menuDialog.setView(menuView);
	// menuDialog.setOnKeyListener(new OnKeyListener() {
	// public boolean onKey(DialogInterface dialog, int keyCode,
	// KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_MENU)// 监听按键
	// dialog.dismiss();
	// return false;
	// }
	// });
	//
	// menuGrid = (GridView) menuView.findViewById(R.id.main_mune_gridview);
	// menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
	// /** 监听menu选项 **/
	// menuGrid.setOnItemClickListener(new OnItemClickListener() {
	// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
	// long arg3) {
	// switch (arg2) {
	// case ITEM_SEARCH:// 搜索
	// startActivity(new Intent(MainActivity.this,
	// AddActivity.class));
	// menuDialog.dismiss();
	// break;
	// case ITEM_USER_INFO:// 用户信息
	// startActivity(new Intent(MainActivity.this,
	// UserActivity.class).putExtra(UserActivity.EXTRA_ID,
	// IM.getString(IM.ACCOUNT_JID)));
	// menuDialog.dismiss();
	// break;
	// case ITEM_REFRESH:// 刷新
	// menuDialog.dismiss();
	// final ProgressDialog dialog = ProgressDialog.show(
	// MainActivity.this, "提示", "正在刷新...");
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
	// case ITEM_IMAGE:// 聊天图片
	// startActivity(new Intent(MainActivity.this,
	// ChatImageActivity.class));
	// menuDialog.dismiss();
	// break;
	// case ITEM_DOWN_MANAGER:// 下载
	// startActivity(new Intent(MainActivity.this,
	// DownFileActivity.class));
	// menuDialog.dismiss();
	// break;
	// case ITEM_SETTING:// 设置
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
		menu.add("menu");// 必须创建一项
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
		return false;// 返回为true 则显示系统menu
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
	// case R.id.action_search:// 搜索
	// return true;
	// case R.id.action_add:// 查找好友
	// startActivity(new Intent(this, AddActivity.class));
	// return true;
	// case R.id.action_account:// 个人用户信息
	// startActivity(new Intent(this, UserActivity.class).putExtra(
	// UserActivity.EXTRA_ID, IM.getString(IM.ACCOUNT_JID)));
	// return true;
	// case R.id.action_setting:// 软件设置
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
			// 发送home action
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
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

				System.out.println("删除好友 ： result = " + result);
				// if (result)
				// MyToast.makeText(MainActivity.this, "删除好友成功",
				// MyToast.LENGTH_SHORT).show();
				// else
				// MyToast.makeText(MainActivity.this, "删除好友失败",
				// MyToast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 服务连接
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
				MyToast.makeText(MainActivity.this, "登陆成功", MyToast.LENGTH_LONG)
						.show();
				break;
			case LoginAsyncTask.LOGIN_ERROR:
				MyToast.makeText(MainActivity.this, "登录失败", MyToast.LENGTH_LONG)
						.show();
				break;
			case LoginAsyncTask.CONNECTION_ERROR:
				MyToast.makeText(MainActivity.this, "连接失败", MyToast.LENGTH_LONG)
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
			// 加载数据
			groups = new ArrayList<String>();
			groups.add("我的资料");
			groups.add("我的图片");
			groups.add("添加好友");
			groups.add("个人设置");

			MenuGroupAdapter groupAdapter = new MenuGroupAdapter(this, groups);
			lv_group.setAdapter(groupAdapter);
			// 创建一个PopuWidow对象
			popupWindow = new PopupWindow(view, 300, 450);
		}

		// 使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);

		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		// 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
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