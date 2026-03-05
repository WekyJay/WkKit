package cn.wekyjay.www.wkkit.command.v2;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.kit.KitCache;
import cn.wekyjay.www.wkkit.kit.KitLoader;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * 新版礼包重载命令
 * 重新加载所有礼包配置
 */
public class KitReloadCommand extends BaseCommand {
    
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        // 检查权限
        if (!checkPermission(sender)) {
            return false;
        }
        
        boolean forceReload = args.length > 1 && "--force".equalsIgnoreCase(args[1]);
        
        return reloadKits(sender, forceReload);
    }
    
    @Override
    public String getPermission() {
        return "wkkit.reload";
    }
    
    @Override
    public String getUsage() {
        return "/wkkit reload [--force]";
    }
    
    @Override
    public String getDescription() {
        return "重载所有礼包配置 (--force强制清空缓存)";
    }
    
    /**
     * 重载礼包配置
     */
    private boolean reloadKits(@NotNull CommandSender sender, boolean forceReload) {
        try {
            sendInfo(sender, "开始重载礼包配置...");
            
            // 清空缓存
            if (forceReload) {
                sendInfo(sender, "强制清空缓存...");
            }
            KitCache.clear();
            
            // 重新加载配置
            long startTime = System.currentTimeMillis();
            KitLoader.reloadAllKits();
            long loadTime = System.currentTimeMillis() - startTime;
            
            // 重新加载玩家数据
            WkKit.getInstance().reloadPlayerData();
            
            // 发送成功消息
            int kitCount = KitLoader.getKitCount();
            sendSuccess(sender, "成功重载 " + kitCount + " 个礼包配置");
            sendInfo(sender, "耗时: " + loadTime + "ms");
            
            if (forceReload) {
                sendInfo(sender, "缓存已清空并重建");
            }
            
            // 记录日志
            ExceptionHandler.handleSilently("礼包配置重载 操作者: " + sender.getName(), 
                new Exception("重载时间: " + System.currentTimeMillis() + ", 礼包数: " + kitCount));
            
            return true;
            
        } catch (Exception e) {
            ExceptionHandler.handle("重载礼包配置", e);
            sendError(sender, "重载配置失败: " + e.getMessage());
            return false;
        }
    }
}
