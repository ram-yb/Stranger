package com.silence.im.imageutils;

import java.util.ArrayList;
import java.util.List;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Õº∆¨‰Ø¿¿ÀÈ∆¨  ≈‰∆˜
 * 
 * @author JerSuen
 */
public class ImageFragmentAdapter extends FragmentPagerAdapter {

	private List<String> files;
	private List<ViewImageFragment> views;

	public ViewImageFragment getFragment(int position) {
		return views.get(position);
	}

	public ImageFragmentAdapter(FragmentManager fm, List<String> files) {
		super(fm);
		this.files = files;
		views = new ArrayList<ViewImageFragment>();
	}

	public ViewImageFragment getItem(int position) {

		ViewImageFragment fragment = null;
		fragment = new ViewImageFragment();
		
		System.out.println("files = "+files+"  position = "+position);
		
		fragment.setImagePath(files.get(position));
		views.add(fragment);
		return fragment;
	}

	public int getCount() {
		return files.size();
	}
}