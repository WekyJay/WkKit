package cn.wekyjay.www.wkkit.tool;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.api.PlayersKitRefreshEvent;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.api.PlayersReceiveKitEvent;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.api.ReceiveType;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KitCache implements Listener{
	private static KitCache cache = null;
	private static List<String> cacheList = null;
	private final static String CACHEPATH = WkKit.getWkKit().getDataFolder().getAbsolutePath() + File.separatorChar + "Caches";

    // 动态获取静态的Cache对象
	public static KitCache getCache() {
		return cache == null? cache = new KitCache() : cache;
	}
	
	public KitCache() {
        boolean isEnable = WkKit.getWkKit().getConfig().getBoolean("Cache.Enable");
		if(isEnable) {
			WkKit.getWkKit().getLogger().info(LangConfigLoader.getString("LOG_MANAGE_ENABLE"));
			Bukkit.getPluginManager().registerEvents(this, WkKit.getWkKit());
			File file = new File(CACHEPATH);
			// 不存在该路径则创建一个
			if(!file.exists()) file.mkdir();
			// 初始化cacheYaml
			cacheList = new ArrayList<>();
		}else{
			WkKit.getWkKit().getLogger().info(LangConfigLoader.getString("LOG_MANAGE_DISABLE"));
		}

	}
	
	public void reloadCache() {
		cache = new KitCache();
	}
	/**
	 * 保存当前存储的日志
	 */
	public void saveCache() throws IOException {
		if(cacheList.size() <= 0) return; // 如果没有数据则不进行操作
		String filename = "[Cache]" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
		File file = new File(CACHEPATH,filename + ".log");
		if(!file.exists()) file.createNewFile();
		
		try {
			RandomAccessFile ra;
			ra = new RandomAccessFile(file,"rw");
			ra.seek(ra.length());//代表从文件的结尾写不会覆盖，也就是文件的追加
			for(String str : cacheList) {
				ra.write(str.getBytes("UTF-8"));
				ra.write("\n".getBytes("UTF-8"));
			}
			ra.close();
			String msg = "Log caches has been saved in Caches\\"+filename+".log";
			WkKit.getWkKit().getLogger().info(msg);
		} catch (FileNotFoundException e) {
			ExceptionHandler.handle("未知操作", e);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
	}
	
	@EventHandler
	public void onPlayerRececiveKit(PlayersReceiveKitEvent e) {
		ReceiveType type = e.getType();
		String str = "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] ";
		str += "[" + e.getEventName() + "] ";
		str += LangConfigLoader.getString("PLAYER_RECEIVE_KIT").replaceAll("[{]player[}]","\"" + e.getPlayername() + "\"")
				.replaceAll("[{]type[}]","\"" + type.toString() + "\"")
				.replaceAll("[{]kit[}]", "\"" + e.getKit().getKitname() + "\"");
		cacheList.add(str);
	}

	@EventHandler
	public void onPlayerKitRefresh(PlayersKitRefreshEvent e) {
		String kitName = e.getKit().getKitname();
		String playerName = e.getOfflinePlayer().getName();
		String str = "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] ";
		str += "[" + e.getEventName() + "] ";
		str += LangConfigLoader.getString("PLAYER_KIT_REFRESH").replaceAll("[{]player[}]","\"" + e.getOfflinePlayer().getName() + "\"")
				.replaceAll("[{]kit[}]", "\"" + e.getKit().getKitname() + "\"");
		cacheList.add(str);
	}
}
