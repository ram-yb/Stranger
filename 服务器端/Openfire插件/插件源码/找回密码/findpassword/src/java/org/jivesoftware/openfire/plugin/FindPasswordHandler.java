package org.jivesoftware.openfire.plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.crowd.jaxb.User;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import com.lotus.sametime.community.kernel.vpkmsg.t;
import com.tangosol.util.UUID;

public class FindPasswordHandler extends IQHandler {

	private static final String SUCCESS = "1";
	private static final String NO_USER = "2";
	private static final String EMAIL_UNAVAILABLE = "3";
	private static final String SERVER_ERROR = "4";
	private static final String FAIL = "5";
	private static final String NAME_SPACE = "urn:xmpp:rayo:findpassword";
	private IQHandlerInfo info;
	private Connection openfireConn;
	private static final String SQL_CODE = "update FindPassword set code=?, time=? where username=?";
	private static final String SQL_UPDATE = "update FindPassword set password=?, passwordMD5=?, e_mail=? where username=?";
	private static final String SQL_INSERT = "insert into FindPassword(username, password,passwordMD5,e_mail,account) values(?,?,?,?,?)";
	private static final String SQL_QUERY = "select * from FindPassword where username=";

	private static final String content1 = "亲爱的Stranger用户，您好！<br/><br/>您申请修改Stranger账号密码，如非本人操作，请忽略此邮件。<br/><br/>服务器验证码：<font size=\"3\" color=\"red\">";
	private static final String content2 = "</font><br/>您可以根据此验证码到App上对Stranger账户进行密码修改<br/><br/>注意：此验证码只在一星期内生效！<br/><br/><br/><br/>本邮件是系统自动发送的，请勿直接回复!";

	public FindPasswordHandler(String moduleName) {
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

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		System.out.println("-->>FindPasswordHandler");
		IQ reply = saveData(packet);
		System.out.println("reply-->>" + reply.toXML());
		return reply;
	}

