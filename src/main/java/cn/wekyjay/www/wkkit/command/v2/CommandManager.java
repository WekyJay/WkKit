package cn.wekyjay.www.wkkit.command.v2;

import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 新版命令管理器
 * 统一管理所有v2命令
 */
public class CommandManager implements TabExecutor {
    
    private final Map<String, BaseCommand> commands = new HashMap<>();
    private final Map<String, String> aliases = new HashMap<>();
    
    public CommandManager() {
        registerCommands();
    }
    
    /**
     * 注册所有命令
     */
    private void registerCommands() {
        // 注册命令
        registerCommand("info", new KitInfoCommand());
        registerCommand("give", new KitGiveCommand());
        
        // 注册别名
        registerAlias("info", "查看");
        registerAlias("info", "information");
        registerAlias("info", "详情");
        registerAlias("give", "发放");
        registerAlias("give", "send");
        registerAlias("give", "授予");
    }
    
    /**
     * 注册命令
     */
    private void registerCommand(@NotNull String name, @NotNull BaseCommand command) {
        commands.put(name.toLowerCase(), command);
    }
    
    /**
     * 注册别名
     */
    private void registerAlias(@NotNull String command, @NotNull String alias) {
        aliases.put(alias.toLowerCase(), command.toLowerCase());
    }
    
    /**
     * 获取命令
     */
    @Nullable
    private BaseCommand getCommand(@NotNull String name) {
        String lowerName = name.toLowerCase();
        
        // 直接查找
        BaseCommand command = commands.get(lowerName);
        if (command != null) {
            return command;
        }
        
        // 通过别名查找
        String actualCommand = aliases.get(lowerName);
        if (actualCommand != null) {
            return commands.get(actualCommand);
        }
        
        return null;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        try {
            if (args.length == 0) {
                showHelp(sender);
                return true;
            }
            
            String subCommand = args[0];
            BaseCommand cmd = getCommand(subCommand);
            
            if (cmd == null) {
                sender.sendMessage(ChatColor.RED + "未知命令: " + subCommand);
                showAvailableCommands(sender);
                return false;
            }
            
            // 执行命令（去掉第一个参数，即子命令名）
            String[] cmdArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
            return cmd.execute(sender, cmdArgs);
            
        } catch (Exception e) {
            ExceptionHandler.handle("执行命令: " + Arrays.toString(args), e);
            sender.sendMessage(ChatColor.RED + "执行命令时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                               @NotNull String alias, @NotNull String[] args) {
        try {
            if (args.length == 1) {
                // 返回所有可用的子命令
                return filterCompletions(getAvailableCommands(sender), args[0]);
            }
            
            if (args.length >= 2) {
                String subCommand = args[0];
                BaseCommand cmd = getCommand(subCommand);
                
                if (cmd != null) {
                    // 这里可以添加命令特定的补全逻辑
                    // 暂时返回空列表
                    return Collections.emptyList();
                }
            }
            
            return Collections.emptyList();
            
        } catch (Exception e) {
            ExceptionHandler.handleSilently("命令补全", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 显示帮助信息
     */
    private void showHelp(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== WkKit v2 命令帮助 ===");
        sender.sendMessage(ChatColor.YELLOW + "使用 /wkkit <命令> 来执行命令");
        sender.sendMessage("");
        
        for (Map.Entry<String, BaseCommand> entry : commands.entrySet()) {
            BaseCommand cmd = entry.getValue();
            
            // 检查权限
            if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) {
                continue;
            }
            
            sender.sendMessage(ChatColor.GREEN + entry.getKey() + ChatColor.WHITE + " - " + 
                             ChatColor.AQUA + cmd.getDescription());
            sender.sendMessage(ChatColor.GRAY + "  用法: " + cmd.getUsage());
            
            if (cmd.getPermission() != null) {
                sender.sendMessage(ChatColor.GRAY + "  权限: " + cmd.getPermission());
            }
            
            sender.sendMessage("");
        }
        
        // 显示别名
        if (!aliases.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "命令别名:");
            Map<String, List<String>> aliasMap = new HashMap<>();
            
            for (Map.Entry<String, String> entry : aliases.entrySet()) {
                String actualCmd = entry.getValue();
                aliasMap.computeIfAbsent(actualCmd, k -> new ArrayList<>()).add(entry.getKey());
            }
            
            for (Map.Entry<String, List<String>> entry : aliasMap.entrySet()) {
                sender.sendMessage(ChatColor.GRAY + "  " + entry.getKey() + ": " + 
                                 String.join(", ", entry.getValue()));
            }
        }
    }
    
    /**
     * 显示可用命令
     */
    private void showAvailableCommands(@NotNull CommandSender sender) {
        List<String> available = getAvailableCommands(sender);
        if (!available.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "可用命令: " + String.join(", ", available));
        }
    }
    
    /**
     * 获取用户可用的命令列表
     */
    @NotNull
    private List<String> getAvailableCommands(@NotNull CommandSender sender) {
        List<String> available = new ArrayList<>();
        
        for (Map.Entry<String, BaseCommand> entry : commands.entrySet()) {
            BaseCommand cmd = entry.getValue();
            
            // 检查权限
            if (cmd.getPermission() == null || sender.hasPermission(cmd.getPermission())) {
                available.add(entry.getKey());
            }
        }
        
        // 添加别名
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            String alias = entry.getKey();
            String actualCmd = entry.getValue();
            BaseCommand cmd = commands.get(actualCmd);
            
            if (cmd != null && (cmd.getPermission() == null || sender.hasPermission(cmd.getPermission()))) {
                available.add(alias);
            }
        }
        
        Collections.sort(available);
        return available;
    }
    
    /**
     * 过滤补全列表
     */
    @NotNull
    private List<String> filterCompletions(@NotNull List<String> completions, @NotNull String input) {
        List<String> filtered = new ArrayList<>();
        String lowerInput = input.toLowerCase();
        
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(lowerInput)) {
                filtered.add(completion);
            }
        }
        
        Collections.sort(filtered);
        return filtered;
    }
    
    /**
     * 获取所有注册的命令
     */
    @NotNull
    public Map<String, BaseCommand> getCommands() {
        return new HashMap<>(commands);
    }
    
    /**
     * 获取所有别名
     */
    @NotNull
    public Map<String, String> getAliases() {
        return new HashMap<>(aliases);
    }
}