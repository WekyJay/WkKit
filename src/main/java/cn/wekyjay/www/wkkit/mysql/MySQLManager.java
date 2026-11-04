package cn.wekyjay.www.wkkit.mysql;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.config.LangConfigLoader;
import cn.wekyjay.www.wkkit.mysql.mapper.CdkMapper;
import cn.wekyjay.www.wkkit.mysql.mapper.KitGroupMapper;
import cn.wekyjay.www.wkkit.mysql.mapper.KitMapper;
import cn.wekyjay.www.wkkit.mysql.mapper.MailMapper;
import cn.wekyjay.www.wkkit.mysql.mapper.PlayerMapper;
import cn.wekyjay.www.wkkit.tool.Druid;
import org.bukkit.ChatColor;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** MyBatis-Plus 生命周期和会话管理。 */
public final class MySQLManager {
    private static final MySQLManager INSTANCE = new MySQLManager();
    private volatile SqlSessionFactory sessionFactory;
    private volatile boolean ready;
    private String tablePrefix = "";
    private boolean sqlite;

    private MySQLManager() {}

    public static MySQLManager get() { return INSTANCE; }
    public static String getTablePrefix() { return get().tablePrefix; }
    public boolean isReady() { return ready; }
    public boolean isSQLite() { return sqlite; }
    public static boolean isDatabaseConfigured() {
        WkKit plugin = WkKit.getWkKit();
        if (plugin == null) return false;
        if (plugin.getConfig().getBoolean("MySQL.Enable", false)) return true;
        String type = plugin.getConfig().getString("Storage.Type", "SQLITE");
        return "SQLITE".equalsIgnoreCase(type) || "MYSQL".equalsIgnoreCase(type);
    }

