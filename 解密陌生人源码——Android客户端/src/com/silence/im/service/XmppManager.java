package com.silence.im.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.XMPPTCPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.RemoteException;
import android.os.Vibrator;
import android.text.TextUtils;

import com.silence.im.IM;
import com.silence.im.IMService;
import com.silence.im.R;
import com.silence.im.provider.ContactsProvider;
import com.silence.im.provider.ContactsProvider.ContactColumns;
import com.silence.im.provider.SMSProvider;
import com.silence.im.provider.SMSProvider.SMSColumns;
import com.silence.im.ui.AddFriendActivity;
import com.silence.im.ui.ChatActivity;
import com.silence.im.ui.FindPasswordActivity;
import com.silence.im.ui.SettingActivity;
import com.silence.im.util.Base64;
import com.silence.im.util.FileTransferHelper;
import com.silence.im.util.MD5Utils;
import com.silence.im.util.PinYin;
import com.stranger.client.util.FileMessage;
import com.stranger.client.util.GeneralIQ;
import com.stranger.client.util.LocationIQ;
import com.stranger.client.util.LocationItem;

/**
 * XMPP���ӹ���
 * 
 * @author JerSuen
 */
public class XmppManager extends IXmppManager.Stub {

	private static final String FILEINFO = "fileinfo";
	long[] pattern = { 0, 100, 200, 300 }; // ֹͣ ���� ֹͣ ����
	private XMPPConnection connection;
	private String account, password;
	private ConnectionListener connectionListener;
	private RosterListener rosterListener;
	private IMService imService;
	private PacketListener messageListener;
	private NotificationManager notificationManager;
	private Map<String, Chat> jidChats = Collections
			.synchronizedMap(new HashMap<String, Chat>());
	private FileMessage fileMessage;
	private Map<String, String> fileInfoMap;
	private MediaPlayer player = new MediaPlayer();
	private Vibrator vibrator;

	public XmppManager(ConnectionConfiguration config, String account,
			String password, IMService imService) {
		this(new XMPPTCPConnection(config), account, password, imService);
	}

