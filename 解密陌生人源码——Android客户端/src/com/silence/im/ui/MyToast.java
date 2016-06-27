package com.silence.im.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.silence.im.R;

public class MyToast extends Toast {

	public MyToast(Context context) {
		super(context);
	}

	public static Toast makeText(Context context, CharSequence text,
			int duration) {
		Toast result = new Toast(context);

		// ��ȡLayoutInflater����
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// ��layout�ļ�����һ��View����
		View layout = inflater.inflate(R.layout.mytoast, null);

		// �õ���Ļ��Ⱥ͸߶�
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();

		// ����layout���
		layout.setMinimumWidth(width);

		// ʵ����TextView����
		TextView textView = (TextView) layout.findViewById(R.id.mytoast_text);

		textView.setText(text);

		result.setView(layout);
		result.setMargin(0, 0);
		result.setGravity(Gravity.BOTTOM, 0, 0);
		result.setDuration(duration);

		return result;
	}

}
