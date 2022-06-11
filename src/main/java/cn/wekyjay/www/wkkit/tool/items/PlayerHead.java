package cn.wekyjay.www.wkkit.tool.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import cn.wekyjay.www.wkkit.tool.WKTool;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;

public enum PlayerHead {
	DEFAULT(),
	PRESENT_RED("dca29a3a-76d3-4979-88a2-2da034b99212","[I;-593323462,1993558393,-2002637408,884576786]","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0ZWU4OTcyMDYzZjQ2NmRlNjc4MzYzY2Y3YjFhMjFhODViNyJ9fX0="),
	MENU_BOOK("412a2b41-327c-4f2a-a555-9a12b8c4174a","[I;1093282625,847007530,-1521116654,-1195108534]","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjE0Y2Q1NGY3MmMxMTAxMzRjNjljZWU1Nzc1ZDE1ODM4NmE2ODc2MDk1ZjM3OWRkNDI3ZmVkNzNmYjNiNjc0MCJ9fX0="),
	STEV_PLUSHIE("6a4729be-2ecd-4ce9-8581-725a03b76b5f","[I;1783048638,785206505,-2055114150,62352223]","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGE1NWQwZmYwNDA0MDJlNmE5MjQ2ZDA5YmE5MGUyZTE1YzY4YTE5ZjdkOWE3ZGMwNWVjNGE1NzE3MDc4NGNjZSJ9fX0=");
	
	private ItemStack head;
	PlayerHead(){
		int version = WKTool.getVersion();
		if(version <= 12 && version > 7 ) {
			NBTContainer c = new NBTContainer("{id:\"minecraft:skull\",Count:1b,Damage:3s}");
			head = NBTItem.convertNBTtoItem(c);
		}else if(version <= 7) {
			NBTContainer c = new NBTContainer("{id:397s,Count:1b,Damage:3s,}");
			head = NBTItem.convertNBTtoItem(c);
		}
		else {
			NBTContainer c = new NBTContainer("{id:\"minecraft:player_head\",Count:1b}");
			head = NBTItem.convertNBTtoItem(c);
		}
	}
	PlayerHead(String id,String newid,String textures){
		int version = WKTool.getVersion();
		if(version <= 12 && version > 7) {
			String nbt = "{display:{Name:\"Present (red)\"},SkullOwner:{Id:\""+ id + "\",Properties:{textures:[{Value:\""+ textures + "\"}]}}}";
			NBTContainer c = new NBTContainer("{id:\"minecraft:skull\",Count:1b,Damage:3s}");
			head = NBTItem.convertNBTtoItem(c);//新建一个头颅物品
		    NBTItem nbti = new NBTItem(head);//传入头颅的NBT到NBTItem
			NBTContainer newc = new NBTContainer(nbt);//添加一个NBT容器
		    nbti.mergeCompound(newc); // 合并NBT
		    head = nbti.getItem();
		    return;
		}
		if(version <= 7) {
			NBTContainer c = new NBTContainer("{id:397s,Count:1b,Damage:3s,}");
			head = NBTItem.convertNBTtoItem(c);//新建一个头颅物品
			return;
		}
		if(version >= 13 && version <= 15){
			String nbt = "{display:{Name:\"{\\\"text\\\":\\\"Present (red)\\\"}\"},SkullOwner:{Id:\""+ id + "\",Properties:{textures:[{Value:\""+ textures + "\"}]}}}";
			NBTContainer c = new NBTContainer("{id:\"minecraft:player_head\",Count:1b}");
			head = NBTItem.convertNBTtoItem(c);//新建一个头颅物品
		    NBTItem nbti = new NBTItem(head);//传入头颅的NBT到NBTItem
			NBTContainer newc = new NBTContainer(nbt);//添加一个NBT容器
		    nbti.mergeCompound(newc); // 合并NBT
		    head = nbti.getItem();
		    return;
		}
		if(version > 15) {
			String nbt = "{display:{Name:\"{\\\"text\\\":\\\"Present (red)\\\"}\"},SkullOwner:{Id:"+ newid + ",Properties:{textures:[{Value:\""+ textures +"\"}]}}}";
			NBTContainer c = new NBTContainer("{id:\"minecraft:player_head\",Count:1b}");
			head = NBTItem.convertNBTtoItem(c);//新建一个头颅物品
		    NBTItem nbti = new NBTItem(head);//传入头颅的NBT到NBTItem
			NBTContainer newc = new NBTContainer(nbt);//添加一个NBT容器
		    nbti.mergeCompound(newc); // 合并NBT
		    head = nbti.getItem();
		    return;
		}
	}
	
	
	public ItemStack getItemStack() {
		return head;
	}
	
	public static ItemStack getPlayerHead(Player p) {
		ItemStack head = DEFAULT.getItemStack();
		NBTContainer c = NBTItem.convertItemtoNBT(head);
        NBTItem nbti = new NBTItem(head);
        NBTCompound skull = nbti.addCompound("SkullOwner");
        skull.setString("Name", p.getName());
        skull.setString("Id", p.getUniqueId().toString());
        head = nbti.getItem();
		return head;
		
	}
	
}
