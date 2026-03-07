package cn.wekyjay.www.wkkit.kit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * KitService 单元测试
 * Day 5: 测试与质量保证
 */
public class KitServiceTest {
    
    private KitService kitService;
    private Kit testKit;
    
    @BeforeEach
    void setUp() {
        kitService = KitService.getInstance();
        
        KitConfig config = KitConfig.builder()
            .name("test_kit")
            .displayName("&6Test Kit")
            .items(new String[]{"DIAMOND:10"})
            .cooldown(3600L)
            .maxUses(5)
            .build();
        
        testKit = new Kit(config);
    }
    
    @Test
    void testSingletonInstance() {
        KitService instance1 = KitService.getInstance();
        KitService instance2 = KitService.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test
    void testKitRegistration() {
        kitService.registerKit(testKit);
        assertTrue(kitService.hasKit("test_kit"));
        
        Kit retrieved = kitService.getKit("test_kit");
        assertNotNull(retrieved);
        assertEquals("test_kit", retrieved.getConfig().getName());
    }
    
    @Test
    void testDuplicateKitRegistration() {
        kitService.registerKit(testKit);
        
        // Should update existing kit
        KitConfig newConfig = KitConfig.builder()
            .name("test_kit")
            .displayName("&cUpdated")
            .build();
        Kit newKit = new Kit(newConfig);
        
        kitService.registerKit(newKit);
        assertEquals("&cUpdated", kitService.getKit("test_kit").getConfig().getDisplayName());
    }
    
    @Test
    void testGetAllKits() {
        kitService.registerKit(testKit);
        assertFalse(kitService.getAllKits().isEmpty());
    }
    
    @Test
    void testRemoveKit() {
        kitService.registerKit(testKit);
        assertTrue(kitService.hasKit("test_kit"));
        
        kitService.removeKit("test_kit");
        assertFalse(kitService.hasKit("test_kit"));
    }
}
