package com.silence.im;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.silence.im.service.IXmppManager;
import com.silence.im.service.XmppManager;

/**
 * XMPP后台服务
 * 
 * @author JerSuen
 */
public class IMService extends Service {

	private static XmppManager xmppManager;
	private ConnectionConfiguration connectionConfig;
	private IXmppManager.Stub binder;

	private boolean newThread;
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();

	public void onCreate() {
		super.onCreate();
		configureProviderManager(ProviderManager.getInstance());
		binder = createConnection();

		// 定位相关
		mLocationClient = new LocationClient(getApplicationContext());// 声明LocationClient类
		mLocationClient.registerLocationListener(myListener);// 注册监听函数
		LocationClientOption option = new LocationClientOption();// 设置请求参数
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		mLocationClient.start();// 启动请求
		newThread = true;
		thread.start();
	}

	public IBinder onBind(Intent intent) {
		return binder;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			createConnection().connect();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/** 初始化ConnectionConfiguration */
	private ConnectionConfiguration initConnectionConfig() {
		if (connectionConfig == null) {
			connectionConfig = new ConnectionConfiguration(IM.HOST, IM.PORT);
			connectionConfig.setDebuggerEnabled(true);
			connectionConfig
					.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		}
		return connectionConfig;
	}

	/** 创建XmppManager */
	public XmppManager createConnection() {
		if (xmppManager == null) {
			xmppManager = new XmppManager(initConnectionConfig(),
					IM.getString(IM.ACCOUNT_JID),
					IM.getString(IM.ACCOUNT_PASSWORD), this);
		}
		return xmppManager;
	}

	private Thread thread = new Thread(new Runnable() {

		@Override
		public void run() {
			while (newThread) {
				if (!mLocationClient.isStarted())
					mLocationClient.start();
				try {
					Thread.sleep(5 * 60 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Thread is running");
				try {
					if (!xmppManager.isConnected())
						xmppManager.connect();
					if (!xmppManager.isLogin())
						xmppManager.login();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	});

	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}

			IM.putString(IM.LONGITUDE, location.getLongitude() + "");
			IM.putString(IM.LATITUDE, location.getLatitude() + "");
			mLocationClient.stop();
		}
	}

	@Override
	public void onDestroy() {
		if (mLocationClient.isStarted())
			mLocationClient.stop();
		newThread = false;
	};

	public void configureProviderManager(ProviderManager pm) {
		// VCard
		pm.addIQProvider(VCardManager.ELEMENT, VCardManager.NAMESPACE,
				new VCardProvider());
	}
}
