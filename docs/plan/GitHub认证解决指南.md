# GitHub认证问题解决指南

## 🔍 问题描述
**当前状态**: 使用GitHub token认证失败 (token已从文档中移除)
**错误信息**: `fatal: Authentication failed for 'https://github.com/WekyJay/WkKit.git/'`
**影响**: 无法推送代码到GitHub仓库

## 🎯 解决目标
1. 恢复GitHub仓库访问权限
2. 建立稳定的认证机制
3. 确保后续自动更新不受影响

## 🔧 解决方案

### 方案1: 使用SSH密钥认证 (推荐)
**步骤**:
1. 生成新的SSH密钥对
   ```bash
   ssh-keygen -t ed25519 -C "wkkit-updater@openclaw"
   ```
2. 将公钥添加到GitHub账户
   - 访问: https://github.com/settings/keys
   - 点击 "New SSH key"
   - 粘贴公钥内容 (`~/.ssh/id_ed25519.pub`)
3. 更新远程仓库URL为SSH格式
   ```bash
   git remote set-url origin git@github.com:WekyJay/WkKit.git
   ```
4. 测试连接
   ```bash
   ssh -T git@github.com
   ```

### 方案2: 获取新的Personal Access Token
**步骤**:
1. 生成新的PAT (Classic)
   - 访问: https://github.com/settings/tokens
   - 点击 "Generate new token (classic)"
   - 权限选择: `repo` (完全控制仓库)
   - 过期时间: 90天 (建议)
2. 更新远程仓库URL
   ```bash
   git remote set-url origin https://WekyJay:NEW_TOKEN@github.com/WekyJay/WkKit.git
   ```
3. 测试推送
   ```bash
   git push origin master
   ```

### 方案3: 使用GitHub App Token
**步骤**:
1. 创建GitHub App (如果需要)
2. 生成App安装token
3. 使用JWT进行认证
4. 配置仓库访问权限

## 📋 实施计划

### 阶段1: 诊断和准备
1. 检查当前认证配置
   ```bash
   git remote -v
   cat ~/.git-credentials
   ```
2. 验证网络连接
   ```bash
   curl -I https://github.com
   ```
3. 检查token状态 (如果可能)

### 阶段2: 实施解决方案
**优先级顺序**:
1. 尝试SSH密钥 (最稳定)
2. 获取新PAT token (次选)
3. 其他认证方式 (备选)

### 阶段3: 验证和测试
1. 测试认证连接
2. 执行简单推送测试
3. 验证权限完整性
4. 记录解决方案

## 🛡️ 安全考虑

### 密钥管理
1. **SSH密钥**: 使用密码保护，定期轮换
2. **PAT token**: 设置合理过期时间，最小权限原则
3. **环境变量**: 不硬编码敏感信息

### 访问控制
1. **权限范围**: 仅限必要仓库
2. **操作限制**: 仅推送权限，不涉及敏感操作
3. **监控日志**: 记录所有认证活动

## 🔄 自动化集成

### 定时任务集成
```json
{
  "schedule": {
    "kind": "cron",
    "expr": "0 9 * * *",
    "tz": "UTC"
  },
  "payload": {
    "kind": "agentTurn",
    "message": "执行GitHub认证检查，然后开始每日更新..."
  }
}
```

### 错误处理
1. **认证失败**: 自动重试，记录日志
2. **网络问题**: 等待重试，指数退避
3. **权限不足**: 暂停任务，等待人工干预

## 📊 进度跟踪

### 成功标志
- [ ] SSH密钥生成和配置
- [ ] GitHub公钥添加成功
- [ ] SSH连接测试通过
- [ ] 仓库推送验证
- [ ] 定时任务集成测试

### 问题解决验证
1. **连接测试**: `ssh -T git@github.com` 返回成功
2. **推送测试**: `git push origin master` 无错误
3. **权限验证**: 可以创建分支、提交、推送
4. **稳定性**: 连续多次操作无失败

## 💡 最佳实践

### 长期维护
1. **定期轮换**: SSH密钥每6个月轮换一次
2. **监控告警**: 设置认证失败告警
3. **备份方案**: 准备多个认证方式
4. **文档更新**: 保持认证配置文档最新

### 故障恢复
1. **快速恢复**: 准备备用token
2. **逐步降级**: 从SSH降级到HTTPS
3. **人工介入**: 严重问题时请求协助
4. **回滚方案**: 恢复到上一个可用状态

## 📝 实施记录

### 当前状态记录
**时间**: 2026-03-03 14:40 UTC
**问题**: PAT token认证失败
**影响**: 代码推送受阻
**临时方案**: 本地提交，等待解决

### 解决过程记录
**计划开始**: 2026-03-04 09:00 UTC
**负责人**: AI助手 (deepseek-reasoner)
**预计完成**: 2026-03-04 10:00 UTC

### 结果验证记录
**待填写**: 解决后的验证结果
**待填写**: 使用的具体方案
**待填写**: 遇到的挑战和解决方案

---

**指南创建时间**: 2026-03-03 14:40 UTC  
**计划执行时间**: 2026-03-04 09:00 UTC  
**目标解决时间**: 2026-03-04 10:00 UTC