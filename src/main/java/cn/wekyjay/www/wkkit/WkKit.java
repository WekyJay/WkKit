package cn.wekyjay.www.wkkit;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import cn.wekyjay.www.wkkit.command.KitInfo;
import cn.wekyjay.www.wkkit.command.TabCompleter;
import cn.wekyjay.www.wkkit.config.ConfigManager;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.data.cdkdata.CdkData;
import cn.wekyjay.www.wkkit.data.cdkdata.CdkData_MySQL;
import cn.wekyjay.www.wkkit.data.cdkdata.CdkData_Yaml;
import cn.wekyjay.www.wkkit.data.playerdata.PlayerData;
import cn.wekyjay.www.wkkit.data.playerdata.PlayerData_MySQL;
import cn.wekyjay.www.wkkit.data.playerdata.PlayerData_Yaml;
import cn.wekyjay.www.wkkit.edit.EditGUI;
import cn.wekyjay.www.wkkit.edit.EditKit;
import cn.wekyjay.www.wkkit.event.DropKitEvent;
import cn.wekyjay.www.wkkit.event.KitMailEvent;
import cn.wekyjay.www.wkkit.event.KitMenuEvent;
import cn.wekyjay.www.wkkit.event.NewComerEvent;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.kitcode.CodeManager;
import cn.wekyjay.www.wkkit.menu.MenuManager;
import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import cn.wekyjay.www.wkkit.other.Metrics;
import cn.wekyjay.www.wkkit.other.WkKitPAPI;
import cn.wekyjay.www.wkkit.tool.ChackPluginVersion;
import cn.wekyjay.www.wkkit.tool.KitRefresh;

public class WkKit extends JavaPlugin {
	
	/*声明静态属性*/
	public static File playerConfigFile;
	public static File playerMailConfigFile;
	public static File CDKConfigFile;
	public static File msgConfigFile;
	public static File menuFile;
	public static File kitFile;
	public static File langFile;
	public static FileConfiguration playerConfig;
	public static FileConfiguration playerMailConfig;
	public static FileConfiguration CDKConfig;
	private static PlayerData playerdata = null;
	private static CdkData cdkdata = null;
	public static int refreshCount = 0;
	
	// 初始化数据
	public static PlayerData getPlayerData() {
		if(WkKit.wkkit.getConfig().getString("MySQL.Enable").equalsIgnoreCase("true")) {
			return playerdata == null ? new PlayerData_MySQL() : playerdata;
		}
		return playerdata == null ? new PlayerData_Yaml() : playerdata;
	}
	public static CdkData getCdkData() {
		if(WkKit.wkkit.getConfig().getString("MySQL.Enable").equalsIgnoreCase("true")) {
			return cdkdata == null ? new CdkData_MySQL() : cdkdata;
		}
		return cdkdata == null ? new CdkData_Yaml() : cdkdata;
	}
	

