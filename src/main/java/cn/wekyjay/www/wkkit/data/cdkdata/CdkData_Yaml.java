package cn.wekyjay.www.wkkit.data.cdkdata;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CdkData_Yaml implements CdkData {

	@Override
	public void addCDKToFile(String CDK, String kits, String data, String mark) {
		WkKit.CDKConfig.set(CDK + ".Kits", kits);
		WkKit.CDKConfig.set(CDK + ".Date", data);
		WkKit.CDKConfig.set(CDK + ".Status", "Available");
		WkKit.CDKConfig.set(CDK + ".Mark", mark);
		try {
			WkKit.CDKConfig.save(WkKit.CDKConfigFile);
			WkKit.CDKConfig = YamlConfiguration.loadConfiguration(WkKit.CDKConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
		
	}

	@Override
	public void setCDKStatus(String CDK, String playername) {
		WkKit.CDKConfig.set(CDK + ".Status", playername);
		try {
			WkKit.CDKConfig.save(WkKit.CDKConfigFile);
			WkKit.CDKConfig = YamlConfiguration.loadConfiguration(WkKit.CDKConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
	}

	@Override
	public void setCDKMark(String mark, String newmark) {
		for(String CDK : findCDK(mark)) {
			WkKit.CDKConfig.set(CDK + ".Mark", newmark);
		}
		try {
			WkKit.CDKConfig.save(WkKit.CDKConfigFile);
			WkKit.CDKConfig = YamlConfiguration.loadConfiguration(WkKit.CDKConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
	}

	@Override
	public void delCDKOfMark(String mark) {
		for(String CDK : findCDK(mark)) {
			WkKit.CDKConfig.set(CDK, null);
		}
		try {
			WkKit.CDKConfig.save(WkKit.CDKConfigFile);
			WkKit.CDKConfig = YamlConfiguration.loadConfiguration(WkKit.CDKConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
	}

	@Override
	public void delCDK(String cdk) {
		WkKit.CDKConfig.set(cdk, null);
		try {
			WkKit.CDKConfig.save(WkKit.CDKConfigFile);
			WkKit.CDKConfig = YamlConfiguration.loadConfiguration(WkKit.CDKConfigFile);
		} catch (IOException e) {
			ExceptionHandler.handle("未知操作", e);
		}
	}

	@Override
	public List<String> findCDK(String mark) {
		List<String> list = new ArrayList<>();
		for(String CDK : WkKit.CDKConfig.getKeys(false)) {
			if(WkKit.CDKConfig.getString(CDK + ".Mark").equals(mark))list.add(CDK);
		}
		return list;
	}

	@Override
	public boolean Contain_CDK(String CDK) {
		return WkKit.CDKConfig.getKeys(false).contains(CDK)?true:false;
	}

	@Override
	public boolean Contain_CDKMark(String mark) {
		for(String CDK : WkKit.CDKConfig.getKeys(false)) {
			if(WkKit.CDKConfig.getString(CDK + ".Mark").equals(mark))return true;
		}
		return false;
	}

	@Override
	public String getCDKKits(String cdk) {
		return WkKit.CDKConfig.getString(cdk + ".Kits");
	}

	@Override
	public String getCDKDate(String cdk) {
		return WkKit.CDKConfig.getString(cdk + ".Date");
	}

	@Override
	public String getCDKStatus(String cdk) {
		return WkKit.CDKConfig.getString(cdk + ".Status");
	}

	@Override
	public String getCDKMark(String cdk) {
		return WkKit.CDKConfig.getString(cdk + ".Mark");
	}

}
