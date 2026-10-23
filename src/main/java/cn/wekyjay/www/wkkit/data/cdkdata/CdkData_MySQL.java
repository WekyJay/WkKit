package cn.wekyjay.www.wkkit.data.cdkdata;

import cn.wekyjay.www.wkkit.mysql.cdksqldata.CdkSQLData;

import java.util.List;

public class CdkData_MySQL implements CdkData {

	private final CdkSQLData cdkSQL = new CdkSQLData();

	@Override
	public void addCDKToFile(String CDK, String kits, String data, String mark) {
		cdkSQL.insertData(CDK, kits, data, mark);
	}

	@Override
	public void setCDKStatus(String CDK, String playername) {
		cdkSQL.update_Status(playername, CDK);
	}

	@Override
	public void setCDKMark(String mark, String newmark) {
		cdkSQL.update_Mark(newmark, mark);
	}

	/**
	 * 修复：原来先 findCDK(mark) 查所有 cdk 再循环 deleteData
	 * 直接用一条 DELETE FROM cdk WHERE mark = ? 搞定
	 */
	@Override
	public void delCDKOfMark(String mark) {
		cdkSQL.deleteDataByMark(mark);
	}

	@Override
	public void delCDK(String cdk) {
		cdkSQL.deleteData(cdk);
	}

	@Override
	public List<String> findCDK(String mark) {
		return cdkSQL.findCDK(mark);
	}

	@Override
	public boolean Contain_CDK(String CDK) {
		return cdkSQL.containCDK(CDK);
	}

	@Override
	public boolean Contain_CDKMark(String mark) {
		return cdkSQL.containMark(mark);
	}

	@Override
	public String getCDKKits(String cdk) {
		return cdkSQL.findKits(cdk);
	}

	@Override
	public String getCDKDate(String cdk) {
		return cdkSQL.findDate(cdk);
	}

	@Override
	public String getCDKStatus(String cdk) {
		return cdkSQL.findStatus(cdk);
	}

	@Override
	public String getCDKMark(String cdk) {
		return cdkSQL.findMark(cdk);
	}
}
