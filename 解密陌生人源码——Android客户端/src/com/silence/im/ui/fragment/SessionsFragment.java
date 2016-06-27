package com.silence.im.ui.fragment;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.silence.im.R;
import com.silence.im.service.Contact;
import com.silence.im.ui.ChatActivity;
import com.silence.im.ui.adapter.SessionsAdapter;
import com.silence.im.ui.adapter.SessionsAdapter.Item;

/**
 * �Ự�б�
 * 
 * @author JerSuen
 */
public class SessionsFragment extends ListFragment implements
		AdapterView.OnItemClickListener {

	private SessionsAdapter adapter;
	private ContentObserver co;

	public SessionsFragment() {
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new SessionsAdapter();

		// ���ݹ۲���
		co = new ContentObserver(new Handler()) {
			public void onChange(boolean selfChange) {
				// ���ݿ����ݸı䣬ˢ��������
				System.out.println("SessionsFragment change");
				adapter = new SessionsAdapter();
				getListView().setAdapter(adapter);
			}
		};

		Uri uri1 = Uri.parse("content://com.silence.im.provider.SMSProvider");
		// ע�����ݹ۲���
		getActivity().getContentResolver().registerContentObserver(uri1, true,
				co);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_sessions, container, false);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setAdapter(adapter);
		getListView().setOnItemClickListener(this);
	}

	public void onDestroy() {
		// �������ݹ۲���
		getActivity().getContentResolver().unregisterContentObserver(co);
		super.onDestroy();
	}

	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		// ����������
		Item item = (Item) adapter.getItem(i);
		Contact contact = new Contact();
		contact.account = item.account;
		contact.nickname = item.name;

		// ��δ����Ϣ�ÿ�
		// Uri uri = Uri.parse("content://com.jersuen.im.provider.SMSProvider/"
		// + StringUtils.parseName(IM.getString(IM.ACCOUNT_JID)) + "____sms");
		// ContentValues values = new ContentValues();
		// values.put(SMSColumns.UNREAD, 0);

		startActivity(new Intent(getActivity(), ChatActivity.class).putExtra(
				ChatActivity.EXTRA_CONTACT, contact));
		//
		// getActivity().getContentResolver().update(uri, values,
		// SMSColumns.SESSION_ID + "=?", new String[] { contact.account });
	}
}
