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

	// 手势监听
	private GestureDetector gestureScanner;
	private ImageOnTouchListener touchListener;
	// 手势触摸的屏幕位置坐标
	private float locationX, locationY;

	// 获取屏幕大小
	public static int DISPLAY_WIDTH;
	public static int DISPLAY_HEIGHT;

	// MyImageView及其相关参数
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
		// MyImageView实例化及参数加载
		myImageView = (MyImageView) getView().findViewById(
				R.id.fragment_windows_full_image);
		progressBar = (ProgressBar) getView().findViewById(
				R.id.fragment_windows_full_wait);
		// 等待Bitmap加载
		myImageView.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {

			@Override
			public void run() {
				bitmap = BitmapTool.decodeBitmap(imagePath, 0, 0, false);

				loadImageHandler.sendEmptyMessage(0);
			}
		}).start();

		// 手势监听实例化
		gestureScanner = new GestureDetector(getActivity(),
				new MySimpleGesture());
		// touch监听
		myImageView.setOnTouchListener(touchListener);
	}

	float v1[] = new float[9];
	private boolean onePoint = false;
	int kEvent = KEY_INVALID; // invalid
	public static final int KEY_INVALID = -1;

	private class ImageOnTouchListener implements OnTouchListener {
		float baseValue;// 两点距离
		float originalScale;// 缩放比例

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if (gestureScanner != null) {// 注册手势监听
				gestureScanner.onTouchEvent(event);
			}
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				baseValue = 0;
				originalScale = myImageView.getScale();
				if (event.getPointerCount() == 1)// 检测单指触摸
					onePoint = true;
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if (event.getPointerCount() == 2) {
					onePoint = false;
					// 计算两指距离
					float x = event.getX(0) - event.getX(1);
					float y = event.getY(0) - event.getY(1);
					float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离

					// System.out.println("value:" + value);
					if (baseValue == 0) {
						baseValue = value;
					} else {
						float scale = value / baseValue;// 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。
						// scale the image
						if (!(scale < 1 + 1e-6 && scale > 1 - 1e-6)) {// 一倍缩放以外，进行缩放
							myImageView.zoomTo(originalScale * scale,
									x + event.getX(1), y + event.getY(1));
						}
					}
				} else if (event.getPointerCount() == 1) {
					// 单指拖动
					if (onePoint) {
						// 获取MyImageView的Matrix参数
						Matrix m = myImageView.getImageMatrix();
						m.getValues(v1);
						float left = v1[Matrix.MTRANS_X];

						// 图片的实时宽，高
						float width = myImageView.getScale()
								* myImageView.getImageWidth();
						float height = myImageView.getScale()
								* myImageView.getImageHeight();

						if ((int) width > DISPLAY_WIDTH
								|| (int) height > DISPLAY_HEIGHT) {// 如果图片当前大小<屏幕大小，直接处理滑屏事件
							left = v1[Matrix.MTRANS_X];
							float right = left + width;
							Rect r = new Rect();
							myImageView.getGlobalVisibleRect(r);
							if (event.getX() - locationX < 0)// 向左滑动
							{
								if (r.left <= 0 && right >= DISPLAY_WIDTH) {// 判断当前ImageView是否显示完全
									myImageView.postTranslate(event.getX()
											- locationX, event.getY()
											- locationY);
								}
							} else if (event.getX() - locationX > 0)// 向右滑动
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
				// 判断边界是否越界
				if (kEvent != KEY_INVALID) { // 是否切换上一页或下一页
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
		// 双击最大化图片或缩小图片
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
