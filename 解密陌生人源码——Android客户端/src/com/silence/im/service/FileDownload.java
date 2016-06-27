package com.silence.im.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import android.os.AsyncTask;
import android.util.Log;

import com.silence.im.IM;
import com.silence.im.util.FileTransferHelper;
import com.stranger.client.util.FileMessage;
//传参顺序
//String fileName = fileMessage.getFileName();
//String saveFileName = fileMessage.getSaveFileName();
//String mime_type = fileMessage.getMime_type();
//String md5_from = fileMessage.getFileMD5Code();
//long date = fileMessage.getDate();

public class FileDownload extends
		AsyncTask<Map<String, String>, Integer, Boolean> {

	protected FileTransferHelper helper;
	private boolean result;
	protected String path;

	@Override
	protected Boolean doInBackground(Map<String, String>... params) {
		// 初始化参数值
		Map<String, String> map = params[0];
		// 获得下载输入流
		helper = new FileTransferHelper();

		System.out.println("FileDownload-->>filename = " + map.get("fileName")
				+ "  saveFileName = " + map.get("saveFileName")
				+ " mime_type =  " + map.get("mime_type"));

		InputStream inputStream = helper.DownloadFile(map.get("fileName"),
				map.get("saveFileName"), map.get("mime_type"), map.get("date"));
		// 判断语音图片
		path = null;
		if (map.get("mime_type").contains("image")) {
			// 语音
			path = IM.IMAGE_PATH;
		} else if (map.get("mime_type").contains("audio")) {
			// 图片
			path = IM.AUDIO_PATH;
		} else {
			path = IM.DOWNLOAD_PATH;
		}

		path = path + UUID.randomUUID() + map.get("fileName");
		// 获得文件输出流
		File file = new File(path);
		Log.i("MyTAG-->>", path + "/" + UUID.randomUUID() + map.get("fileName"));
		try {
			if (!file.exists())
				file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
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

			inputStream.close();
			outputStream.close();
			helper.cancelDownload();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.i("MyLog-->>", "MyTAG-->>>New a File");
		// 校验MD5值
		String md5_now = helper.getFileMD5(file);
		result = md5_now.equals(map.get("md5"));
		System.out.println("rec--download-over>>" + FileMessage.SENDING
				+ "result = " + result);

		return result;
	}
}
