package com.silence.im.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.util.StringUtils;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent.CanceledException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.R;
import com.silence.im.provider.SMSProvider.SMSColumns;
import com.silence.im.service.Contact;
import com.silence.im.service.IXmppManager;
import com.silence.im.ui.adapter.ChatAdapter;
import com.silence.im.util.TimeRender;

/**
 * 单聊界面
 * 
 * 
 * @author JerSuen
 */
public class ChatActivity extends FragmentActivity implements OnClickListener,
		OnItemClickListener, OnTouchListener, OnItemLongClickListener {
	public static final String EXTRA_CONTACT = "contact";

	private ViewPager viewPager;
	private GridView gridView1, gridView2, gridView3;
	private List<View> list = new ArrayList<View>();// 表示每个子页的视图列表 动态装载的布局

	private PagerAdapter pageAdapter = new EmojiGridAdapter();
	private boolean isShow = false;

	private SimpleAdapter simpleAdapter1 = null, simpleAdapter2 = null,
			simpleAdapter3 = null;
	private int pageNum;

	private Contact contact;
	private ListView listView;
	private EditText input;
	private ServiceConnection serviceConnect = new XmppServiceConnect();
	private ChatAdapter adapter;
	private ContentObserver co;
	private IXmppManager xmppManager;
	private Uri uri_sms = Uri
			.parse("content://com.silence.im.provider.SMSProvider/"
					+ StringUtils.parseName(IM.getString(IM.ACCOUNT_JID))
					+ "____sms");

	private Dialog selectDialog;
	private ImageButton titleBtn;
	private TextView titleText;

	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// 自定义ActionBar布局
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				ChatActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		listView = (ListView) findViewById(R.id.activity_chat_list);
		input = (EditText) findViewById(R.id.activity_chat_send_input);
		findViewById(R.id.activity_chat_send_btn).setOnClickListener(this);
		findViewById(R.id.activity_chat_send_emoj).setOnClickListener(this);

		// 传递的联系人
		contact = getIntent().getParcelableExtra(EXTRA_CONTACT);
		if (contact.name_by_me != null)
			titleText.setText(contact.name_by_me);
		else if (contact.nickname != null)
			titleText.setText(contact.nickname);
		titleText.setText(StringUtils.parseName(contact.account));

		// 改变输入框
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		}
		// 装配适配器
		adapter = new ChatAdapter(contact.account);
		// 给适配器设置监听器
		adapter.setOnChatViewClickListener(this);
		listView.setAdapter(adapter);

		// 加载表情布局
		initEmoji();

		// 内容观察者
		co = new ContentObserver(new Handler()) {
			public void onChange(boolean selfChange) {

				Cursor cursor = getContentResolver().query(uri_sms, null,
						SMSColumns.SESSION_ID + " = ?",
						new String[] { contact.account }, null);
				// adapter.changeCursor(cursor);
				System.out.println("ChatActivity change");
				adapter.swapCursor(cursor);
			}
		};

		Uri uri = Uri.parse("content://com.silence.im.provider.SMSProvider");

		// 注册观察者
		getContentResolver().registerContentObserver(uri, true, co);

		input.setOnClickListener(this);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				System.out.println("hasFocus = " + hasFocus + "  isShow = "
						+ isShow);
				if (hasFocus)
					hideKeyboard();
			}
		});

		findViewById(R.id.activity_chat_send_image).setOnClickListener(this);
		findViewById(R.id.activity_chat_add_voice).setOnClickListener(this);
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!input.getText().toString().isEmpty()) {
					// TODO 语音按钮变成发送按钮
					findViewById(R.id.activity_chat_add_voice).setVisibility(
							View.GONE);
					findViewById(R.id.activity_chat_send_btn).setVisibility(
							View.VISIBLE);
				} else
				// if (input.getText().toString().isEmpty())
				{
					// TODO 发送按钮变成语音按钮
					findViewById(R.id.activity_chat_add_voice).setVisibility(
							View.VISIBLE);
					findViewById(R.id.activity_chat_send_btn).setVisibility(
							View.GONE);
				}
			}
		});
	}

	private void initEmoji() {
		viewPager = (ViewPager) this.findViewById(R.id.viewpager);

		// 动态加载布局
		View view1 = LayoutInflater.from(ChatActivity.this).inflate(
				R.layout.eoj1, null);
		gridView1 = (GridView) view1.findViewById(R.id.gridview1);
		View view2 = LayoutInflater.from(ChatActivity.this).inflate(
				R.layout.eoj2, null);
		gridView2 = (GridView) view2.findViewById(R.id.gridview2);
		View view3 = LayoutInflater.from(ChatActivity.this).inflate(
				R.layout.eoj3, null);
		gridView3 = (GridView) view3.findViewById(R.id.gridview3);
		list.add(view1);
		list.add(view2);
		list.add(view3);
		// 加载适配器
		viewPager.setAdapter(pageAdapter);

		new AsyncTask<String, Integer, Boolean>() {

			@Override
			protected Boolean doInBackground(String... params) {
				List<Map<String, Integer>> data1 = new ArrayList<Map<String, Integer>>();
				for (int i = 0; i < 18; i++) {
					Map<String, Integer> map = new HashMap<String, Integer>();
					map.put("imageview", IM.resIds1[i]);
					data1.add(map);
				}
				simpleAdapter1 = new SimpleAdapter(ChatActivity.this, data1,
						R.layout.activity_chat_eoj_cell,
						new String[] { "imageview" },
						new int[] { R.id.imageview });// 配置适配器

				List<Map<String, Integer>> data2 = new ArrayList<Map<String, Integer>>();
				for (int i = 0; i < 18; i++) {
					Map<String, Integer> map = new HashMap<String, Integer>();
					map.put("imageview", IM.resIds2[i]);
					data2.add(map);
				}
				simpleAdapter2 = new SimpleAdapter(ChatActivity.this, data2,
						R.layout.activity_chat_eoj_cell,
						new String[] { "imageview" },
						new int[] { R.id.imageview });// 配置适配器

				List<Map<String, Integer>> data3 = new ArrayList<Map<String, Integer>>();
				for (int i = 0; i < 18; i++) {
					Map<String, Integer> map = new HashMap<String, Integer>();
					map.put("imageview", IM.resIds3[i]);
					data3.add(map);
				}
				simpleAdapter3 = new SimpleAdapter(ChatActivity.this, data3,
						R.layout.activity_chat_eoj_cell,
						new String[] { "imageview" },
						new int[] { R.id.imageview });// 配置适配器
				return null;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				gridView1.setAdapter(simpleAdapter1);
				gridView2.setAdapter(simpleAdapter2);
				gridView3.setAdapter(simpleAdapter3);
			}
		}.execute();

		gridView1.setOnItemClickListener(this);
		gridView2.setOnItemClickListener(this);
		gridView3.setOnItemClickListener(this);
		// gridView3.setOnTouchListener(this);
		// gridView3.setOnItemLongClickListener(this);

		pageNum = 1;
		isShow = false;

		// 注册事件
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO 自动生成的方法存根
				pageNum = arg0 + 1;
				Log.i("MainActivity1", "---Selected---" + arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO 自动生成的方法存根
				Log.i("MainActivity2", "---onPageScrolled---" + arg0);
				Log.i("MainActivity3", "---onPageScrolled---" + arg1);
				Log.i("MainActivity4", "---onPageScrolled---" + arg2);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO 自动生成的方法存根
				adapter.notifyDataSetChanged();
				// pageNum=arg0;
				Log.i("MainActivity5", "---onPageScrollStateChanged---" + arg0);
			}
		});
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_chat_send_input:
			hideKeyboard();
			break;
		case R.id.activity_chat_send_btn:// 发送消息
			String bodyStr = input.getText().toString();
			if (!TextUtils.isEmpty(bodyStr)) {
				try {
					xmppManager.sendMessage(contact.account, contact.nickname,
							bodyStr, "chat");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				// 清空输入框
				input.setText(null);
			}
			break;
		case R.id.activity_chat_item_avatar:// 点击头像 ，跳转到用户详情
			startActivity(new Intent(ChatActivity.this, UserActivity.class)
					.putExtra(UserActivity.EXTRA_ID, v.getTag().toString()));
			break;
		case R.id.activity_chat_item_voice:// 点击播放语音
			System.out.println("v = " + v.toString() + "  tag = " + v.getTag());
			if (v.getTag() == null) {
				MyToast.makeText(ChatActivity.this, "语音记录异常",
						MyToast.LENGTH_SHORT).show();
				return;
			}
			String path = v.getTag().toString();
			// 初始化MediaPlayer
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
				if (!audioSessionID.equals(path))
					audioPlay(path);
			} else {
				audioPlay(path);
			}
			break;
		case R.id.activity_chat_item_image:// 点击观看收到的图片
			if (v.getTag() == null) {
				MyToast.makeText(ChatActivity.this, "图片文件损坏",
						MyToast.LENGTH_SHORT).show();
				return;
			}
			Intent intent = new Intent(ChatActivity.this,
					WindowsFullActivity.class);
			intent.putExtra("type", WindowsFullActivity.CHAT_READ);
			intent.putExtra("path", v.getTag().toString());
			intent.putExtra("sessionID", contact.account);
			startActivity(intent);
			break;
		// case R.id.activity_chat_item_content:
		// // 文件传输同意监听
		// XmppTool xmppTool = new XmppTool(ChatActivity.this);
		// XMPPConnection connection = xmppTool.getConnection();
		// FileMessage message = new FileMessage();
		// message.setStatus(FileMessage.ACCEPT);
		// try {
		// connection.sendPacket(message);
		// } catch (NotConnectedException e) {
		// e.printStackTrace();
		// }
		// xmppTool.closeConnection();
		// break;
		case R.id.activity_chat_send_emoj:
			// 发送表情
			if (!isShow)
				showKeyboard();
			else {
				hideKeyboard();
				InputMethodManager inputMethodManager1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager1.toggleSoftInput(0,
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
			break;
		case R.id.activity_chat_send_image:// 发送图片
			// // 浏览图库,发送图片
			Intent intentSelect = new Intent();
			intentSelect.setType("image/*");
			intentSelect.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intentSelect, 2);
			break;
		case R.id.activity_chat_add_voice:
			// 发送语音
			filetype = IM.MEDIA_AUDIO;
			final String filename = TimeRender.getTime() + ".mp3";
			audiofile = IM.AUDIO_PATH + filename;
			initMediaRecorder();

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
			cacel.setText("说完了");
			delete.setText("取消");

			TextView textView = (TextView) selectDialog
					.findViewById(R.id.dialog_text);

			textView.setText("请说话");

			cacel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					mRecorder.stop();

					System.out.println("filename = " + filename + "  path = "
							+ audiofile);

					// base64Task = new FileSendByBase64Task();
					// base64Task.execute(contact.account,
					// contact.nickname, filename, audiofile);
					try {
						xmppManager.sendFileByHTTPNoRequest(contact.account,
								contact.nickname, audiofile, filename,
								"audio/mp3");
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					selectDialog.dismiss();
				}
			});

			delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					mRecorder.stop();
					selectDialog.dismiss();
				}
			});
			Window dialogWindow = selectDialog.getWindow();
			WindowManager m = dialogWindow.getWindowManager();
			Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
			WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
			p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3
			p.width = (int) (d.getWidth() * 0.65); // 宽度设置为屏幕的0.65
			dialogWindow.setAttributes(p);

			try {
				mRecorder.prepare();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mRecorder.start();
			selectDialog.show();// 显示对话框

			// AlertDialog.Builder builder = new AlertDialog.Builder(
			// ChatActivity.this);
			// builder.setTitle("请说话");
			// builder.setPositiveButton("说完了",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// mRecorder.stop();
			//
			// System.out.println("filename = " + filename
			// + "  path = " + audiofile);
			//
			// // base64Task = new FileSendByBase64Task();
			// // base64Task.execute(contact.account,
			// // contact.nickname, filename, audiofile);
			// try {
			// xmppManager.sendFileByHTTPNoRequest(
			// contact.account, contact.nickname,
			// audiofile, filename, "audio/mp3");
			// } catch (RemoteException e) {
			// e.printStackTrace();
			// }
			// dialog.dismiss();
			// }
			// });
			// builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			//
			// @Override
			// public boolean onKey(DialogInterface dialog, int keyCode,
			// KeyEvent event) {
			// if (KeyEvent.KEYCODE_BACK == keyCode) {
			// mRecorder.stop();
			// File file = new File(audiofile);
			// if (file.exists())
			// file.delete();
			// dialog.dismiss();
			// }
			// return true;
			// }
			// });
			// AlertDialog dialog = builder.create();
			// mRecorder.start();
			// dialog.show();
			break;
		default:
			break;
		}
	}

	private void audioPlay(String path) {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(getApplicationContext(),
					Uri.fromFile(new File(path)));
			mediaPlayer.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 获得时间长度
		final int length = mediaPlayer.getDuration();
		audioSessionID = path;
		System.out.println("1-->>audioSessionID-->>" + audioSessionID);
		// 放音
		mediaPlayer.start();
	}

	private String audioSessionID = null;
	private MediaPlayer mediaPlayer = null;
	private String filetype;
	private String audiofile;
	private MediaRecorder mRecorder;
	private FileSendByBase64Task base64Task;

	private int position;

	private void initMediaRecorder() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(audiofile);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e("MainActivity", "prepare() failed");
		}
	}

	// TODO
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if (arg0 == 2 && arg1 == Activity.RESULT_OK) {
			Uri uri = arg2.getData();
			String[] pojo = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(uri, pojo, null, null,
					null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					String pathStr = cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					if (!TextUtils.isEmpty(pathStr)) {
						// 文件后缀判断
						String[] temp = pathStr.split("/");
						String filename = temp[temp.length - 1];
						String mime_type = filename.substring(filename
								.lastIndexOf(".") + 1);
						System.out.println("filename = " + filename
								+ "  path = " + pathStr);
						// sendFileByHTTP(pathStr, filename);

						filetype = IM.MEDIA_IMAGE;
						// new FileSendByBase64Task().execute(contact.account,
						// contact.nickname, filename, pathStr);
						try {
							xmppManager.sendFileByHTTPNoRequest(
									contact.account, contact.nickname, pathStr,
									filename, "image/" + mime_type);
						} catch (RemoteException e) {
							e.printStackTrace();
						}

					}
				}
			}
		}
	}

	private class FileSendByBase64Task extends
			AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			// 发送前控件UI操作
			MyToast.makeText(ChatActivity.this, "发送前", 1).show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			try {
				result = xmppManager.sendBase64File(params[0], params[1],
						params[2], params[3], filetype);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// 发送后控件UI操作
			if (result)
				MyToast.makeText(ChatActivity.this, "发送成功", 1).show();
			else
				MyToast.makeText(ChatActivity.this, "发送失败", 1).show();
		}
	}

	/** XMPP连接服务 */
	private class XmppServiceConnect implements ServiceConnection {
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
		// 把未读信息置空
		ContentValues values = new ContentValues();
		values.put(SMSColumns.UNREAD, 0);
		getContentResolver().update(uri_sms, values,
				SMSColumns.SESSION_ID + "=?", new String[] { contact.account });
		// 储存当前正在聊天窗口的状态
		IM.putString(IM.CHAT_STATUS, IM.CHATTING);
	}

	private void showKeyboard() {
		isShow = true;
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(ChatActivity.this
				.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
		// try {
		// Thread.sleep(500);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		viewPager.setVisibility(View.VISIBLE);
	}

	private void hideKeyboard() {
		isShow = false;
		viewPager.setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		if (isShow) {
			hideKeyboard();
			return;
		}
		super.onBackPressed();
	}

	class EmojiGridAdapter extends PagerAdapter {

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) container).addView(list.get(position));
			return list.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// super.destroyItem(container, position, object);
			((ViewPager) container).removeView(list.get(position));
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	// 判断字符串是否为整数
	public boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// 监听表情界面按钮，添加表情，图文混排
		Editable editable = input.getText();
		int start = input.getSelectionStart();
		if (position < 17) {
			try {
				Field field = R.drawable.class.getDeclaredField("e"
						+ (pageNum + "") + (position + 11));// 查找文件，利用随机数

				System.out.println("face name -->> " + "e" + (pageNum + "")
						+ (position + 11));

				int result = Integer.parseInt(field.get(null).toString());// 获取图片id

				Drawable drawable = this.getResources().getDrawable(result);
				drawable.setBounds(0, 0, 60, 60);// 这里设置图片的大小
				ImageSpan imageSpan = new ImageSpan(drawable,
						ImageSpan.ALIGN_BASELINE);

				SpannableString spannableString = new SpannableString("/"
						+ (pageNum + "") + (position + 11));// 字符序列串
				spannableString.setSpan(imageSpan, 0, 4,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);// 连接字符序列串和图片序列串，4和face的长度相关
				editable.insert(start, spannableString);

				System.out.println("editable.length() -->> "
						+ editable.length());

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// 表情界面删除按键
			if (editable != null && editable.length() > 0) {
				if (isEmoji(editable, start)) {
					editable.delete(start - 4, start);
				} else
					editable.delete(start - 1, start);
			}
		}
	}

	// 表情编码 /111
	// 判断删除按键前是否为表情
	private boolean isEmoji(Editable editable, int start) {
		if (start < 4)
			return false;
		String numString = editable.toString().substring(start - 3, start);
		try {
			int num = Integer.parseInt(numString);
			int page = num / 100;
			int index = num % 100;
			System.out.println("page = " + page + "    index = " + index);
			if (page >= 1 && page <= 3 && index >= 11 && index <= 27)
				if (editable.toString().substring(start - 4, start - 3)
						.equals("/"))
					return true;
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		this.position = position;
		return false;
	}

	private Editable editable;
	private int start;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				// todo something....
				if (editable != null && editable.length() > 0) {
					editable.delete(start - 1, start);
					start--;
				}
			}
		}
	};

	private Timer timer = new Timer(true);

	// 任务
	private TimerTask task = new TimerTask() {
		public void run() {
			Message msg = new Message();
			msg.what = 1;
			handler.sendMessage(msg);
		}
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		editable = input.getText();
		start = input.getSelectionStart();

		System.out.println("action = " + event.getAction());

		if (position == 17) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				// 启动定时器
				timer.schedule(task, 0, 100);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				timer.cancel();
				task.cancel();
			}
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 储存当前正在聊天窗口的状态
		IM.putString(IM.CHAT_STATUS, IM.CHATTING);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 储存当前不在聊天窗口的状态
		IM.putString(IM.CHAT_STATUS, IM.NO_CHATTING);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 储存当前不在聊天窗口的状态
		IM.putString(IM.CHAT_STATUS, IM.NO_CHATTING);
	}

	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnect);
		getContentResolver().unregisterContentObserver(co);
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

}