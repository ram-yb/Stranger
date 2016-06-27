package com.silence.im.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.util.StringUtils;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.R;
import com.silence.im.service.Contact;
import com.silence.im.service.IXmppManager;
import com.silence.im.util.Base64;

public class PuzzleGame extends Activity {

	private IXmppManager xmppManager;// ���ӹ�����
	private ServiceConnection serviceConnect = new XMPPServiceConnection();
	private String sessionID;// �Է�JID
	private Contact stranger;// �Է���Ƭ
	TableLayout tableLayout;

	/**
	 * ������ʾ����߹��Ĳ���
	 */
	TextView tvStep;
	/**
	 * ��ŵ���ͼ
	 */
	ImageView ivSamll;
	/**
	 * ��Ž�����Ϸʱ��ͼƬ
	 */
	ImageView ivFugai;
	/**
	 * �û��߹��Ĳ���
	 */
	static int step = 0;
	/**
	 * ����и���ͼƬ
	 */
	Map<Point, MyImageView> map = new HashMap<Point, MyImageView>();

	/**
	 * ƴͼ������
	 */
	static int ROW;
	/**
	 * ƴͼ������
	 */
	static int COL;
	/**
	 * ����û��߹���·��
	 */

	static List<Integer> listPath;
	/**
	 * ���峣��,����Ϊ-1
	 */
	final int UP = -1;
	/**
	 * ���峣��,����Ϊ1
	 */
	final int DOWN = 1;
	/**
	 * ���峣��,����Ϊ-2
	 */
	final int LEFT = -2;
	/**
	 * ���峣��,����Ϊ2
	 */
	final int RIGHT = 2;
	/**
	 * ���ڼ�¼back���鳤��,��Ҫԭ·���ؼ���
	 */
	static int pathlength = 0;
	/**
	 * ��ȡ�ĺ�ɫͼƬ��λ��x
	 */
	private int x;
	/**
	 * ��ȡ�ĺ�ɫͼƬ��λ��y
	 */
	private int y;

	Handler handLer;

	/**
	 * ����һ��Activity��������ͼƬ·��
	 */
	int picPath;
	int picPahtTitle;

	/**
	 * ����·��
	 */
	static List listMost;

	/**
	 * ͼƬ����,���ڴ���и���ͼƬ
	 */
	MyImageView iv[];

	int[] back;// ���ڴ��ͼƬ���·��
	/**
	 * ��ź�ɫ�������x
	 */
	static int backx = 0;
	/**
	 * ��ź�ɫ�������y
	 */
	static int backy = 0;
	/**
	 * �ж���Ϸ�Ƿ������Զ�����
	 */
	static boolean isAuto = true;

	private ImageButton titleBtn;
	private TextView titleText;

