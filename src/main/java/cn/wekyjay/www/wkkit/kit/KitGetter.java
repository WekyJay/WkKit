package cn.wekyjay.www.wkkit.kit;

import cn.handyplus.lib.adapter.PlayerSchedulerUtil;
import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.api.PlayersReceiveKitEvent;
import cn.wekyjay.www.wkkit.api.ReceiveType;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.hook.MythicMobsHooker;
import cn.wekyjay.www.wkkit.hook.VaultHooker;
import cn.wekyjay.www.wkkit.tool.CronManager;
import cn.wekyjay.www.wkkit.tool.MessageManager;
import cn.wekyjay.www.wkkit.tool.WKTool;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/**
 * 用于领取礼包后保存玩家领取数据(目前只有菜单使用)
 * @author Administrator
 *
 */
public class KitGetter{
	/**
	 * 领取礼包
	 */
	public boolean getKit(Kit kit,Player p, String menuname) {
		// 回调事件
		PlayersReceiveKitEvent event = new PlayersReceiveKitEvent(p, kit, menuname, ReceiveType.GET);
		if (menuname != null) event = new PlayersReceiveKitEvent(p, kit, menuname, ReceiveType.MENU);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())return false;

		String kitname = kit.getKitname();
		String lastTimeStr = WkKit.getPlayerData().getKitData(p.getName(), kitname);

