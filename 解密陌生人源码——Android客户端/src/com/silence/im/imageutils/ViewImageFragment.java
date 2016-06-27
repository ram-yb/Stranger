package com.silence.im.imageutils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.silence.im.IM;
import com.silence.im.R;
import com.silence.im.util.BitmapTool;

public class ViewImageFragment extends Fragment {

	// ���Ƽ���
	private GestureDetector gestureScanner;
	private ImageOnTouchListener touchListener;
	// ���ƴ�������Ļλ������
	private float locationX, locationY;

	// ��ȡ��Ļ��С
	public static int DISPLAY_WIDTH;
	public static int DISPLAY_HEIGHT;

	// MyImageView������ز���
	private MyImageView myImageView;
	private Bitmap bitmap;
	private String imagePath;
	private ProgressBar progressBar;

	private Handler loadImageHandler = new Handler() {
		public void handleMessage(Message msg) {
			myImageView.setImageBitmap(bitmap);
			progressBar.setVisibility(View.GONE);
			myImageView.setVisibility(View.VISIBLE);
		};
	};

	public ViewImageFragment() {
		DISPLAY_HEIGHT = IM.getSetting("height");
		DISPLAY_WIDTH = IM.getSetting("width");
		touchListener = new ImageOnTouchListener();
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public MyImageView getImageView() {
		return myImageView;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_windows_full_item,
				container, false);
		// myImageView = (MyImageView) view
		// .findViewById(R.id.activity_windows_full_image);
		// bitmap = BitmapTool.decodeBitmap(imagePath, 0, 0, false);
		// myImageView.setImageBitmap(bitmap);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// MyImageViewʵ��������������
		myImageView = (MyImageView) getView().findViewById(
				R.id.fragment_windows_full_image);
		progressBar = (ProgressBar) getView().findViewById(
				R.id.fragment_windows_full_wait);
		// �ȴ�Bitmap����
		myImageView.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {

			@Override
			public void run() {
				bitmap = BitmapTool.decodeBitmap(imagePath, 0, 0, false);

				loadImageHandler.sendEmptyMessage(0);
			}
		}).start();

		// ���Ƽ���ʵ����
		gestureScanner = new GestureDetector(getActivity(),
				new MySimpleGesture());
		// touch����
		myImageView.setOnTouchListener(touchListener);
	}

	float v1[] = new float[9];
	private boolean onePoint = false;
	int kEvent = KEY_INVALID; // invalid
	public static final int KEY_INVALID = -1;

	private class ImageOnTouchListener implements OnTouchListener {
		float baseValue;// �������
		float originalScale;// ���ű���

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if (gestureScanner != null) {// ע�����Ƽ���
				gestureScanner.onTouchEvent(event);
			}
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				baseValue = 0;
				originalScale = myImageView.getScale();
				if (event.getPointerCount() == 1)// ��ⵥָ����
					onePoint = true;
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if (event.getPointerCount() == 2) {
					onePoint = false;
					// ������ָ����
					float x = event.getX(0) - event.getX(1);
					float y = event.getY(0) - event.getY(1);
					float value = (float) Math.sqrt(x * x + y * y);// ��������ľ���

					// System.out.println("value:" + value);
					if (baseValue == 0) {
						baseValue = value;
					} else {
						float scale = value / baseValue;// ��ǰ�����ľ��������ָ����ʱ�����ľ��������Ҫ���ŵı�����
						// scale the image
						if (!(scale < 1 + 1e-6 && scale > 1 - 1e-6)) {// һ���������⣬��������
							myImageView.zoomTo(originalScale * scale,
									x + event.getX(1), y + event.getY(1));
						}
					}
				} else if (event.getPointerCount() == 1) {
					// ��ָ�϶�
					if (onePoint) {
						// ��ȡMyImageView��Matrix����
						Matrix m = myImageView.getImageMatrix();
						m.getValues(v1);
						float left = v1[Matrix.MTRANS_X];

						// ͼƬ��ʵʱ����
						float width = myImageView.getScale()
								* myImageView.getImageWidth();
						float height = myImageView.getScale()
								* myImageView.getImageHeight();

						if ((int) width > DISPLAY_WIDTH
								|| (int) height > DISPLAY_HEIGHT) {// ���ͼƬ��ǰ��С<��Ļ��С��ֱ�Ӵ������¼�
							left = v1[Matrix.MTRANS_X];
							float right = left + width;
							Rect r = new Rect();
							myImageView.getGlobalVisibleRect(r);
							if (event.getX() - locationX < 0)// ���󻬶�
							{
								if (r.left <= 0 && right >= DISPLAY_WIDTH) {// �жϵ�ǰImageView�Ƿ���ʾ��ȫ
									myImageView.postTranslate(event.getX()
											- locationX, event.getY()
											- locationY);
								}
							} else if (event.getX() - locationX > 0)// ���һ���
							{
								if (r.right >= DISPLAY_WIDTH && left <= 0) {
									myImageView.postTranslate(event.getX()
											- locationX, event.getY()
											- locationY);
								}
							}
						}
					}
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				// �жϱ߽��Ƿ�Խ��
				if (kEvent != KEY_INVALID) { // �Ƿ��л���һҳ����һҳ
					kEvent = KEY_INVALID;
				}
				float width = myImageView.getScale()
						* myImageView.getImageWidth();
				float height = myImageView.getScale()
						* myImageView.getImageHeight();
				float v1[] = new float[9];
				Matrix m = myImageView.getImageMatrix();
				m.getValues(v1);
				float top = v1[Matrix.MTRANS_Y];
				float bottom = top + height;
				if (top < 0 && bottom < DISPLAY_HEIGHT) {
					myImageView.postTranslateDur(DISPLAY_HEIGHT / 2 - bottom
							/ 2, 200f);
				}
				if (top > 0 && bottom > DISPLAY_HEIGHT) {
					myImageView.postTranslateDur(-top / 2, 200f);
				}

				float left = v1[Matrix.MTRANS_X];
				float right = left + width;
				if (left < 0 && right < DISPLAY_WIDTH) {
					myImageView.postTranslateXDur(
							DISPLAY_WIDTH / 2 - right / 2, 200f);
				}
				if (left > 0 && right > DISPLAY_WIDTH) {
					myImageView.postTranslateXDur(-left / 2, 200f);
				}
			}
			locationX = event.getX();
			locationY = event.getY();
			return true;
		}
	}

	private class MySimpleGesture extends SimpleOnGestureListener {
		// ˫�����ͼƬ����СͼƬ
		public boolean onDoubleTap(MotionEvent e) {
			if (myImageView.getScale() > myImageView.getMiniZoom()) {
				myImageView.zoomTo(myImageView.getMiniZoom());
			} else {
				myImageView.zoomTo(myImageView.getMaxZoom());
			}
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// Logger.LOG("onSingleTapConfirmed",
			// "onSingleTapConfirmed excute");
			// mTweetShow = !mTweetShow;
			// tweetLayout.setVisibility(mTweetShow ? View.VISIBLE
			// : View.INVISIBLE);
			return true;
		}
	}
}
