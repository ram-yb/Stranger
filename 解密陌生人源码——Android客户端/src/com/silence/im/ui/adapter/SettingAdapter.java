package com.silence.im.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.R;
import com.silence.im.ui.view.RoundedImageView;

public class SettingAdapter extends BaseAdapter {

	private static final int ACCOUNT = 0;
	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final int TEXTVIEW = 1;
	private static final int NO_VIEW = 2;
	private static final String COLOR = "color";

	private View.OnClickListener listener;;
	private List<Map<String, Object>> list;
	private Context context;

	public SettingAdapter(Context context) {
		this.context = context;
		list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;

		// 标题1：账户
		map = new HashMap<String, Object>();
		map.put(TYPE, NO_VIEW);
		map.put(NAME, "账户");
		list.add(map);
		map = null;
		// 账号
		map = new HashMap<String, Object>();
		map.put(TYPE, ACCOUNT);
		list.add(map);
		map = null;
		// 在线状态
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "在线");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;

		// 标题2：消息通知
		map = new HashMap<String, Object>();
		map.put(NAME, "消息通知");
		map.put(TYPE, NO_VIEW);
		list.add(map);
		map = null;
		// 声音
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "声音提醒");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;
		// 通知
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "通知栏提醒");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;

		// 标题3： 聊天记录
		map = new HashMap<String, Object>();
		map.put(NAME, "聊天记录");
		map.put(TYPE, NO_VIEW);
		list.add(map);
		map = null;
		// 清空聊天记录
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "清空聊天记录");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;
		// 清空聊天图片
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "清空聊天图片");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;
		// 清空缓存
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "清空缓存");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;

		// 清空设置参数
		map = new HashMap<String, Object>();
		map.put(NAME, " ");
		map.put(TYPE, NO_VIEW);
		list.add(map);
		map = null;
		map = new HashMap<String, Object>();
		map.put(NAME, "恢复初始设置");
		map.put(TYPE, TEXTVIEW);
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;

		// 关于
		map = new HashMap<String, Object>();
		map.put(NAME, " ");
		map.put(TYPE, NO_VIEW);
		list.add(map);
		map = null;
		map = new HashMap<String, Object>();
		map.put(NAME, "关于");
		map.put(TYPE, TEXTVIEW);
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.activity_setting_account_item, null);
			holder.content = (TextView) convertView
					.findViewById(R.id.activity_setting_account_content);
			holder.avatar = (RoundedImageView) convertView
					.findViewById(R.id.activity_setting_account_avatar);
			holder.delete = (Button) convertView
					.findViewById(R.id.activity_setting_account_delete);

			holder.title = (TextView) convertView
					.findViewById(R.id.activity_setting_noview_title);

			holder.textView = (TextView) convertView
					.findViewById(R.id.activity_setting_textview_content);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		int type = (int) list.get(position).get(TYPE);
		switch (type) {
		case ACCOUNT:
			convertView.findViewById(R.id.activity_setting_account)
					.setVisibility(View.VISIBLE);
			convertView.findViewById(R.id.activity_setting_noview)
					.setVisibility(View.GONE);
			convertView.findViewById(R.id.activity_setting_textview)
					.setVisibility(View.GONE);
			holder.avatar.setImageDrawable(IM.getAvatar(StringUtils
					.parseName(IM.getString(IM.ACCOUNT_JID))));
			holder.avatar.setOnClickListener(listener);
			holder.content.setText(StringUtils.parseName(IM
					.getString(IM.ACCOUNT_JID)));
			holder.delete.setText("删除");
			holder.delete.setOnClickListener(listener);
			break;
		case TEXTVIEW:
			convertView.findViewById(R.id.activity_setting_account)
					.setVisibility(View.GONE);
			convertView.findViewById(R.id.activity_setting_textview)
					.setVisibility(View.VISIBLE);
			convertView.findViewById(R.id.activity_setting_noview)
					.setVisibility(View.GONE);
			holder.textView.setText(list.get(position).get(NAME).toString());
			holder.textView.setTextColor(Integer.parseInt(list.get(position)
					.get(COLOR).toString()));
			break;
		case NO_VIEW:
			convertView.findViewById(R.id.activity_setting_account)
					.setVisibility(View.GONE);
			convertView.findViewById(R.id.activity_setting_textview)
					.setVisibility(View.GONE);
			convertView.findViewById(R.id.activity_setting_noview)
					.setVisibility(View.VISIBLE);
			holder.title.setText(list.get(position).get(NAME).toString());
			break;
		default:
			break;
		}
		return convertView;
	}

	public class ViewHolder {
		public TextView content, title, textView;
		public RoundedImageView avatar;
		public Button delete;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setOnClickListener(View.OnClickListener listener) {
		this.listener = listener;
	}
}
