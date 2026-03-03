package cn.wekyjay.www.wkkit.mysql.playersqldata;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.tool.MessageManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;

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
	 * 向数据库添加数据
	 * @param kitname
	 * @param data
	 * @param time
	 */
	public void insertData(String kitname, String playername,String data, int time) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		try {
			String s = SQLCommand.ADD_DATA.format(MySQLManager.getTablePrefix());
			ps = connection.prepareStatement(s);
			ps.setInt(1, 0);
			ps.setString(2, playername);
			ps.setString(3, kitname);
			ps.setString(4, data);
			ps.setInt(5, time);
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
			MySQLManager.close(rs,ps,connection);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
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
			MySQLManager.close(rs,ps,connection);
		} catch (SQLException e) {
		}finally {
			MySQLManager.close(rs,ps,connection);
		}
		return false;
	}
	public String findKitData(String playername,String kitname) {
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
					return rs.getString("data");
				}
			}
			MySQLManager.close(rs,ps,connection);
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
		return null;
	}
	public Integer findKitTime(String playername,String kitname) {
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
					if(rs.getString("time") == null) return null;
					else return rs.getInt("time");
				}
			}
			MySQLManager.close(rs,ps,connection);
		} catch (SQLException e) {
			ExceptionHandler.handle("未知操作", e);
		}finally {
			MySQLManager.close(rs,ps,connection);
		}
		return null;
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
			MySQLManager.close(null,ps,connection);
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(null,ps,connection);
		}
	}

	/**
	 * 更新礼包Data数据
	 * @param playername
	 * @param kitname
	 * @param value
	 */
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
			MySQLManager.close(null,ps,connection);
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(null,ps,connection);
		}
	}
	/**
	 * （加入排他锁）更新礼包Data数据
	 * @param playername
	 * @param kitname
	 * @param value
	 */
	public void update_Data_Data_Lock(String playername,String kitname, String value) {
		Connection connection = MySQLManager.get().getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// 设置不自动提交事务
			connection.setAutoCommit(false);
			// 语句执行
			int id = 0;
			String data = "true";
			String s1 = "SELECT `id` FROM `" + MySQLManager.getTablePrefix() + "player` WHERE `player` = ? AND `kitname` = ?"; // 搜索ID
			ps = connection.prepareStatement(s1);
			ps.setString(1, playername);
			ps.setString(2, kitname);
			rs = ps.executeQuery();
			while (rs.next()) if(rs.getString("id") != null) id = rs.getInt("id");


			String s2 = "SELECT * FROM `" + MySQLManager.getTablePrefix() + "player` WHERE `id` = ? for update"; // 锁定行
			ps = connection.prepareStatement(s2);
			ps.setInt(1,id);
			try {
				rs = ps.executeQuery();
				while (rs.next()){
					data = rs.getString("data") != null?rs.getString("data"):"false";
				}

			} catch(SQLIntegrityConstraintViolationException sql) {
				WkKit.getWkKit().getLogger().warning(ChatColor.YELLOW + LangConfigLoader.getString("MYSQL_DATEEXISTS"));
			}catch (SQLException e) {
				ExceptionHandler.handle("未知操作", e);
			}


			if (data.equals("false")){
				String s3 = SQLCommand.UPDATE_DATA_DATA.format(MySQLManager.getTablePrefix()); // 修改数据
				ps = connection.prepareStatement(s3);
				ps.setString(1, value);
				ps.setString(2, playername);
				ps.setString(3, kitname);
				MySQLManager.get().doCommand(ps);
			}



			// 提交数据(自动解锁行)
			connection.commit();
			// 还原数据并归还
			connection.setAutoCommit(true);
			MySQLManager.close(rs,ps,connection);
		} catch (SQLException e) {}
		finally {
			MySQLManager.close(rs,ps,connection);
		}
	}
}
