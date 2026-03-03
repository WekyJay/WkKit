package cn.wekyjay.www.wkkit.mysql;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.mysql.cdksqldata.CdkSQLData;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.mysql.mailsqldata.MailSQLData;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.mysql.playersqldata.PlayerSQLData;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.tool.Druid;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;

import java.sql.*;

public class MySQLManager {
	private String ip;
	private String databaseName;
	private String userName;
	private String userPassword;
	private Connection connection;
	private String port;
	private boolean useSSL;
	private String tablePrefix;
	public static MySQLManager instance = null;

	
	public static MySQLManager get() {
        return instance == null ? instance = new MySQLManager() : instance;
    }
	
	public static String getTablePrefix() {
        return get().tablePrefix == null ? "" : get().tablePrefix;
    }
	

	public Connection getConnection() {
		try {
			connection = Druid.getConnection();
		} catch (SQLException throwables) {
			ExceptionHandler.handle("未知操作", throwables);
		}
		return connection;
	}
	
	/**
	 * 启动数据库
	 */
	public void enableMySQL()
	{
		WkKit.getWkKit().getLogger().info(LangConfigLoader.getString("MYSQL_CONNECTING"));
		ip = WkKit.getWkKit().getConfig().getString("MySQL.ip");
		databaseName = WkKit.getWkKit().getConfig().getString("MySQL.databasename");
		userName = WkKit.getWkKit().getConfig().getString("MySQL.username");
		userPassword = WkKit.getWkKit().getConfig().getString("MySQL.password");
		port = WkKit.getWkKit().getConfig().getString("MySQL.port");
		useSSL = WkKit.getWkKit().getConfig().getBoolean("MySQL.useSSL", false);
		tablePrefix = WkKit.getWkKit().getConfig().getString("MySQL.tablePrefix", "");
		// 连接数据库
		connectMySQL();
		// 创建表
		MailSQLData.createTable();
		PlayerSQLData.createTable();
		CdkSQLData.createTable();
	}
	
	/**
	 * 连接数据库
	 */
	private void connectMySQL(){
		try {
			String init = databaseName.contains("?")?"&serverTimezone=UTC":"?serverTimezone=UTC";
			init += "&useSSL=" + useSSL;
			Druid druid = new Druid("jdbc:mysql://" + ip + ":" + port + "/" + databaseName + init,userName,userPassword);
			connection = druid.getConnection();
			WkKit.getWkKit().getLogger().info(LangConfigLoader.getString("MYSQL_CONNECT_SUCCESS"));
		}catch(SQLTimeoutException e1) {
			WkKit.getWkKit().getLogger().severe(LangConfigLoader.getString("MYSQL_CONECTTIMEOUT"));
			WkKit.getWkKit().getConfig().set("MySQL.Enable",false);
			WkKit.getWkKit().saveConfig();
		}catch (SQLException e) {
			WkKit.getWkKit().getLogger().severe(LangConfigLoader.getString("MYSQL_CONECTFAILED"));
			WkKit.getWkKit().getConfig().set("MySQL.Enable",false);
			WkKit.getWkKit().saveConfig();
		}finally {
			close(null,null,connection);
		}
	}
	/**
	 * 执行MySQL命令
	 * @param ps
	 */
	public void doCommand(PreparedStatement ps){
		try {
			ps.executeUpdate();
		} catch(SQLIntegrityConstraintViolationException sql) {
			WkKit.getWkKit().getLogger().warning(ChatColor.YELLOW + LangConfigLoader.getString("MYSQL_DATEEXISTS"));
		}catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}
	}
	
	/**
	 * 关闭数据库
	 */
	public void shutdown() {
		close(null,null,connection); // 关闭数据库连接
		Druid.shutdown(); // 回收连接池
		WkKit.getWkKit().getLogger().info(LangConfigLoader.getString("MYSQL_SHUTDOWN"));
	}

	/**
	 * 关闭连接<br/>
	 * 在数据库连接池技术中，close 不是真的断掉连接,而是把使用的 Connection 对象放回连接池
	 * @param resultSet
	 * @param statement
	 * @param connection
	 */
	public static void close(ResultSet resultSet, Statement statement, Connection connection) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
