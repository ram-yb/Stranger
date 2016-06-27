package org.jivesoftware.openfire.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.roster.RosterManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import com.lotus.sametime.awareness.f;

public class UserDetailManager extends IQHandler {
	// 12��Varchar��1�з�Varchar��β��
	// username primary key, nickname , realname(����),
	// ,gender
	// ,age,birthday,constellation��������,animal����Ф��,hometown,location_now�����ڵأ�,phone_number,e_mail,headimage
	// ���ͣ�age��int ,birthday��date��headimage��blob ,��������varchar
	private static final String NAME_SPACE = "urn:xmpp:rayo:userdetail";
	private static final String SQL_UPDATE1 = "update userDetail set ";
	private static final String SQL_UPDATE2 = " where username = ";
	private static final String SQL_INSERT = "insert into userDetail(username, nickname, realname,gender,constellation,animal,hometown,location_now,phone_number,e_mail,user_age,user_birthday) values(?,?,?,?,?,?,?,?,?,?,?,?)";
	private String SQL_UPDATE;

	private static final String SQL_QUERY = "select * from userDetail where username=?";

	private String[] columnName = { "username", "nickname", "realname",
			"gender", "constellation", "animal", "hometown", "location_now",
			"phone_number", "e_mail", "user_age", "user_birthday" };
	private IQHandlerInfo info;
	private Connection openfireConn;

