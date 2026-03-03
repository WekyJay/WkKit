package cn.wekyjay.www.wkkit.mysql.mailsqldata;

import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
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

	public void insertData(String playername, String kitname, int num) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.ADD_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setInt(1, 0);  // id
			ps.setString(2, playername);
			ps.setString(3, kitname);
			ps.setInt(4, num);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(null,ps,connection);
		}
	}
	public void deleteData(String playername,String kitname) {
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
	public List<String> findKitName(String playername) {
		List<String> list = new ArrayList<String>();
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			rs = ps.executeQuery();
			while (rs.next())
			{
				list.add(rs.getString("kitname"));
			}
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return list;
	}
	public Boolean findPlayer(String playername) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			rs = ps.executeQuery();
			while (rs.next())
			{
				if(rs.getString("player").equals(playername)) {
					return true;
				}
			}
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return false;
	}
	public int findKitNum(String playername,String kitname) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String s = SQLCommand.SELECT_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setString(1, playername);
			rs = ps.executeQuery();
			while (rs.next())
			{
				if(rs.getString("kitname").equals(kitname)) {
					return rs.getInt("num");
				}
			}
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return 0;
	}

	public void update_Num(String playername,String kitname, int value) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.UPDATE_NUM_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setInt(1, value);
			ps.setString(2, playername);
			ps.setString(3, kitname);
			MySQLManager.get().doCommand(ps);
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(null,ps,connection);
		}
	}
}
