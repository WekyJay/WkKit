package cn.wekyjay.www.wkkit.listeners;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.command.KitInfo;
import cn.wekyjay.www.wkkit.invholder.MenuHolder;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.kit.KitGetter;
import cn.wekyjay.www.wkkit.menu.MenuOpenner;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.inventory.InventoryAction.NOTHING;
import static org.bukkit.event.inventory.InventoryAction.UNKNOWN;


public class KitMenuListener implements Listener{

	public static List<String> menutitles = new ArrayList<>();
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerClick(InventoryClickEvent e) {

		if(e.getInventory().getHolder() instanceof MenuHolder) {
			e.setCancelled(true);// 取消物品的拿取
			if(e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) {
				e.setCancelled(true);
		        return;
			}
			
			Player p = Bukkit.getPlayer(e.getWhoClicked().getName());
			//如果点击是空格子就取消事件
			try {
				new NBTItem(e.getCurrentItem());
			}catch(NullPointerException e1) {
				return;
			}
			
			NBTItem itemnbt = new NBTItem(e.getCurrentItem());//获取被点击的礼包的NBT
			// 如果存在key就是可以进行礼包领取操作
			if(itemnbt.hasKey("wkkit")) {
				String kitname = itemnbt.getString("wkkit");
				// 如果是右键则预览礼包
				if(e.getClick().isRightClick()) {
					new KitInfo().getKitInfo(kitname, p);
					return;
				}else {
				// 如果是其它的键就领取礼包
					MenuHolder menuholder = (MenuHolder) e.getInventory().getHolder();
					new KitGetter().getKit(Kit.getKit(kitname), p, menuholder.getMenuname());
				}
			}else {
				return;
			}
			
			//领取物品后是否关闭GUI
			if(WkKit.getWkKit().getConfig().getBoolean("GUI.ClickClose")) {
				p.closeInventory();
			}else {
				MenuHolder menuholder = (MenuHolder) e.getInventory().getHolder();
				p.closeInventory();
				new MenuOpenner().openMenu(menuholder.getMenuname(), p);
			}
		}
		
	}
}
