package cn.wekyjay.www.wkkit.command;

import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.invholder.KitPreviewHolder;
import cn.wekyjay.www.wkkit.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.event.inventory.InventoryAction.NOTHING;
import static org.bukkit.event.inventory.InventoryAction.UNKNOWN;

public class KitInfo implements Listener{
	String title;
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 2) {
			sender.sendMessage(LangConfigLoader.getStringWithPrefix("Commands.info", ChatColor.RED));
			return;
		}
		
		this.getKitInfo(args[1], (Player)sender);
	}
	
	/**
	 * 礼包预览
	 * @param kitname
	 * @param p
	 */
	public void getKitInfo(String kitname,Player p) {
		int kitnum;//礼包礼物个数
		int guisize;//GUI大小

		if(Kit.getKit(kitname) == null)return;//如果不存在该礼包就返回
		Kit kit = Kit.getKit(kitname);
		ItemStack[] itemlist = kit.getItemStacks();//物品集合
		

		
		//创建GUI
		kitnum = itemlist.length;
		guisize = ((kitnum / 10) + 1) * 9;
		
		Inventory inv = Bukkit.createInventory(new KitPreviewHolder(), guisize, kit.getDisplayName());
		for(int i = 0;i < kitnum; i++) {
			inv.setItem(inv.firstEmpty(),itemlist[i]);
		}
		p.openInventory(inv);

	}
	@EventHandler
	public void onInventory(InventoryClickEvent e){
		
		// 确认是那个GUI
		if(e.getInventory().getHolder() instanceof KitPreviewHolder) {
			e.setCancelled(true);
			if(e.getRawSlot()<0 || e.getRawSlot() >= e.getInventory().getSize() || e.getInventory()==null) {
		        return;
			}
			if(e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) {
		        return;
			}
			return;
		}
	}
}
