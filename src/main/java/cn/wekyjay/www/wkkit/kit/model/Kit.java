package cn.wekyjay.www.wkkit.kit.model;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

/**
 * 礼包核心数据模型 - 不可变对象
 * 表示一个完整的礼包定义
 */
public final class Kit {
    
    private final String id;
    private final String displayName;
    private final KitConfig config;
    private final List<ItemStack> items;
    private final Instant createdAt;
    private final String creator;
    
    private Kit(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Kit ID cannot be null");
        this.displayName = Objects.requireNonNull(builder.displayName, "Display name cannot be null");
        this.config = Objects.requireNonNull(builder.config, "Kit config cannot be null");
        this.items = Collections.unmodifiableList(new ArrayList<>(builder.items));
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.creator = builder.creator;
        
        // 验证
        if (this.id.isEmpty()) {
            throw new IllegalArgumentException("Kit ID cannot be empty");
        }
        if (this.displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be empty");
        }
    }
    
    // Getters
    @NotNull
    public String getId() {
        return id;
    }
    
    @NotNull
    public String getDisplayName() {
        return displayName;
    }
    
    @NotNull
    public KitConfig getConfig() {
        return config;
    }
    
    @NotNull
    public List<ItemStack> getItems() {
        return items;
    }
    
    @NotNull
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    @Nullable
    public String getCreator() {
        return creator;
    }
    
    // 便捷方法
    public boolean hasItems() {
        return !items.isEmpty();
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    public boolean isNewComerKit() {
        // 可以根据配置判断是否为新人礼包
        return "NCKit".equalsIgnoreCase(id);
    }
    
    public boolean requiresPermission() {
        return config.hasPermission();
    }
    
    public boolean hasCooldown() {
        return config.getDelaySeconds() > 0;
    }
    
    public boolean hasLimitedUses() {
        return !config.isUnlimitedUses();
    }
    
    public boolean hasCronSchedule() {
        return config.hasCron();
    }
    
    public boolean hasEconomyReward() {
        return config.hasVaultReward();
    }
    
    public boolean hasMythicMobsReward() {
        return config.hasMythicMobs();
    }
    
    // 构建器模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder from(@NotNull Kit existing) {
        return new Builder()
                .id(existing.id)
                .displayName(existing.displayName)
                .config(existing.config)
                .items(existing.items)
                .createdAt(existing.createdAt)
                .creator(existing.creator);
    }
    
    public static class Builder {
        private String id;
        private String displayName;
        private KitConfig config;
        private List<ItemStack> items = new ArrayList<>();
        private Instant createdAt;
        private String creator;
        
        private Builder() {}
        
        public Builder id(@NotNull String id) {
            this.id = id;
            return this;
        }
        
        public Builder displayName(@NotNull String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        public Builder config(@NotNull KitConfig config) {
            this.config = config;
            return this;
        }
        
        public Builder items(@NotNull List<ItemStack> items) {
            this.items = new ArrayList<>(items);
            return this;
        }
        
        public Builder addItem(@NotNull ItemStack item) {
            this.items.add(item);
            return this;
        }
        
        public Builder addItems(@NotNull ItemStack... items) {
            Collections.addAll(this.items, items);
            return this;
        }
        
        public Builder createdAt(@NotNull Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder creator(@Nullable String creator) {
            this.creator = creator;
            return this;
        }
        
        @NotNull
        public Kit build() {
            return new Kit(this);
        }
    }
    
    // 静态工厂方法
    @NotNull
    public static Kit createBasic(@NotNull String id, @NotNull String displayName, 
                                  @NotNull List<ItemStack> items) {
        return builder()
                .id(id)
                .displayName(displayName)
                .config(KitConfig.builder().build())
                .items(items)
                .build();
    }
    
    @NotNull
    public static Kit createWithConfig(@NotNull String id, @NotNull String displayName,
                                       @NotNull KitConfig config, @NotNull List<ItemStack> items) {
        return builder()
                .id(id)
                .displayName(displayName)
                .config(config)
                .items(items)
                .build();
    }
    
    // equals和hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kit kit = (Kit) o;
        return Objects.equals(id, kit.id) &&
               Objects.equals(displayName, kit.displayName) &&
               Objects.equals(config, kit.config) &&
               Objects.equals(items, kit.items) &&
               Objects.equals(createdAt, kit.createdAt) &&
               Objects.equals(creator, kit.creator);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, config, items, createdAt, creator);
    }
    
    @Override
    public String toString() {
        return "Kit{" +
               "id='" + id + '\'' +
               ", displayName='" + displayName + '\'' +
               ", config=" + config +
               ", items=" + items.size() +
               ", createdAt=" + createdAt +
               ", creator='" + creator + '\'' +
               '}';
    }
}