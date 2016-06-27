package com.silence.im.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.silence.im.IM;
import com.silence.im.util.BitmapTool;
import com.silence.im.util.FileTransferHelper;
import com.stranger.client.util.FileMessage;

public class FileUpload extends
		AsyncTask<Map<String, String>, Integer, Boolean> {

	protected FileTransferHelper helper;
	private boolean result;
	protected String tempPath;
	protected Map<String, String> map;

	protected final String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR";
	protected byte[] endInfo;

	@Override
	protected Boolean doInBackground(Map<String, String>... params) {

		try {
			endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		// ��ʼ������ֵ
		map = params[0];

		File newFile = new File(map.get("path"));
		if (map.get("mime_type").contains(IM.MEDIA_IMAGE)
				&& map.get("Zoom").equals("true")) {
			// ����Ƿ���ͼƬ������ͼƬѹ��
			double maxSize = Double.parseDouble(map.get("MaxSize"));
			Bitmap bitmap = BitmapTool.decodeBitmap(map.get("path"), 0, 0,
					false);
			bitmap = BitmapTool.imageZoom(bitmap, maxSize);
			tempPath = IM.CACHE_PATH
					+ map.get("date")
					+ map.get("fileName").substring(
							map.get("fileName").lastIndexOf("."));
			try {
				BitmapTool.saveFile(bitmap, tempPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			newFile = new File(tempPath);
		}
		// ����ϴ������
		helper = new FileTransferHelper();
		// ��ȡ�ļ�MD5��
		map.put("md5", helper.getFileMD5(newFile));

		OutputStream outputStream = null;
		try {
			System.out.println("prepare to get socket outputstream");
			outputStream = helper.uploadFromBySocket(map, newFile);
			System.out.println("after get socket outputstream");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// ����ļ�������
		Log.i("MyTAG-->>", map.get("path"));
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(newFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Log.i("MyLog-->>", "MyTAG-->>>prepare to write");
		byte[] buffer = new byte[1024];
		int len = 0;
		int total_len = 0;
		try {
			while ((len = inputStream.read(buffer)) > 0) {
				total_len += len;
				int value = (int) ((total_len / (float) helper.getFileLength()) * 100);
				outputStream.write(buffer, 0, len);
				publishProgress(value);
			}
			outputStream.write(endInfo);
			inputStream.close();
			outputStream.close();
			helper.cancelUpload();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("rec--upload-over>>" + FileMessage.SENDING
				+ "result = " + result);
		return result;
	}

}
