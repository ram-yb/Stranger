package com.silence.im.ui.adapter;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.silence.im.R;

/**
 * ע��ҳ������
 * 
 * @author JerSuen
 */
public class SignViewAdapter extends PagerAdapter {

	private View.OnClickListener clickListener;
	private List<View> views;

	public SignViewAdapter(List<View> views) {
		this.views = views;
	}

	public int getCount() {
		return (views == null) ? 0 : views.size();
	}

	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	public Object instantiateItem(ViewGroup container, int position) {
		final View view;
		switch (position) {
		case 0:
			view = views.get(0);
			view.findViewById(R.id.activity_sign_view_create_account_commit)
					.setOnClickListener(clickListener);
			break;
		case 1:
			view = views.get(1);
			view.findViewById(R.id.activity_sign_view_perfect_account_commit)
					.setOnClickListener(clickListener);
			break;
		case 2:
			view = views.get(2);
			view.findViewById(R.id.activity_sign_view_upload_avatar_commit)
					.setOnClickListener(clickListener);
			view.findViewById(R.id.activity_sign_view_upload_avatar_avatar)
					.setOnClickListener(clickListener);
			view.findViewById(R.id.activity_sign_view_upload_avatar_layout)
					.setOnClickListener(clickListener);
			break;
		default:
			view = views.get(position);
		}
		container.addView(view);
		return view;
	}

	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(views.get(position));
	}

	/**
	 * ���������ݼ�����
	 * 
	 * @param clickListener
	 */
	public void setOnSignViewClickListener(View.OnClickListener clickListener) {
		this.clickListener = clickListener;
	}
}
