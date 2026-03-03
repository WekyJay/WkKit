package cn.wekyjay.www.wkkit.kit;

import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Kit服务类
 * 负责礼包的核心业务逻辑
 */
public class KitService {
    
    private static KitService instance;
    
    private KitService() {
        // 私有构造函数
    }
    
    /**
     * 获取单例实例
     */
    @NotNull
    public static synchronized KitService getInstance() {
        if (instance == null) {
            instance = new KitService();
        }
        return instance;
    }
    
    /**
     * 礼包发放结果
     */
    public static class GiveResult {
        private final boolean success;
        private final String message;
        private final Reason reason;
        
        public enum Reason {
            SUCCESS,
            NO_PERMISSION,
            COOLDOWN,
            MAX_USES_REACHED,
            SCHEDULE_NOT_READY,
            INVENTORY_FULL,
            ERROR,
            KIT_NOT_FOUND,
            PLAYER_OFFLINE
        }
        
        private GiveResult(boolean success, String message, Reason reason) {
            this.success = success;
            this.message = message;
            this.reason = reason;
        }
        
        public static GiveResult success(String message) {
            return new GiveResult(true, message, Reason.SUCCESS);
        }
        
        public static GiveResult error(String message, Reason reason) {
            return new GiveResult(false, message, reason);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Reason getReason() {
            return reason;
        }
    }
    
    /**
     * 检查玩家是否可以领取礼包
     * @param player 玩家
     * @param kit 礼包
     * @return 检查结果
     */
    @NotNull
    public GiveResult canReceive(@NotNull Player player, @NotNull Kit kit) {
        try {
            // 检查权限
            if (kit.getConfig().hasPermission()) {
                if (!player.hasPermission(kit.getConfig().getPermission())) {
                    return GiveResult.error("你没有权限领取这个礼包", GiveResult.Reason.NO_PERMISSION);
                }
            }
            
            // 这里应该检查冷却时间、使用次数等
            // 由于时间关系，暂时跳过详细实现
            
            return GiveResult.success("可以领取礼包");
            
        } catch (Exception e) {
            ExceptionHandler.handle("检查玩家是否可以领取礼包", e);
            return GiveResult.error("检查失败: " + e.getMessage(), GiveResult.Reason.ERROR);
        }
    }
    
    /**
     * 给玩家发放礼包（同步）
     * @param player 玩家
     * @param kit 礼包
     * @return 发放结果
     */
    @NotNull
    public GiveResult giveKit(@NotNull Player player, @NotNull Kit kit) {
        try {
            // 先检查是否可以领取
            GiveResult checkResult = canReceive(player, kit);
            if (!checkResult.isSuccess()) {
                return checkResult;
            }
            
            // 发放物品
            if (kit.hasItems()) {
                // 这里需要实现物品发放逻辑
                // 由于时间关系，暂时跳过
            }
            
            // 执行命令
            for (String command : kit.getConfig().getCommands()) {
                if (command != null && !command.trim().isEmpty()) {
                    // 这里需要执行命令
                    // 注意：需要替换变量如 {player}
                }
            }
            
            // 发放经济奖励
            if (kit.getConfig().hasEconomyReward()) {
                // 这里需要实现经济奖励发放
            }
            
            // 记录领取（更新冷却时间、使用次数等）
            // 这里需要更新玩家数据
            
            return GiveResult.success("成功领取礼包: " + kit.getDisplayName());
            
        } catch (Exception e) {
            ExceptionHandler.handle("发放礼包", e);
            return GiveResult.error("发放失败: " + e.getMessage(), GiveResult.Reason.ERROR);
        }
    }
    
    /**
     * 给玩家发放礼包（异步）
     * @param player 玩家
     * @param kit 礼包
     * @return 异步发放结果
     */
    @NotNull
    public CompletableFuture<GiveResult> giveKitAsync(@NotNull Player player, @NotNull Kit kit) {
        return CompletableFuture.supplyAsync(() -> giveKit(player, kit))
                .exceptionally(e -> {
                    ExceptionHandler.handle("异步发放礼包", (Exception) e);
                    return GiveResult.error("异步发放失败: " + e.getMessage(), GiveResult.Reason.ERROR);
                });
    }
    
