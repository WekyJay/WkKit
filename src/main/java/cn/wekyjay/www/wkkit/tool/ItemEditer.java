package cn.wekyjay.www.wkkit.tool;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemEditer {
	private ItemStack itemStack;
	public ItemEditer(ItemStack itemStack) {
		this.itemStack = itemStack.clone();
	}
	public ItemEditer(ItemStack itemStack,String displayName){
		this.itemStack = itemStack.clone();
		ItemMeta im = itemStack.getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		itemStack.setItemMeta(im);
	}
	
	/**
	 * 设置物品名称
	 * @param name
	 * @return
	 */
	public ItemEditer setDisplayName(String name) {
		ItemMeta im = itemStack.getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		itemStack.setItemMeta(im);
		return this;
	}

	/**
	 * 设置物品Lore
	 * @param lore
	 * @return
	 */
	public ItemEditer setLore(List<String> lore) {
		ItemMeta im = itemStack.getItemMeta();
		List<String> newlore = new ArrayList<>();
		for(String str : lore) {
			newlore.add(ChatColor.translateAlternateColorCodes('&', str));
		}
		im.setLore(newlore);
		itemStack.setItemMeta(im);
		return this;
	}
	
	public ItemEditer setNBTString(String key, String value) {
		ReadWriteNBT nbt = NBT.itemStackToNBT(itemStack);
		nbt.setString(key, value);
		itemStack = NBT.itemStackFromNBT(nbt);
		return this;
	}
	public ItemEditer setNBTInteger(String key, Integer value) {
		ReadWriteNBT nbt = NBT.itemStackToNBT(itemStack);
		nbt.setInteger(key, value);
		itemStack = NBT.itemStackFromNBT(nbt);
		return this;
	}
	
	public ItemEditer removeNBT(String key) {
		ReadWriteNBT nbt = NBT.itemStackToNBT(itemStack);
		nbt.removeKey(key);
		itemStack = NBT.itemStackFromNBT(nbt);
		return this;
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public String getDisplayName() {
		return itemStack.getItemMeta().getDisplayName();
	}
	public List<String> getLore() {
		return itemStack.getItemMeta().getLore();
	}
	public ReadWriteNBT getNBT() {
		return NBT.itemStackToNBT(itemStack);
	}

	/**
	 * 判断物品是否有wkkit标签，自动适配不同版本
	 */
	public static boolean hasWkKitTag(ItemStack item) {
		String fullVersion = WKTool.getFullVersion();
		ReadWriteNBT nbt = WKTool.getItemNBT(item);
		if (WKTool.compareVersion(fullVersion, "1.20.5") >= 0) {
			// 1.20.5及以上，查components.minecraft:custom_data
			ReadWriteNBT components = nbt.getCompound("components");
			if (components != null) {
				ReadWriteNBT customData = components.getCompound("minecraft:custom_data");
				return customData != null && customData.hasTag("wkkit");
			}
			return false;
		} else {
			// 低于1.20.5，直接查根节点
			ReadWriteNBT tag = nbt.getCompound("tag");
			return tag != null && tag.hasTag("wkkit");
		}
	}

	/**
	 * 获取物品wkkit标签的值，自动适配不同版本
	 */
	public static String getWkKitTagValue(ItemStack item) {
		String fullVersion = WKTool.getFullVersion();
		ReadWriteNBT nbt = WKTool.getItemNBT(item);
		if (WKTool.compareVersion(fullVersion, "1.20.5") >= 0) {
			// 1.20.5及以上
			ReadWriteNBT components = nbt.getCompound("components");
			if (components != null) {
				ReadWriteNBT customData = components.getCompound("minecraft:custom_data");
				if (customData != null && customData.hasTag("wkkit")) {
					return customData.getString("wkkit");
				}
			}
			return null;
		} else {
			// 低于1.20.5
			ReadWriteNBT tag = nbt.getCompound("tag");
			if (tag != null && tag.hasTag("wkkit")) {
				return tag.getString("wkkit");
			}
			return null;
		}
	}

}
