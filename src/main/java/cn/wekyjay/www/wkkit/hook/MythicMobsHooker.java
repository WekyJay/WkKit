package cn.wekyjay.www.wkkit.hook;

import cn.wekyjay.www.wkkit.WkKit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * MythicMobs 挂钩器
 * 使用反射调用 MythicMobs API，避免编译期强依赖
 */
public class MythicMobsHooker {
    public static MythicMobsHooker mythicMobsHooker;
    private static boolean available = false;
    private static Class<?> mythicMobsClass;
    private static Class<?> activeMobClass;

    public MythicMobsHooker() {
        try {
            mythicMobsClass = Class.forName("io.lumine.xikage.mythicmobs.MythicMobs");
            activeMobClass = Class.forName("io.lumine.xikage.mythicmobs.mobs.ActiveMob");
            available = true;
            WkKit.getWkKit().getLogger().info("§7已成功与 §6MythicMobs §7挂钩服务.");
        } catch (ClassNotFoundException e) {
            available = false;
            WkKit.getWkKit().getLogger().warning("§cMythicMobs API未找到，挂钩失败。");
        }
    }

    /**
     * 获取MM怪的Hooker
     */
    public static MythicMobsHooker getMythicMobs(){
        return mythicMobsHooker == null ? mythicMobsHooker = new MythicMobsHooker() : mythicMobsHooker;
    }

    /**
     * 指定MM怪名称在玩家Location生成Mob（反射实现）
     */
    public boolean spawnMob(Player player, String mobName){
        if (!available) return false;
        try {
            // MythicMobs.inst().getMobManager().getMythicMob(mobName)
            Method instMethod = mythicMobsClass.getMethod("inst");
            Object mythicMobs = instMethod.invoke(null);
            Object mobManager = mythicMobsClass.getMethod("getMobManager").invoke(mythicMobs);
            Object mythicMob = mobManager.getClass().getMethod("getMythicMob", String.class).invoke(mobManager, mobName);

            if (mythicMob != null) {
                Location spawnLocation = player.getLocation();
                // mob.spawn(BukkitAdapter.adapt(spawnLocation), 1)
                // 尝试调用 spawn 方法（参数可能是 AdaptedLocation 或 Location）
                try {
                    Method spawnMethod = mythicMob.getClass().getMethod("spawn", Location.class, int.class);
                    spawnMethod.invoke(mythicMob, spawnLocation, 1);
                } catch (NoSuchMethodException e) {
                    // 兼容其他 spawn 签名
                    Method spawnMethod = mythicMob.getClass().getMethod("spawn", Object.class, int.class);
                    spawnMethod.invoke(mythicMob, spawnLocation, 1);
                }
                return true;
            }
        } catch (Exception e) {
            WkKit.getWkKit().getLogger().warning("生成 MythicMobs 怪物失败: " + e.getMessage());
        }
        return false;
    }
}