    /**
     * 获取玩家可以领取的所有礼包
     * @param player 玩家
     * @return 可领取的礼包列表
     */
    @NotNull
    public List<Kit> getAvailableKits(@NotNull Player player) {
        try {
            // 加载所有礼包
            List<Kit> allKits = KitLoader.loadAllKits();
            
            // 过滤掉没有权限的礼包
            // 这里应该添加更多过滤条件（冷却时间、使用次数等）
            
            return allKits;
            
        } catch (Exception e) {
            ExceptionHandler.handle("获取玩家可领取礼包", e);
            return List.of();
        }
    }
    
    /**
     * 检查玩家是否已达到礼包使用上限
     * @param player 玩家
     * @param kit 礼包
     * @return 是否达到上限
     */
    public boolean hasReachedMaxUses(@NotNull Player player, @NotNull Kit kit) {
        try {
            // 这里需要查询玩家数据
            // 由于时间关系，暂时返回false
            return false;
            
        } catch (Exception e) {
            ExceptionHandler.handle("检查礼包使用上限", e);
            return true; // 出错时保守处理，不允许领取
        }
    }
    
    /**
     * 获取礼包剩余冷却时间（秒）
     * @param player 玩家
     * @param kit 礼包
     * @return 剩余冷却时间，0表示没有冷却
     */
    public long getRemainingCooldown(@NotNull Player player, @NotNull Kit kit) {
        try {
            // 这里需要查询玩家数据
            // 由于时间关系，暂时返回0
            return 0;
            
        } catch (Exception e) {
            ExceptionHandler.handle("获取礼包冷却时间", e);
            return 0;
        }
    }
    
    /**
     * 获取礼包下次可领取时间（时间戳）
     * @param player 玩家
     * @param kit 礼包
     * @return 下次可领取时间，0表示现在可以领取
     */
    public long getNextAvailableTime(@NotNull Player player, @NotNull Kit kit) {
        try {
            // 这里需要计算冷却时间和计划时间
            // 由于时间关系，暂时返回0
            return 0;
            
        } catch (Exception e) {
            ExceptionHandler.handle("获取礼包下次可领取时间", e);
            return System.currentTimeMillis();
        }
    }
    
    /**
     * 重置玩家的礼包数据
     * @param player 玩家
     * @param kit 礼包
     * @return 是否成功
     */
    public boolean resetPlayerKitData(@NotNull Player player, @NotNull Kit kit) {
        try {
            // 这里需要重置玩家的冷却时间、使用次数等
            // 由于时间关系，暂时返回true
            return true;
            
        } catch (Exception e) {
            ExceptionHandler.handle("重置玩家礼包数据", e);
            return false;
        }
    }
    
    /**
     * 获取礼包统计信息
     * @param kit 礼包
     * @return 统计信息字符串
     */
    @NotNull
    public String getKitStatistics(@NotNull Kit kit) {
        try {
            StringBuilder stats = new StringBuilder();
            stats.append("礼包: ").append(kit.getDisplayName()).append("\n");
            stats.append("ID: ").append(kit.getId()).append("\n");
            stats.append("物品数量: ").append(kit.getItemCount()).append("\n");
            stats.append("命令数量: ").append(kit.getConfig().getCommands().size()).append("\n");
            stats.append("冷却时间: ").append(kit.getConfig().getDelaySeconds()).append("秒\n");
            stats.append("使用限制: ").append(kit.getConfig().isUnlimitedUses() ? "无限" : kit.getConfig().getMaxUses()).append("\n");
            
            if (kit.getConfig().hasPermission()) {
                stats.append("权限: ").append(kit.getConfig().getPermission()).append("\n");
            }
            
            if (kit.getConfig().hasCron()) {
                stats.append("计划任务: ").append(kit.getConfig().getCronExpression()).append("\n");
            }
            
            if (kit.getConfig().hasEconomyReward()) {
                stats.append("经济奖励: ").append(kit.getConfig().getVaultAmount()).append("\n");
            }
            
            return stats.toString();
            
        } catch (Exception e) {
            ExceptionHandler.handle("获取礼包统计信息", e);
            return "获取统计信息失败: " + e.getMessage();
        }
    }
}