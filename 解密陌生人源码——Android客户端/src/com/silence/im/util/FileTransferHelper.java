package com.silence.im.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.silence.im.IM;

import android.content.Context;
import android.util.Log;

public class FileTransferHelper {

	private static String FILE_SERVER_ADDRESS;
	private final String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR";
	private Socket socket;
	private HttpURLConnection connection;
	private long fileLength = 0;

	public FileTransferHelper() {
		fileLength = 0;
		Properties properties = new Properties();
		try {
			properties.load(IM.im.getAssets().open("property.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		FILE_SERVER_ADDRESS = "http://" + properties.getProperty("host")
				+ ":8080/FileTransfer/servlet/";
	}

	public void disconnect() {
		connection.disconnect();
	}

	public long getFileLength() {
		return fileLength;
	}

	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}

	// 从HTTP服务器下载
	public InputStream DownloadFile(String fileName, String saveFileName,
			String mime_type, String date) {
		boolean flag = false;
		URL url = null;
		BufferedInputStream inputStream = null;
		try {
			url = new URL(FILE_SERVER_ADDRESS + "download?fileName=" + fileName
					+ "&saveFileName=" + saveFileName + "&mime_type="
					+ mime_type + "&date=" + date);
			Log.i("MyLog-->>", FILE_SERVER_ADDRESS);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setReadTimeout(3000);
			connection.setRequestMethod("POST");

			Log.i("MyLog-->>", "MyTAG-->>>test");
			int responseCode = connection.getResponseCode();
			Log.i("MyTAG-->>", "responseCode = " + responseCode);

			inputStream = null;
			FileOutputStream outputStream = null;
			if (responseCode == 200) {
				Log.i("MyLog-->>", "MyTAG-->>>test1");
				fileLength = connection.getContentLength();
				inputStream = new BufferedInputStream(
						connection.getInputStream());

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inputStream;
	}

	// 向HTTP服务器发送下载确认信息
	public void sendOKToHTTP(String fileName, String saveFileName,
			String mime_type, long date) {
		URL url = null;
		try {
			url = new URL(FILE_SERVER_ADDRESS + "download?fileName=" + fileName
					+ "&saveFileName=" + saveFileName + "&mime_type="
					+ mime_type + "&date=" + date + "&status=" + "success");
			Log.i("MyLog-->>", FILE_SERVER_ADDRESS);
			connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(3000);
			connection.setRequestMethod("GET");
			Log.i("MyLog-->>", "MyTAG-->>>test");
			int responseCode = connection.getResponseCode();
			Log.i("MyTAG-->>", "responseCode = " + responseCode);
			connection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 取消发送（上传）
	public void cancelUpload() throws IOException {
		if (connection != null) {
			connection.disconnect();
			connection = null;
		}
		if (socket != null) {
			socket.shutdownOutput();
			socket.close();
			socket = null;
		}
	}

	// 取消下载
	public void cancelDownload() throws IOException {
		if (connection != null) {
			connection.disconnect();
			connection = null;
		}
	}

	// 计算文件MD5码
	public String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	// 计算文件夹所有文件的MD5码
	public Map<String, String> getDirMD5(File file, boolean listChild) {
		if (!file.isDirectory()) {
			return null;
		}
		// <filepath,md5>
		Map<String, String> map = new HashMap<String, String>();
		String md5;
		File files[] = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory() && listChild) {
				map.putAll(getDirMD5(f, listChild));
			} else {
				md5 = getFileMD5(f);
				if (md5 != null) {
					map.put(f.getPath(), md5);
				}
			}
		}
		return map;
	}

	/**
	 * 表单上传
	 * 
	 * @param params
	 *            传递的普通参数
	 * @param uploadFile
	 *            需要上传的文件
	 * @param fileName
	 *            需要上传文件表单中的名字
	 * @param urlStr
	 *            上传的服务器的路径
	 * @throws IOException
	 */
	public OutputStream uploadForm(Map<String, String> params, File uploadFile)
			throws IOException {

		StringBuilder sb = new StringBuilder();
		/**
		 * 普通的表单数据
		 */
		for (String key : params.keySet()) {
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"" + key + "\""
					+ "\r\n");
			sb.append("\r\n");
			sb.append(params.get(key) + "\r\n");
		}
		/**
		 * 上传文件的头
		 */
		sb.append("--" + BOUNDARY + "\r\n");
		sb.append("Content-Disposition: form-data; name=\""
				+ params.get("saveFileName") + "\"; filename=\""
				+ params.get("saveFileName") + "\"" + "\r\n");
		sb.append("Content-Type: " + params.get("mime_type") + "\r\n");// 如果服务器端有文件类型的校验，必须明确指定ContentType
		sb.append("\r\n");

		byte[] headerInfo = sb.toString().getBytes("UTF-8");
		byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");
		System.out.println(sb.toString());
		// TODO
		URL url = new URL(FILE_SERVER_ADDRESS + "upload");
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + BOUNDARY);
		connection.setRequestProperty(
				"Content-Length",
				String.valueOf(headerInfo.length + uploadFile.length()
						+ endInfo.length));
		connection.setDoOutput(true);

		OutputStream out = connection.getOutputStream();

		InputStream in = new FileInputStream(uploadFile);
		out.write(headerInfo);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) != -1)
			out.write(buf, 0, len);

		out.write(endInfo);
		in.close();
		out.close();
		if (connection.getResponseCode() == 200) {
			System.out.println("上传成功");
		}
		return out;
	}

