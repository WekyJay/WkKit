package cn.wekyjay.www.wkkit.kit;

import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 礼包缓存管理器
 * 使用 Caffeine 提供高性能缓存
 * 
 * 缓存策略：
 * - 写入后5分钟过期
 * - 最大缓存1000个礼包
 * - 支持异步刷新
 */
public class KitCache {
    
    // 礼包缓存：key=礼包ID, value=Kit对象
    private static Cache<String, Kit> kitCache;
    
    // 所有礼包列表缓存
    private static Cache<String, List<Kit>> allKitsCache;
    
    // 缓存统计
    private static long cacheHits = 0;
    private static long cacheMisses = 0;
    
    static {
        initializeCache();
    }
    
    /**
     * 初始化缓存
     */
    private static void initializeCache() {
        try {
            // 检查是否有 Caffeine 依赖
            Class.forName("com.github.benmanes.caffeine.cache.Caffeine");
            
            kitCache = Caffeine.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .refreshAfterWrite(1, TimeUnit.MINUTES)
                    .recordStats()
                    .build();
            
            allKitsCache = Caffeine.newBuilder()
                    .maximumSize(1)
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .recordStats()
                    .build();
            
        } catch (ClassNotFoundException e) {
            // Caffeine 不可用，使用内存缓存
            ExceptionHandler.handleSilently("Caffeine缓存不可用，使用备用缓存", e);
            initializeFallbackCache();
        }
    }
    
    /**
     * 初始化备用缓存（无外部依赖）
     */
    private static void initializeFallbackCache() {
        // 备用实现：使用 Guava Cache 或 ConcurrentHashMap
        // 简化版本：直接使用 KitLoader 每次都从文件加载
        // 实际项目中应该添加 Guava 依赖
        kitCache = null;
        allKitsCache = null;
    }
    
    /**
     * 从缓存获取礼包
     * @param kitId 礼包ID
     * @return 礼包对象，如果不存在返回null
     */
    @Nullable
    public static Kit get(@NotNull String kitId) {
        if (kitCache == null) {
            cacheMisses++;
            return null; // 缓存未启用，从文件加载
        }
        
        Kit kit = kitCache.getIfPresent(kitId.toLowerCase());
        if (kit != null) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        return kit;
    }
    
    /**
     * 将礼包放入缓存
     * @param kitId 礼包ID
     * @param kit 礼包对象
     */
    public static void put(@NotNull String kitId, @NotNull Kit kit) {
        if (kitCache != null) {
            kitCache.put(kitId.toLowerCase(), kit);
        }
    }
    
    /**
     * 从缓存移除礼包
     * @param kitId 礼包ID
     */
    public static void invalidate(@NotNull String kitId) {
        if (kitCache != null) {
            kitCache.invalidate(kitId.toLowerCase());
        }
    }
    
    /**
     * 清空所有缓存
     */
    public static void clear() {
        if (kitCache != null) {
            kitCache.invalidateAll();
        }
        if (allKitsCache != null) {
            allKitsCache.invalidateAll();
        }
        cacheHits = 0;
        cacheMisses = 0;
    }
    
    /**
     * 获取所有缓存的礼包列表
     * @return 缓存的礼包列表，如果未缓存返回null
     */
    @Nullable
    public static List<Kit> getAllKits() {
        if (allKitsCache == null) {
            cacheMisses++;
            return null;
        }
        
        List<Kit> kits = allKitsCache.getIfPresent("all");
        if (kits != null) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
        return kits;
    }
    
    /**
     * 缓存所有礼包列表
     * @param kits 礼包列表
     */
    public static void putAllKits(@NotNull List<Kit> kits) {
        if (allKitsCache != null) {
            allKitsCache.put("all", kits);
        }
    }
    
    /**
     * 使所有礼包列表缓存失效
     */
    public static void invalidateAllKits() {
        if (allKitsCache != null) {
            allKitsCache.invalidate("all");
        }
    }
    
    /**
     * 获取缓存统计信息
     * @return 统计信息字符串
     */
    @NotNull
    public static String getStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("缓存统计:\n");
        
        long total = cacheHits + cacheMisses;
        double hitRate = total > 0 ? (double) cacheHits / total * 100 : 0;
        
        stats.append("  命中次数: ").append(cacheHits).append("\n");
        stats.append("  未命中次数: ").append(cacheMisses).append("\n");
        stats.append("  命中率: ").append(String.format("%.2f%%", hitRate)).append("\n");
        
        if (kitCache != null) {
            stats.append("  缓存大小: ").append(kitCache.estimatedSize()).append("\n");
        }
        
        return stats.toString();
    }
    
    /**
     * 检查缓存是否启用
     * @return 是否启用
     */
    public static boolean isEnabled() {
        return kitCache != null;
    }
}
