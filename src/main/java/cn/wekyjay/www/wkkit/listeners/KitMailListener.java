package cn.wekyjay.www.wkkit.listeners;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.api.PlayersReceiveKitEvent;
import cn.wekyjay.www.wkkit.api.ReceiveType;
import cn.wekyjay.www.wkkit.command.KitMail;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.invholder.MailHolder;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.tool.ItemEditer;
import cn.wekyjay.www.wkkit.tool.WKTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static org.bukkit.event.inventory.InventoryAction.NOTHING;
import static org.bukkit.event.inventory.InventoryAction.UNKNOWN;

public class KitMailListener implements Listener {
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        String firstPageTitle = WKTool.replacePlaceholder("page", "1", LangConfigLoader.getString("GUI_PAGETITLE"));
        String title = WKTool.getEventInvTitle(event);
        if (title.equals(LangConfigLoader.getString("KITMAIL_TITLE"))
                || title.equals(LangConfigLoader.getString("KITMAIL_TITLE") + " - " + firstPageTitle)) {
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MailHolder)
                || event.getWhoClicked() != WKTool.getEventInvPlayer(event)) {
            return;
        }

        event.setCancelled(true);
        if (event.getAction() == NOTHING || event.getAction() == UNKNOWN) return;

        Player player = (Player) event.getWhoClicked();
        String playerName = player.getName();
        ItemStack clicked = event.getCurrentItem();

        if (event.getRawSlot() > 8 && event.getRawSlot() < 45
                && clicked != null && ItemEditer.hasWkKitTag(clicked)) {
            String kitName = ItemEditer.getWkKitTagValue(clicked);
            boolean fullyClaimed = claimMailStack(player, playerName, kitName, clicked);
            if (!fullyClaimed) sendInventorySpaceMessage(player);
            refreshMailbox(player);
            return;
        }

        if (event.getRawSlot() == 52) {
            boolean fullyClaimed = true;
            for (ItemStack item : event.getInventory().getContents()) {
                if (item != null && ItemEditer.hasWkKitTag(item)) {
                    String kitName = ItemEditer.getWkKitTagValue(item);
                    if (!claimMailStack(player, playerName, kitName, item)) fullyClaimed = false;
                }
            }
            if (!fullyClaimed) sendInventorySpaceMessage(player);
            refreshMailbox(player);
            return;
        }

        if ((event.getRawSlot() == 48 || event.getRawSlot() == 50) && clicked != null && clicked.hasItemMeta()) {
            String displayName = clicked.getItemMeta().getDisplayName();
            int page = ((MailHolder) event.getInventory().getHolder()).getPage();
            if (displayName.equals(LangConfigLoader.getString("NEXT_PAGE"))) page++;
            else if (displayName.equals(LangConfigLoader.getString("PREVIOUS_PAGE"))) page--;
            else return;
            new KitMail().openKitMail(player, Math.max(1, page));
        }
    }

    /** Deliver one displayed stack and subtract only the amount accepted by the inventory. */
    private boolean claimMailStack(Player player, String playerName, String kitName, ItemStack mailStack) {
        Kit kit = Kit.getKit(kitName);
        if (kit == null) return false;

        PlayersReceiveKitEvent receiveEvent = new PlayersReceiveKitEvent(player, kit, ReceiveType.MAIL);
        Bukkit.getPluginManager().callEvent(receiveEvent);
        if (receiveEvent.isCancelled()) return false;

        int requested = mailStack.getAmount();
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(mailStack.clone());
        int notAdded = leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
        int delivered = requested - notAdded;
        if (delivered > 0) {
            Integer mailCount = WkKit.getPlayerData().getMailKitNum(playerName, kitName);
            int remaining = Math.max(0, (mailCount == null ? 0 : mailCount) - delivered);
            if (remaining == 0) {
                WkKit.getPlayerData().delMailToFile(playerName, kitName);
            } else {
                WkKit.getPlayerData().setMailNum(playerName, kitName, remaining);
            }
        }
        return notAdded == 0;
    }

    private void refreshMailbox(Player player) {
        player.closeInventory();
        new KitMail().openKitMail(player, 1);
    }

    private void sendInventorySpaceMessage(Player player) {
        player.sendMessage(LangConfigLoader.getStringWithPrefix("KIT_GET_FAILED", ChatColor.YELLOW));
    }
}
