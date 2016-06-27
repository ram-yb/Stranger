package com.silence.im.ui;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.silence.im.IM;
import com.silence.im.R;
import com.silence.im.R.color;

/**
 * ��ʾ��������÷�
 */
@SuppressLint("NewApi")
public class LocationActivity extends Activity {

	private ArrayList<String> list = new ArrayList<String>();

	/**
	 * MapView �ǵ�ͼ���ؼ�
	 */
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private List<Marker> mMarkers = new ArrayList<Marker>();
	BitmapDescriptor bdA, bdB, bdC, bdD, bd, bdGround;

	private ImageButton titleBtn;
	private TextView titleText;

	private void init() {
		// ��ʼ��ȫ�� bitmap ��Ϣ������ʱ��ʱ recycle
		bdA = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
		bdB = BitmapDescriptorFactory.fromResource(R.drawable.icon_markb);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		init();
		setContentView(R.layout.activity_location);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// �Զ���ActionBar����
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				LocationActivity.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("��������");

		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		list = getIntent().getStringArrayListExtra("list");

		LatLng ll = new LatLng(Double.parseDouble(list.get(1).toString()),
				Double.parseDouble(list.get(2).toString()));
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.setMapStatus(u);
		initOverlay();
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				for (int i = 1; i < mMarkers.size(); i++) {
					System.out.println("i = " + i + "  marker = "
							+ marker.toString() + "  mMarkers.get(i) = ");
					if (marker == mMarkers.get(i)) {
						// MyToast.makeText(getApplication(),
						// "this is the " + i + " marker",
						// MyToast.LENGTH_LONG).show();

						Intent intent = new Intent(LocationActivity.this,
								PuzzleGame.class);
						intent.putExtra("sessionID",
								StringUtils.parseBareAddress(list.get(4 * i)));
						startActivity(intent);
						LocationActivity.this.finish();
					}
				}
				return true;
			}
		});
	}

	// ��ʾ�û��ǳ�
	public void initOverlay() {
		// add marker overlay
		// ��ȡ�ǳ�
		String nickname = list.get(3);

		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.mymaker, null);
		// �޸�view
		TextView nameTextView = (TextView) view
				.findViewById(R.id.text_mymarker);
		ImageView ImageView = (ImageView) view
				.findViewById(R.id.image_mymarker);
		nameTextView.setText(nickname);
		nameTextView.setTextColor(color.myfav1);
		ImageView.setBackgroundResource(R.drawable.icon_markb);

		// add marker overlay
		Marker mMarkerMe;
		LatLng llMe = new LatLng(Double.parseDouble(list.get(1).toString()),
				Double.parseDouble(list.get(2).toString()));
		OverlayOptions ooM = new MarkerOptions().position(llMe)
				.icon(BitmapDescriptorFactory.fromBitmap(getViewBitmap(view)))
				.zIndex(9).draggable(true);

		mMarkerMe = (Marker) (mBaiduMap.addOverlay(ooM));
		mMarkers.add(mMarkerMe);

		for (int i = 1; i < list.size() / 4; i++) {

			Marker mMarker = null;
			String nickname1 = list.get(4 * i + 3);

			nameTextView.setText(nickname1);
			nameTextView.setTextColor(Color.RED);
			ImageView.setBackgroundResource(R.drawable.icon_marka);

			LatLng llm = new LatLng(Double.parseDouble(list.get(4 * i + 1)),
					Double.parseDouble(list.get(4 * i + 2)));

			OverlayOptions oom = new MarkerOptions()
					.position(llm)
					.icon(BitmapDescriptorFactory
							.fromBitmap(getViewBitmap(view))).zIndex(9)
					.draggable(true);

			mMarker = (Marker) (mBaiduMap.addOverlay(oom));
			mMarkers.add(mMarker);
		}
	}

	// viewת����ͼƬ
	private Bitmap getViewBitmap(View addViewContent) {

		addViewContent.setDrawingCacheEnabled(true);

		addViewContent.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		addViewContent.layout(0, 0, addViewContent.getMeasuredWidth(),
				addViewContent.getMeasuredHeight());

		addViewContent.buildDrawingCache();
		Bitmap cacheBitmap = addViewContent.getDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

		return bitmap;
	}

	/**
	 * �������Overlay
	 * 
	 * @param view
	 */
	public void clearOverlay(View view) {
		mBaiduMap.clear();
	}

	/**
	 * �������Overlay
	 * 
	 * @param view
	 */
	public void resetOverlay(View view) {
		clearOverlay(null);
		initOverlay();
	}

	@Override
	protected void onPause() {
		// MapView������������Activityͬ������activity����ʱ�����MapView.onPause()
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// MapView������������Activityͬ������activity�ָ�ʱ�����MapView.onResume()
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(IM.MESSAGE_NOTIFICATION);
	}

	@Override
	protected void onDestroy() {
		// MapView������������Activityͬ������activity����ʱ�����MapView.destroy()
		mMapView.onDestroy();
		// ���� bitmap ��Դ
		bdA.recycle();
		bdB.recycle();
		// bd.recycle();
		// bdGround.recycle();
		super.onDestroy();
	}

}
