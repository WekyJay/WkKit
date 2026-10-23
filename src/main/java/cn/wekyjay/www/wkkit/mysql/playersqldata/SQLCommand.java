package cn.wekyjay.www.wkkit.mysql.playersqldata;

enum SQLCommand {
	// 创建表 — 修复：加 UNIQUE 索引 (player, kitname) 避免全表扫描；utf8→utf8mb4
	CREATE_TABLE(
			"CREATE TABLE IF NOT EXISTS `{table}` (" +
			"`id` INT UNSIGNED AUTO_INCREMENT," +
			"`player` VARCHAR(50) NOT NULL," +
			"`kitname` VARCHAR(50) NOT NULL," +
			"`data` VARCHAR(50) NOT NULL," +
			"`time` SMALLINT NOT NULL," +
			"PRIMARY KEY (`id`)," +
			"UNIQUE KEY `idx_player_kit` (`player`, `kitname`)" +
			") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
	),

	// 添加数据 — 修复：去掉硬编码 id=0（AUTO_INCREMENT 会自动生成）
	ADD_DATA(
			"INSERT INTO `{table}` " +
			"(`player`,`kitname`,`data`,`time`)" +
			"VALUES (?, ?, ?, ?)"
	),

	// Upsert：插入或更新（依赖 UNIQUE KEY idx_player_kit）
	UPSERT_DATA(
			"INSERT INTO `{table}` " +
			"(`player`,`kitname`,`data`,`time`)" +
			"VALUES (?, ?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE `data` = VALUES(`data`), `time` = VALUES(`time`)"
	),

	// 更新领取次数数据
	UPDATE_TIME_DATA(
			"UPDATE `{table}` SET `time` = ? WHERE `player` = ? AND `kitname` = ?"
	),

	// 更新领取时间数据
	UPDATE_DATA_DATA(
			"UPDATE `{table}` SET `data` = ? WHERE `player` = ? AND `kitname` = ?"
	),

	// 删除数据
	DELETE_DATA(
			"DELETE FROM `{table}` WHERE `player` = ? AND `kitname` = ?"
	),

	// 查询某玩家所有礼包名（带索引，速度快）
	SELECT_KIT_NAMES(
			"SELECT `kitname` FROM `{table}` WHERE `player` = ?"
	),

	// 查询某玩家是否存在（高效 COUNT 检查）
	SELECT_EXISTS(
			"SELECT 1 FROM `{table}` WHERE `player` = ? LIMIT 1"
	),

	// 查询某玩家+某礼包的 data（直接 WHERE 过滤，不再 SELECT * 再 Java 过滤）
	SELECT_KIT_DATA(
			"SELECT `data` FROM `{table}` WHERE `player` = ? AND `kitname` = ? LIMIT 1"
	),

	// 查询某玩家+某礼包的 time
	SELECT_KIT_TIME(
			"SELECT `time` FROM `{table}` WHERE `player` = ? AND `kitname` = ? LIMIT 1"
	),

	// 检查某玩家+某礼包记录是否存在（用于 contain_Kit）
	SELECT_PLAYER_KIT_EXISTS(
			"SELECT 1 FROM `{table}` WHERE `player` = ? AND `kitname` = ? LIMIT 1"
	);


	private String command;

	SQLCommand(String command){
        this.command = command;
	}
	public String commandToString() {
		return command;
	}
	public String format(String prefix) {
		return command.replace("{table}", prefix + "player");
	}
}
