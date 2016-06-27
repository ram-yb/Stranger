package com.silence.im;

import java.util.ArrayList;

public class List {

	public ArrayList<String> list = new ArrayList<String>();

	public void getString(String str) {
		list.add(str);
	}

	public String getString(int i) {
		return list.get(i);
	}

}
