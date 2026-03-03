package cn.wekyjay.www.wkkit.command.v2;

import cn.wekyjay.www.wkkit.kit.KitLoader;
import cn.wekyjay.www.wkkit.kit.KitService;
import cn.wekyjay.www.wkkit.kit.KitValidator;
import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 新版命令处理器基类
 * 使用新的Kit模型架构
 */
public abstract class BaseCommand {
    
    protected final KitService kitService;
    
    protected BaseCommand() {
        this.kitService = KitService.getInstance();
    }
    
    /**
     * 执行命令
     * @param sender 命令发送者
     * @param args 参数
     * @return 是否执行成功
     */
    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String[] args);
    
    /**
     * 获取命令权限
     * @return 权限字符串，null表示不需要权限
     */
    @Nullable
    public abstract String getPermission();
    
    /**
     * 获取命令用法
     * @return 用法说明
     */
    @NotNull
    public abstract String getUsage();
    
    /**
     * 获取命令描述
     * @return 描述
     */
    @NotNull
    public abstract String getDescription();
    
    /**
     * 检查玩家是否在线
     */
    protected boolean checkPlayerOnline(@NotNull CommandSender sender, @NotNull String playerName) {
        Player player = sender.getServer().getPlayer(playerName);
        if (player == null || !player.isOnline()) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 不在线或不存在");
            return false;
        }
        return true;
    }
    
    /**
     * 检查玩家实例
     */
    protected boolean checkPlayerInstance(@NotNull CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "该命令只能由玩家执行");
            return false;
        }
        return true;
    }
    
    /**
     * 检查权限
     */
    protected boolean checkPermission(@NotNull CommandSender sender) {
        String permission = getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限");
            return false;
        }
        return true;
    }
    
    /**
     * 检查参数数量
     */
    protected boolean checkArgs(@NotNull CommandSender sender, @NotNull String[] args, int minArgs) {
        if (args.length < minArgs) {
            sender.sendMessage(ChatColor.RED + "用法: " + getUsage());
            return false;
        }
        return true;
    }
    
    /**
     * 加载Kit
     */
    @Nullable
    protected Kit loadKit(@NotNull CommandSender sender, @NotNull String kitName) {
        try {
            if (!KitLoader.kitExists(kitName)) {
                sender.sendMessage(ChatColor.RED + "礼包 " + kitName + " 不存在");
                return null;
            }
            
            Kit kit = KitLoader.loadKit(kitName);
            if (kit == null) {
                sender.sendMessage(ChatColor.RED + "加载礼包 " + kitName + " 失败");
                return null;
            }
            
            return kit;
            
        } catch (Exception e) {
            ExceptionHandler.handle("加载礼包: " + kitName, e);
            sender.sendMessage(ChatColor.RED + "加载礼包时发生错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证Kit
     */
    protected boolean validateKit(@NotNull CommandSender sender, @NotNull Kit kit) {
        KitValidator.ValidationResult result = KitValidator.validateKit(kit);
        if (!result.isValid()) {
            sender.sendMessage(ChatColor.RED + "礼包验证失败:");
            for (String error : result.getErrors()) {
                sender.sendMessage(ChatColor.RED + "  - " + error);
            }
            return false;
        }
        
        // 显示警告
        for (String warning : result.getWarnings()) {
            sender.sendMessage(ChatColor.YELLOW + "警告: " + warning);
        }
        
        return true;
    }
    
    /**
     * 发送成功消息
     */
    protected void sendSuccess(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(ChatColor.GREEN + message);
    }
    
    /**
     * 发送错误消息
     */
    protected void sendError(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(ChatColor.RED + message);
    }
    
    /**
     * 发送信息消息
     */
    protected void sendInfo(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(ChatColor.AQUA + message);
    }
    
    /**
     * 格式化列表为字符串
     */
    @NotNull
    protected String formatList(@NotNull List<String> items) {
        if (items.isEmpty()) {
            return "无";
        }
        return String.join(", ", items);
    }
    
    /**
     * 获取玩家对象
     */
    @Nullable
    protected Player getPlayer(@NotNull CommandSender sender, @NotNull String playerName) {
        return sender.getServer().getPlayer(playerName);
    }
    
    /**
     * 获取当前玩家
     */
    @Nullable
    protected Player getCurrentPlayer(@NotNull CommandSender sender) {
        return sender instanceof Player ? (Player) sender : null;
    }
    
    /**
     * 解析整数参数
     */
    protected Integer parseInt(@NotNull String str, @NotNull CommandSender sender) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            sendError(sender, "无效的数字: " + str);
            return null;
        }
    }
    
    /**
     * 解析布尔参数
     */
    protected Boolean parseBoolean(@NotNull String str, @NotNull CommandSender sender) {
        if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes") || str.equals("1")) {
            return true;
        } else if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no") || str.equals("0")) {
            return false;
        } else {
            sendError(sender, "无效的布尔值: " + str + " (使用 true/false)");
            return null;
        }
    }
    
    /**
     * 显示帮助信息
     */
    protected void showHelp(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + getDescription() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "用法: " + getUsage());
        if (getPermission() != null) {
            sender.sendMessage(ChatColor.YELLOW + "权限: " + getPermission());
        }
    }
    
    /**
     * 合并参数
     */
    @NotNull
    protected String mergeArgs(@NotNull String[] args, int startIndex) {
        if (startIndex >= args.length) {
            return "";
        }
        return String.join(" ", Arrays.copyOfRange(args, startIndex, args.length));
    }
}