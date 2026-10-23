package cn.wekyjay.www.wkkit.data.playerdata;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.mysql.mailsqldata.MailSQLData;
import cn.wekyjay.www.wkkit.mysql.playersqldata.PlayerSQLData;

import java.util.List;

public class PlayerData_MySQL implements PlayerData{

	private final PlayerSQLData playerSQL = new PlayerSQLData();
	private final MailSQLData mailSQL = new MailSQLData();

	@Override
	public void setKitToFile(String playername, String kitname, String data, int time) {
		playerSQL.upsertData(kitname, playername, data, time);
	}

	@Override
	public List<String> getKits(String playername) {
		return playerSQL.findKitName(playername);
	}

	@Override
	public String getKitData(String playername, String kitname) {
		return playerSQL.findKitData(playername, kitname);
	}

	@Override
	public Integer getKitTime(String playername, String kitname) {
		return playerSQL.findKitTime(playername, kitname);
	}

	@Override
	public void delKitToFile(String playername, String kitname) {
		playerSQL.deleteData(kitname, playername);
	}

	/**
	 * 修复：直接 upsert，不再先查 exist 再 INSERT/UPDATE（原来两次查询变一次）
	 */
	@Override
	public void setKitData(String playername, String kitname, String value) {
		int time;
		Integer kitTimes = Kit.getKit(kitname).getTimes();
		if(kitTimes == null) time = -1;
		else time = kitTimes;
		playerSQL.upsertData(kitname, playername, value, time);
	}

	public void setKitDataOfLock(String playername, String kitname, String value){
		playerSQL.update_Data_Data_Lock(playername, kitname, value);
	}

	/**
	 * 修复：直接 upsert，不再先查 exist 再 INSERT/UPDATE
	 */
	@Override
	public void setKitTime(String playername, String kitname, int value) {
		playerSQL.update_Time_Data(playername, kitname, value);
	}

	/**
	 * 修复：用 SELECT 1 ... LIMIT 1 直接查数据库，
	 * 替代原来 getKits() 拉全量数据再 .contains() 的 O(N²) 反模式
	 */
	@Override
	public Boolean contain_Kit(String playername, String kitname) {
		return playerSQL.findPlayerKitExists(playername, kitname);
	}

	@Override
	public Boolean contain_Kit(String playername) {
		return playerSQL.findPlayer(playername);
	}

	@Override
	public void setMailToFile(String playername, String kitname, int num) {
		mailSQL.upsertData(playername, kitname, num);
	}

	@Override
	public void delMailToFile(String playername, String kitname) {
		mailSQL.deleteData(playername, kitname);
	}

	/**
	 * 修复：直接 upsert
	 */
	@Override
	public void setMailNum(String playername, String kitname, int num) {
		mailSQL.upsertData(playername, kitname, num);
	}

	@Override
	public List<String> getMailKits(String playername) {
		return mailSQL.findKitName(playername);
	}

	/**
	 * 修复：用 SELECT 1 ... LIMIT 1 直接查数据库
	 */
	@Override
	public Boolean contain_Mail(String playername, String kitname) {
		return mailSQL.findPlayerKitExists(playername, kitname);
	}

	@Override
	public Boolean contain_Mail(String playername) {
		return mailSQL.findPlayer(playername);
	}

	@Override
	public Integer getMailKitNum(String playername, String kitname) {
		return mailSQL.findKitNum(playername, kitname);
	}
}