		// 检查是否可以领取
		if (!kit.canReceive(p.getName())) {
			if ("false".equalsIgnoreCase(lastTimeStr) || (WkKit.getPlayerData().getKitTime(p.getName(), kitname) != null && WkKit.getPlayerData().getKitTime(p.getName(), kitname) == 0)) {
				p.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_GET_CANTGET", ChatColor.RED) + " §c(次数已用完)");
			} else {
				Calendar nextTime = kit.getNextReceiveTime(p.getName());
				p.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_GET_CANTGET", ChatColor.RED) + 
					" " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nextTime.getTime()));
			}
			return false;
		}

		// 其他条件检查
		// Bug 2b 修复：先检查次数，再检查 NoRefreshFirst，避免次数用完时被 MENU_NEED_WAIT 拦截
		if(kit.getTimes() != null) {if(!this.runTimes(kit, p)) {return false;}}
		if(kit.isNoRefreshFirst()) {if(!this.runNoRefreshFirst(kit, p)) {return false;}}
		if(kit.getPermission() != null) {if(!this.runPermission(kit, p)) {return false;}}
                if(kit.getItemStacks() != null) {if(!this.runItem(kit, p)) {return false;}}
                if(kit.getVault() != null && kit.getVault() > 0) {if(!this.runVault(kit,p)){return false;}}

		// 以下代码可以安全执行
		if(kit.getCommands() != null) { this.runCommands(kit, p);}
		if(kit.getMythicMobs() != null) {this.runMythicMobs(kit,p);}
		this.getSuccess(kit, p);
		return true;
	}

	//******************************命 令 行*********************************//
	/**
	 * 运行指令
	 */
	public void runCommands(Kit kit,Player p) {
		List<String> commands = kit.getCommands();
		for(String str : commands) {
			String[] splitstr = str.split(":");
			String command = null;
			if(splitstr.length > 1) {//判断是否有指定的指令发送方式
				command = WKTool.replacePlaceholder("player", p.getName(), str.substring(splitstr[0].length() + 1));
			}else {
				command = WKTool.replacePlaceholder("player", p.getName(), splitstr[0]);
			}
			//根据不同的指令发送方式发送
			if(splitstr[0].equalsIgnoreCase("cmd")) {
				PlayerSchedulerUtil.dispatchCommand(command);
			}else if(splitstr[0].equalsIgnoreCase("op") && !p.isOp()) {
				PlayerSchedulerUtil.performOpCommand(p, command);
			}else {
				PlayerSchedulerUtil.performCommand(p,command);
			}
		}
	}
	/**
	 * 权限检查，如果玩家没有权限则返回false
	 * @param kit 礼包名称
	 * @param p
	 * @return
	 */
	public Boolean runPermission(Kit kit,Player p) {
		if( p.hasPermission(kit.getPermission())) {
			return true;
		}else {
			p.sendMessage(LangConfigLoader.getStringWithPrefix("MENU_NEED_PERMISSION", ChatColor.RED)+ " - " + kit.getPermission());
			return false;
		}
	}

	/**
	 *
	 * @param kit
	 * @param p
	 * @return
	 */
	public boolean runNoRefreshFirst(Kit kit,Player p) {
		if (kit.isNoRefreshFirst()) {
			String kitname = kit.getKitname();
			// 如果玩家有历史领取记录且不是"true"状态，说明需要等待刷新
			String kitData = WkKit.getPlayerData().getKitData(p.getName(), kitname);
			if (kitData != null && !"true".equalsIgnoreCase(kitData)) {
				p.sendMessage(LangConfigLoader.getStringWithPrefix("MENU_NEED_WAIT",ChatColor.YELLOW));
				return false;
			}
		}
		return true;
	}
        /**
         * 经济插件支持，给玩家发放金币奖励。
         * @param kit 礼包
         * @param p 玩家
         * @return
         */
        public boolean runVault(Kit kit,Player p) {
                if(kit.getVault() == null || kit.getVault() <= 0) return true;
                if(VaultHooker.getEcon() == null) return true;
                EconomyResponse economyResponse = VaultHooker.getEcon().depositPlayer(p.getPlayer(),kit.getVault());
                if (economyResponse.transactionSuccess()){
                        p.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_GET_DEDUCT_MONEY_SUCCESS",ChatColor.GREEN) + VaultHooker.getEcon().format(kit.getVault()));
                }else{
                        p.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_GET_DEDUCT_MONEY_FAILED",ChatColor.RED));
                        return false;
                }

                return true;
        }
	/**
	 * 物品领取到背包
	 * @param kit 礼包名称
	 * @param p
	 * @return
	 */
	public Boolean runItem(Kit kit,Player p) {
		if(!WKTool.hasSpace(p, kit)) {//判断有没有足够的背包空间
			p.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_GET_FAILED", ChatColor.YELLOW));
			return false;
		}
		return true;
	}

	/**
	 * 计算领取次数
	 * @param kit 礼包名称
	 * @param p
	 * @return
	 */
	public Boolean runTimes(Kit kit,Player p) {
		//如果玩家没有领取次数就创建一个
		String kitname = kit.getKitname();
		if(WkKit.getPlayerData().getKitTime(p.getName(), kitname) == null) {
			// 修复：先检查 null 再比较数值，避免 NPE
			if(kit.getTimes() == null || kit.getTimes() < 0) {
				WkKit.getPlayerData().setKitTime(p.getName(), kitname, -1);
				return true;
			}else {
				WkKit.getPlayerData().setKitTime(p.getName(), kitname, kit.getTimes());
				return true;
			}

		}
		int times = WkKit.getPlayerData().getKitTime(p.getName(),kitname);
		if(times != 0) return true;
		// Bug 1 修复：次数用完时发送提示消息
		p.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_GET_CANTGET", ChatColor.RED) + " §c(领取次数已用完)");
		return false;

	}

	/**
	 * 生成MM怪
	 * @param kit
	 * @param p
	 * @return
	 */
	public boolean runMythicMobs(Kit kit,Player p){
		kit.getMythicMobs().forEach(mob->{
			MythicMobsHooker.getMythicMobs().spawnMob(p,mob);
		});
		return true;
	}

	/**
	 * 领取后执行
	 * @param kit 礼包名称
	 * @param p
	 */
	private void getSuccess(Kit kit, Player p) {
		String kitname = kit.getKitname();
		
		// 处理领取次数（防御性 null 检查）
		Integer timesObj = kit.getTimes();
		int times = (timesObj != null) ? timesObj : -1;
		if(WkKit.getPlayerData().getKitTime(p.getName(), kitname) != null) {
			times = WkKit.getPlayerData().getKitTime(p.getName(), kitname);
		} else {
			WkKit.getPlayerData().setKitTime(p.getName(), kitname, times); // 初始化次数
		}
		
		// 更新为下次可领取时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar nextTime = Calendar.getInstance();
		
		if(kit.getDocron() != null) {
			// 如果有Cron表达式，使用下次执行时间
			Date nextExecution = CronManager.getNextExecution(kit.getDocron());
			nextTime.setTime(nextExecution);
		} else if(kit.getDelay() != null && kit.getDelay() > 0) {
			// 如果有固定延迟，计算下次可领取时间
			nextTime.add(Calendar.SECOND, kit.getDelay());
		}
		
		// 更新data为下次领取时间
		String nextTimeStr = sdf.format(nextTime.getTime());
		WkKit.getPlayerData().setKitData(p.getName(), kitname, nextTimeStr);
		
		// 处理领取次数
		if(times > 0) {
			times--;
			WkKit.getPlayerData().setKitTime(p.getName(), kitname, times);
			
			// 如果领取次数变成0，标记为不可领取
			if(times == 0) {
				WkKit.getPlayerData().setKitData(p.getName(), kitname, "false");
			}
		}
		
		// 发送物品
		ItemStack[] itemlist = kit.getItemStacks();
		if(itemlist != null) {
			for(ItemStack item : itemlist) {
				if(item != null) {
					WKTool.addItem(item, p);
				}
			}
		}
		
		// 发送成功消息
		p.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_GET_SUCCESS", ChatColor.GREEN));
	}
}
