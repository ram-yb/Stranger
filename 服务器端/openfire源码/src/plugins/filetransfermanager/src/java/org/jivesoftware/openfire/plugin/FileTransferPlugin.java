package org.jivesoftware.openfire.plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;

public class FileTransferPlugin implements Plugin {

	private static final String MODULE_NAME_LOCATION = "file-transfer";
	private FileTransferManager manager = null;
	private String is_exist = "select * from ofFileTransfer";
	// auto_increment not null id bigint ,
	private String new_sql = "create table ofFileTransfer (_id bigint primary key,fileName varchar(256), saveFileName varchar(300),mime_type varchar(40),status varchar(30),_from varchar(100),_to varchar(100),date bigint)";
	private String delete_sql = "drop table ofFileTransfer;";
	private Connection openfireDBConn;

	// id,fileName, saveFileName,mime_type,status,from,to,date

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		System.out.println("插件加载中。。。FileTransferManager");

		try {
			openfireDBConn = DbConnectionManager.getConnection();
			Statement statement = openfireDBConn.createStatement();
			statement.executeQuery(is_exist);
			openfireDBConn.close();
			openfireDBConn = null;
		} catch (SQLException e) {
			try {
				DbConnectionManager.getConnection().createStatement()
						.execute(new_sql);
				System.out.println("table create success!!");
				openfireDBConn.close();
				openfireDBConn = null;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

		// try {
		// Statement testStatement = openfireDBConn.createStatement();
		// ResultSet testResultSet = testStatement
		// .executeQuery("select * from ofFileTransfer");
		// int count = testResultSet.getMetaData().getColumnCount();
		// System.out.println("count = " + count);
		// for (int i = 1; i <= count; i++)
		// System.out
		// .println(testResultSet.getMetaData().getColumnName(i));
		// } catch (SQLException e1) {
		// e1.printStackTrace();
		// }

		this.manager = new FileTransferManager();
		InterceptorManager.getInstance().addInterceptor(this.manager);
	}

	@Override
	public void destroyPlugin() {
		// TODO Auto-generated method stub
		System.out.println("插件销毁中。。。FileTransferManager");
		if (openfireDBConn != null) {
			try {
				openfireDBConn.close();
				openfireDBConn = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (manager != null) {
			InterceptorManager.getInstance().removeInterceptor(manager);
			manager = null;
		}
	}
}
