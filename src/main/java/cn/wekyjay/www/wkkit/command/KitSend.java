package cn.wekyjay.www.wkkit.command;

import cn.handyplus.lib.adapter.HandySchedulerUtil;
import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.api.PlayersReceiveKitEvent;
import cn.wekyjay.www.wkkit.api.ReceiveType;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public  class KitSend {
	static WkKit wk = WkKit.getWkKit();// 调用主类实例		

	/**
	 * 发放礼包给特定群体或个人
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 * @return
	 */
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(args.length < 3) {
			sender.sendMessage(LangConfigLoader.getStringWithPrefix("Commands.send", ChatColor.GREEN));
			return;
		}
		int kitnum = 1;
		if(args.length == 4) {
			kitnum = Integer.parseInt(args[3]);
			if(kitnum <= 0) {
				sender.sendMessage(LangConfigLoader.getStringWithPrefix("COMMAND_INVALID", ChatColor.RED));
				return;
			}
		}
		String kitname = args[1];
		String target = args[2];

		//判断礼包是否存在
		if(Kit.getKit(kitname) != null) {
			// 异步执行判断
			int finalKitnum = kitnum;

			if (HandySchedulerUtil.isFolia()){
				HandySchedulerUtil.runTaskAsynchronously(() ->{
					this.sendAsyn(target,kitname,sender,finalKitnum);
				});
			}else {
				Bukkit.getScheduler().runTask(wk,()->{
					this.sendAsyn(target,kitname,sender,finalKitnum);
				});
			}

				// 发放礼包给自己
				if(target.equalsIgnoreCase("@Me") && sender instanceof Player) {
					String pname = sender.getName();
					// 回调事件
					if(PlayersReceiveKitEvent.callEvent((Player)sender, Kit.getKit(kitname), ReceiveType.SEND).isCancelled()) return;
					if(WkKit.getPlayerData().contain_Mail(pname,kitname)) {
						int num = WkKit.getPlayerData().getMailKitNum(pname, kitname);
						WkKit.getPlayerData().setMailNum(pname, kitname, num + finalKitnum);
					}else {
						WkKit.getPlayerData().setMailNum(pname, kitname, finalKitnum);
					}
					// 发送提示
					sender.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_SEND_PLAYER", ChatColor.GREEN));
					sender.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_SEND_PICKUP", ChatColor.GREEN));
					return;
				}
				//发放礼包给：player
				if(!target.equalsIgnoreCase("@All") && !target.equalsIgnoreCase("@Online") && !target.equalsIgnoreCase("@Me")) {
					String pname = target;
					for(OfflinePlayer offlineplayer : Bukkit.getOfflinePlayers()) {
						if(offlineplayer.getName()!= null && offlineplayer instanceof OfflinePlayer && offlineplayer.getName().equals(pname)) {
							// 回调事件
							if(PlayersReceiveKitEvent.callEvent(offlineplayer.getPlayer(), pname ,Kit.getKit(kitname),ReceiveType.SEND).isCancelled()) return;
							if(WkKit.getPlayerData().contain_Mail(pname,kitname)) {
								int num = WkKit.getPlayerData().getMailKitNum(pname, kitname);
								WkKit.getPlayerData().setMailNum(pname, kitname, num + finalKitnum);
							}else {
								WkKit.getPlayerData().setMailNum(pname, kitname, finalKitnum);
							}
							// 发送消息
							if(offlineplayer.isOnline()) offlineplayer.getPlayer().sendMessage(LangConfigLoader.getStringWithPrefix("KIT_SEND_PICKUP", ChatColor.GREEN));
							sender.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_SEND_PLAYER", ChatColor.GREEN));
							return;
						}
					}
					// 如果执行到这里代表没找到玩家
					sender.sendMessage(LangConfigLoader.getStringWithPrefix("NO_PLAYER", ChatColor.RED));
					return;
				}
				sender.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_SENDING", ChatColor.RED));
				return;
		}
	}

	private void sendAsyn(String target,String kitname,CommandSender sender,int finalKitnum){
		//发放实体礼包给：@all
		if(target.equalsIgnoreCase("@All")) {
			OfflinePlayer[] playerlist = Bukkit.getOfflinePlayers();
			for(OfflinePlayer player : playerlist) {
				if (player != null && player instanceof OfflinePlayer) {
					String pname = player.getName();
					if(PlayersReceiveKitEvent.callEvent(player.getPlayer(),pname,Kit.getKit(kitname), ReceiveType.SEND).isCancelled()) return;
					if(WkKit.getPlayerData().contain_Mail(pname,kitname)) {
						int num = WkKit.getPlayerData().getMailKitNum(pname, kitname);
						WkKit.getPlayerData().setMailNum(pname, kitname, num + finalKitnum);
					}else {
						WkKit.getPlayerData().setMailNum(pname, kitname, finalKitnum);
					}
					// 发送提示
					if(player.isOnline()) player.getPlayer().sendMessage(LangConfigLoader.getStringWithPrefix("KIT_SEND_PICKUP", ChatColor.GREEN));
				}
			}
			sender.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_SEND_ALL", ChatColor.GREEN));
			return;
		}
		//发放礼包给：@online
		if(target.equalsIgnoreCase("@Online")) {
			OfflinePlayer[] playerlist = Bukkit.getOfflinePlayers();
			for(OfflinePlayer player : playerlist) {
				if (player != null && player instanceof Player) {
					String pname = player.getName();
					if(player.isOnline()) {//判断是否在线
						// 回调事件
						if(PlayersReceiveKitEvent.callEvent(player.getPlayer(),pname,Kit.getKit(kitname), ReceiveType.SEND).isCancelled()) return;

						if(WkKit.getPlayerData().contain_Mail(pname,kitname)) {
							int num = WkKit.getPlayerData().getMailKitNum(pname, kitname);
							WkKit.getPlayerData().setMailNum(pname, kitname, num + finalKitnum);
						}else {
							WkKit.getPlayerData().setMailNum(pname, kitname, finalKitnum);
						}
						// 发送提示
						player.getPlayer().sendMessage(LangConfigLoader.getStringWithPrefix("KIT_SEND_PICKUP", ChatColor.GREEN));
					}
				}
			}
			sender.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_SEND_ONLINE", ChatColor.GREEN));
			return;
		}
	}
}
