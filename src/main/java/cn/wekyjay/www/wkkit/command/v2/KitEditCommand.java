package cn.wekyjay.www.wkkit.command.v2;

import cn.wekyjay.www.wkkit.kit.KitLoader;
import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 新版礼包编辑命令
 * 支持修改礼包的各种属性
 */
public class KitEditCommand extends BaseCommand {
    
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        // 检查参数
        if (args.length < 4) {
            sendUsage(sender);
            return false;
        }
        
        // 检查权限
        if (!checkPermission(sender)) {
            return false;
        }
        
        String kitName = args[1];
        String action = args[2].toLowerCase();
        
        // 加载礼包
        Kit kit = loadKit(sender, kitName);
        if (kit == null) {
            return false;
        }
        
        // 执行编辑操作
        return handleEditAction(sender, kit, action, args);
    }
    
    @Override
    public String getPermission() {
        return "wkkit.edit";
    }
    
    @Override
    public String getUsage() {
        return "/wkkit edit <礼包名> <操作> [参数...]";
    }
    
    @Override
    public String getDescription() {
        return "编辑礼包属性 (add/remove/set)";
    }
    
    /**
     * 处理编辑操作
     */
    private boolean handleEditAction(@NotNull CommandSender sender, @NotNull Kit kit, @NotNull String action, @NotNull String[] args) {
        switch (action) {
            case "add":
            case "a":
                return handleAddAction(sender, kit, args);
            case "remove":
            case "rm":
                return handleRemoveAction(sender, kit, args);
            case "set":
            case "s":
                return handleSetAction(sender, kit, args);
            case "icon":
            case "i":
                return handleIconAction(sender, kit, args);
            default:
                sendError(sender, "未知的编辑操作: " + action);
                sendInfo(sender, "可用操作: add, remove, set, icon");
                return false;
        }
    }
    
    /**
     * 处理添加操作
     */
    private boolean handleAddAction(@NotNull CommandSender sender, @NotNull Kit kit, @NotNull String[] args) {
        if (args.length < 4) {
            sendError(sender, "缺少添加类型");
            sendInfo(sender, "用法: /wkkit edit <礼包名> add <item|command> [参数]");
            return false;
        }
        
        String addType = args[3].toLowerCase();
        
        switch (addType) {
            case "item":
            case "i":
                return addItem(sender, kit, args);
            case "command":
            case "cmd":
            case "c":
                return addCommand(sender, kit, args);
            case "lore":
            case "l":
                return addLore(sender, kit, args);
            default:
                sendError(sender, "未知的添加类型: " + addType);
                return false;
        }
    }
    
    /**
     * 添加物品到礼包
     */
    private boolean addItem(@NotNull CommandSender sender, @NotNull Kit kit, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendError(sender, "只有玩家可以添加手持物品");
            return false;
        }
        
        Player player = (Player) sender;
        
        if (player.getInventory().getItemInMainHand().getType().isAir()) {
            sendError(sender, "请手持要添加的物品");
            return false;
        }
        
        try {
            // 这里需要调用 KitLoader 的方法添加物品
            // 由于 Kit 模型是不可变的，需要重新创建配置
            // 简化实现：提示用户使用 create 命令重新创建
            sendInfo(sender, "添加物品功能需要重新设计...");
            sendInfo(sender, "临时方案：使用 /wkkit create " + kit.getId() + " 重新配置");
            
            // 显示当前手持物品信息
            player.sendMessage(ChatColor.YELLOW + "手持物品: " + 
                player.getInventory().getItemInMainHand().getType().name());
            
            return true;
            
        } catch (Exception e) {
            ExceptionHandler.handle("添加物品到礼包", e);
            sendError(sender, "添加物品失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 添加命令到礼包
     */
    private boolean addCommand(@NotNull CommandSender sender, @NotNull Kit kit, @NotNull String[] args) {
        if (args.length < 5) {
            sendError(sender, "缺少命令内容");
            sendInfo(sender, "用法: /wkkit edit <礼包名> add command <命令内容>");
            return false;
        }
        
        // 拼接命令（支持空格）
        StringBuilder commandBuilder = new StringBuilder();
        for (int i = 4; i < args.length; i++) {
            if (i > 4) commandBuilder.append(" ");
            commandBuilder.append(args[i]);
        }
        String command = commandBuilder.toString();
        
        sendInfo(sender, "添加命令到礼包 \"" + kit.getDisplayName() + "\":");
        sender.sendMessage(ChatColor.GRAY + "  " + command);
        sendInfo(sender, "注意：需要通过配置文件手动添加此命令");
        
        return true;
    }
    
    /**
     * 添加描述到礼包
     */
    private boolean addLore(@NotNull CommandSender sender, @NotNull Kit kit, @NotNull String[] args) {
        if (args.length < 5) {
            sendError(sender, "缺少描述内容");
            return false;
        }
        
        // 拼接描述
        StringBuilder loreBuilder = new StringBuilder();
        for (int i = 4; i < args.length; i++) {
            if (i > 4) loreBuilder.append(" ");
            loreBuilder.append(args[i]);
        }
        String lore = loreBuilder.toString();
        
        sendInfo(sender, "添加描述到礼包 \"" + kit.getDisplayName() + "\":");
        sender.sendMessage(ChatColor.GRAY + "  " + lore);
        sendInfo(sender, "注意：需要通过配置文件手动添加此描述");
        
        return true;
    }
    
    /**
     * 处理移除操作
     */
    private boolean handleRemoveAction(@NotNull CommandSender sender, @NotNull Kit kit, @NotNull String[] args) {
        if (args.length < 4) {
            sendError(sender, "缺少移除类型");
            sendInfo(sender, "用法: /wkkit edit <礼包名> remove <item|command|lore> [索引]");
            return false;
        }
        
        String removeType = args[3].toLowerCase();
        
        sendInfo(sender, "移除功能需要通过配置文件操作");
        sendInfo(sender, "请编辑 plugins/WkKit/Kits/" + kit.getId() + ".yml");
        
        return true;
    }
    
    /**
     * 处理设置操作
     */
    private boolean handleSetAction(@NotNull CommandSender sender, @NotNull Kit kit, @NotNull String[] args) {
        if (args.length < 5) {
            sendError(sender, "缺少设置项和值");
            sendInfo(sender, "用法: /wkkit edit <礼包名> set <属性> <值>");
            return false;
        }
        
        String property = args[3].toLowerCase();
        String value = args[4];
        
        switch (property) {
            case "name":
            case "displayname":
            case "n":
                sendInfo(sender, "设置显示名称: " + value);
                sendInfo(sender, "注意：需要通过配置文件修改");
                return true;
                
            case "delay":
            case "cooldown":
            case "d":
                try {
                    int seconds = Integer.parseInt(value);
                    sendInfo(sender, "设置冷却时间: " + seconds + " 秒");
                    sendInfo(sender, "注意：需要通过配置文件修改");
                    return true;
                } catch (NumberFormatException e) {
                    sendError(sender, "无效的数值: " + value);
                    return false;
                }
                
            case "permission":
            case "perm":
            case "p":
                sendInfo(sender, "设置权限: " + value);
                sendInfo(sender, "注意：需要通过配置文件修改");
                return true;
                
            case "maxuses":
            case "uses":
            case "u":
                try {
                    int uses = Integer.parseInt(value);
                    if (uses < 0) {
                        sendInfo(sender, "设置使用限制: 无限次 (-1)");
                    } else {
                        sendInfo(sender, "设置使用限制: " + uses + " 次");
                    }
                    sendInfo(sender, "注意：需要通过配置文件修改");
                    return true;
                } catch (NumberFormatException e) {
                    sendError(sender, "无效的数值: " + value);
                    return false;
                }
                
            default:
                sendError(sender, "未知的属性: " + property);
                sendInfo(sender, "可用属性: name, delay, permission, maxuses");
                return false;
        }
    }
    
    /**
     * 处理图标操作
     */
    private boolean handleIconAction(@NotNull CommandSender sender, @NotNull Kit kit, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendError(sender, "只有玩家可以设置图标");
            return false;
        }
        
        Player player = (Player) sender;
        
        if (player.getInventory().getItemInMainHand().getType().isAir()) {
            sendError(sender, "请手持要设置为图标的物品");
            return false;
        }
        
        String iconMaterial = player.getInventory().getItemInMainHand().getType().name();
        
        sendInfo(sender, "设置礼包图标: " + iconMaterial);
        sendInfo(sender, "注意：需要通过配置文件修改");
        
        return true;
    }
}
