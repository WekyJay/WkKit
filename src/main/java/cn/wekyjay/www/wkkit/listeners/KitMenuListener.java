package cn.wekyjay.www.wkkit.listeners;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.command.KitInfo;
import cn.wekyjay.www.wkkit.invholder.MenuHolder;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.kit.KitGetter;
import cn.wekyjay.www.wkkit.menu.MenuOpenner;
import cn.wekyjay.www.wkkit.tool.ItemEditer;
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
				return;
			}
			
			Player p = Bukkit.getPlayer(e.getWhoClicked().getName());
			//如果点击是空格子就取消事件
			try {
				if(ItemEditer.hasWkKitTag(e.getCurrentItem())) {
					String kitname = ItemEditer.getWkKitTagValue(e.getCurrentItem());
					Kit kit = Kit.getKit(kitname);
					
					// 如果是右键则预览礼包
					if(e.getClick().isRightClick()) {
						new KitInfo().getKitInfo(kitname, p);
						return;
					}
					
					// 尝试领取礼包
					MenuHolder menuholder = (MenuHolder) e.getInventory().getHolder();
					new KitGetter().getKit(kit, p, menuholder.getMenuname());
				}
			} catch(NullPointerException e1) {
				return;
			}
			
			//领取物品后是否关闭GUI
			if(WkKit.getWkKit().getConfig().getBoolean("GUI.ClickClose")) {
				p.closeInventory();
			}else {
				MenuHolder menuholder = (MenuHolder) e.getInventory().getHolder();
				String menuname = menuholder.getMenuname();
				// 使用任务延迟刷新菜单，确保数据已保存且客户端状态稳定
				Bukkit.getScheduler().runTaskLater(WkKit.getWkKit(), () -> {
					if (p.isOnline()) {
						new MenuOpenner().openMenu(menuname, p);
					}
				}, 1L);
			}
		}
		
	}
}
