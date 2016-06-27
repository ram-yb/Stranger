package org.jivesoftware.openfire.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DownloadServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String filePath = this.getServletContext().getRealPath(
				"/WEB-INF/upload");
		String fileName = req.getParameter("fileName");
		String mime_type = req.getParameter("mime_type");
		String saveFileName = req.getParameter("saveFileName");
		String status = req.getParameter("status");
		if (status != null && status.equals("success")) {
			final File file1 = getFile(resp, filePath, mime_type, saveFileName);
			file1.delete();
			return;
		}

		final File file = getFile(resp, filePath, mime_type, saveFileName);
		resp.setHeader("content-disposition", "attachment;filename="
				+ URLEncoder.encode(saveFileName));
		resp.setHeader("content-length", file.length() + "");
		System.out.println(file.getPath());

		FileInputStream inputStream = new FileInputStream(file);
		OutputStream outputStream = resp.getOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, len);
		}
		inputStream.close();
		outputStream.close();
		// new Thread(new Runnable() {
		//
		// public void run() {
		// try {
		// Thread.sleep(3000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// file.delete();
		// }
		// }).start();
	}

	private File getFile(HttpServletResponse resp, String filePath,
			String mime_type, String saveFileName) throws IOException {
		String realPath = null;
		if (mime_type != null && mime_type.contains("image")) {
			filePath = filePath + "/image";
			realPath = EncodePath(saveFileName, filePath);
		} else if (mime_type != null && mime_type.contains("audio")) {
			filePath = filePath + "/audio";
			realPath = EncodePath(saveFileName, filePath);
		} else {
			filePath = filePath + "/others";
			realPath = EncodePath(saveFileName, filePath);
		}

		final File file = new File(realPath, saveFileName);
		if (!file.exists()) {// 文件不存在的异常
			resp.sendError(0, "file is not exist!");
		}
		return file;
	}

	public String EncodePath(String filename, String savePath) {

		int hashCode = filename.hashCode();
		int dir1 = hashCode & 0xf;
		int dir2 = (hashCode & 0xf0) >> 4;
		String path = savePath + "\\" + dir1 + "\\" + dir2 + "\\";
		return path;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}
}
