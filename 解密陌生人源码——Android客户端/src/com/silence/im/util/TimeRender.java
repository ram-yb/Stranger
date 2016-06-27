package com.silence.im.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeRender {

	private static SimpleDateFormat formatBuilder;

	public static String getDate(String format) {
		formatBuilder = new SimpleDateFormat(format);
		return formatBuilder.format(new Date());
	}

	public static String getTime() {
		return System.currentTimeMillis() + "";
	}

	public static String getDate() {
		return getDate("MM-dd HH:mm:ss");
	}

	public static String getDate(Date date) {
		formatBuilder = new SimpleDateFormat("HH:mm");
		return formatBuilder.format(date);
	}
}
