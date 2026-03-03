package cn.wekyjay.www.wkkit.tool;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;


public class ChackFiles {
	public void chack(File file) {
		ergodicFile(file);
	}
	/**
	 * 遍历文件执行检查节点
	 * @param file
	 */
	private void ergodicFile(File file) {
        for(File cfile : file.listFiles()) {
        	// 如果不是文件则重新遍历
        	if(cfile.isDirectory()){
        		ergodicFile(cfile);
        	}else {
        		chackFile(cfile);
        	}
        }
	}
	/**
	 * 检查文件节点是否与内部包一致，如果不一致则更新节点。
	 * @param file
	 */
	private void chackFile(File file) {
		if(file.isFile()) {
			String tmpfilepath = file.getPath();
			int idex = tmpfilepath.lastIndexOf("WkKit");
			String filepath = tmpfilepath.substring(idex + 5);
			YamlConfiguration fileYaml = null;
			YamlConfiguration jarYaml = getYamlConfig(filepath.replaceAll("\\\\", "/"));
			if(file.exists() && jarYaml != null) {
				if(filepath.contains("config.yml") || filepath.contains("Language")) {
					fileYaml = YamlConfiguration.loadConfiguration(file);
					// 判断旧文件是否存在新节点
					for(String key : jarYaml.getKeys(true)) {
						// 如果不存在就添加
						if(!fileYaml.contains(key)) {
							MessageManager.infoDeBug(key + " - §cNO EXIST§a(ADDED)");
							fileYaml.set(key, jarYaml.get(key));
						}
					}
					// 判断新文件是否缺少旧节点
					for(String key : fileYaml.getKeys(true)) {
						// 如果不存在就删除
						if(!jarYaml.contains(key)) {
							MessageManager.infoDeBug(key + " - §eNO EXIST§7(DELETED)");
							fileYaml.set(key, null);
						}
					}
					try {
						fileYaml.save(file);
					} catch (IOException e) {
						ExceptionHandler.handle("未知操作", e);
					}
				}
			}
		}
	}
	
	
	/**
	 * 获取Jar内部资源的配置
	 * @param path
	 * @return
	 */
	private YamlConfiguration getYamlConfig(String path){
        InputStream inputStream = null;
        YamlConfiguration yaml = null;
        try {
            inputStream = this.getClass().getResourceAsStream(path);
            if(inputStream == null) return null;
            File file = null;
			try {
				file = asFile(inputStream);
			} catch (IOException e) {
				ExceptionHandler.handle("未知操作", e);
			}
            if(null != file) {
                yaml = YamlConfiguration.loadConfiguration(file);
            }
            // 关闭流
        } finally {
            if(inputStream != null){
                try{
                    inputStream.close();
                }catch (IOException e){
                    ExceptionHandler.handle("未知操作", e);
                }
            }
        }
        return yaml;
    }
	/**
	 * 输入流转文件
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static File asFile(InputStream inputStream) throws IOException{
		File tmp = File.createTempFile("tmp", null);
		OutputStream os = new FileOutputStream(tmp);
		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
		  os.write(buffer, 0, bytesRead);
		}
		inputStream.close();
		os.close();
		return tmp;
	}

}
