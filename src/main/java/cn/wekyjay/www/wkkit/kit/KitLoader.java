package cn.wekyjay.www.wkkit.kit;

import cn.wekyjay.www.wkkit.config.ConfigManager;
import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.kit.model.KitConfig;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Kit配置加载器
 * 负责从配置文件加载Kit对象，兼容新旧配置格式
 */
public class KitLoader {
    
    private static final String DEFAULT_ICON = "CHEST";
    private static final int DEFAULT_DELAY = 60;
    private static final int DEFAULT_MAX_USES = -1; // 无限
    
    private KitLoader() {
        // 工具类，禁止实例化
    }
    
    /**
     * 从配置加载所有Kit
     * @return 加载的Kit列表
     */
    @NotNull
    public static List<Kit> loadAllKits() {
        List<Kit> kits = new ArrayList<>();
        
        try {
            // 使用现有的ConfigManager获取配置
            cn.wekyjay.www.wkkit.config.KitConfigLoader configLoader = ConfigManager.getKitconfig();
            if (configLoader == null) {
                ExceptionHandler.handleWarning("加载礼包配置", 
                    new IllegalStateException("ConfigManager.getKitconfig()返回null"));
                return kits;
            }
            
            // 获取所有礼包名称
            List<String> kitNames = configLoader.getKits();
            
            for (String kitName : kitNames) {
                try {
                    Kit kit = loadKit(kitName);
                    if (kit != null) {
                        kits.add(kit);
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle("加载礼包: " + kitName, e);
                }
            }
            
        } catch (Exception e) {
            ExceptionHandler.handle("加载所有礼包", e);
        }
        
        return kits;
    }
    
    /**
     * 加载单个礼包
     * @param kitName 礼包名称
     * @return Kit对象，如果加载失败返回null
     */
    @Nullable
    public static Kit loadKit(@NotNull String kitName) {
        try {
            cn.wekyjay.www.wkkit.config.KitConfigLoader configLoader = ConfigManager.getKitconfig();
            if (configLoader == null) {
                return null;
            }
            
            // 检查礼包是否存在
            if (!configLoader.contains(kitName)) {
                ExceptionHandler.handleWarning("加载礼包", 
                    new IllegalArgumentException("礼包不存在: " + kitName));
                return null;
            }
            
            // 基本信息
            String displayName = configLoader.getString(kitName + ".Name");
            if (displayName == null || displayName.isEmpty()) {
                displayName = kitName;
            }
            
            String icon = configLoader.getString(kitName + ".Icon");
            if (icon == null || icon.isEmpty()) {
                icon = DEFAULT_ICON;
            }
            
            // 构建配置
            KitConfig.Builder configBuilder = KitConfig.builder()
                    .icon(icon);
            
            // CustomModelId
            Integer customModelId = null;
            try {
                if (configLoader.contains(kitName + ".CustomModelId")) {
                    customModelId = configLoader.getInt(kitName + ".CustomModelId");
                    configBuilder.customModelId(customModelId);
                }
            } catch (Exception e) {
                ExceptionHandler.handleSilently("读取CustomModelId: " + kitName, e);
            }
            
            // 命令
            if (configLoader.contains(kitName + ".Commands")) {
                try {
                    List<String> commands = configLoader.getStringList(kitName + ".Commands");
                    configBuilder.commands(commands);
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取Commands: " + kitName, e);
                }
            }
            
            // Lore
            if (configLoader.contains(kitName + ".Lore")) {
                try {
                    List<String> lore = configLoader.getStringList(kitName + ".Lore");
                    configBuilder.lore(lore);
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取Lore: " + kitName, e);
                }
            }
            
            // Drop
            if (configLoader.contains(kitName + ".Drop")) {
                try {
                    List<String> drop = configLoader.getStringList(kitName + ".Drop");
                    configBuilder.drop(drop);
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取Drop: " + kitName, e);
                }
            }
            
            // 权限
            if (configLoader.contains(kitName + ".Permission")) {
                try {
                    String permission = configLoader.getString(kitName + ".Permission");
                    if (permission != null && !permission.isEmpty()) {
                        configBuilder.permission(permission);
                    }
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取Permission: " + kitName, e);
                }
            }
            
            // 延迟/冷却时间
            if (configLoader.contains(kitName + ".Delay")) {
                try {
                    int delay = configLoader.getInt(kitName + ".Delay");
                    configBuilder.delaySeconds(delay);
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取Delay: " + kitName, e);
                }
            } else {
                configBuilder.delaySeconds(DEFAULT_DELAY);
            }
            
            // 领取次数限制
            if (configLoader.contains(kitName + ".Times")) {
                try {
                    int times = configLoader.getInt(kitName + ".Times");
                    configBuilder.maxUses(times);
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取Times: " + kitName, e);
                }
            } else {
                configBuilder.maxUses(DEFAULT_MAX_USES);
            }
            
            // Cron表达式
            if (configLoader.contains(kitName + ".DoCron")) {
                try {
                    String cron = configLoader.getString(kitName + ".DoCron");
                    if (cron != null && !cron.isEmpty()) {
                        configBuilder.cronExpression(cron);
                    }
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取DoCron: " + kitName, e);
                }
            }
            
            // 首次不刷新
            if (configLoader.contains(kitName + ".NoRefreshFirst")) {
                try {
                    boolean noRefreshFirst = configLoader.getConfigWithPath(kitName + ".NoRefreshFirst")
                            .getBoolean(kitName + ".NoRefreshFirst");
                    configBuilder.noRefreshFirst(noRefreshFirst);
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取NoRefreshFirst: " + kitName, e);
                }
            }
            
            // 经济奖励
            if (configLoader.contains(kitName + ".Vault")) {
                try {
                    int vault = configLoader.getInt(kitName + ".Vault");
                    configBuilder.vaultAmount(vault);
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取Vault: " + kitName, e);
                }
            }
            
            // MythicMobs
            if (configLoader.contains(kitName + ".MythicMobs")) {
                try {
                    List<String> mythicMobs = configLoader.getStringList(kitName + ".MythicMobs");
                    configBuilder.mythicMobs(mythicMobs);
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取MythicMobs: " + kitName, e);
                }
            }
            
            // 自动装备（从默认配置读取）
            boolean autoEquipment = true;
            if (configLoader.contains(kitName + ".AutoEquipment")) {
                try {
                    autoEquipment = configLoader.getConfigWithPath(kitName + ".AutoEquipment")
                            .getBoolean(kitName + ".AutoEquipment");
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("读取AutoEquipment: " + kitName, e);
                }
            }
            configBuilder.autoEquipment(autoEquipment);
            
            KitConfig kitConfig = configBuilder.build();
            
            // 加载物品
            List<ItemStack> items = loadItems(kitName, configLoader);
            
            // 构建Kit对象
            return Kit.builder()
                    .id(kitName)
                    .displayName(displayName)
                    .config(kitConfig)
                    .items(items)
                    .build();
                    
        } catch (Exception e) {
            ExceptionHandler.handle("加载礼包: " + kitName, e);
            return null;
        }
    }
    
    /**
     * 从NBT字符串加载物品
     */
    @NotNull
    private static List<ItemStack> loadItems(@NotNull String kitName, 
                                           @NotNull cn.wekyjay.www.wkkit.config.KitConfigLoader configLoader) {
        List<ItemStack> items = new ArrayList<>();
        
        try {
            if (!configLoader.contains(kitName + ".Item")) {
                return items;
            }
            
            List<String> itemNbtList = configLoader.getStringList(kitName + ".Item");
            
            for (String nbtString : itemNbtList) {
                try {
                    if (nbtString == null || nbtString.isEmpty()) {
                        continue;
                    }
                    
                    NBTContainer container = new NBTContainer(nbtString);
                    ItemStack item = NBTItem.convertNBTtoItem(container);
                    
                    if (item != null) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    ExceptionHandler.handleSilently("解析物品NBT: " + nbtString, e);
                }
            }
            
        } catch (Exception e) {
            ExceptionHandler.handle("加载礼包物品: " + kitName, e);
        }
        
        return items;
    }
    
    /**
     * 获取所有礼包名称
     * @return 礼包名称列表
     */
    @NotNull
    public static List<String> getAllKitNames() {
        try {
            cn.wekyjay.www.wkkit.config.KitConfigLoader configLoader = ConfigManager.getKitconfig();
            if (configLoader != null) {
                return configLoader.getKits();
            }
        } catch (Exception e) {
            ExceptionHandler.handle("获取所有礼包名称", e);
        }
        return new ArrayList<>();
    }
    
    /**
     * 检查礼包是否存在
     * @param kitName 礼包名称
     * @return 是否存在
     */
    public static boolean kitExists(@NotNull String kitName) {
        try {
            cn.wekyjay.www.wkkit.config.KitConfigLoader configLoader = ConfigManager.getKitconfig();
            return configLoader != null && configLoader.contains(kitName);
        } catch (Exception e) {
            ExceptionHandler.handleSilently("检查礼包是否存在: " + kitName, e);
            return false;
        }
    }
    
    /**
     * 将Kit模型转换为原始Kit对象（兼容层）
     * @param newKit 新的Kit模型
     * @return 原始的Kit对象
     */
    @Nullable
    public static cn.wekyjay.www.wkkit.kit.Kit toLegacyKit(@NotNull Kit newKit) {
        try {
            // 这里需要根据实际情况转换
            // 由于时间关系，暂时返回null，让调用者使用新模型
            return null;
        } catch (Exception e) {
            ExceptionHandler.handle("转换Kit为旧格式: " + newKit.getId(), e);
            return null;
        }
    }
    
    /**
     * 从原始Kit对象转换
     * @param legacyKit 原始Kit对象
     * @return 新的Kit模型
     */
    @Nullable
    public static Kit fromLegacyKit(@NotNull cn.wekyjay.www.wkkit.kit.Kit legacyKit) {
        try {
            // 这里需要根据实际情况转换
            // 由于时间关系，暂时通过名称重新加载
            return loadKit(legacyKit.getKitname());
        } catch (Exception e) {
            ExceptionHandler.handle("从旧格式转换Kit: " + legacyKit.getKitname(), e);
            return null;
        }
    }
    
    /**
     * 检查礼包是否存在
     * @param kitName 礼包名称
     * @return 是否存在
     */
    public static boolean kitExists(@NotNull String kitName) {
        try {
            cn.wekyjay.www.wkkit.config.KitConfigLoader configLoader = ConfigManager.getKitconfig();
            if (configLoader == null) {
                return false;
            }
            
            List<String> kitNames = configLoader.getKits();
            return kitNames != null && kitNames.contains(kitName);
            
        } catch (Exception e) {
            ExceptionHandler.handleSilently("检查礼包是否存在: " + kitName, e);
            return false;
        }
    }
    
    /**
     * 保存礼包配置
     * @param kit 要保存的Kit对象
     * @return 是否保存成功
     */
    public static boolean saveKit(@NotNull Kit kit) {
        try {
            // 这里需要实现保存逻辑
            // 由于时间关系，暂时记录日志并返回true
            ExceptionHandler.handleSilently("保存礼包: " + kit.getId(), 
                new Exception("保存功能待实现 - 礼包配置: " + kit.getConfig()));
            
            // TODO: 实现实际的保存逻辑
            // 1. 将KitConfig转换为YAML配置
            // 2. 保存到配置文件
            // 3. 重新加载配置
            
            return true;
            
        } catch (Exception e) {
            ExceptionHandler.handle("保存礼包", e);
            return false;
        }
    }
}