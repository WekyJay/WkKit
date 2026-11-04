package cn.wekyjay.www.wkkit.mysql.playersqldata;

import cn.wekyjay.www.wkkit.mysql.MySQLManager;
import cn.wekyjay.www.wkkit.mysql.entity.PlayerRecord;
import cn.wekyjay.www.wkkit.mysql.mapper.PlayerMapper;
import cn.wekyjay.www.wkkit.util.ExceptionHandler;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.util.List;

public class PlayerSQLData {
    public static void createTable() { }
    private static QueryWrapper<PlayerRecord> playerKit(String player, String kit) { return new QueryWrapper<PlayerRecord>().eq("player", player).eq("kitname", kit); }
    public void insertData(String kitname, String playername, String data, int time) {
        try { MySQLManager.get().execute(s -> { PlayerRecord r = record(playername, kitname, data, time); s.getMapper(PlayerMapper.class).insert(r); return null; }); }
        catch (Exception e) { if (MySQLManager.isDuplicateKey(e)) MySQLManager.warnDuplicateKey(); else ExceptionHandler.handle("保存玩家礼包数据", e); }
    }
    public void upsertData(String kitname, String playername, String data, int time) {
        try { MySQLManager.get().execute(s -> { PlayerMapper m = s.getMapper(PlayerMapper.class); PlayerRecord old = m.selectOne(playerKit(playername, kitname)); if (old == null) m.insert(record(playername, kitname, data, time)); else { old.setData(data); old.setTime(time); m.updateById(old); } return null; }); }
        catch (Exception e) {
            if (MySQLManager.isDuplicateKey(e)) { update(playername, kitname, "data", data); update(playername, kitname, "time", time); }
            else ExceptionHandler.handle("保存玩家礼包数据", e);
        }
    }
    public void deleteData(String kitname, String playername) {
        try { MySQLManager.get().execute(s -> s.getMapper(PlayerMapper.class).delete(playerKit(playername, kitname))); }
        catch (Exception e) { ExceptionHandler.handle("删除玩家礼包数据", e); }
    }
    public List<String> findKitName(String playername) {
        try { return MySQLManager.get().execute(s -> s.getMapper(PlayerMapper.class).selectList(new QueryWrapper<PlayerRecord>().select("kitname").eq("player", playername)).stream().map(PlayerRecord::getKitname).collect(java.util.stream.Collectors.toList())); }
        catch (Exception e) { ExceptionHandler.handle("查询玩家礼包数据", e); return java.util.Collections.emptyList(); }
    }
    public Boolean findPlayer(String playername) {
        try { return MySQLManager.get().execute(s -> s.getMapper(PlayerMapper.class).selectCount(new QueryWrapper<PlayerRecord>().eq("player", playername)) > 0); }
        catch (Exception e) { ExceptionHandler.handle("查询玩家数据", e); return false; }
    }
    public String findKitData(String playername, String kitname) {
        try { return MySQLManager.get().execute(s -> { PlayerRecord r = s.getMapper(PlayerMapper.class).selectOne(new QueryWrapper<PlayerRecord>().select("id", "player", "kitname", "data", "time").eq("player", playername).eq("kitname", kitname)); return r == null ? null : r.getData(); }); }
        catch (Exception e) { ExceptionHandler.handle("查询玩家礼包数据", e); return null; }
    }
    public Integer findKitTime(String playername, String kitname) {
        try { return MySQLManager.get().execute(s -> { PlayerRecord r = s.getMapper(PlayerMapper.class).selectOne(playerKit(playername, kitname)); return r == null ? null : r.getTime(); }); }
        catch (Exception e) { ExceptionHandler.handle("查询玩家礼包数据", e); return null; }
    }
    public Boolean findPlayerKitExists(String playername, String kitname) {
        try { return MySQLManager.get().execute(s -> s.getMapper(PlayerMapper.class).selectCount(playerKit(playername, kitname)) > 0); }
        catch (Exception e) { ExceptionHandler.handle("查询玩家礼包数据", e); return false; }
    }
    public void update_Time_Data(String playername, String kitname, int value) { update(playername, kitname, "time", value); }
    public void update_Data_Data(String playername, String kitname, String value) { update(playername, kitname, "data", value); }
    private void update(String player, String kit, String field, Object value) {
        try { MySQLManager.get().execute(s -> s.getMapper(PlayerMapper.class).update(null, new UpdateWrapper<PlayerRecord>().eq("player", player).eq("kitname", kit).set(field, value))); }
        catch (Exception e) { ExceptionHandler.handle("更新玩家礼包数据", e); }
    }
    public void update_Data_Data_Lock(String playername, String kitname, String value) {
        try { MySQLManager.get().execute(s -> { PlayerMapper mapper = s.getMapper(PlayerMapper.class); PlayerRecord r = mapper.selectOne(new QueryWrapper<PlayerRecord>().eq("player", playername).eq("kitname", kitname).last(MySQLManager.get().isSQLite() ? "" : "FOR UPDATE")); if (r != null && "false".equals(r.getData())) { r.setData(value); mapper.updateById(r); } return null; }); }
        catch (Exception e) { ExceptionHandler.handle("更新玩家礼包数据", e); }
    }
    private static PlayerRecord record(String player, String kit, String data, int time) {
        PlayerRecord r = new PlayerRecord(); r.setPlayer(player); r.setKitname(kit); r.setData(data); r.setTime(time); return r;
    }
}
