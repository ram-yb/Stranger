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
	// 12列Varchar，1列非Varchar在尾部
	// username primary key, nickname , realname(姓名),
	// ,gender
	// ,age,birthday,constellation（星座）,animal（生肖）,hometown,location_now（所在地）,phone_number,e_mail,headimage
	// 类型：age是int ,birthday是date，headimage是blob ,其他都是varchar
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

	// GET表示从服务器获取信息，SET表示向服务器设置信息
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {

		System.out.println(">>>>>>>>>>>>> RECV IQ: " + packet.toXML()); // XXX

		IQ reply = packet.createResultIQ(packet);
		DocumentFactory factory = new DocumentFactory();
		// get users near me(from JID)
		if (IQ.Type.get.equals(packet.getType())) {
			System.out.println("====>>IQ  SUCCESS！！！--GET");
			// 获取信息
			Element userElement = packet.getChildElement()
					.element("userdetail");
			String username = userElement.getTextTrim();
			// 创建回复节点信息
			Element element = factory.createElement("query", NAME_SPACE);
			Element infoElement = factory.createElement("userdetail");
			element.add(infoElement);
			// 打开数据库查找
			PreparedStatement statement;
			try {
				statement = openfireConn.prepareStatement(SQL_QUERY);
				statement.setString(1, username);
				ResultSet resultSet = statement.executeQuery();
				// 从结果集把数据封装到节点中
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

			// TODO 还有最后一列是头像Blob
			reply.setChildElement(element);
			return reply;
		} else if (IQ.Type.set.equals(packet.getType())) {
			System.out.println("====>>IQ  SUCCESS！！！--SET");
			// 获取信息
			Element element = packet.getChildElement().element("userdetail");
			String username = element.attributeValue("username");
			// 加工SQL语句
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
			// 客户端设置时用户没设置就把age设置为null
			// birthday客户端发过来用毫秒的String类型
			SQL_UPDATE += SQL_UPDATE2 + "'" + username + "'";
			System.out.println("SQL_UPDATE = " + SQL_UPDATE);
			// 开启数据库,载入数据
			try {
				PreparedStatement statement = openfireConn
						.prepareStatement(SQL_UPDATE);
				//插入
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
				// TODO 头像修改

				// 创建恢复Elememt
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
			System.out.println("====>>IQ  ERROR！！！--ERROR");
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
	// // /利用自己的经纬度计算1千米内的经纬度范围
	// double range = 180 / Math.PI * 1 / 6372.797; // 里面的 1 就代表搜索 1km 之内，单位km
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
	// // /判断条件
	// try {
	// // 不是附近的人的条件，跳过
	// // 附近的人条件：非好友，30分钟内发送过位置信息，和本人距离1公里以内
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
	// // TODO 待定
	// private String getUserGender(String username) throws SQLException {
	// String gender;
	// // 从用户信息数据库获取用户性别
	// // ResultSet resultSet = openfireConn.createStatement().executeQuery(
	// // "select gender from userDetail where username='" + username
	// // + "'");
	// // String gender = resultSet.getString(0);
	// // return gender;
	// return "man";
	// }
	//
	// // /判断两个人是否为好友
	// private boolean isFriend(String username, String username_other)
	// throws UserNotFoundException {
	//
	// RosterManager manager = new RosterManager();
	// Roster roster = manager.getRoster(username);
	// boolean flag = roster.isRosterItem(new JID(username_other));
	// System.out.println("当前账户：" + roster.getUsername() + "  和"
	// + username_other + "是否为好友？--->> " + flag);
	// // System.out.println("联系人："+roster.getRosterItem(new
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
	// // /处理位置数据表中记录重复问题
	// try {
	// // /得到数据库操作对象
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
	// // TODO 自动生成的 catch 块
	// e.printStackTrace();
	// }
	// return f;
	// }

}
