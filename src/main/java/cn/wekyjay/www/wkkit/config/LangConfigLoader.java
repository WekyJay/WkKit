package cn.wekyjay.www.wkkit.config;

import cn.wekyjay.www.wkkit.WkKit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class LangConfigLoader {
	static File langFile = null;
	static FileConfiguration langConfig = null;
	
	/**
	 * 加载文件夹中的配置文件
	 */
	public static void loadConfig() {
		String path = WkKit.getWkKit().getDataFolder().getAbsolutePath() + File.separator + "Language";
		String lang = WkKit.getWkKit().getConfig().getString("Setting.Language");
		if(lang.equals("none")) {
			Locale locale = Locale.getDefault();
			String language = locale.getLanguage();
			String country = locale.getCountry();
			if ("zh".equals(language) && "CN".equals(country)) lang = "zh_CN";
			else if ("zh".equals(language) && "TW".equals(country)) lang = "zh_TW";
			else if ("zh".equals(language) && "HK".equals(country)) lang = "zh_HK";
			else if ("ja".equals(language) && "JP".equals(country)) lang = "ja_JP";
			else if ("ko".equals(language) && "KR".equals(country)) lang = "ko_KR";
			else lang = "en_US";
			WkKit.getWkKit().getConfig().set("Setting.Language", lang);
			WkKit.getWkKit().saveConfig();
		}
		File[] ff = new File(path).listFiles();
		boolean found = false;
		 for(File fs : ff) {
			 if(fs.isFile() && fs.getName().equals(lang + ".yml")) {
				 langFile = fs;
				 langConfig = YamlConfiguration.loadConfiguration(fs);
				found = true;
				break;
			}
		}
		// 如果没找到，降级为en_US
		if(!found) {
			for(File fs : ff) {
				if(fs.isFile() && fs.getName().equals("en_US.yml")) {
					langFile = fs;
					langConfig = YamlConfiguration.loadConfiguration(fs);
					break;
				}
			 }
		 }
	}
	/**
	 * 重载配置
	 */
	public static void reloadConfig() {
		LangConfigLoader.loadConfig();
	}
	
	// 封装BukkitAPI的方法
	
	public static Boolean contains(String path) {
		if (langConfig == null) return false;
		return langConfig.contains(path);
	}
	
	public static String getString(String path) {
		if (langConfig == null) return "Lang missing: " + path;
		if(LangConfigLoader.contains(path)) {
			return ChatColor.translateAlternateColorCodes('&',langConfig.getString(path));
		}
		return null;
	}
	
	public static String getStringWithPrefix(String path,ChatColor color) {
		if(LangConfigLoader.contains(path)) {
			if(color == null) {
				return getString("Prefix") + " " + ChatColor.translateAlternateColorCodes('&',langConfig.getString(path));
			}
			return color + getString("Prefix") + " " + ChatColor.translateAlternateColorCodes('&',langConfig.getString(path));
			
		}
		return ChatColor.GRAY + getString("Prefix");
	}
	public static List<String> getStringList(String path) {
		return langConfig.getStringList(path);
		
	}
	public static int getInt(String path) {
		return langConfig.getInt(path);
		
	}
	public static long getLong(String path) {
		return langConfig.getLong(path);
		
	}
	public static Boolean getBoolean(String path) {
		return langConfig.getBoolean(path);
		
	}
}
