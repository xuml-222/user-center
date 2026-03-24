# 流程控制服务 v3.0 - 条件分支与动态跳转

## 🎉 重大更新

流程控制服务再次升级，新增**条件分支和动态跳转**功能！现在的流程引擎支持根据节点返回数据智能决定下一个执行节点，真正实现了工作流的核心能力。

## 🚀 新增功能

### 1. 扩展处理器接口

**ExtendedFlowHandler**
- 支持返回复杂的执行结果
- 可以控制流程走向和跳转

**FlowHandlerResult**
- `JUMP_TO` - 跳转到指定节点
- `SKIP_NEXT` - 跳过下一个节点
- `END` - 结束流程
- `ROLLBACK` - 回退到指定节点
- 支持链式调用和扩展数据

### 2. 条件分支规则

**BranchRule**
- 12种条件类型（等于、不等于、大于、小于、包含、为空等）
- 优先级控制
- 默认分支支持
- 灵活的条件表达式

### 3. 节点分支配置

**FlowNode** 新增属性：
- `branchRules` - 分支规则列表
- `enableBranch` - 是否启用条件分支

## 📁 新增文件

### 核心框架（user-common模块）
1. **FlowHandlerResult.java** - 处理器执行结果类
2. **ExtendedFlowHandler.java** - 扩展处理器接口
3. **BranchRule.java** - 分支规则类

### 示例实现（user-service模块）
1. **ConditionalBranchHandler.java** - 条件分支处理器示例
2. **BranchExampleService.java** - 分支示例服务
3. **BranchExampleController.java** - 分支API控制器

### 文档
1. **BRANCH_GUIDE.md** - 条件分支功能详细指南 ⭐

## 🔧 修改的文件

### 核心框架
1. **FlowEngine.java**
   - 支持扩展处理器接口
   - 处理跳转指令
   - 支持条件分支评估
   - 实现节点跳转逻辑

2. **FlowNode.java**
   - 新增分支规则支持
   - 新增分支评估方法

3. **FlowConfigBuilder.java**
   - 新增分支配置方法

## 📊 功能对比

| 功能 | v1.0 | v2.0 | v3.0 |
|------|-------|-------|-------|
| 基础责任链 | ✅ | ✅ | ✅ |
| 流程配置 | ✅ | ✅ | ✅ |
| 条件执行 | ✅ | ✅ | ✅ |
| 必需/非必需节点 | ✅ | ✅ | ✅ |
| 回退功能 | ❌ | ✅ | ✅ |
| 4种回退策略 | ❌ | ✅ | ✅ |
| 回退历史 | ❌ | ✅ | ✅ |
| 扩展处理器接口 | ❌ | ❌ | ✅ |
| 跳转指令 | ❌ | ❌ | ✅ |
| 条件分支 | ❌ | ❌ | ✅ |
| 12种条件类型 | ❌ | ❌ | ✅ |

## 🎯 使用场景

### 1. 用户类型分支
```java
// VIP用户跳过审批，普通用户进入审批流程
if ("VIP".equals(userType)) {
    return FlowHandlerResult.success("complete-order");
} else {
    return FlowHandlerResult.success("approval");
}
```

### 2. 金额判断分支
```java
if (amount < 1000) {
    return FlowHandlerResult.success("auto-process");
} else if (amount < 10000) {
    return FlowHandlerResult.success("manager-approval");
} else {
    return FlowHandlerResult.success("ceo-approval");
}
```

### 3. 风险评估分支
```java
// 低风险 -> 快速处理
// 中风险 -> 正常处理
// 高风险 -> 严格处理
// 默认 -> 人工审核
```

## 🌐 API接口

### 条件分支接口
- `POST /api/flow/branch/vip` - 执行VIP用户流程
- `POST /api/flow/branch/normal` - 执行普通用户流程
- `POST /api/flow/branch/rule?riskLevel=xxx` - 执行分支规则示例
- `GET /api/flow/branch/config` - 获取流程配置

### 回退功能接口（v2.0）
- `POST /api/flow/rollback/init` - 初始化回退示例
- `POST /api/flow/rollback/execute` - 执行回退示例
- `POST /api/flow/rollback/normal` - 执行正常流程

### 基础接口（v1.0）
- `POST /api/flow/register` - 用户注册流程
- `GET /api/flow/config` - 获取配置
- `GET /api/flow/health` - 健康检查

## 🔄 完全向后兼容

✅ **所有现有代码无需修改**

- `FlowHandler` 接口继续可用
- 现有流程配置保持不变
- 新功能完全可选

## 📈 项目统计

