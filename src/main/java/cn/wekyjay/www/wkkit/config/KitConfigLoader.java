package cn.wekyjay.www.wkkit.config;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.kit.Kit;
import cn.wekyjay.www.wkkit.kit.KitGroupManager;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitConfigLoader{
	private List<File> fileList = new ArrayList<>();
	private Map<String, FileConfiguration> fileConfigMap = new HashMap<>();
	private List<Kit> kits = new ArrayList<Kit>();

	protected KitConfigLoader() {}
	
	public List<Kit> getKitsList(){
		return kits;
	}
	
	public List<File> getFileList() {
		return fileList;
	}
	
	public Map<String, FileConfiguration> getFileConfigMap() {
		return fileConfigMap;
	}
	
	/**
	 * 加载文件夹中的配置文件
	 */
	public void loadConfig() {
		String path = WkKit.getWkKit().getDataFolder().getAbsolutePath() + File.separator + "Kits";
		File[] ff = new File(path).listFiles();
		 for(File fs : ff) {
			 if(fs.isFile() && fs.getName().endsWith(".yml")) {
				 fileList.add(fs);
				 fileConfigMap.put(fs.getName(),YamlConfiguration.loadConfiguration(fs));
			 }
		 }
		 readKitConfig(); 
	}

	
	/**
	 * 返回存在该路径的文件名
	 * @param path
	 * @return
	 */
	public String getContainsFilename(String path) {
		for(File fl : fileList) {
			if(fileConfigMap.get(fl.getName()).contains(path)) {
				return fl.getName();
			}
		}
		return null;
	}
	
	/**
	 * 返回存在该路径的文件
	 * @param path
	 * @return
	 */
	public File getContainsFile(String path) {
		for(File fl : fileList) {
			if(fileConfigMap.get(fl.getName()).contains(path)) {
				return fl;
			}
		}
		return null;
	}
	
	
	/**
	 * 判断是否存在该路径
	 * @param path
	 * @return
	 */
	public Boolean contains(String path) {
		for(File fl : fileList) {
			if(fileConfigMap.get(fl.getName()).contains(path)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 文件名返回对象
	 * @param filename
	 * @return
	 */
	public  FileConfiguration getConfig(String filename) {
		return fileConfigMap.get(filename);
	}
	/**
	 * 路径返回对象
	 * @param path
	 * @return
	 */
	public  FileConfiguration getConfigWithPath(String path) {
		String filename = getContainsFilename(path);
		return fileConfigMap.get(filename);
	}
	
	/**
	 * 获得文件中的存在的礼包名
	 * @return List<String>
	 */
	public  List<String> getKits() {
		List<String> list = new ArrayList<>();
		for(File fl : fileList) {
			Set<String> set = fileConfigMap.get(fl.getName()).getKeys(false);
			for(String key : set) {
				if(!list.contains(key)) {
					list.add(key);
				}
			}
		}
		return list;
	}
	
	/**
	 * 判断是否存在重复的礼包，并且返回值。
	 * @return int
	 */
	public  int checkRepeatKits() {
		List<String> list = new ArrayList<>();
		int num = 0;
		for(File fl : fileList) {
			Set<String> set = fileConfigMap.get(fl.getName()).getKeys(false);
			for(String key : set) {
				if(!list.contains(key)) {
					list.add(key);
				}else {
					Bukkit.getLogger().warning(LangConfigLoader.getString("CONTAIN_REPEAT_KIT") + key);
					num++;
				}
			}
		}
		return num;
	}

	/**
	 * 读取配置中存在的礼包,并检测有效性
	 */
	public void readKitConfig() {
		List<String> kitlist = this.getKits();
		int repeatnum = this.checkRepeatKits();
		//查找重复的礼包
		if(repeatnum != 0) {
			Bukkit.getLogger().warning("[WkKit]" + repeatnum + "个礼包加载失败！");
		}

		for(String kitname : kitlist) {
			String displayName = this.getString(kitname + ".Name");
			String icon = this.getString(kitname + ".Icon");
			List<String> itemNBT = this.getStringList(kitname + ".Item");
			ItemStack[] itemStack = new ItemStack[itemNBT.size()];// 0-9 刚好十个
			int num = 0;
			for(String nbt : itemNBT) {
				NBTContainer c = new NBTContainer(nbt);
				itemStack[num] = NBTItem.convertNBTtoItem(c);
				num++;
			}
			new Kit(kitname,displayName,icon,itemStack);
		}
	}
	
	// 封装BukkitAPI的方法
	public  String getString(String path) {
		if(this.contains(path)) {
			return getConfigWithPath(path).getString(path);
		}
		return null;
	}
	public  List<String> getStringList(String path) {
		return getConfigWithPath(path).getStringList(path);
		
	}
	public  int getInt(String path) {
		return getConfigWithPath(path).getInt(path);
		
	}
	public  long getLong(String path) {
		return getConfigWithPath(path).getLong(path);
		
	}
	public  Boolean getBoolean(String path) {
		return getConfigWithPath(path).getBoolean(path);
		
	}
	public  void set(String path,Object value) {
		getConfigWithPath(path).set(path, value);
	}
	/**
	 * 添加一段信息到指定路径，如果没有该路径就默认创建一个文件存放信息
	 * @param path
	 * @param value
	 * @param def
	 */
	public void set(String path,Object value,String filename) {
		if(getConfigWithPath(path) == null) {
			File file = new File(WkKit.getWkKit().getDataFolder().getAbsolutePath() + File.separator + "Kits" + File.separator + filename + ".yml");
			if(!file.exists()) {try {
				file.createNewFile();
				new KitGroupManager(filename);
				fileConfigMap.put(file.getName(),YamlConfiguration.loadConfiguration(file));
			} catch (IOException e) {
				ExceptionHandler.handle("未知操作", e);
			}}
			getConfig(file.getName()).set(path, value);
		}else {
			getConfigWithPath(path).set(path, value);
		}
		
	}

	
	public  void save(String filename) throws IOException {
		String path = WkKit.getWkKit().getDataFolder().getAbsolutePath() +  File.separator + "Kits" + File.separator + filename;
		getConfig(filename).save(new File(path));
	}
}