	/*创建一个可调用本类的方法的Getter*/
	public static WkKit wkkit;
	public static WkKit getWkKit() {
		return wkkit;
	}
	
	
	/*插件启动标识*/
    @Override 
    public void onEnable() {
		wkkit = this;//为Getter赋值为本类
		
		// 启动数据库
		if(WkKit.wkkit.getConfig().getString("MySQL.Enable").equalsIgnoreCase("true")) {
			new BukkitRunnable() {
				@Override
				public void run() {
					MySQLManager.get().enableMySQL();
				}
			}.runTaskAsynchronously(this);
		}
		
		File file = new File(getDataFolder(),"config.yml");
		if (!(file.exists())) { //判断file的地址是否存在该文件否则创建一个
			saveDefaultConfig();
		}
		reloadConfig(); //初始化Config
		
		//创建文件
		playerConfigFile = new File(getDataFolder(),"player.yml");
		playerMailConfigFile = new File(getDataFolder(),"maildata.yml");
		CDKConfigFile = new File(getDataFolder(),"CDK.yml");

		
		//检测是否存在不存在就创建一个
		if(!playerConfigFile.exists()) {saveResource("player.yml", false);}
		if(!playerMailConfigFile.exists()) {saveResource("maildata.yml", false);}
		if(!CDKConfigFile.exists()) {saveResource("CDK.yml", false);}
		
		//读取文件
		playerConfig = YamlConfiguration.loadConfiguration(WkKit.playerConfigFile);
		playerMailConfig = YamlConfiguration.loadConfiguration(WkKit.playerMailConfigFile);
		CDKConfig = YamlConfiguration.loadConfiguration(WkKit.CDKConfigFile);
		
		
		// 创建配置文件
		String menupath = getDataFolder().getAbsolutePath() + File.separatorChar + "Menus";
		String kitpath = getDataFolder().getAbsolutePath() + File.separatorChar + "Kits";
		String langpath = getDataFolder().getAbsolutePath() + File.separatorChar + "Language";
		menuFile = new File(menupath);
		kitFile = new File(kitpath);
		langFile = new File(langpath);
		
		if(!menuFile.exists()) {
			menuFile.mkdir();
			saveResource("Menus" + File.separatorChar + "ExampleMenu.yml", false);
		}
		if(!langFile.exists()) {
			saveResource("Language" + File.separatorChar + "zh_CN.yml", false);
			saveResource("Language" + File.separatorChar + "zh_HK.yml", false);
			saveResource("Language" + File.separatorChar + "zh_TW.yml", false);
			saveResource("Language" + File.separatorChar + "en_US.yml", false);
		}
		if(!kitFile.exists()) {
			kitFile.mkdir();
			saveResource("Kits" + File.separatorChar + "ExampleKits.yml", false);
		}
				
		ConfigManager.loadConfig();
		LangConfigLoader.loadConfig();
		
		
		//指令&监听注册
		Bukkit.getPluginCommand("wkkit").setExecutor(new KitCommand());//注册指令
		Bukkit.getPluginCommand("wkkit").setTabCompleter(new TabCompleter());//补全指令
		Bukkit.getPluginManager().registerEvents(new KitMailEvent(),this);
		Bukkit.getPluginManager().registerEvents(new DropKitEvent(),this);
		Bukkit.getPluginManager().registerEvents(new NewComerEvent(),this);
		Bukkit.getPluginManager().registerEvents(new KitInfo(),this);
		Bukkit.getPluginManager().registerEvents(new KitMenuEvent(),this);
		Bukkit.getPluginManager().registerEvents(new  ChackPluginVersion(),this);
		Bukkit.getPluginManager().registerEvents(EditGUI.getEditGUI(),this);
		Bukkit.getPluginManager().registerEvents(new EditKit(),this);

		//插件检测
        if (Bukkit.getPluginManager().getPlugin("NBTAPI") != null) {
        	getLogger().info("");
        	getLogger().info(" __ __ __   ___   ___   ___   ___   ________  _________  ");
        	getLogger().info("/_//_//_/\\ /___/\\/__/\\ /___/\\/__/\\ /_______/\\/________/\\ ");
        	getLogger().info("\\:\\\\:\\\\:\\ \\\\::.\\ \\\\ \\ \\\\::.\\ \\\\ \\ \\\\__.::._\\/\\__.::.__\\/ ");
        	getLogger().info(" \\:\\\\:\\\\:\\ \\\\:: \\/_) \\ \\\\:: \\/_) \\ \\  \\::\\ \\    \\::\\ \\   ");
        	getLogger().info("  \\:\\\\:\\\\:\\ \\\\:. __  ( ( \\:. __  ( (  _\\::\\ \\__  \\::\\ \\  ");
        	getLogger().info("   \\:\\\\:\\\\:\\ \\\\: \\ )  \\ \\ \\: \\ )  \\ \\/__\\::\\__/\\  \\::\\ \\ ");
        	getLogger().info("    \\_______\\/ \\__\\/\\__\\/  \\__\\/\\__\\/\\________\\/   \\__\\/ ");
        	getLogger().info("");
        	getLogger().info("Version: "+ getDescription().getVersion() + " | Author: WekyJay | QQ Group: 945144520");
        	getLogger().info("§a特别鸣谢：§eBiulay Gentry §7(排名不分先后)");
        } else {
            getLogger().warning(LangConfigLoader.getString("PLUGIN_NONBTAPI"));
            Bukkit.getPluginManager().disablePlugin(this);
        }
        
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new WkKitPAPI(this).register();
        }
        
        //bStats
        int pluginId = 13579;
        new Metrics(this, pluginId);
        
        //Check Version
        if(getConfig().contains("Setting.ChackUpdate") && getConfig().getBoolean("Setting.ChackUpdate")) {
            Thread t = new Thread(new ChackPluginVersion());
            t.start();
        }
        
        
        // 启动自动刷新礼包检测
        new KitRefresh().enable();         
        this.getLogger().info(LangConfigLoader.getString("REFRESH_NUM") + WkKit.refreshCount);
        this.getLogger().info(LangConfigLoader.getString("KIT_NUM") + Kit.getKits().size());
        this.getLogger().info(LangConfigLoader.getString("MENU_NUM") + MenuManager.getInvs().size());
        
        // 检查礼包密码
        CodeManager.checkPassWord();
    }
    
    /*插件关闭标识*/
    @Override
    public void onDisable() {
    	// 保存关服时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		getConfig().getConfigurationSection("Default").set("ShutDate", sdf.format(new Date()));
		try {
			getConfig().save(new File(getDataFolder(),"config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

    	// 关闭数据库
		if(WkKit.wkkit.getConfig().getString("MySQL.Enable").equalsIgnoreCase("true")) {
			MySQLManager.get().shutdown(); 
		}
		this.getLogger().info(LangConfigLoader.getString("PLUGIN_UNINSTALL"));
    }

}
