package cn.wekyjay.www.wkkit.tool;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import cn.wekyjay.www.wkkit.listeners.ChackPluginListener;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import com.alibaba.druid.support.json.JSONParser;
import org.bukkit.Bukkit;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;


public class ChackPluginVersion{

	private static final WkKit wkkit = WkKit.getWkKit();

	private static Map<String, Object> resourceInfo = null;

	private static boolean needUpdate = false;
	public static void setNeedUpdate(boolean value) { needUpdate = value; }
	public static boolean isNeedUpdate() { return needUpdate; }

	public static Map<String, Object> getResourceInfo() {
		return resourceInfo;
	}

	/**
	 * Modify of 1.2.5
	 * 1.2.5之后修改为Spigot Resource API
	 */
	public ChackPluginVersion(){
		MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_IN"));

		ChackPluginVersion.resourceInfo = this.getResourceInfo("versions/latest");

		if (ChackPluginVersion.resourceInfo != null) {
			runCheck();
		}

	}




	public Map<String,Object> getResourceInfo(String path){
		HttpURLConnection con = null;
		BufferedReader buffer = null;
		InputStream inputStream = null;


		//得到连接对象
		try {
			URL url = new URL("https://api.spiget.org/v2/resources/98415/" + path);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			//设置连接超时时间
			con.setConnectTimeout(10000);
			//设置读取超时时间
			con.setReadTimeout(10000);
			//添加请求头
			con.setRequestProperty("Connection","keep-alive");
			//获取服务器返回的输入流
			inputStream = con.getInputStream();

			//得到响应码
			int responseCode = con.getResponseCode();
			// 如果响应码成功了则存储响应数据
			if (responseCode == HttpURLConnection.HTTP_OK){
				//读取输入流s
				buffer = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder respose = new StringBuilder();
				String line;
				while((line = buffer.readLine())!=null){
					respose.append(line);
				}
				// 响应成功则注册玩家登陆检查更新事件
				Bukkit.getPluginManager().registerEvents(new ChackPluginListener(),wkkit);
				return new JSONParser(respose.toString()).parseMap();
			}


		} catch (IOException e) {
			MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_FAILED"));
			return null;
		}finally {
			if (inputStream != null){
				try {
					inputStream.close();
				} catch (IOException ignored) {}
			}
			if (buffer != null){
				try {
					buffer.close();
				} catch (IOException ignored) {}
			}
		}
		MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_FAILED"));
		return null;
	}

	public void runCheck() {
        if(resourceInfo == null) {return;}
        String lver = resourceInfo.get("name").toString();
        String curVer = WkKit.getWkKit().getDescription().getVersion();
        if(WKTool.compareVersion(curVer, lver) < 0) {
            setNeedUpdate(true);
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_LINK") + "https://www.spigotmc.org/resources/wkkit.98415/");
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_LATESTVERSION") + lver + " "
                    + LangConfigLoader.getString("PLUGIN_CHACKUPDATE_CURRENTVERSION") + curVer
            );
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_CONFIRM"));
        }else {
            setNeedUpdate(false);
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_ED"));
        }
	}

    /**
     * 自动下载并替换插件jar
     */
    public void autoUpdatePlugin() {
        String downloadUrl = "https://api.spiget.org/v2/resources/98415/download";
        // 目标路径：plugins/WkKit-新版本号.jar
        String pluginsDir = wkkit.getDataFolder().getParentFile().getAbsolutePath();
        String newJarName = "WkKit-" + resourceInfo.get("name") + ".jar";
        File targetFile = new File(pluginsDir +"/"+ newJarName);
        MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_START"));
        try {
            File currentJar = new File(wkkit.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			// 获取文件真实名称
			String currentJarName = currentJar.getName();
			// 获取真文件路径
			String currentJarPath = pluginsDir + "/" +currentJarName;
			File currentJarFile = new File(currentJarPath);
			// 删除当前jar
			if (currentJarFile.exists()) {
				if (currentJarFile.delete()) {
					MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_DELETE_OLD_SUCCESS") + currentJarFile.getName());
				} else {
					MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_DELETE_OLD_FAIL") + currentJarFile.getName());
				}
			}

        } catch (Exception e) {
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_DELETE_OLD_ERROR") + e.getMessage());
        }
        int byteread = 0;
        try {
            URL url = new URL(downloadUrl);
            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1204];
            while ((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }
            fs.close();
            inStream.close();
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_DOWNLOAD_SUCCESS") + targetFile.getAbsolutePath());
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_RESTART_TIP"));

        } catch (FileNotFoundException e) {
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_PATH_NOTFOUND"));
            ExceptionHandler.handle("未知操作", e);
        } catch (IOException e) {
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_UPDATE_IO_ERROR"));
            ExceptionHandler.handle("未知操作", e);
        }
    }

    public static void doUpdate() {
        if (!isNeedUpdate()) {
            MessageManager.sendMessageWithPrefix(LangConfigLoader.getString("PLUGIN_CHACKUPDATE_NOTNEED"));
            return;
        }
        new ChackPluginVersion().autoUpdatePlugin();
        setNeedUpdate(false);
    }



}
