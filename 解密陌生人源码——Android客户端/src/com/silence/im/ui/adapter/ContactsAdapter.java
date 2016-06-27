package com.silence.im.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.R;
import com.silence.im.provider.ContactsProvider.ContactColumns;
import com.silence.im.service.Contact;
import com.silence.im.ui.view.PinnedSectionListView.PinnedSectionListAdapter;
import com.silence.im.ui.view.RoundedImageView;

/**
 * ¡™œµ»À  ≈‰∆˜
 * 
 * @author JerSuen
 */
public class ContactsAdapter extends BaseAdapter implements
		PinnedSectionListAdapter {
	private View.OnClickListener clickListener;

	private static final int[] COLORS = new int[] { R.color.green_light,
			R.color.orange_light, R.color.blue_light, R.color.red_light };
	private List<Item> items;

	public ContactsAdapter() {

		Uri uri = Uri
				.parse("content://com.silence.im.provider.ContactsProvider/"
						+ StringUtils.parseName(IM.getString(IM.ACCOUNT_JID))
						+ "____group");
		Cursor group = IM.im.getContentResolver().query(uri, null, null, null,
				ContactColumns.SECTION);

		if (group != null && group.getCount() > 0) {
			items = new ArrayList<ContactsAdapter.Item>();
			for (int i = 0; i < group.getCount(); i++) {
				int sectionPosition = 0, listPosition = 0;
				group.moveToPosition(i);
				String index = group.getString(group
						.getColumnIndex(ContactColumns.SECTION));
				Item section = new Item(Item.SECTION, index, null);
				section.sectionPosition = sectionPosition;
				section.listPosition = listPosition++;
				items.add(section);

				Uri uri1 = Uri
						.parse("content://com.silence.im.provider.ContactsProvider/"
								+ StringUtils.parseName(IM
										.getString(IM.ACCOUNT_JID))
								+ "____contact");
				Cursor entry = IM.im.getContentResolver().query(uri1, null,
						ContactColumns.SECTION + " = ?",
						new String[] { index }, ContactColumns.SORT);

				if (entry != null && entry.getCount() > 0) {
					for (int j = 0; j < entry.getCount(); j++) {
						entry.moveToPosition(j);
						String nickname = entry.getString(entry
								.getColumnIndex(ContactColumns.NICKNAME));
						String account = entry.getString(entry
								.getColumnIndex(ContactColumns.ACCOUNT));
						String sort = entry.getString(entry
								.getColumnIndex(ContactColumns.SORT));
						String name_by_me = entry.getString(entry
								.getColumnIndex(ContactColumns.NAME_BY_ME));

						Contact contact = new Contact();
						contact.account = account;
						contact.nickname = nickname;
						contact.sort = sort;
						contact.index = index;
						contact.name_by_me = name_by_me;

						Item item = new Item(Item.ITEM, nickname, contact);
						item.sectionPosition = sectionPosition;
						item.listPosition = listPosition++;

						items.add(item);
					}
				}
				sectionPosition++;
			}
		}
	}

	public boolean isItemViewTypePinned(int viewType) {
		return viewType == Item.SECTION;
	}

	public int getViewTypeCount() {
		return 2;
	}

	public int getItemViewType(int position) {
		return getItem(position).type;
	}

	public static class Item {

		public static final int ITEM = 0;
		public static final int SECTION = 1;

		public final int type;
		public final String text;
		public final Contact contact;
		public int sectionPosition;
		public int listPosition;
		public boolean show;

		public Item(int type, String text, Contact contact) {
			this.type = type;
			this.text = text;
			this.contact = contact;
		}

		public String toString() {
			return text;
		}

	}

	public int getCount() {
		return (items == null) ? 0 : items.size();
	}

	public Item getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.fragment_contacts_list_item, null);
			holder.avatar = (RoundedImageView) convertView
					.findViewById(R.id.fragment_contacts_list_item_avatar);
			holder.avatar.setOnClickListener(clickListener);
			holder.name = (TextView) convertView
					.findViewById(R.id.fragment_contacts_list_item_name);
			holder.title = (TextView) convertView
					.findViewById(R.id.fragment_contacts_list_item_title);
			holder.layout = convertView
					.findViewById(R.id.fragment_contacts_list_item_layout);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Item item = getItem(position);
		if (item.type == Item.SECTION) {
			holder.layout.setVisibility(View.GONE);
			holder.title.setVisibility(View.VISIBLE);
			holder.title.setText("   " + item.text);
			// holder.title.setBackgroundColor(parent.getResources().getColor(COLORS[position
			// % COLORS.length]));
		} else {
			holder.layout.setVisibility(View.VISIBLE);
			holder.title.setVisibility(View.GONE);
			if (item.contact.name_by_me != null
					&& !item.contact.name_by_me.trim().equals(""))
				holder.name.setText(item.contact.name_by_me);
			else if (item.contact.nickname != null
					&& !item.contact.nickname.trim().equals(""))
				holder.name.setText(item.contact.nickname);
			else
				holder.name
						.setText(StringUtils.parseName(item.contact.account));
			holder.avatar.setImageDrawable(IM.getAvatar(StringUtils
					.parseName(item.contact.account)));
			holder.avatar.setTag(item.contact.account);
		}
		return convertView;
	}

	private static class ViewHolder {
		TextView name, title;
		RoundedImageView avatar;
		View layout;
	}

	/**
	 *   ≈‰∆˜ƒ⁄»›º‡Ã˝∆˜
	 * 
	 * @param clickListener
	 */
	public void setOnItemViewClickListener(View.OnClickListener clickListener) {
		this.clickListener = clickListener;
	}
}