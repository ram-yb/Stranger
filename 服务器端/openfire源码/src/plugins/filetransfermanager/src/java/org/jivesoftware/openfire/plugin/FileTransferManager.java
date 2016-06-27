package org.jivesoftware.openfire.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.dom4j.Element;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class FileTransferManager implements PacketInterceptor {

	public static final String REQUEST = "request";// �������ļ�
	public static final String REJECT = "reject";// �ܾ������ļ�
	public static final String ACCEPT = "accept";// ͬ�ⷢ���ļ�����
	public static final String SENDING = "sending";// ������ϣ���������
	public static final String COMPLETE = "complete";// �������
	public static final String FAIL = "fail";// ����ʧ�ܣ�MD5У��ʧ��
	public static final String ERROR = "error";// ���ʹ���
	public static final String CANCEL = "cancel";// ȡ���ļ�����
	private static final String SQL_INSERT = "INSERT INTO ofFileTransfer (_id,fileName,saveFileName,mime_type,status,_from,_to,date) VALUES (?,?,?,?,?,?,?,?)";
	private static final String SQL_DELETE = "DELETE FROM ofFileTransfer WHERE fileName = ?";
	private static final String SQL_UPDATE = "UPDATE ofFileTransfer SET status = '?', date = '?' WHERE fileName = '?'";
	private String[] elementName = { "fileName", "saveFileName", "mime_type",
			"status", "date" };
	private Connection openfireConn;
	private long temp = 1;

	@Override
	public void interceptPacket(Packet packet, Session session,
			boolean incoming, boolean processed) throws PacketRejectedException {
		// �����������
		if (!processed && packet instanceof Message) {
			Message message = (Message) packet;
			String subject = message.getSubject();
			System.out.println("subject = " + subject);
			if ("file-transfer".equals(subject))
				try {
					writeToDatabase(message);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			processed = true;
		}
	}

	private void writeToDatabase(Message message) throws SQLException {
		Map<String, String> map = new HashMap<String, String>();
		PreparedStatement statement = null;
		try {
			this.openfireConn = DbConnectionManager.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Element element = message.getElement();
		Element fileElement = element.element("fileinfo");
		map.put("from", message.getFrom().toString());
		map.put("to", message.getTo().toString());
		System.out.println("status = "
				+ fileElement.element("status").getText());

		Element temp = null;
		for (int i = 0; i < 5; i++) {
			temp = fileElement.element(elementName[i]);
			map.put(elementName[i], temp.getText());
		}

		switch (map.get("status")) {
		case REQUEST:
			statement = openfireConn.prepareStatement(SQL_INSERT);
			statement.setLong(1, System.currentTimeMillis() + (this.temp++));
			statement.setString(2, map.get("fileName"));
			statement.setString(3, map.get("saveFileName"));
			statement.setString(4, map.get("mime_type"));
			statement.setString(5, map.get("status"));
			statement.setString(6, map.get("from"));
			statement.setString(7, map.get("to"));
			statement.setLong(8, Long.parseLong(map.get("date")));
			statement.execute();
			break;
		case REJECT:
			statement = openfireConn.prepareStatement(SQL_DELETE);
			statement.setString(1, map.get("fileName"));
			statement.execute();
			break;
		case SENDING:
			statement = openfireConn.prepareStatement(SQL_UPDATE);
			statement.setString(1, map.get("status"));
			statement.setLong(2, Long.parseLong(map.get("date")));
			statement.setString(3, map.get("fileName"));
			statement.execute();
			break;
		case ACCEPT:
			statement = openfireConn.prepareStatement(SQL_UPDATE);
			statement.setString(1, map.get("status"));
			statement.setLong(2, Long.parseLong(map.get("date")));
			statement.setString(3, map.get("fileName"));
			statement.execute();
			break;
		case COMPLETE:
			statement = openfireConn.prepareStatement(SQL_UPDATE);
			statement.setString(1, map.get("status"));
			statement.setString(2, map.get("date"));
			statement.setString(3, map.get("fileName"));
			statement.execute();
			break;
		default:
			break;
		}
	}
}
