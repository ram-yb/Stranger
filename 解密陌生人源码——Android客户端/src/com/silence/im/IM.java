package com.silence.im;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.text.TextUtils;

import com.silence.im.service.IXmppManager;
import com.silence.im.util.LogUtils;

/**
 * @author Silence
 */
public class IM extends Application {

	// 头像文件夹
	public static final String AVATAR_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/Stranger/avatar/";
	public static final String DOWNLOAD_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/Stranger/download/";
	public static final String IMAGE_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/Stranger/image/";
	public static final String AUDIO_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/Stranger/audio/";
	public static final String CACHE_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/Stranger/cache/";

	public static final String MEDIA_IMAGE = "image";
	public static final String MEDIA_AUDIO = "audio";

	public static final String ACCOUNT_JID = "account_jid";
	public static final String ACCOUNT_PASSWORD = "account_password";
	public static final String ACCOUNT_NICKNAME = "account_nickname";
	public static final String ACCOUNT_REALNAME = "account_realname";
	public static final String ACCOUNT_EMAIL = "account_email";
	public static final String ACCOUNT_GENDER = "account_gender";
	public static final String TEMP = "temp";
	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";

	// 版本状态相关
	public static final String VERSION_KEY = "version";
	public static final String PACKAGE_NAME = "com.silence.im";

	// 在线状态
	public static final String ONLINE_STATUS = "online_status";
	public static final int AVAILABLE = 0;
	public static final int UNAVAILABLE = 1;

	// 聊天状态
	public static final String CHAT_STATUS = "chat_status";
	public static final String CHATTING = "1";
	public static final String NO_CHATTING = "0";

	public static final int MESSAGE_NOTIFICATION = 111;

	public static String HOST;
	public static int PORT;
	public static String IMAGE_MAX_SIZE;
	public static IM im;

	public void onCreate() {
		super.onCreate();
		im = this;
		initProperties();
	}

	public static int[] resIds1 = new int[] {
			R.drawable.e111,// 图片信息数组
			R.drawable.e112, R.drawable.e113, R.drawable.e114, R.drawable.e115,
			R.drawable.e116, R.drawable.e117, R.drawable.e211, R.drawable.e119,
			R.drawable.e120, R.drawable.e121, R.drawable.e122, R.drawable.e123,
			R.drawable.e124, R.drawable.e125, R.drawable.e126, R.drawable.e127,
			R.drawable.emoji_del };
	public static int[] resIds2 = new int[] {
			R.drawable.e211,// 图片信息数组
			R.drawable.e212, R.drawable.e213, R.drawable.e214, R.drawable.e215,
			R.drawable.e216, R.drawable.e217, R.drawable.e218, R.drawable.e219,
			R.drawable.e220, R.drawable.e221, R.drawable.e222, R.drawable.e223,
			R.drawable.e224, R.drawable.e225, R.drawable.e226, R.drawable.e227,
			R.drawable.emoji_del };
	public static int[] resIds3 = new int[] {
			R.drawable.e311,// 图片信息数组
			R.drawable.e312, R.drawable.e313, R.drawable.e314, R.drawable.e315,
			R.drawable.e316, R.drawable.e317, R.drawable.e318, R.drawable.e319,
			R.drawable.e320, R.drawable.e321, R.drawable.e322, R.drawable.e323,
			R.drawable.e324, R.drawable.e325, R.drawable.e326, R.drawable.e327,
			R.drawable.emoji_del };

