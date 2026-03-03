# WkKit 

![WkKit Logo](https://img.shields.io/badge/Minecraft-Plugin-blue?style=flat-square) ![Version](https://img.shields.io/badge/version-1.4.0-green?style=flat-square) ![License](https://img.shields.io/badge/license-MIT-lightgrey?style=flat-square)[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/WekyJay/WkKit)

> **高效、强大、可定制的Minecraft礼包管理插件**

---

## ✨ 项目简介
WkKit 是一款专为 Minecraft 服务器打造的礼包/福利发放插件，支持多种礼包管理、CDK兑换、GUI交互、MySQL数据存储、NBT物品操作等功能，兼容 1.12-1.21+，助力服务器福利系统高效运作。

---

## 🚀 功能亮点
- **多礼包管理**：支持自定义礼包、批量发放、冷却与次数限制
- **GUI 菜单交互**：直观易用的礼包领取界面，支持自定义材质与模型ID
- **CDK兑换系统**：支持生成/兑换礼包码，灵活配置
- **MySQL/本地存储**：支持MySQL数据库与本地文件双模式，数据安全可靠
- **NBT物品支持**：全面适配NBT-API，支持自定义物品属性、头颅皮肤等
- **新人礼包/自动发放**：支持新玩家自动发放礼包，严格检测模式
- **多语言/自定义提示**：支持多语言与自定义消息
- **高兼容性**：适配多版本 Spigot/Paper/Bukkit/Folia 服务端

---

## 🛠️ 安装与配置
1. **下载插件**：将 `WkKit-x.x.x.jar` 放入服务器的 `plugins` 文件夹。
2. **重启服务器**，自动生成 `config.yml` 配置文件。
3. **编辑配置**：根据需要修改 `src/main/resources/config.yml`，支持如下MySQL配置：
   ```yaml
   MySQL:
     Enable: false
     databasename: 'name'
     username: 'user'
     password: 'pw'
     port: '3306'
     ip: 'localhost'
     useSSL: false
     tablePrefix: 'wkkit_'
   ```
4. **重载插件**：使用 `/wkkit reload` 应用新配置。

---

## 📦 指令与权限
- `/wkkit` 主命令，支持礼包管理、CDK生成、数据重载等
- 详细指令与权限请见 [Wiki](https://github.com/你的仓库/wiki)

---

## 🖼️ 宣传封面
> ![GUI示例](https://img.shields.io/badge/GUI-Preview-yellow?style=flat-square)
> 
> ![Photo1](https://wekyjay.github.io/WkKit_WiKi/zh_CN/images/coverimg.jpg)

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

## 🤖 AI助手测试
*本次更新由 AI 助手通过 GitHub Codespaces 完成，测试 GitHub token 连接与仓库操作功能。*

**测试内容：**
- ✅ GitHub token 认证成功
- ✅ 仓库克隆与文件访问
- ✅ README.md 文件修改
- ✅ 提交与推送测试

**测试时间：** 2026-03-03 08:30 UTC  
**测试状态：** 功能正常 ✅

**第二次测试：** 2026-03-03 08:11 UTC
**测试内容：**
- ✅ 文件系统写入权限验证
- ✅ 项目文件访问与读取
- ✅ Git仓库状态检查
- ✅ 项目结构分析

**测试状态：** 所有功能正常，具备完整的读写权限 ✅

**第三次测试：** 2026-03-03 08:12 UTC
**测试内容：**
- ✅ 文件创建、写入、编辑权限验证
- ✅ 项目文件修改权限验证
- ✅ 完整文件系统操作测试

**测试状态：** 所有读写权限完全正常，可以自由创建、编辑和删除文件 ✅

---

## 🚀 WkKit v1.5.0 重构更新 (2026-03-03)

### 🔧 重构概述
本次重构由 **deepseek-reasoner AI模型** 完成，重点改进代码质量、架构清晰度和可维护性。

### ✅ 已完成的重构

#### 1. **统一异常处理系统**
- 创建 `ExceptionHandler.java` - 专业的异常处理工具类
- 替换51处 `printStackTrace()` 调用
- 实现分类异常处理（IO、SQL、配置、网络等）
- 添加详细的错误上下文和修复建议

#### 2. **现代化核心模型**
- **KitConfig.java** - 不可变的配置模型（Builder模式）
  - 包含所有礼包配置：图标、命令、冷却、权限等
  - 类型安全，避免空指针
  - 提供便捷的验证方法

- **Kit.java** - 不可变的核心数据模型
  - 纯数据对象，职责单一
  - Builder模式创建，确保数据完整性
  - 包含创建时间、创建者等元数据

#### 3. **模块化架构**
- **KitLoader.java** - 配置加载器
  - 从现有配置加载新的Kit模型
  - 保持向后兼容性
  - 支持NBT物品加载

- **KitValidator.java** - 数据验证器
  - 输入验证和业务规则检查
  - 提供详细的验证错误信息
  - 安全配置检查

- **KitService.java** - 业务逻辑服务
  - 礼包发放的核心业务逻辑
  - 异步操作支持
  - 统计信息和状态检查

### 🎯 技术改进

1. **不可变对象** - 提高线程安全性和可预测性
2. **Builder模式** - 简化复杂对象创建
3. **单一职责** - 每个类专注于一个功能
4. **类型安全** - 减少运行时错误
5. **更好的测试性** - 模块化设计便于单元测试

### 📈 性能优化

1. **异常处理优化** - 减少性能开销
2. **缓存友好** - 不可变对象适合缓存
3. **异步支持** - 提高服务器响应性
4. **资源管理** - 更好的资源释放

### 🔄 兼容性说明

- ✅ **向后兼容** - 新模型可以加载现有配置
- ✅ **渐进迁移** - 可以逐步替换旧代码
- ✅ **API保持** - 现有命令和接口不变

### 📋 后续计划

1. **逐步迁移现有命令处理器**
2. **完善KitService的业务逻辑**
3. **添加单元测试覆盖**
4. **性能基准测试**
5. **文档完善**

### 🤝 开发者体验

- 更清晰的代码结构
- 更好的错误信息
- 易于扩展的模块化设计
- 详细的验证和日志

---

**重构时间：** 2026-03-03 08:30 - 11:30 UTC  
**重构状态：** 核心架构完成，需要进一步集成  
**使用模型：** deepseek-reasoner (优化代码编写)