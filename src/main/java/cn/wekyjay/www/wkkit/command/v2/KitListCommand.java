package cn.wekyjay.www.wkkit.command.v2;

import cn.wekyjay.www.wkkit.kit.KitLoader;
import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 新版礼包列表命令
 * 显示所有礼包及其状态
 */
public class KitListCommand extends BaseCommand {
    
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        // 检查权限
        if (!checkPermission(sender)) {
            return false;
        }
        
        // 解析参数
        boolean showAll = false;
        boolean showStats = false;
        int page = 1;
        
        for (int i = 1; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            switch (arg) {
                case "-a":
                case "--all":
                    showAll = true;
                    break;
                case "-s":
                case "--stats":
                    showStats = true;
                    break;
                default:
                    try {
                        page = Integer.parseInt(arg);
                        if (page < 1) page = 1;
                    } catch (NumberFormatException e) {
                        // 忽略无效参数
                    }
                    break;
            }
        }
        
        // 显示礼包列表
        return showKitList(sender, showAll, showStats, page);
    }
    
    @Override
    public String getPermission() {
        return "wkkit.list";
    }
    
    @Override
    public String getUsage() {
        return "/wkkit list [页码] [-a|--all] [-s|--stats]";
    }
    
    @Override
    public String getDescription() {
        return "列出所有礼包 (-a显示全部, -s显示统计)";
    }
    
    /**
     * 显示礼包列表
     */
    private boolean showKitList(@NotNull CommandSender sender, boolean showAll, boolean showStats, int page) {
        try {
            // 加载所有礼包
            List<Kit> allKits = KitLoader.loadAllKits();
            
            if (allKits.isEmpty()) {
                sendInfo(sender, "当前没有配置任何礼包");
                sendInfo(sender, "使用 /wkkit create <礼包名> 创建新礼包");
                return true;
            }
            
            // 过滤有权限的礼包（如果不是显示全部）
            List<Kit> displayKits = new ArrayList<>();
            if (!showAll && sender instanceof Player) {
                Player player = (Player) sender;
                for (Kit kit : allKits) {
                    if (!kit.getConfig().hasPermission() || 
                        player.hasPermission(kit.getConfig().getPermission()) ||
                        player.hasPermission("wkkit.admin")) {
                        displayKits.add(kit);
                    }
                }
            } else {
                displayKits = allKits;
            }
            
            // 分页
            int kitsPerPage = 10;
            int totalKits = displayKits.size();
            int totalPages = (totalKits + kitsPerPage - 1) / kitsPerPage;
            
            if (page > totalPages) {
                page = totalPages;
            }
            
            int startIndex = (page - 1) * kitsPerPage;
            int endIndex = Math.min(startIndex + kitsPerPage, totalKits);
            
            // 显示头部
            sender.sendMessage(ChatColor.GOLD + "=== 礼包列表 (" + totalKits + "个, 第" + page + "/" + totalPages + "页) ===");
            
            // 显示礼包
            for (int i = startIndex; i < endIndex; i++) {
                Kit kit = displayKits.get(i);
                showKitSummary(sender, kit, showStats);
            }
            
            // 显示分页信息
            if (totalPages > 1) {
                sender.sendMessage(ChatColor.YELLOW + "翻页: " + ChatColor.WHITE + 
                    "/wkkit list " + (page + 1) + (showAll ? " -a" : "") + (showStats ? " -s" : ""));
            }
            
            // 显示提示
            sender.sendMessage(ChatColor.GRAY + "提示: 使用 /wkkit info <礼包名> 查看详情");
            
            return true;
            
        } catch (Exception e) {
            ExceptionHandler.handle("显示礼包列表", e);
            sendError(sender, "获取礼包列表失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 显示单个礼包摘要
     */
    private void showKitSummary(@NotNull CommandSender sender, @NotNull Kit kit, boolean showStats) {
        String kitId = kit.getId();
        String displayName = kit.getDisplayName();
        
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.YELLOW).append("• ").append(ChatColor.WHITE).append(displayName);
        sb.append(ChatColor.GRAY).append(" (").append(kitId).append(")");
        
        // 显示图标
        if (kit.getConfig().getIcon() != null && !kit.getConfig().getIcon().isEmpty()) {
            sb.append(" ").append(ChatColor.GRAY).append("[").append(kit.getConfig().getIcon()).append("]");
        }
        
        sender.sendMessage(sb.toString());
        
        // 显示详细信息（如果启用统计）
        if (showStats) {
            sender.sendMessage(ChatColor.GRAY + "  物品: " + kit.getItemCount() + 
                " | 命令: " + kit.getConfig().getCommands().size() +
                " | 冷却: " + kit.getConfig().getDelaySeconds() + "秒");
            
            if (kit.getConfig().hasPermission()) {
                sender.sendMessage(ChatColor.GRAY + "  权限: " + kit.getConfig().getPermission());
            }
        }
    }
}
