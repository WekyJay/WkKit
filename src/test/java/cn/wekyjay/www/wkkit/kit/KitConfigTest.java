package cn.wekyjay.www.wkkit.kit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * KitConfig 单元测试
 * Day 5: 测试与质量保证
 */
public class KitConfigTest {
    
    private KitConfig validConfig;
    
    @BeforeEach
    void setUp() {
        validConfig = KitConfig.builder()
            .name("test_kit")
            .displayName("&6Test Kit")
            .items(new String[]{"DIAMOND_SWORD:1", "GOLDEN_APPLE:5"})
            .commands(new String[]{"say Hello", "give %player% diamond 1"})
            .cooldown(3600L)
            .maxUses(10)
            .permission("wkkit.kit.test")
            .build();
    }
    
    @Test
    void testBuilderCreatesValidConfig() {
        assertNotNull(validConfig);
        assertEquals("test_kit", validConfig.getName());
        assertEquals("&6Test Kit", validConfig.getDisplayName());
    }
    
    @Test
    void testImmutableConfig() {
        // KitConfig should be immutable
        String[] items = validConfig.getItems();
        items[0] = "modified";
        
        // Original should remain unchanged
        assertEquals("DIAMOND_SWORD:1", validConfig.getItems()[0]);
    }
    
    @Test
    void testCooldownValidation() {
        assertTrue(validConfig.getCooldown() >= 0);
        
        KitConfig zeroCooldown = KitConfig.builder()
            .name("instant")
            .cooldown(0L)
            .build();
        assertEquals(0L, zeroCooldown.getCooldown());
    }
    
    @Test
    void testMaxUsesValidation() {
        assertTrue(validConfig.getMaxUses() > 0);
        
        KitConfig unlimited = KitConfig.builder()
            .name("unlimited")
            .maxUses(-1)
            .build();
        assertEquals(-1, unlimited.getMaxUses());
    }
    
    @Test
    void testPermissionNotNull() {
        assertNotNull(validConfig.getPermission());
        
        KitConfig noPerm = KitConfig.builder()
            .name("free")
            .build();
        assertEquals("", noPerm.getPermission());
    }
}
