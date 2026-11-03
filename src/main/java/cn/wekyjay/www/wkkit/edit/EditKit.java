package cn.wekyjay.www.wkkit.edit;

import cn.wekyjay.www.wkkit.WkKit;
import cn.handyplus.lib.adapter.HandySchedulerUtil;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.edit.prompt.KitDeletePrompt;
import cn.wekyjay.www.wkkit.edit.prompt.KitFlagPrompt;
import cn.wekyjay.www.wkkit.invholder.EditKitGroupHolder;
import cn.wekyjay.www.wkkit.invholder.EditKitHolder;
import cn.wekyjay.www.wkkit.invholder.EditKitItemHolder;
import cn.wekyjay.www.wkkit.invholder.EditKitMainHolder;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.kit.KitGroupManager;
import cn.wekyjay.www.wkkit.config.ConfigManager;
import cn.wekyjay.www.wkkit.mysql.KitDataStore;
import cn.wekyjay.www.wkkit.mysql.entity.KitGroupRecord;
import cn.wekyjay.www.wkkit.mysql.entity.KitRecord;
import cn.wekyjay.www.wkkit.tool.ItemEditer;
import cn.wekyjay.www.wkkit.tool.WKTool;
import cn.wekyjay.www.wkkit.tool.items.GlassPane;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.bukkit.event.inventory.InventoryAction.NOTHING;
import static org.bukkit.event.inventory.InventoryAction.UNKNOWN;

public class EditKit implements Listener {
	private static final ConcurrentHashMap<UUID, AtomicLong> PAGE_REQUESTS = new ConcurrentHashMap<>();
	private String editTitle;
	private Inventory[] kitGroupInvs; // 礼包组管理界面
	private List<ItemStack> kitGroupItemList;
	/**
	 * 礼包管理主页
	 */
	public EditKit() {
		int groupNum = KitGroupManager.getGroups().size();
		kitGroupItemList = new ArrayList<>();
		editTitle = LangConfigLoader.getString("EDIT_KIT_TITLE");
		//判断可创建的gui个数
		int guinum = 0;
		if (groupNum % 45 == 0 && groupNum != 0) guinum = groupNum / 45;
	    else guinum = groupNum / 45 + 1;
		
		kitGroupInvs = new Inventory[guinum];
		
		//添加物品到itemlist

		for(String kitGroupName : KitGroupManager.getGroups()) {
			ItemStack item = new ItemEditer(new ItemStack(Material.BOOK)).setDisplayName(kitGroupName).getItemStack();
			NBT.modify(item, nbti->{
			nbti.setString("wkkit", kitGroupName);
			});
			kitGroupItemList.add(item);
		}
		
		//创建gui到linv
		for(int i = 1; i <= guinum; i++) {
			Inventory inv;
			if(guinum == 1) {//如果只有一页就不加页数
				inv = Bukkit.createInventory(new EditKitMainHolder(), 6*9, editTitle);
			}else {
				String pagetitle = WKTool.replacePlaceholder("page", i+"", LangConfigLoader.getString("GUI_PAGETITLE"));
				inv = Bukkit.createInventory(new EditKitMainHolder(), 6*9, editTitle + " - " + pagetitle);
			}

			
			//添加物品：功能区
			ItemStack item_mn;
			if(WkKit.getWkKit().getConfig().getString("GUI.MenuMaterial").equalsIgnoreCase("Default")){
				item_mn = GlassPane.DEFAULT.getItemStack();
			}else {
				item_mn = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.MenuMaterial")));
			}
			ItemMeta im = item_mn.getItemMeta();
			im.setDisplayName(LangConfigLoader.getString("DO_NOT_TOUCH"));
			item_mn.setItemMeta(im);
			//添加功能性物品：上一页
			ItemStack item_pre = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.TurnPageMaterial")));
			ItemMeta ip = item_pre.getItemMeta();
			ip.setDisplayName(LangConfigLoader.getString("PREVIOUS_PAGE"));
			item_pre.setItemMeta(ip);
			//添加功能性物品：下一页
			ItemStack item_next = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.TurnPageMaterial")));
			ItemMeta in = item_next.getItemMeta();
			in.setDisplayName(LangConfigLoader.getString("NEXT_PAGE"));
			item_next.setItemMeta(in);
			
			for(int i1 = 54 - 9; i1 < 54; i1++) {//最下一排
				inv.setItem(i1, item_mn);
			}

			kitGroupInvs[i-1] = inv;
		}
		//添加物品到指定的inv
		int num = 0;
		for(int invnum = 0; invnum < guinum; invnum++) {
			for(int i2 = 0; i2 < 45; i2++) {
				if(num >= groupNum || kitGroupItemList.size() == 0 ) {
					break;
				}
				kitGroupInvs[invnum].setItem(i2, kitGroupItemList.get(num));
				num += 1;
			}
		}
	}

