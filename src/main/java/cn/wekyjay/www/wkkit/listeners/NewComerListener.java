package cn.wekyjay.www.wkkit.listeners;

import cn.handyplus.lib.adapter.HandySchedulerUtil;
import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.tool.WKTool;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NewComerListener implements Listener {
	WkKit wk = WkKit.getWkKit();
	
	/*玩家第一次进入事件*/
	@EventHandler
	public void onPlayerJion(PlayerJoinEvent e) {
		// 初始化数据
	    String pname = e.getPlayer().getName();
	    boolean isnc = this.wk.getConfig().getBoolean("NewComer.Enable");//新人礼包是否开启
	    boolean isauto = this.wk.getConfig().getBoolean("NewComer.Auto"); // 是否自动发放
		int mode = this.wk.getConfig().getInt("NewComer.Mode");
	    String nckitname = this.wk.getConfig().getString("NewComer.Kit");//
		Kit nckit = Kit.getKit(nckitname);
		// 逻辑层
	    if (isnc && nckit != null) {
	    	if(this.wk.getConfig().getBoolean("NewComer.Strict")
					&& e.getPlayer().getStatistic(Statistic.LEAVE_GAME) > 0) return;
	    	if(WkKit.getPlayerData().contain_Kit(pname, nckitname))return;

			WkKit.runForPlayer(e.getPlayer(), ()->{
				if(isauto) {
					if (mode == 1){
						e.getPlayer().getInventory().addItem(nckit.getKitItem());
					}else {
						WKTool.addItem(e.getPlayer(),nckit.getItemStacks());
					}
					WkKit.getPlayerData().setKitToFile(pname, nckitname, "false", 0);
				}else {
					WkKit.getPlayerData().setKitToFile(pname, nckitname, "true", 1);
				}

			},20L);
	    }
	    
	  }
}
