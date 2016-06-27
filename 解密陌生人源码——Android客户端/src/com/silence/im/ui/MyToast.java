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

		// 获取LayoutInflater对象
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// 由layout文件创建一个View对象
		View layout = inflater.inflate(R.layout.mytoast, null);

		// 得到屏幕宽度和高度
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();

		// 设置layout宽度
		layout.setMinimumWidth(width);

		// 实例化TextView对象
		TextView textView = (TextView) layout.findViewById(R.id.mytoast_text);

		textView.setText(text);

		result.setView(layout);
		result.setMargin(0, 0);
		result.setGravity(Gravity.BOTTOM, 0, 0);
		result.setDuration(duration);

		return result;
	}

}
