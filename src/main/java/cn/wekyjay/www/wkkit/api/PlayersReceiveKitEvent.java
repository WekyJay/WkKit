package cn.wekyjay.www.wkkit.api;

import cn.handyplus.lib.adapter.PlayerSchedulerUtil;
import cn.wekyjay.www.wkkit.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayersReceiveKitEvent extends PlayerEvent implements Cancellable {
	private Kit kit;
	private String menuname = null;
	private String playername = null;
	private boolean isCancelled = false;
	
	private ReceiveType receivetype;
    private static final HandlerList handlers = new HandlerList();
    
	public PlayersReceiveKitEvent(Player who,Kit kit, ReceiveType type) {
		super(who);
		this.kit = kit;
		this.playername = who.getName();
		this.receivetype = type;

	}
	public PlayersReceiveKitEvent(Player who,String playername,Kit kit, ReceiveType type) {
		super(who);
		this.kit = kit;
		this.playername = playername;
		this.receivetype = type;
	}
	public PlayersReceiveKitEvent(Player who,Kit kit,String menuname, ReceiveType type) {
		super(who);
		this.kit = kit;
		this.menuname = menuname;
		this.playername = who.getName();
		this.receivetype = type;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		isCancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public Kit getKit() {
		return kit;
	}
	
	/**
	 * 返回领取礼包的菜单名
	 * @return
	 * @Nullable 从何处领取的菜单名称，有可能为NULL.
	 */
	public String getMenuname() {
		return menuname;
	}

	public String getPlayername() {
		return playername;
	}

	public ReceiveType getType() {
		return receivetype;
	}
	
    public static HandlerList getHandlerList() {// 事件类的「获取处理器」方法
        return handlers;
    }
    
	/**
	 * 回调方法
	 * @return PlayersReceiveKitEvent
	 */
	public static PlayersReceiveKitEvent callEvent(Player player,Kit kit,ReceiveType type) {
	    PlayersReceiveKitEvent event = new PlayersReceiveKitEvent(player, kit, type);
	    Bukkit.getPluginManager().callEvent(event);
	    return event;
	}

	/**
	 * 兼容离线玩家的回调方法
	 * @param player
	 * @param playername
	 * @param kit
	 * @param type
	 * @return
	 */
	public static PlayersReceiveKitEvent callEvent(Player player,String playername,Kit kit,ReceiveType type) {
	    PlayersReceiveKitEvent event = new PlayersReceiveKitEvent(player,playername, kit, type);
	    Bukkit.getPluginManager().callEvent(event);

	    return event;
	}
	public static PlayersReceiveKitEvent callEvent(Player player,Kit kit,String value,ReceiveType type) {
	    PlayersReceiveKitEvent event = new PlayersReceiveKitEvent(player, kit, value, type);
	    Bukkit.getPluginManager().callEvent(event);
	    return event;
	}


}


