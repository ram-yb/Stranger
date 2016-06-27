package com.silence.im.ui.adapter;

import java.util.List;
import java.util.Map;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.silence.im.R;
import com.silence.im.provider.ContactsProvider;
import com.silence.im.util.Base64;

public class SearchAdapter extends BaseAdapter {

	private Context context;
	private List<Map<String, Object>> list;

	public SearchAdapter(Context context, List<Map<String, Object>> list) {
		this.list = list;
		this.context = context;
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

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.activity_add_view_search_account_list_adapter,
					null);
			holder.nickname = (TextView) convertView
					.findViewById(R.id.activity_add_view_search_account_list_nickname);
			holder.avatar = (ImageView) convertView
					.findViewById(R.id.activity_add_view_search_account_list_avatar);
			holder.gender = (TextView) convertView
					.findViewById(R.id.activity_add_view_search_account_list_gender);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		holder.nickname.setText((CharSequence) list.get(position).get(
				"nickname"));
		String avatarsString = (String) list.get(position).get("avatar");
		if (avatarsString != null) {
			byte[] avatarBytes = Base64.decode(avatarsString);
			holder.avatar.setImageBitmap(BitmapFactory.decodeByteArray(
					avatarBytes, 0, avatarBytes.length));
		} else
			holder.avatar.setImageResource(R.drawable.ic_launcher);
		String genderString = (String) list.get(position).get("gender");
		if (genderString.contains(":")) {
			int index = genderString.indexOf(":");
			String gender = genderString.substring(0, index);
			String genderContent = null;
			if (genderString.length() - index > 1)
				genderContent = genderString.substring(index + 1,
						genderString.length());
			if (ContactsProvider.MAN.equals(gender)) {
				holder.gender.setBackgroundResource(R.drawable.man_normal);
			} else if (ContactsProvider.WOMAN.equals(gender)) {
				holder.gender.setBackgroundResource(R.drawable.woman_normal);
			} else if (ContactsProvider.OTHER.equals(gender)) {
				holder.gender.setBackground(null);
				holder.gender.setText(genderContent);
			}
		} else {
			if (genderString.equals(ContactsProvider.MAN))
				holder.gender.setBackgroundResource(R.drawable.man_normal);
			else if (genderString.equals(ContactsProvider.WOMAN))
				holder.gender.setBackgroundResource(R.drawable.woman_normal);
		}
		// String[] genders = genderString.split(":");
		// if (genders[0].equals(ContactsProvider.MAN)) {
		// holder.gender.setBackgroundResource(R.drawable.man_normal);
		// } else if (genders[0].equals(ContactsProvider.WOMAN)) {
		// holder.gender.setBackgroundResource(R.drawable.woman_normal);
		// } else {
		// holder.gender.setText(genders[1]);
		// }
		return convertView;
	}

	public class ViewHolder {
		public ImageView avatar;
		public TextView nickname, gender;
	}

}
