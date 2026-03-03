package cn.wekyjay.www.wkkit.menu;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.api.PlayersKitRefreshEvent;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.config.MenuConfigLoader;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.data.playerdata.PlayerData_MySQL;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.invholder.MenuHolder;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.tool.CronManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.tool.MessageManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.tool.WKTool;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;


public class MenuOpenner {
	public void openMenu(String menuname,Player p) {
		// 判断是否存在权限
		if(!(MenuManager.getPermission(menuname) == null) && !p.hasPermission(MenuManager.getPermission(menuname))) {// 缺少权限
			p.sendMessage(LangConfigLoader.getStringWithPrefix("MENU_NEED_PERMISSION", ChatColor.RED )+ " - "  + MenuManager.getPermission(menuname));
			return;
		} 
		String playername = p.getName();
		Inventory inv;
		if(MenuManager.getType(menuname).equals(InventoryType.CHEST)){
			inv = Bukkit.createInventory(new MenuHolder(menuname), MenuManager.getInvs().get(menuname).getSize(), MenuManager.getTitle(menuname));
		}else {
			inv = Bukkit.createInventory(new MenuHolder(menuname), MenuManager.getType(menuname), MenuManager.getTitle(menuname));
		}



		/*
		 适配1.8版本的服务器
		 2024/6/29
		 */
		ListIterator<ItemStack> iterator = MenuManager.getMenu(menuname).iterator();
		List<ItemStack> temp = new ArrayList<>();
		iterator.forEachRemaining(temp::add);

		inv.setContents(temp.toArray(new ItemStack[0]));

		List<String> kitlist = MenuManager.getSlotOfKit(menuname);

		// 遍历礼包是否能打开
		kitlist.forEach(kitName->{
			Kit kit = Kit.getKit(kitName);
			if(kit != null && kit.getDocron() != null) {
				// 获取玩家当前时间和上次领取时间
				String lastTimeStr = WkKit.getPlayerData().getKitData(playername, kitName);
				Calendar cnow = Calendar.getInstance();
				
				try {
					// 如果状态为true，说明已经可以领取，不需要进行时间判断
					if("true".equalsIgnoreCase(lastTimeStr)) {
						return;
					}
					
					if(lastTimeStr != null) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						
						Calendar lastTime = Calendar.getInstance();
						lastTime.setTime(sdf.parse(lastTimeStr));
						
						// 如果现在时间已经超过了记录的时间
						if(cnow.after(lastTime)) {
							// 判断是否为首次不刷新礼包
							if(kit.isNoRefreshFirst()) {
								kit.setNoRefreshFirst(false);
								return; // 跳过首次刷新
							}
							
							// 回调玩家接收礼包
							PlayersKitRefreshEvent.callEvent(p, kit); // 回调
							
							// 更新礼包状态
							if(WkKit.getPlayerData() instanceof PlayerData_MySQL) {
								((PlayerData_MySQL) WkKit.getPlayerData()).setKitDataOfLock(playername, kitName, "true");
							} else {
								WkKit.getPlayerData().setKitData(playername, kitName, "true");
							}
							
							MessageManager.infoDeBug("已刷新礼包：" + kitName);
						}
					} else {
						// 如果没有记录时间，且不是首次不刷新，则设置为可领取
						if(!kit.isNoRefreshFirst()) {
							WkKit.getPlayerData().setKitData(playername, kitName, "true");
						}
					}
				} catch(ParseException e) {
					MessageManager.infoDeBug("解析时间出错：" + kitName);
					ExceptionHandler.handle("未知操作", e);
				}
			}
		});
		
