package cn.wekyjay.www.wkkit.mysql.cdksqldata;

enum SQLCommand {
    // 创建表 — 修复：单独加 INDEX(mark)，因为原 UNIQUE(cdk, mark) 对 WHERE mark=? 无效；utf8→utf8mb4
    CREATE_TABLE(
            "CREATE TABLE IF NOT EXISTS `{table}` (" +
                    "`id` INT UNSIGNED AUTO_INCREMENT," +
                    "`cdk` VARCHAR(20) NOT NULL ," +
                    "`kits` VARCHAR(100) NOT NULL," +
                    "`date` VARCHAR(50) NOT NULL," +
                    "`status` VARCHAR(50) NOT NULL," +
                    "`mark` VARCHAR(100) NOT NULL," +
                    "PRIMARY KEY (`id`)," +
                    "UNIQUE KEY `uk_cdk` (`cdk`)," +
                    "INDEX `idx_mark` (`mark`)" +
                    ") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
    ),

    // 添加数据 — 修复：去掉硬编码 id=0
    ADD_DATA(
            "INSERT INTO `{table}` " +
                    "(`cdk`,`kits`,`date`,`status`,`mark`)" +
                    "VALUES (?, ?, ?, ?, ?)"
    ),

    // 更新领取状态
    UPDATE_STATUS_DATA(
            "UPDATE `{table}` SET `status` = ? WHERE `cdk` = ?"
    ),

    // 更新Mark
    UPDATE_MARK_DATA(
            "UPDATE `{table}` SET `mark` = ? WHERE `mark` = ?"
    ),

    // 删除数据（按 mark 批量删除 — 替代先查再循环删）
    DELETE_DATA(
            "DELETE FROM `{table}` WHERE `cdk` = ?"
    ),

    // 按 mark 批量删除（新增）
    DELETE_DATA_BY_MARK(
            "DELETE FROM `{table}` WHERE `mark` = ?"
    ),

    // 查找 CDK（按 mark）
    SELECT_MARK(
            "SELECT `cdk` FROM `{table}` WHERE `mark` = ?"
    ),

    // 查找某条 CDK 的完整信息（一次查出所有字段，替代原来 4 次查询）
    SELECT_CDK_INFO(
            "SELECT `kits`, `date`, `status`, `mark` FROM `{table}` WHERE `cdk` = ? LIMIT 1"
    ),

    // 简单存在性检查
    SELECT_CDK_EXISTS(
            "SELECT 1 FROM `{table}` WHERE `cdk` = ? LIMIT 1"
    ),

    SELECT_MARK_EXISTS(
            "SELECT 1 FROM `{table}` WHERE `mark` = ? LIMIT 1"
    );


    private String command;

    SQLCommand(String command) {
        this.command = command;
    }

    public String commandToString() {
        return command;
    }

    public String format(String prefix) {
        return command.replace("{table}", prefix + "cdk");
    }
}
