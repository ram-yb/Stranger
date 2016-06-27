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
	 *            传入的位图对象
	 * @param maxSize图片允许最大空间单位
	 *            ：KB
	 * @return
	 */
	public static Bitmap imageZoom(Bitmap bitMap, double maxSize) {

		// 将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		// 将字节换成KB
		double mid = b.length / 1024;
		// 判断bitmap占用空间是否大于允许最大空间 如果大于则压缩 小于则不压缩
		if (mid > maxSize) {
			// 获取bitmap大小 是允许最大大小的多少倍
			double i = mid / maxSize;
			// 开始压缩 此处用到平方根 将宽带和高度压缩掉对应的平方根倍
			// （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
			bitMap = zoomImage(bitMap, bitMap.getWidth() / Math.sqrt(i),
					bitMap.getHeight() / Math.sqrt(i));
		}
		return bitMap;
	}

	/***
	 * 图片的缩放方法
	 * 
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		return bitmap;
	}
	public static Bitmap zoomImageWH(Bitmap bgimage, float newWidth,
			float newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(newWidth, newHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		return bitmap;
	}

	/**
	 * 保存文件
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
	 * 从path中获取图片信息
	 * 
	 * @param path文件路径
	 * @param width适屏宽度
	 * @param height适屏高度
	 * @param zoom是否适屏压缩
	 * @return
	 */
	public static Bitmap decodeBitmap(String path, double width, double height,
			boolean zoom) {
		BitmapFactory.Options op = new BitmapFactory.Options();
		// inJustDecodeBounds
		// If set to true, the decoder will return null (no bitmap), but the
		// out…
		op.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(path, op); // 获取尺寸信息

		if (zoom) {
			// 获取比例大小
			// 对图片的宽度和高度对应屏幕进行匹配
			int hRatio = (int) Math.ceil(op.outHeight / (float) height);
			// 如果大于1，表示图片的高度大于手机屏幕的高度
			int wRatio = (int) Math.ceil(op.outWidth / (float) width);
			// 如果大于1，表示图片的宽度大于手机屏幕的宽度
			// 缩放到1/ratio的尺寸和1/ratio^2像素
			if (hRatio > 1 || wRatio > 1) {
				if (hRatio > wRatio)
					op.inSampleSize = hRatio;// inSampleSize如果被赋值一个大于1
												// 的数，将会请求解码器对图片进行取样，获得一个小图来节约内存
				else
					op.inSampleSize = wRatio;
			}
		}

		op.inJustDecodeBounds = false;
		bmp = BitmapFactory.decodeFile(path, op);
		return bmp;
	}
}