    public static boolean isDuplicateKey(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException && "23000".equals(((SQLException) cause).getSQLState())) return true;
        }
        return false;
    }

    public static void warnDuplicateKey() {
        WkKit.getWkKit().getLogger().warning(ChatColor.YELLOW + LangConfigLoader.getString("MYSQL_DATEEXISTS"));
    }

    public synchronized void enableMySQL() { enableDatabase(true); }

    public synchronized void enableDatabase() {
        boolean legacyMySql = WkKit.getWkKit().getConfig().getBoolean("MySQL.Enable", false);
        String type = WkKit.getWkKit().getConfig().getString("Storage.Type", "SQLITE");
        enableDatabase(legacyMySql || "MYSQL".equalsIgnoreCase(type));
    }

    private synchronized void enableDatabase(boolean useMySQL) {
        if (sessionFactory != null) return;
        WkKit plugin = WkKit.getWkKit();
        sqlite = !useMySQL;
        plugin.getLogger().info(LangConfigLoader.getString("MYSQL_CONNECTING"));
        tablePrefix = plugin.getConfig().getString("MySQL.tablePrefix", "wkkit_");
        if (sqlite) {
            File databaseFile = new File(plugin.getDataFolder(), "wkkit.db");
            new Druid("jdbc:sqlite:" + databaseFile.getAbsolutePath(), "", "", "org.sqlite.JDBC", true);
        } else {
            String host = plugin.getConfig().getString("MySQL.ip", "localhost");
            String database = plugin.getConfig().getString("MySQL.databasename", "name");
            String port = plugin.getConfig().getString("MySQL.port", "3306");
            String user = plugin.getConfig().getString("MySQL.username", "user");
            String password = plugin.getConfig().getString("MySQL.password", "");
            boolean ssl = plugin.getConfig().getBoolean("MySQL.useSSL", false);
            String query = database.contains("?") ? "&serverTimezone=UTC" : "?serverTimezone=UTC";
            query += "&useSSL=" + ssl + "&allowPublicKeyRetrieval=true";
            new Druid("jdbc:mysql://" + host + ":" + port + "/" + database + query, user, password);
        }
        try {
            // 验证连通性，再把连接池作为 MyBatis 的 DataSource。
            try (Connection ignored = Druid.getConnection()) {}
            MybatisConfiguration configuration = new MybatisConfiguration();
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setEnvironment(new Environment("wkkit-druid", new JdbcTransactionFactory(), Druid.getDataSource()));
            GlobalConfig globalConfig = new GlobalConfig();
            globalConfig.setDbConfig(new GlobalConfig.DbConfig().setTablePrefix(tablePrefix));
            GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);
            configuration.addMapper(PlayerMapper.class);
            configuration.addMapper(MailMapper.class);
            configuration.addMapper(CdkMapper.class);
            configuration.addMapper(KitGroupMapper.class);
            configuration.addMapper(KitMapper.class);
            MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(sqlite ? DbType.SQLITE : DbType.MYSQL));
            configuration.addInterceptor(interceptor);
            sessionFactory = new MybatisSqlSessionFactoryBuilder().build(configuration);
            createTables();
            if (sqlite) importLegacyYamlData();
            cn.wekyjay.www.wkkit.config.ConfigManager.getKitconfig().importYamlToDatabase();
            if (!sqlite) dropLegacyGroupConfig();
            ready = true;
            WkKit.getWkKit().getLogger().info(LangConfigLoader.getString("MYSQL_CONNECT_SUCCESS"));
        } catch (Exception e) {
            sessionFactory = null;
            ready = false;
            Druid.shutdown();
            plugin.getLogger().severe(LangConfigLoader.getString("MYSQL_CONECTFAILED") + ": " + e.getMessage());
            plugin.getLogger().severe(sqlite ? "SQLite storage could not be initialized." : "MySQL storage could not be initialized.");
            plugin.saveConfig();
        }
    }

    private void createTables() throws Exception {
        try (Connection connection = Druid.getConnection(); Statement statement = connection.createStatement()) {
            String p = tablePrefix;
            if (sqlite) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "player` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `player` TEXT NOT NULL, `kitname` TEXT NOT NULL, `data` TEXT NOT NULL, `time` INTEGER NOT NULL, UNIQUE (`player`,`kitname`))");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "maildata` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `player` TEXT NOT NULL, `kitname` TEXT NOT NULL, `num` INTEGER NOT NULL, UNIQUE (`player`,`kitname`))");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "cdk` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `cdk` TEXT NOT NULL UNIQUE, `kits` TEXT NOT NULL, `date` TEXT NOT NULL, `status` TEXT NOT NULL, `mark` TEXT NOT NULL)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS `idx_" + p + "cdk_mark` ON `" + p + "cdk` (`mark`)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "kit_group` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT NOT NULL UNIQUE)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "kit` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `kitname` TEXT NOT NULL UNIQUE, `group_id` INTEGER NOT NULL, `config` TEXT NOT NULL)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS `idx_" + p + "kit_group_id` ON `" + p + "kit` (`group_id`)");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "storage_meta` (`key` TEXT PRIMARY KEY, `value` TEXT NOT NULL)");
                return;
            }
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "player` (`id` INT UNSIGNED AUTO_INCREMENT, `player` VARCHAR(50) NOT NULL, `kitname` VARCHAR(50) NOT NULL, `data` VARCHAR(50) NOT NULL, `time` SMALLINT NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `idx_player_kit` (`player`,`kitname`)) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "maildata` (`id` INT UNSIGNED AUTO_INCREMENT, `player` VARCHAR(50) NOT NULL, `kitname` VARCHAR(50) NOT NULL, `num` SMALLINT UNSIGNED NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `idx_player_kit` (`player`,`kitname`)) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "cdk` (`id` INT UNSIGNED AUTO_INCREMENT, `cdk` VARCHAR(20) NOT NULL, `kits` VARCHAR(100) NOT NULL, `date` VARCHAR(50) NOT NULL, `status` VARCHAR(50) NOT NULL, `mark` VARCHAR(100) NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `uk_cdk` (`cdk`), INDEX `idx_mark` (`mark`)) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "kit_group` (`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, `name` VARCHAR(128) NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `uk_kit_group_name` (`name`)) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + p + "kit` (`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, `kitname` VARCHAR(128) NOT NULL, `group_id` BIGINT UNSIGNED NOT NULL, `config` LONGTEXT NOT NULL, PRIMARY KEY (`id`), UNIQUE KEY `uk_kit_kitname` (`kitname`), INDEX `idx_kit_group_id` (`group_id`)) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

            String groupTable = p + "kit_group";
            String kitTable = p + "kit";
            if (!hasColumn(connection, groupTable, "id")) {
                statement.executeUpdate("ALTER TABLE `" + groupTable + "` DROP PRIMARY KEY, ADD COLUMN `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT FIRST, ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `uk_kit_group_name` (`name`)");
            } else if (!hasUniqueIndex(connection, groupTable, "name")) {
                statement.executeUpdate("ALTER TABLE `" + groupTable + "` ADD UNIQUE KEY `uk_kit_group_name` (`name`)");
            }
            if (hasColumn(connection, groupTable, "config")) {
                statement.executeUpdate("ALTER TABLE `" + groupTable + "` MODIFY COLUMN `config` LONGTEXT NULL");
            }

            boolean legacyGroupName = hasColumn(connection, kitTable, "group_name");
            if (legacyGroupName) {
                if (hasColumn(connection, groupTable, "config")) {
                    statement.executeUpdate("INSERT IGNORE INTO `" + groupTable + "` (`name`, `config`) SELECT DISTINCT `group_name`, '' FROM `" + kitTable + "` WHERE `group_name` IS NOT NULL");
                } else {
                    statement.executeUpdate("INSERT IGNORE INTO `" + groupTable + "` (`name`) SELECT DISTINCT `group_name` FROM `" + kitTable + "` WHERE `group_name` IS NOT NULL");
                }
            }
            if (!hasColumn(connection, kitTable, "id")) {
                statement.executeUpdate("ALTER TABLE `" + kitTable + "` DROP PRIMARY KEY, ADD COLUMN `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT FIRST, ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `uk_kit_kitname` (`kitname`), ADD COLUMN `group_id` BIGINT UNSIGNED NULL AFTER `kitname`");
            } else if (!hasColumn(connection, kitTable, "group_id")) {
                statement.executeUpdate("ALTER TABLE `" + kitTable + "` ADD COLUMN `group_id` BIGINT UNSIGNED NULL AFTER `kitname`");
            }
            if (hasColumn(connection, kitTable, "group_name")) {
                statement.executeUpdate("UPDATE `" + kitTable + "` k JOIN `" + groupTable + "` g ON g.`name` = k.`group_name` SET k.`group_id` = g.`id`");
                statement.executeUpdate("ALTER TABLE `" + kitTable + "` MODIFY COLUMN `group_id` BIGINT UNSIGNED NOT NULL");
                if (hasIndex(connection, kitTable, "idx_group_name")) {
                    statement.executeUpdate("ALTER TABLE `" + kitTable + "` DROP INDEX `idx_group_name`");
                }
                statement.executeUpdate("ALTER TABLE `" + kitTable + "` DROP COLUMN `group_name`");
            }
            if (!hasUniqueIndex(connection, kitTable, "kitname")) {
                statement.executeUpdate("ALTER TABLE `" + kitTable + "` ADD UNIQUE KEY `uk_kit_kitname` (`kitname`)");
            }
            if (!hasIndex(connection, kitTable, "idx_kit_group_id")) {
                statement.executeUpdate("ALTER TABLE `" + kitTable + "` ADD INDEX `idx_kit_group_id` (`group_id`)");
            }
        }
    }

    /** Import legacy YAML player, mail and CDK data without overwriting existing database rows. */
    private void importLegacyYamlData() throws SQLException {
        String prefix = tablePrefix;
        try (Connection connection = Druid.getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                try (java.sql.ResultSet marker = statement.executeQuery("SELECT `value` FROM `" + prefix + "storage_meta` WHERE `key`='legacy_yaml_imported'")) {
                    if (marker.next()) {
                        connection.rollback();
                        return;
                    }
                }
                File playerFile = new File(WkKit.getWkKit().getDataFolder(), "player.yml");
                org.bukkit.configuration.file.YamlConfiguration players = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(playerFile);
                try (java.sql.PreparedStatement insert = connection.prepareStatement("INSERT OR IGNORE INTO `" + prefix + "player` (`player`,`kitname`,`data`,`time`) VALUES (?,?,?,?)")) {
                    for (String player : players.getKeys(false)) {
                        org.bukkit.configuration.ConfigurationSection kits = players.getConfigurationSection(player);
                        if (kits == null) continue;
                        for (String kit : kits.getKeys(false)) {
                            String path = player + "." + kit;
                            insert.setString(1, player);
                            insert.setString(2, kit);
                            insert.setString(3, players.getString(path + ".data", "true"));
                            insert.setInt(4, players.getInt(path + ".time", -1));
                            insert.addBatch();
                        }
                    }
                    insert.executeBatch();
                }
                File mailFile = new File(WkKit.getWkKit().getDataFolder(), "maildata.yml");
                org.bukkit.configuration.file.YamlConfiguration mail = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(mailFile);
                try (java.sql.PreparedStatement insert = connection.prepareStatement("INSERT OR IGNORE INTO `" + prefix + "maildata` (`player`,`kitname`,`num`) VALUES (?,?,?)")) {
                    for (String player : mail.getKeys(false)) {
                        org.bukkit.configuration.ConfigurationSection kits = mail.getConfigurationSection(player);
                        if (kits == null) continue;
                        for (String kit : kits.getKeys(false)) {
                            insert.setString(1, player);
                            insert.setString(2, kit);
                            insert.setInt(3, mail.getInt(player + "." + kit));
                            insert.addBatch();
                        }
                    }
                    insert.executeBatch();
                }
                File cdkFile = new File(WkKit.getWkKit().getDataFolder(), "CDK.yml");
                org.bukkit.configuration.file.YamlConfiguration cdks = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(cdkFile);
                try (java.sql.PreparedStatement insert = connection.prepareStatement("INSERT OR IGNORE INTO `" + prefix + "cdk` (`cdk`,`kits`,`date`,`status`,`mark`) VALUES (?,?,?,?,?)")) {
                    for (String code : cdks.getKeys(false)) {
                        insert.setString(1, code);
                        insert.setString(2, cdks.getString(code + ".Kits", ""));
                        insert.setString(3, cdks.getString(code + ".Date", ""));
                        insert.setString(4, cdks.getString(code + ".Status", "Available"));
                        insert.setString(5, cdks.getString(code + ".Mark", ""));
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                statement.executeUpdate("INSERT OR REPLACE INTO `" + prefix + "storage_meta` (`key`,`value`) VALUES ('legacy_yaml_imported','true')");
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof SQLException) throw (SQLException) e;
                throw new SQLException("Failed importing legacy YAML data", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        java.sql.DatabaseMetaData metadata = connection.getMetaData();
        String escape = metadata.getSearchStringEscape();
        String tablePattern = table;
        if (escape != null && !escape.isEmpty()) {
            tablePattern = tablePattern.replace(escape, escape + escape)
                    .replace("_", escape + "_")
                    .replace("%", escape + "%");
        }
        try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, tablePattern, null)) {
            while (columns.next()) if (column.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) return true;
        }
        return false;
    }

    private boolean hasIndex(Connection connection, String table, String index) throws SQLException {
        try (ResultSet indexes = connection.getMetaData().getIndexInfo(connection.getCatalog(), null, table, false, false)) {
            while (indexes.next()) if (index.equalsIgnoreCase(indexes.getString("INDEX_NAME"))) return true;
        }
        return false;
    }

    private boolean hasUniqueIndex(Connection connection, String table, String column) throws SQLException {
        try (ResultSet indexes = connection.getMetaData().getIndexInfo(connection.getCatalog(), null, table, true, false)) {
            while (indexes.next()) {
                if (column.equalsIgnoreCase(indexes.getString("COLUMN_NAME")) && !indexes.getBoolean("NON_UNIQUE")) return true;
            }
        }
        return false;
    }

    public Map<String, String> getLegacyGroupConfigs() throws SQLException {
        Map<String, String> snapshots = new LinkedHashMap<>();
        String table = tablePrefix + "kit_group";
        try (Connection connection = Druid.getConnection()) {
            if (!hasColumn(connection, table, "config")) return snapshots;
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT `name`, `config` FROM `" + table + "`")) {
                while (rows.next()) snapshots.put(rows.getString("name"), rows.getString("config"));
            }
        }
        return snapshots;
    }

    private void dropLegacyGroupConfig() throws SQLException {
        String table = tablePrefix + "kit_group";
        try (Connection connection = Druid.getConnection(); Statement statement = connection.createStatement()) {
            if (hasColumn(connection, table, "config")) statement.executeUpdate("ALTER TABLE `" + table + "` DROP COLUMN `config`");
        }
    }

    public <T> T execute(Function<SqlSession, T> work) {
        SqlSessionFactory factory = sessionFactory;
        if (factory == null) throw new IllegalStateException("MySQL/MyBatis is not initialized");
        try (SqlSession session = factory.openSession(false)) {
            try {
                T result = work.apply(session);
                session.commit();
                return result;
            } catch (RuntimeException | Error e) {
                session.rollback();
                throw e;
            }
        }
    }

    public void shutdown() {
        ready = false;
        sessionFactory = null;
        try { Druid.shutdown(); } catch (Exception ignored) {}
        WkKit.getWkKit().getLogger().info(LangConfigLoader.getString("MYSQL_SHUTDOWN"));
    }
}