	public static void openDatabaseMain(Player player, int page) {
		requestPage(player, page, null, true);
	}

	public static void openDatabaseGroup(Player player, String group, int page) {
		requestPage(player, page, group, false);
	}

	private static void requestPage(Player player, int page, String group, boolean groups) {
		Inventory source = WKTool.getOpenTopInventory(player);
		long version = PAGE_REQUESTS.computeIfAbsent(player.getUniqueId(), ignored -> new AtomicLong()).incrementAndGet();
		int requestedPage = Math.max(1, page);
		HandySchedulerUtil.runTaskAsynchronously(() -> {
			try {
				if (groups) {
					int resolvedPage = requestedPage;
					com.baomidou.mybatisplus.extension.plugins.pagination.Page<KitGroupRecord> result = KitDataStore.pageGroups(resolvedPage, 45);
					if (result.getPages() == 0) resolvedPage = 1;
					else if (resolvedPage > result.getPages()) {
						resolvedPage = (int) result.getPages();
						result = KitDataStore.pageGroups(resolvedPage, 45);
					}
					final int displayPage = resolvedPage;
					final com.baomidou.mybatisplus.extension.plugins.pagination.Page<KitGroupRecord> pageResult = result;
					WkKit.runForPlayer(player, () -> {
						AtomicLong latest = PAGE_REQUESTS.get(player.getUniqueId());
						if (!player.isOnline() || latest == null || latest.get() != version || WKTool.getOpenTopInventory(player) != source) return;
						player.openInventory(buildDatabaseMain(pageResult.getRecords(), displayPage, pageResult.getPages()));
					}, 0L);
				} else {
					int resolvedPage = requestedPage;
					com.baomidou.mybatisplus.extension.plugins.pagination.Page<KitRecord> result = KitDataStore.pageKits(group, resolvedPage, 28);
					if (result.getPages() == 0) resolvedPage = 1;
					else if (resolvedPage > result.getPages()) {
						resolvedPage = (int) result.getPages();
						result = KitDataStore.pageKits(group, resolvedPage, 28);
					}
					final int displayPage = resolvedPage;
					final com.baomidou.mybatisplus.extension.plugins.pagination.Page<KitRecord> pageResult = result;
					WkKit.runForPlayer(player, () -> {
						AtomicLong latest = PAGE_REQUESTS.get(player.getUniqueId());
						if (!player.isOnline() || latest == null || latest.get() != version || WKTool.getOpenTopInventory(player) != source) return;
						player.openInventory(buildDatabaseGroup(group, pageResult.getRecords(), displayPage, pageResult.getPages()));
					}, 0L);
				}
			} catch (Exception e) {
				WkKit.getWkKit().getLogger().warning("编辑菜单分页查询失败: " + e.getMessage());
				WkKit.runForPlayer(player, () -> player.sendMessage("§c编辑菜单读取失败，请稍后重试。"), 0L);
			}
		});
	}

	private static Inventory buildDatabaseMain(List<KitGroupRecord> groups, int page, long pages) {
		String title = LangConfigLoader.getString("EDIT_KIT_TITLE");
		if (pages > 1) title += " - " + WKTool.replacePlaceholder("page", String.valueOf(page), LangConfigLoader.getString("GUI_PAGETITLE"));
		Inventory inv = Bukkit.createInventory(new EditKitMainHolder(), 54, title);
		ItemStack pane = menuPane();
		for (int slot = 45; slot < 54; slot++) inv.setItem(slot, pane);
		for (int i = 0; i < groups.size() && i < 45; i++) {
			String name = groups.get(i).getName();
			ItemStack item = new ItemEditer(new ItemStack(Material.BOOK)).setDisplayName(name).getItemStack();
			NBT.modify(item, nbt -> { nbt.setString("wkkit", name); });
			inv.setItem(i, item);
		}
		if (pages > 1) {
			if (page > 1) inv.setItem(48, pageButton(LangConfigLoader.getString("PREVIOUS_PAGE"), page - 1, null));
			if (page < pages) inv.setItem(50, pageButton(LangConfigLoader.getString("NEXT_PAGE"), page + 1, null));
		}
		return inv;
	}

