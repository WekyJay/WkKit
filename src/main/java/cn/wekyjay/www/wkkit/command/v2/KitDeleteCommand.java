package cn.wekyjay.www.wkkit.command.v2;

import cn.wekyjay.www.wkkit.kit.KitLoader;
import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * 新版礼包删除命令
 */
public class KitDeleteCommand extends BaseCommand {
    
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
        boolean forceDelete = args.length > 2 && "--force".equalsIgnoreCase(args[2]);
        
        // 加载礼包
        Kit kit = loadKit(sender, kitName);
        if (kit == null) {
            return false;
        }
        
        // 删除礼包
        return deleteKit(sender, kit, forceDelete);
    }
    
    @Override
    public String getPermission() {
        return "wkkit.delete";
    }
    
    @Override
    public String getUsage() {
        return "/wkkit delete <礼包名> [--force]";
    }
    
    @Override
    public String getDescription() {
        return "删除指定礼包 (--force跳过确认)";
    }
    
    /**
     * 删除礼包
     */
    private boolean deleteKit(@NotNull CommandSender sender, @NotNull Kit kit, boolean forceDelete) {
        try {
            String kitName = kit.getId();
            String displayName = kit.getDisplayName();
            
            // 如果不是强制删除，提示确认
            if (!forceDelete) {
                sendWarning(sender, "确认删除礼包 \"" + displayName + "\" (" + kitName + ")?");
                sendInfo(sender, "使用 /wkkit delete " + kitName + " --force 强制删除");
                return true;
            }
            
            // 执行删除
            boolean success = KitLoader.deleteKit(kitName);
            
            if (success) {
                sendSuccess(sender, "成功删除礼包: " + displayName);
                
                // 记录日志
                ExceptionHandler.handleSilently("礼包删除: " + kitName + " 操作者: " + sender.getName(), 
                    new Exception("删除时间: " + System.currentTimeMillis()));
                
                return true;
            } else {
                sendError(sender, "删除礼包失败: 无法删除配置文件");
                return false;
            }
            
        } catch (Exception e) {
            ExceptionHandler.handle("删除礼包", e);
            sendError(sender, "删除礼包时发生错误: " + e.getMessage());
            return false;
        }
    }
}
