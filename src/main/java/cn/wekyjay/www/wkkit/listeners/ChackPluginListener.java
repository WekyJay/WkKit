package cn.wekyjay.www.wkkit.listeners;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.tool.ChackPluginVersion;
import cn.wekyjay.www.wkkit.tool.WKTool;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class ChackPluginListener implements Listener {
    private final WkKit wk = WkKit.getWkKit();

    @EventHandler
    public void onOpLogin(PlayerJoinEvent event) {
        if (!wk.getConfig().getBoolean("Setting.CheckUpdate") || !event.getPlayer().isOp()) {
            return;
        }

        Map<String, Object> release = ChackPluginVersion.getResourceInfo();
        if (release == null || release.get("name") == null) {
            return;
        }

        String latestVersion = release.get("name").toString();
        String currentVersion = wk.getDescription().getVersion();
        if (WKTool.compareVersion(currentVersion, latestVersion) < 0) {
            event.getPlayer().sendMessage(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_LINK") + "https://github.com/WekyJay/WkKit");
            event.getPlayer().sendMessage(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_LATESTVERSION") + latestVersion + " "
                    + LangConfigLoader.getString("PLUGIN_CHACKUPDATE_CURRENTVERSION") + currentVersion);
        }
    }
}