	public IQ saveData(IQ packet) {
		System.out.println(">>>>>>>>>>>>> RECV IQ: " + packet.toXML()); //
		IQ reply = IQ.createResultIQ(packet);
		DocumentFactory factory = new DocumentFactory();
		// get users near me(from JID)
		if (IQ.Type.get.equals(packet.getType())) {// 查找密码
			System.out.println("====>>IQ  SUCCESS！！！--GET");
			// 获取信息
			Element findElement = packet.getChildElement().element(
					"findpassword");
			String username = findElement.attributeValue("username");
			String type = findElement.attributeValue("type");
			System.out.println("USERNAME = " + username);

			if ("code".equals(type)) {
				String email = findElement.attributeValue("email");
				String account = findElement.attributeValue("account");
				// 创建回复节点信息
				Element element = factory.createElement("query", NAME_SPACE);
				Element infoElement = factory.createElement("findpassword");
				element.add(infoElement);
				// 打开数据库查找
				try {
					Statement statement = openfireConn.createStatement(
							ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_UPDATABLE);
					ResultSet resultSet = statement.executeQuery(SQL_QUERY
							+ "'" + username + "'");

					resultSet.last();
					int count = resultSet.getRow();
					if (count <= 0) {
						infoElement.addAttribute("status", NO_USER);
					} else {
						// 从结果集把数据封装到节点中
						resultSet.first();
						// TODO 发邮件部分
						System.out.println("发邮件");
						Map<String, String> map = new HashMap<String, String>();
						for (int i = 1; i <= resultSet.getMetaData()
								.getColumnCount(); i++) {
							String name = resultSet.getMetaData()
									.getColumnName(i);
							String value = resultSet.getString(i);
							map.put(name, value);
						}
						System.out.println("-->> username="
								+ map.get("USERNAME") + " server email = "
								+ map.get("E_MAIL")+"  client email = "+email);
						if (map.get("E_MAIL").equals(email.trim())) {
							// 生成8位随机验证码
							String code = (new Random().nextInt(1000000) + System
									.currentTimeMillis()) + "";
							code = code.substring(code.length() - 8);
							System.out.println("code = " + code);
							// 保存验证码
							PreparedStatement statement2 = openfireConn
									.prepareStatement(SQL_CODE);
							statement2.setString(1, code);
							statement2.setString(2, System.currentTimeMillis()
									+ "");
							statement2.setString(3, username);
							statement2.execute();
							try {
								// 发送邮件
								JavaMailWithAttachment se = new JavaMailWithAttachment(
										true);
								// File affix = new File("D:\\1.txt");
								boolean result = se.doSendHtmlEmail(
										"Stranger安全中心", content1 + code
												+ content2, email, null);
								if (result)
									infoElement.addAttribute("status", SUCCESS);
								else
									infoElement.addAttribute("status",
											EMAIL_UNAVAILABLE);
							} catch (Exception e) {
								infoElement.addAttribute("status",
										EMAIL_UNAVAILABLE);
							}
						} else
							infoElement.addAttribute("status",
									EMAIL_UNAVAILABLE);
					}
				} catch (SQLException e) {
					e.printStackTrace();
					infoElement.addAttribute("status", SERVER_ERROR);
				}
				reply.setChildElement(element);
			} else if ("password".equals(type)) {
				String code = findElement.attributeValue("code");
				System.out.println("User send code -->> " + code);
				// 创建回复节点信息
				Element element = factory.createElement("query", NAME_SPACE);
				Element infoElement = factory.createElement("findpassword");
				element.add(infoElement);
				// 打开数据库查找
				try {
					Statement statement = openfireConn.createStatement();
					ResultSet resultSet = statement.executeQuery(SQL_QUERY
							+ "'" + username + "'");
					resultSet.next();
					String username1 = resultSet.getString("USERNAME");
					String code1 = resultSet.getString("CODE");
					String passwordMD5 = resultSet.getString("PASSWORDMD5");
					long time = Long.parseLong(resultSet.getString("TIME"));
					System.out.println("-->> username=" + username1
							+ "  code = " + code1);

					if (code1.equals(code)
							&& System.currentTimeMillis() - time < 7 * 24 * 3600) {
						infoElement.addAttribute("status", SUCCESS);
						infoElement.addAttribute("password", passwordMD5);
					} else
						infoElement.addAttribute("status", FAIL);
				} catch (SQLException e) {
					e.printStackTrace();
					infoElement.addAttribute("status", SERVER_ERROR);
				}
				reply.setChildElement(element);
			}
			return reply;
		} else if (IQ.Type.set.equals(packet.getType())) {
			System.out.println("====>>IQ  SUCCESS！！！--SET");
			// 获取信息
			Element element = packet.getChildElement().element("findpassword");
			String username = element.attributeValue("username");
			String type = element.attributeValue("type");

			if ("insert".equals(type)) {// 插入，注册时
				// 加工SQL语句
				String passwordMD5 = element.attributeValue("password");
				String password = MD5Utils.convertMD5(passwordMD5);
				String email = element.attributeValue("email");
				String account = element.attributeValue("account");

				// 创建回复Elememt
				Element childElement = factory.createElement("query",
						NAME_SPACE);
				Element infoElement = factory.createElement("findpassword");
				childElement.add(infoElement);

				// 开启数据库,载入数据
				try {
					PreparedStatement statement = openfireConn
							.prepareStatement(SQL_INSERT);
					statement.setString(1, username);
					statement.setString(2, password);
					statement.setString(3, passwordMD5);
					statement.setString(4, email);
					statement.setString(5, account);
					statement.execute();

					infoElement.addAttribute("status", SUCCESS);
				} catch (SQLException e) {
					e.printStackTrace();
					infoElement.addAttribute("status", FAIL);
				}
				reply.setChildElement(childElement);
				System.out.println(reply.toXML());
			} else if ("update".equals(type)) {// 更新，登陆时
				// 加工SQL语句
				String passwordMD5 = element.attributeValue("password");
				String password = MD5Utils.convertMD5(passwordMD5);
				String email = element.attributeValue("email");
				String account = element.attributeValue("account");

				// 创建恢复Elememt
				Element childElement = factory.createElement("query",
						NAME_SPACE);
				Element infoElement = factory.createElement("findpassword");
				childElement.add(infoElement);

				// 开启数据库,载入数据
				try {
					PreparedStatement statement = openfireConn
							.prepareStatement(SQL_UPDATE);
					statement.setString(1, password);
					statement.setString(2, passwordMD5);
					statement.setString(3, email);
					statement.setString(4, username);
					statement.execute();

					infoElement.addAttribute("status", SUCCESS);
				} catch (SQLException e) {
					e.printStackTrace();
					infoElement.addAttribute("status", FAIL);
				}
				reply.setChildElement(childElement);
				System.out.println(reply.toXML());
			}
		} else {
			System.out.println("====>>IQ  ERROR！！！--ERROR");
			reply.setType(IQ.Type.error);
			reply.setError(PacketError.Condition.bad_request);
		}
		return reply;
	}
}
