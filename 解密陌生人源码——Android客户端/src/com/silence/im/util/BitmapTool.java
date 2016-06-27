package com.silence.im.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BitmapTool {

	/**
	 * @param bitMap
	 *            �����λͼ����
	 * @param maxSizeͼƬ�������ռ䵥λ
	 *            ��KB
	 * @return
	 */
	public static Bitmap imageZoom(Bitmap bitMap, double maxSize) {

		// ��bitmap���������У�����bitmap�Ĵ�С����ʵ�ʶ�ȡ��ԭ�ļ�Ҫ��
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		// ���ֽڻ���KB
		double mid = b.length / 1024;
		// �ж�bitmapռ�ÿռ��Ƿ�����������ռ� ���������ѹ�� С����ѹ��
		if (mid > maxSize) {
			// ��ȡbitmap��С ����������С�Ķ��ٱ�
			double i = mid / maxSize;
			// ��ʼѹ�� �˴��õ�ƽ���� ������͸߶�ѹ������Ӧ��ƽ������
			// ��1.���̶ֿȺ͸߶Ⱥ�ԭbitmap����һ�£�ѹ����Ҳ�ﵽ������Сռ�ÿռ�Ĵ�С��
			bitMap = zoomImage(bitMap, bitMap.getWidth() / Math.sqrt(i),
					bitMap.getHeight() / Math.sqrt(i));
		}
		return bitMap;
	}

	/***
	 * ͼƬ�����ŷ���
	 * 
	 * @param bgimage
	 *            ��ԴͼƬ��Դ
	 * @param newWidth
	 *            �����ź���
	 * @param newHeight
	 *            �����ź�߶�
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {
		// ��ȡ���ͼƬ�Ŀ�͸�
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// ��������ͼƬ�õ�matrix����
		Matrix matrix = new Matrix();
		// ������������
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// ����ͼƬ����
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		return bitmap;
	}
	public static Bitmap zoomImageWH(Bitmap bgimage, float newWidth,
			float newHeight) {
		// ��ȡ���ͼƬ�Ŀ�͸�
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// ��������ͼƬ�õ�matrix����
		Matrix matrix = new Matrix();
		// ������������
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// ����ͼƬ����
		matrix.postScale(newWidth, newHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		return bitmap;
	}

	/**
	 * �����ļ�
	 * 
	 * @param bm
	 * @param fileName
	 * @throws IOException
	 */
	public static void saveFile(Bitmap bm, String filePath) throws IOException {

		File myCaptureFile = new File(filePath);
		if (!myCaptureFile.exists())
			myCaptureFile.createNewFile();
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(myCaptureFile));
		bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		bos.flush();
		bos.close();
	}

	/**
	 * ��path�л�ȡͼƬ��Ϣ
	 * 
	 * @param path�ļ�·��
	 * @param width�������
	 * @param height�����߶�
	 * @param zoom�Ƿ�����ѹ��
	 * @return
	 */
	public static Bitmap decodeBitmap(String path, double width, double height,
			boolean zoom) {
		BitmapFactory.Options op = new BitmapFactory.Options();
		// inJustDecodeBounds
		// If set to true, the decoder will return null (no bitmap), but the
		// out��
		op.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(path, op); // ��ȡ�ߴ���Ϣ

		if (zoom) {
			// ��ȡ������С
			// ��ͼƬ�Ŀ�Ⱥ͸߶ȶ�Ӧ��Ļ����ƥ��
			int hRatio = (int) Math.ceil(op.outHeight / (float) height);
			// �������1����ʾͼƬ�ĸ߶ȴ����ֻ���Ļ�ĸ߶�
			int wRatio = (int) Math.ceil(op.outWidth / (float) width);
			// �������1����ʾͼƬ�Ŀ�ȴ����ֻ���Ļ�Ŀ��
			// ���ŵ�1/ratio�ĳߴ��1/ratio^2����
			if (hRatio > 1 || wRatio > 1) {
				if (hRatio > wRatio)
					op.inSampleSize = hRatio;// inSampleSize�������ֵһ������1
												// ���������������������ͼƬ����ȡ�������һ��Сͼ����Լ�ڴ�
				else
					op.inSampleSize = wRatio;
			}
		}

		op.inJustDecodeBounds = false;
		bmp = BitmapFactory.decodeFile(path, op);
		return bmp;
	}
}