	public static boolean checkXMPPConnection(IXmppManager xmppManager) {
		try {
			if (!xmppManager.isConnected())
				xmppManager.connect();
			if (!xmppManager.isLogin())
				xmppManager.login();
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 插入字符串
	 * 
	 * @param key
	 * @param value
	 * @return 插入结果
	 */
	public static boolean putString(String key, String value) {
		SharedPreferences settings = im.getSharedPreferences("im_account",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		return editor.commit();
	}

	/**
	 * 获取字符串
	 * 
	 * @param key
	 * @return 默认值为空字符串
	 */
	public static String getString(String key) {
		SharedPreferences settings = im.getSharedPreferences("im_account",
				MODE_PRIVATE);
		return settings.getString(key, "");
	}

	public static boolean initProperties() {
		Properties properties = new Properties();
		try {
			properties.load(im.getAssets().open("property.properties"));
			HOST = properties.getProperty("host");
			IMAGE_MAX_SIZE = properties.getProperty("imagesize");
			PORT = Integer.parseInt(properties.getProperty("port"));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 插入字符串
	 * 
	 * @param key
	 * @param value
	 * @return 插入结果
	 */
	public static boolean putSetting(String key, int value) {
		SharedPreferences settings = im.getSharedPreferences("setting",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(key, value);
		return editor.commit();
	}

	/**
	 * 获取字符串
	 * 
	 * @param key
	 * @return 默认值为空字符串
	 */
	public static int getSetting(String key) {
		SharedPreferences settings = im.getSharedPreferences("setting",
				MODE_PRIVATE);
		return settings.getInt(key, 0);
	}

	public static byte[] getFile(String fileName, String directory) {
		FileInputStream fis = null;
		try {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				String SDCardPath = Environment.getExternalStorageDirectory()
						.getPath() + directory;
				File file = new File(SDCardPath, fileName);
				fis = new FileInputStream(file);
			} else {
				fis = im.openFileInput(fileName);
			}
			int length = fis.available();
			byte[] buffer = new byte[length];
			fis.read(buffer);
			fis.close();
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
			LogUtils.LOGE(IM.class, "getFile()" + e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static Drawable getAvatar(String fileName) {
		byte[] bytes = getFile(fileName, AVATAR_PATH);
		if (bytes != null) {
			if (bytes.length > 0) {
				Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
						bytes.length);
				return IM.Bitmap2Drawable(bitmap);
			}
		}
		return IM.im.getResources().getDrawable(R.drawable.ic_launcher);
	}

	public static boolean saveAvatar(byte[] bytes, String fileName) {
		if (bytes == null || TextUtils.isEmpty(fileName)) {
			return false;
		}
		return saveFile(bytes, fileName, AVATAR_PATH);
	}

	/**
	 * 保存文件
	 */
	public static boolean saveFile(byte[] bytes, String fileName,
			String directory) {
		FileOutputStream fos = null;
		try {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				String SDCardPath = Environment.getExternalStorageDirectory()
						.getPath() + directory;
				File fileDirectory = new File(SDCardPath);
				if (!fileDirectory.exists()) {
					fileDirectory.mkdirs();
				}
				File file = new File(fileDirectory, fileName);
				fos = new FileOutputStream(file);
			} else {
				fos = im.openFileOutput(fileName, MODE_PRIVATE);
			}
			fos.write(bytes);
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			LogUtils.LOGE(IM.class, "saveFile()" + e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * 获取拍照文件
	 * 
	 * @return 拍照文件
	 */
	@SuppressLint("NewApi")
	public static File getCameraFile() {
		// 使用系统当前日期加以调整作为照片的名称
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		// 拍照文件
		return new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
				dateFormat.format(date) + ".jpg");
	}

	/**
	 * 启动系统裁剪
	 * 
	 * @param activity
	 * @param data
	 * @param picCode
	 */

	public static void doCropPhoto(Activity activity, Uri data, int picCode) {
		Intent intent = getCropImageIntent(data);
		System.out.println("intent is OK");
		activity.startActivityForResult(intent, picCode);
	}

	public static Intent getCropImageIntent(Uri data) {
		System.out.println("剪裁");
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(data, "image/*");
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		return intent;
	}

	/**
	 * Bitmap转byte[]
	 * 
	 * @param bitmap
	 *            要转换的bitmap文件
	 * @return 转换好的byte[]
	 */
	public static byte[] Bitmap2Bytes(Bitmap bitmap) {

		if (bitmap == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static Drawable Bitmap2Drawable(Bitmap bitmap) {
		return new BitmapDrawable(im.getResources(), bitmap);
	}
}