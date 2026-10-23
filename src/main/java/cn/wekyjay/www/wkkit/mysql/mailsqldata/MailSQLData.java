package cn.wekyjay.www.wkkit.mysql.mailsqldata;

import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import cn.wekyjay.www.wkkit.tool.MessageManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MailSQLData {
	public static void createTable(){
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		String cmd = SQLCommand.CREATE_TABLE.format(MySQLManager.getTablePrefix());
		try {
			ps = connection.prepareStatement(cmd);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			MessageManager.info(LangConfigLoader.getString("MAIL_SQL_CREATE_FAIL"));
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * 插入数据 — 修复：去掉硬编码 id=0
	 */
	public void insertData(String playername, String kitname, int num) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.ADD_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			ps.setInt(3, num);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * Upsert：插入或更新（依赖 UNIQUE KEY idx_player_kit）
	 */
	public void upsertData(String playername, String kitname, int num) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.UPSERT_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			ps.setInt(3, num);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	public void deleteData(String playername, String kitname) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.DELETE_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * 查询某玩家所有邮箱 kitname — 修复：只 SELECT kitname
	 */
	public List<String> findKitName(String playername) {
		List<String> list = new ArrayList<String>();
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_KIT_NAMES.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(rs.getString("kitname"));
			}
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
		return list;
	}

	/**
	 * 判断玩家是否存在 — 修复：LIMIT 1 + SELECT 1
	 */
	public Boolean findPlayer(String playername) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_EXISTS.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
		return false;
	}

	/**
	 * 查询某玩家+某礼包的 num — 修复：直接 WHERE 过滤，避免 SELECT * + Java 遍历
	 */
	public int findKitNum(String playername, String kitname) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_KIT_NUM.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt("num");
			}
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
		return 0;
	}

	/**
	 * 检查某玩家+某礼包是否存在 — 新增，用于 contain_Mail
	 */
	public Boolean findPlayerKitExists(String playername, String kitname) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_PLAYER_KIT_EXISTS.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
		return false;
	}

	public void update_Num(String playername, String kitname, int value) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.UPDATE_NUM_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setInt(1, value);
			ps.setString(2, playername);
			ps.setString(3, kitname);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(null,ps,connection);
		}
	}
}
