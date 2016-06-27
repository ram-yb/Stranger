package org.jivesoftware.openfire.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadServlet extends HttpServlet {

	private Map<String, String> map;
	private String realPath = null;

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// /�������ļ������Ŀ¼
		String savePath = this.getServletContext().getRealPath(
				"/WEB-INF/upload");
		map = new HashMap<String, String>();
		System.out.println("savePath = " + savePath);
		// ��ʼ��upload����
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setHeaderEncoding("UTF-8");

		if (!upload.isMultipartContent(request)) {
			// /��ͨ���͵ı�
			return;
		}
		// /����ϴ�����
		List<FileItem> list = null;
		try {
			list = upload.parseRequest(request);
		} catch (FileUploadException e1) {
			e1.printStackTrace();
		}
		// �����ļ��������
		for (FileItem item : list) {

			if (item.isFormField()) {
				// /���е���ͨ��������
				String name = item.getFieldName();
				String value = item.getString("UTF-8");
				map.put(name, value);
				System.out.println(name + " = " + value);
			} else {
				// ����ļ���
				String filename = item.getName();

				// �ж��ϴ��ļ�Ϊ��
				if (filename == null || filename.trim().equals(""))
					continue;

				filename = filename.substring(filename.lastIndexOf("\\") + 1);
				String typeString = map.get("mime_type");
				System.out.println("upload : filename = " + filename);
				// /ԭ�ļ������ɶ�Ӧ���漴Ŀ¼
				if (typeString != null && typeString.contains("image")) {
					savePath = savePath + "/image";
					File temp = new File(savePath);
					if (!temp.exists())
						temp.mkdir();
					realPath = EncodePath(filename, savePath);
				} else if (typeString != null && typeString.contains("audio")) {
					savePath = savePath + "/audio";
					File temp = new File(savePath);
					if (!temp.exists())
						temp.mkdir();
					realPath = EncodePath(filename, savePath);
				} else {
					savePath = savePath + "/others";
					File temp = new File(savePath);
					if (!temp.exists())
						temp.mkdir();
					realPath = EncodePath(filename, savePath);
				}

				// ��ȡ�ļ���
				InputStream inputStream = item.getInputStream();
				File file = new File(realPath + filename);
				file.createNewFile();
				FileOutputStream outputStream = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, len);
				}
				inputStream.close();
				outputStream.close();
			}
		}
	}

	public String EnCodeFileName(String filename) {

		return UUID.randomUUID().toString() + "_" + filename;
	}

	public String EncodePath(String filename, String savePath) {

		int hashCode = filename.hashCode();
		int dir1 = hashCode & 0xf;
		File file = new File(savePath + "\\" + dir1);
		if (!file.exists())
			file.mkdir();
		int dir2 = (hashCode & 0xf0) >> 4;
		File file2 = new File(savePath + "\\" + dir1 + "\\" + dir2);
		if (!file2.exists())
			file2.mkdir();
		String path = savePath + "\\" + dir1 + "\\" + dir2 + "\\";
		return path;
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

}