	public UserDetailManager(String moduleName) {
		super(moduleName);
		try {
			this.openfireConn = DbConnectionManager.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		info = new IQHandlerInfo(moduleName, NAME_SPACE);
	}

	@Override
	public IQHandlerInfo getInfo() {
		return info;
	}

	// GET��ʾ�ӷ�������ȡ��Ϣ��SET��ʾ�������������Ϣ
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {

		System.out.println(">>>>>>>>>>>>> RECV IQ: " + packet.toXML()); // XXX

		IQ reply = packet.createResultIQ(packet);
		DocumentFactory factory = new DocumentFactory();
		// get users near me(from JID)
		if (IQ.Type.get.equals(packet.getType())) {
			System.out.println("====>>IQ  SUCCESS������--GET");
			// ��ȡ��Ϣ
			Element userElement = packet.getChildElement()
					.element("userdetail");
			String username = userElement.getTextTrim();
			// �����ظ��ڵ���Ϣ
			Element element = factory.createElement("query", NAME_SPACE);
			Element infoElement = factory.createElement("userdetail");
			element.add(infoElement);
			// �����ݿ����
			PreparedStatement statement;
			try {
				statement = openfireConn.prepareStatement(SQL_QUERY);
				statement.setString(1, username);
				ResultSet resultSet = statement.executeQuery();
				// �ӽ���������ݷ�װ���ڵ���
				while (resultSet.next()) {
					for (int i = 1; i <= resultSet.getMetaData()
							.getColumnCount(); i++) {
						String name = resultSet.getMetaData().getColumnName(i);
						String value = resultSet.getString(i);
						System.out.println(name + " = " + value);
						infoElement.addAttribute(name, value);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// TODO �������һ����ͷ��Blob
			reply.setChildElement(element);
			return reply;
		} else if (IQ.Type.set.equals(packet.getType())) {
			System.out.println("====>>IQ  SUCCESS������--SET");
			// ��ȡ��Ϣ
			Element element = packet.getChildElement().element("userdetail");
			String username = element.attributeValue("username");
			// �ӹ�SQL���
			SQL_UPDATE = SQL_UPDATE1;
			System.out.println(SQL_UPDATE);
			for (int i = 1; i < 12; i++) {
				if (i < 11)
					SQL_UPDATE += columnName[i] + " = '"
							+ element.attributeValue(columnName[i]) + "',";
				else
					SQL_UPDATE += columnName[i] + " = '"
							+ element.attributeValue(columnName[i])+"'";
			}
			// �ͻ�������ʱ�û�û���þͰ�age����Ϊnull
			// birthday�ͻ��˷������ú����String����
			SQL_UPDATE += SQL_UPDATE2 + "'" + username + "'";
			System.out.println("SQL_UPDATE = " + SQL_UPDATE);
			// �������ݿ�,��������
			try {
				PreparedStatement statement = openfireConn
						.prepareStatement(SQL_UPDATE);
				//����
				// PreparedStatement statement = openfireConn
				// .prepareStatement(SQL_INSERT);
				// for (int i = 1; i <= 12; i++) {
				// statement.setString(i,
				// element.attributeValue(columnName[i - 1]));
				// System.out.println("column = "
				// + element.attributeValue(columnName[i - 1]));
				// }
				// statement.setBlob(13, new in);
				statement.execute();
				// TODO ͷ���޸�

				// �����ָ�Elememt
				Element childElement = factory.createElement("query",
						NAME_SPACE);
				Element infoElement = factory.createElement("userdetail",
						NAME_SPACE);
				childElement.add(infoElement);
				infoElement.setText("success");
				reply.setChildElement(childElement);
				System.out.println(reply.toXML());
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			System.out.println("====>>IQ  ERROR������--ERROR");
			reply.setType(IQ.Type.error);
			reply.setError(PacketError.Condition.bad_request);
			return reply;
		}
		return reply;
	}

	/**
	 * 
	 * 
	 * @param packet
	 * @return
	 */
	// private IQ getUsersNearme(IQ packet) {
	//
	// System.out.println("id-->" + packet.getID() + " -element.name--> "
	// + packet.getChildElement().getName() + " --from -> "
	// + packet.getFrom() + " --to-> " + packet.getTo() + "--type-> "
	// + packet.getType() + " --namespace->"
	// + packet.getElement().getNamespace());
	//
	// IQ reply = IQ.createResultIQ(packet);
	// DocumentFactory factory = new DocumentFactory();
	// Element data = factory.createElement("query", NAME_SPACE);
	// JID from = packet.getFrom();
	// reply.setTo(from);
	//
	// Element iq = packet.getChildElement();
	// Element item = iq.element("item");
	// long updatetime = System.currentTimeMillis();
	// String username = from.getNode();
	// Double myLon = Double.parseDouble(item.attributeValue("longitude"));
	// Double myLat = Double.parseDouble(item.attributeValue("latitude"));
	//
	// System.out.println("-->> username=" + username + "  updatetime = "
	// + updatetime / 1000 + "   longitude = " + myLon
	// + "  latitude = " + myLat);
	//
	// // XXX: update user location firstly
	// insertLocation(myLon, myLat, username, updatetime);
	//
	// // /�����Լ��ľ�γ�ȼ���1ǧ���ڵľ�γ�ȷ�Χ
	// double range = 180 / Math.PI * 1 / 6372.797; // ����� 1 �ʹ������� 1km ֮�ڣ���λkm
	// double lngR = range / Math.cos(myLat * Math.PI / 180.0);
	// double maxLat = myLat + range;
	// double minLat = myLat - range;
	// double maxLng = myLon + lngR;
	// double minLng = myLon - lngR;
	//
	// System.out.println("minLat=" + minLat + "  maxlat=" + maxLat
	// + "  minlong=" + minLng + "  maxlong=" + maxLng);
	//
	// // find users near me
	// PreparedStatement pstmt = null;
	// ResultSet rs = null;
	// try {
	// pstmt = openfireConn.prepareStatement(SQL_USERS_NEARME);
	// pstmt.setDouble(1, minLng);
	// pstmt.setDouble(2, maxLng);
	// pstmt.setDouble(3, minLat);
	// pstmt.setDouble(4, maxLat);
	// rs = pstmt.executeQuery();
	//
	// String username_other = null;
	// double nearLon = 0;
	// double nearLat = 0;
	// long neartime = 0;
	// long now = System.currentTimeMillis();
	//
	// while (rs.next()) {
	// username_other = rs.getString("username");
	// nearLon = rs.getDouble("longitude");
	// nearLat = rs.getDouble("latitude");
	// neartime = now - rs.getLong("updatetime");
	// String gender = rs.getString("gender");
	//
	// // /�ж�����
	// try {
	// // ���Ǹ������˵�����������
	// // ���������������Ǻ��ѣ�30�����ڷ��͹�λ����Ϣ���ͱ��˾���1��������
	// if (username_other.equals(username)
	// || neartime > 30 * 60 * 1000
	// || isFriend(username, username_other))
	// continue;
	// } catch (UserNotFoundException e1) {
	// e1.printStackTrace();
	// }
	//
	// Element e = data.addElement("item");
	//
	// e.addAttribute("username", username_other);
	// e.addAttribute("longitude", Double.toString(nearLon));
	// e.addAttribute("latitude", Double.toString(nearLat));
	// e.addAttribute("updatetime", Double.toString(neartime));
	// e.addAttribute("gender", gender);
	// }
	// reply.setChildElement(data);
	//
	// } catch (SQLException e1) {
	// reply.setType(IQ.Type.error);
	// reply.setError(PacketError.Condition.internal_server_error);
	// e1.printStackTrace();
	// try {
	// pstmt.close();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }
	// try {
	// pstmt.close();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// return reply;
	// }
	//
	// // TODO ����
	// private String getUserGender(String username) throws SQLException {
	// String gender;
	// // ���û���Ϣ���ݿ��ȡ�û��Ա�
	// // ResultSet resultSet = openfireConn.createStatement().executeQuery(
	// // "select gender from userDetail where username='" + username
	// // + "'");
	// // String gender = resultSet.getString(0);
	// // return gender;
	// return "man";
	// }
	//
	// // /�ж��������Ƿ�Ϊ����
	// private boolean isFriend(String username, String username_other)
	// throws UserNotFoundException {
	//
	// RosterManager manager = new RosterManager();
	// Roster roster = manager.getRoster(username);
	// boolean flag = roster.isRosterItem(new JID(username_other));
	// System.out.println("��ǰ�˻���" + roster.getUsername() + "  ��"
	// + username_other + "�Ƿ�Ϊ���ѣ�--->> " + flag);
	// // System.out.println("��ϵ�ˣ�"+roster.getRosterItem(new
	// // JID(username_other)).getNickname());
	// return flag;
	// }
	//
	// private IQ updateLocation(IQ packet) {
	// IQ reply = IQ.createResultIQ(packet);
	//
	// Element iq = packet.getChildElement();
	// JID from = packet.getFrom();
	// String username = from.getNode();
	//
	// Element item = iq.element("item");
	// Double myLon = Double.parseDouble(item.attributeValue("longitude"));
	// Double myLat = Double.parseDouble(item.attributeValue("latitude"));
	// long updatetime = System.currentTimeMillis();
	//
	// boolean f = insertLocation(myLon, myLat, username, updatetime);
	// if (f) {
	// // reply.setChildElement(iq);
	// } else {
	// reply.setType(IQ.Type.error);
	// reply.setError(PacketError.Condition.internal_server_error);
	// }
	//
	// return reply;
	// }
	//
	// private boolean insertLocation(Double myLon, double myLat, String
	// username,
	// long updatetime) {
	// boolean f = false;
	//
	// // /����λ�����ݱ��м�¼�ظ�����
	// try {
	// // /�õ����ݿ��������
	// Statement statement = openfireConn.createStatement();
	// // /
	// ResultSet resultSet = statement
	// .executeQuery("select * from ofLocation where username = \'"
	// + username + "\';");
	// System.out.println(resultSet.toString());
	// if (resultSet.next())
	// statement.execute("delete from ofLocation where username = \'"
	// + username + "\';");
	// statement.close();
	// } catch (SQLException e2) {
	// e2.printStackTrace();
	// }
	//
	// PreparedStatement pstmt = null;
	// try {
	// pstmt = openfireConn.prepareStatement(SQL_UPDATE_LOCATION);
	// pstmt.setString(1, username);
	// pstmt.setLong(2, updatetime);
	// pstmt.setDouble(3, myLon);
	// pstmt.setDouble(4, myLat);
	// pstmt.setString(5, getUserGender(username));
	// pstmt.executeUpdate();
	//
	// f = true;
	// } catch (SQLException e1) {
	// f = false;
	// e1.printStackTrace();
	// }
	// try {
	// pstmt.close();
	// } catch (SQLException e) {
	// // TODO �Զ����ɵ� catch ��
	// e.printStackTrace();
	// }
	// return f;
	// }

}
