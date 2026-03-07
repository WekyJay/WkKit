package cn.wekyjay.www.wkkit.performance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;

/**
 * 性能基准测试
 * Day 5: 测试与质量保证
 */
public class PerformanceBenchmark {
    
    private static final int ITERATIONS = 10000;
    
    @Test
    void testKitLoadingPerformance() {
        long startTime = System.nanoTime();
        
        // Simulate loading 1000 kits
        for (int i = 0; i < 1000; i++) {
            KitConfig config = KitConfig.builder()
                .name("kit_" + i)
                .displayName("&6Kit " + i)
                .items(new String[]{"DIAMOND:" + (i % 10 + 1)})
                .build();
            new Kit(config);
        }
        
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        
        // Should complete within 1 second
        assertTrue(duration < 1000, 
            "Kit loading too slow: " + duration + "ms for 1000 kits");
    }
    
    @Test
    void testKitServiceLookupPerformance() {
        KitService service = KitService.getInstance();
        
        // Pre-populate with kits
        for (int i = 0; i < 100; i++) {
            KitConfig config = KitConfig.builder()
                .name("perf_kit_" + i)
                .build();
            service.registerKit(new Kit(config));
        }
        
        // Benchmark lookup
        long startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            service.getKit("perf_kit_" + (i % 100));
        }
        long endTime = System.nanoTime();
        
        long avgNanos = (endTime - startTime) / ITERATIONS;
        
        // Average lookup should be under 1 microsecond
        assertTrue(avgNanos < 1000, 
            "Kit lookup too slow: " + avgNanos + "ns average");
    }
    
    @Test
    void testMemoryEfficiency() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Create many kits
        for (int i = 0; i < 1000; i++) {
            KitConfig config = KitConfig.builder()
                .name("mem_test_" + i)
                .items(new String[]{"ITEM1:1", "ITEM2:2", "ITEM3:3"})
                .commands(new String[]{"cmd1", "cmd2"})
                .build();
            new Kit(config);
        }
        
        runtime.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryPerKit = (memoryAfter - memoryBefore) / 1000;
        
        // Should use less than 1KB per kit on average
        assertTrue(memoryPerKit < 1024,
            "Memory usage too high: " + memoryPerKit + " bytes per kit");
    }
}
