# 更新日志

## v1.5.0（2026-11-04）

### 中文

- **默认存储改为 SQLite**：新增 `Storage.Type`，支持 `SQLITE`、`MYSQL` 和 `YAML`。首次启用 SQLite 时导入现有玩家领取记录、邮件、CDK 和礼包 YAML 数据；旧配置中显式启用 `MySQL.Enable` 的服务器继续使用 MySQL。
- **减少 YAML 磁盘操作**：更新内存配置后合并延迟写入，不再每次保存后重新读取整个文件；插件重载和关闭时写入待保存数据。
- **优化礼包数据库写入**：导入和保存礼包组时批量查询已有礼包，减少逐条数据库查询。
- **Folia 加载适配**：加入 `folia-supported` 声明，并替换版本检查与 YAML 保存中的 Bukkit 调度器调用。Folia 的其他运行路径仍需在实际服务器上验证。
- **连接池配置**：为 Druid 空闲连接检测设置 `SELECT 1`，消除缺少验证查询的日志。
- **构建**：使用 Java 21 打包，插件类保持 Java 8 字节码。

升级前请备份现有 YAML 文件和数据库。旧配置未设置 `Storage.Type` 且未启用 MySQL 时，升级后默认使用 SQLite。

### English

- **SQLite is the default storage backend.** Added `Storage.Type` with `SQLITE`, `MYSQL`, and `YAML` options. The first SQLite startup imports existing player, mail, CDK, and kit YAML data. Servers with `MySQL.Enable: true` continue to use MySQL.
- **Reduced YAML disk I/O.** Changes are kept in memory and saved in batches after a short delay. Pending changes are saved on plugin reload and shutdown.
- **Faster kit database writes.** Kit group imports and saves now fetch existing kits in batches instead of querying each kit separately.
- **Folia loading support.** Added the `folia-supported` declaration and replaced Bukkit scheduler calls in version checking and YAML saving. Other Folia runtime paths still require server validation.
- **Connection pool validation.** Added `SELECT 1` for Druid idle connection checks.
- **Build.** Package with Java 21 while retaining Java 8 bytecode for plugin classes.

Back up existing YAML files and databases before upgrading. Legacy configurations without `Storage.Type` and without MySQL enabled will use SQLite by default.

## v1.4.9（2026-11-03）

- **MySQL 数据访问**：改用 MyBatis-Plus 管理玩家领取记录、邮件、CDK 和礼包数据，并启用分页插件；保留现有玩家、邮件和 CDK 表结构及表前缀兼容。
- **礼包数据迁移**：MySQL 启动时幂等导入 `Kits/*.yml`，以数据库已有数据为准，并保留空礼包组和完整礼包配置。切回 YAML 模式时不从数据库同步数据。
- **编辑器分页**：礼包组和组内礼包列表改为异步分页查询；切页或关闭界面后，过期查询结果不会覆盖当前界面。
- **邮件领取修复**：按玩家背包实际接收数量扣减邮箱记录，避免领取相同堆叠物品时数量被合并或丢失。
- **版本兼容**：使用 Java 8 字节码构建，`api-version` 固定为 `1.13`；适配目标包含 Minecraft 1.8–1.21 和 26.3。仅在 Folia 服务器调用 Folia 调度，其他核心使用 Bukkit 调度。
- **物品数据兼容**：更新 NBT-API，统一通过 NBT-API 读写物品标签，兼容旧版物品 NBT 与新版数据组件。

> 注意: 本次更新重构SQL表和语句,MYSQL服务器使用该版本之前,需要先备份之前的数据,以防造成不可逆的损失.
