package org.jivesoftware.openfire.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

/**
 * Location IQHandler
 * 
 * @author Chris, node@github
 * 
 */
public class LocationHandler extends IQHandler {// /数据库操作不行 查找陌生人功能

	private static final String NAME_SPACE = "urn:xmpp:rayo:lbsservice";
	private static final String SQL_UPDATE_LOCATION = "INSERT INTO ofLocation (username, updatetime, longitude, latitude,gender,nickname,jid) VALUES (?,?,?,?,?,?,?)";
	// /先简化查找范围，排除大部分人
	private static final String SQL_USERS_NEARME = "select * from ofLocation where (longitude between ? and ? ) AND (latitude between ? and ?)";

	private IQHandlerInfo info;
	private Connection openfireConn;

	public LocationHandler(String moduleName) {
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

	// GET是查找附近的人，SET是把自己的位置信息发送给好友
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {

		System.out.println(">>>>>>>>>>>>> RECV IQ: " + packet.toXML()); // XXX

		// get users near me(from JID)
		if (IQ.Type.get.equals(packet.getType())) {
			System.out.println("====>>IQ  SUCCESS！！！--1");
			return getUsersNearme(packet);
			// set from JID's location to ...
		} else if (IQ.Type.set.equals(packet.getType())) {
			JID to = packet.getTo();

			// send from JID's location to to JID
			if (to.getNode() != null && !to.getNode().equals("")) {
				XMPPServer.getInstance().getIQRouter().route(packet); // route
																		// to
																		// another
																		// user
				System.out.println("====>>IQ  to friend！！！--2");
				return IQ.createResultIQ(packet);
				// send from JID's location to server , and update ofLocation
			} else {
				System.out.println("====>>IQ  username is null！！！--3");
				return updateLocation(packet);
			}
		} else {
			IQ reply = IQ.createResultIQ(packet);
			System.out.println("====>>IQ  ERROR！！！--4");
			reply.setType(IQ.Type.error);
			reply.setError(PacketError.Condition.bad_request);
			return reply;
		}
	}

	/**
	 * 
	 * 
	 * @param packet
	 * @return
	 */
	private IQ getUsersNearme(IQ packet) {

		System.out.println("id-->" + packet.getID() + " -element.name--> "
				+ packet.getChildElement().getName() + " --from -> "
				+ packet.getFrom() + " --to-> " + packet.getTo() + "--type-> "
				+ packet.getType() + " --namespace->"
				+ packet.getElement().getNamespace());

		IQ reply = IQ.createResultIQ(packet);
		DocumentFactory factory = new DocumentFactory();
		Element data = factory.createElement("query", NAME_SPACE);
		JID from = packet.getFrom();
		reply.setTo(from);

		Element iq = packet.getChildElement();
		Element item = iq.element("item");
		long updatetime = System.currentTimeMillis();
		String username = from.getNode();
		// String username = item.attributeValue("username");
		Double myLon = Double.parseDouble(item.attributeValue("longitude"));
		Double myLat = Double.parseDouble(item.attributeValue("latitude"));
		String gender = item.attributeValue("gender");
		String nickname = item.attributeValue("nickname");
		String jid = from.toString();

		System.out.println("-->> username=" + username + "  updatetime = "
				+ updatetime / 1000 + "   longitude = " + myLon
				+ "  latitude = " + myLat + " gender = " + gender
				+ " nickname=" + nickname + " jid = " + jid);

		// XXX: update user location firstly
		insertLocation(myLon, myLat, username, updatetime, gender, nickname,
				jid);

		// /利用自己的经纬度计算1千米内的经纬度范围
		double range = 180 / Math.PI * 1 / 6372.797; // 里面的 1 就代表搜索 1km 之内，单位km
		double lngR = range / Math.cos(myLat * Math.PI / 180.0);
		double maxLat = myLat + range;
		double minLat = myLat - range;
		double maxLng = myLon + lngR;
		double minLng = myLon - lngR;

		System.out.println("minLat=" + minLat + "  maxlat=" + maxLat
				+ "  minlong=" + minLng + "  maxlong=" + maxLng);

		// find users near me
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = openfireConn.prepareStatement(SQL_USERS_NEARME);
			pstmt.setDouble(1, minLng);
			pstmt.setDouble(2, maxLng);
			pstmt.setDouble(3, minLat);
			pstmt.setDouble(4, maxLat);
			rs = pstmt.executeQuery();

			String username_other = null;
			double nearLon = 0;
			double nearLat = 0;
			long neartime = 0;
			long now = System.currentTimeMillis();

			while (rs.next()) {
				username_other = rs.getString("username");
				nearLon = rs.getDouble("longitude");
				nearLat = rs.getDouble("latitude");
				neartime = now - rs.getLong("updatetime");
				gender = rs.getString("gender");
				nickname = rs.getString("nickname");
				jid = rs.getString("jid");

				// /判断条件
				try {
					// 不是附近的人的条件，跳过
					// 附近的人条件：非好友，30分钟内发送过位置信息，和本人距离1公里以内
					if (username_other.equals(username)
							|| neartime > 30 * 60 * 1000
							|| isFriend(username, username_other))
						continue;
				} catch (UserNotFoundException e1) {
					e1.printStackTrace();
				}

				Element e = data.addElement("item");

				e.addAttribute("username", username_other);
				e.addAttribute("longitude", Double.toString(nearLon));
				e.addAttribute("latitude", Double.toString(nearLat));
				e.addAttribute("updatetime", Double.toString(neartime));
				e.addAttribute("gender", gender);
				e.addAttribute("nickname", nickname);
				e.addAttribute("jid", jid);
			}
			reply.setChildElement(data);

		} catch (SQLException e1) {
			reply.setType(IQ.Type.error);
			reply.setError(PacketError.Condition.internal_server_error);
			e1.printStackTrace();
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reply;
	}

	// TODO 待定
	// private String getUserGender(String username) throws SQLException {
	// // 从用户信息数据库获取用户性别
	// ResultSet resultSet = openfireConn.createStatement().executeQuery(
	// "select gender from userDetail where username='" + username
	// + "'");
	// String gender = null;
	// for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
	// if ("gender".equalsIgnoreCase(resultSet.getMetaData().getCatalogName(i)))
	// gender = resultSet.getString(i);
	// }
	// System.out.println("LocationHander-->>gender = " + gender);
	// return gender;
	// }

	// /判断两个人是否为好友
	private boolean isFriend(String username, String username_other)
			throws UserNotFoundException {

		RosterManager manager = new RosterManager();
		Roster roster = manager.getRoster(username);
		boolean flag = roster.isRosterItem(new JID(username_other));
		System.out.println("当前账户：" + roster.getUsername() + "  和"
				+ username_other + "是否为好友？--->> " + flag);
		// System.out.println("联系人："+roster.getRosterItem(new
		// JID(username_other)).getNickname());
		return flag;
	}

	private IQ updateLocation(IQ packet) {
		IQ reply = IQ.createResultIQ(packet);

		Element iq = packet.getChildElement();
		JID from = packet.getFrom();
		String username = from.getNode();

		Element item = iq.element("item");
		Double myLon = Double.parseDouble(item.attributeValue("longitude"));
		Double myLat = Double.parseDouble(item.attributeValue("latitude"));
		String gender = item.attributeValue("gender");
		String nickname = item.attributeValue("nickname");
		String jid = item.attributeValue("jid");
		long updatetime = System.currentTimeMillis();

		boolean f = insertLocation(myLon, myLat, username, updatetime, gender,
				nickname, jid);
		if (f) {
			// reply.setChildElement(iq);
		} else {
			reply.setType(IQ.Type.error);
			reply.setError(PacketError.Condition.internal_server_error);
		}

		return reply;
	}

	private boolean insertLocation(Double myLon, double myLat, String username,
			long updatetime, String gender, String nickname, String jid) {
		boolean f = false;

		// /处理位置数据表中记录重复问题
		try {
			// /得到数据库操作对象
			Statement statement = openfireConn.createStatement();
			// /
			ResultSet resultSet = statement
					.executeQuery("select * from ofLocation where username = \'"
							+ username + "\';");
			System.out.println(resultSet.toString());
			if (resultSet.next())
				statement.execute("delete from ofLocation where username = \'"
						+ username + "\';");
			statement.close();
		} catch (SQLException e2) {
			e2.printStackTrace();
		}

		PreparedStatement pstmt = null;
		try {
			pstmt = openfireConn.prepareStatement(SQL_UPDATE_LOCATION);
			pstmt.setString(1, username);
			pstmt.setLong(2, updatetime);
			pstmt.setDouble(3, myLon);
			pstmt.setDouble(4, myLat);
			pstmt.setString(5, gender);
			pstmt.setString(6, nickname);
			pstmt.setString(7, jid);
			pstmt.executeUpdate();

			f = true;
		} catch (SQLException e1) {
			f = false;
			e1.printStackTrace();
		}
		try {
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return f;
	}

}