		// 展开类型菜单
		if(MenuConfigLoader.contains(menuname + ".Spread") && MenuConfigLoader.getBoolean(menuname + ".Spread") && kitlist.size() == 1) {
			// 领取按钮
			if(MenuConfigLoader.contains(menuname + ".Slots.Get")){
				String id = MenuConfigLoader.getString(menuname + ".Slots.Get.id");

				String kitname = kitlist.get(0);
				Kit kit = Kit.getKit(kitname);
				// 判断是否领过礼包，如果领过就重新判断权限
				if(Kit.getKit(kitname).isNoRefreshFirst()
						|| WkKit.getPlayerData().contain_Kit(playername, kitname)) {
					// [判断是否可以领取]
					// 不能领取的条件
					if(Kit.getKit(kitname).isNoRefreshFirst()
							|| WkKit.getPlayerData().getKitData(playername, kitname) != null && !WkKit.getPlayerData().getKitData(playername, kitname).equalsIgnoreCase("true")
							|| WkKit.getPlayerData().getKitTime(playername, kitname) != null && WkKit.getPlayerData().getKitTime(playername, kitname) == 0) {
						for(int num : WKTool.getSlotNum(menuname + ".Slots.Get.slot")) {
							ItemStack item = new ItemStack(Material.BARRIER);
							// 设置自定义图标
							if(MenuConfigLoader.contains(menuname + ".Slots." + kitname + ".offid")) {
								item = new ItemStack(Material.getMaterial(MenuConfigLoader.getString(menuname + ".Slots." + kitname + ".offid")));
							}
							ItemMeta meta = item.getItemMeta();
							
							List<String> list = new ArrayList<String>();
							// 设置自定义lore
							if(MenuConfigLoader.contains(menuname + ".Slots." + kitname + ".offlore")) {
								list = MenuConfigLoader.getStringList(menuname + ".Slots." + kitname + ".offlore");
								if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
									list = PlaceholderAPI.setPlaceholders(p, list);
								}
							}else {// 没有设置就默认
								list.add(LangConfigLoader.getString("CLICK_GET_NEXT_STATUS"));
								if(kit.getDocron() != null) list.add(LangConfigLoader.getString("CLICK_GET_NEXT_DATE") + "§e" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(CronManager.getNextExecution(kit.getDocron())));
								else list.add(LangConfigLoader.getString("CLICK_GET_NEXT_NODATE"));
								if(kit.getTimes() != null) list.add(LangConfigLoader.getString("CLICK_GET_NEXT_TIMES") + "§e" + WkKit.getPlayerData().getKitTime(playername, kitname));
								else list.add(LangConfigLoader.getString("CLICK_GET_NEXT_NOTIMES"));
							}
							meta.setLore(list);
							meta.setDisplayName(kit.getDisplayName());
							item.setItemMeta(meta);
							ReadWriteNBT nbt = de.tr7zw.changeme.nbtapi.NBT.itemStackToNBT(item);
							nbt.setString("wkkit", kitname);
							inv.setItem(num, de.tr7zw.changeme.nbtapi.NBT.itemStackFromNBT(nbt));
						}
					}else {
						ItemStack is = new ItemStack(Material.getMaterial(id));
						List<Integer> slotnum = WKTool.getSlotNum(menuname + ".Slots.Get.slot");
						for(int num : slotnum) {
							// 设置自定义lore
							if(MenuConfigLoader.contains(menuname + ".Slots." + kitname + ".lore")) {
								ItemMeta meta = is.getItemMeta();
								List<String> list = MenuConfigLoader.getStringList(menuname + ".Slots." + kitname + ".lore");
								if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
									list = PlaceholderAPI.setPlaceholders(p, list);
								}
								meta.setLore(list);
								meta.setDisplayName(kit.getDisplayName());
								is.setItemMeta(meta);
							}
							// 设置NBT
							ReadWriteNBT nbt = de.tr7zw.changeme.nbtapi.NBT.itemStackToNBT(is);
							nbt.setString("wkkit", kitname);
							inv.setItem(num, de.tr7zw.changeme.nbtapi.NBT.itemStackFromNBT(nbt));
						}
					}
				}else {
					ItemStack is = new ItemStack(Material.getMaterial(id));
					List<Integer> slotnum = WKTool.getSlotNum(menuname + ".Slots.Get.slot");
					for(int num : slotnum) {
						// 设置自定义lore
						if(MenuConfigLoader.contains(menuname + ".Slots." + kitname + ".lore")) {
							ItemMeta meta = is.getItemMeta();
							List<String> list = MenuConfigLoader.getStringList(menuname + ".Slots." + kitname + ".lore");
							if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
								list = PlaceholderAPI.setPlaceholders(p, list);
							}
							meta.setLore(list);
							meta.setDisplayName(kit.getDisplayName());
							is.setItemMeta(meta);
						}
						// 设置NBT
						ReadWriteNBT nbt = de.tr7zw.changeme.nbtapi.NBT.itemStackToNBT(is);
						nbt.setString("wkkit", kitname);
						inv.setItem(num, de.tr7zw.changeme.nbtapi.NBT.itemStackFromNBT(nbt));
					}
				}
				
			}
			// 遍历检查
			for(String kitname : kitlist) {
				Kit kit = Kit.getKit(kitname);
				int itemnum = (int) Stream.of(kit.getItemStacks()).filter(item -> item != null).count();
				int nounnum = (int) Stream.of(inv.getContents()).filter(item -> item == null).count();
				// 比较大小，如果有足够空间
				if(itemnum <= nounnum) {
					List<Integer> slotsIndex = new ArrayList<>();
					// 遍历物品
					for(int i = 0; i < inv.getContents().length; i++) {
						if(inv.getItem(i) == null) slotsIndex.add(i);
					}
					// 添加物品
					for(int i = 0; i < kit.getItemStacks().length; i++) {
						inv.setItem(slotsIndex.get(i), kit.getItemStacks()[i]);
					}
				}
				
			}
			// 删除空气方块
			for(int i = 0; i < inv.getSize(); i++) {
				ItemStack is = inv.getItem(i);
				if(is != null && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().equalsIgnoreCase("AIR")) {
					inv.clear(i);
				}
			}

			// 打开菜单
			p.openInventory(inv);
			return;
		}
		
		// 普通类型菜单
		// 遍历检查
		for(String kitname : kitlist) {
			if(Kit.getKit(kitname).isNoRefreshFirst() ||WkKit.getPlayerData().contain_Kit(playername, kitname)) {
				// 如果不能领取
				if(Kit.getKit(kitname).isNoRefreshFirst()
						||WkKit.getPlayerData().getKitData(playername, kitname) != null && !WkKit.getPlayerData().getKitData(playername, kitname).equalsIgnoreCase("true")
						|| WkKit.getPlayerData().getKitTime(playername, kitname) != null && WkKit.getPlayerData().getKitTime(playername, kitname) == 0) {
						for(int num : WKTool.getSlotNum(menuname + ".Slots." + kitname + ".slot")) {
							ItemStack item = new ItemStack(Material.BARRIER);
							// 设置自定义图标
							if(MenuConfigLoader.contains(menuname + ".Slots." + kitname + ".offid")) {
								item = new ItemStack(Material.getMaterial(MenuConfigLoader.getString(menuname + ".Slots." + kitname + ".offid")));
							}
							ItemMeta meta = item.getItemMeta();
							Kit kit = Kit.getKit(kitname);
							List<String> list = new ArrayList<String>();
							// 设置自定义lore
							if(MenuConfigLoader.contains(menuname + ".Slots." + kitname + ".offlore")) {
								list = MenuConfigLoader.getStringList(menuname + ".Slots." + kitname + ".offlore");
								if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
									list = PlaceholderAPI.setPlaceholders(p, list);
								}
							}else {// 没有设置就默认
								list.add(LangConfigLoader.getString("CLICK_GET_NEXT_STATUS"));
								if(kit.getDocron() != null) list.add(LangConfigLoader.getString("CLICK_GET_NEXT_DATE") + "§e" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(CronManager.getNextExecution(kit.getDocron())));
								else list.add(LangConfigLoader.getString("CLICK_GET_NEXT_NODATE"));
								if(kit.getTimes() != null) list.add(LangConfigLoader.getString("CLICK_GET_NEXT_TIMES") + "§e" + WkKit.getPlayerData().getKitTime(playername, kitname));
								else list.add(LangConfigLoader.getString("CLICK_GET_NEXT_NOTIMES"));
							}
							
							meta.setLore(list);
							meta.setDisplayName(kit.getDisplayName());
							item.setItemMeta(meta);
							ReadWriteNBT nbt = de.tr7zw.changeme.nbtapi.NBT.itemStackToNBT(item);
							nbt.removeKey("wkkit");
							inv.setItem(num, de.tr7zw.changeme.nbtapi.NBT.itemStackFromNBT(nbt));
						}
				}else {
					ItemStack is = Kit.getKit(kitname).getKitItem();
					List<Integer> slotnum = WKTool.getSlotNum(menuname + ".Slots." + kitname + ".slot");
					for(int num : slotnum) {
						// 设置自定义lore
						if(MenuConfigLoader.contains(menuname + ".Slots." + kitname + ".lore")) {
							ItemMeta meta = is.getItemMeta();
							List<String> list = MenuConfigLoader.getStringList(menuname + ".Slots." + kitname + ".lore");
							if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
								list = PlaceholderAPI.setPlaceholders(p, list);
							}
							meta.setLore(list);
							is.setItemMeta(meta);
						}
						ReadWriteNBT nbt = de.tr7zw.changeme.nbtapi.NBT.itemStackToNBT(is);
						nbt.setString("wkkit", kitname);
						inv.setItem(num, de.tr7zw.changeme.nbtapi.NBT.itemStackFromNBT(nbt));
					}
				}
			}else {
				ItemStack is = Kit.getKit(kitname).getKitItem();
				List<Integer> slotnum = WKTool.getSlotNum(menuname + ".Slots." + kitname + ".slot");
				for(int num : slotnum) {
					// 设置自定义lore
					if(MenuConfigLoader.contains(menuname + ".Slots." + kitname + ".lore")) {
						ItemMeta meta = is.getItemMeta();
						List<String> list = MenuConfigLoader.getStringList(menuname + ".Slots." + kitname + ".lore");
						if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
							list = PlaceholderAPI.setPlaceholders(p, list);
						}
						meta.setLore(list);
						is.setItemMeta(meta);
					}
					ReadWriteNBT nbt = de.tr7zw.changeme.nbtapi.NBT.itemStackToNBT(is);
					nbt.setString("wkkit", kitname);
					inv.setItem(num, de.tr7zw.changeme.nbtapi.NBT.itemStackFromNBT(nbt));
				}
			}
		}
		// 删除空气方块
		for(int i = 0; i < inv.getSize(); i++) {
			ItemStack is = inv.getItem(i);
			if(is != null && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().equalsIgnoreCase("AIR")) {
				inv.clear(i);
			}
		}
		p.openInventory(inv);
		
	}
}