	private static Inventory buildDatabaseGroup(String group, List<KitRecord> kits, int page, long pages) {
		String title = LangConfigLoader.getString("EDIT_KIT_GROUP_TITLE") + " - " + group;
		if (pages > 1) title += " - " + WKTool.replacePlaceholder("page", String.valueOf(page), LangConfigLoader.getString("GUI_PAGETITLE"));
		Inventory inv = Bukkit.createInventory(new EditKitGroupHolder(), 54, title);
		List<Integer> slots = Arrays.asList(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43);
		ItemStack pane = new ItemEditer(menuPane(), LangConfigLoader.getString("DO_NOT_TOUCH")).getItemStack();
		for (int i = 0; i < 54; i++) if (!slots.contains(i)) inv.setItem(i, pane);
		inv.setItem(1, new ItemEditer(GlassPane.BLACK.getItemStack(), LangConfigLoader.getString("EDIT_BACK")).getItemStack());
		for (int i = 0; i < kits.size() && i < slots.size(); i++) {
			Kit kit = Kit.getKit(kits.get(i).getKitname());
			if (kit == null) continue;
			ItemStack item = kit.getKitItem();
			ItemMeta meta = item.getItemMeta();
			if (meta != null) { meta.setLore(java.util.Collections.singletonList(LangConfigLoader.getString("EDIT_CLICK_EDITKIT"))); item.setItemMeta(meta); }
			NBT.modify(item, nbt -> { nbt.setString("wkkit", kit.getKitname()); });
			inv.setItem(slots.get(i), item);
		}
		if (pages > 1) {
			if (page > 1) inv.setItem(48, pageButton(LangConfigLoader.getString("PREVIOUS_PAGE"), page - 1, group));
			if (page < pages) inv.setItem(50, pageButton(LangConfigLoader.getString("NEXT_PAGE"), page + 1, group));
		}
		return inv;
	}

	private static ItemStack menuPane() {
		String material = WkKit.getWkKit().getConfig().getString("GUI.MenuMaterial", "Default");
		Material parsed = Material.matchMaterial(material);
		return "Default".equalsIgnoreCase(material) || parsed == null ? GlassPane.DEFAULT.getItemStack() : new ItemStack(parsed);
	}

	private static ItemStack pageButton(String name, int page, String group) {
		String material = WkKit.getWkKit().getConfig().getString("GUI.TurnPageMaterial", "SLIME_BALL");
		ItemStack item = new ItemEditer(new ItemStack(Material.matchMaterial(material) == null ? Material.SLIME_BALL : Material.matchMaterial(material)), name).getItemStack();
		NBT.modify(item, nbt -> { nbt.setInteger("page", page); if (group != null) nbt.setString("groupname", group); });
		return item;
	}

	/**
	 * 获取礼包组展示界面的首页
	 * @return
	 */
	public Inventory getInventory() {
		return kitGroupInvs[0];
	}

