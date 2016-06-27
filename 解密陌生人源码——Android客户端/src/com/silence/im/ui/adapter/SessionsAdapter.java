package com.silence.im.ui.adapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jivesoftware.smack.util.StringUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.silence.im.IM;
import com.silence.im.R;
import com.silence.im.provider.SMSProvider;
import com.silence.im.provider.SMSProvider.SMSColumns;
import com.silence.im.ui.view.RoundedImageView;
import com.silence.im.util.TimeRender;

/**
 * 会话列表
 * 
 * @author JerSuen
 */
@SuppressLint("NewApi")
public class SessionsAdapter extends SimpleCursorAdapter {

	private static Uri uri = Uri
			.parse("content://com.silence.im.provider.SMSProvider/"
					+ StringUtils.parseName(IM.getString(IM.ACCOUNT_JID))
					+ "____sessions");
	private List<Item> items;

	public SessionsAdapter() {// 设置近期会话的适配器。内容和名字
		super(IM.im, R.layout.fragment_sessions_item, IM.im
				.getContentResolver().query(uri, null, null, null, null),
				new String[] { SMSProvider.SMSColumns.BODY,
						SMSProvider.SMSColumns.SESSION_NAME }, new int[] {
						R.id.fragment_sessions_item_content,
						R.id.fragment_sessions_item_name },
				FLAG_REGISTER_CONTENT_OBSERVER);

		Cursor cursor = IM.im.getContentResolver().query(uri, null, null, null,
				null);

		if (cursor != null && cursor.getCount() > 0) {
			items = new ArrayList<Item>();
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);

				// 提取数据
				String account = cursor.getString(cursor
						.getColumnIndex(SMSColumns.SESSION_ID));
				String name = cursor.getString(cursor
						.getColumnIndex(SMSColumns.SESSION_NAME));
				String type = cursor.getString(cursor
						.getColumnIndex(SMSColumns.TYPE));
				Date date = new Date(Long.parseLong(cursor.getString(cursor
						.getColumnIndex(SMSColumns.TIME))));
				String time = TimeRender.getDate(date);

				String content = null;
				if (type == null)
					content = "[无消息]";
				else if (type.equals("chat"))
					content = cursor.getString(cursor
							.getColumnIndex(SMSColumns.BODY));
				else if (type.contains(IM.MEDIA_AUDIO))
					content = "[语音]";
				else if (type.contains(IM.MEDIA_IMAGE))
					content = "[图片]";
				else
					content = "[无消息]";

				// 计算未读信息个数
				Uri uri1 = Uri
						.parse("content://com.silence.im.provider.SMSProvider/"
								+ StringUtils.parseName(IM
										.getString(IM.ACCOUNT_JID)) + "____sms");
				Cursor countCursor = IM.im.getContentResolver().query(uri1,
						null, SMSColumns.SESSION_ID + "=?",
						new String[] { account }, SMSColumns.TIME + " desc");
				int count = 0;
				countCursor.moveToFirst();
				count += countCursor.getInt(cursor
						.getColumnIndex(SMSColumns.UNREAD));
				while (countCursor.moveToNext()) {
					count += countCursor.getInt(cursor
							.getColumnIndex(SMSColumns.UNREAD));
				}
				countCursor.close();
				Item item = new Item(account, content, time, count, name);
				items.add(item);
				System.out.println("SessionsAdapter-->> name = " + name
						+ "  items = " + items.toString());
			}
		}
	}

	public static class Item {

		public String account, content, time, name;
		public int count;

		public Item(String account, String content, String time, int count,
				String name) {
			this.account = account;
			this.content = content;
			this.time = time;
			this.count = count;
			this.name = name;
		}

		@Override
		public String toString() {
			return "Item [account=" + account + ", content=" + content
					+ ", time=" + time + ", name=" + name + ", count=" + count
					+ "]";
		}

	}

	public int getCount() {
		return (items == null) ? 0 : items.size();
	}

	public Item getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 创建视图
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.fragment_sessions_item, null);
			holder.avatar = (RoundedImageView) convertView
					.findViewById(R.id.fragment_sessions_item_avatar);
			// holder.avatar.setOnClickListener(clickListener);
			holder.account = (TextView) convertView
					.findViewById(R.id.fragment_sessions_item_name);
			holder.count = (TextView) convertView
					.findViewById(R.id.fragment_sessions_item_count);
			holder.time = (TextView) convertView
					.findViewById(R.id.fragment_sessions_item_time);
			holder.content = (TextView) convertView
					.findViewById(R.id.fragment_sessions_item_content);
			holder.layout = convertView
					.findViewById(R.id.fragment_sessions_item_avatar_layout);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 封装数据
		Item item = getItem(position);
		holder.account.setText(item.name);
		// 消息内容
		SpannableStringBuilder builder = insertEmoji(item.content);
		if (builder != null)
			holder.content.setText(builder);
		else
			holder.content.setText(item.content);
		holder.time.setText(item.time);
		holder.avatar.setImageDrawable(IM.getAvatar(StringUtils
				.parseName(item.account)));
		if (item.count > 0) {
			holder.count.setText(item.count + "");
			holder.count.setVisibility(View.VISIBLE);
		} else {
			holder.count.setVisibility(View.GONE);
		}
		return convertView;
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

	private class ViewHolder {
		TextView content, time, count, account;
		RoundedImageView avatar;
		View layout;
	}

	public void bindView(View view, Context context, Cursor cursor) {
		// 得到最近时间
		// cursor.moveToFirst();
		// Date date = new Date(Long.parseLong(cursor.getString(cursor
		// .getColumnIndex(SMSColumns.TIME))));
		// String time = TimeRender.getDate(date);
		// ((TextView) view.findViewById(R.id.fragment_sessions_item_time))
		// .setText(time);

		// 设置近期会话者头像
		// RoundedImageView avatar = (RoundedImageView) view
		// .findViewById(R.id.fragment_sessions_item_avatar);
		// String account = cursor.getString(cursor
		// .getColumnIndex(SMSColumns.SESSION_ID));
		// avatar.setImageDrawable(IM.getAvatar(StringUtils.parseName(account)));

		// 得到未读信息数目
		// Uri uri1 = Uri.parse("content://com.jersuen.im.provider.SMSProvider/"
		// + IM.getUsername(IM.getString(IM.ACCOUNT_JID)) + "____sms");
		// Cursor cursor2 = IM.im.getContentResolver().query(uri1, null,
		// SMSColumns.SESSION_ID + "=?", new String[] { account },
		// SMSColumns.TIME + " desc");
		//
		// cursor2.moveToFirst();
		// int count =
		// cursor2.getInt(cursor2.getColumnIndex(SMSColumns.UNREAD));
		// while (cursor2.moveToNext()) {
		// count += cursor2.getInt(cursor2.getColumnIndex(SMSColumns.UNREAD));
		// }
		// cursor2.close();
		// System.out.println("time = " + time + "  count = " + count);
		// if (count > 0) {
		// ((TextView) view.findViewById(R.id.fragment_sessions_item_count))
		// .setText(count + "");
		// view.findViewById(R.id.fragment_sessions_item_count).setVisibility(
		// View.VISIBLE);
		// } else {
		// view.findViewById(R.id.fragment_sessions_item_count).setVisibility(
		// View.GONE);
		// }
		//
		// cursor.moveToFirst();
		super.bindView(view, context, cursor);
	}
}