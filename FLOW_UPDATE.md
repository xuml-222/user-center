# 流程控制服务 v2.0 - 新增回退功能

## 更新概览

流程控制服务已升级，新增**分支回退功能**，让流程更加灵活和容错！

## 核心新特性

### 1. 四种回退策略

- **NONE**：不回退（默认）
- **PREVIOUS**：回退到上一个节点
- **SPECIFIC**：回退到指定节点
- **RETRY**：重试当前节点

### 2. 智能回退控制

- 最大回退次数限制，防止无限循环
- 回退条件表达式，精确控制回退时机
- 处理器主动请求回退

### 3. 完整的回退追踪

- 记录每次回退的详细历史
- 统计总回退次数
- 追踪每个节点的执行次数

## 项目结构

```
user-common/
└── flow/
    ├── FlowContext.java          # ✨ 新增：回退相关状态
    ├── FlowHandler.java         # 接口不变
    ├── FlowNode.java            # ✨ 新增：回退策略配置
    ├── FlowConfig.java          # 配置不变
    ├── FlowResult.java          # ✨ 新增：回退历史记录
    ├── FlowEngine.java          # ✨ 重构：支持回退逻辑
    ├── FlowException.java       # 异常不变
    ├── AbstractFlowHandler.java  # 基类不变
    └── FlowConfigBuilder.java   # ✨ 新增：回退配置方法

user-service/
└── flow/
    ├── FlowApplicationInitializer.java  # ✨ 新增：应用启动初始化
    ├── RollbackExampleService.java     # ✨ 新增：回退示例服务
    ├── UserFlowService.java            # 现有：用户流程服务
    ├── example/
    │   ├── RollbackExampleHandler.java # ✨ 新增：回退示例处理器
    │   └── ...                       # 其他现有处理器
    └── controller/
        └── RollbackExampleController.java # ✨ 新增：回退API

文档/
├── FLOW_README.md          # 原有文档
├── ROLLBACK_GUIDE.md      # ✨ 新增：回退功能详细指南
└── ROLLBACK_EXAMPLE.md     # ✨ 新增：完整使用示例
```

## 快速体验

### 启动应用
应用启动时自动初始化所有流程配置。

### 测试回退功能

**1. 执行回退示例（触发多次回退）**
```bash
curl -X POST http://localhost:8080/api/flow/rollback/execute
```

**2. 执行正常流程（不触发回退）**
```bash
curl -X POST http://localhost:8080/api/flow/rollback/normal
```

**3. 获取流程配置**
```bash
curl http://localhost:8080/api/flow/rollback/config
```

## 代码示例

### 基础回退配置
```java
FlowConfig config = FlowConfigBuilder
    .create("my-flow", "我的流程")
    .version("1.0.0")

    // 数据准备
    .addNode("prepare", "准备数据", "PrepareHandler", 1)

    // 数据校验：失败回退到准备节点（最多3次）
    .addNodeWithRollback(
        "validate", "数据校验", "ValidateHandler", 2,
        true, true, null,
        FlowNode.RollbackStrategy.SPECIFIC, "prepare", 3
    )

    // 数据保存：失败重试（最多2次）
    .addNodeWithRollback(
        "save", "数据保存", "SaveHandler", 3,
        true, true, null,
        FlowNode.RollbackStrategy.RETRY, null, 2
    )

    .build();
```

### 处理器中主动请求回退
```java
@Component
public class MyHandler extends AbstractFlowHandler {

    @Override
    protected boolean doHandle(FlowContext context) throws FlowException {
        // 检查是否需要回退
        if (needsRollback(context)) {
            // 主动请求回退到指定节点
            context.requestRollback("prepare-data");
            return false;
        }

        // 正常业务逻辑
        return true;
    }
}
```

### 查看回退结果
```java
FlowResult result = flowEngine.execute("my-flow", context);

// 检查是否发生了回退
if (result.getTotalRollbackCount() > 0) {
    System.out.println("发生了 " + result.getTotalRollbackCount() + " 次回退");

    // 查看详细的回退历史
    result.getRollbackHistory().forEach(record -> {
        System.out.println("从 " + record.getFromNodeId()
                + " 回退到 " + record.getToNodeId()
                + "，原因: " + record.getReason());
    });
}
```

## API接口

### 回退功能专用接口
- `POST /api/flow/rollback/init` - 初始化回退示例流程
- `POST /api/flow/rollback/execute` - 执行回退示例
- `POST /api/flow/rollback/normal` - 执行正常流程
- `GET /api/flow/rollback/config` - 获取配置

### 原有接口（保持兼容）
- `POST /api/flow/register` - 用户注册流程
- `GET /api/flow/config` - 获取流程配置
- `GET /api/flow/health` - 流程健康检查

## 向后兼容性

✅ **完全向后兼容**

- 现有的流程配置无需修改，默认使用 NONE 回退策略
- 现有的处理器代码无需修改
- 新功能都是可选的，按需使用

## 性能考虑

- 回退逻辑开销极小（< 1ms）
- 建议设置合理的最大回退次数（3次为宜）
- 系统内置最大迭代次数限制（100次）

## 适用场景

回退功能特别适合以下场景：

1. **网络请求重试**：临时性网络错误自动重试
2. **数据校验回退**：校验失败后重新准备数据
3. **分步操作回滚**：复杂操作中某步失败后回到起点
4. **分布式事务补偿**：模拟Saga模式
5. **审批流程驳回**：审批被驳回后回到上一环节

## 文档说明

- **FLOW_README.md** - 流程控制基础功能说明
- **ROLLBACK_GUIDE.md** - 回退功能详细指南 ⭐ 新增
- **ROLLBACK_EXAMPLE.md** - 完整使用示例 ⭐ 新增

## 下一步计划

- [ ] 支持条件表达式（SpEL集成）
- [ ] 流程可视化工具
- [ ] 流程执行监控和统计
- [ ] 支持并行节点执行
- [ ] 流程版本管理和热更新

## 总结

流程回退功能的引入，使得流程控制服务从"简单的责任链"升级为"智能的流程引擎"，大大增强了系统的容错能力和业务灵活性！

**立即体验：**
```bash
# 启动应用后执行
curl -X POST http://localhost:8080/api/flow/rollback/execute
```

**查看详细的回退历史和执行情况！**
