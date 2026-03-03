package cn.wekyjay.www.wkkit.command.v2;

import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 新版礼包发放命令
 * 给玩家发放礼包
 */
public class KitGiveCommand extends BaseCommand {
    
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
        
        String targetPlayerName = args[1];
        String kitName = args.length > 2 ? args[2] : null;
        
        // 获取目标玩家
        Player targetPlayer = getPlayer(sender, targetPlayerName);
        if (targetPlayer == null) {
            sendError(sender, "玩家 " + targetPlayerName + " 不在线或不存在");
            return false;
        }
        
        // 如果没有指定礼包名，显示玩家可领取的礼包
        if (kitName == null) {
            showAvailableKits(sender, targetPlayer);
            return true;
        }
        
        // 加载礼包
        Kit kit = loadKit(sender, kitName);
        if (kit == null) {
            return false;
        }
        
        // 验证礼包
        if (!validateKit(sender, kit)) {
            return false;
        }
        
        // 发放礼包
        return giveKitToPlayer(sender, targetPlayer, kit);
    }
    
    @Override
    public String getPermission() {
        return "wkkit.give";
    }
    
    @Override
    public String getUsage() {
        return "/wkkit give <玩家> [礼包名]";
    }
    
    @Override
    public String getDescription() {
        return "给玩家发放礼包";
    }
    
    /**
     * 显示玩家可领取的礼包
     */
    private void showAvailableKits(@NotNull CommandSender sender, @NotNull Player player) {
        try {
            var availableKits = kitService.getAvailableKits(player);
            
            if (availableKits.isEmpty()) {
                sendInfo(sender, "玩家 " + player.getName() + " 没有可领取的礼包");
                return;
            }
            
            sender.sendMessage(ChatColor.GOLD + "=== " + player.getName() + " 可领取的礼包 ===");
            
            for (Kit kit : availableKits) {
                StringBuilder line = new StringBuilder();
                line.append(ChatColor.YELLOW).append("• ").append(ChatColor.WHITE).append(kit.getDisplayName());
                line.append(ChatColor.GRAY).append(" (").append(kit.getId()).append(")");
                
                // 显示冷却时间
                long cooldown = kitService.getRemainingCooldown(player, kit);
                if (cooldown > 0) {
                    long minutes = cooldown / 60;
                    long seconds = cooldown % 60;
                    line.append(ChatColor.RED).append(" [冷却: ");
                    if (minutes > 0) {
                        line.append(minutes).append("分");
                    }
                    line.append(seconds).append("秒]");
                } else {
                    line.append(ChatColor.GREEN).append(" [可领取]");
                }
                
                sender.sendMessage(line.toString());
            }
            
            sender.sendMessage(ChatColor.GRAY + "使用 " + getUsage() + " 发放指定礼包");
            
        } catch (Exception e) {
            ExceptionHandler.handle("显示可领取礼包", e);
            sendError(sender, "获取可领取礼包失败: " + e.getMessage());
        }
    }
    
    /**
     * 给玩家发放礼包
     */
    private boolean giveKitToPlayer(@NotNull CommandSender sender, @NotNull Player player, @NotNull Kit kit) {
        try {
            // 检查玩家是否可以领取
            var checkResult = kitService.canReceive(player, kit);
            if (!checkResult.isSuccess()) {
                sendError(sender, "玩家 " + player.getName() + " 无法领取礼包: " + checkResult.getMessage());
                return false;
            }
            
            // 发放礼包
            var giveResult = kitService.giveKit(player, kit);
            
            if (giveResult.isSuccess()) {
                sendSuccess(sender, "成功给 " + player.getName() + " 发放礼包: " + kit.getDisplayName());
                sendSuccess(player, "你收到了礼包: " + kit.getDisplayName());
                return true;
            } else {
                sendError(sender, "发放礼包失败: " + giveResult.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            ExceptionHandler.handle("发放礼包给玩家", e);
            sendError(sender, "发放礼包时发生错误: " + e.getMessage());
            return false;
        }
    }
}