	/**
	 * Socket上传
	 * 
	 * @param params
	 *            传递的普通参数
	 * @param uploadFile
	 *            需要上传的文件名
	 * @param fileName
	 *            需要上传文件表单中的名字
	 * @param urlStr
	 *            上传的服务器的路径
	 * @return
	 * @throws IOException
	 */
	public OutputStream uploadFromBySocket(Map<String, String> params,
			File uploadFile) throws IOException {

		System.out.println("uploadFromBySocket --mime_type = "
				+ params.get("mime_type"));
		StringBuilder sb = new StringBuilder();
		/**
		 * 普通的表单数据
		 */

		if (params != null)
			for (String key : params.keySet()) {
				sb.append("--" + BOUNDARY + "\r\n");
				sb.append("Content-Disposition: form-data; name=\"" + key
						+ "\"" + "\r\n");
				sb.append("\r\n");
				sb.append(params.get(key) + "\r\n");
			}
		else {
			sb.append("\r\n");
		}
		/**
		 * 上传文件的头
		 */
		sb.append("--" + BOUNDARY + "\r\n");
		sb.append("Content-Disposition: form-data; name=\""
				+ params.get("saveFileName") + "\"; filename=\""
				+ params.get("saveFileName") + "\"" + "\r\n");
		sb.append("Content-Type: " + params.get("mime_type") + "\r\n");// 如果服务器端有文件类型的校验，必须明确指定ContentType
		sb.append("\r\n");

		byte[] headerInfo = sb.toString().getBytes("UTF-8");
		byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");

		System.out.println(sb.toString());

		URL url = new URL(FILE_SERVER_ADDRESS + "upload");
		socket = new Socket(url.getHost(), url.getPort());
		OutputStream os = socket.getOutputStream();

		PrintStream ps = new PrintStream(os, true, "UTF-8");

		// 写出请求头
		ps.println("POST " + FILE_SERVER_ADDRESS + "upload" + " HTTP/1.1");
		ps.println("Content-Type: multipart/form-data; boundary=" + BOUNDARY);
		ps.println("Content-Length: "
				+ String.valueOf(headerInfo.length + uploadFile.length()
						+ endInfo.length));
		ps.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

		// InputStream in = new FileInputStream(uploadFile);
		// // 写出数据
		os.write(headerInfo);
		//
		// byte[] buf = new byte[1024];
		// int len;
		// while ((len = in.read(buf)) != -1)
		// os.write(buf, 0, len);
		//
		// os.write(endInfo);
		//
		// in.close();
		// os.close();
		return os;
	}

}
