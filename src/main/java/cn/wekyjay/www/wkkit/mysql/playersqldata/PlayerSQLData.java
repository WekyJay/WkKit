package cn.wekyjay.www.wkkit.mysql.playersqldata;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import cn.wekyjay.www.wkkit.tool.MessageManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerSQLData {
	public static void createTable(){
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		String cmd = SQLCommand.CREATE_TABLE.format(MySQLManager.getTablePrefix());
		try {
			ps = connection.prepareStatement(cmd);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			MessageManager.info(LangConfigLoader.getString("PLAYER_SQL_CREATE_FAIL"));
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * 向数据库添加数据 — 修复：去掉硬编码 id=0
	 */
	public void insertData(String kitname, String playername,String data, int time) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.ADD_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			ps.setString(3, data);
			ps.setInt(4, time);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * 插入或更新（依赖 UNIQUE KEY idx_player_kit）
	 */
	public void upsertData(String kitname, String playername, String data, int time) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.UPSERT_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			ps.setString(3, data);
			ps.setInt(4, time);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	public void deleteData(String kitname, String playername) {
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
		} finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * 查询某玩家所有 kitname — 修复：只 SELECT kitname 字段，不用 SELECT *
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
	 * 判断玩家是否存在 — 修复：用 LIMIT 1 + SELECT 1，不遍历所有数据
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
	 * 查询某玩家+某礼包的 data — 修复：直接 WHERE player=? AND kitname=?，避免 SELECT * 后 Java 遍历
	 */
	public String findKitData(String playername, String kitname) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_KIT_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getString("data");
			}
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
		return null;
	}

	/**
	 * 查询某玩家+某礼包的 time — 修复：同上，直接 WHERE 过滤
	 */
	public Integer findKitTime(String playername, String kitname) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_KIT_TIME.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			rs = ps.executeQuery();
			if (rs.next()) {
				int time = rs.getInt("time");
				if (rs.wasNull()) return null;
				return time;
			}
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
		return null;
	}

	/**
	 * 检查某玩家+某礼包是否存在 — 新增，用于 contain_Kit，避免查全量再 .contains()
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

	public void update_Time_Data(String playername,String kitname, int value) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.UPDATE_TIME_DATA.format(MySQLManager.getTablePrefix());
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

	public void update_Data_Data(String playername,String kitname, String value) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.UPDATE_DATA_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, value);
			ps.setString(2, playername);
			ps.setString(3, kitname);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * （加入排他锁）更新礼包Data数据
	 * 修复：去掉 SELECT * 的全字段扫描，只 SELECT id；去掉 try 里多余的 close
	 */
	public void update_Data_Data_Lock(String playername,String kitname, String value) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// 设置不自动提交事务
			connection.setAutoCommit(false);
			// 搜索ID（只取 id，避免 SELECT *）
			int id = 0;
			String s1 = "SELECT `id` FROM `" + MySQLManager.getTablePrefix() + "player` WHERE `player` = ? AND `kitname` = ? LIMIT 1";
			ps = connection.prepareStatement(s1);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			rs = ps.executeQuery();
			if (rs.next()) id = rs.getInt("id");
			// 确保释放上一个 rs
			MySQLManager.close(rs, ps, null);
			rs = null;
			ps = null;

			// 锁定行
			String s2 = "SELECT `data` FROM `" + MySQLManager.getTablePrefix() + "player` WHERE `id` = ? FOR UPDATE";
			ps = connection.prepareStatement(s2);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			String data = "true";
			if (rs.next()) {
				String rsData = rs.getString("data");
				data = rsData != null ? rsData : "false";
			}

			if ("false".equals(data)) {
				String s3 = SQLCommand.UPDATE_DATA_DATA.format(MySQLManager.getTablePrefix());
				ps = connection.prepareStatement(s3);
				ps.setString(1, value);
				ps.setString(2, playername);
				ps.setString(3, kitname);
				MySQLManager.get().doCommand(ps);
			}

			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
			try {
				connection.rollback();
				connection.setAutoCommit(true);
			} catch (SQLException ignored) {}
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
	}
}
