package cn.wekyjay.www.wkkit.data.playerdata;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlayerData_Yaml implements PlayerData{

	@Override
	public void setKitToFile(String playername, String kitname, String data, int time) {
			WkKit.playerConfig.set(playername + "." + kitname + ".data", data);
			WkKit.playerConfig.set(playername + "." + kitname + ".time", time);
			try {
				WkKit.playerConfig.save(WkKit.playerConfigFile);
				WkKit.playerConfig = YamlConfiguration.loadConfiguration(WkKit.playerConfigFile);
			} catch (IOException e) {
				ExceptionHandler.handle("未知操作", e);
			}
	}

	@Override
	public List<String> getKits(String playername) {
		List<String> list = null;
		ConfigurationSection cs = WkKit.playerConfig.getConfigurationSection(playername);
		try {
			Set<String> kl = cs.getKeys(false);
			list = new ArrayList<>(kl);
		}catch(NullPointerException e) {}
		return list;
	}

	@Override
	public String getKitData(String playername, String kitname) {
		if(WkKit.playerConfig.contains(playername)) {
			return WkKit.playerConfig.getString(playername + "." + kitname + ".data"); 
		}
		return null;
	}

	@Override
	public Integer getKitTime(String playername, String kitname) {
		if(WkKit.playerConfig.contains(playername) && WkKit.playerConfig.contains(playername + "." + kitname + ".time")) {
			return WkKit.playerConfig.getInt(playername + "." + kitname + ".time"); 
		}
		return null;
	}

	@Override
	public void delKitToFile(String playername, String kitname) {
		WkKit.playerConfig.set(playername + "." + kitname, null);
		try {
			WkKit.playerConfig.save(WkKit.playerConfigFile);
			WkKit.playerConfig = YamlConfiguration.loadConfiguration(WkKit.playerConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
		
	}

	@Override
	public void setKitData(String playername, String kitname, String value) {
		WkKit.playerConfig.set(playername + "." + kitname + ".data", value);
		try {
			WkKit.playerConfig.save(WkKit.playerConfigFile);
			WkKit.playerConfig = YamlConfiguration.loadConfiguration(WkKit.playerConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
		
	}

	@Override
	public void setKitTime(String playername, String kitname, int value) {
		WkKit.playerConfig.set(playername + "." + kitname + ".time", value);
		try {
			WkKit.playerConfig.save(WkKit.playerConfigFile);
			WkKit.playerConfig = YamlConfiguration.loadConfiguration(WkKit.playerConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
	}

	@Override
	public Boolean contain_Kit(String playername, String kitname) {
		return WkKit.playerConfig.contains(playername + "." + kitname);
	}

	@Override
	public Boolean contain_Kit(String playername) {
		return WkKit.playerConfig.contains(playername);
	}

	@Override
	public void setMailToFile(String playername, String kitname, int num) {
		WkKit.playerMailConfig.set(playername + "." + kitname, num);
		try {
			WkKit.playerMailConfig.save(WkKit.playerMailConfigFile);
			WkKit.playerMailConfig = YamlConfiguration.loadConfiguration(WkKit.playerMailConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
	}

	@Override
	public void delMailToFile(String playername, String kitname) {
		WkKit.playerMailConfig.set(playername + "." + kitname, null);
		try {
			WkKit.playerMailConfig.save(WkKit.playerMailConfigFile);
			WkKit.playerMailConfig = YamlConfiguration.loadConfiguration(WkKit.playerMailConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
		
	}

	@Override
	public void setMailNum(String playername, String kitname, int num) {
		WkKit.playerMailConfig.set(playername + "." + kitname, num);
		try {
			WkKit.playerMailConfig.save(WkKit.playerMailConfigFile);
			WkKit.playerMailConfig = YamlConfiguration.loadConfiguration(WkKit.playerMailConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
		
	}

	@Override
	public List<String> getMailKits(String playername) {
		if(WkKit.playerMailConfig.contains(playername)){
			List<String> list = new ArrayList<>(WkKit.playerMailConfig.getConfigurationSection(playername).getKeys(false));
			return list;
		}else {
			return null;
		}
	}

	@Override
	public Boolean contain_Mail(String playername, String kitname) {
		return WkKit.playerMailConfig.contains(playername + "." + kitname);
	}

	@Override
	public Boolean contain_Mail(String playername) {

		return WkKit.playerMailConfig.contains(playername);
	}

	@Override
	public Integer getMailKitNum(String playername, String kitname) {
		return WkKit.playerMailConfig.getInt(playername + "." + kitname);
	}


}
