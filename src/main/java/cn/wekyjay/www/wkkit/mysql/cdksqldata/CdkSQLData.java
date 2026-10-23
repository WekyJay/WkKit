package cn.wekyjay.www.wkkit.mysql.cdksqldata;

import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.tool.MessageManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CdkSQLData {
	public static void createTable(){
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		String cmd = SQLCommand.CREATE_TABLE.format(MySQLManager.getTablePrefix());
		try {
			ps = connection.prepareStatement(cmd);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			MessageManager.info("§cCDK数据表创建失败");
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * 插入数据 — 修复：去掉硬编码 id=0
	 */
	public void insertData(String CDK, String kits, String date, String mark) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.ADD_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, CDK);
			ps.setString(2, kits);
			ps.setString(3, date);
			ps.setString(4, "Available");
			ps.setString(5, mark);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	public void deleteData(String CDK) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.DELETE_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, CDK);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * 按 mark 批量删除所有 CDK — 修复：替代原来先查所有 cdk 再循环 delete
	 */
	public void deleteDataByMark(String mark) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.DELETE_DATA_BY_MARK.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, mark);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * 按 mark 查所有 CDK
	 */
	public List<String> findCDK(String mark) {
		List<String> list = new ArrayList<String>();
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_MARK.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, mark);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(rs.getString("cdk"));
			}
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
		return list;
	}

	/**
	 * 一次性查出某 CDK 的所有信息 — 修复：替代原来 4 次独立查询
	 * 返回 {kits, date, status, mark} 数组，顺序固定
	 */
	public String[] findCdkInfo(String cdk) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_CDK_INFO.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, cdk);
			rs = ps.executeQuery();
			if (rs.next()) {
				return new String[]{
						rs.getString("kits"),
						rs.getString("date"),
						rs.getString("status"),
						rs.getString("mark")
				};
			}
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
		return null;
	}

	/**
	 * 以下四个方法现在都委托给 findCdkInfo — 消除 4 次独立查询风暴
	 */
	public String findKits(String cdk) {
		String[] info = findCdkInfo(cdk);
		return info != null ? info[0] : null;
	}

	public String findDate(String cdk) {
		String[] info = findCdkInfo(cdk);
		return info != null ? info[1] : null;
	}

	public String findStatus(String cdk) {
		String[] info = findCdkInfo(cdk);
		return info != null ? info[2] : null;
	}

	public String findMark(String cdk) {
		String[] info = findCdkInfo(cdk);
		return info != null ? info[3] : null;
	}

	public Boolean containMark(String mark) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_MARK_EXISTS.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, mark);
			rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
		return false;
	}

	public Boolean containCDK(String CDK) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_CDK_EXISTS.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, CDK);
			rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(rs,ps,connection);
		}
		return false;
	}

	public void update_Status(String playername, String CDK) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.UPDATE_STATUS_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, CDK);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	public void update_Mark(String newmark, String mark) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.UPDATE_MARK_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, newmark);
			ps.setString(2, mark);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		} finally {
			MySQLManager.close(null,ps,connection);
		}
	}
}
