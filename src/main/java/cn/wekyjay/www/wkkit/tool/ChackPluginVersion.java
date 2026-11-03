package cn.wekyjay.www.wkkit.tool;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.listeners.ChackPluginListener;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import com.alibaba.druid.support.json.JSONParser;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChackPluginVersion {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/WekyJay/WkKit/releases/latest";
    private static final String PROJECT_URL = "https://github.com/WekyJay/WkKit";
    private static final WkKit wkkit = WkKit.getWkKit();

    private static Map<String, Object> resourceInfo;
    private static boolean needUpdate;

    public static void setNeedUpdate(boolean value) {
        needUpdate = value;
    }

    public static boolean isNeedUpdate() {
        return needUpdate;
    }

    public static Map<String, Object> getResourceInfo() {
        return resourceInfo;
    }

    public ChackPluginVersion() {
        MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_IN"));
        resourceInfo = getLatestRelease();
        if (resourceInfo != null) {
            registerJoinListener();
            runCheck();
        }
    }

    private void registerJoinListener() {
        Bukkit.getScheduler().runTask(wkkit, () ->
                Bukkit.getPluginManager().registerEvents(new ChackPluginListener(), wkkit));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLatestRelease() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(GITHUB_API_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("User-Agent", "WkKit-UpdateChecker");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("GitHub API returned HTTP " + connection.getResponseCode());
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            Map<String, Object> release = new JSONParser(response.toString()).parseMap();
            String version = normalizeVersion(release.get("name"));
            if (version == null) {
                version = normalizeVersion(release.get("tag_name"));
            }
            Map<String, Object> info = new HashMap<String, Object>();

            Object assetsValue = release.get("assets");
            List<Map<String, String>> assets = new ArrayList<Map<String, String>>();
            if (assetsValue instanceof List) {
                for (Object value : (List<?>) assetsValue) {
                    if (value instanceof Map) {
                        Map<String, Object> asset = (Map<String, Object>) value;
                        Object name = asset.get("name");
                        Object downloadUrl = asset.get("browser_download_url");
                        if (name != null && downloadUrl != null) {
                            Map<String, String> parsedAsset = new HashMap<String, String>();
                            parsedAsset.put("name", name.toString());
                            parsedAsset.put("downloadUrl", downloadUrl.toString());
                            assets.add(parsedAsset);
                        }
                    }
                }
            }
            if (version == null) {
                for (Map<String, String> asset : assets) {
                    String assetName = asset.get("name");
                    if (assetName != null && assetName.matches("(?i)WkKit-[0-9]+(?:\\.[0-9]+)*\\.jar")) {
                        version = assetName.substring("WkKit-".length(), assetName.length() - ".jar".length());
                        break;
                    }
                }
            }
            if (version == null) {
                throw new IOException("Latest GitHub release has no semantic version in its name, tag, or jar asset");
            }
            info.put("name", version);
            info.put("assets", assets);
            return info;
        } catch (Exception e) {
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_FAILED"));
            ExceptionHandler.handle("GitHub Releases update check", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String normalizeVersion(Object value) {
        if (value == null) {
            return null;
        }
        String candidate = value.toString().trim().replaceFirst("^[vV]", "");
        return candidate.matches("[0-9]+(?:\\.[0-9]+)*") ? candidate : null;
    }

    public void runCheck() {
        if (resourceInfo == null) {
            return;
        }
        String latestVersion = resourceInfo.get("name").toString();
        String currentVersion = wkkit.getDescription().getVersion();
        try {
            if (WKTool.compareVersion(currentVersion, latestVersion) < 0) {
                setNeedUpdate(true);
                MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_LINK") + PROJECT_URL);
                MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_LATESTVERSION") + latestVersion + " "
                        + LangConfigLoader.getString("PLUGIN_CHACKUPDATE_CURRENTVERSION") + currentVersion);
                MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_CONFIRM"));
            } else {
                setNeedUpdate(false);
                MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_ED"));
            }
        } catch (NumberFormatException e) {
            setNeedUpdate(false);
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_FAILED"));
            ExceptionHandler.handle("Invalid GitHub release version: " + latestVersion, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean autoUpdatePlugin() {
        MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_START"));
        File tempFile = null;
        try {
            if (resourceInfo == null) {
                throw new IOException("GitHub release information is unavailable");
            }
            String latestVersion = resourceInfo.get("name").toString();
            String downloadUrl = findJarDownloadUrl((List<Map<String, String>>) resourceInfo.get("assets"));
            if (downloadUrl == null) {
                throw new IOException("The latest GitHub release does not contain a WkKit jar asset");
            }

            File pluginsDir = wkkit.getDataFolder().getParentFile();
            if (pluginsDir == null || (!pluginsDir.exists() && !pluginsDir.mkdirs())) {
                throw new IOException("Could not access the plugins directory");
            }
            String safeVersion = latestVersion.replaceAll("[^A-Za-z0-9._-]", "_");
            File targetFile = new File(pluginsDir, "WkKit-" + safeVersion + ".jar");
            tempFile = File.createTempFile("WkKit-update-", ".jar.part", pluginsDir);

            HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "WkKit-UpdateChecker");
            try {
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException("GitHub asset download returned HTTP " + connection.getResponseCode());
                }
                try (InputStream input = connection.getInputStream(); FileOutputStream output = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                }
            } finally {
                connection.disconnect();
            }

            if (tempFile.length() == 0) {
                throw new IOException("Downloaded GitHub asset is empty");
            }
            try (java.util.jar.JarFile ignored = new java.util.jar.JarFile(tempFile)) {
                // Confirm the downloaded asset is a readable jar before installing it.
            }

            Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile = null;
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_DOWNLOAD_SUCCESS") + targetFile.getAbsolutePath());
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_RESTART_TIP"));
            return true;
        } catch (Exception e) {
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_IO_ERROR"));
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_LINK") + PROJECT_URL);
            ExceptionHandler.handle("GitHub Releases plugin download", e);
            return false;
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                tempFile.deleteOnExit();
            }
        }
    }

    private String findJarDownloadUrl(List<Map<String, String>> assets) {
        if (assets == null) {
            return null;
        }
        String fallbackUrl = null;
        for (Map<String, String> asset : assets) {
            String name = asset.get("name");
            String url = asset.get("downloadUrl");
            if (name == null || url == null || !name.toLowerCase().endsWith(".jar")) {
                continue;
            }
            if (name.equalsIgnoreCase("WkKit-" + resourceInfo.get("name") + ".jar")) {
                return url;
            }
            if (name.toLowerCase().startsWith("wkkit-")) {
                fallbackUrl = url;
            } else if (fallbackUrl == null) {
                fallbackUrl = url;
            }
        }
        return fallbackUrl;
    }

    public static void doUpdate() {
        if (!isNeedUpdate()) {
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_NOTNEED"));
            return;
        }
        if (autoUpdatePlugin()) {
            setNeedUpdate(false);
        }
    }
}
