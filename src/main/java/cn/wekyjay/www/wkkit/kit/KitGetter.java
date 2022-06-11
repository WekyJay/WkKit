package cn.wekyjay.www.wkkit.kit;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.tool.WKTool;
/**
 * 用于领取礼包后保存玩家领取数据(目前只有菜单使用)
 * @author Administrator
 *
 */
public class KitGetter{
	/**
	 * 领取礼包
	 * @param kitname
	 * @param p
	 */
	public void getKit(Kit kit,Player p) {
		if(kit.getPermission() != null) {if(!this.runPermission(kit, p)) {return;}}
		if(kit.getItemStack() != null) {if(!this.runItem(kit, p)) {return;}}
		if(kit.getTimes() != null) {if(!this.runTimes(kit, p)) {return;}}
		// 可以执行
		if(kit.getCommands() != null) {this.runCommands(kit, p);}
		this.getSuccess(kit, p);
	}
	
	//******************************命 令 行*********************************//
	/**
	 * 运行指令
	 * @param kitname
	 * @param playername
	 */
	public void runCommands(Kit kit,Player p) {
		List<String> commands = kit.getCommands();
		for(String str : commands) {
			String[] splitstr = str.split(":");
			String command = null;
			if(splitstr.length > 1) {//判断是否有指定的指令发送方式
				command = WKTool.replacePlaceholder("player", p.getName(), splitstr[1]);
			}else {
				command = WKTool.replacePlaceholder("player", p.getName(), splitstr[0]);
			}
			//根据不同的指令发送方式发送
			if(splitstr[0].equalsIgnoreCase("cmd")) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
			}else if(splitstr[0].equalsIgnoreCase("op") && !p.isOp()) {
				p.setOp(true);
				Bukkit.dispatchCommand(p, command);
				p.setOp(false);
			}else {
				p.performCommand(command);
			}
		}
	}
	/**
	 * 权限检查，如果玩家没有权限则返回false
	 * @param kitname
	 * @param p
	 * @return
	 */
	public Boolean runPermission(Kit kit,Player p) {
		return p.hasPermission(kit.getPermission())?true:false;
	}
	/**
	 * 物品领取到背包
	 * @param kitname
	 * @param p
	 * @return
	 */
	public Boolean runItem(Kit kit,Player p) {
		if(!WKTool.hasSpace(p, kit)) {//判断有没有足够的背包空间
			p.sendMessage(LangConfigLoader.getString("KIT_GET_FAILED"));
			return false;
		}
		return true;
	}
	
	/**
	 * 计算领取次数
	 * @param kitname
	 * @param p
	 * @return
	 */
	public Boolean runTimes(Kit kit,Player p) {
		//如果玩家没有领取次数就创建一个
		String kitname = kit.getKitname();
		if(WkKit.getPlayerData().getKitTime(p.getName(), kitname) == null) {
			if(kit.getTimes() < 0 || kit.getTimes() == null) {
				WkKit.getPlayerData().setKitTime(p.getName(), kitname, -1);
				return true;
			}else {
				WkKit.getPlayerData().setKitTime(p.getName(), kitname, kit.getTimes());
				return true;
			}
			
		}
		int times = WkKit.getPlayerData().getKitTime(p.getName(),kitname);
		if(times != 0) return true;
		return false;
		
	}
	
	/**
	 * 领取后执行
	 * @param kitname
	 * @param p
	 */
	private void getSuccess(Kit kit, Player p) {
		String kitname = kit.getKitname();
		int times = -1;
		if(WkKit.getPlayerData().getKitTime(p.getName(),kitname) != null) {
			times = WkKit.getPlayerData().getKitTime(p.getName(),kitname);
		}
		// 计算领取状态
		if(kit.getDocron() != null) {
			WkKit.getPlayerData().setKitData(p.getName(), kitname, "false");
		}
		// 计算领取次数
		if(times > 0)WkKit.getPlayerData().setKitTime(p.getName(), kitname, times - 1);
		// 如果领取次数变成0了就也变成false
		if(WkKit.getPlayerData().getKitTime(p.getName(),kitname) != null && WkKit.getPlayerData().getKitTime(p.getName(),kitname) == 0)WkKit.getPlayerData().setKitData(p.getName(), kitname, "false");
		// 发送物品
		PlayerInventory pinv = p.getInventory();//使用封装类的getplayer方法获取玩家背包
		ItemStack[] itemlist = kit.getItemStack();
		for(ItemStack item : itemlist) {
			pinv.addItem(item);//添加物品至背包
		}
		// 发送消息
		p.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_GET_SUCCESS",ChatColor.GREEN));
	}
}
