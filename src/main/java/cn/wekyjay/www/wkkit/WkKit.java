package cn.wekyjay.www.wkkit;


import cn.handyplus.lib.adapter.HandySchedulerUtil;
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
import cn.wekyjay.www.wkkit.hook.Metrics;
import cn.wekyjay.www.wkkit.hook.MythicMobsHooker;
import cn.wekyjay.www.wkkit.hook.PapiHooker;
import cn.wekyjay.www.wkkit.hook.VaultHooker;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.kitcode.CodeManager;
import cn.wekyjay.www.wkkit.listeners.*;
import cn.wekyjay.www.wkkit.menu.MenuManager;
import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import cn.wekyjay.www.wkkit.tool.*;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

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
    private static WkKit wkkit;
    public static WkKit getWkKit() {
        return wkkit;
    }
    // 别名方法，兼容 v2 重构代码
    public static WkKit getInstance() {
        return wkkit;
    }


    /*插件启动标识*/
    @Override
    public void onEnable() {
        wkkit = this;//为Getter赋值为本类
        // 初始化FoliaLib适配
        HandySchedulerUtil.init(this);
        saveDefaultConfig();//初始化Config文件

        reloadConfig();

        //创建文件
        playerConfigFile = new File(getDataFolder(),"player.yml");
        playerMailConfigFile = new File(getDataFolder(),"maildata.yml");
        CDKConfigFile = new File(getDataFolder(),"CDK.yml");


        //检测是否存在不存在就创建一个
        if(!playerConfigFile.exists()) {saveResource("player.yml", false);}
        if(!playerMailConfigFile.exists()) {saveResource("maildata.yml", false);}
        if(!CDKConfigFile.exists()) {saveResource("CDK.yml", false);}

        //加载文件
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

        // 检查文件节点
        ChackFiles cf = new ChackFiles();
        cf.chack(this.getDataFolder());

        // 读取文件
        ConfigManager.loadConfig();
        LangConfigLoader.loadConfig();


        //指令&监听注册
        Bukkit.getPluginCommand("wkkit").setExecutor(new KitCommand());//注册指令
        Bukkit.getPluginCommand("wkkit").setTabCompleter(new TabCompleter());//补全指令
        Bukkit.getPluginManager().registerEvents(new KitMailListener(),this);
        Bukkit.getPluginManager().registerEvents(new DropKitListener(),this);
        Bukkit.getPluginManager().registerEvents(new NewComerListener(),this);
        Bukkit.getPluginManager().registerEvents(new KitReminderListener(),this);
        Bukkit.getPluginManager().registerEvents(new KitInfo(),this);
        Bukkit.getPluginManager().registerEvents(new KitMenuListener(),this);
        Bukkit.getPluginManager().registerEvents(EditGUI.getEditGUI(),this);
        Bukkit.getPluginManager().registerEvents(new EditKit(),this);

        //插件检测
        MessageManager.sendMessageWithPrefix("");
        MessageManager.sendMessageWithPrefix(" __ __ __   ___   ___   ___   ___   ________  _________  ");
        MessageManager.sendMessageWithPrefix("/_//_//_/\\ /___/\\/__/\\ /___/\\/__/\\ /_______/\\/________/\\ ");
        MessageManager.sendMessageWithPrefix("\\:\\\\:\\\\:\\ \\\\::.\\ \\\\ \\ \\\\::.\\ \\\\ \\ \\\\__.::._\\/\\__.::.__\\/ ");
        MessageManager.sendMessageWithPrefix(" \\:\\\\:\\\\:\\ \\\\:: \\/_) \\ \\\\:: \\/_) \\ \\  \\::\\ \\    \\::\\ \\   ");
        MessageManager.sendMessageWithPrefix("  \\:\\\\:\\\\:\\ \\\\:. __  ( ( \\:. __  ( (  _\\::\\ \\__  \\::\\ \\  ");
        MessageManager.sendMessageWithPrefix("   \\:\\\\:\\\\:\\ \\\\: \\ )  \\ \\ \\: \\ )  \\ \\/__\\::\\__/\\  \\::\\ \\ ");
        MessageManager.sendMessageWithPrefix("    \\_______\\/ \\__\\/\\__\\/  \\__\\/\\__\\/\\________\\/   \\__\\/ ");
        MessageManager.sendMessageWithPrefix("");
        MessageManager.sendMessageWithPrefix("Version: "+ getDescription().getVersion() + " | Author: WekyJay | QQ Group: 945144520");
        // MessageManager.sendMessageWithPrefix("§a特别鸣谢：§eBiulay Gentry §7(排名不分先后)");


        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PapiHooker(this).register();
        }

        if(Bukkit.getPluginManager().getPlugin("Vault") != null) {
            new VaultHooker();
        }

        if(Bukkit.getPluginManager().getPlugin("MythicMobs") != null){
            new MythicMobsHooker();
        }

        //bStats
        int pluginId = 13579;
        new Metrics(this, pluginId);

        //Check Version
        if(getConfig().contains("Setting.CheckUpdate") && getConfig().getBoolean("Setting.CheckUpdate")) {
            HandySchedulerUtil.runTaskAsynchronously(ChackPluginVersion::new);
        }

        // 启动数据库
        if(Objects.requireNonNull(WkKit.wkkit.getConfig().getString("MySQL.Enable")).equalsIgnoreCase("true")) {
            HandySchedulerUtil.runTaskAsynchronously(()-> {
                    MySQLManager.get().enableMySQL();
                    if (MySQLManager.get().isReady()) {
                        HandySchedulerUtil.runTask(() -> {
                            ConfigManager.reloadKit();
                            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("KIT_NUM") + Kit.getKits().size());
                            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("MENU_NUM") + MenuManager.getInvs().size());
                        });
                        return;
                    }
                    MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("KIT_NUM") + Kit.getKits().size());
                    MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("MENU_NUM") + MenuManager.getInvs().size());
            });
        }else {
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("KIT_NUM") + Kit.getKits().size());
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("MENU_NUM") + MenuManager.getInvs().size());
        }


        // 防崩服记录线程启用
        this.enableAntiShutDown();

        // 检查礼包密码
        CodeManager.checkPassWord();

        // 日志管理
        KitCache.getCache();

    }

    public static boolean isFoliaServer() {
        return HandySchedulerUtil.isFolia();
    }

    public static void runForPlayer(Player player, Runnable task, long delayTicks) {
        Runnable onlineTask = () -> { if (player.isOnline()) task.run(); };
        if (!isFoliaServer()) {
            Bukkit.getScheduler().runTaskLater(wkkit, onlineTask, Math.max(1L, delayTicks));
            return;
        }
        // Resolve Folia classes only on Folia; ordinary Bukkit servers do not provide them.
        try {
            Object scheduler = org.bukkit.entity.Entity.class.getMethod("getScheduler").invoke(player);
            Class<?> schedulerType = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            java.util.function.Consumer<Object> callback = ignored -> onlineTask.run();
            schedulerType.getMethod("runDelayed", org.bukkit.plugin.Plugin.class,
                    java.util.function.Consumer.class, Runnable.class, long.class)
                    .invoke(scheduler, wkkit, callback, null, Math.max(1L, delayTicks));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to schedule task on the Folia player thread", e);
        }
    }

    /*插件关闭标识*/
    @Override
    public void onDisable() {
        // 关闭数据库
        if(WkKit.wkkit.getConfig().getString("MySQL.Enable").equalsIgnoreCase("true")) {
            MySQLManager.get().shutdown();
            WkKit.getWkKit().getLogger().info(LangConfigLoader.getString("MYSQL_SHUTDOWN"));
        }
        try {
            // 保存日志
            KitCache cache = KitCache.getInitializedCache();
            if (cache != null && getConfig().getBoolean("Cache.Enable")) cache.saveCache();
            // 保存关服时间
            File file = new File(WkKit.getWkKit().getDataFolder(),"config.yml");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            YamlConfiguration fileYaml = YamlConfiguration.loadConfiguration(file);
            fileYaml.set("Default.ShutDate", sdf.format(new Date()));
            fileYaml.save(file);
        } catch (IOException e) {
            ExceptionHandler.handleIO("保存插件关闭时间到配置文件", e);
        }
       MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UNINSTALL"));
    }

    /**
     * 启用防崩服记录线程启用
     */
    public void enableAntiShutDown() {
        if(this.getConfig().getInt("Default.AntiShutDown") == 0) return;
        long ticks = 20L * this.getConfig().getInt("Default.AntiShutDown") * 60;
        // 放入线程
        HandySchedulerUtil.runTaskTimer(() -> {
            File file = new File(WkKit.getWkKit().getDataFolder(), "config.yml");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            YamlConfiguration fileYaml = YamlConfiguration.loadConfiguration(file);
            fileYaml.set("Default.ShutDate", sdf.format(new Date()));
            try {
                fileYaml.save(file);
            } catch (IOException e) {
                ExceptionHandler.handleIO("定时保存防崩服记录", e);
            }
        }, 20, ticks);

    }

}
