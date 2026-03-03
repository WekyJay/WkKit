package cn.wekyjay.www.wkkit;

import cn.wekyjay.www.wkkit.command.*;
import cn.wekyjay.www.wkkit.config.ConfigManager;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.edit.EditGUI;
import cn.wekyjay.www.wkkit.edit.EditKit;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.kit.KitGetter;
import cn.wekyjay.www.wkkit.menu.MenuManager;
import cn.wekyjay.www.wkkit.menu.MenuOpenner;
import cn.wekyjay.www.wkkit.tool.KitCache;
import cn.wekyjay.www.wkkit.tool.MessageManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


public class KitCommand implements CommandExecutor{
	WkKit wk = WkKit.getWkKit();//创建一个主类的实例
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		/*指令帮助中心*/
		try {
			if(args.length <= 0 && sender.isOp() || args[0].equalsIgnoreCase("help") && sender.isOp()){
				sender.sendMessage("§a━━━━━━━━━━━━ WkKit Command ━━━━━━━━━━━━");
				sender.sendMessage(LangConfigLoader.getString("Commands.PS"));
				sender.sendMessage(LangConfigLoader.getString("Commands.help"));
				sender.sendMessage(LangConfigLoader.getString("Commands.edit"));
				sender.sendMessage(LangConfigLoader.getString("Commands.edit_kitname"));
				sender.sendMessage(LangConfigLoader.getString("Commands.savecache"));
				sender.sendMessage(LangConfigLoader.getString("Commands.create"));
				sender.sendMessage(LangConfigLoader.getString("Commands.delete"));
				sender.sendMessage(LangConfigLoader.getString("Commands.mail"));
				sender.sendMessage(LangConfigLoader.getString("Commands.info"));
				sender.sendMessage(LangConfigLoader.getString("Commands.kits"));
				sender.sendMessage(LangConfigLoader.getString("Commands.send"));
				sender.sendMessage(LangConfigLoader.getString("Commands.give"));
				sender.sendMessage(LangConfigLoader.getString("Commands.get"));
				sender.sendMessage(LangConfigLoader.getString("Commands.open"));
				sender.sendMessage(LangConfigLoader.getString("Commands.cdk_create"));
				sender.sendMessage(LangConfigLoader.getString("Commands.cdk_verify"));
				sender.sendMessage(LangConfigLoader.getString("Commands.cdk_exchange"));
				sender.sendMessage(LangConfigLoader.getString("Commands.cdk_export"));
				sender.sendMessage(LangConfigLoader.getString("Commands.group_create"));
				sender.sendMessage(LangConfigLoader.getString("Commands.group_delete"));
				sender.sendMessage(LangConfigLoader.getString("Commands.group_move"));
				sender.sendMessage(LangConfigLoader.getString("Commands.transfer"));
				sender.sendMessage(LangConfigLoader.getString("Commands.reload"));
				sender.sendMessage("§a━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
				return true;
			}
			else if(!sender.isOp() && args[0].equalsIgnoreCase("help")){
				sender.sendMessage("§a━━━━━━━━━━━━ WkKit Command ━━━━━━━━━━━━");
				sender.sendMessage(LangConfigLoader.getString("Commands.mail"));
				sender.sendMessage(LangConfigLoader.getString("Commands.open"));
				sender.sendMessage(LangConfigLoader.getString("Commands.get"));
				sender.sendMessage(LangConfigLoader.getString("Commands.cdk_verify"));
				sender.sendMessage(LangConfigLoader.getString("Commands.cdk_exchange"));
				sender.sendMessage("§a━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
				return true;
			}
		}catch(ArrayIndexOutOfBoundsException e) {return true;}
		
		/*重载插件*/
		if (args[0].equalsIgnoreCase("reload") && sender.isOp()){
			ConfigManager.reloadPlugin();
	        sender.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_NUM",ChatColor.GRAY) + Kit.getKits().size());
	        sender.sendMessage(LangConfigLoader.getStringWithPrefix("MENU_NUM",ChatColor.GRAY) + MenuManager.getInvs().size());
			sender.sendMessage(LangConfigLoader.getStringWithPrefix("CONFIG_RELOAD",ChatColor.GREEN));
		    return true;
		}
		
		/*打开邮箱*/
		if(args[0].equalsIgnoreCase("mail") && sender.hasPermission("wkkit.mail") && sender instanceof Player) {
			new KitMail().onCommand(sender, command, label, args);
			return true;
		}
		
		/*发放礼包*/
		if(args[0].equalsIgnoreCase("send") && sender.isOp()) {
			KitSend ksd = new KitSend();
			ksd.onCommand(sender, command, label, args);
			return true;
		}
		
		
		
		/*create指令*/
		
		if(args[0].equalsIgnoreCase("create") && sender.isOp() && sender instanceof Player){//判断指令参数是否为/wkkit create <name>
			KitCreate kc = new KitCreate();
			kc.onCommand(sender, args);
			return true;
		}
	
		
		/*delete指令逻辑*/
		if(args[0].equalsIgnoreCase("delete") && sender.isOp()) {
				KitDelete kr = new KitDelete();
				kr.onCommand(sender, command, label, args);
				return true;
			}
		
		/*查看配置里的礼包*/
		if(args[0].equalsIgnoreCase("kits") && sender.isOp()) {
			StringBuilder allkit = new StringBuilder();
			int num = 0;
			List<Kit> kits = Kit.getKits();
			Iterator<Kit> it = kits.iterator(); //遍历list
			while(it.hasNext()){
				allkit.append(it.next().getKitname() + " ");
				num += 1;
			}
			if(num == 0) sender.sendMessage(LangConfigLoader.getStringWithPrefix("NO_KIT_CONFIG", ChatColor.YELLOW));
			sender.sendMessage(ChatColor.GREEN + "Kits(" + num + ")：" + allkit.toString());
			return true;
		}
		
		/*get获取礼包的内容*/
		if(args[0].equalsIgnoreCase("info")  && sender instanceof Player) {
			if(sender.hasPermission("wkkit.info") || sender.isOp()){
				new KitInfo().onCommand(sender, command, label, args);
				return true;
			}
		}
		
		
		/*直接发送礼包*/
		if(args[0].equalsIgnoreCase("give") && sender.isOp()) {
			KitGive ks = new KitGive();
			ks.onCommand(sender, command, label, args);
			return true;
		}
		
		/*数据迁移*/
		if(args[0].equalsIgnoreCase("transfer") && sender.isOp()) {
			KitTransfer kt = new KitTransfer();
			kt.onCommand(sender, command, label, args);
		}
		if(args[0].equalsIgnoreCase("open") && sender.hasPermission("wkkit.open") && sender instanceof Player) {
			if(args.length < 2) {
				sender.sendMessage(LangConfigLoader.getStringWithPrefix("Commands.open", ChatColor.GREEN));
				return true;
			}
			Player p = (Player)sender;
			if(MenuManager.getMenu(args[1]) != null) new MenuOpenner().openMenu(args[1], p);
			else sender.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_MENU_INVALID", ChatColor.RED));
			return true;

		}
		
		if(args[0].equalsIgnoreCase("group") && sender.isOp()) {
			new KitGroup().onCommand(sender, command, label, args);
			return true;
		}
		
		/** 礼包编辑 **/
		if(args[0].equalsIgnoreCase("edit") && sender.isOp()) {
			Player p = (Player)sender;
			if(args.length > 1){
				if(Kit.getKitNames().contains(args[1])){
					p.openInventory(new EditKit().editKit(args[1]));
				}else{
					MessageManager.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_DELETE_NONEXIST",ChatColor.YELLOW),p);
				}
				return true;
			}
			p.openInventory(EditGUI.getEditGUI().getEditInv());
			return true;
		}
		
		/* cdk系统 */
		if(args[0].equalsIgnoreCase("cdk")) {
			new KitCDK().onCommand(sender, args);
			return true;
		}

		/* 玩家模拟菜单领取指令 */
		if(wk.getConfig().getBoolean("Setting.UseCommandGet") && args.length >= 2 && args[0].equalsIgnoreCase("get") && sender instanceof Player && sender.hasPermission("wkkit.get")) {
			String kitName = args[1];
			Player p = (Player)sender;
			Kit kit = Kit.getKit(kitName);

			if (kit == null){
				sender.sendMessage(LangConfigLoader.getStringWithPrefix("NO_KIT",ChatColor.RED) + " - " + kitName);
				return true;
			}

			// 直接使用KitGetter处理领取逻辑
			new KitGetter().getKit(kit, p, null);
			return true;
		}
		
		/* 保存日志 */
		if(args[0].equalsIgnoreCase("SaveCache") && sender.isOp()) {
			try {
				KitCache.getCache().saveCache();
			} catch (IOException e) {
				ExceptionHandler.handle("未知操作", e);
			}
			return true;
		}
//		/* 测试代码 */
//		if(args.length >= 1 && args[0].equalsIgnoreCase("head") && sender.isOp()) {
//			EditPlayer.selectPlayerGUI((Player)sender);
//		}
		
		/*插件更新命令*/
		if (args[0].equalsIgnoreCase("update") && sender.isOp()) {
			if (cn.wekyjay.www.wkkit.tool.ChackPluginVersion.isNeedUpdate()) {
				cn.wekyjay.www.wkkit.tool.ChackPluginVersion.doUpdate();
			} else {
				sender.sendMessage(cn.wekyjay.www.wkkit.config.LangConfigLoader.getStringWithPrefix("PLUGIN_CHACKUPDATE_NOTNEED", ChatColor.YELLOW));
			}
			return true;
		}
		
		/*到底了*/
		return true;
	}

}
