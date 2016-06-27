package com.silence.im.util;

import java.util.ArrayList;
import java.util.Locale;

/**
 * ����תƴ��
 * 
 * @author JerSuen
 */
public class PinYin {
	// ���ַ���ƴ������ĸԭ�����أ���ת��ΪСд
	public static String getPinYin(String input) {
		ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance()
				.get(input);
		StringBuilder sb = new StringBuilder();
		if (tokens != null && tokens.size() > 0) {
			for (HanziToPinyin.Token token : tokens) {
				if (HanziToPinyin.Token.PINYIN == token.type) {
					sb.append(token.target);
				} else {
					sb.append(token.source);
				}
			}
		}
		return sb.toString().toLowerCase(Locale.CHINESE);
	}
}