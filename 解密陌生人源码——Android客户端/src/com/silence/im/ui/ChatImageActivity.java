package com.silence.im.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.R;
import com.silence.im.util.BitmapTool;

@SuppressLint({ "InlinedApi", "NewApi" })
public class ChatImageActivity extends Activity implements OnItemClickListener,
		OnItemSelectedListener, OnItemLongClickListener {

	private GridView imageGridView;
	private ImageSimpleAdapter adapter;
	private List<Map<String, Object>> data;
	// private ProgressDialog dialog;
	private CustomProgressDialog progressDialog;

	private int position;
	private Dialog selectDialog;

	private int[] colors = { 0xFF99FF, 0xFF9966, 0x9933CC, 0x33FF00, 0x00CCCC,
			0xFF0000, 0x6666FF, 0x0000CC };

	private ImageButton titleBtn;
	private TextView titleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatimage);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// 自定义ActionBar布局
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				ChatImageActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("聊天图片");

		// dialog = ProgressDialog.show(ChatImageActivity.this, "提示",
		// "正在刷新...");
		progressDialog = CustomProgressDialog.createDialog(this);
		progressDialog.setMessage("loading....");
		progressDialog.show();

		imageGridView = (GridView) this
				.findViewById(R.id.activity_chatimage_gridview);
		new LoadImageTask().execute();
		imageGridView.setOnItemClickListener(this);
		imageGridView.setOnItemSelectedListener(this);
		imageGridView.setOnItemLongClickListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.menu_chatimage, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.chatimage_save:
			String path = (String) data.get(position).get("image");
			File file = new File(path);
			file.renameTo(new File(IM.DOWNLOAD_PATH
					+ path.substring(path.lastIndexOf("/") + 1)));
			MyToast.makeText(
					ChatImageActivity.this,
					"图片已保存到"
							+ IM.DOWNLOAD_PATH.substring(0,
									IM.DOWNLOAD_PATH.length() - 1) + "目录下",
					MyToast.LENGTH_SHORT).show();
			break;
		case R.id.chatimage_delete:
			// AlertDialog.Builder builder = new AlertDialog.Builder(
			// ChatImageActivity.this);
			// builder.setTitle("提示！");
			// builder.setMessage("删除此图片会造成聊天记录中图片消失，确定还要删除此图片吗？");
			// builder.setPositiveButton("确定",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// String path = (String) data.get(position).get(
			// "image");
			// File file = new File(path);
			// if (file.exists())
			// file.delete();
			// dialog.dismiss();
			// }
			// });
			// builder.setNegativeButton("取消",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// dialog.dismiss();
			// }
			// });
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
			// alertDialog = builder.create();
			// alertDialog.show();

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
			TextView textView = (TextView) selectDialog
					.findViewById(R.id.dialog_text);

			textView.setText("删除所有图片会造成聊天记录中图片消失，确定还要删除所有图片吗？");

			delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectDialog.dismiss();// 隐藏对话框
					String path = (String) data.get(position).get("image");
					File file = new File(path);
					if (file.exists())
						file.delete();

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
			break;

		case R.id.chatimage_alldelete:

			/* 初始化普通对话框。并设置样式 */
			selectDialog = new Dialog(this, R.style.dialog);
			selectDialog.setCancelable(true);
			selectDialog.setCanceledOnTouchOutside(true);
			/* 设置普通对话框的布局 */
			selectDialog.setContentView(R.layout.dialog_delete);

			Button cacel2 = (Button) selectDialog
					.findViewById(R.id.dialog_cacel);
			Button delete2 = (Button) selectDialog
					.findViewById(R.id.dialog_delete);
			TextView textView2 = (TextView) selectDialog
					.findViewById(R.id.dialog_text);

			textView2.setText("删除所有图片会造成聊天记录中图片消失，确定还要删除所有图片吗？");

			delete2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectDialog.dismiss();// 隐藏对话框
					String path = (String) data.get(position).get("image");
					File file = new File(path).getParentFile();
					if (file.exists() && file.isDirectory()) {
						File[] files = file.listFiles();
						for (File temp : files)
							if (temp.exists())
								temp.delete();
					}
				}
			});

			cacel2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					selectDialog.dismiss();// 隐藏对话框
				}
			});
			dialogWindow = selectDialog.getWindow();
			m = dialogWindow.getWindowManager();
			d = m.getDefaultDisplay(); // 获取屏幕宽、高用
			p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
			p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.3
			p.width = (int) (d.getWidth() * 0.65); // 宽度设置为屏幕的0.65
			dialogWindow.setAttributes(p);

			selectDialog.show();// 显示对话框

			//
			//
			// AlertDialog.Builder builder1 = new AlertDialog.Builder(
			// ChatImageActivity.this);
			// builder1.setTitle("提示！");
			// builder1.setMessage("删除所有图片会造成聊天记录中图片消失，确定还要删除所有图片吗？");
			// builder1.setPositiveButton("确定",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// String path = (String) data.get(position).get(
			// "image");
			// File file = new File(path).getParentFile();
			// if (file.exists() && file.isDirectory()) {
			// File[] files = file.listFiles();
			// for (File temp : files)
			// if (temp.exists())
			// temp.delete();
			// }
			// dialog.dismiss();
			// }
			// });
			// builder1.setNegativeButton("取消",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// dialog.dismiss();
			// }
			// });
			// builder1.setOnKeyListener(new DialogInterface.OnKeyListener() {
			//
			// @Override
			// public boolean onKey(DialogInterface dialog, int keyCode,
			// KeyEvent event) {
			// if (keyCode == KeyEvent.KEYCODE_BACK)
			// dialog.dismiss();
			// return false;
			// }
			// });
			// alertDialog = builder1.create();
			// alertDialog.show();
			break;
		default:
			break;
		}
		return true;

	}

	private class LoadImageTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			data = new ArrayList<Map<String, Object>>();
			File dir = new File(IM.IMAGE_PATH);
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				String temp = files[i].getPath();
				if (files[i].isFile()
						&& (temp.endsWith("jpg") || temp.endsWith("png")
								|| temp.endsWith("gif") || temp.endsWith("bmp"))) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("image", files[i].getPath());
					System.out.println("file-->>" + files[i].getPath());
					data.add(map);
				}
			}
			adapter = new ImageSimpleAdapter(ChatImageActivity.this, data,
					R.layout.activity_chatimage_item, new String[] { "image" },
					new int[] { R.id.activity_chatimage_item_image });
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			imageGridView.setAdapter(adapter);
		}
	}

	private class ImageSimpleAdapter extends SimpleAdapter {

		private List<? extends Map<String, ?>> list;
		private int resource;
		private Context context;
		private String[] from;
		private int[] to;

		public ImageSimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			list = data;
			this.resource = resource;
			this.context = context;
			this.from = from;
			this.to = to;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(resource,
						null);
				holder.imageView = (ImageView) convertView.findViewById(to[0]);
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();
			String path = (String) list.get(position).get(from[0]);
			Bitmap bitmap = BitmapTool.decodeBitmap(path, 200, 200, true);
			int pos = new Random().nextInt(8);
			holder.imageView.setBackgroundColor(colors[pos]);
			holder.imageView.setImageBitmap(bitmap);
			// holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			holder.imageView.setTag(path);
			return convertView;
		}

		public class ViewHolder {
			public ImageView imageView;
			// public TextView textView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(ChatImageActivity.this,
				WindowsFullActivity.class);
		intent.putExtra("type", WindowsFullActivity.DIR_READ);
		intent.putExtra("position", position);
		startActivity(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(IM.MESSAGE_NOTIFICATION);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		String path = (String) data.get(position).get("image");
		Intent intent = new Intent(ChatImageActivity.this,
				WindowsFullActivity.class);
		intent.putExtra("path", path);
		startActivity(intent);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		this.position = position;
		return false;
	}
}
