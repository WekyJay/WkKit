package cn.wekyjay.www.wkkit.command;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.config.ConfigManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.kit.KitGroupManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.tool.WKTool;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KitCreate {
	static WkKit wk = WkKit.getWkKit();		

	public Boolean onCommand(CommandSender sender, String[] args) {
		if(args.length < 2) {
			sender.sendMessage(LangConfigLoader.getStringWithPrefix("Commands.create", ChatColor.GREEN));
			return true;
		}
		
		Player player = (Player) sender;
		String kitname = args[1];
		String target = "Default";
		String displayname = wk.getConfig().getString("Default.Name");
		String itemtype = wk.getConfig().getString("Default.Icon");
		List<String> lore = new ArrayList<>();
		
		// 设置礼包名称
		if(args.length >= 3) {
			displayname = ChatColor.translateAlternateColorCodes('&', args[2]);
		}
		
		// 检查礼包是否存在
		if(Kit.getKit(kitname) != null && args[0].equals("create")) {
			player.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_CREATE_EXISTS", ChatColor.YELLOW));
			return true;
		}
		
		// 设置礼包组
		if(args.length >= 4 && KitGroupManager.contains(args[3])) {
			target = args[3];
		}else {
			File file = new File(WkKit.kitFile.getAbsolutePath(),"Default.yml");
			if(!file.exists())new KitGroupManager("Default");
			sender.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_GROUP_DEFAULT", ChatColor.YELLOW));
		}

		// 获取玩家背包物品
		ItemStack is;
		if(WKTool.getVersion() >= 9) {
			is =  player.getInventory().getItemInMainHand();
		}else {
			is =  player.getInventory().getItemInHand();
		}

		if(is.getAmount() == 0) {
			lore = new ArrayList<String>();
		}else {
			ItemMeta im =is.getItemMeta();
			itemtype = is.getType().toString();
			if(im.hasLore()) lore = im.getLore();
		}
		
		
		/* 获取玩家背包物品 */
		
		// 获取玩家背包物品
		PlayerInventory pinv = ((Player) sender).getInventory();
		
		List<String> l = new ArrayList<String>();
		
		// 获取玩家背包物品
		ItemStack[] obj = pinv.getContents();
		
		for(int i = 0; i < obj.length; i++) {
			if(obj[i] != null) {
				// 将物品转换为NBT
				l.add(NBT.itemStackToNBT(obj[i]).toString());

			}
		}
		ItemStack[] iss = new ItemStack[l.size()];
		for(int i = 0; i < l.size(); i++) {
			ReadWriteNBT nbt = NBT.parseNBT(l.get(i));
			iss[i] = NBT.itemStackFromNBT(nbt);
			
		}
		
			// 设置礼包名称
			ConfigManager.getKitconfig().set(kitname + ".Name", displayname, target);
			// 设置礼包图标
			ConfigManager.getKitconfig().set(kitname + ".Icon", itemtype, target);
			// 设置礼包lore
			if(lore.isEmpty()) {
				ConfigManager.getKitconfig().set(kitname + ".Lore", "", target);	
			}else {
				ConfigManager.getKitconfig().set(kitname + ".Lore", lore, target);	
			}
			// 设置礼包物品
			if(l.isEmpty()) {
				ConfigManager.getKitconfig().set(kitname + ".Item", "", target);
			}else {
				ConfigManager.getKitconfig().set(kitname + ".Item", l, target);
			}


		
		// 保存配置
		try {
			ConfigManager.getKitconfig().save(target + ".yml");
			new Kit(kitname, displayname, itemtype, iss);
			player.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_CREATE_SUCCESS",ChatColor.GREEN));
			
		} catch (Exception e) {
			ExceptionHandler.handle("未知操作", e);
		}
		return true;
	}
}
