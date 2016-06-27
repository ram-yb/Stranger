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

	private static final String content1 = "�װ���Stranger�û������ã�<br/><br/>�������޸�Stranger�˺����룬��Ǳ��˲���������Դ��ʼ���<br/><br/>��������֤�룺<font size=\"3\" color=\"red\">";
	private static final String content2 = "</font><br/>�����Ը��ݴ���֤�뵽App�϶�Stranger�˻����������޸�<br/><br/>ע�⣺����֤��ֻ��һ��������Ч��<br/><br/><br/><br/>���ʼ���ϵͳ�Զ����͵ģ�����ֱ�ӻظ�!";

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
		if (IQ.Type.get.equals(packet.getType())) {// ��������
			System.out.println("====>>IQ  SUCCESS������--GET");
			// ��ȡ��Ϣ
			Element findElement = packet.getChildElement().element(
					"findpassword");
			String username = findElement.attributeValue("username");
			String type = findElement.attributeValue("type");
			System.out.println("USERNAME = " + username);

			if ("code".equals(type)) {
				String email = findElement.attributeValue("email");
				String account = findElement.attributeValue("account");
				// �����ظ��ڵ���Ϣ
				Element element = factory.createElement("query", NAME_SPACE);
				Element infoElement = factory.createElement("findpassword");
				element.add(infoElement);
				// �����ݿ����
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
						// �ӽ���������ݷ�װ���ڵ���
						resultSet.first();
						// TODO ���ʼ�����
						System.out.println("���ʼ�");
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
							// ����8λ�����֤��
							String code = (new Random().nextInt(1000000) + System
									.currentTimeMillis()) + "";
							code = code.substring(code.length() - 8);
							System.out.println("code = " + code);
							// ������֤��
							PreparedStatement statement2 = openfireConn
									.prepareStatement(SQL_CODE);
							statement2.setString(1, code);
							statement2.setString(2, System.currentTimeMillis()
									+ "");
							statement2.setString(3, username);
							statement2.execute();
							try {
								// �����ʼ�
								JavaMailWithAttachment se = new JavaMailWithAttachment(
										true);
								// File affix = new File("D:\\1.txt");
								boolean result = se.doSendHtmlEmail(
										"Stranger��ȫ����", content1 + code
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
				// �����ظ��ڵ���Ϣ
				Element element = factory.createElement("query", NAME_SPACE);
				Element infoElement = factory.createElement("findpassword");
				element.add(infoElement);
				// �����ݿ����
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
			System.out.println("====>>IQ  SUCCESS������--SET");
			// ��ȡ��Ϣ
			Element element = packet.getChildElement().element("findpassword");
			String username = element.attributeValue("username");
			String type = element.attributeValue("type");

			if ("insert".equals(type)) {// ���룬ע��ʱ
				// �ӹ�SQL���
				String passwordMD5 = element.attributeValue("password");
				String password = MD5Utils.convertMD5(passwordMD5);
				String email = element.attributeValue("email");
				String account = element.attributeValue("account");

				// �����ظ�Elememt
				Element childElement = factory.createElement("query",
						NAME_SPACE);
				Element infoElement = factory.createElement("findpassword");
				childElement.add(infoElement);

				// �������ݿ�,��������
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
			} else if ("update".equals(type)) {// ���£���½ʱ
				// �ӹ�SQL���
				String passwordMD5 = element.attributeValue("password");
				String password = MD5Utils.convertMD5(passwordMD5);
				String email = element.attributeValue("email");
				String account = element.attributeValue("account");

				// �����ָ�Elememt
				Element childElement = factory.createElement("query",
						NAME_SPACE);
				Element infoElement = factory.createElement("findpassword");
				childElement.add(infoElement);

				// �������ݿ�,��������
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
			System.out.println("====>>IQ  ERROR������--ERROR");
			reply.setType(IQ.Type.error);
			reply.setError(PacketError.Condition.bad_request);
		}
		return reply;
	}
}
