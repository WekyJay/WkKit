package cn.wekyjay.www.wkkit.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具类单元测试
 * Day 5: 测试与质量保证
 */
public class UtilTest {
    
    @Test
    void testTimeFormatUtil() {
        // Test seconds to formatted time
        long seconds = 3661; // 1 hour, 1 minute, 1 second
        String formatted = TimeFormatUtil.format(seconds);
        assertNotNull(formatted);
        assertTrue(formatted.contains("1") || formatted.contains("小时"));
    }
    
    @Test
    void testItemParser() {
        // Test valid item format
        String itemStr = "DIAMOND_SWORD:1:SHARPNESS:5";
        // Should parse without exception
        assertDoesNotThrow(() -> ItemParser.parse(itemStr));
    }
    
    @Test
    void testPlaceholderReplacer() {
        String template = "Hello %player%, you have %amount% items!";
        String result = PlaceholderUtil.replace(template, 
            "%player%", "TestPlayer",
            "%amount%", "10"
        );
        assertEquals("Hello TestPlayer, you have 10 items!", result);
    }
    
    @Test
    void testConfigValidator() {
        // Test valid kit name
        assertTrue(ConfigValidator.isValidKitName("valid_kit_name"));
        assertTrue(ConfigValidator.isValidKitName("kit123"));
        
        // Test invalid names
        assertFalse(ConfigValidator.isValidKitName(""));
        assertFalse(ConfigValidator.isValidKitName("kit with spaces"));
        assertFalse(ConfigValidator.isValidKitName("kit!@#$"));
    }
}
