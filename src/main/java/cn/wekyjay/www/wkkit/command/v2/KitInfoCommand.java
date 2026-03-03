package cn.wekyjay.www.wkkit.command.v2;

import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 新版礼包信息命令
 * 显示礼包详情和预览
 */
public class KitInfoCommand extends BaseCommand {
    
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        // 检查参数
        if (!checkArgs(sender, args, 2)) {
            return false;
        }
        
        // 检查权限
        if (!checkPermission(sender)) {
            return false;
        }
        
        String kitName = args[1];
        
        // 加载礼包
        Kit kit = loadKit(sender, kitName);
        if (kit == null) {
            return false;
        }
        
        // 验证礼包
        if (!validateKit(sender, kit)) {
            return false;
        }
        
        // 显示礼包信息
        showKitInfo(sender, kit);
        
        // 如果是玩家，打开预览界面
        if (sender instanceof Player) {
            openKitPreview((Player) sender, kit);
        }
        
        return true;
    }
    
    @Override
    public String getPermission() {
        return "wkkit.info";
    }
    
    @Override
    public String getUsage() {
        return "/wkkit info <礼包名>";
    }
    
    @Override
    public String getDescription() {
        return "查看礼包信息和预览";
    }
    
    /**
     * 显示礼包详细信息
     */
    private void showKitInfo(@NotNull CommandSender sender, @NotNull Kit kit) {
        sender.sendMessage(ChatColor.GOLD + "=== 礼包信息: " + kit.getDisplayName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + kit.getId());
        sender.sendMessage(ChatColor.YELLOW + "显示名称: " + ChatColor.WHITE + kit.getDisplayName());
        sender.sendMessage(ChatColor.YELLOW + "图标: " + ChatColor.WHITE + kit.getConfig().getIcon());
        
        if (kit.getConfig().hasCustomModel()) {
            sender.sendMessage(ChatColor.YELLOW + "自定义模型ID: " + ChatColor.WHITE + kit.getConfig().getCustomModelId());
        }
        
        sender.sendMessage(ChatColor.YELLOW + "物品数量: " + ChatColor.WHITE + kit.getItemCount());
        sender.sendMessage(ChatColor.YELLOW + "命令数量: " + ChatColor.WHITE + kit.getConfig().getCommands().size());
        
        if (kit.getConfig().hasPermission()) {
            sender.sendMessage(ChatColor.YELLOW + "权限: " + ChatColor.WHITE + kit.getConfig().getPermission());
        }
        
        sender.sendMessage(ChatColor.YELLOW + "冷却时间: " + ChatColor.WHITE + kit.getConfig().getDelaySeconds() + "秒");
        
        if (kit.getConfig().hasLimitedUses()) {
            sender.sendMessage(ChatColor.YELLOW + "使用限制: " + ChatColor.WHITE + kit.getConfig().getMaxUses() + "次");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "使用限制: " + ChatColor.WHITE + "无限次");
        }
        
        if (kit.getConfig().hasCron()) {
            sender.sendMessage(ChatColor.YELLOW + "计划任务: " + ChatColor.WHITE + kit.getConfig().getCronExpression());
        }
        
        if (kit.getConfig().hasEconomyReward()) {
            sender.sendMessage(ChatColor.YELLOW + "经济奖励: " + ChatColor.WHITE + kit.getConfig().getVaultAmount());
        }
        
        if (kit.getConfig().hasMythicMobsReward()) {
            sender.sendMessage(ChatColor.YELLOW + "MythicMobs: " + ChatColor.WHITE + formatList(kit.getConfig().getMythicMobs()));
        }
        
        if (!kit.getConfig().getLore().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "描述:");
            for (String line : kit.getConfig().getLore()) {
                sender.sendMessage(ChatColor.GRAY + "  " + line);
            }
        }
        
        // 显示统计信息
        String stats = kitService.getKitStatistics(kit);
        sender.sendMessage(ChatColor.YELLOW + "统计信息:");
        for (String line : stats.split("\n")) {
            sender.sendMessage(ChatColor.GRAY + "  " + line);
        }
    }
    
    /**
     * 打开礼包预览界面
     */
    private void openKitPreview(@NotNull Player player, @NotNull Kit kit) {
        try {
            List<ItemStack> items = kit.getItems();
            if (items.isEmpty()) {
                sendInfo(player, "该礼包没有物品");
                return;
            }
            
            // 计算GUI大小
            int itemCount = items.size();
            int guiSize = ((itemCount + 8) / 9) * 9; // 向上取整到9的倍数
            guiSize = Math.max(9, Math.min(54, guiSize)); // 限制在9-54之间
            
            // 创建预览界面
            Inventory preview = Bukkit.createInventory(null, guiSize, 
                ChatColor.GOLD + "预览: " + kit.getDisplayName());
            
            // 添加物品
            for (int i = 0; i < Math.min(itemCount, guiSize); i++) {
                preview.setItem(i, items.get(i));
            }
            
            // 打开界面
            player.openInventory(preview);
            sendSuccess(player, "已打开礼包预览界面");
            
        } catch (Exception e) {
            ExceptionHandler.handle("打开礼包预览", e);
            sendError(player, "打开预览界面失败: " + e.getMessage());
        }
    }
}