package cn.wekyjay.www.wkkit.kit;


import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.config.ConfigManager;
import cn.wekyjay.www.wkkit.tool.CronManager;
import cn.wekyjay.www.wkkit.tool.WKTool;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NbtApiException;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Kit {
	private String kitname;
	private String displayName;
	private String icon;
	private ItemStack[] itemStack;
	private List<String> commands;
	private List<String> lore;
	private List<String> drop;
	private String permission;
	private String docron;
	private Integer delay;
	private Integer times = -1; // 初始值为-1（无限领取次数）
	private Integer vault;
	private List<String> mythicMobs;
	private boolean noRefreshFirst;
	/**
	 * 下次礼包刷新的时间
	 */
	private Calendar nextRC;

	
	public Kit(@NotNull String kitname,@NotNull String displayname,@NotNull String icon,ItemStack[] itemStack){
		this.kitname = kitname;
		this.displayName = displayname;
		this.icon = icon;
		this.itemStack = itemStack;
		this.loadKit(this);
	}
	
	
	public static List<Kit> getKits() {
		return ConfigManager.getKitconfig().getKitsList();
	}
	
	public static List<String> getKitNames(){
		List<String> list = new ArrayList<String>();
		for(Kit kit : Kit.getKits()) {
			list.add(kit.getKitname());
		}
		return list;
	}
	
	
	/**
	 * 通过礼包的名字返回Kit，如果没找到就返回null
	 * @param kitname
	 * @return
	 */
	public static Kit getKit(String kitname) {
		for(Kit kit : Kit.getKits()) {
			if(kit.getKitname().equals(kitname)) return kit;
		}
		return null;
	}
	

	
	
	/**
	 * 加载礼包
	 * @param kit
	 */
	public void loadKit(Kit kit) {
		if (kit.isKit()) {
			String kitname = kit.getKitname();
			if(ConfigManager.getKitconfig().contains(kitname + ".Commands")) commands = ConfigManager.getKitconfig().getStringList(kitname + ".Commands");
			if(ConfigManager.getKitconfig().contains(kitname + ".Lore")) lore = ConfigManager.getKitconfig().getStringList(kitname + ".Lore");
			if(ConfigManager.getKitconfig().contains(kitname + ".Drop")) {drop = ConfigManager.getKitconfig().getStringList(kitname + ".Drop");}
			if(ConfigManager.getKitconfig().contains(kitname + ".Permission")) permission =  ConfigManager.getKitconfig().getString(kitname + ".Permission");
			if(ConfigManager.getKitconfig().contains(kitname + ".Delay")) delay =  ConfigManager.getKitconfig().getInt(kitname + ".Delay");
			if(ConfigManager.getKitconfig().contains(kitname + ".Times")) times = ConfigManager.getKitconfig().getInt(kitname + ".Times");
			if(ConfigManager.getKitconfig().contains(kitname + ".DoCron")) docron = ConfigManager.getKitconfig().getString(kitname + ".DoCron");
			if(ConfigManager.getKitconfig().contains(kitname + ".NoRefreshFirst")) noRefreshFirst = ConfigManager.getKitconfig().getBoolean(kitname + ".NoRefreshFirst");
			if(ConfigManager.getKitconfig().contains(kitname + ".Vault")) vault = ConfigManager.getKitconfig().getInt(kitname + ".Vault");
			if(ConfigManager.getKitconfig().contains(kitname + ".MythicMobs")) mythicMobs = ConfigManager.getKitconfig().getStringList(kitname + ".MythicMobs");
			Kit.getKits().add(kit);
		}
	}
	
	/**
	 * 判断是否是一个有效礼包
	 * @return Boolean
	 */
	public Boolean isKit() {
		if(displayName != null & icon != null) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获得礼包的ID名(非展示名)
	 * @return
	 */
	public String getKitname() {
		return kitname;
	}

	/**
	 * 判断礼包是否为创建时自动刷新 true为首次创建不刷新
	 * @return
	 */
	public boolean isNoRefreshFirst() {
		return noRefreshFirst;
	}

	public void setNoRefreshFirst(boolean noRefreshFirst) {
		this.noRefreshFirst = noRefreshFirst;
		this.getConfigurationSection().set("NoRefreshFirst", noRefreshFirst);
	}

	// Getter & Setter
	/**
	 * 获得该礼包的文件块
	 * @return
	 */
	public ConfigurationSection getConfigurationSection() {
		String groupname = KitGroupManager.getContainName(this);
		ConfigurationSection cs = KitGroupManager.getGroup(groupname).getConfigurationSection(this.getKitname());
		return cs;
	}
	
	/**
	 * 设置完礼包属性之后一定要记得保存
	 * @throws IOException
	 */
	public final void saveConfig() {
		String groupname = KitGroupManager.getContainName(this);
		try {
			KitGroupManager.getGroup(groupname).save(ConfigManager.getKitconfig().getContainsFile(this.kitname));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			ExceptionHandler.handle("未知操作", e);
		}
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName; // 更改栈内存
		this.getConfigurationSection().set("Name", displayName);    // 更改文件内存
	}

	public String getPermission() {
		return permission;
	}
	
	public void setPermission(String permission) {
		this.permission = permission;
		this.getConfigurationSection().set("Permission", permission); 
	}
	
	public Integer getTimes() {
		return times;
	}
	
	public void setTimes(Integer times) {
		this.times = times;
		this.getConfigurationSection().set("Times", times);
	}
	
	public List<String> getCommands() {
		return commands;
	}

	public Integer getVault() {
		return vault;
	}

	public void setVault(Integer vault) {
		this.vault = vault;
		this.getConfigurationSection().set("Vault", vault);
	}

	public List<String> getMythicMobs() {
		return mythicMobs;
	}

	public void setMythicMobs(List<String> mythicMobs) {
		this.mythicMobs = mythicMobs;
		this.getConfigurationSection().set("MythicMobs", mythicMobs);
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
		this.getConfigurationSection().set("Commands", commands);
		
	}
	
	public Integer getDelay() {
		return delay;
	}
	
	public void setDelay(Integer delay) {
		this.delay = delay;
		this.getConfigurationSection().set("Delay", delay);
	}
	
	public String getDocron() {
		return docron;
	}
	
	public void setDocron(String docron) {
		this.docron = docron;
		this.getConfigurationSection().set("DoCron", docron);
	}
	
	public List<String> getDrop() {
		return drop;
	}
	
	public void setDrop(List<String> drop) {
		this.drop = drop;
		this.getConfigurationSection().set("Drop", drop);
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
		this.getConfigurationSection().set("Icon", icon);
	}

	/**
	 * 获取礼包的礼品内容
	 * @return
	 */
	public ItemStack[] getItemStacks() {
		return itemStack;
	}
	
	public void setItemStack(ItemStack[] itemStack) {
		this.itemStack = itemStack;
		List<String> list = new ArrayList<String>();
		for(ItemStack is : itemStack) {
			list.add(NBT.itemStackToNBT(is).toString());
		}
		this.getConfigurationSection().set("Item", list);
	}
	
	public List<String> getLore() {
		return lore;
	}
	
	public void setLore(List<String> lore) {
		this.lore = lore;
		this.getConfigurationSection().set("Lore", lore);
	}
	
	public Calendar getNextRC() {
		return nextRC;
	}


	
	/**
	 * 重新计算下次礼包的刷新时间
	 */
	public void restNextRC() {
		if(docron != null) {
			Calendar cnext = Calendar.getInstance();
			cnext.setTime(CronManager.getNextExecution(this.docron));
			this.nextRC = cnext;
		}
	}
	
	public Map<String,Object> getFlags() {
		Map<String,Object> kitflags = new HashMap<>();
		kitflags.put("DisplayName", displayName);
		kitflags.put("Icon", icon);
		kitflags.put("Times", times);
		kitflags.put("Delay", delay);
		kitflags.put("Permission", permission);
		kitflags.put("DoCron", docron);
		kitflags.put("Commands", commands);
		kitflags.put("Lore", lore);
		kitflags.put("Drop", drop);
		kitflags.put("Item", itemStack);
		kitflags.put("Vault", vault);
		kitflags.put("NoRefreshFirst", noRefreshFirst);
		kitflags.put("MythicMobs", mythicMobs);
		return kitflags;
	}

	/**
	 * 获得类型为Item的礼包实体
	 * @return
	 */
	public ItemStack getKitItem() {
		// 创建物品
		ItemStack item = null;
		try {
			if(icon.contains("[SKULL]")) {
				item = WKTool.nbtCovertoSkull(icon.substring(7));
			}else if(icon.contains("[NBT]")) {
				ReadWriteNBT nbt = NBT.parseNBT(icon.substring(5));
				item = NBT.itemStackFromNBT(nbt);
			}else if(icon.contains("[CUSTOMDATA]")){
				String[] str = icon.substring(12).split(":");
				item = new ItemStack(Material.getMaterial(str[0]));
				/**
				 * @20230819 修复CUSTOMDATA图标效问题
				 */
				ItemMeta im = item.getItemMeta();
                im.setCustomModelData(Integer.parseInt(str[1]));
				item.setItemMeta(im);

			}
		}catch(NbtApiException e) {
			ExceptionHandler.handle("未知操作", e);
		}
		if(item == null || item.getAmount() == 0) {
			// 如果出错或不存在就用默认配置的ICON替代
			if(Material.getMaterial(icon) == null) item = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("Default.Icon")));
			else item = new ItemStack(Material.getMaterial(icon));
		}

		// 先用NBT.modify修改NBT
		NBT.modify(item, nbt -> {
			nbt.setString("wkkit", kitname);
		});
		// 再获取ItemMeta并设置显示名和lore
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	
	
	
	
	// 添加新的判断方法
	public boolean canReceive(String playerName) {
		Calendar now = Calendar.getInstance();
		String lastTimeStr = WkKit.getPlayerData().getKitData(playerName, this.kitname);
		// 如果是第一次领取
		if(lastTimeStr == null) {
			return true;
		}
		
		try {
			// 解析上次领取时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d-H-m-s");
			Calendar lastTime = Calendar.getInstance();
			lastTime.setTime(sdf.parse(lastTimeStr));
			
			// 检查固定延迟(delay)
			if(delay != null && delay > 0) {
				Calendar nextTime = (Calendar) lastTime.clone();
				nextTime.add(Calendar.MINUTE, delay);
				if(now.before(nextTime)) {
					return false;
				}
			}
			
			// 检查Cron表达式
			if(docron != null) {
				Date nextExecution = CronManager.getNextExecution(this.docron);
				Calendar cronNextTime = Calendar.getInstance();
				cronNextTime.setTime(nextExecution);
				
				// 如果现在时间在上次领取时间和下次执行时间之间，说明还未到领取时间
				if(now.after(lastTime) && now.before(cronNextTime)) {
					return false;
				}
			}
			
			return true;
		} catch (ParseException e) {
			ExceptionHandler.handle("未知操作", e);
			return false;
		}
	}

	/**
	 * 计算并返回下次可领取时间
	 * @param playerName 玩家名
	 * @return 下次可领取时间
	 */
	public Calendar getNextReceiveTime(String playerName) {
		String lastTimeStr = WkKit.getPlayerData().getKitData(playerName, this.kitname);
		Calendar nextTime = Calendar.getInstance();
		
		// 如果状态为true，直接返回当前时间
		if("true".equalsIgnoreCase(lastTimeStr)) {
			return nextTime;
		}
		
		try {
			// 如果有历史记录，解析上次领取时间
			if(lastTimeStr != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar lastTime = Calendar.getInstance();
				lastTime.setTime(sdf.parse(lastTimeStr));
				
				// 如果有固定延迟
				if(delay != null && delay > 0) {
					nextTime = (Calendar) lastTime.clone();
					nextTime.add(Calendar.MINUTE, delay);
				}
			}
			
			// 如果有Cron表达式
			if(docron != null) {
				Date nextExecution = CronManager.getNextExecution(this.docron);
				Calendar cronNextTime = Calendar.getInstance();
				cronNextTime.setTime(nextExecution);
				
				// 取较晚的时间作为下次可领取时间
				if(cronNextTime.after(nextTime)) {
					nextTime = cronNextTime;
				}
			}
			
		} catch (ParseException e) {
			ExceptionHandler.handle("未知操作", e);
		}
		
		return nextTime;
	}
}
