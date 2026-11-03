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
		setDisplayName(displayName);
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
		NBT.modify(itemStack, nbt -> { nbt.setString(key, value); });
		return this;
	}
	public ItemEditer setNBTInteger(String key, Integer value) {
		NBT.modify(itemStack, nbt -> { nbt.setInteger(key, value); });
		return this;
	}
	
	public ItemEditer removeNBT(String key) {
		NBT.modify(itemStack, nbt -> { nbt.removeKey(key); });
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
        return item != null && item.getType() != org.bukkit.Material.AIR
                && NBT.get(item, (java.util.function.Function<de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT, Boolean>) nbt -> nbt.hasTag("wkkit"));
    }

    public static String getWkKitTagValue(ItemStack item) {
        if (!hasWkKitTag(item)) return null;
        return NBT.get(item, (java.util.function.Function<de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT, String>) nbt -> nbt.getString("wkkit"));
    }
}
