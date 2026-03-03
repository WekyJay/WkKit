package cn.wekyjay.www.wkkit.mysql.cdksqldata;

import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.tool.MessageManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;

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
	// 插入数据
	public void insertData(String CDK, String kits, String date, String mark) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.ADD_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setInt(1, 0);  // id
			ps.setString(2, CDK);
			ps.setString(3, kits);
			ps.setString(4, date);
			ps.setString(5, "Available");
			ps.setString(6, mark);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}
	// 删除数据
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
			while (rs.next())
			{
				list.add(rs.getString("cdk"));
			}
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return list;
	}
	public String findKits(String cdk) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_CDK.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, cdk);
			rs = ps.executeQuery();
			while (rs.next())
			{
				if(rs.getString("cdk").equals(cdk))return rs.getString("kits");
			}
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return null;
	}
	public String findDate(String cdk) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_CDK.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, cdk);
			rs = ps.executeQuery();
			while (rs.next())
			{
				if(rs.getString("cdk").equals(cdk))return rs.getString("date");
			}
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return null;
	}
	public String findStatus(String cdk) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_CDK.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, cdk);
			rs = ps.executeQuery();
			while (rs.next())
			{
				if(rs.getString("cdk").equals(cdk))return rs.getString("status");
			}
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return null;
	}
	public String findMark(String cdk) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_CDK.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, cdk);
			rs = ps.executeQuery();
			while (rs.next())
			{
				if(rs.getString("cdk").equals(cdk))return rs.getString("mark");
			}
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return null;
	}
	/**
	 * 判断指定mark是否存在
	 * @param mark
	 * @return
	 */
	public Boolean containMark(String mark) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_MARK.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, mark);
			rs = ps.executeQuery();
			if(rs.next())return true;
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return false;
	}
	public Boolean containCDK(String CDK) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_CDK.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, CDK);
			rs = ps.executeQuery();
			if(rs.next())return true;
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return false;
	}
	
	/**
	 * 更新status
	 * @param playername
	 * @param CDK
	 */
	public void update_Status(String playername,String CDK) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.UPDATE_STATUS_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			ps.setString(2, CDK);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(null,ps,connection);
		}
	}
	/**
	 * 更新mark
	 * @param newmark
	 * @param mark
	 */
	public void update_Mark(String newmark,String mark) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.UPDATE_MARK_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, newmark);
			ps.setString(2, mark);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(null,ps,connection);
		}
	}
}
