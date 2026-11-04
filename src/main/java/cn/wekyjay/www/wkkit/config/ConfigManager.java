package cn.wekyjay.www.wkkit.config;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.data.YamlSaveQueue;
import cn.wekyjay.www.wkkit.kit.KitGroupManager;
import cn.wekyjay.www.wkkit.menu.MenuManager;
import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigManager {
	private static KitConfigLoader kitconfig = null;
	private static KitGroupManager kitGroupManager = null;
	
	// 获得礼包配置
	public static KitConfigLoader getKitconfig() {
		return kitconfig == null ? kitconfig = new KitConfigLoader() : kitconfig;
	}
	
	// 获得菜单配置
	public static KitGroupManager getKitGroupManager() {
		return kitGroupManager;
	}
	
	public static void loadConfig() {
		getKitconfig().loadConfig();
		kitGroupManager = new KitGroupManager();
		MenuConfigLoader.loadConfig();
		
	}
	
	public static void reloadPlugin() {
		YamlSaveQueue.flush();
		WkKit.getWkKit().reloadConfig();
		boolean databaseEnabled = MySQLManager.isDatabaseConfigured();
		if (MySQLManager.get().isReady()) MySQLManager.get().shutdown();
		if (databaseEnabled) MySQLManager.get().enableDatabase();
		WkKit.playerConfig = YamlConfiguration.loadConfiguration(WkKit.playerConfigFile);
		WkKit.playerMailConfig = YamlConfiguration.loadConfiguration(WkKit.playerMailConfigFile);
		LangConfigLoader.reloadConfig();
		ConfigManager.reloadKit();
		ConfigManager.reloadMenu();
    	WkKit.getWkKit().saveConfig();
    	WkKit.getWkKit().enableAntiShutDown(); //防崩服记录线程启用
	}
	
	/**
	 * 重载菜单配置
	 */
	public static void reloadMenu() {
		MenuManager.getInvs().clear();
		MenuManager.getMenus().clear();
		MenuConfigLoader.filelist.clear();
		MenuConfigLoader.map.clear();
		MenuConfigLoader.loadConfig();
	}
	/**
	 * 重载礼包配置
	 */
	public static void reloadKit() {
		// 保存重置时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	WkKit.getWkKit().getConfig().set("Default.ShutDate", sdf.format(new Date()));
    	WkKit.getWkKit().saveConfig();
    	kitconfig = new KitConfigLoader(); // 重置配置
    	getKitconfig().loadConfig(); // 加载配置
	}

}
