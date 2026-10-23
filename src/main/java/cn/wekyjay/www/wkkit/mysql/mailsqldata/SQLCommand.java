package cn.wekyjay.www.wkkit.mysql.mailsqldata;

enum SQLCommand {
	// 创建表 — 修复：加 UNIQUE 索引 (player, kitname)；utf8→utf8mb4
	CREATE_TABLE(
			"CREATE TABLE IF NOT EXISTS `{table}` (" +
					"`id` INT UNSIGNED AUTO_INCREMENT," +
					"`player` VARCHAR(50) NOT NULL," +
					"`kitname` VARCHAR(50) NOT NULL," +
					"`num` SMALLINT UNSIGNED NOT NULL," +
					"PRIMARY KEY (`id`)," +
					"UNIQUE KEY `idx_player_kit` (`player`, `kitname`)" +
					") DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
	),

	// 添加数据 — 修复：去掉硬编码 id=0
	ADD_DATA(
			"INSERT INTO `{table}` " +
					"(`player`,`kitname`,`num`)" +
					"VALUES (?, ?, ?)"
	),

	// Upsert：插入或更新
	UPSERT_DATA(
			"INSERT INTO `{table}` " +
					"(`player`,`kitname`,`num`)" +
					"VALUES (?, ?, ?) " +
					"ON DUPLICATE KEY UPDATE `num` = VALUES(`num`)"
	),

	// 更新领取次数数据
	UPDATE_NUM_DATA(
			"UPDATE `{table}` SET `num` = ? WHERE `player` = ? AND `kitname` = ?"
	),

	// 删除数据
	DELETE_DATA(
			"DELETE FROM `{table}` WHERE `player` = ? AND `kitname` = ?"
	),

	// 查询某玩家所有邮箱礼包名
	SELECT_KIT_NAMES(
			"SELECT `kitname` FROM `{table}` WHERE `player` = ?"
	),

	// 查询某玩家是否存在
	SELECT_EXISTS(
			"SELECT 1 FROM `{table}` WHERE `player` = ? LIMIT 1"
	),

	// 查询某玩家+某礼包的 num（直接 WHERE 过滤）
	SELECT_KIT_NUM(
			"SELECT `num` FROM `{table}` WHERE `player` = ? AND `kitname` = ? LIMIT 1"
	),

	// 检查某玩家+某礼包记录是否存在
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
		return command.replace("{table}", prefix + "maildata");
	}
}