	/**
	 * 编辑礼包组
	 * @param groupname
	 * @return
	 */
	public Inventory editGroup(String groupname, int page) {
		List<String> kitsname = KitGroupManager.getGroupKits(groupname);
		int kitnum = kitsname.size();
		List<ItemStack> itemlist = new ArrayList<>();
		String title = LangConfigLoader.getString("EDIT_KIT_GROUP_TITLE") + " - " + groupname;
		List<Integer> slot = Arrays.asList(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43);
		//判断可创建的gui个数
		int guinum = 0;
		if(kitnum % 28 == 0 && !(kitnum == 0)) guinum = kitnum / 28;
		else guinum = (kitnum / 28) + 1;
		Inventory[] invlist = new Inventory[guinum];
		
		//添加物品到itemlist
		for(String kitname : kitsname) {
			Kit kit = Kit.getKit(kitname);
			ItemStack is = kit.getKitItem();
			ItemMeta im = is.getItemMeta();
			List<String> lore = new ArrayList<String>();
			lore.add(LangConfigLoader.getString("EDIT_CLICK_EDITKIT"));
			im.setLore(lore);
			is.setItemMeta(im);
			itemlist.add(is);
		}
		
		//创建gui到linv
		for(int i = 1; i <= guinum; i++) {
			Inventory inv;
			ItemStack item_mn;
		      if (guinum == 1) {
		    	  	inv = Bukkit.createInventory(new EditKitGroupHolder(), 54, title);
		        } else {
		        	String pagetitle = WKTool.replacePlaceholder("page", i + "", LangConfigLoader.getString("GUI_PAGETITLE"));
		          	inv = Bukkit.createInventory(new EditKitGroupHolder(), 54, title + " - " + pagetitle);
		        } 
		        if (WkKit.getWkKit().getConfig().getString("GUI.MenuMaterial").equalsIgnoreCase("Default")) {
		        	item_mn = GlassPane.DEFAULT.getItemStack();
		        } else {
		        	item_mn = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.MenuMaterial")));
		        } 
		        // 设置按钮名称
		        item_mn = new ItemEditer(item_mn,LangConfigLoader.getString("DO_NOT_TOUCH")).getItemStack();
		        // 填入物品
		        for (int j = 0; j < 54; j++) {
		          if (!slot.contains(Integer.valueOf(j))) {
		        	  if(j == 1) {
	    				ItemStack is = GlassPane.BLACK.getItemStack();
	    				inv.setItem(j, new ItemEditer(is, LangConfigLoader.getString("EDIT_BACK")).getItemStack());
	    				continue;
		        	  }
		        	 inv.setItem(j, item_mn);  
		          }

		        } 
		        // 如果页数大于1则添加翻页按钮
		      if (guinum > 1) {
		          ItemStack item_pre = new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.TurnPageMaterial")));
		          item_pre = new ItemEditer(item_pre, LangConfigLoader.getString("PREVIOUS_PAGE")).getItemStack();
                  final int currentPage = i;
                  NBT.modify(item_pre, nbt -> { nbt.setInteger("page", currentPage - 1); nbt.setString("groupname", groupname); });
                  ItemStack item_next = new ItemEditer(new ItemStack(Material.getMaterial(WkKit.getWkKit().getConfig().getString("GUI.TurnPageMaterial"))), LangConfigLoader.getString("NEXT_PAGE")).getItemStack();
                  NBT.modify(item_next, nbt -> { nbt.setInteger("page", currentPage + 1); nbt.setString("groupname", groupname); });
		          if (i == 1) {
		            inv.setItem(50, item_next);
		          } else if (i == guinum) {
		            inv.setItem(48, item_pre);
		          } else {
		            inv.setItem(48, item_pre);
		            inv.setItem(50, item_next);
		          } 
		        } 
		        invlist[i - 1] = inv;
		}
		//添加物品到指定的inv
	    int counter = 0;
	    for (int invnum = 0; invnum < guinum; invnum++) {
	      for (int si = 0; si < slot.size() && counter != itemlist.size() && itemlist.size() != 0; si++) {
	        invlist[invnum].setItem(slot.get(si), itemlist.get(counter));
	        counter++;
	      } 
	    } 
	    return invlist[page - 1];
	}
	
