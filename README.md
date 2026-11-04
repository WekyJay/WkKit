# WkKit 

![WkKit Logo](https://img.shields.io/badge/Minecraft-Plugin-blue?style=flat-square) ![Version](https://img.shields.io/badge/version-1.5.0-green?style=flat-square) ![License](https://img.shields.io/badge/license-MIT-lightgrey?style=flat-square)[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/WekyJay/WkKit)

> **高效、强大、可定制的Minecraft礼包管理插件**

---

## ✨ 项目简介
WkKit 是一款专为 Minecraft 服务器打造的礼包/福利发放插件，支持多种礼包管理、CDK兑换、GUI交互、MySQL数据存储、NBT物品操作等功能，兼容 1.12-1.21+，助力服务器福利系统高效运作。

---

## 🚀 功能亮点
- **多礼包管理**：支持自定义礼包、批量发放、冷却与次数限制
- **GUI 菜单交互**：直观易用的礼包领取界面，支持自定义材质与模型ID
- **CDK兑换系统**：支持生成/兑换礼包码，灵活配置
- **多种存储方式**：默认使用 SQLite，也可选择 MySQL 或 YAML
- **NBT物品支持**：全面适配NBT-API，支持自定义物品属性、头颅皮肤等
- **新人礼包/自动发放**：支持新玩家自动发放礼包，严格检测模式
- **多语言/自定义提示**：支持多语言与自定义消息
- **高兼容性**：适配多版本 Spigot/Paper/Bukkit/Folia 服务端

---

## 🛠️ 安装与配置
1. **下载插件**：将 `WkKit-x.x.x.jar` 放入服务器的 `plugins` 文件夹。
2. **重启服务器**，自动生成 `config.yml` 配置文件。
3. **编辑配置**：根据需要修改插件目录中的 `config.yml`。默认使用 SQLite；多服共享数据可选择 MySQL：
   ```yaml
   Storage:
     Type: 'SQLITE' # SQLITE、MYSQL 或 YAML
   MySQL:
     Enable: false # 兼容旧配置；true 时优先使用 MySQL
     databasename: 'name'
     username: 'user'
     password: 'pw'
     port: '3306'
     ip: 'localhost'
     useSSL: false
     tablePrefix: 'wkkit_'
   ```
   从旧版 YAML 存储升级时，请先备份数据文件；首次使用 SQLite 会自动导入旧数据。
4. **重载插件**：使用 `/wkkit reload` 应用新配置。

---

## 📦 指令与权限
- `/wkkit` 主命令，支持礼包管理、CDK生成、数据重载等
- 详细指令与权限请见 [Wiki](https://wekyjay.github.io/WkKit_WiKi/#/)

---

## 📝 更新日志
详见 [CHANGELOG](./CHANGELOG.md) 或 Releases 页面。

---

## 🤝 联系与支持
- **作者**：WekyJay
- **反馈/建议**：请在 [Issues](https://github.com/WekyJay/WkKit/issues) 提交
- **QQ1️⃣群**：945144520
- **QQ2️⃣群**：60484123

---

> 本插件已适配最新NBT-API与MySQL配置，欢迎Star与PR！

---

## 📝 更新日志

详见 [CHANGELOG](./CHANGELOG.md) 或 Releases 页面。
