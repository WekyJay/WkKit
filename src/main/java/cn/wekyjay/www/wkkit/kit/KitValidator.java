package cn.wekyjay.www.wkkit.kit;

import cn.wekyjay.www.wkkit.kit.model.Kit;
import cn.wekyjay.www.wkkit.kit.model.KitConfig;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Kit数据验证器
 * 负责验证Kit数据的合法性
 */
public class KitValidator {
    
    private static final Pattern VALID_KIT_NAME = Pattern.compile("^[a-zA-Z0-9_-]{1,32}$");
    private static final Pattern VALID_PERMISSION = Pattern.compile("^[a-zA-Z0-9._-]{1,64}$");
    
    private KitValidator() {
        // 工具类，禁止实例化
    }
    
    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        private ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, new ArrayList<>(), new ArrayList<>());
        }
        
        public static ValidationResult invalid(String error) {
            List<String> errors = new ArrayList<>();
            errors.add(error);
            return new ValidationResult(false, errors, new ArrayList<>());
        }
        
        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors, new ArrayList<>());
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public String getErrorSummary() {
            if (errors.isEmpty()) {
                return "验证通过";
            }
            return String.join("; ", errors);
        }
    }
    
    /**
     * 验证Kit名称
     * @param kitName 礼包名称
     * @return 验证结果
     */
    @NotNull
    public static ValidationResult validateKitName(@NotNull String kitName) {
        List<String> errors = new ArrayList<>();
        
        if (kitName == null || kitName.isEmpty()) {
            errors.add("礼包名称不能为空");
            return ValidationResult.invalid(errors);
        }
        
        if (kitName.length() > 32) {
            errors.add("礼包名称长度不能超过32个字符");
        }
        
        if (!VALID_KIT_NAME.matcher(kitName).matches()) {
            errors.add("礼包名称只能包含字母、数字、下划线和连字符");
        }
        
        // 保留名称检查
        if (kitName.equalsIgnoreCase("new") || kitName.equalsIgnoreCase("all") || 
            kitName.equalsIgnoreCase("default") || kitName.equalsIgnoreCase("none")) {
            errors.add("礼包名称不能使用保留名称: " + kitName);
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * 验证显示名称
     * @param displayName 显示名称
     * @return 验证结果
     */
    @NotNull
    public static ValidationResult validateDisplayName(@NotNull String displayName) {
        List<String> errors = new ArrayList<>();
        
        if (displayName == null || displayName.isEmpty()) {
            errors.add("显示名称不能为空");
            return ValidationResult.invalid(errors);
        }
        
        if (displayName.length() > 64) {
            errors.add("显示名称长度不能超过64个字符");
        }
        
        // 检查颜色代码
        if (displayName.contains("&") && !displayName.matches(".*&[0-9a-fk-or].*")) {
            errors.add("显示名称中包含无效的颜色代码");
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * 验证权限字符串
     * @param permission 权限
     * @return 验证结果
     */
    @NotNull
    public static ValidationResult validatePermission(@NotNull String permission) {
        List<String> errors = new ArrayList<>();
        
        if (permission == null || permission.isEmpty()) {
            // 空权限是允许的（表示不需要权限）
            return ValidationResult.valid();
        }
        
        if (permission.length() > 64) {
            errors.add("权限长度不能超过64个字符");
        }
        
        if (!VALID_PERMISSION.matcher(permission).matches()) {
            errors.add("权限只能包含字母、数字、点、下划线和连字符");
        }
        
        // 检查常见错误
        if (permission.startsWith(".") || permission.endsWith(".")) {
            errors.add("权限不能以点开头或结尾");
        }
        
        if (permission.contains("..")) {
            errors.add("权限不能包含连续的点");
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * 验证冷却时间
     * @param delaySeconds 冷却时间（秒）
     * @return 验证结果
     */
    @NotNull
    public static ValidationResult validateDelay(int delaySeconds) {
        List<String> errors = new ArrayList<>();
        
        if (delaySeconds < 0) {
            errors.add("冷却时间不能为负数");
        }
        
        if (delaySeconds > 31536000) { // 1年
            errors.add("冷却时间不能超过1年");
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * 验证使用次数限制
     * @param maxUses 最大使用次数
     * @return 验证结果
     */
    @NotNull
    public static ValidationResult validateMaxUses(int maxUses) {
        List<String> errors = new ArrayList<>();
        
        if (maxUses < -1) {
            errors.add("使用次数不能小于-1（-1表示无限）");
        }
        
        if (maxUses == 0) {
            errors.add("使用次数不能为0（设为-1表示无限，或正整数表示限制次数）");
        }
        
        if (maxUses > 1000000) {
            errors.add("使用次数限制过大");
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * 验证经济奖励
     * @param vaultAmount 经济奖励数量
     * @return 验证结果
     */
    @NotNull
    public static ValidationResult validateVaultAmount(int vaultAmount) {
        List<String> errors = new ArrayList<>();
        
        if (vaultAmount < 0) {
            errors.add("经济奖励不能为负数");
        }
        
        if (vaultAmount > 1000000000) { // 10亿
            errors.add("经济奖励过大");
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * 验证Cron表达式
     * @param cronExpression Cron表达式
     * @return 验证结果
     */
    @NotNull
    public static ValidationResult validateCronExpression(@NotNull String cronExpression) {
        List<String> errors = new ArrayList<>();
        
        if (cronExpression == null || cronExpression.isEmpty()) {
            // 空Cron表达式是允许的（表示不按计划刷新）
            return ValidationResult.valid();
        }
        
        // 基本格式检查
        String[] parts = cronExpression.split(" ");
        if (parts.length < 5 || parts.length > 7) {
            errors.add("Cron表达式格式不正确，应为5-7个字段");
        }
        
        // 检查常见错误
        if (cronExpression.contains("  ")) {
            errors.add("Cron表达式中不能有连续的空格");
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }
        
        // 更详细的验证需要cron-utils库，这里只做基本检查
        return ValidationResult.valid();
    }
    
    /**
     * 完整验证Kit对象
     * @param kit Kit对象
     * @return 验证结果
     */
    @NotNull
    public static ValidationResult validateKit(@NotNull Kit kit) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 验证名称
        ValidationResult nameResult = validateKitName(kit.getId());
        if (!nameResult.isValid()) {
            errors.addAll(nameResult.getErrors());
        }
        
        // 验证显示名称
        ValidationResult displayResult = validateDisplayName(kit.getDisplayName());
        if (!displayResult.isValid()) {
            errors.addAll(displayResult.getErrors());
        }
        
        KitConfig config = kit.getConfig();
        
        // 验证权限
        if (config.hasPermission()) {
            ValidationResult permResult = validatePermission(config.getPermission());
            if (!permResult.isValid()) {
                errors.addAll(permResult.getErrors());
            }
        }
        
        // 验证冷却时间
        ValidationResult delayResult = validateDelay(config.getDelaySeconds());
        if (!delayResult.isValid()) {
            errors.addAll(delayResult.getErrors());
        }
        
        // 验证使用次数
        ValidationResult usesResult = validateMaxUses(config.getMaxUses());
        if (!usesResult.isValid()) {
            errors.addAll(usesResult.getErrors());
        }
        
        // 验证经济奖励
        if (config.hasEconomyReward()) {
            ValidationResult vaultResult = validateVaultAmount(config.getVaultAmount());
            if (!vaultResult.isValid()) {
                errors.addAll(vaultResult.getErrors());
            }
        }
        
        // 验证Cron表达式
        if (config.hasCron()) {
            ValidationResult cronResult = validateCronExpression(config.getCronExpression());
            if (!cronResult.isValid()) {
                errors.addAll(cronResult.getErrors());
            }
        }
        
        // 验证物品
        if (!kit.hasItems()) {
            warnings.add("礼包没有包含任何物品");
        } else if (kit.getItemCount() > 54) {
            warnings.add("礼包物品数量较多（" + kit.getItemCount() + "个），可能会影响性能");
        }
        
        // 检查命令安全性
        for (String command : config.getCommands()) {
            if (command != null && command.trim().toLowerCase().startsWith("op ")) {
                warnings.add("礼包包含OP命令，请谨慎使用: " + command);
            }
        }
        
        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }
        
        if (!warnings.isEmpty()) {
            return new ValidationResult(true, new ArrayList<>(), warnings);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * 验证配置是否安全（用于创建新礼包前的检查）
     * @param config 配置
     * @return 安全验证结果
     */
    @NotNull
    public static ValidationResult validateConfigSafety(@NotNull KitConfig config) {
        List<String> warnings = new ArrayList<>();
        
        // 检查过于频繁的Cron表达式
        if (config.hasCron()) {
            String cron = config.getCronExpression();
            if (cron != null) {
                // 检查是否过于频繁（每分钟或每几分钟）
                if (cron.matches(".*\\*/([1-5]|10).*")) {
                    warnings.add("Cron表达式过于频繁，可能会对服务器性能造成影响");
                }
            }
        }
        
        // 检查非常短的冷却时间
        if (config.getDelaySeconds() < 10) {
            warnings.add("冷却时间过短（" + config.getDelaySeconds() + "秒），玩家可能滥用");
        }
        
        // 检查无限使用但没有冷却时间
        if (config.isUnlimitedUses() && config.getDelaySeconds() == 0) {
            warnings.add("礼包无限使用且没有冷却时间，玩家可能滥用");
        }
        
        if (!warnings.isEmpty()) {
            return new ValidationResult(true, new ArrayList<>(), warnings);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * 获取验证建议
     * @param kit Kit对象
     * @return 改进建议列表
     */
    @NotNull
    public static List<String> getImprovementSuggestions(@NotNull Kit kit) {
        List<String> suggestions = new ArrayList<>();
        KitConfig config = kit.getConfig();
        
        // 没有权限设置
        if (!config.hasPermission() && !kit.isNewComerKit()) {
            suggestions.add("建议为礼包设置权限控制");
        }
        
        // 没有冷却时间
        if (config.getDelaySeconds() == 0 && !config.hasCron()) {
            suggestions.add("建议设置冷却时间或Cron计划，防止玩家滥用");
        }
        
        // 无限使用
        if (config.isUnlimitedUses()) {
            suggestions.add("无限使用礼包建议设置冷却时间");
        }
        
        // 没有描述（Lore）
        if (config.getLore().isEmpty()) {
            suggestions.add("建议为礼包添加描述（Lore）");
        }
        
        // 没有经济奖励但有物品
        if (!config.hasEconomyReward() && kit.hasItems()) {
            suggestions.add("可以考虑添加经济奖励作为补充");
        }
        
        return suggestions;
    }
}