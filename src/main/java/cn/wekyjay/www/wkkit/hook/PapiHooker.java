package cn.wekyjay.www.wkkit.hook;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.config.ConfigManager;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.menu.MenuManager;
import cn.wekyjay.www.wkkit.tool.CronManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PapiHooker extends PlaceholderExpansion {

	private final WkKit plugin;
    
    public PapiHooker(WkKit plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getAuthor() {
        return "WekyJay";
    }
    
    @Override
    public String getIdentifier() {
        return "wkkit";
    }

    @Override
    public String getVersion() {
        return WkKit.getWkKit().getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }
    
    @Override
    public String onRequest(OfflinePlayer player, String params) {
    	String playername = player.getName();
    	// 服务器礼包数
    	if(params.equalsIgnoreCase("serverkits")) {
    		return ConfigManager.getKitconfig().getKitsList().size() + "";
    	}
    	// 服务器菜单数
    	if(params.equalsIgnoreCase("servermenus")) {
    		return MenuManager.getMenus().size() + "";
    	}
        //玩家礼包邮箱的总礼包数量
        if(params.equalsIgnoreCase("kitmail")) {
        	int kitnum = 0;
        	List<String> kits = WkKit.getPlayerData().getMailKits(player.getName());
        	if(kits != null) {
            	for(String kitname : kits) {
            		kitnum += WkKit.getPlayerData().getMailKitNum(playername, kitname);
            	}
            	return kitnum + "";
        	}
        	return "0";
        }
        // 指定礼包的信息
        String[] s = params.split("_");
        // 领取次数
        if(s.length == 2 && s[0].equalsIgnoreCase("times")) {
        	String kitname = s[1];
        	Kit kit = Kit.getKit(kitname);
        	if(kit != null) {
        		if(kit.getTimes() != null) return kit.getTimes() + "";
        		else return "-1";
        	}
        	return null;
        }
        // 指定礼包的冷却时间
        if(s.length == 2 && s[0].equalsIgnoreCase("delay")) {
        	String kitname = s[1];
        	Kit kit = Kit.getKit(kitname);
        	if(kit != null) {
        		if(kit.getDelay() != null) return kit.getDelay() + "";
        		else return "0";
        	}
        	return null;
        }
        // 指定礼包的展示名
        if(s.length == 2 && s[0].equalsIgnoreCase("name")) {
        	String kitname = s[1];
        	Kit kit = Kit.getKit(kitname);
        	if(kit != null) return kit.getDisplayName();
        	return null;
        }
        // 指定礼包所需要的权限
        if(s.length == 2 && s[0].equalsIgnoreCase("permission")) {
        	String kitname = s[1];
        	Kit kit = Kit.getKit(kitname);
        	if(kit != null) {
        		if(kit.getPermission() != null) return kit.getPermission();
        	}
        	return "None";
        }
        // 获得指定礼包距离下次刷新的大致时间
        if(s.length == 2 && s[0].equalsIgnoreCase("tonext")) {
        	String kitname = s[1];
        	Kit kit = Kit.getKit(kitname);
        	if(kit != null) {
        		String kitData = WkKit.getPlayerData().getKitData(playername, kitname);
        		if ("false".equalsIgnoreCase(kitData)) return "次数已用完";
        		
        		if (kit.canReceive(playername)) return "§a可领取";
        		
        		Calendar nextTime = kit.getNextReceiveTime(playername);
        		long diffMillis = nextTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
        		return diffMillis <= 0 ? "§a可领取" : formatDuration(diffMillis);
        	}
        	return "None";
        }
        // 获得指定礼包的下次刷新日期
        if(s.length == 2 && s[0].equalsIgnoreCase("next")) {
        	String kitname = s[1];
        	Kit kit = Kit.getKit(kitname);
        	if(kit != null) {
        		String kitData = WkKit.getPlayerData().getKitData(playername, kitname);
        		if ("false".equalsIgnoreCase(kitData)) return "次数已用完";
        		
        		if (kit.canReceive(playername)) return "§a可领取";
        		
        		Calendar nextTime = kit.getNextReceiveTime(playername);
        		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nextTime.getTime());
        	}
        	return "None";
        }
        return null; // Placeholder is unknown by the Expansion
    }
    
    /**
     * 将毫秒数格式化为易读的时间描述（如：3天2小时30分钟15秒）
     * 参考 CronManager.getDescribeToNext 的格式
     * @param millis 毫秒数
     * @return 格式化的时间描述
     */
    private String formatDuration(long millis) {
    	Calendar calc = Calendar.getInstance();
    	calc.setTimeInMillis(millis);
    	
    	// 计算时间差的各个单位值
    	long totalSeconds = millis / 1000;
    	long days = totalSeconds / (24 * 3600);
    	long hours = (totalSeconds % (24 * 3600)) / 3600;
    	long minutes = (totalSeconds % 3600) / 60;
    	long seconds = totalSeconds % 60;
    	
    	// 为时间差加上单位
    	String lang = WkKit.getWkKit().getConfig().getString("Setting.Language");
    	List<String> units = new ArrayList<String>();
    	List<Long> values = new ArrayList<Long>();
    	
    	if(!lang.equals("zh_CN")) {
    		if (days > 0) { units.add("D"); values.add(days); }
    		if (hours > 0) { units.add("H"); values.add(hours); }
    		if (minutes > 0) { units.add("MIN"); values.add(minutes); }
    		if (seconds > 0) { units.add("S"); values.add(seconds); }
    	} else {
    		if (days > 0) { units.add("天"); values.add(days); }
    		if (hours > 0) { units.add("小时"); values.add(hours); }
    		if (minutes > 0) { units.add("分钟"); values.add(minutes); }
    		if (seconds > 0) { units.add("秒"); values.add(seconds); }
    	}
    	
    	StringBuilder str = new StringBuilder();
    	for (int i = 0; i < units.size(); i++) {
    		if (i > 0) str.append("/");
    		str.append(values.get(i)).append(units.get(i));
    	}
    	
    	return str.length() > 0 ? str.toString() : "0" + (lang.equals("zh_CN") ? "秒" : "S");
    }
}
