package cn.wekyjay.www.wkkit.tool.items;

import cn.wekyjay.www.wkkit.tool.WKTool;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.inventory.ItemStack;

public enum GlassPane {
	DEFAULT(),
	WHITE("white","0s"),
	ORANGE("orange","1s"),
	MAGENTA("magenta","2s"),
	LIGHT_BLUE("light_blue","3s"),
	YELLOW("yellow","4s"),
	LIME("lime","5s"),
	PINK("pink","6s"),
	GRAY("gray","7s"),
	LIGHT_GRAY("light_gray","8s"),
	CYAN("cyan","9s"),
	PURPLE("purple","10s"),
	BLUE("blue","11s"),
	BROWN("brown","12s"),
	GREEN("green","13s"),
	RED("red","14s"),
	BLACK("black","15s");

	private ItemStack item;
	GlassPane() {
		int version = WKTool.getVersion();
		if(version <= 12 && WKTool.getVersion() > 7) {
			item = NBT.itemStackFromNBT(NBT.parseNBT("{id:\"minecraft:stained_glass_pane\",Count:1b,Damage:0s}"));
		}else if(WKTool.getVersion() <= 7) {
			item = NBT.itemStackFromNBT(NBT.parseNBT("{id:102s,Count:1b,Damage:0s,}"));
		}else {
			item = NBT.itemStackFromNBT(NBT.parseNBT("{id:\"white_stained_glass_pane\",Count:1b}"));
		}
	}
	GlassPane(String color,String data) {
		if(WKTool.getVersion() <= 12 && WKTool.getVersion() > 7) {
			item = NBT.itemStackFromNBT(NBT.parseNBT("{id:\"minecraft:stained_glass_pane\",Count:1b,Damage:"+ data + "}"));
		}else if(WKTool.getVersion() <= 7) {
			item = NBT.itemStackFromNBT(NBT.parseNBT("{id:102s,Count:1b,Damage:0s,}"));
		}else {
			item = NBT.itemStackFromNBT(NBT.parseNBT("{id:\""+ color +"_stained_glass_pane\",Count:1b}"));
		}
	}
	public ItemStack getItemStack() {
		return item;
	}
}
