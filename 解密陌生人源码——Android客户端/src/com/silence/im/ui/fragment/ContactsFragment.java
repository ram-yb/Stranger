package com.silence.im.ui.fragment;

import org.jivesoftware.smack.util.StringUtils;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.MainActivity;
import com.silence.im.R;
import com.silence.im.provider.SMSProvider.SMSColumns;
import com.silence.im.ui.ChatActivity;
import com.silence.im.ui.UserActivity;
import com.silence.im.ui.adapter.ContactsAdapter;
import com.silence.im.ui.adapter.ContactsAdapter.Item;

/**
 * ��ϵ���б�
 * 
 * @author JerSuen
 */
public class ContactsFragment extends ListFragment implements
		OnItemClickListener, View.OnClickListener, OnItemLongClickListener,
		View.OnLongClickListener {
	private ContactsAdapter adapter;
	private ContentObserver co;
	private Dialog selectDialog;

	public ContactsFragment() {
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ���ݹ۲���
		co = new ContentObserver(new Handler()) {
			public void onChange(boolean selfChange) {
				adapter = new ContactsAdapter();
				adapter.setOnItemViewClickListener(ContactsFragment.this);
				getListView().setAdapter(adapter);
			}
		};
		// ע��۲���
		Uri uri1 = Uri
				.parse("content://com.silence.im.provider.ContactsProvider");
		getActivity().getContentResolver().registerContentObserver(uri1, true,
				co);

	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_contacts, container, false);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// ������������
		adapter = new ContactsAdapter();
		adapter.setOnItemViewClickListener(this);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener(this);
	}

	public void onDestroy() {
		super.onDestroy();
		// �Ƴ��۲���
		getActivity().getContentResolver().unregisterContentObserver(co);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Item item = adapter.getItem(position);
		ContentValues values = new ContentValues();
		values.put(SMSColumns.UNREAD, 0);
		if (item.contact != null) {
			Uri uri = Uri
					.parse("content://com.silence.im.provider.SMSProvider/"
							+ StringUtils.parseName(IM
									.getString(IM.ACCOUNT_JID)) + "____sms");
			startActivity(new Intent(getActivity(), ChatActivity.class)
					.putExtra(ChatActivity.EXTRA_CONTACT, item.contact));
			getActivity().getContentResolver().update(uri, values,
					SMSColumns.SESSION_ID + "=?",
					new String[] { item.contact.account });
		}
	}

	public void onClick(View v) {
		if (v.getTag() != null) {
			if (v.getId() == R.id.fragment_contacts_list_item_avatar) {
				Intent intent = new Intent(getActivity(), UserActivity.class);
				intent.putExtra(UserActivity.EXTRA_ID, v.getTag().toString());
				System.out
						.println("ContactsFragment-->>v.getTag().toString() = "
								+ v.getTag().toString());
				// // ��δ����Ϣ�ÿ�
				// Uri uri = Uri
				// .parse("content://com.jersuen.im.provider.SMSProvider/"
				// + StringUtils.parseName(IM
				// .getString(IM.ACCOUNT_JID)) + "____sms");
				// ContentValues values = new ContentValues();
				// values.put(SMSColumns.UNREAD, 0);
				startActivity(intent);
				// getActivity().getContentResolver().update(uri, values,
				// SMSColumns.SESSION_ID + "=?",
				// new String[] { v.getTag().toString() });
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		Item item = adapter.getItem(arg2);

		if (item.contact != null) {

			// ��ɾ�������˺�
			final String account = item.contact.account;
			View popupView = getLayoutInflater(null).inflate(
					R.layout.contacts_long_click_layout, null);

			final PopupWindow popupWindowGroup = new PopupWindow(popupView,
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
			popupWindowGroup.setTouchable(true);
			popupWindowGroup.setOutsideTouchable(true);
			popupWindowGroup.setBackgroundDrawable(new BitmapDrawable(
					getResources(), (Bitmap) null));

			popupWindowGroup.getContentView().setFocusableInTouchMode(true);
			popupWindowGroup.getContentView().setFocusable(true);

			popupWindowGroup.showAtLocation(LayoutInflater.from(getActivity())
					.inflate(R.layout.activity_main, null), Gravity.BOTTOM, 0,
					0);

			// ����
			Button btnCancel = (Button) popupView.findViewById(R.id.cancel);
			btnCancel.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {

					popupWindowGroup.dismiss();
				}
			});

			Button btnDelete = (Button) popupView.findViewById(R.id.delete);
			btnDelete.setOnClickListener(new OnClickListener() {

				public void onClick(View arg0) {

					System.out.println("LongClick");
					deleteContact(account);
					popupWindowGroup.dismiss();
				}
			});

		}

		return true;
	}

	@SuppressWarnings("deprecation")
	private void deleteContact(final String account) {

		/* ��ʼ����ͨ�Ի��򡣲�������ʽ */
		selectDialog = new Dialog(getActivity(), R.style.dialog);
		selectDialog.setCancelable(true);
		selectDialog.setCanceledOnTouchOutside(true);
		/* ������ͨ�Ի���Ĳ��� */
		selectDialog.setContentView(R.layout.dialog_delete);

		Button cacel = (Button) selectDialog.findViewById(R.id.dialog_cacel);
		Button delete = (Button) selectDialog.findViewById(R.id.dialog_delete);
		TextView textView = (TextView) selectDialog
				.findViewById(R.id.dialog_text);

		textView.setText("ȷ��Ҫɾ��������");

		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ɾ������
				selectDialog.dismiss();// ���ضԻ���
				Message message = new Message();
				message.what = 0;
				message.obj = account;
				MainActivity.handler.sendMessage(message);
			}
		});

		cacel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				selectDialog.dismiss();// ���ضԻ���
			}
		});
		Window dialogWindow = selectDialog.getWindow();
		WindowManager m = dialogWindow.getWindowManager();
		Display d = m.getDefaultDisplay(); // ��ȡ��Ļ������
		WindowManager.LayoutParams p = dialogWindow.getAttributes(); // ��ȡ�Ի���ǰ�Ĳ���ֵ
		p.height = (int) (d.getHeight() * 0.3); // �߶�����Ϊ��Ļ��0.3
		p.width = (int) (d.getWidth() * 0.65); // �������Ϊ��Ļ��0.65
		dialogWindow.setAttributes(p);

		selectDialog.show();// ��ʾ�Ի���
	}

	@Override
	public boolean onLongClick(View v) {

		return false;
	}

	// private void deleteContact(final String account) {
	//
	// /* ��ʼ����ͨ�Ի��򡣲�������ʽ */
	// selectDialog = new Dialog(getActivity(), R.style.dialog);
	// // /* selectDialog.setCancelable(false); */
	// /* ������ͨ�Ի���Ĳ��� */
	// selectDialog.setContentView(R.layout.dialog_deletequotation);
	//
	// Button cacel = (Button) selectDialog.findViewById(R.id.cacel);
	// Button delete = (Button) selectDialog.findViewById(R.id.delete);
	// delete.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View arg0) {
	//
	// }
	// });
	//
	// cacel.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View arg0) {
	//
	// selectDialog.dismiss();// ���ضԻ���
	// }
	// });
	// Window dialogWindow = selectDialog.getWindow();
	// WindowManager m = dialogWindow.getWindowManager();
	// Display d = m.getDefaultDisplay(); // ��ȡ��Ļ������
	// WindowManager.LayoutParams p = dialogWindow.getAttributes(); //
	// ��ȡ�Ի���ǰ�Ĳ���ֵ
	// p.height = (int) (d.getHeight() * 0.6); // �߶�����Ϊ��Ļ��0.6
	// p.width = (int) (d.getWidth() * 0.65); // �������Ϊ��Ļ��0.65
	// dialogWindow.setAttributes(p);
	//
	// selectDialog.show();// ��ʾ�Ի���
	// }
}
