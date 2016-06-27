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
public class OpenfireLBSPlugin implements Plugin {

	private static final String MODULE_NAME_LOCATION = "location";
	private String new_sql = "create table ofLocation (username varchar(40) primary key, updatetime bigint, longitude double, latitude double,gender varchar(10),nickname varchar(30),jid varchar(30))";
	private String delete_sql = "drop table ofLocation;";
	private String is_exist = "select * from ofLocation";
	private XMPPServer server;
	private Connection openfireDBConn;
	private LocationHandler locationHandler;

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		System.out.println("插件加载中。。。。OpenfireLBSPlugin");

		// /新建数据表
		try {
			openfireDBConn = DbConnectionManager.getConnection();
			Statement statement = openfireDBConn.createStatement();
			// statement.execute(delete_sql);
			// statement.execute(new_sql);
			statement.executeQuery(is_exist);
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
		// .executeQuery("select * from ofLocation");
		// int count = testResultSet.getMetaData().getColumnCount();
		// System.out.println("count = " + count);
		// for (int i = 1; i <= count; i++)
		// System.out
		// .println(testResultSet.getMetaData().getColumnName(i));
		// } catch (SQLException e1) {
		// e1.printStackTrace();
		// }

		server = XMPPServer.getInstance();
		locationHandler = new LocationHandler(MODULE_NAME_LOCATION);
		server.getIQRouter().addHandler(locationHandler);

	}

	@Override
	public void destroyPlugin() {
		server = XMPPServer.getInstance();

		if (locationHandler != null) {
			server.getIQRouter().removeHandler(locationHandler);
			locationHandler = null;
		}

		System.out.println("插件销毁中。。。OpenfireLBSPlugin");
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
