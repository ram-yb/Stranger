package com.silence.im.imageutils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import android.graphics.Bitmap;

public class BitmapCache {

	private int MAX_SIZE;
	private Deque<Bitmap> deque;
	private int offset;

	public BitmapCache(int size) {

		MAX_SIZE = size;
		offset = 0;
		deque = new ArrayDeque<Bitmap>();
	}

	public void addBitmap(Bitmap bitmap) {
		deque.addLast(bitmap);
	}

	public Bitmap getBitmap(int position) {
		int pos = position + MAX_SIZE / 2;
		if (pos < 0 || pos > deque.size())
			return null;
		Iterator<Bitmap> iterator = deque.iterator();
		Bitmap bitmap = null;
		for (int i = 0; i <= pos + offset; i++) {
			bitmap = iterator.next();
			if (bitmap == null)
				return bitmap;
		}
		return bitmap;
	}

	public void addRare(Bitmap bitmap) {
		deque.addLast(bitmap);
		if (deque.size() > MAX_SIZE)
			deque.removeFirst();
	}

	public void addFront(Bitmap bitmap) {
		deque.addFirst(bitmap);
		if (deque.size() > MAX_SIZE)
			deque.removeLast();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		for (Bitmap t : deque) {
			buffer.append("[ " + t + " ] ; ");
		}

		return buffer.toString();
	}

	public void decrease() {
		offset--;
	}
}
