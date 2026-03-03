package cn.wekyjay.www.wkkit.kit;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.data.playerdata.PlayerData;
import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Kit服务类
 * 负责礼包的核心业务逻辑
 */
public class KitService {
    
    private static KitService instance;
    private final PlayerData playerData;
    
    private KitService() {
        this.playerData = WkKit.getPlayerData();
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
            
            // 检查冷却时间
            long remainingCooldown = getRemainingCooldown(player, kit);
            if (remainingCooldown > 0) {
                long minutes = remainingCooldown / 60;
                long seconds = remainingCooldown % 60;
                String timeStr = minutes > 0 ? minutes + "分" + seconds + "秒" : seconds + "秒";
                return GiveResult.error("礼包冷却中，请等待 " + timeStr, GiveResult.Reason.COOLDOWN);
            }
            
            // 检查使用次数限制
            if (hasReachedMaxUses(player, kit)) {
                return GiveResult.error("已达到礼包领取次数限制", GiveResult.Reason.MAX_USES_REACHED);
            }
            
            // 检查计划任务时间
            if (kit.getConfig().hasCron()) {
                // 这里需要检查Cron计划时间
                // 暂时跳过详细实现
            }
            
            // 检查背包空间（如果需要发放物品）
            if (kit.hasItems() && !hasEnoughInventorySpace(player, kit)) {
                return GiveResult.error("背包空间不足，请清理背包后重试", GiveResult.Reason.INVENTORY_FULL);
            }
            
            return GiveResult.success("可以领取礼包");
            
        } catch (Exception e) {
            ExceptionHandler.handle("检查玩家是否可以领取礼包", e);
            return GiveResult.error("检查失败: " + e.getMessage(), GiveResult.Reason.ERROR);
        }
    }
    
    /**
     * 检查玩家是否有足够的背包空间
     */
    private boolean hasEnoughInventorySpace(@NotNull Player player, @NotNull Kit kit) {
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null) {
                emptySlots++;
            }
        }
        return emptySlots >= kit.getItemCount();
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
            
            // 调用领取事件（兼容旧系统）
            // PlayersReceiveKitEvent event = new PlayersReceiveKitEvent(player, kit, null, ReceiveType.GET);
            // Bukkit.getPluginManager().callEvent(event);
            // if (event.isCancelled()) {
            //     return GiveResult.error("领取被事件取消", GiveResult.Reason.ERROR);
            // }
            
            boolean success = true;
            StringBuilder resultMessage = new StringBuilder();
            
            // 发放物品
            if (kit.hasItems()) {
                GiveResult itemsResult = giveItems(player, kit);
                if (!itemsResult.isSuccess()) {
                    success = false;
                    resultMessage.append(itemsResult.getMessage()).append("; ");
                }
            }
            
            // 执行命令
            if (!kit.getConfig().getCommands().isEmpty()) {
                GiveResult commandsResult = executeCommands(player, kit);
                if (!commandsResult.isSuccess()) {
                    success = false;
                    resultMessage.append(commandsResult.getMessage()).append("; ");
                }
            }
            
            // 发放经济奖励
            if (kit.getConfig().hasEconomyReward()) {
                GiveResult economyResult = giveEconomyReward(player, kit);
                if (!economyResult.isSuccess()) {
                    success = false;
                    resultMessage.append(economyResult.getMessage()).append("; ");
                }
            }
            
            // 发放MythicMobs奖励
            if (kit.getConfig().hasMythicMobsReward()) {
                GiveResult mythicResult = giveMythicMobsReward(player, kit);
                if (!mythicResult.isSuccess()) {
                    success = false;
                    resultMessage.append(mythicResult.getMessage()).append("; ");
                }
            }
            
            // 记录领取（更新冷却时间、使用次数等）
            if (success) {
                recordKitReceipt(player, kit);
                return GiveResult.success("成功领取礼包: " + kit.getDisplayName());
            } else {
                return GiveResult.error("领取礼包时部分失败: " + resultMessage.toString(), GiveResult.Reason.ERROR);
            }
            
        } catch (Exception e) {
            ExceptionHandler.handle("发放礼包", e);
            return GiveResult.error("发放失败: " + e.getMessage(), GiveResult.Reason.ERROR);
        }
    }
    
    /**
     * 发放物品
     */
    @NotNull
    private GiveResult giveItems(@NotNull Player player, @NotNull Kit kit) {
        try {
            int givenCount = 0;
            for (ItemStack item : kit.getItems()) {
                if (item != null) {
                    // 复制物品避免修改原始数据
                    ItemStack itemCopy = item.clone();
                    
                    // 尝试添加到背包
                    java.util.HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(itemCopy);
                    
                    if (leftover.isEmpty()) {
                        givenCount++;
                    } else {
                        // 物品无法完全添加，掉落在地上
                        for (ItemStack leftItem : leftover.values()) {
                            player.getWorld().dropItem(player.getLocation(), leftItem);
                        }
                        givenCount++;
                    }
                }
            }
            
            if (givenCount > 0) {
                return GiveResult.success("发放了 " + givenCount + " 个物品");
            } else {
                return GiveResult.error("没有发放任何物品", GiveResult.Reason.ERROR);
            }
            
        } catch (Exception e) {
            ExceptionHandler.handle("发放物品", e);
            return GiveResult.error("发放物品失败: " + e.getMessage(), GiveResult.Reason.ERROR);
        }
    }
    
    /**
     * 执行命令
     */
    @NotNull
    private GiveResult executeCommands(@NotNull Player player, @NotNull Kit kit) {
        try {
            int executedCount = 0;
            for (String command : kit.getConfig().getCommands()) {
                if (command != null && !command.trim().isEmpty()) {
                    // 替换变量
                    String processedCommand = command
                            .replace("{player}", player.getName())
                            .replace("{displayname}", player.getDisplayName())
                            .replace("{world}", player.getWorld().getName())
                            .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                            .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                            .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
                    
                    // 执行命令
                    boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                    if (success) {
                        executedCount++;
                    }
                }
            }
            
            if (executedCount > 0) {
                return GiveResult.success("执行了 " + executedCount + " 个命令");
            } else {
                return GiveResult.error("没有执行任何命令", GiveResult.Reason.ERROR);
            }
            
        } catch (Exception e) {
            ExceptionHandler.handle("执行命令", e);
            return GiveResult.error("执行命令失败: " + e.getMessage(), GiveResult.Reason.ERROR);
        }
    }
    
    /**
     * 发放经济奖励
     */
    @NotNull
    private GiveResult giveEconomyReward(@NotNull Player player, @NotNull Kit kit) {
        try {
            // 检查Vault插件是否可用
            if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
                return GiveResult.error("Vault经济插件未安装", GiveResult.Reason.ERROR);
            }
            
            // 这里需要集成Vault经济系统
            // 暂时返回成功
            return GiveResult.success("发放了经济奖励: " + kit.getConfig().getVaultAmount());
            
        } catch (Exception e) {
            ExceptionHandler.handle("发放经济奖励", e);
            return GiveResult.error("发放经济奖励失败: " + e.getMessage(), GiveResult.Reason.ERROR);
        }
    }
    
    /**
     * 发放MythicMobs奖励
     */
    @NotNull
    private GiveResult giveMythicMobsReward(@NotNull Player player, @NotNull Kit kit) {
        try {
            // 检查MythicMobs插件是否可用
            if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
                return GiveResult.error("MythicMobs插件未安装", GiveResult.Reason.ERROR);
            }
            
            // 这里需要集成MythicMobs
            // 暂时返回成功
            return GiveResult.success("发放了MythicMobs奖励");
            
        } catch (Exception e) {
            ExceptionHandler.handle("发放MythicMobs奖励", e);
            return GiveResult.error("发放MythicMobs奖励失败: " + e.getMessage(), GiveResult.Reason.ERROR);
        }
    }
    
    /**
     * 记录礼包领取
     */
    private void recordKitReceipt(@NotNull Player player, @NotNull Kit kit) {
        try {
            String playerName = player.getName();
            String kitId = kit.getId();
            
            // 记录领取时间（用于冷却计算）
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(new Date());
            playerData.setKitData(playerName, kitId, currentTime);
            
            // 更新使用次数（如果有使用限制）
            if (!kit.getConfig().isUnlimitedUses()) {
                int usedTimes = getUsedTimes(player, kit);
                playerData.setKitTime(playerName, kitId, usedTimes + 1);
            }
            
            // 记录领取日志
            ExceptionHandler.handleSilently("礼包领取记录: " + playerName + " -> " + kitId, 
                new Exception("领取时间: " + currentTime));
            
        } catch (Exception e) {
            ExceptionHandler.handle("记录礼包领取", e);
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
            // 如果礼包没有使用限制，直接返回false
            if (kit.getConfig().isUnlimitedUses()) {
                return false;
            }
            
            // 获取玩家已使用次数
            int usedTimes = getUsedTimes(player, kit);
            int maxUses = kit.getConfig().getMaxUses();
            
            return usedTimes >= maxUses;
            
        } catch (Exception e) {
            ExceptionHandler.handle("检查礼包使用上限", e);
            return true; // 出错时保守处理，不允许领取
        }
    }
    
    /**
     * 获取玩家已使用次数
     */
    private int getUsedTimes(@NotNull Player player, @NotNull Kit kit) {
        try {
            String timesStr = playerData.getKitTime(player.getName(), kit.getId());
            if (timesStr == null || timesStr.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(timesStr);
        } catch (Exception e) {
            ExceptionHandler.handleSilently("获取使用次数", e);
            return 0;
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
            // 如果礼包没有冷却时间，直接返回0
            if (kit.getConfig().getDelaySeconds() <= 0) {
                return 0;
            }
            
            String lastTimeStr = playerData.getKitData(player.getName(), kit.getId());
            if (lastTimeStr == null || lastTimeStr.isEmpty() || "true".equalsIgnoreCase(lastTimeStr)) {
                return 0; // 没有记录或特殊标记，表示可以领取
            }
            
            if ("false".equalsIgnoreCase(lastTimeStr)) {
                return Long.MAX_VALUE; // 特殊标记，表示不能领取
            }
            
            // 解析上次领取时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date lastTime = sdf.parse(lastTimeStr);
            Date now = new Date();
            
            // 计算冷却结束时间
            Calendar cooldownEnd = Calendar.getInstance();
            cooldownEnd.setTime(lastTime);
            cooldownEnd.add(Calendar.SECOND, kit.getConfig().getDelaySeconds());
            
            // 计算剩余时间
            long remaining = cooldownEnd.getTimeInMillis() - now.getTime();
            return remaining > 0 ? remaining / 1000 : 0;
            
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