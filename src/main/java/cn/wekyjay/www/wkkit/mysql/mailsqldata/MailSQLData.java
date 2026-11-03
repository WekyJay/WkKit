package cn.wekyjay.www.wkkit.mysql.mailsqldata;

import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import cn.wekyjay.www.wkkit.mysql.entity.MailRecord;
import cn.wekyjay.www.wkkit.mysql.mapper.MailMapper;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.util.List;

public class MailSQLData {
    public static void createTable() { }
    private static QueryWrapper<MailRecord> key(String player, String kit) { return new QueryWrapper<MailRecord>().eq("player", player).eq("kitname", kit); }
    public void insertData(String player, String kit, int num) {
        try { MySQLManager.get().execute(s -> { MailRecord r = row(player, kit, num); s.getMapper(MailMapper.class).insert(r); return null; }); }
        catch (Exception e) { if (MySQLManager.isDuplicateKey(e)) MySQLManager.warnDuplicateKey(); else ExceptionHandler.handle("保存玩家邮件", e); }
    }
    public void upsertData(String player, String kit, int num) {
        try { MySQLManager.get().execute(s -> { MailMapper m = s.getMapper(MailMapper.class); MailRecord r = m.selectOne(key(player, kit)); if (r == null) m.insert(row(player, kit, num)); else { r.setNum(num); m.updateById(r); } return null; }); }
        catch (Exception e) {
            if (MySQLManager.isDuplicateKey(e)) update_Num(player, kit, num);
            else ExceptionHandler.handle("保存玩家邮件", e);
        }
    }
    public void deleteData(String player, String kit) {
        try { MySQLManager.get().execute(s -> s.getMapper(MailMapper.class).delete(key(player, kit))); }
        catch (Exception e) { ExceptionHandler.handle("删除玩家邮件", e); }
    }
    public List<String> findKitName(String player) {
        try { return MySQLManager.get().execute(s -> s.getMapper(MailMapper.class).selectList(new QueryWrapper<MailRecord>().select("kitname").eq("player", player)).stream().map(MailRecord::getKitname).collect(java.util.stream.Collectors.toList())); }
        catch (Exception e) { ExceptionHandler.handle("查询玩家邮件", e); return java.util.Collections.emptyList(); }
    }
    public Boolean findPlayer(String player) {
        try { return MySQLManager.get().execute(s -> s.getMapper(MailMapper.class).selectCount(new QueryWrapper<MailRecord>().eq("player", player)) > 0); }
        catch (Exception e) { ExceptionHandler.handle("查询玩家邮件", e); return false; }
    }
    public int findKitNum(String player, String kit) {
        try { return MySQLManager.get().execute(s -> { MailRecord r = s.getMapper(MailMapper.class).selectOne(key(player, kit)); return r == null ? 0 : r.getNum(); }); }
        catch (Exception e) { ExceptionHandler.handle("查询玩家邮件", e); return 0; }
    }
    public Boolean findPlayerKitExists(String player, String kit) {
        try { return MySQLManager.get().execute(s -> s.getMapper(MailMapper.class).selectCount(key(player, kit)) > 0); }
        catch (Exception e) { ExceptionHandler.handle("查询玩家邮件", e); return false; }
    }
    public void update_Num(String player, String kit, int value) {
        try { MySQLManager.get().execute(s -> s.getMapper(MailMapper.class).update(null, new UpdateWrapper<MailRecord>().eq("player", player).eq("kitname", kit).set("num", value))); }
        catch (Exception e) { ExceptionHandler.handle("更新玩家邮件", e); }
    }
    private static MailRecord row(String player, String kit, int num) { MailRecord r = new MailRecord(); r.setPlayer(player); r.setKitname(kit); r.setNum(num); return r; }
}
