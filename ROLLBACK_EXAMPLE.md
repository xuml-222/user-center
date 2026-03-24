# 流程回退功能完整示例

## 快速开始

### 1. 启动应用
应用启动时会自动初始化所有流程配置，包括：
- 用户注册流程
- 回退示例流程

### 2. 测试回退功能

#### 执行回退示例（会触发多次回退）
```bash
curl -X POST http://localhost:8080/api/flow/rollback/execute
```

预期结果：
- 会触发数据校验失败，回退到准备数据节点
- 会触发数据保存失败，重试当前节点
- 最终执行成功

#### 执行正常流程（不触发回退）
```bash
curl -X POST http://localhost:8080/api/flow/rollback/normal
```

预期结果：
- 提供了完整数据，不会触发任何回退
- 流程正常执行完成

## 实际应用场景

### 场景1：订单处理流程
```java
FlowConfig orderFlow = FlowConfigBuilder
    .create("order-process", "订单处理")
    .addNode("validate", "订单校验", "OrderValidateHandler", 1)
    .addNode("lock-stock", "库存锁定", "LockStockHandler", 2)
    .addNodeWithRollback(
        "payment", "支付", "PaymentHandler", 3,
        true, true, null,
        FlowNode.RollbackStrategy.SPECIFIC, "lock-stock", 2 // 支付失败回退到库存锁定
    )
    .addNode("confirm-order", "订单确认", "ConfirmOrderHandler", 4)
    .build();
```

### 场景2：用户注册流程
```java
FlowConfig registerFlow = FlowConfigBuilder
    .create("user-register", "用户注册")
    .addNode("validate", "数据校验", "ValidateHandler", 1)
    .addNode("check-exist", "存在性检查", "CheckExistHandler", 2)
    .addNodeWithRollback(
        "save-user", "保存用户", "SaveUserHandler", 3,
        true, true, null,
        FlowNode.RollbackStrategy.RETRY, null, 3 // 保存失败重试3次
    )
    .addNode("send-email", "发送邮件", "SendEmailHandler", 4)
    .build();
```

### 场景3：数据同步流程
```java
FlowConfig syncFlow = FlowConfigBuilder
    .create("data-sync", "数据同步")
    .addNode("prepare-data", "准备数据", "PrepareDataHandler", 1)
    .addNode("transform", "数据转换", "TransformHandler", 2)
    .addNodeWithRollback(
        "upload", "数据上传", "UploadHandler", 3,
        true, true, null,
        FlowNode.RollbackStrategy.RETRY, null, 3 // 上传失败重试3次
    )
    .addNodeWithRollback(
        "verify", "数据验证", "VerifyHandler", 4,
        true, true, null,
        FlowNode.RollbackStrategy.PREVIOUS, null, 1 // 验证失败回退到上一个节点
    )
    .addNode("cleanup", "清理临时数据", "CleanupHandler", 5)
    .build();
```

## 回退策略选择指南

| 策略 | 适用场景 | 示例 |
|------|---------|------|
| NONE | 一步到位，不需要回退 | 简单的日志记录 |
| PREVIOUS | 失败时回到前一步 | 数据处理失败后回到准备阶段 |
| SPECIFIC | 失败时回到特定节点 | 支付失败后回到库存锁定 |
| RETRY | 临时性错误，需要重试 | 网络请求、文件写入 |

## 最佳实践

### 1. 设置合理的最大回退次数
```java
// 推荐：3次重试
.setMaxRollbackTimes(3)

// 避免无限制
// .setMaxRollbackTimes(Integer.MAX_VALUE) // ❌ 不要这样做
```

### 2. 使用回退条件
```java
// 只有在特定错误时才回退
.setRollbackCondition("shouldRollback")

// 在处理器中设置条件
if (isTemporaryError(error)) {
    context.setData("shouldRollback", true);
} else {
    context.setData("shouldRollback", false);
}
```

### 3. 处理器中主动请求回退
```java
@Component
public class MyHandler extends AbstractFlowHandler {

    @Override
    protected boolean doHandle(FlowContext context) {
        // 检查业务逻辑
        if (needsRetry(context)) {
            // 计算应该回退到哪个节点
            String targetNode = determineRollbackTarget(context);
            context.requestRollback(targetNode);
            return false;
        }

        return true;
    }
}
```

### 4. 结合非必需节点
```java
// 关键节点失败时回退
.addNodeWithRollback(
    "critical-step", "关键步骤", "CriticalHandler", 2,
    true, true, null,
    FlowNode.RollbackStrategy.RETRY, null, 3
)

// 非关键节点失败不中断流程
.addNode(
    "optional-step", "可选步骤", "OptionalHandler", 3,
    true, false // required=false
)
```

## 监控和调试

### 查看回退历史
```java
FlowResult result = flowEngine.execute("flow-id", context);

// 输出回退信息
if (result.getTotalRollbackCount() > 0) {
    System.out.println("发生了 " + result.getTotalRollbackCount() + " 次回退:");
    result.getRollbackHistory().forEach(record -> {
        System.out.println("  " + record.getFromNodeId() + " -> " + record.getToNodeId());
    });
}
```

### 查看节点执行次数
```java
// 在处理器中查看当前节点的执行次数
int count = context.getNodeExecutionCount();
log.info("当前节点第 {} 次执行", count);

// 如果超过某个次数，可以做一些特殊处理
if (count > 3) {
    log.warn("节点已执行多次，可能存在问题");
    // 采取降级措施...
}
```

## 常见问题

### Q: 如何防止无限循环？
A: 系统内部有最大迭代次数限制（默认100次），同时建议设置合理的最大回退次数。

### Q: 回退会影响性能吗？
A: 回退本身的开销很小，但要注意不要设置过大的重试次数。

### Q: 如何在回退时传递额外信息？
A: 使用 context.setData() 和 context.getExtData() 在节点间传递数据。

### Q: 可以跳过某些节点吗？
A: 可以通过设置节点的 enabled=false 或者 condition 来控制跳过。
