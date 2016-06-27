package com.silence.im.ui;

import java.util.ArrayList;

import android.R.integer;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Window;
import android.widget.ImageView;

import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.R;
import com.silence.im.service.IXmppManager;

public class ShakeActivity extends Activity {

	private ServiceConnection serviceConnect = new XMPPServiceConnection();
	private IXmppManager xmppManager;

	private SensorManager sensorManager;
	private ImageView imageView;

	private String latitude = null, longitude = null;

	private ArrayList<String> list = new ArrayList<String>();

	private CustomProgressDialog progressDialog;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_shake);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(listener, sensor,
				SensorManager.SENSOR_DELAY_NORMAL);

		imageView = (ImageView) findViewById(R.id.activity_shake_imageview);
		AnimatorSet set = new AnimatorSet();
		ObjectAnimator anim = ObjectAnimator.ofFloat(imageView, "rotationY",
				0.0f, 360.0f);
		anim.setDuration(6000);
		anim.setRepeatCount(100000);
		set.play(anim);
		set.start();

		// dialog = new ProgressDialog(ShakeActivity.this);
		// dialog.setTitle("附近的人");
		// dialog.setMessage("正在获取附近的人...");
		progressDialog = CustomProgressDialog.createDialog(this);
		progressDialog.setMessage("loading....");

		initList();
	}

	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnect);
		if (sensorManager != null) {
			sensorManager.unregisterListener(listener);
		}
	}

	private SensorEventListener listener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent arg0) {

			float xValue = Math.abs(arg0.values[0]);
			float yValue = Math.abs(arg0.values[1]);
			float zValue = Math.abs(arg0.values[2]);
			if (xValue > 12 || yValue > 12 || zValue > 12) {

				sensorManager.unregisterListener(listener);
				new LocationTask().execute();
			}
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}
	};

	private void initList() {
		list.clear();
		list.add(0, IM.getString(IM.ACCOUNT_JID));
		latitude = IM.getString(IM.LATITUDE);
		longitude = IM.getString(IM.LONGITUDE);
		list.add(1, latitude);
		list.add(2, longitude);
		list.add(IM.getString(IM.ACCOUNT_NICKNAME));
	}

	private class LocationTask extends AsyncTask<String, integer, String[]> {

		@Override
		protected String[] doInBackground(String... params) {

			// 判断longitude和latitude为空
			if (longitude == null || latitude == null) {
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (longitude == null || latitude == null) {
				return null;
			}

			String[] tttt = null;
			initList();
			try {
				tttt = xmppManager.uploadLocation(longitude, latitude);
				int i = 1;
				for (String t : tttt) {
					System.out.println("result--" + (i++) + "-->>" + t);

					String[] temp = t.split(";");
					list.add(temp[4]);
					// TODO
					list.add(temp[1]);
					list.add(temp[2]);
					list.add(temp[3]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("list-->>" + list.toString());
			return tttt;
		}

		@Override
		protected void onPostExecute(String[] result) {
			progressDialog.dismiss();
			if (result == null) {
				MyToast.makeText(ShakeActivity.this, "附近的人获取失败，请重试...",
						MyToast.LENGTH_SHORT).show();
				sensorManager.registerListener(listener, sensorManager
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_NORMAL);
			} else {
				Intent intent = new Intent(ShakeActivity.this,
						LocationActivity.class);
				System.out.println("list1 = " + list.toString());
				intent.putStringArrayListExtra("list", list);
				startActivity(intent);
				ShakeActivity.this.finish();
			}
		}

		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}
	}

	/** 连接服务 */
	private class XMPPServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName componentName,
				IBinder iBinder) {
			xmppManager = IXmppManager.Stub.asInterface(iBinder);
		}

		public void onServiceDisconnected(ComponentName componentName) {
			xmppManager = null;
		}
	}

	protected void onStart() {
		super.onStart();
		bindService(new Intent(this, IMService.class), serviceConnect,
				BIND_AUTO_CREATE);
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(IM.MESSAGE_NOTIFICATION);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// sensorManager.registerListener(listener, sensor,
		// SensorManager.SENSOR_DELAY_NORMAL);
	}
}
