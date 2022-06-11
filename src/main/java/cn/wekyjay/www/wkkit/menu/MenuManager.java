package cn.wekyjay.www.wkkit.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import cn.wekyjay.www.wkkit.config.MenuConfigLoader;
import cn.wekyjay.www.wkkit.event.KitMenuEvent;
import cn.wekyjay.www.wkkit.invholder.MenuHolder;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.tool.WKTool;

public class MenuManager {
	private static Map<String,Menu> menus = new HashMap<>();
	private static Map<String,Inventory> invs = new HashMap<>();
	
	public static Map<String, Inventory> getInvs() {
		return invs;
	}
	public static Map<String, Menu> getMenus() {
		return menus;
	}
	public void loadMenu(Menu menu) {
		String menuname = menu.getMenuName();
		String menutitle = menu.getMenuTitle();
		int menusize = menu.getSize();
		if(menu.getSize() < 0 && menu.getSize() > 6) {
			menusize = 1;
		}
		menus.put(menuname, menu);
		// 生成一个初始菜单
		Inventory inv;
		if(MenuConfigLoader.contains(menuname + ".Type") && !getType(menuname).equals(InventoryType.CHEST)) {
			inv = Bukkit.createInventory(new MenuHolder(menuname), getType(menuname), menutitle);
		}else {
			inv = Bukkit.createInventory(new MenuHolder(menuname), menusize * 9, menutitle);
		}
		// 往初始菜单内添加初始物品
		List<String> slotlist = MenuManager.getSlots(menuname);
		for(String slotname : slotlist) {
			if(Kit.getKit(slotname) == null) {//如果不是礼包
				String id = null,name = null;
				List<String> lore = new ArrayList<>();
				ItemStack item = null;
				if(MenuConfigLoader.contains(menuname + ".Slots." + slotname + ".id") && MenuConfigLoader.getString(menuname + ".Slots." + slotname + ".id") != null) {id = MenuConfigLoader.getString(menuname + ".Slots." + slotname + ".id");}
				if(MenuConfigLoader.contains(menuname + ".Slots." + slotname + ".name") && MenuConfigLoader.getString(menuname + ".Slots." + slotname + ".name") != null) {name = MenuConfigLoader.getString(menuname + ".Slots." + slotname + ".name");}
				if(MenuConfigLoader.contains(menuname + ".Slots." + slotname + ".lore") && MenuConfigLoader.getStringList(menuname + ".Slots." + slotname + ".lore") != null) {lore = MenuConfigLoader.getStringList(menuname + ".Slots." + slotname + ".lore");}
				if(id != null && name != null) {
					item = new ItemStack(Material.valueOf(id));
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(name);
					// 设置lore
					if(lore != null) {
						meta.setLore(lore);
					}
					item.setItemMeta(meta);
					List<Integer> slotnum = WKTool.getSlotNum(menuname + ".Slots." + slotname + ".slot");
					for(int num : slotnum) {
						inv.setItem(num, item);
					}
				}
				
			}
		}
		
		
		
		
		invs.put(menuname, inv);
		if(!KitMenuEvent.menutitles.contains(menutitle)) {KitMenuEvent.menutitles.add(menutitle);}
	}
	/**
	 * 返回Menu中属于礼包的Slot列表
	 * @param menuname
	 * @return
	 */
	public static List<String> getSlotOfKit(String menuname){
		List<String> kitlist = new ArrayList<>();
		for(String slotname : menus.get(menuname).slots) {
			if(Kit.getKit(slotname) != null) {
				kitlist.add(slotname);
			}
		}
		return kitlist;
		
	}
	
	public static Inventory getMenu(String menuname) {
		return invs.get(menuname);
	}
	
	public static String getTitle(String menuname) {
		return menus.get(menuname).getMenuTitle();
	}
	
	public static InventoryType getType(String menuname) {
		return InventoryType.valueOf(menus.get(menuname).getType());
	}
	
	public static String getPermission(String menuname) {
		return menus.get(menuname).getMenuPermission();
	}
	
	public static List<String> getSlots(String menuname){
		return menus.get(menuname).slots;
	}
}