	/**
	 * 单个礼包编辑页面
	 * @param kitname
	 * @return
	 */
	public Inventory editKit(String kitname) {
		Kit kit = Kit.getKit(kitname);
		Inventory kitinv = Bukkit.createInventory(new EditKitHolder(), 5*9, LangConfigLoader.getString("EDIT_KIT_TITLE") + " - " + kitname);
		List<Integer> slot = Arrays.asList(0,2,3,5,6,8,13,22,31,40);
		List<Integer> hasflags = Arrays.asList(14,15,16,17,23,24,25,26,32,33,34,35,41,42,43,44);
		List<Integer> nonflags = Arrays.asList(9,10,11,12,18,19,20,21,27,28,29,30,36,37,38,39);
		// 填充物品
		for(int i = 0; i < 45; i++) {
			if(slot.contains(i)) {
				ItemStack is = GlassPane.DEFAULT.getItemStack();
				ItemMeta im = is.getItemMeta();
                assert im != null;
                im.setDisplayName(LangConfigLoader.getString("DO_NOT_TOUCH"));
				is.setItemMeta(im);
				kitinv.setItem(i, is);
				continue;
			}
			if(i == 1) {
				ItemStack is = GlassPane.BLACK.getItemStack();
				ItemMeta im = is.getItemMeta();
                assert im != null;
                im.setDisplayName(LangConfigLoader.getString("EDIT_BACK"));
				is.setItemMeta(im);
				NBT.modify(is, nbt -> {
					nbt.setString("wkkit", kitname);
				});
				kitinv.setItem(i, is);
				continue;
			}
			if(i == 4) {
				kitinv.setItem(i, Kit.getKit(kitname).getKitItem());
				continue;
			}
			if(i == 7) {
				ItemStack is = GlassPane.RED.getItemStack();
				ItemMeta im = is.getItemMeta();
                assert im != null;
                im.setDisplayName(LangConfigLoader.getString("EDIT_DELETE_KIT"));
				is.setItemMeta(im);
				NBT.modify(is, nbt -> {
					nbt.setString("wkkit", kitname);
				});
				kitinv.setItem(i, is);
			}
		}
		// 添加flag
		int hascount = 0;
		int noncount = 0;
		Map<String, Object> flags = kit.getFlags();
		for(Map.Entry<String, Object> flag : flags.entrySet()) {
			String key = flag.getKey();
			Object obj = flag.getValue();
			if(obj != null) {
				if (hascount >= hasflags.size()) continue;
				ItemStack is = new ItemStack(Material.NAME_TAG); // 可能会报错
				ItemMeta im = is.getItemMeta();
				im.setDisplayName("§e§l[√]§f§l " + key);
				if(obj instanceof String)im.setLore(Arrays.asList((String)obj));
				if(obj instanceof Integer) im.setLore(Arrays.asList(String.valueOf((int)obj)));
				if(obj instanceof Boolean) im.setLore(Arrays.asList(String.valueOf((boolean) obj)));
				if(obj instanceof List) im.setLore((List<String>)obj);
				if(obj instanceof ItemStack[]) im.setLore(Arrays.asList(LangConfigLoader.getString("EDIT_CLICK_EDIT")));
				is.setItemMeta(im);
				NBT.modify(is, nbt -> {
					nbt.setString("wkkit", key);
				});
				kitinv.setItem(hasflags.get(hascount),is);
				hascount++;
			}else {
				if (noncount >= nonflags.size()) continue;
				ItemStack is = new ItemStack(Material.NAME_TAG); // 可能会报错
				ItemMeta im = is.getItemMeta();
				im.setDisplayName("§e§l[§a§l+§e§l]§f§l " + key);
				is.setItemMeta(im);
				NBT.modify(is, nbt -> {
					nbt.setString("wkkit", key);
				});
				kitinv.setItem(nonflags.get(noncount),is);
				noncount++;
			}
			
		}
		return kitinv;
	}
	/**
	 * 管理礼包的Item内容
	 * @param kitname
	 * @return
	 */
	public Inventory editKitItem(String kitname) {
		Kit kit = Kit.getKit(kitname);
		Inventory kitinv = Bukkit.createInventory(new EditKitItemHolder(), 5*9, LangConfigLoader.getString("EDIT_KIT_ITEM_TITLE") + " - " + kitname);
		// 填充物品
		kitinv.addItem(kit.getItemStacks());
		for(int i = 36; i < 45; i++) {
			if(i == 40) {
				ItemStack is = GlassPane.GREEN.getItemStack();
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(LangConfigLoader.getString("EDIT_SAVE"));
				is.setItemMeta(im);
				NBT.modify(is, nbt -> {
					nbt.setString("wkkit", kitname);
				});
				kitinv.setItem(i, is);
				continue;
			}else {
				ItemStack is = GlassPane.DEFAULT.getItemStack();
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(LangConfigLoader.getString("DO_NOT_TOUCH"));
				is.setItemMeta(im);
				kitinv.setItem(i, is);
				continue;
			}
		}
		return kitinv;
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		// 礼包管理界面
		if(e.getInventory().getHolder() instanceof EditKitMainHolder) {
			e.setCancelled(true);
			if(e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) return;
			if (e.getCurrentItem() == null) return;
			if (ConfigManager.getKitconfig().isDatabaseMode()) {
				if (e.getRawSlot() == 48 || e.getRawSlot() == 50) {
					Integer targetPage = NBT.get(e.getCurrentItem(), (java.util.function.Function<de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT, Integer>) nbt -> nbt.getInteger("page"));
					if (targetPage != null && targetPage > 0) openDatabaseMain((Player) e.getWhoClicked(), targetPage);
					return;
				}
				if (ItemEditer.hasWkKitTag(e.getCurrentItem())) openDatabaseGroup((Player) e.getWhoClicked(), ItemEditer.getWkKitTagValue(e.getCurrentItem()), 1);
				return;
			}
			if (ItemEditer.hasWkKitTag(e.getCurrentItem())) {
				String groupname = e.getCurrentItem().getItemMeta().getDisplayName();
				e.getWhoClicked().openInventory(this.editGroup(groupname,1));
				return;
			}
			return;
		}
		// 礼包组界面
		if(e.getInventory().getHolder() instanceof EditKitGroupHolder) {
		      e.setCancelled(true);
		      if (e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN))return; 
		      if (e.getCurrentItem() == null) return;
		      if (ConfigManager.getKitconfig().isDatabaseMode()) {
			      if (e.getRawSlot() == 1) { openDatabaseMain((Player) e.getWhoClicked(), 1); return; }
			      if (e.getRawSlot() == 48 || e.getRawSlot() == 50) {
				      Integer targetPage = NBT.get(e.getCurrentItem(), (java.util.function.Function<de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT, Integer>) nbt -> nbt.getInteger("page"));
				      String targetGroup = NBT.get(e.getCurrentItem(), (java.util.function.Function<de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT, String>) nbt -> nbt.getString("groupname"));
				      if (targetPage != null && targetPage > 0 && targetGroup != null && !targetGroup.isEmpty()) openDatabaseGroup((Player) e.getWhoClicked(), targetGroup, targetPage);
				      return;
			      }
			      if (ItemEditer.hasWkKitTag(e.getCurrentItem())) {
				      String kitname = ItemEditer.getWkKitTagValue(e.getCurrentItem());
				      if (kitname != null) e.getWhoClicked().openInventory(editKit(kitname));
			      }
			      return;
		      }
		      // 如果存在数据值wkkit
		      if (ItemEditer.hasWkKitTag(e.getCurrentItem())) {
		        String kitname = ItemEditer.getWkKitTagValue(e.getCurrentItem());
		        if (kitname != null) {
		            e.getWhoClicked().openInventory(editKit(kitname));
		        }
		      } 
		      // 如果是返回按钮
		      if(e.getRawSlot() == 1) {
		    	  e.getWhoClicked().openInventory(new EditKit().getInventory());
		    	  return;
		      }
		      // 如果是按钮
              if (e.getRawSlot() == 48 || e.getRawSlot() == 50) {
                  Integer targetPage = NBT.get(e.getCurrentItem(), (java.util.function.Function<de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT, Integer>) nbt -> nbt.getInteger("page"));
                  String targetGroup = NBT.get(e.getCurrentItem(), (java.util.function.Function<de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT, String>) nbt -> nbt.getString("groupname"));
                  if (targetPage > 0 && targetGroup != null && !targetGroup.isEmpty()) {
                      e.getWhoClicked().openInventory(editGroup(targetGroup, targetPage));
                  }
                  return;
              }
		}
		// 礼包编辑界面
		if(e.getInventory().getHolder() instanceof EditKitHolder) {
			e.setCancelled(true);
			if(e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) return;
			if( ItemEditer.hasWkKitTag(e.getCurrentItem()) && e.getClick().equals(ClickType.LEFT)) {
				String kitName = ItemEditer.getWkKitTagValue(e.getInventory().getItem(1));
				if (kitName != null) {
					String key = ItemEditer.getWkKitTagValue(e.getCurrentItem());
					if (key != null) {
						if(e.getRawSlot() == 1) {
							String groupName = KitGroupManager.getContainName(Kit.getKit(kitName));
							if (groupName != null) {
								e.getWhoClicked().openInventory(this.editGroup(groupName, 1));
								return;
							}
						}
				if(e.getRawSlot() == 7) {
					e.getWhoClicked().closeInventory();
					KitDeletePrompt.newConversation((Player)e.getWhoClicked(), kitName);
					return;
				}
				if(key.equals("DisplayName")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "DisplayName");return;}
				if(key.equals("Icon")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "Icon");return;}
				if(key.equals("Times")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "Times");return;}
				if(key.equals("Delay")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "Delay");return;}
				if(key.equals("Permission")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "Permission");return;}
				if(key.equals("DoCron")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "DoCron");return;}
				if(key.equals("Commands")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "Commands");return;}
				if(key.equals("Lore")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "Lore");return;}
				if(key.equals("Drop")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "Drop");return;}
				if(key.equals("Vault")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "Vault");return;}
				if(key.equals("NoRefreshFirst")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "NoRefreshFirst");return;}
				if(key.equals("MythicMobs")) {e.getWhoClicked().closeInventory();KitFlagPrompt.setFlag((Player)e.getWhoClicked(), kitName, "MythicMobs");return;}
				if(key.equals("Item")) {e.getWhoClicked().openInventory(this.editKitItem(kitName));return;}
			}
				}
			}
			if( ItemEditer.hasWkKitTag(e.getCurrentItem()) && e.getClick().equals(ClickType.RIGHT)) {
				String kitname = ItemEditer.getWkKitTagValue(e.getInventory().getItem(1));
				if (kitname != null) {
					String key = ItemEditer.getWkKitTagValue(e.getCurrentItem());
					if (key != null) {
				List<Integer> hasflags = Arrays.asList(14,15,16,17,23,24,25,26,32,33,34,35,41,42,43,44);
				if(hasflags.contains(e.getRawSlot())) {
					if(key.equals("DisplayName")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "DisplayName");return;}
					if(key.equals("Icon")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "Icon");return;}
					if(key.equals("Times")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "Times");return;}
					if(key.equals("Delay")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "Delay");return;}
					if(key.equals("Permission")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "Permission");return;}
					if(key.equals("DoCron")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "DoCron");return;}
					if(key.equals("Commands")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "Commands");return;}
					if(key.equals("Lore")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "Lore");return;}
					if(key.equals("Drop")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "Drop");return;}
					if(key.equals("Vault")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "Vault");return;}
					if(key.equals("NoRefreshFirst")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "NoRefreshFirst");return;}
					if(key.equals("MythicMobs")) {e.getWhoClicked().closeInventory();KitFlagPrompt.deFlag((Player)e.getWhoClicked(), kitname, "MythicMobs");return;}
				}else {
					return;
						}
					}
				}
			}
		}
		if(e.getInventory().getHolder() instanceof EditKitItemHolder) {
			if(e.getAction().equals(NOTHING) || e.getAction().equals(UNKNOWN)) return;
			if(e.getRawSlot() > 35 && e.getRawSlot() < 45) {
				e.setCancelled(true);
				if(e.getRawSlot() == 40) {
					String kitName = ItemEditer.getWkKitTagValue(e.getCurrentItem());
					if (kitName != null) {
						Kit kit = Kit.getKit(kitName);
						if (kit != null) {
					List<ItemStack> list = new ArrayList<ItemStack>();
					for(int i = 0;i < 36; i++) {
						if(e.getInventory().getItem(i) == null) continue;
						list.add(e.getInventory().getItem(i));
					}
					kit.setItemStack(list.toArray(new ItemStack[list.size()]));
					kit.saveConfig();
					e.getWhoClicked().openInventory(this.editKit(kit.getKitname()));
					e.getWhoClicked().sendMessage(LangConfigLoader.getStringWithPrefix("SAVE_SUCCESS", ChatColor.GREEN));
						}
					}
				}
			}

		}
	}

	@EventHandler
	public void invalidatePendingPageOnClose(InventoryCloseEvent event) {
		AtomicLong requests = PAGE_REQUESTS.get(event.getPlayer().getUniqueId());
		if (requests != null) requests.incrementAndGet();
	}
	
}
