package com.silence.im.ui;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.R;

public class DownFileActivity extends Activity implements OnItemClickListener,
		OnItemLongClickListener {

	private ListView mListView;
	private FileListAdapter mFileAdpter;
	private AlertDialog alertDialog;
	private int position;
	private File[] filterFiles;
	private Dialog selectDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downfiles);
		initView();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_downfile_rename:

			AlertDialog.Builder builder3 = new AlertDialog.Builder(
					DownFileActivity.this);
			LayoutInflater layoutInflater = LayoutInflater
					.from(DownFileActivity.this);
			View view = layoutInflater.inflate(
					R.layout.activity_downloadfile_dialog_filerename, null);
			final EditText filename = (EditText) view
					.findViewById(R.id.activity_downloadfile_filerename_filename);
			builder3.setView(view);
			builder3.setTitle("重命名");
			builder3.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String name = filename.getText().toString();
							if (name != null && !name.trim().equals("")) {
								filterFiles[position].renameTo(new File(
										filterFiles[position].getParent(), name));
								dialog.dismiss();
								MyToast.makeText(DownFileActivity.this,
										"重命名成功", MyToast.LENGTH_SHORT).show();
							} else
								MyToast.makeText(DownFileActivity.this,
										"文件名不能为空哦", MyToast.LENGTH_SHORT)
										.show();

						}
					});
			builder3.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder3.setOnKeyListener(new DialogInterface.OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK)
						dialog.dismiss();
					return false;
				}
			});
			alertDialog = builder3.create();
			alertDialog.show();

			break;
		case R.id.menu_downfile_delete:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					DownFileActivity.this);
			builder.setTitle("提示！");
			builder.setMessage("确定要删除此文件吗？");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String path = filterFiles[position].getPath();
							File file = new File(path);
							if (file.exists())
								file.delete();
							dialog.dismiss();
						}
					});
			builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.setOnKeyListener(new DialogInterface.OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK)
						dialog.dismiss();
					return false;
				}
			});
			alertDialog = builder.create();
			alertDialog.show();
			break;
		case R.id.menu_downfile_alldelete:
			AlertDialog.Builder builder1 = new AlertDialog.Builder(
					DownFileActivity.this);
			builder1.setTitle("提示！");
			builder1.setMessage("确定要删除所有文件吗？");
			builder1.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String path = filterFiles[position].getPath();
							File file = new File(path).getParentFile();
							if (file.exists() && file.isDirectory()) {
								File[] files = file.listFiles();
								for (File temp : files)
									if (temp.exists())
										temp.delete();
							}
							dialog.dismiss();
						}
					});
			builder1.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder1.setOnKeyListener(new DialogInterface.OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK)
						dialog.dismiss();
					return false;
				}
			});
			alertDialog = builder1.create();
			alertDialog.show();
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.menu_downloadfile, menu);
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.activity_downfiles_listview);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		File folder = new File(IM.DOWNLOAD_PATH);
		initData(folder);
	}

	private void initData(File folder) {
		ArrayList<File> files = new ArrayList<File>();
		filterFiles = folder.listFiles();
		if (null != filterFiles && filterFiles.length > 0) {
			for (File file : filterFiles) {
				if (file.isFile())
					files.add(file);
			}
		}
		mFileAdpter = new FileListAdapter(this, files);
		mListView.setAdapter(mFileAdpter);
	}

	private class FileListAdapter extends BaseAdapter {

		private ArrayList<File> files;
		private LayoutInflater mInflater;

		public FileListAdapter(Context context, ArrayList<File> files) {
			this.files = files;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return files.size();
		}

		@Override
		public Object getItem(int position) {
			return files.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mInflater.inflate(
						R.layout.activity_downfiles_item, null);
				convertView.setTag(viewHolder);
				viewHolder.title = (TextView) convertView
						.findViewById(R.id.file_title);
				viewHolder.type = (TextView) convertView
						.findViewById(R.id.file_type);
				viewHolder.data = (TextView) convertView
						.findViewById(R.id.file_date);
				viewHolder.size = (TextView) convertView
						.findViewById(R.id.file_size);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			File file = (File) getItem(position);
			String fileName = file.getName();
			viewHolder.title.setText(fileName);
			if (file.isDirectory()) {
				viewHolder.size.setText("文件夹");
				viewHolder.size.setTextColor(Color.RED);
				viewHolder.type.setVisibility(View.GONE);
				viewHolder.data.setVisibility(View.GONE);
			} else {
				long fileSize = file.length();
				if (fileSize > 1024 * 1024) {
					float size = fileSize / (1024f * 1024f);
					viewHolder.size.setText(new DecimalFormat("#.00")
							.format(size) + "MB");
				} else if (fileSize >= 1024) {
					float size = fileSize / 1024;
					viewHolder.size.setText(new DecimalFormat("#.00")
							.format(size) + "KB");
				} else {
					viewHolder.size.setText(fileSize + "B");
				}
				int dot = fileName.indexOf('.');
				if (dot > -1 && dot < (fileName.length() - 1)) {
					viewHolder.type.setText(fileName.substring(dot + 1) + "文件");
				}
				viewHolder.data
						.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm")
								.format(file.lastModified()));
			}
			return convertView;
		}

		class ViewHolder {
			private TextView title;
			private TextView type;
			private TextView data;
			private TextView size;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		File file = (File) mFileAdpter.getItem(position);
		if (!file.canRead()) {
			MyToast.makeText(this, "权限不足", MyToast.LENGTH_LONG).show();

		} else if (file.isDirectory()) {
			initData(file);
		} else {
			openFile(file);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(IM.MESSAGE_NOTIFICATION);
	}

	private void openFile(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		String type = getMIMEType(file);
		intent.setDataAndType(Uri.fromFile(file), type);
		try {
			startActivity(intent);
		} catch (Exception e) {
			MyToast.makeText(this, "未知类型，不能打开", MyToast.LENGTH_SHORT).show();
		}
	}

	private String getMIMEType(File file) {
		String type = "*/*";
		String fileName = file.getName();
		int dotIndex = fileName.indexOf('.');
		if (dotIndex < 0) {
			return type;
		}
		String end = fileName.substring(dotIndex, fileName.length())
				.toLowerCase();
		if (end.equals("")) {
			return type;
		}
		for (int i = 0; i < MIME_MapTable.length; i++) {
			if (end.equals(MIME_MapTable[i][0])) {
				type = MIME_MapTable[i][1];
			}
		}
		return type;
	}

	private final String[][] MIME_MapTable = {
			// {后缀名， MIME类型}
			{ ".3gp", "video/3gpp" },
			{ ".apk", "application/vnd.android.package-archive" },
			{ ".asf", "video/x-ms-asf" },
			{ ".avi", "video/x-msvideo" },
			{ ".bin", "application/octet-stream" },
			{ ".bmp", "image/bmp" },
			{ ".c", "text/plain" },
			{ ".class", "application/octet-stream" },
			{ ".conf", "text/plain" },
			{ ".cpp", "text/plain" },
			{ ".doc", "application/msword" },
			{ ".docx",
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
			{ ".xls", "application/vnd.ms-excel" },
			{ ".xlsx",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
			{ ".exe", "application/octet-stream" },
			{ ".gif", "image/gif" },
			{ ".gtar", "application/x-gtar" },
			{ ".gz", "application/x-gzip" },
			{ ".h", "text/plain" },
			{ ".htm", "text/html" },
			{ ".html", "text/html" },
			{ ".jar", "application/java-archive" },
			{ ".java", "text/plain" },
			{ ".jpeg", "image/jpeg" },
			{ ".jpg", "image/jpeg" },
			{ ".js", "application/x-javascript" },
			{ ".log", "text/plain" },
			{ ".m3u", "audio/x-mpegurl" },
			{ ".m4a", "audio/mp4a-latm" },
			{ ".m4b", "audio/mp4a-latm" },
			{ ".m4p", "audio/mp4a-latm" },
			{ ".m4u", "video/vnd.mpegurl" },
			{ ".m4v", "video/x-m4v" },
			{ ".mov", "video/quicktime" },
			{ ".mp2", "audio/x-mpeg" },
			{ ".mp3", "audio/x-mpeg" },
			{ ".mp4", "video/mp4" },
			{ ".mpc", "application/vnd.mpohun.certificate" },
			{ ".mpe", "video/mpeg" },
			{ ".mpeg", "video/mpeg" },
			{ ".mpg", "video/mpeg" },
			{ ".mpg4", "video/mp4" },
			{ ".mpga", "audio/mpeg" },
			{ ".msg", "application/vnd.ms-outlook" },
			{ ".ogg", "audio/ogg" },
			{ ".pdf", "application/pdf" },
			{ ".png", "image/png" },
			{ ".pps", "application/vnd.ms-powerpoint" },
			{ ".ppt", "application/vnd.ms-powerpoint" },
			{ ".pptx",
					"application/vnd.openxmlformats-officedocument.presentationml.presentation" },
			{ ".prop", "text/plain" }, { ".rc", "text/plain" },
			{ ".rmvb", "audio/x-pn-realaudio" }, { ".rtf", "application/rtf" },
			{ ".sh", "text/plain" }, { ".tar", "application/x-tar" },
			{ ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },
			{ ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
			{ ".wmv", "audio/x-ms-wmv" },
			{ ".wps", "application/vnd.ms-works" }, { ".xml", "text/plain" },
			{ ".z", "application/x-compress" },
			{ ".zip", "application/x-zip-compressed" }, { "", "*/*" } };

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		this.position = position;
		return false;
	}

}