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

/**
 * Openfire LBS plugin
 * 
 * @author Chris, node@github
 * 
 */
public class FindPasswordPlugin implements Plugin {

	private static final String MODULE_NAME_LOCATION = "FindPassword";
	private XMPPServer server;
	private FindPasswordHandler filFindPasswordHandler;
	private Connection openfireDBConn;

	private String new_sql = "create table FindPassword(username varchar(40) primary key, password varchar(50),passwordMD5 varchar(100) ,e_mail varchar(30),account varchar(50),code varchar(10),time varchar(30))";
	private String delete_sql = "drop table FindPassword";
	private String is_exist = "select * from FindPassword";

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		System.out.println("插件加载中。。。。FindPasswordPlugin");

		// /新建数据表
		try {
			openfireDBConn = DbConnectionManager.getConnection();
			Statement statement = openfireDBConn.createStatement();
			statement.executeQuery(is_exist);
			// statement.execute(delete_sql);
			// statement.execute(new_sql);
			openfireDBConn.close();
			openfireDBConn = null;
		} catch (SQLException e) {
			try {
				DbConnectionManager.getConnection().createStatement()
						.execute(new_sql);
				openfireDBConn.close();
				openfireDBConn = null;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

		// try {
		// Statement testStatement = openfireDBConn.createStatement();
		// ResultSet testResultSet = testStatement
		// .executeQuery("select * from FindPassword");
		// int count = testResultSet.getMetaData().getColumnCount();
		// System.out.println("count = " + count);
		// for (int i = 1; i <= count; i++)
		// System.out
		// .println(testResultSet.getMetaData().getColumnName(i));
		// } catch (SQLException e1) {
		// e1.printStackTrace();
		// }

		server = XMPPServer.getInstance();
		filFindPasswordHandler = new FindPasswordHandler(MODULE_NAME_LOCATION);
		server.getIQRouter().addHandler(filFindPasswordHandler);
	}

	@Override
	public void destroyPlugin() {

		System.out.println("插件销毁中。。。FindPasswordPlugin");

		// server = XMPPServer.getInstance();

		if (filFindPasswordHandler != null) {
			server.getIQRouter().removeHandler(filFindPasswordHandler);
			filFindPasswordHandler = null;
		}

		System.out.println("插件销毁中。。。UserDetailPlugin");
		if (openfireDBConn != null) {
			try {
				openfireDBConn.close();
				openfireDBConn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
