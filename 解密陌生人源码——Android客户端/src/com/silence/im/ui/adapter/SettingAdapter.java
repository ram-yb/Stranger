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

		// ����1���˻�
		map = new HashMap<String, Object>();
		map.put(TYPE, NO_VIEW);
		map.put(NAME, "�˻�");
		list.add(map);
		map = null;
		// �˺�
		map = new HashMap<String, Object>();
		map.put(TYPE, ACCOUNT);
		list.add(map);
		map = null;
		// ����״̬
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "����");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;

		// ����2����Ϣ֪ͨ
		map = new HashMap<String, Object>();
		map.put(NAME, "��Ϣ֪ͨ");
		map.put(TYPE, NO_VIEW);
		list.add(map);
		map = null;
		// ����
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "��������");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;
		// ֪ͨ
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "֪ͨ������");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;

		// ����3�� �����¼
		map = new HashMap<String, Object>();
		map.put(NAME, "�����¼");
		map.put(TYPE, NO_VIEW);
		list.add(map);
		map = null;
		// ��������¼
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "��������¼");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;
		// �������ͼƬ
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "�������ͼƬ");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;
		// ��ջ���
		map = new HashMap<String, Object>();
		map.put(TYPE, TEXTVIEW);
		map.put(NAME, "��ջ���");
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;

		// ������ò���
		map = new HashMap<String, Object>();
		map.put(NAME, " ");
		map.put(TYPE, NO_VIEW);
		list.add(map);
		map = null;
		map = new HashMap<String, Object>();
		map.put(NAME, "�ָ���ʼ����");
		map.put(TYPE, TEXTVIEW);
		map.put(COLOR, Color.GRAY);
		list.add(map);
		map = null;

		// ����
		map = new HashMap<String, Object>();
		map.put(NAME, " ");
		map.put(TYPE, NO_VIEW);
		list.add(map);
		map = null;
		map = new HashMap<String, Object>();
		map.put(NAME, "����");
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
			holder.delete.setText("ɾ��");
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
