package com.silence.im.ui.adapter;

import java.io.File;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.R;
import com.silence.im.provider.SMSProvider.SMSColumns;
import com.silence.im.ui.view.RoundedImageView;
import com.silence.im.util.BitmapTool;
import com.stranger.client.util.FileMessage;

/**
 * 单聊适配器
 * 
 * @author JerSuen
 */
public class ChatAdapter extends CursorAdapter {
	// 设置图片适屏比例
	public static final float DISPLAY_WIDTH = 200;
	public static final float DISPLAY_HEIGHT = 200;

	private final int ITEM_RIGHT = 0;
	private final int ITEM_LEFT = 1;
	private static Uri uri = Uri
			.parse("content://com.silence.im.provider.SMSProvider/"
					+ StringUtils.parseName(IM.getString(IM.ACCOUNT_JID))
					+ "____sms");
	private View.OnClickListener clickListener;

	public ChatAdapter(String account) {
		super(IM.im,
				IM.im.getContentResolver().query(uri, null,
						SMSColumns.SESSION_ID + " = ?",
						new String[] { account }, null),
				FLAG_REGISTER_CONTENT_OBSERVER);
	}

	public View getView(int position, View view, ViewGroup group) {
		ViewHolder holder;

		Cursor cursor = (Cursor) getItem(position);
		String type = cursor.getString(cursor.getColumnIndex(SMSColumns.TYPE));
		System.out.println("ChatAdapter : type = " + type);
		int typeflag = 0;
		if (view == null) {
			holder = new ViewHolder();
			switch (getItemViewType(position)) {
			case ITEM_RIGHT:
				view = LayoutInflater.from(group.getContext()).inflate(
						R.layout.activity_chat_item_right, null);
				break;
			case ITEM_LEFT:
				view = LayoutInflater.from(group.getContext()).inflate(
						R.layout.activity_chat_item_left, null);
			}
			holder.avatar = (RoundedImageView) view
					.findViewById(R.id.activity_chat_item_avatar);
			holder.avatar.setOnClickListener(clickListener);
			holder.audio = (Button) view
					.findViewById(R.id.activity_chat_item_voice);
			holder.audio.setOnClickListener(clickListener);
			holder.image = (ImageView) view
					.findViewById(R.id.activity_chat_item_image);
			holder.image.setOnClickListener(clickListener);
			holder.content = (TextView) view
					.findViewById(R.id.activity_chat_item_content);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		// 根据类型选择显示的视图
		if (type != null && type.equals("chat")) {
			System.out.println("chat");
			view.findViewById(R.id.activity_chat_layout_image).setVisibility(
					View.GONE);
			view.findViewById(R.id.activity_chat_layout_text).setVisibility(
					View.VISIBLE);
			view.findViewById(R.id.activity_chat_layout_voice).setVisibility(
					View.GONE);
			typeflag = 1;
		} else if (type != null && type.contains(IM.MEDIA_AUDIO)) {
			System.out.println("audio");
			view.findViewById(R.id.activity_chat_layout_voice).setVisibility(
					View.VISIBLE);
			view.findViewById(R.id.activity_chat_layout_text).setVisibility(
					View.GONE);
			view.findViewById(R.id.activity_chat_layout_image).setVisibility(
					View.GONE);
			typeflag = 2;
		} else if (type != null && type.contains(IM.MEDIA_IMAGE)) {
			System.out.println("image");
			view.findViewById(R.id.activity_chat_layout_image).setVisibility(
					View.VISIBLE);
			view.findViewById(R.id.activity_chat_layout_text).setVisibility(
					View.GONE);
			view.findViewById(R.id.activity_chat_layout_voice).setVisibility(
					View.GONE);
			typeflag = 3;
		} else
			System.out.println("other");

		// 数据填充
		if (typeflag == 1) {// 文字信息
			String bodyStr = cursor.getString(cursor
					.getColumnIndex(SMSColumns.BODY));
			String account = cursor.getString(cursor
					.getColumnIndex(SMSColumns.WHO_ID));

			SpannableStringBuilder builder = insertEmoji(bodyStr);
			if (builder == null)
				holder.content.setText(bodyStr);
			else
				holder.content.setText(builder);

			holder.avatar.setTag(account);
			holder.avatar.setImageDrawable(IM.getAvatar(StringUtils
					.parseName(account)));

		} else if (typeflag == 2) {// 语音信息
			String status = cursor.getString(cursor
					.getColumnIndex(SMSColumns.STATUS));
			if (FileMessage.COMPLETE.equals(status)) {
				// 接收成功
				holder.audio.setTag(cursor.getString(cursor
						.getColumnIndex(SMSColumns.FILEPATH)));
			} else if (FileMessage.FAIL.equals(status)) {
				// 接收失败
			} else if (FileMessage.SENDING.equals(status)) {
				// 正在接收
			}
			String account = cursor.getString(cursor
					.getColumnIndex(SMSColumns.WHO_ID));
			holder.avatar.setTag(account);
			holder.avatar.setImageDrawable(IM.getAvatar(StringUtils
					.parseName(account)));
		} else if (typeflag == 3) {// 图片信息
			String status = cursor.getString(cursor
					.getColumnIndex(SMSColumns.STATUS));
			String path = cursor.getString(cursor
					.getColumnIndex(SMSColumns.FILEPATH));
			if (FileMessage.COMPLETE.equals(status)) {
				// 接收成功
				String time = cursor.getString(cursor
						.getColumnIndex(SMSColumns.TIME));
				Bitmap bm = null;

				// 有压缩文件就显示压缩文件
				File temp = new File(IM.CACHE_PATH + time);
				if (temp.exists())
					bm = BitmapTool.decodeBitmap(IM.CACHE_PATH + time,
							DISPLAY_WIDTH, DISPLAY_HEIGHT, true);
				else
					bm = BitmapTool.decodeBitmap(path, DISPLAY_WIDTH,
							DISPLAY_HEIGHT, true);

				holder.image.setImageBitmap(bm);
				holder.image.setTag(path);
			} else if (FileMessage.FAIL.equals(status)) {
				// 接收失败
				holder.image
						.setBackgroundResource(R.drawable.activity_chat_image_fail);
			} else if (FileMessage.SENDING.equals(status)) {
				// 正在接收
				holder.image
						.setBackgroundResource(R.drawable.activity_chat_image_wait);
			}
			String account = cursor.getString(cursor
					.getColumnIndex(SMSColumns.WHO_ID));
			holder.avatar.setTag(account);
			holder.avatar.setImageDrawable(IM.getAvatar(StringUtils
					.parseName(account)));
		}
		// else {// TODO 备用
		// String body = "body="
		// + cursor.getString(cursor.getColumnIndex(SMSColumns.BODY))
		// + "progress="
		// + cursor.getString(cursor
		// .getColumnIndex(SMSColumns.PROGRESS))
		// + "status="
		// + cursor.getString(cursor.getColumnIndex(SMSColumns.STATUS))
		// + "name="
		// + cursor.getString(cursor
		// .getColumnIndex(SMSColumns.SESSION_NAME));
		// String account = cursor.getString(cursor
		// .getColumnIndex(SMSColumns.WHO_ID));
		// holder.content.setText(body);
		// holder.content.setOnClickListener(clickListener);
		// holder.avatar.setTag(account);
		// holder.avatar.setImageDrawable(IM.getAvatar(StringUtils
		// .parseName(account)));
		// }
		return view;

	}

	// 读取消息，根据关键串插入表情
	private SpannableStringBuilder insertEmoji(String body) {
		if (body == null) {
			return null;
		}
		try {
			SpannableStringBuilder builder = new SpannableStringBuilder(body);
			String rexgString = "[/]{1}[1-3]{1}([1]{1}[1-9]{1}|[2]{1}[0-7]{1})";
			Pattern pattern = Pattern.compile(rexgString);
			Matcher matcher = pattern.matcher(body);
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();

				String group = matcher.group().substring(1);
				Field field = R.drawable.class.getDeclaredField("e" + group);// 查找文件，利用随机数
				int result = Integer.parseInt(field.get(null).toString());// 获取图片id

				Drawable drawable = IM.im.getResources().getDrawable(result);
				drawable.setBounds(0, 0, 50, 50);// 这里设置图片的大小

				ImageSpan imageSpan = new ImageSpan(drawable,
						ImageSpan.ALIGN_BASELINE);
				builder.setSpan(imageSpan, start, end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			return builder;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getViewTypeCount() {
		return 2;
	}

	public int getItemViewType(int position) {
		Cursor cursor = (Cursor) getItem(position);
		String whoJid = cursor.getString(cursor
				.getColumnIndex(SMSColumns.WHO_ID));
		// 用户判断
		if (IM.getString(IM.ACCOUNT_JID).equals(whoJid)) {
			return ITEM_RIGHT;
		} else {
			return ITEM_LEFT;
		}
	}

	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return null;
	}

	public void bindView(View view, Context context, Cursor cursor) {
	}

	private static class ViewHolder {
		TextView content;
		RoundedImageView avatar;
		ImageView image;
		Button audio;
	}

	/**
	 * 适配器内容监听器
	 * 
	 * @param clickListener
	 */
	public void setOnChatViewClickListener(View.OnClickListener clickListener) {
		this.clickListener = clickListener;
	}
}