| 模块 | 类数量 | 文件数量 |
|------|--------|----------|
| user-common（流程核心） | 12 | 12 |
| user-service（示例实现） | 12 | 12 |
| user-service（控制器） | 3 | 3 |
| 测试代码 | 2 | 2 |
| 文档 | 5 | 5 |
| **总计** | **34** | **34** |

## 📖 文档结构

```
文档/
├── FLOW_README.md          # 基础功能指南
├── ROLLBACK_GUIDE.md      # 回退功能详细指南
├── ROLLBACK_EXAMPLE.md     # 回退功能使用示例
├── FLOW_UPDATE.md         # v2.0更新说明
└── BRANCH_GUIDE.md        # v3.0条件分支指南 ⭐ 新增
```

## 💡 核心特性

### 1. 智能跳转
```java
// 跳转到指定节点
FlowHandlerResult.success("target-node-id")

// 跳过下一个节点
FlowHandlerResult.skipNext()

// 结束流程
FlowHandlerResult.end()

// 回退到指定节点
FlowHandlerResult.rollback("target-node-id")
```

### 2. 灵活的条件判断
```java
// 等于
rule.setCondition("status=success")
rule.setConditionType(BranchRule.ConditionType.EQUALS)

// 大于
rule.setCondition("amount>1000")
rule.setConditionType(BranchRule.ConditionType.GREATER_THAN)

// 包含
rule.setCondition("name=admin")
rule.setConditionType(BranchRule.ConditionType.CONTAINS)

// 布尔值
rule.setCondition("verified=true")
rule.setConditionType(BranchRule.ConditionType.BOOLEAN)
```

### 3. 优先级控制
```java
// 按优先级匹配分支
lowRiskRule.setPriority(1)     // 优先匹配
mediumRiskRule.setPriority(2)
highRiskRule.setPriority(3)
```

## 🎨 架构优势

1. **高度灵活** - 支持运行时动态决策
2. **易于配置** - 声明式配置，无需编码
3. **向后兼容** - 渐进式升级
4. **类型安全** - 强类型，减少错误
5. **易于扩展** - 插件化设计

## 🚦 适用场景

条件分支功能特别适合：

1. **用户认证** - VIP/普通用户不同流程
2. **订单处理** - 金额/类型不同的审批流程
3. **风险管理** - 不同风险等级的处理方式
4. **工作流** - 条件审批、分级处理
5. **数据处理** - 不同类型数据的不同处理逻辑

## 📚 快速开始

### 1. 实现扩展处理器
```java
@Component
public class MyHandler implements ExtendedFlowHandler {
    @Override
    public FlowHandlerResult handleWithResult(FlowContext context) {
        String type = context.getData("type", String.class);

        if ("A".equals(type)) {
            return FlowHandlerResult.success("node-a");
        } else if ("B".equals(type)) {
            return FlowHandlerResult.success("node-b");
        }

        return FlowHandlerResult.success("default-node");
    }
}
```

### 2. 配置流程
```java
FlowConfig config = FlowConfigBuilder
    .create("my-flow", "我的流程")
    .addNode("check-type", "类型检查", "MyHandler", 1)
    .addNode("node-a", "处理A", "HandlerA", 2)
    .addNode("node-b", "处理B", "HandlerB", 3)
    .addNode("default-node", "默认处理", "DefaultHandler", 4)
    .build();

flowEngine.registerFlowConfig(config);
```

### 3. 执行流程
```java
FlowContext context = new FlowContext("my-flow", "我的流程");
context.setData("type", "A");

FlowResult result = flowEngine.execute("my-flow", context);
```

## 🔮 未来规划

- [ ] 支持并行节点执行
- [ ] 流程可视化工具
- [ ] SpEL表达式引擎集成
- [ ] 流程版本管理
- [ ] 流程执行监控
- [ ] 分布式流程支持

## ✨ 总结

流程控制服务从简单的责任链，进化为具备：
- ✅ 回退机制（v2.0）
- ✅ 条件分支（v3.0）
- ✅ 动态跳转（v3.0）

的**企业级流程引擎**！

现在可以轻松实现复杂的业务流程，如：
- 多级审批
- 条件分支
- 动态路由
- 错误恢复

**立即体验：**
```bash
# 测试VIP用户流程
curl -X POST http://localhost:8080/api/flow/branch/vip

# 测试不同风险等级
curl -X POST http://localhost:8080/api/flow/branch/rule?riskLevel=low
curl -X POST http://localhost:8080/api/flow/branch/rule?riskLevel=high
```

**查看详细的执行结果和分支跳转！**
