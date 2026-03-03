package cn.wekyjay.www.wkkit.command;

import cn.wekyjay.www.wkkit.command.v2.CommandManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * 新版命令执行器
 * 桥接新旧命令系统
 */
public class KitCommandV2 implements CommandExecutor {
    
    private final CommandManager commandManager;
    private final KitCommand legacyCommand;
    
    public KitCommandV2() {
        this.commandManager = new CommandManager();
        this.legacyCommand = new KitCommand();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        try {
            // 如果没有参数，显示帮助
            if (args.length == 0) {
                return legacyCommand.onCommand(sender, command, label, args);
            }
            
            String subCommand = args[0].toLowerCase();
            
            // 检查是否是新版命令支持的命令
            if (commandManager.getCommands().containsKey(subCommand) || 
                commandManager.getAliases().containsKey(subCommand)) {
                // 使用新版命令系统
                return commandManager.onCommand(sender, command, label, args);
            } else {
                // 使用旧版命令系统（逐步迁移）
                return legacyCommand.onCommand(sender, command, label, args);
            }
            
        } catch (Exception e) {
            ExceptionHandler.handle("执行命令: " + String.join(" ", args), e);
            sender.sendMessage(ChatColor.RED + "执行命令时发生错误: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取命令管理器
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }
    
    /**
     * 获取旧版命令
     */
    public KitCommand getLegacyCommand() {
        return legacyCommand;
    }
}