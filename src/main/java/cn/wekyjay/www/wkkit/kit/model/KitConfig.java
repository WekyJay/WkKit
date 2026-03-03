package cn.wekyjay.www.wkkit.kit.model;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Kit配置类 - 不可变对象
 * 包含礼包的所有配置信息
 */
public final class KitConfig {
    
    private final String icon;
    private final Integer customModelId;
    private final List<String> commands;
    private final List<String> lore;
    private final List<String> drop;
    private final String permission;
    private final String cronExpression;
    private final Integer delaySeconds;
    private final Integer maxUses;
    private final Integer vaultAmount;
    private final List<String> mythicMobs;
    private final boolean noRefreshFirst;
    private final boolean autoEquipment;
    
    private KitConfig(Builder builder) {
        this.icon = builder.icon;
        this.customModelId = builder.customModelId;
        this.commands = Collections.unmodifiableList(builder.commands);
        this.lore = Collections.unmodifiableList(builder.lore);
        this.drop = Collections.unmodifiableList(builder.drop);
        this.permission = builder.permission;
        this.cronExpression = builder.cronExpression;
        this.delaySeconds = builder.delaySeconds;
        this.maxUses = builder.maxUses;
        this.vaultAmount = builder.vaultAmount;
        this.mythicMobs = Collections.unmodifiableList(builder.mythicMobs);
        this.noRefreshFirst = builder.noRefreshFirst;
        this.autoEquipment = builder.autoEquipment;
    }
    
    // Getters
    @NotNull
    public String getIcon() {
        return icon != null ? icon : "CHEST";
    }
    
    @Nullable
    public Integer getCustomModelId() {
        return customModelId;
    }
    
    @NotNull
    public List<String> getCommands() {
        return commands;
    }
    
    @NotNull
    public List<String> getLore() {
        return lore;
    }
    
    @NotNull
    public List<String> getDrop() {
        return drop;
    }
    
    @Nullable
    public String getPermission() {
        return permission;
    }
    
    @Nullable
    public String getCronExpression() {
        return cronExpression;
    }
    
    public int getDelaySeconds() {
        return delaySeconds != null ? delaySeconds : 60;
    }
    
    public int getMaxUses() {
        return maxUses != null ? maxUses : -1;
    }
    
    public int getVaultAmount() {
        return vaultAmount != null ? vaultAmount : 0;
    }
    
    @NotNull
    public List<String> getMythicMobs() {
        return mythicMobs;
    }
    
    public boolean isNoRefreshFirst() {
        return noRefreshFirst;
    }
    
    public boolean isAutoEquipment() {
        return autoEquipment;
    }
    
    public boolean hasCustomModel() {
        return customModelId != null;
    }
    
    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }
    
    public boolean hasCron() {
        return cronExpression != null && !cronExpression.isEmpty();
    }
    
    public boolean hasVaultReward() {
        return vaultAmount != null && vaultAmount > 0;
    }
    
    public boolean hasMythicMobs() {
        return mythicMobs != null && !mythicMobs.isEmpty();
    }
    
    public boolean isUnlimitedUses() {
        return getMaxUses() == -1;
    }
    
    // Builder模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String icon = "CHEST";
        private Integer customModelId;
        private List<String> commands = Collections.emptyList();
        private List<String> lore = Collections.emptyList();
        private List<String> drop = Collections.emptyList();
        private String permission;
        private String cronExpression;
        private Integer delaySeconds = 60;
        private Integer maxUses = -1;
        private Integer vaultAmount;
        private List<String> mythicMobs = Collections.emptyList();
        private boolean noRefreshFirst = false;
        private boolean autoEquipment = true;
        
        private Builder() {}
        
        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }
        
        public Builder icon(Material material) {
            this.icon = material.name();
            return this;
        }
        
        public Builder customModelId(Integer customModelId) {
            this.customModelId = customModelId;
            return this;
        }
        
        public Builder commands(List<String> commands) {
            this.commands = commands != null ? commands : Collections.emptyList();
            return this;
        }
        
        public Builder lore(List<String> lore) {
            this.lore = lore != null ? lore : Collections.emptyList();
            return this;
        }
        
        public Builder drop(List<String> drop) {
            this.drop = drop != null ? drop : Collections.emptyList();
            return this;
        }
        
        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }
        
        public Builder cronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }
        
        public Builder delaySeconds(Integer delaySeconds) {
            this.delaySeconds = delaySeconds;
            return this;
        }
        
        public Builder delayMinutes(Integer delayMinutes) {
            this.delaySeconds = delayMinutes != null ? delayMinutes * 60 : 60;
            return this;
        }
        
        public Builder maxUses(Integer maxUses) {
            this.maxUses = maxUses;
            return this;
        }
        
        public Builder unlimitedUses() {
            this.maxUses = -1;
            return this;
        }
        
        public Builder vaultAmount(Integer vaultAmount) {
            this.vaultAmount = vaultAmount;
            return this;
        }
        
        public Builder mythicMobs(List<String> mythicMobs) {
            this.mythicMobs = mythicMobs != null ? mythicMobs : Collections.emptyList();
            return this;
        }
        
        public Builder noRefreshFirst(boolean noRefreshFirst) {
            this.noRefreshFirst = noRefreshFirst;
            return this;
        }
        
        public Builder autoEquipment(boolean autoEquipment) {
            this.autoEquipment = autoEquipment;
            return this;
        }
        
        public KitConfig build() {
            return new KitConfig(this);
        }
    }
    
    // equals和hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KitConfig kitConfig = (KitConfig) o;
        return noRefreshFirst == kitConfig.noRefreshFirst &&
               autoEquipment == kitConfig.autoEquipment &&
               Objects.equals(icon, kitConfig.icon) &&
               Objects.equals(customModelId, kitConfig.customModelId) &&
               Objects.equals(commands, kitConfig.commands) &&
               Objects.equals(lore, kitConfig.lore) &&
               Objects.equals(drop, kitConfig.drop) &&
               Objects.equals(permission, kitConfig.permission) &&
               Objects.equals(cronExpression, kitConfig.cronExpression) &&
               Objects.equals(delaySeconds, kitConfig.delaySeconds) &&
               Objects.equals(maxUses, kitConfig.maxUses) &&
               Objects.equals(vaultAmount, kitConfig.vaultAmount) &&
               Objects.equals(mythicMobs, kitConfig.mythicMobs);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(icon, customModelId, commands, lore, drop, permission, 
                           cronExpression, delaySeconds, maxUses, vaultAmount, 
                           mythicMobs, noRefreshFirst, autoEquipment);
    }
    
    @Override
    public String toString() {
        return "KitConfig{" +
               "icon='" + icon + '\'' +
               ", customModelId=" + customModelId +
               ", commands=" + commands.size() +
               ", lore=" + lore.size() +
               ", drop=" + drop.size() +
               ", permission='" + permission + '\'' +
               ", cronExpression='" + cronExpression + '\'' +
               ", delaySeconds=" + delaySeconds +
               ", maxUses=" + maxUses +
               ", vaultAmount=" + vaultAmount +
               ", mythicMobs=" + mythicMobs.size() +
               ", noRefreshFirst=" + noRefreshFirst +
               ", autoEquipment=" + autoEquipment +
               '}';
    }
}