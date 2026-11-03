package cn.wekyjay.www.wkkit.mysql.cdksqldata;

import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import cn.wekyjay.www.wkkit.mysql.entity.CdkRecord;
import cn.wekyjay.www.wkkit.mysql.mapper.CdkMapper;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.util.List;

public class CdkSQLData {
    public static void createTable() { }
    public void insertData(String cdk, String kits, String date, String mark) {
        try { MySQLManager.get().execute(s -> { CdkRecord r = new CdkRecord(); r.setCdk(cdk); r.setKits(kits); r.setDate(date); r.setStatus("Available"); r.setMark(mark); return s.getMapper(CdkMapper.class).insert(r); }); }
        catch (Exception e) { if (MySQLManager.isDuplicateKey(e)) MySQLManager.warnDuplicateKey(); else ExceptionHandler.handle("保存CDK", e); }
    }
    public void deleteData(String cdk) { updateDelete(new QueryWrapper<CdkRecord>().eq("cdk", cdk)); }
    public void deleteDataByMark(String mark) { updateDelete(new QueryWrapper<CdkRecord>().eq("mark", mark)); }
    private void updateDelete(QueryWrapper<CdkRecord> query) {
        try { MySQLManager.get().execute(s -> s.getMapper(CdkMapper.class).delete(query)); }
        catch (Exception e) { ExceptionHandler.handle("删除CDK", e); }
    }
    public List<String> findCDK(String mark) {
        try { return MySQLManager.get().execute(s -> s.getMapper(CdkMapper.class).selectList(new QueryWrapper<CdkRecord>().select("cdk").eq("mark", mark)).stream().map(CdkRecord::getCdk).collect(java.util.stream.Collectors.toList())); }
        catch (Exception e) { ExceptionHandler.handle("查询CDK", e); return java.util.Collections.emptyList(); }
    }
    public String[] findCdkInfo(String cdk) {
        try { return MySQLManager.get().execute(s -> { CdkRecord r = s.getMapper(CdkMapper.class).selectOne(new QueryWrapper<CdkRecord>().eq("cdk", cdk)); return r == null ? null : new String[]{r.getKits(), r.getDate(), r.getStatus(), r.getMark()}; }); }
        catch (Exception e) { ExceptionHandler.handle("查询CDK", e); return null; }
    }
    public String findKits(String cdk) { String[] info = findCdkInfo(cdk); return info == null ? null : info[0]; }
    public String findDate(String cdk) { String[] info = findCdkInfo(cdk); return info == null ? null : info[1]; }
    public String findStatus(String cdk) { String[] info = findCdkInfo(cdk); return info == null ? null : info[2]; }
    public String findMark(String cdk) { String[] info = findCdkInfo(cdk); return info == null ? null : info[3]; }
    public Boolean containMark(String mark) { return exists(new QueryWrapper<CdkRecord>().eq("mark", mark)); }
    public Boolean containCDK(String cdk) { return exists(new QueryWrapper<CdkRecord>().eq("cdk", cdk)); }
    private boolean exists(QueryWrapper<CdkRecord> query) {
        try { return MySQLManager.get().execute(s -> s.getMapper(CdkMapper.class).selectCount(query) > 0); }
        catch (Exception e) { ExceptionHandler.handle("检查CDK", e); return false; }
    }
    public void update_Status(String playername, String cdk) { updateField("status", playername, "cdk", cdk); }
    public void update_Mark(String newmark, String mark) {
        try { MySQLManager.get().execute(s -> s.getMapper(CdkMapper.class).update(null, new UpdateWrapper<CdkRecord>().eq("mark", mark).set("mark", newmark))); }
        catch (Exception e) { ExceptionHandler.handle("更新CDK分组", e); }
    }
    private void updateField(String field, Object value, String key, Object keyValue) {
        try { MySQLManager.get().execute(s -> s.getMapper(CdkMapper.class).update(null, new UpdateWrapper<CdkRecord>().eq(key, keyValue).set(field, value))); }
        catch (Exception e) { ExceptionHandler.handle("更新CDK", e); }
    }
}
