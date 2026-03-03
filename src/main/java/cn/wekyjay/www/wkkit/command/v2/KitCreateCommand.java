package cn.wekyjay.www.wkkit.command.v2;

import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.kit.model.KitConfig;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 新版礼包创建命令
 * 创建新的礼包
 */
public class KitCreateCommand extends BaseCommand {
    
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
        
        // 检查礼包是否已存在
        if (KitLoader.kitExists(kitName)) {
            sendError(sender, "礼包 " + kitName + " 已存在");
            return false;
        }
        
        // 创建默认配置
        KitConfig config = createDefaultConfig(kitName, sender);
        
        // 创建礼包
        return createKit(sender, kitName, config);
    }
    
    @Override
    public String getPermission() {
        return "wkkit.create";
    }
    
    @Override
    public String getUsage() {
        return "/wkkit create <礼包名>";
    }
    
    @Override
    public String getDescription() {
        return "创建新的礼包";
    }
    
    /**
     * 创建默认配置
     */
    @NotNull
    private KitConfig createDefaultConfig(@NotNull String kitName, @NotNull CommandSender sender) {
        KitConfig.Builder builder = KitConfig.builder();
        
        // 基本配置
        builder.id(kitName)
               .displayName(kitName)
               .icon("DIAMOND")
               .delaySeconds(86400) // 24小时冷却
               .unlimitedUses(true);
        
        // 如果是玩家，使用手中的物品作为图标
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem != null && !handItem.getType().isAir()) {
                builder.icon(handItem.getType().name());
                
                // 如果有自定义模型数据
                if (handItem.hasItemMeta() && handItem.getItemMeta().hasCustomModelData()) {
                    builder.customModelId(handItem.getItemMeta().getCustomModelData());
                }
            }
        }
        
        // 默认描述
        List<String> lore = new ArrayList<>();
        lore.add("这是一个新创建的礼包");
        lore.add("使用 /wkkit edit " + kitName + " 进行编辑");
        builder.lore(lore);
        
        return builder.build();
    }
    
    /**
     * 创建礼包
     */
    private boolean createKit(@NotNull CommandSender sender, @NotNull String kitName, @NotNull KitConfig config) {
        try {
            // 创建礼包
            Kit.Builder kitBuilder = Kit.builder();
            kitBuilder.id(kitName)
                     .displayName(config.getDisplayName())
                     .config(config)
                     .createdAt(System.currentTimeMillis())
                     .createdBy(sender.getName());
            
            Kit kit = kitBuilder.build();
            
            // 验证礼包
            var validationResult = KitValidator.validateKit(kit);
            if (!validationResult.isValid()) {
                sendError(sender, "礼包验证失败:");
                for (String error : validationResult.getErrors()) {
                    sendError(sender, "  - " + error);
                }
                return false;
            }
            
            // 保存礼包配置
            boolean saved = KitLoader.saveKit(kit);
            if (!saved) {
                sendError(sender, "保存礼包配置失败");
                return false;
            }
            
            sendSuccess(sender, "成功创建礼包: " + kitName);
            sendInfo(sender, "使用 /wkkit edit " + kitName + " 进行详细配置");
            sendInfo(sender, "使用 /wkkit info " + kitName + " 查看礼包信息");
            
            return true;
            
        } catch (Exception e) {
            ExceptionHandler.handle("创建礼包", e);
            sendError(sender, "创建礼包失败: " + e.getMessage());
            return false;
        }
    }
}