	// int key;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamebody);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_common_title);// �Զ���ActionBar����
		titleBtn = (ImageButton) findViewById(R.id.activity_common_title_btn_back);
		titleBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				PuzzleGame.this.finish();
			}
		});
		titleText = (TextView) findViewById(R.id.activity_common_title_text);
		titleText.setText("��  ��");

		tvStep = (TextView) findViewById(R.id.tvStep);
		ivSamll = (ImageView) findViewById(R.id.ivSmall);
		ivFugai = (ImageView) findViewById(R.id.ivFugai);

		picPath = R.drawable.ai002;// ��ȡ��һ��Activity��������ͼƬ·��
		picPahtTitle = R.drawable.ai002;// ��ȡ��һ��Activity��������ͼƬ·��
		//
		// key = it.getIntExtra("key", 0);
		tableLayout = (TableLayout) findViewById(R.id.myTableLayout);// ��ʼ��tableLayout
		ivSamll.setImageResource(picPahtTitle);// ���õ���ͼ·��
		ivFugai.setVisibility(View.INVISIBLE);// ������Ϸ����ʱ������ͼƬ���ɼ�
		ivFugai.setImageResource(picPath);// ������Ϸ����ʱ������ͼƬ·��
		// ��ʼ��Ϸ
		ROW = 3;// ���峤
		COL = 3;// �����
		initView();// ��ʼ����Ϸ����
		gogogo(45);// ����ͼƬ˳��

		sessionID = getIntent().getStringExtra("sessionID");
		System.out.println("sessionID = " + sessionID);
	}

	/**
	 * ����,���������û��߹����ظ�·��,�����Զ����ص����ߵĴ���
	 */
	public void guolv() {
		int sum = 0;
		listMost = new ArrayList();
		back = new int[listPath.size()];

		for (int i = 0; i < listPath.size(); i++) {
			back[i] = listPath.get(i); // ���û����ߵ�·����ӵ�back������
		}
		for (int i = 0; i < back.length; i++) {
			sum = 0;
			for (int j = 0; j < 2; j++) {
				if (i + j < back.length) {// �ж�ǰһ��·�����һ��·���Ƿ��ظ�
					sum += back[i + j];
				}
			}
			if (sum == 0) {
				System.out.println("sum==0");
				i = i + 1;
			} else {
				listMost.add(back[i]);// �����������ͬ·��,����ӵ�����·����list����
			}
		}

	}

	/**
	 * �߳�,��������ԭ��ƴͼ
	 */
	Runnable update = new Runnable() {

		@Override
		public void run() {

			if (pathlength >= 0) {

				gotoback();// ����gotoback����,���ڰ�ԭ������

			} else if (pathlength <= 0) {
				pandun();// ���Զ�ƴͼ��ԭ��,���һ���ж�,������ʾ������MyToast
			}

		}
	};

	/**
	 * �Զ�ԭ·����,ͨ���ж���������,����ԭ����·��,���෴������
	 */
	public void gotoback() {
		switch (back[pathlength]) {
		case UP:
			changePic(map.get(new Point(x + 1, y)).getId());
			x = x + 1;
			break;

		case DOWN:
			changePic(map.get(new Point(x - 1, y)).getId());
			x = x - 1;

			break;
		case LEFT:

			changePic(map.get(new Point(x, y + 1)).getId());
			y = y + 1;

			break;
		case RIGHT:

			changePic(map.get(new Point(x, y - 1)).getId());
			y = y - 1;

			break;
		default:
			break;
		}
		pathlength--;
		step += 1;// ������һ
		tvStep.setText("����:" + step);// ������ʾ����
		handLer.postDelayed(update, 200);// �ȴ�200����֮���߳�������ӵ�handler��
	}

	Bitmap myPic;

	public void addPic() {

		myPic = BitmapFactory.decodeResource(super.getResources(), picPath);// ��ȡͼƬ��Դ
		Matrix m = new Matrix();// ��ȡ������
		WindowManager wm = (WindowManager) this
				.getSystemService(WINDOW_SERVICE);
		int wid = wm.getDefaultDisplay().getWidth() - 20;// ��ȡ��Ļ�Ŀ��
		int hei = wm.getDefaultDisplay().getHeight() / 3 * 2 - 40;// ��ȡ��Ļ�ĸ߶�
		m.postScale((float) wid / myPic.getWidth(),
				(float) hei / myPic.getHeight());// ͼƬ�Ŀ�ߺ���Ļ�Ŀ�߱�
		int x = 0;
		for (int i = 0; i < ROW; i++) {

			TableRow tr = new TableRow(this);

			for (int j = 0; j < COL; j++) {
				iv[x] = new MyImageView(this, i * 10 + j);// ʵ����һ��imageview

				Bitmap bitmap = Bitmap.createBitmap(myPic, myPic.getWidth()
						/ COL * j, myPic.getHeight() / ROW * i,
						myPic.getWidth() / COL, myPic.getHeight() / ROW, m,
						true);// ��ͼƬ����Ϊһ�������СͼƬ

				iv[x].setId(i * 10 + j);// ����id

				iv[x].setImageBitmap(bitmap);// ����ͼƬ

				iv[x].setPadding(1, 1, 1, 1);// ����ÿ��ͼƬ�ı߾�

				iv[x].setBackgroundColor(Color.WHITE);// ����ͼƬ����ɫ,������ʾ��ɫ�߿�

				map.put(new Point(i, j), iv[x]);// ��ӵ�map��,����֮�󽻻�ͼƬ,���Ը���point��ȡimageview

				iv[x].setOnClickListener(new OnClickImage());

				tr.addView(iv[x]);

				if (i == ROW - 1 && j == COL - 1) {

					iv[x].setVisibility(View.INVISIBLE);// Ԥ�����һ��ͼƬΪ���ɼ�

					iv[x].setId(100);// Ԥ�����һ��ͼƬidΪ100
				}
				x++;
			}

			tableLayout.addView(tr, new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
		}
	}

	/**
	 * �����ж���Ϸ�Ƿ����,����ͼƬ֮ǰ���õ�index��id���Ƚ�
	 */
	public void pandun() {
		int k = 0;
		for (int i = 0; i < ROW * COL; i++) {

			if (iv[i].getIndex() == iv[i].getId()) {
				k += 1;
			}

		}

		if (k == ROW * COL - 1) {
			MyToast.makeText(PuzzleGame.this, "��ս�ɹ�", MyToast.LENGTH_SHORT)
					.show();
			tableLayout.setVisibility(View.INVISIBLE);// ������tablelayout����Ϊ���ɼ�
			ivFugai.setVisibility(View.VISIBLE);// ������ϷͼƬ����
			Animation anim;// ��Ӷ���
			anim = AnimationUtils.loadAnimation(PuzzleGame.this, R.anim.rotate);
			ivFugai.startAnimation(anim);// ��ʼ����

			System.out.println("Game Over");

			// ��ת����Ӻ��ѽ���
			Intent intent = new Intent(PuzzleGame.this, AddFriendActivity.class);
			intent.putExtra("stranger", stranger);
			startActivity(intent);
			PuzzleGame.this.finish();
		}
	}

	/**
	 * ���ͼƬ�¼�,�ж���������ͼƬ�Ƿ��ܹ��ƶ�
	 * 
	 * @author Administrator
	 * 
	 */
	class OnClickImage implements OnClickListener {

		@Override
		public void onClick(View v) {

			int i = v.getId();
			isAuto = false;// ���Զ���Ϸ��Ϊfalse
			changePic(i);// ����ͼƬ��ת����
			pandun();// �ж���Ϸ�Ƿ����

		}

	}

	public void changePic(int id) {
		int x = id / 10;// ����������iֵתΪx
		int y = id % 10;// ����������iֵתΪy
		if (map.get(new Point(x - 1, y)) != null) {// �ж������Ƿ�Ϊ��

			if (map.get(new Point(x - 1, y)).getId() == 100) {// �ж�����һ��ͼƬ�Ƿ�Ϊ�ڿ�,���򽻻�λ��
				iv[(x) * COL + y].setVisibility(View.INVISIBLE);// ����ǰһ��ͼƬ��Ϊ���ɼ�

				iv[(x - 1) * COL + y].setImageDrawable(iv[(x) * COL + y]
						.getDrawable());// ���ڿ�ͼƬ��Ϊ��������λ�õ�����ͼ

				iv[(x - 1) * COL + y].setVisibility(View.VISIBLE);// �ڿ���Ϊ�ɼ�

				map.get(new Point(x - 1, y)).setIndex(
						map.get(new Point(x, y)).getIndex());// �ڿ��ȡ��ǰͼƬ��index

				map.get(new Point(x - 1, y)).setId((x - 1) * 10 + y);// �ڿ��id���Ÿı�

				map.get(new Point(x, y)).setId(100);// ���õ�ǰͼƬidΪ100

				listPath.add(1);// ��·����ӵ�listpath��

				if (isAuto == false) {// �Ƿ������Զ�����,���򲻼�������
					step += 1;
					tvStep.setText("����:" + step);
					backx = backx + 1;
				}

			}

		}

		if (map.get(new Point(x + 1, y)) != null) {// �ж��±��Ƿ�Ϊ��
			if (map.get(new Point(x + 1, y)).getId() == 100) {// �ж�����һ��ͼƬ�Ƿ�Ϊ�ڿ�,���򽻻�λ��
				iv[(x) * COL + y].setVisibility(View.INVISIBLE);
				iv[(x + 1) * COL + y].setImageDrawable(iv[(x) * COL + y]
						.getDrawable());
				iv[(x + 1) * COL + y].setVisibility(View.VISIBLE);
				map.get(new Point(x + 1, y)).setIndex(
						map.get(new Point(x, y)).getIndex());
				map.get(new Point(x + 1, y)).setId((x + 1) * 10 + y);
				map.get(new Point(x, y)).setId(100);

				listPath.add(-1);

				if (isAuto == false) {
					step += 1;
					tvStep.setText("����:" + step);
					backx = backx - 1;
				}

			}

		}

		if (map.get(new Point(x, y + 1)) != null) {// �ж��ұ��Ƿ�Ϊ��
			if (map.get(new Point(x, y + 1)).getId() == 100) {// �ж��ұ�һ��ͼƬ�Ƿ�Ϊ�ڿ�,���򽻻�λ��
				iv[(x) * COL + y].setVisibility(View.INVISIBLE);
				iv[(x) * COL + y + 1].setImageDrawable(iv[(x) * COL + y]
						.getDrawable());

				iv[(x) * COL + y + 1].setVisibility(View.VISIBLE);

				map.get(new Point(x, y + 1)).setIndex(
						map.get(new Point(x, y)).getIndex());
				map.get(new Point(x, y + 1)).setId((x) * 10 + y + 1);

				map.get(new Point(x, y)).setId(100);

				listPath.add(-2);

				if (isAuto == false) {
					step += 1;
					tvStep.setText("����:" + step);
					backy = backy - 1;
				}

			}

		}

		if (map.get(new Point(x, y - 1)) != null) {// �ж�����Ƿ�Ϊ��
			if (map.get(new Point(x, y - 1)).getId() == 100) {// �ж����һ��ͼƬ�Ƿ�Ϊ�ڿ�,���򽻻�λ��

				iv[(x) * COL + y].setVisibility(View.INVISIBLE);
				iv[(x) * COL + y - 1].setImageDrawable(iv[(x) * COL + y]
						.getDrawable());

				iv[(x) * COL + y - 1].setVisibility(View.VISIBLE);

				map.get(new Point(x, y - 1)).setIndex(
						map.get(new Point(x, y)).getIndex());

				map.get(new Point(x, y - 1)).setId((x) * 10 + y - 1);

				map.get(new Point(x, y)).setId(100);

				listPath.add(2);

				if (isAuto == false) {
					step += 1;
					tvStep.setText("����:" + step);
					backy = backy + 1;
				}

			}

		}

	}

	public void gogogo(int num) {
		listPath = new ArrayList();
		int[] arr = { 1, -1, 2, -2 };// ����һ��һά����,������������ĸ�����
		int x = ROW - 1;// ����xΪ���½�x����
		int y = COL - 1;// ����yΪ���½�y����

		for (int i = 0; i < num; i++) {
			int index = (int) (Math.random() * arr.length);// �������һ������
			int rand = arr[index];
			switch (rand) {
			case UP:
				if (x + 1 > ROW - 1) {
					break;
				} else {
					changePic(map.get(new Point(x + 1, y)).getId());
					x = x + 1;

				}

				break;

			case DOWN:
				if (x - 1 < 0) {
					break;
				} else {
					changePic(map.get(new Point(x - 1, y)).getId());
					x = x - 1;

				}

				break;
			case LEFT:
				if (y + 1 > COL - 1) {
					break;
				} else {
					changePic(map.get(new Point(x, y + 1)).getId());
					y = y + 1;

				}

				break;
			case RIGHT:
				if (y - 1 < 0) {
					break;
				} else {

					changePic(map.get(new Point(x, y - 1)).getId());
					y = y - 1;

				}

				break;
			default:
				break;
			}
		}

		backx = x;// ��x���긳���ڿ�x����
		backy = y;// ��y���긳���ڿ�y����

	}

	/**
	 * �Զ�����,���һ�����캯��,����һ������index,�����ж���Ϸ����ʱ�õ�
	 * 
	 */
	class MyImageView extends ImageView {

		int index;// ������±�,��������ж�ƴͼ�Ƿ��Ѿ�ƴ��

		public MyImageView(Context context, int index) {
			super(context);
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

	}

	/**
	 * �����Ϸ��ʼʱ,��ʼ����Ϸ����
	 */
	public void initView() {
		tableLayout.removeAllViews();// ��������Ŀؼ�ȫ��ɾ��
		tableLayout.setVisibility(View.VISIBLE);// ������Ϊ�ɼ�
		ivFugai.setVisibility(View.INVISIBLE);// ��Ϸ������ͼƬ���ɼ�
		step = 0;// ������Ϊ0
		backx = 0;// �ڿ�x������Ϊ0
		backy = 0;// �ڿ�y������Ϊ0
		isAuto = true;
		iv = new MyImageView[ROW * COL];// ����ͼƬ����Ĵ�С
		addPic();// ���ͼƬ,�����и�
	}

	private class StrangerVCardTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				if (xmppManager == null)
					System.out.println("XmppManager is null");
				stranger = xmppManager.getVCard(params[0]);
				if (stranger.avatar != null) {
					File file = new File(IM.CACHE_PATH
							+ StringUtils.parseName(params[0]));
					if (!file.exists())
						file.createNewFile();
					FileOutputStream outputStream = new FileOutputStream(file);
					outputStream.write(Base64.decode(stranger.avatar));
					// ����Intent��ת����Я�������ݣ���˰�ͷ���ɻ����ļ���avatar�д����ļ�·��
					stranger.avatar = file.getPath();
					System.out.println("path = " + file.getPath());
					outputStream.close();
				}
			} catch (IOException | RemoteException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			MyToast.makeText(PuzzleGame.this, "İ������Ƭ��ȡ�ɹ�", 1).show();
		}
	}

	/** ���ӷ��� */
	private class XMPPServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName componentName,
				IBinder iBinder) {
			xmppManager = IXmppManager.Stub.asInterface(iBinder);
			// XmppManagerʵ����֮���ٵ���
			new StrangerVCardTask().execute(sessionID);
		}

		public void onServiceDisconnected(ComponentName componentName) {
			System.out.println("-->>onServiceDisconnected");
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

	protected void onDestroy() {
		if (myPic != null) {
			myPic.recycle();
		}
		unbindService(serviceConnect);
		super.onDestroy();
	}
}
