package org.jivesoftware.openfire.plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;

public class UserDetailPlugin implements Plugin {

	private static final String MODULE_NAME_LOCATION = "userdetail";
	private String new_sql = "create table userDetail(username varchar(40) primary key, nickname varchar(40), realname varchar(20),gender varchar(8),constellation varchar(10),animal varchar(5),location_now varchar(100),hometown varchar(100),phone_number varchar(20),e_mail varchar(30),user_age varchar(5),user_birthday varchar(20))";
	//
	// username primary key, nickname , realname(����),
	// ,gender
	// ,user_age,user_birthday,constellation��������,animal����Ф��,hometown,location_now�����ڵأ�,phone_number,e_mail,headimage
	// ���ͣ�headimage ��blob ,��������varchar
	private String delete_sql = "drop table userDetail";
	private String is_exist = "select * from userDetail";
	private XMPPServer server;
	private Connection openfireDBConn;
	private UserDetailManager manager;

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		System.out.println("��������С�������UserDetailManager");

		// /�½����ݱ�
		// try {
		// openfireDBConn = DbConnectionManager.getConnection();
		// Statement statement = openfireDBConn.createStatement();
		// statement.executeQuery(is_exist);
		// openfireDBConn.close();
		// openfireDBConn = null;
		// } catch (SQLException e) {
		// try {
		// DbConnectionManager.getConnection().createStatement()
		// .execute(new_sql);
		// openfireDBConn.close();
		// openfireDBConn = null;
		// } catch (SQLException e1) {
		// e1.printStackTrace();
		// }
		// }

		// try {
		// Statement testStatement = openfireDBConn.createStatement();
		// ResultSet testResultSet = testStatement
		// .executeQuery("select * from userDetail");
		// int count = testResultSet.getMetaData().getColumnCount();
		// System.out.println("count = " + count);
		// for (int i = 1; i <= count; i++)
		// System.out
		// .println(testResultSet.getMetaData().getColumnName(i));
		// } catch (SQLException e1) {
		// e1.printStackTrace();
		// }

		server = XMPPServer.getInstance();
		this.manager = new UserDetailManager(MODULE_NAME_LOCATION);
		server.getIQRouter().addHandler(this.manager);
	}

	@Override
	public void destroyPlugin() {
		// server = XMPPServer.getInstance();

		if (manager != null) {
			server.getIQRouter().removeHandler(manager);
			manager = null;
		}

		System.out.println("��������С�����UserDetailPlugin");
		// if (openfireDBConn != null) {
		// try {
		// openfireDBConn.close();
		// openfireDBConn = null;
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }
		// }
	}

}
