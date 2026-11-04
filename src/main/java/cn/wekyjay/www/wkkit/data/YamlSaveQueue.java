package cn.wekyjay.www.wkkit.data;

import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.handyplus.lib.adapter.HandySchedulerUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;

/** Debounces YAML writes while keeping the live configuration object in memory. */
public final class YamlSaveQueue {
    private static final long SAVE_DELAY_TICKS = 20L;
    private static final Map<FileConfiguration, File> DIRTY = new IdentityHashMap<>();
    private static boolean scheduled;

    private YamlSaveQueue() {}

    public static synchronized void schedule(FileConfiguration configuration, File file) {
        DIRTY.put(configuration, file);
        if (!scheduled) {
            scheduled = true;
            HandySchedulerUtil.runTaskLater(YamlSaveQueue::flush, SAVE_DELAY_TICKS);
        }
    }

    public static synchronized void flush() {
        scheduled = false;
        java.util.Iterator<Map.Entry<FileConfiguration, File>> iterator = DIRTY.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<FileConfiguration, File> entry = iterator.next();
            try {
                entry.getKey().save(entry.getValue());
                iterator.remove();
            } catch (IOException e) {
                ExceptionHandler.handle("保存 YAML 数据文件 " + entry.getValue().getName(), e);
            }
        }
    }
}