	public XmppManager(XMPPConnection connection, String account,
			String password, IMService imService) {
		System.out.println("look XMPPManager");
		this.connection = connection;
		this.account = account;
		this.password = password;
		this.imService = imService;
		notificationManager = (NotificationManager) this.imService
				.getSystemService(this.imService.NOTIFICATION_SERVICE);
		vibrator = (Vibrator) imService
				.getSystemService(Context.VIBRATOR_SERVICE);
		try {
			player.setDataSource(
					imService,
					Uri.parse("android.resource://"
							+ imService.getPackageName() + "/" + R.raw.ring));
			player.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����XMPP����
	 */
	public boolean connect() throws RemoteException {
		// �Ѿ�����
		if (connection.isConnected()) {
			return true;
		} else {
			try {
				// ��ʼ����
				connection.connect();
				if (connectionListener == null) {
					// ���һ�����Ӽ�����
					connectionListener = new IMClientConnectListener();
				}
				connection.addConnectionListener(connectionListener);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * ��½XMPP������
	 */
	public boolean login() throws RemoteException {
		// δ����XMPP����
		if (!connection.isConnected()) {
			System.out.println("XMPPManager-->>>not connection");
			return false;
		}
		// �Ѿ���½��
		if (connection.isAuthenticated()) {
			System.out.println("XMPPManager-->>>isAuthenticated");
			return true;
		} else {
			// ��ʼ��½
			try {
				System.out.println("XmppManager Login-->> account = " + account
						+ "  password = " + password);
				connection.login(account, password,
						imService.getString(R.string.app_name));
				if (messageListener == null) {
					messageListener = new MessageListener();
				}
				// �����Ϣ������
				connection.addPacketListener(messageListener,
						new PacketTypeFilter(Message.class));
				Roster roster = connection.getRoster();
				if (rosterListener == null) {
					rosterListener = new IMClientRosterListener();
				}
				// ��ӻ����������
				roster.addRosterListener(rosterListener);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * �ر�XMPP����
	 */
	public boolean disconnect() throws RemoteException {
		if (connection != null && connection.isConnected()) {
			connection.disconnect();
		}
		return true;
	}

	/** �������״̬ */
	public boolean isConnected() {
		if (connection.isConnected())
			return true;
		else
			return false;
	}

	/** ����¼״̬ */
	public boolean isLogin() {
		if (connection.isAuthenticated())
			return true;
		else
			return false;
	}

	@SuppressLint("NewApi")
	public void notificationStart(Contact contact, String message) {

		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		Intent openintent = new Intent(imService, ChatActivity.class);
		openintent.putExtra(ChatActivity.EXTRA_CONTACT, contact);
		// �������Ϣʱ�ͻ���ϵͳ����openintent��ͼ
		PendingIntent contentIntent = PendingIntent.getActivity(this.imService,
				0, openintent, 0);

		Notification.Builder builder = new Notification.Builder(imService);
		builder.setContentIntent(contentIntent);
		builder.setContentText(message);
		builder.setWhen(when);
		builder.setSmallIcon(icon);
		builder.setLights(0x00ff00, 1000, 1000);
		builder.setLargeIcon(BitmapFactory.decodeResource(
				imService.getResources(), R.drawable.ic_launcher));
		builder.setContentTitle(StringUtils.parseName(contact.account));
		Notification notification = builder.build();
		if (IM.getSetting(SettingActivity.SETTING_NOTIFICATION) == 0
				&& !isRunningForeground())
			notificationManager.notify(IM.MESSAGE_NOTIFICATION, notification);
	}

	// �ж�APP�Ƿ���ǰ̨����
	private boolean isRunningForeground() {
		ActivityManager am = (ActivityManager) imService
				.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (!TextUtils.isEmpty(currentPackageName)
				&& currentPackageName.equals(imService.getPackageName())) {
			return true;
		}
		return false;
	}

	public MediaPlayer ring() {
		vibrator.vibrate(pattern, -1);
		if (IM.getSetting(SettingActivity.SETTING_VOICE) == 0)
			try {
				player.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		return player;
	}

	// TODO
	/** ����ͼƬ������ */
	/**
	 * @param sessionJID
	 *            �Է�JID
	 * @param sessionName
	 *            �Է��û���
	 * @param filename
	 *            �����ļ���
	 * @param filepath
	 *            �����ļ�·������/IMClient/·����
	 * @param type
	 *            �����ļ����ͣ�������ͼƬ image/audio
	 * @throws RemoteException
	 */
	public boolean sendBase64File(String sessionJID, String sessionName,
			String filename, String filePath, String type)
			throws RemoteException {
		ChatManager chatManager = ChatManager.getInstanceFor(connection);
		Chat chat;
		// ����Chat�Բ�
		if (jidChats.containsKey(sessionJID)) {
			chat = jidChats.get(sessionJID);
			// ����Chat
		} else {
			chat = chatManager.createChat(sessionJID, null);
			// ��ӵ�����
			jidChats.put(sessionJID, chat);
		}

		if (chat != null) {

			File file = new File(filePath);
			String md5 = new FileTransferHelper().getFileMD5(file);
			String result = null;
			try {
				System.out.println("test0-->>" + filePath + "  " + md5);
				// �õ��ļ�������
				FileInputStream inputStream = new FileInputStream(file);
				// ת�����ֽ�����
				byte[] buffer = new byte[1024];
				int len = 0;
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				while ((len = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, len);
				}
				// Base64����
				result = Base64.encodeBytes(outputStream.toByteArray());
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("IOException");
				return false;
			}

			Message message = new Message();
			message.setProperty("filename", filename);
			message.setSubject(type);
			message.setFrom(account);
			message.setTo(sessionJID);
			message.setType(Message.Type.chat);
			message.setBody(filename + "////" + md5 + "////" + result);
			System.out.println("Send-->>filename = " + filename
					+ "  filepath = " + filePath + "  type = " + type
					+ "  md5 = " + md5);

			try {
				// ������Ϣ
				chat.sendMessage(message);

				// ���������¼
				ContentValues values = new ContentValues();
				values.put(SMSProvider.SMSColumns.BODY, filename);
				values.put(SMSProvider.SMSColumns.TYPE, type);
				values.put(SMSProvider.SMSColumns.TIME,
						System.currentTimeMillis());
				values.put(SMSProvider.SMSColumns.WHO_ID,
						IM.getString(IM.ACCOUNT_JID));
				values.put(SMSProvider.SMSColumns.SESSION_ID, sessionJID);
				values.put(SMSProvider.SMSColumns.SESSION_NAME, sessionName);
				values.put(SMSColumns.FILEPATH, filePath);
				values.put(SMSColumns.STATUS, FileMessage.COMPLETE);
				values.put(SMSColumns.UNREAD, 0);

				Uri uri = Uri
						.parse("content://com.silence.im.provider.SMSProvider/"
								+ StringUtils.parseName(IM
										.getString(IM.ACCOUNT_JID)) + "____sms");
				imService.getContentResolver().insert(uri, values);

				System.out.println("upload OK");
			} catch (NotConnectedException e) {
				e.printStackTrace();
				System.out.println("NotConnectedException");
				return false;
			}
			System.out.println("OK");
			return true;
		}
		System.out.println("not to if");
		return false;
	}

	private static final int EACH_SIZE = 10240;

	// TODO
	/** ����ͼƬ������ */
	/**
	 * �ϵ㷢�ͣ�֧�ִ��ļ�
	 * 
	 * @param sessionJID
	 *            �Է�JID
	 * @param sessionName
	 *            �Է��û���
	 * @param filename
	 *            �����ļ���
	 * @param filepath
	 *            �����ļ�·������/Stranger/·����
	 * @param type
	 *            �����ļ����ͣ�������ͼƬ image/audio
	 * @throws RemoteException
	 */
	public boolean sendBase64FileBySpliter(String sessionJID,
			String sessionName, String filename, String filePath, String type)
			throws RemoteException {
		ChatManager chatManager = ChatManager.getInstanceFor(connection);
		Chat chat;
		// ����Chat�Բ�
		if (jidChats.containsKey(sessionJID)) {
			chat = jidChats.get(sessionJID);
			// ����Chat
		} else {
			chat = chatManager.createChat(sessionJID, null);
			// ��ӵ�����
			jidChats.put(sessionJID, chat);
		}

		if (chat != null) {

			File file = new File(filePath);
			String md5 = new FileTransferHelper().getFileMD5(file);
			String result = null;
			Message message = new Message();
			message.setProperty("filename", filename);
			message.setSubject(type);
			message.setFrom(account);
			message.setTo(sessionJID);
			message.setType(Message.Type.chat);
			try {
				// �ȷ����ļ���Ϣͷ
				message.setBody(filename + ">><<" + md5 + ">><<"
						+ file.length());
				chat.sendMessage(message);

				System.out.println("test0-->>" + filePath + "  " + md5);
				// �õ��ļ�������
				FileInputStream inputStream = new FileInputStream(file);
				// ת�����ֽ�����
				byte[] buffer = new byte[EACH_SIZE];
				int len = 0;
				long nextPos = 0;
				while ((len = inputStream.read(buffer)) > 0) {
					result = Base64.encodeBytes(buffer);
					String temp = nextPos + "////" + result;
					message.setBody(temp);
					chat.sendMessage(message);
					nextPos += len;
				}
				inputStream.close();
			} catch (IOException | NotConnectedException e) {
				e.printStackTrace();
				System.out.println("IOException");
				return false;
			}
			// ���������¼
			ContentValues values = new ContentValues();
			values.put(SMSProvider.SMSColumns.BODY, filename);
			values.put(SMSProvider.SMSColumns.TYPE, type);
			values.put(SMSProvider.SMSColumns.TIME, System.currentTimeMillis());
			values.put(SMSProvider.SMSColumns.WHO_ID,
					IM.getString(IM.ACCOUNT_JID));
			values.put(SMSProvider.SMSColumns.SESSION_ID, sessionJID);
			values.put(SMSProvider.SMSColumns.SESSION_NAME, sessionName);
			values.put(SMSColumns.FILEPATH, filePath);
			values.put(SMSColumns.STATUS, FileMessage.COMPLETE);
			values.put(SMSColumns.UNREAD, 0);

			Uri uri = Uri
					.parse("content://com.silence.im.provider.SMSProvider/"
							+ StringUtils.parseName(IM
									.getString(IM.ACCOUNT_JID)) + "____sms");
			imService.getContentResolver().insert(uri, values);

			System.out.println("upload OK");
			return true;
		}
		System.out.println("not to if");
		return false;
	}

	// HTTP�����ļ�,��Ҫͬ��
	public void sendFileByHTTPNeedRequest(String sessionID, String pathStr,
			String filename, String type) {

		checkAndLogin();

		fileMessage = new FileMessage();
		fileMessage.setDate(System.currentTimeMillis());
		fileMessage.setFileName(filename);
		fileMessage.setFrom(account);
		fileMessage.setMime_type(type);
		fileMessage.setPath(pathStr);
		fileMessage.setStatus(FileMessage.REQUEST);
		fileMessage.setSubject("file-transfer");
		fileMessage.setType(org.jivesoftware.smack.packet.Message.Type.chat);
		fileMessage.setSaveFileName(UUID.randomUUID() + "_" + filename);

		FileTransferHelper helper = new FileTransferHelper();
		String md5 = helper.getFileMD5(new File(pathStr));
		fileMessage.setFileMD5Code(md5);
		fileMessage.setTo(sessionID);
		try {
			connection.sendPacket(fileMessage);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	private void checkAndLogin() {
		try {
			if (!connection.isConnected())
				connection.connect();
			if (!connection.isAuthenticated())
				login();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// HTTP�����ļ�������Ҫͬ��
	public void sendFileByHTTPNoRequest(String sessionID, String sessionName,
			String pathStr, String filename, String type) {

		checkAndLogin();

		String saveFileName = UUID.randomUUID() + "_" + filename;
		long date = System.currentTimeMillis();
		fileMessage = new FileMessage();
		fileMessage.setDate(date);
		fileMessage.setFileName(filename);
		fileMessage.setFrom(account);
		fileMessage.setMime_type(type);
		fileMessage.setPath(pathStr);
		fileMessage.setStatus(FileMessage.SENDING);
		fileMessage.setSubject("file-transfer");
		fileMessage.setType(org.jivesoftware.smack.packet.Message.Type.chat);
		fileMessage.setSaveFileName(saveFileName);
		fileMessage.setTo(sessionID);

		// TODO
		fileInfoMap = new HashMap<String, String>();
		fileInfoMap.put("fileName", filename);
		fileInfoMap.put("saveFileName", saveFileName);
		fileInfoMap.put("mime_type", type);
		fileInfoMap.put("sessionID", sessionID);
		fileInfoMap.put("date", date + "");
		fileInfoMap.put("path", pathStr);
		fileInfoMap.put("sessionName", sessionName);
		fileInfoMap.put("Zoom", "true");
		fileInfoMap.put("MaxSize", IM.IMAGE_MAX_SIZE);

		System.out.println("fileName = " + filename + "  saveFileName = "
				+ saveFileName + "  mime_type = " + type + "  sessionID = "
				+ sessionID);

		new FileUploadTask().execute(fileInfoMap);
	}

	/** ������Ϣ */
	public void sendMessage(String sessionJID, String sessionName,
			String message, String type) throws RemoteException {

		checkAndLogin();

		ChatManager chatManager = ChatManager.getInstanceFor(connection);
		Chat chat;

		System.out.println("connection status : " + connection.isConnected()
				+ "   login status : " + connection.isAuthenticated());

		// ����Chat�Բ�
		if (jidChats.containsKey(sessionJID)) {
			chat = jidChats.get(sessionJID);
			// ����Chat
		} else {
			chat = chatManager.createChat(sessionJID, null);
			// ��ӵ�����
			jidChats.put(sessionJID, chat);
		}

		if (chat != null) {
			try {
				// ������Ϣ
				chat.sendMessage(message);

				// ���������¼
				ContentValues values = new ContentValues();
				values.put(SMSProvider.SMSColumns.BODY, message);
				values.put(SMSProvider.SMSColumns.TYPE, type);
				values.put(SMSProvider.SMSColumns.TIME,
						System.currentTimeMillis());
				values.put(SMSColumns.UNREAD, 0);

				values.put(SMSProvider.SMSColumns.WHO_ID,
						IM.getString(IM.ACCOUNT_JID));

				values.put(SMSProvider.SMSColumns.SESSION_ID, sessionJID);
				values.put(SMSProvider.SMSColumns.SESSION_NAME, sessionName);

				Uri uri = Uri
						.parse("content://com.silence.im.provider.SMSProvider/"
								+ StringUtils.parseName(account) + "____sms");
				imService.getContentResolver().insert(uri, values);

			} catch (XMPPException e) {
				e.printStackTrace();
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
	}

	/** ������ϵ�˱�ע */
	public boolean setRosterEntryName(String JID, String name) {
		checkAndLogin();

		try {
			// ���·���������
			connection.getRoster().getEntry(JID).setName(name);
			// ���±�������
			Uri uri1 = Uri
					.parse("content://com.silence.im.provider.ContactsProvider/"
							+ StringUtils.parseName(IM
									.getString(IM.ACCOUNT_JID)) + "____contact");
			ContentValues values = new ContentValues();
			values.put(ContactColumns.NAME_BY_ME, name);
			if (imService.getContentResolver().update(uri1, values,
					ContactsProvider.ContactColumns.ACCOUNT + " = ?",
					new String[] { JID }) == 0) {
				imService.getContentResolver().insert(uri1, values);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/** ������Ƭ��Ϣ */
	// TODO �˻���Ϣ����
	public boolean setVCard(Contact contact) throws RemoteException {
		checkAndLogin();

		VCard vCard = new VCard();
		try {
			// �����Լ���VCard,���·���������
			vCard.load(connection);
			// �����ǳ�
			if (!TextUtils.isEmpty(contact.nickname)) {
				vCard.setNickName(contact.nickname);
			}
			// ����ͷ��
			if (!TextUtils.isEmpty(contact.avatar)) {
				vCard.setAvatar(Base64.decode(contact.avatar));
			}
			// ������ʵ����
			if (!TextUtils.isEmpty(contact.realname)) {
				vCard.setFirstName(contact.realname);
			}
			// ����Email
			if (!TextUtils.isEmpty(contact.email)) {
				vCard.setEmailHome(contact.email);
			}
			// �����Ա�
			if (!TextUtils.isEmpty(contact.gender)) {
				vCard.setLastName(contact.gender);
			}
			// ����
			vCard.save(connection);

			// ���±�������
			if (!TextUtils.isEmpty(contact.avatar)) {
				IM.saveAvatar(contact.avatar.getBytes(),
						StringUtils.parseName(account));
			}
			if (!TextUtils.isEmpty(contact.gender)) {
				IM.putString(IM.ACCOUNT_GENDER, contact.gender);
			}
			if (!TextUtils.isEmpty(contact.nickname)) {
				IM.putString(IM.ACCOUNT_NICKNAME, contact.nickname);
			}
			if (!TextUtils.isEmpty(contact.realname)) {
				IM.putString(IM.ACCOUNT_REALNAME, contact.realname);
			}
			if (!TextUtils.isEmpty(contact.email)) {
				IM.putString(IM.ACCOUNT_EMAIL, contact.email);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/** ��ȡ��Ƭ��Ϣ */
	public Contact getVCard(String JID) throws RemoteException {
		checkAndLogin();

		System.out.println("JID = " + JID);
		if (connection.isAuthenticated())
			System.out.println("connection is login");

		if (!TextUtils.isEmpty(JID)) {
			VCard vCard = new VCard();
			Contact contact = null;
			try {
				vCard.load(connection, JID);
				contact = new Contact();
				System.out.println("test1");
				contact.account = JID;
				System.out.println("test2");
				contact.nickname = vCard.getNickName();
				System.out.println("test3");
				contact.realname = vCard.getFirstName();
				System.out.println("test4");
				contact.email = vCard.getEmailHome();
				System.out.println("test5");
				contact.gender = vCard.getLastName();
				System.out.println("test6");

				if (vCard.getAvatar() != null)// ���ͷ��Ϊ��
					contact.avatar = Base64.encodeBytes(vCard.getAvatar());
				System.out.println("test7");
				System.out.println("avatar = " + vCard.getAvatar());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return contact;
		}
		return null;
	}

	/*
	 * �����˻� XEP-0055 String accountName�������˻��� boolean
	 * stranger��true��ʾֻ����İ���ˣ�false����İ���˶�����
	 */
	public String[] searchAccount(String accountName, boolean stranger)
			throws RemoteException {
		checkAndLogin();

		try {
			// ��������
			UserSearchManager searchManager = new UserSearchManager(connection);
			// ��ȡ������
			Form searchForm = searchManager.getSearchForm("search."
					+ connection.getServiceName());
			// �ύ��
			Form answerForm = searchForm.createAnswerForm();
			// ������������
			answerForm.setAnswer("search", accountName);
			// ������������
			answerForm.setAnswer("Username", true);
			// �ύ������
			ReportedData data;
			data = searchManager.getSearchResults(answerForm, "search."
					+ connection.getServiceName());
			int length = data.getRows().size();
			int i = 0;
			String[] result = new String[length];
			// ���������

			for (ReportedData.Row row : data.getRows()) {
				// ��ȡjid
				String tempJid = row.getValues("jid").get(0);
				if (account.equals(tempJid))
					continue;
				if (stranger) {
					Uri uri = Uri
							.parse("content://com.silence.im.provider.ContactsProvider/"
									+ StringUtils.parseName(IM
											.getString(IM.ACCOUNT_JID))
									+ "____contact");
					Cursor cursor = imService.getContentResolver().query(uri,
							null, ContactColumns.ACCOUNT + "=?",
							new String[] { tempJid }, null);
					if (cursor.getCount() <= 0)
						result[i++] = tempJid;
				} else
					result[i++] = tempJid;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * XMPP���Ӽ�����
	 */
	private class IMClientConnectListener implements ConnectionListener {

		public void connected(XMPPConnection connection) {

		}

		public void authenticated(XMPPConnection connection) {
			VCard me = new VCard();
			try {
				me.load(connection);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			// ��������״̬
			int status = IM.getSetting(IM.ONLINE_STATUS);
			Presence presence = null;
			try {
				if (status == IM.AVAILABLE) {
					presence = new Presence(Presence.Type.available);
					connection.sendPacket(presence);

				} else if (status == IM.UNAVAILABLE) {
					presence = new Presence(Presence.Type.unavailable);
					connection.sendPacket(presence);

				}
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}

			// ����ͬ�����ж�������
			Roster roster = connection.getRoster();
			roster.setSubscriptionMode(SubscriptionMode.accept_all);

			// ˢ�º����б�
			Collection<RosterEntry> collection = connection.getRoster()
					.getEntries();

			Uri uri = Uri
					.parse("content://com.silence.im.provider.ContactsProvider/"
							+ StringUtils.parseName(account) + "____contact");
			for (RosterEntry entry : collection) {

				VCard vCard = new VCard();
				try {
					vCard.load(connection, entry.getUser());
				} catch (Exception e) {
					e.printStackTrace();
				}

				ContentValues values = new ContentValues();
				values.put(ContactColumns.ACCOUNT, entry.getUser());
				values.put(ContactColumns.NICKNAME, vCard.getNickName());
				values.put(ContactColumns.REAL_NAME, vCard.getFirstName());
				values.put(ContactColumns.EMAIL, vCard.getEmailHome());
				values.put(ContactColumns.GENDER, vCard.getLastName());
				values.put(ContactColumns.NAME_BY_ME, entry.getName());
				String sort = PinYin.getPinYin(StringUtils.parseName(entry
						.getUser()));
				values.put(ContactColumns.SORT, sort);
				values.put(ContactColumns.SECTION, sort.substring(0, 1)
						.toUpperCase(Locale.ENGLISH));
				vCard = null;
				if (IM.im.getContentResolver().update(uri, values,
						ContactColumns.ACCOUNT + "=?",
						new String[] { entry.getUser() }) == 0) {
					IM.im.getContentResolver().insert(uri, values);
				}
			}
		}

		public void connectionClosed() {
			try {
				login();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}

		public void connectionClosedOnError(Exception e) {
			try {
				login();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}

		public void reconnectingIn(int seconds) {
			try {
				login();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		public void reconnectionSuccessful() {
			authenticated(connection);
		}

		public void reconnectionFailed(Exception e) {
			try {
				login();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
	}

	/** ����������� */
	private class IMClientRosterListener implements RosterListener {

		public void entriesAdded(Collection<String> strings) {

		}

		public void entriesUpdated(Collection<String> strings) {

		}

		public void entriesDeleted(Collection<String> strings) {

		}

		public void presenceChanged(Presence presence) {

		}
	}

	private String whoNameStr, whoAccountStr;

	/** ��Ϣ������ */
	private class MessageListener implements PacketListener {

		public void processPacket(Packet packet) {
			if (packet instanceof Message) {
				Message message = (Message) packet;
				whoAccountStr = StringUtils.parseBareAddress(message.getFrom());
				String body = message.getBody();
				whoNameStr = StringUtils.parseName(whoAccountStr);

				// ��ѯ��ϵ�˵�����
				// Cursor cursor = imService.getContentResolver().query(
				// ContactsProvider.CONTACT_URI, null,
				// ContactsProvider.ContactColumns.ACCOUNT + " = ?",
				// new String[] { whoAccountStr }, null);

				Uri uri1 = Uri
						.parse("content://com.silence.im.provider.ContactsProvider/"
								+ StringUtils.parseName(account)
								+ "____contact");
				Cursor cursor = imService.getContentResolver().query(uri1,
						null, ContactsProvider.ContactColumns.ACCOUNT + " = ?",
						new String[] { whoAccountStr }, null);

				if (cursor != null && cursor.moveToFirst()) {
					String nickname = cursor.getString(cursor
							.getColumnIndex(ContactColumns.NICKNAME));
					String nameByMe = cursor.getString(cursor
							.getColumnIndex(ContactColumns.NAME_BY_ME));
					cursor.moveToPosition(0);
					if (nameByMe == null || nameByMe.trim().equals(""))
						whoNameStr = nickname;
					else
						whoNameStr = nameByMe;
				}
				System.out.println("Subject = " + message.getSubject());
				if (message.getSubject() == null) {
					// ������Ϣ
					if (message.getType() == Message.Type.chat) {
						body = message.getBody();
						String typeStr = "chat";

						// ������Ϣ
						ContentValues values = new ContentValues();
						values.put(SMSProvider.SMSColumns.BODY, body);
						values.put(SMSProvider.SMSColumns.TYPE, typeStr);
						values.put(SMSProvider.SMSColumns.TIME,
								System.currentTimeMillis());

						values.put(SMSProvider.SMSColumns.WHO_ID, whoAccountStr);

						values.put(SMSProvider.SMSColumns.SESSION_ID,
								whoAccountStr);
						values.put(SMSProvider.SMSColumns.SESSION_NAME,
								whoNameStr);

						if (IM.CHATTING.equals(IM.getString(IM.CHAT_STATUS)))
							values.put(SMSColumns.UNREAD, 0);
						else
							values.put(SMSColumns.UNREAD, 1);

						Uri uri = Uri
								.parse("content://com.silence.im.provider.SMSProvider/"
										+ StringUtils.parseName(account)
										+ "____sms");
						imService.getContentResolver().insert(uri, values);
						try {
							ring();
							notificationStart(getVCard(whoAccountStr), body);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else if ("file-transfer".equals(message.getSubject())) {
					fileMessage = (FileMessage) message;
					System.out.println(fileMessage.toXML());
					System.out.println("status-->>" + fileMessage.getStatus());
					if (FileMessage.REQUEST.equals(fileMessage.getStatus())) {
						System.out.println("rec-->>" + FileMessage.REQUEST);
						// ѯ���Ƿ�ͬ��
						String fileName = fileMessage.getFileName();
						body = "[�ļ�]";
						// ������Ϣ
						ContentValues values = new ContentValues();
						values.put(SMSProvider.SMSColumns.BODY, fileName);
						values.put(SMSProvider.SMSColumns.TYPE, FILEINFO);
						values.put(SMSProvider.SMSColumns.STATUS,
								FileMessage.REQUEST);
						values.put(SMSProvider.SMSColumns.TIME,
								System.currentTimeMillis());

						values.put(SMSProvider.SMSColumns.WHO_ID, whoAccountStr);

						values.put(SMSProvider.SMSColumns.SESSION_ID,
								whoAccountStr);
						values.put(SMSProvider.SMSColumns.SESSION_NAME,
								whoNameStr);

						Uri uri = Uri
								.parse("content://com.silence.im.provider.SMSProvider/"
										+ StringUtils.parseName(account)
										+ "____sms");
						imService.getContentResolver().insert(uri, values);
						try {
							ring();
							notificationStart(getVCard(whoAccountStr), body);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (FileMessage.SENDING.equals(fileMessage
							.getStatus())) {
						System.out.println("rec-->>" + FileMessage.SENDING);

						// �����Ϣ��׼������
						String fileName = fileMessage.getFileName();
						String saveFileName = fileMessage.getSaveFileName();
						String mime_type = fileMessage.getMime_type();
						String md5_from = fileMessage.getFileMD5Code();
						long date = fileMessage.getDate();

						System.out.println("FRV--FileMessage -->>filename = "
								+ fileName + "  saveFileName = " + saveFileName
								+ " mime_type =  " + mime_type);
						fileInfoMap = new HashMap<String, String>();
						fileInfoMap.put("fileName", fileName);
						fileInfoMap.put("saveFileName", saveFileName);
						fileInfoMap.put("mime_type", mime_type);
						fileInfoMap.put("sessionID", whoAccountStr);
						fileInfoMap.put("date", date + "");
						fileInfoMap.put("sessionName", whoNameStr);
						fileInfoMap.put("md5", md5_from);

						// �ļ�����
						new FileDownloadTask().execute(fileInfoMap);

						if (mime_type.contains(IM.MEDIA_AUDIO))
							body = "[����]";
						else if (mime_type.contains(IM.MEDIA_IMAGE))
							body = "[ͼƬ]";
						try {
							ring();
							notificationStart(getVCard(whoAccountStr), body);
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (FileMessage.REJECT.equals(fileMessage
							.getStatus())) {
						body = "[�ļ�]";
						System.out.println("rec-->>" + FileMessage.REJECT);
						// ��ͬ��
						String fileName = fileMessage.getFileName();

						// ������Ϣ
						ContentValues values = new ContentValues();
						values.put(SMSProvider.SMSColumns.TYPE, FILEINFO);
						values.put(SMSProvider.SMSColumns.BODY, "�Է��ܾ�����"
								+ fileName);
						values.put(SMSProvider.SMSColumns.STATUS,
								FileMessage.REJECT);
						values.put(SMSProvider.SMSColumns.TIME,
								System.currentTimeMillis());

						Uri uri = Uri
								.parse("content://com.silence.im.provider.SMSProvider/"
										+ StringUtils.parseName(account)
										+ "____sms");
						imService.getContentResolver().update(
								uri,
								values,
								SMSProvider.SMSColumns.SESSION_ID + "=? and "
										+ SMSProvider.SMSColumns.BODY
										+ "=? and "
										+ SMSProvider.SMSColumns.STATUS + "=?",
								new String[] { whoAccountStr, fileName,
										FileMessage.REQUEST });
					} else if (FileMessage.ACCEPT.equals(fileMessage
							.getStatus())) {
						body = "[�ļ�]";
						System.out.println("rec-->>" + FileMessage.ACCEPT);
						// ͬ��
						String fileName = fileMessage.getFileName();
						String saveFileName = fileMessage.getSaveFileName();
						String mime_type = fileMessage.getMime_type();
						long date = fileMessage.getDate();

						// TODO
						String path = fileMessage.getPath();

						Map<String, String> map = new HashMap<String, String>();
						map.put("fileName", fileName);
						map.put("saveFileName", saveFileName);
						map.put("mime_type", mime_type);
						map.put("path", path);
						map.put("whoAccountStr", whoAccountStr);
						map.put("date", date + "");

						new FileUploadTask().execute(map);

					}
					// else if (FileMessage.COMPLETE.equals(fileMessage
					// .getStatus())) {
					// System.out.println("rec-->>" + FileMessage.COMPLETE);
					// // ���
					// String fileName = fileMessage.getFileName();
					//
					// // ������Ϣ
					// ContentValues values = new ContentValues();
					// values.put(SMSProvider.SMSColumns.TYPE, FILEINFO);
					// values.put(SMSProvider.SMSColumns.BODY, "�Է��ѽ���"
					// + fileName);
					// values.put(SMSProvider.SMSColumns.STATUS,
					// FileMessage.COMPLETE);
					// values.put(SMSProvider.SMSColumns.TIME,
					// System.currentTimeMillis());
					//
					// Uri uri = Uri
					// .parse("content://com.jersuen.im.provider.SMSProvider/"
					// + IM.getUsername(IM
					// .getString(IM.ACCOUNT_JID))
					// + "____sms");
					// imService.getContentResolver().update(
					// uri,
					// values,
					// SMSProvider.SMSColumns.SESSION_ID + "=?,"
					// + SMSProvider.SMSColumns.BODY + "=?,"
					// + SMSProvider.SMSColumns.STATUS + "=?",
					// new String[] { whoAccountStr, fileName,
					// FileMessage.SENDING });
					// }
				}

				// ��������ͼƬBase64�������
				else if (IM.MEDIA_AUDIO.equals(message.getSubject())
						|| IM.MEDIA_IMAGE.equals(message.getSubject())) {
					System.out.println("��������ͼƬ");
					String filename = (String) message.getProperty("filename");
					String type = message.getSubject();
					String dataBase64 = message.getBody();
					body = "[�ļ�]";

					if (dataBase64.contains("////")) {
						String[] temp = dataBase64.split("////");
						long pos = Long.parseLong(temp[0]);
						String result = temp[1];
						byte[] data = Base64.decode(result);

						Uri uri = Uri
								.parse("content://com.silence.im.provider.SMSProvider/"
										+ StringUtils.parseName(account)
										+ "____sms");
						Cursor cursor2 = imService.getContentResolver().query(
								uri,
								null,
								SMSColumns.STATUS + "=? and "
										+ SMSColumns.SESSION_ID + "=?",
								new String[] { FileMessage.SENDING,
										whoAccountStr }, null);

						FileOutputStream outputStream = null;
						File file = new File(cursor2.getString(cursor2
								.getColumnIndex(SMSColumns.FILEPATH)));
						try {
							outputStream = new FileOutputStream(file, true);
							outputStream.write(data);
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								outputStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						String md5 = cursor2.getString(cursor2
								.getColumnIndex(SMSColumns.PROGRESS));
						if (new FileTransferHelper().getFileMD5(file).equals(
								md5)) {
							System.out.println("File Receive SUCCESS");
							ContentValues values = new ContentValues();
							values.put(SMSColumns.STATUS, FileMessage.COMPLETE);
							if (IM.CHATTING
									.equals(IM.getString(IM.CHAT_STATUS)))
								values.put(SMSColumns.UNREAD, 0);
							else
								values.put(SMSColumns.UNREAD, 1);

							Uri uri2 = Uri
									.parse("content://com.silence.im.provider.SMSProvider/"
											+ StringUtils.parseName(account)
											+ "____sms");
							imService.getContentResolver().update(
									uri2,
									values,
									SMSColumns.STATUS + "=? and "
											+ SMSColumns.SESSION_ID + "=?",
									new String[] { FileMessage.SENDING,
											whoAccountStr });
						}
					} else if (dataBase64.contains(">><<")) {
						String[] temp = dataBase64.split(">><<");
						filename = temp[0];
						String md5 = temp[1];
						long length = Long.parseLong(temp[2]);
						System.out.println("File length = " + length);

						String path = null;
						if (IM.MEDIA_AUDIO.equals(type)) {
							// ����
							path = IM.AUDIO_PATH;
							body = "[����]";
						} else if (IM.MEDIA_IMAGE.equals(type)) {
							// ͼƬ
							body = "[ͼƬ]";
							path = IM.IMAGE_PATH;
						} else {
							path = IM.DOWNLOAD_PATH;
						}
						filename = UUID.randomUUID()
								+ filename.substring(filename.lastIndexOf("."));
						String filepath = path + filename;
						// ���������¼
						ContentValues values = new ContentValues();
						values.put(SMSColumns.PROGRESS, md5);
						values.put(SMSProvider.SMSColumns.BODY, filename);
						values.put(SMSProvider.SMSColumns.TYPE, type);
						values.put(SMSColumns.FILEPATH, filepath);
						values.put(SMSColumns.STATUS, FileMessage.SENDING);
						values.put(SMSProvider.SMSColumns.TIME,
								System.currentTimeMillis());
						values.put(SMSProvider.SMSColumns.WHO_ID, whoAccountStr);
						values.put(SMSProvider.SMSColumns.SESSION_ID,
								whoAccountStr);
						values.put(SMSProvider.SMSColumns.SESSION_NAME,
								whoNameStr);

						if (IM.CHATTING.equals(IM.getString(IM.CHAT_STATUS)))
							values.put(SMSColumns.UNREAD, 0);
						else
							values.put(SMSColumns.UNREAD, 1);

						Uri uri = Uri
								.parse("content://com.silence.im.provider.SMSProvider/"
										+ StringUtils.parseName(account)
										+ "____sms");
						imService.getContentResolver().insert(uri, values);

						try {
							ring();
							notificationStart(getVCard(whoAccountStr), body);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					// String[] temp = dataBase64.split("////");
					// filename = temp[0];
					// String md5 = temp[1];
					// dataBase64 = temp[2];
					//
					// System.out.println("Receive-->>filename = " + filename
					// + "  filepath = " + "type = " + type
					// + "  base64 = " + "  md5 = " + md5);
					// String filepath = null;
					// File file = null;
					// try {
					// // ����õ��ֽ�����
					// byte[] data = Base64.decode(dataBase64);
					// // �ж�����ͼƬ
					// String path = null;
					// if (IM.MEDIA_AUDIO.equals(type)) {
					// // ����
					// path = IM.AUDIO_PATH;
					// } else if (IM.MEDIA_IMAGE.equals(type)) {
					// // ͼƬ
					// path = IM.IMAGE_PATH;
					// } else {
					// path = IM.DOWNLOAD_PATH;
					// }
					// filename = UUID.randomUUID()
					// + filename.substring(filename.lastIndexOf("."));
					// filepath = path + filename;
					// file = new File(filepath);
					// if (!file.exists())
					// file.createNewFile();
					// FileOutputStream outputStream = new FileOutputStream(
					// file);
					// outputStream.write(data);
					// outputStream.close();
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
					// String md5_now = new
					// FileTransferHelper().getFileMD5(file);
					//
					// System.out.println("Receive-->>filename = " + filename
					// + "  filepath = " + filepath + "type = " + type
					// + "  base64 = " + dataBase64);
					//
					// // ���������¼
					// ContentValues values = new ContentValues();
					// values.put(SMSProvider.SMSColumns.BODY, filename);
					// values.put(SMSProvider.SMSColumns.TYPE, type);
					// values.put(SMSColumns.FILEPATH, filepath);
					//
					// if (md5.equals(md5_now))
					// values.put(SMSColumns.STATUS, SMSColumns.FILE_OK);
					// else
					// values.put(SMSColumns.STATUS, SMSColumns.FILE_FAIL);
					//
					// values.put(SMSProvider.SMSColumns.TIME,
					// System.currentTimeMillis());
					// values.put(SMSProvider.SMSColumns.WHO_ID, whoAccountStr);
					// values.put(SMSProvider.SMSColumns.SESSION_ID,
					// whoAccountStr);
					// values.put(SMSProvider.SMSColumns.SESSION_NAME,
					// whoNameStr);
					// values.put(SMSColumns.FILEPATH, filepath);
					// values.put(SMSColumns.STATUS, FileMessage.COMPLETE);
					//
					// if (IM.CHATTING.equals(IM.getString(IM.CHAT_STATUS)))
					// values.put(SMSColumns.UNREAD, 0);
					// else
					// values.put(SMSColumns.UNREAD, 1);
					//
					// Uri uri = Uri
					// .parse("content://com.jersuen.im.provider.SMSProvider/"
					// + StringUtils.parseName(account)
					// + "____sms");
					// imService.getContentResolver().insert(uri, values);
					// System.out.println("download OK");

				}
			}
		}
	}

	private class FileUploadTask extends FileUpload {
		@Override
		protected void onPostExecute(Boolean result) {
			ContentValues values = new ContentValues();
			// TODO �ϴ���ϣ��ش���Ϣ
			fileMessage.setDate(Long.parseLong(fileInfoMap.get("date")));
			fileMessage.setFrom(account);
			fileMessage.setStatus(FileMessage.SENDING);
			fileMessage.setFileMD5Code(map.get("md5"));
			fileMessage.setTo(fileInfoMap.get("sessionID"));
			try {
				connection.sendPacket(fileMessage);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
			// ������ʾ�ļ��ϴ�״̬
			// ������Ϣ
			values.put(SMSProvider.SMSColumns.STATUS, FileMessage.COMPLETE);
			values.put(SMSProvider.SMSColumns.PROGRESS, 100);
			values.put(SMSProvider.SMSColumns.BODY, fileInfoMap.get("fileName")
					+ "�ϴ����");
			System.out.println("status = " + FileMessage.SENDING
					+ "  progress = " + 100 + "  bady = "
					+ fileInfoMap.get("FileName") + "  type = " + FILEINFO
					+ "  unread = " + 0);

			Uri uri = Uri
					.parse("content://com.silence.im.provider.SMSProvider/"
							+ StringUtils.parseName(account) + "____sms");
			imService.getContentResolver().update(
					uri,
					values,
					SMSProvider.SMSColumns.SESSION_ID + "=? and "
							+ SMSProvider.SMSColumns.STATUS + "=? and "
							+ SMSColumns.TIME + "=?",
					new String[] { fileInfoMap.get("sessionID"),
							FileMessage.SENDING, fileInfoMap.get("date") });
			fileMessage = null;
		}

		// @Override
		// protected void onProgressUpdate(Integer... values) {
		// ContentValues value = new ContentValues();
		// value.put(SMSProvider.SMSColumns.PROGRESS, values[0]);
		// value.put(SMSProvider.SMSColumns.TYPE, FILEINFO);
		// value.put(SMSProvider.SMSColumns.TIME, System.currentTimeMillis());
		//
		// Uri uri = Uri
		// .parse("content://com.jersuen.im.provider.SMSProvider/"
		// + IM.getUsername(IM.getString(IM.ACCOUNT_JID))
		// + "____sms");
		// imService.getContentResolver().update(
		// uri,
		// value,
		// SMSProvider.SMSColumns.SESSION_ID + "=?,"
		// + SMSProvider.SMSColumns.BODY + "=?,"
		// + SMSProvider.SMSColumns.STATUS + "=?",
		// new String[] { map.get("whoAccountStr"),
		// map.get("fileName"), FileMessage.REQUEST });
		// }

		@Override
		protected void onPreExecute() {
			ContentValues values = new ContentValues();
			// TODO �ϴ�֮ǰˢ��UI
			// ������ʾ�ļ��ϴ�״̬
			// ������Ϣ

			values.put(SMSColumns.WHO_ID, IM.getString(IM.ACCOUNT_JID));
			values.put(SMSColumns.SESSION_ID, fileInfoMap.get("sessionID"));
			values.put(SMSColumns.SESSION_NAME, fileInfoMap.get("sessionName"));
			values.put(SMSProvider.SMSColumns.STATUS, FileMessage.SENDING);
			values.put(SMSProvider.SMSColumns.PROGRESS, 0);
			values.put(SMSProvider.SMSColumns.BODY, fileInfoMap.get("fileName")
					+ "������");
			values.put(SMSProvider.SMSColumns.TYPE,
					fileInfoMap.get("mime_type"));
			values.put(SMSProvider.SMSColumns.TIME, fileInfoMap.get("date"));
			values.put(SMSColumns.FILEPATH, fileInfoMap.get("path"));

			values.put(SMSColumns.UNREAD, 0);

			System.out.println("status = " + FileMessage.SENDING
					+ "  progress = " + 100 + "  bady = "
					+ fileInfoMap.get("FileName") + "  type = " + FILEINFO
					+ "  unread = " + 0);

			Uri uri = Uri
					.parse("content://com.silence.im.provider.SMSProvider/"
							+ StringUtils.parseName(account) + "____sms");
			imService.getContentResolver().insert(uri, values);
		}
	}

	private class FileDownloadTask extends FileDownload {
		@Override
		protected void onPostExecute(Boolean result) {
			ContentValues values = new ContentValues();
			fileMessage.setDate(Long.parseLong(fileInfoMap.get("date")));
			fileMessage.setFrom(IM.getString(IM.ACCOUNT_JID));
			fileMessage.setTo(fileInfoMap.get("sessionID"));
			if (result) {
				// TODO ������ϣ��ش���Ϣ
				fileMessage.setStatus(FileMessage.COMPLETE);

				// ������ʾ�ļ�����״̬
				// ������Ϣ
				values.put(SMSProvider.SMSColumns.STATUS, FileMessage.COMPLETE);
				values.put(SMSProvider.SMSColumns.PROGRESS, 100);
				values.put(SMSProvider.SMSColumns.BODY,
						fileInfoMap.get("fileName") + "�������");
				new Thread(new Runnable() {

					@Override
					public void run() {
						helper.sendOKToHTTP(fileInfoMap.get("fileName"),
								fileInfoMap.get("saveFileName"),
								fileInfoMap.get("mime_type"),
								System.currentTimeMillis());
					}
				}).start();
			} else {
				// TODO �ļ�����ʧ���쳣
				// �ش�����ʧ����Ϣ
				fileMessage.setStatus(FileMessage.FAIL);

				// ������ʾ����ʧ����Ϣ
				values.put(SMSProvider.SMSColumns.STATUS, FileMessage.FAIL);
				values.put(SMSProvider.SMSColumns.BODY,
						fileInfoMap.get("fileName") + "����ʧ��");
			}
			try {
				connection.sendPacket(fileMessage);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}

			values.put(SMSColumns.FILEPATH, path);
			if (IM.CHATTING.equals(IM.getString(IM.CHAT_STATUS)))
				values.put(SMSColumns.UNREAD, 0);
			else
				values.put(SMSColumns.UNREAD, 1);

			Uri uri = Uri
					.parse("content://com.silence.im.provider.SMSProvider/"
							+ StringUtils.parseName(account) + "____sms");
			imService.getContentResolver().update(
					uri,
					values,
					SMSProvider.SMSColumns.SESSION_ID + "=? and "
							+ SMSProvider.SMSColumns.STATUS + "=? and "
							+ SMSColumns.TIME + "=?",
					new String[] { fileInfoMap.get("sessionID"),
							FileMessage.SENDING, fileInfoMap.get("date") });
			System.out.println("download OK");
			fileMessage = null;
		}

		@Override
		protected void onPreExecute() {
			// ������Ϣ
			ContentValues values = new ContentValues();
			// TODO ����֮ǰˢ��UI
			// ������ʾ�ļ�����״̬
			values.put(SMSProvider.SMSColumns.TYPE,
					fileInfoMap.get("mime_type"));
			values.put(SMSColumns.WHO_ID, fileInfoMap.get("sessionID"));
			values.put(SMSColumns.SESSION_ID, fileInfoMap.get("sessionID"));
			values.put(SMSColumns.SESSION_NAME, fileInfoMap.get("sessionName"));
			values.put(SMSProvider.SMSColumns.STATUS, FileMessage.SENDING);
			values.put(SMSProvider.SMSColumns.PROGRESS, 0);
			values.put(SMSProvider.SMSColumns.BODY, fileInfoMap.get("fileName")
					+ "������");
			values.put(SMSProvider.SMSColumns.TIME, fileInfoMap.get("date"));

			if (IM.CHATTING.equals(IM.getString(IM.CHAT_STATUS)))
				values.put(SMSColumns.UNREAD, 0);
			else
				values.put(SMSColumns.UNREAD, 1);

			System.out.println("status = " + FileMessage.SENDING
					+ "  progress = " + 100 + "  bady = "
					+ fileInfoMap.get("fileName") + "  type = " + FILEINFO
					+ "  unread = " + 0);

			Uri uri = Uri
					.parse("content://com.silence.im.provider.SMSProvider/"
							+ StringUtils.parseName(account) + "____sms");
			imService.getContentResolver().insert(uri, values);
		}

		// @Override
		// protected void onProgressUpdate(Integer... values) {
		// ContentValues value = new ContentValues();
		// value.put(SMSProvider.SMSColumns.PROGRESS, values[0]);
		// value.put(SMSProvider.SMSColumns.TYPE, FILEINFO);
		// value.put(SMSProvider.SMSColumns.TIME, System.currentTimeMillis());
		//
		// Uri uri = Uri
		// .parse("content://com.jersuen.im.provider.SMSProvider/"
		// + IM.getUsername(IM.getString(IM.ACCOUNT_JID))
		// + "____sms");
		// imService.getContentResolver().update(
		// uri,
		// value,
		// SMSProvider.SMSColumns.SESSION_ID + "=?,"
		// + SMSProvider.SMSColumns.STATUS + "=?",
		// new String[] { whoAccountStr, FileMessage.ACCEPT });
		// }
	}

	// ��Ӻ���
	@Override
	public int addGroupFriend(String group, String friendJid, String name_by_me)
			throws RemoteException {
		checkAndLogin();

		Uri uri = Uri
				.parse("content://com.silence.im.provider.ContactsProvider/"
						+ StringUtils.parseName(account) + "____contact");
		Cursor cursor = imService.getContentResolver()
				.query(uri, null, ContactColumns.ACCOUNT + "=?",
						new String[] { friendJid }, null);
		if (cursor.getCount() > 0)
			return AddFriendActivity.ADD_FRIEND_EXIST;

		Roster roster = connection.getRoster();
		try {
			roster.createEntry(friendJid, name_by_me, new String[] { group });
		} catch (Exception e) {
			e.printStackTrace();
			return AddFriendActivity.ADD_FRIEND_ERROR;
		}
		VCard vCard = new VCard();
		try {
			vCard.load(connection, friendJid);
		} catch (NoResponseException | XMPPErrorException
				| NotConnectedException e) {
			e.printStackTrace();
			return AddFriendActivity.ADD_FRIEND_ERROR;
		}
		ContentValues values = new ContentValues();
		values.put(ContactColumns.NAME_BY_ME, name_by_me);
		values.put(ContactColumns.ACCOUNT, friendJid);
		values.put(ContactColumns.GENDER, vCard.getLastName());
		values.put(ContactColumns.REAL_NAME, vCard.getFirstName());
		values.put(ContactColumns.EMAIL, vCard.getEmailHome());
		values.put(ContactColumns.NICKNAME, vCard.getNickName());
		String sort = StringUtils.parseName(friendJid);
		values.put(ContactColumns.SORT, sort);
		values.put(ContactColumns.SECTION,
				sort.substring(0, 1).toUpperCase(Locale.ENGLISH));
		// �����ϵ�˼�¼���ݿ�
		imService.getContentResolver().insert(uri, values);
		return AddFriendActivity.ADD_FRIEND_SUCCESS;
	}

	@Override
	public boolean removeFriend(String friendJid) throws RemoteException {
		checkAndLogin();
		Roster roster = connection.getRoster();
		RosterEntry entry = roster.getEntry(friendJid);
		try {
			roster.removeEntry(entry);
		} catch (NotLoggedInException | NoResponseException
				| XMPPErrorException | NotConnectedException e) {
			e.printStackTrace();
			return false;
		}

		// ɾ����ϵ�˼�¼���ݿ�
		Uri uri = Uri
				.parse("content://com.silence.im.provider.ContactsProvider/"
						+ StringUtils.parseName(account) + "____contact");
		imService.getContentResolver().delete(uri,
				ContactColumns.ACCOUNT + " = ?", new String[] { friendJid });

		// ɾ����Ϣ��¼���ݿ�
		Uri uri1 = Uri.parse("content://com.silence.im.provider.SMSProvider/"
				+ StringUtils.parseName(account) + "____sms");
		imService.getContentResolver().delete(uri1,
				SMSColumns.SESSION_ID + " = ?", new String[] { friendJid });
		return true;
	}

	public boolean setOnlineStatus(int status) {
		checkAndLogin();
		Presence presence = null;
		try {
			if (status == IM.AVAILABLE) {
				presence = new Presence(Presence.Type.available);
				connection.sendPacket(presence);
			} else if (status == IM.UNAVAILABLE) {
				presence = new Presence(Presence.Type.unavailable);
				connection.sendPacket(presence);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		IM.putSetting(IM.ONLINE_STATUS, status);
		return true;
	}

	public void logout() {
		if (connection.isAuthenticated()) {
			connection.disconnect();
			try {
				connection.connect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean savePassword(String accountString, String passwordString,
			String emailString, boolean type) {

		String passwordMD5 = MD5Utils.convertMD5(passwordString);
		GeneralIQ iq = new GeneralIQ("query", "urn:xmpp:rayo:findpassword");
		DocumentFactory factory = new DocumentFactory();
		Element element = factory.createElement("findpassword");
		element.addAttribute("username", accountString);
		element.addAttribute("email", emailString);
		element.addAttribute("password", passwordMD5);
		element.addAttribute("account", account);

		if (type)
			element.addAttribute("type", "insert");
		else
			element.addAttribute("type", "update");

		Element childElement = factory.createElement("query",
				"urn:xmpp:rayo:findpassword");
		childElement.add(element);
		iq.setChildElement(childElement);
		iq.setFrom(account);
		iq.setType(IQ.Type.SET);

		try {
			connection.sendPacket(iq);
		} catch (NotConnectedException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println("iq -->> " + iq.toXML());

		PacketCollector collector = connection
				.createPacketCollector(new AndFilter(new PacketIDFilter(iq
						.getPacketID()), new PacketTypeFilter(IQ.class)));
		// /��ȡ���������صĸ���������Ϣ
		GeneralIQ result = (GeneralIQ) collector.nextResult(SmackConfiguration
				.getDefaultPacketReplyTimeout() * 2);
		collector.cancel();
		System.out.println("XMPPManager -->>result" + result.toXML());

		Element resultElement = result.getChildElement()
				.element("findpassword");
		int status = Integer.parseInt(resultElement.attributeValue("status"));
		System.out.println("status = " + status);

		return true;
	}

	public int findPasswordByEmail(String username, String emailString,
			String code, boolean type) {

		if (!connection.isAuthenticated())
			try {
				connection.connect();
				connection.loginAnonymously();
			} catch (Exception e1) {
				return FindPasswordActivity.SERVER_ERROR;
			}
		GeneralIQ iq = new GeneralIQ("query", "urn:xmpp:rayo:findpassword");
		DocumentFactory factory = new DocumentFactory();
		Element element = factory.createElement("findpassword");

		if (type) {
			element.addAttribute("code", code);
			element.addAttribute("username", username);
			element.addAttribute("type", "password");
		} else {
			element.addAttribute("account", account);
			element.addAttribute("email", emailString);
			element.addAttribute("username", username);
			element.addAttribute("type", "code");
		}

		Element childElement = factory.createElement("query",
				"urn:xmpp:rayo:findpassword");
		childElement.add(element);
		iq.setChildElement(childElement);
		iq.setFrom(account);
		iq.setType(IQ.Type.GET);

		try {
			connection.sendPacket(iq);
		} catch (NotConnectedException e) {
			e.printStackTrace();
			return FindPasswordActivity.SERVER_ERROR;
		}
		System.out.println("iq -->> " + iq.toXML());
		PacketCollector collector = connection
				.createPacketCollector(new AndFilter(new PacketIDFilter(iq
						.getPacketID()), new PacketTypeFilter(IQ.class)));
		// ���ؽ����Ϣ
		GeneralIQ result = (GeneralIQ) collector.nextResult(SmackConfiguration
				.getDefaultPacketReplyTimeout() * 2);
		collector.cancel();
		System.out.println("XMPPManager -->>result" + result.toXML());

		Element resultElement = result.getChildElement()
				.element("findpassword");
		int status = Integer.parseInt(resultElement.attributeValue("status"));

		if (type) {
			String passwordTemp = MD5Utils.convertMD5(resultElement
					.attributeValue("password"));
			IM.putString(IM.TEMP, passwordTemp);
		}

		connection.disconnect();
		return status;
	}

	public boolean changePassword(String password) {
		AccountManager manager = AccountManager.getInstance(connection);
		try {
			manager.changePassword(password);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("�쳣");
			return false;
		}
		return true;
	}

	public String[] uploadLocation(String longitude, String latitude) {
		checkAndLogin();
		// ����õ�λ�õ�ַ�ϴ�
		LocationIQ packet = new LocationIQ();
		LocationItem item = new LocationItem();
		item.setLatitude(latitude);
		item.setLongitude(longitude);
		item.setGender(IM.getString(IM.ACCOUNT_GENDER));
		item.setUsername(StringUtils.parseName(account));
		item.setNickname(IM.getString(IM.ACCOUNT_NICKNAME));
		item.setJid(account);
		item.setUpdatetime(System.currentTimeMillis() + "");
		packet.addItem(item);
		packet.setType(IQ.Type.GET);
		packet.setFrom(account);
		try {
			connection.sendPacket(packet);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
		PacketCollector collector = connection
				.createPacketCollector(new AndFilter(new PacketIDFilter(packet
						.getPacketID()), new PacketTypeFilter(IQ.class)));
		// /��ȡ���������صĸ���������Ϣ
		LocationIQ result = (LocationIQ) collector
				.nextResult(SmackConfiguration.getDefaultPacketReplyTimeout());
		collector.cancel();
		System.out.println("XMPPManager -->>result" + result.toXML());

		String[] rrrString = new String[result.size()];
		// List<String> list = new ArrayList<String>();
		int i = 0;
		for (Iterator<LocationItem> iterator = result.getItems(); iterator
				.hasNext();) {
			LocationItem temp = iterator.next();

			String tt = temp.getUsername() + ";" + temp.getLatitude() + ";"
					+ temp.getLongitude() + ";" + temp.getNickname() + ";"
					+ temp.getJid();
			// list.add(tt);
			rrrString[i] = tt;
			i++;
		}
		// System.out.println("XMPPMAnager --list -->> "+list.toString());
		// rrrString = list.toArray(new String[] {});
		return rrrString;
	}
}