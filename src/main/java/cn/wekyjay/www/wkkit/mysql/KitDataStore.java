package cn.wekyjay.www.wkkit.mysql;

import cn.wekyjay.www.wkkit.mysql.entity.KitGroupRecord;
import cn.wekyjay.www.wkkit.mysql.entity.KitRecord;
import cn.wekyjay.www.wkkit.mysql.mapper.KitGroupMapper;
import cn.wekyjay.www.wkkit.mysql.mapper.KitMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public final class KitDataStore {
    private KitDataStore() {}

    public static Page<KitGroupRecord> pageGroups(long page, long size) {
        return MySQLManager.get().execute(s -> s.getMapper(KitGroupMapper.class)
                .selectPage(new Page<>(Math.max(1, page), Math.max(1, size)), new QueryWrapper<KitGroupRecord>().orderByAsc("name")));
    }

    public static Page<KitRecord> pageKits(String group, long page, long size) {
        return MySQLManager.get().execute(session -> {
            KitGroupRecord groupRecord = findGroup(session, group);
            Page<KitRecord> result = new Page<>(Math.max(1, page), Math.max(1, size));
            if (groupRecord == null) return result;
            return session.getMapper(KitMapper.class).selectPage(result,
                    new QueryWrapper<KitRecord>().eq("group_id", groupRecord.getId()).orderByAsc("kitname"));
        });
    }

    public static List<KitGroupRecord> allGroups() {
        return MySQLManager.get().execute(s -> s.getMapper(KitGroupMapper.class)
                .selectList(new QueryWrapper<KitGroupRecord>().orderByAsc("name")));
    }

    public static List<KitRecord> kitsInGroup(long groupId) {
        return MySQLManager.get().execute(s -> s.getMapper(KitMapper.class)
                .selectList(new QueryWrapper<KitRecord>().eq("group_id", groupId).orderByAsc("kitname")));
    }

    public static boolean groupExists(String group) {
        return MySQLManager.get().execute(s -> findGroup(s, group) != null);
    }

    public static boolean kitExists(String kit) {
        return MySQLManager.get().execute(s -> findKit(s, kit) != null);
    }

    public static String groupOfKit(String kit) {
        return MySQLManager.get().execute(session -> {
            KitRecord row = findKit(session, kit);
            if (row == null) return null;
            KitGroupRecord group = session.getMapper(KitGroupMapper.class).selectById(row.getGroupId());
            return group == null ? null : group.getName();
        });
    }

    public static void putGroup(String name) {
        MySQLManager.get().execute(s -> { ensureGroup(s, name); return null; });
    }

    public static void putKit(String name, String group, String config) {
        MySQLManager.get().execute(session -> {
            long groupId = ensureGroup(session, group);
            KitRecord row = findKit(session, name);
            if (row == null) {
                row = new KitRecord();
                row.setKitname(name);
                row.setGroupId(groupId);
                row.setConfig(config);
                session.getMapper(KitMapper.class).insert(row);
            } else {
                row.setGroupId(groupId);
                row.setConfig(config);
                session.getMapper(KitMapper.class).updateById(row);
            }
            return null;
        });
    }

    public static void removeKit(String name) {
        MySQLManager.get().execute(s -> s.getMapper(KitMapper.class)
                .delete(new QueryWrapper<KitRecord>().eq("kitname", name)));
    }

    public static void removeGroup(String name) {
        MySQLManager.get().execute(session -> {
            KitGroupRecord group = findGroup(session, name);
            if (group == null) return null;
            session.getMapper(KitMapper.class).delete(new QueryWrapper<KitRecord>().eq("group_id", group.getId()));
            session.getMapper(KitGroupMapper.class).deleteById(group.getId());
            return null;
        });
    }

    public static void importYamlGroup(String name, List<KitRecord> kits) {
        MySQLManager.get().execute(session -> {
            long groupId = ensureGroup(session, name);
            KitMapper mapper = session.getMapper(KitMapper.class);
            for (KitRecord kit : kits) {
                if (findKit(session, kit.getKitname()) != null) continue;
                kit.setGroupId(groupId);
                mapper.insert(kit);
            }
            return null;
        });
    }

    public static void saveGroup(String name, List<KitRecord> kits) {
        MySQLManager.get().execute(session -> {
            long groupId = ensureGroup(session, name);
            KitMapper mapper = session.getMapper(KitMapper.class);
            List<String> names = new java.util.ArrayList<>();
            for (KitRecord kit : kits) names.add(kit.getKitname());
            QueryWrapper<KitRecord> stale = new QueryWrapper<KitRecord>().eq("group_id", groupId);
            if (names.isEmpty()) mapper.delete(stale);
            else mapper.delete(stale.notIn("kitname", names));

            for (KitRecord kit : kits) {
                KitRecord existing = findKit(session, kit.getKitname());
                if (existing != null && !existing.getGroupId().equals(groupId)) continue;
                kit.setGroupId(groupId);
                if (existing == null) mapper.insert(kit);
                else {
                    kit.setId(existing.getId());
                    mapper.updateById(kit);
                }
            }
            return null;
        });
    }

    private static KitGroupRecord findGroup(SqlSession session, String name) {
        return session.getMapper(KitGroupMapper.class).selectOne(new QueryWrapper<KitGroupRecord>().eq("name", name));
    }

    private static KitRecord findKit(SqlSession session, String name) {
        return session.getMapper(KitMapper.class).selectOne(new QueryWrapper<KitRecord>().eq("kitname", name));
    }

    private static long ensureGroup(SqlSession session, String name) {
        KitGroupRecord group = findGroup(session, name);
        if (group != null) return group.getId();
        group = new KitGroupRecord();
        group.setName(name);
        session.getMapper(KitGroupMapper.class).insert(group);
        return group.getId();
    }
}
