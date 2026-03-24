# 流程回退功能使用指南

## 概述

流程回退功能允许在节点执行失败或条件不满足时，回退到之前的节点重新执行，大大增强了流程的容错能力和灵活性。

## 回退策略

FlowNode.RollbackStrategy 提供了4种回退策略：

### 1. NONE（不回退）
默认策略，节点执行失败时不回退。

```java
.addNode("node1", "节点1", "Handler1", 1)
// 默认就是NONE策略
```

### 2. PREVIOUS（回退到上一个节点）
节点失败时回退到流程中的上一个节点。

```java
.addNodeWithRollback(
    "node2", "节点2", "Handler2", 2,
    true, true, null,
    FlowNode.RollbackStrategy.PREVIOUS, null, 3 // 最多回退3次
)
```

### 3. SPECIFIC（回退到指定节点）
节点失败时回退到指定的节点ID。

```java
.addNodeWithRollback(
    "node3", "节点3", "Handler3", 3,
    true, true, null,
    FlowNode.RollbackStrategy.SPECIFIC, "node1", 2 // 回退到node1，最多2次
)
```

### 4. RETRY（重试当前节点）
节点失败时重新执行当前节点。

```java
.addNodeWithRollback(
    "save-data", "保存数据", "SaveHandler", 1,
    true, true, null,
    FlowNode.RollbackStrategy.RETRY, null, 3 // 重试当前节点，最多3次
)
```

## 回退配置参数

### maxRollbackTimes（最大回退次数）
限制节点最多可以回退的次数，防止无限循环。

```java
.setMaxRollbackTimes(3) // 最多回退3次
```

### rollbackCondition（回退条件）
可选的回退条件表达式，只有满足条件时才回退。

```java
.setRollbackCondition("shouldRollback") // 当context.getData("shouldRollback")为true时回退
```

## 主动请求回退

处理器可以主动请求回退到指定节点：

```java
@Component
public class MyHandler extends AbstractFlowHandler {

    @Override
    protected boolean doHandle(FlowContext context) throws FlowException {
        // 检查某些条件
        if (needRollback()) {
            // 主动请求回退到指定节点
            context.requestRollback("prepare-data");
            return false; // 返回false表示中断当前执行
        }

        // 正常处理逻辑
        return true;
    }
}
```

## 回退历史

FlowResult 记录了完整的回退历史：

```java
FlowResult result = flowEngine.execute("flow-id", context);

// 获取总回退次数
int totalRollbackCount = result.getTotalRollbackCount();

// 获取回退历史列表
List<FlowResult.RollbackRecord> history = result.getRollbackHistory();
for (FlowResult.RollbackRecord record : history) {
    System.out.println("从 " + record.getFromNodeId()
            + " 回退到 " + record.getToNodeId()
            + "，原因: " + record.getReason());
}
```

## 使用场景

### 场景1：数据校验失败回退
```java
// 数据校验失败时，回退到数据准备节点重新准备数据
.addNode("prepare-data", "准备数据", "PrepareHandler", 1)
.addNodeWithRollback(
    "validate-data", "校验数据", "ValidateHandler", 2,
    true, true, null,
    FlowNode.RollbackStrategy.SPECIFIC, "prepare-data", 1
)
```

### 场景2：网络重试
```java
// 网络调用失败时重试，最多3次
.addNodeWithRollback(
    "call-api", "调用API", "ApiHandler", 1,
    true, true, null,
    FlowNode.RollbackStrategy.RETRY, null, 3
)
```

### 场景3：分步操作失败回退
```java
// 分步操作：准备->处理->验证->保存
// 任何步骤失败都回退到准备阶段
.addNode("prepare", "准备", "PrepareHandler", 1)
.addNode("process", "处理", "ProcessHandler", 2)
.addNode("validate", "验证", "ValidateHandler", 3)
.addNodeWithRollback(
    "save", "保存", "SaveHandler", 4,
    true, true, null,
    FlowNode.RollbackStrategy.SPECIFIC, "prepare", 1
)
```

## 执行次数追踪

FlowContext 追踪每个节点的执行次数：

```java
// 获取当前节点的执行次数
int count = context.getNodeExecutionCount();

// 获取指定节点的执行次数
int node1Count = context.getNodeExecutionCount("node1");

// 重置当前节点的执行次数
context.resetNodeExecutionCount();
```

## 注意事项

1. **无限循环防护**：引擎内部有最大迭代次数限制（默认100次），防止无限循环
2. **必需节点限制**：必需节点（required=true）失败时，如果回退策略为NONE，流程会终止
3. **非必需节点**：非必需节点失败不会中断流程，但可能仍会触发回退
4. **回退条件**：可以配置回退条件，只有满足条件时才回退

## API接口

回退功能提供了专门的REST API：

- `POST /api/flow/rollback/init` - 初始化回退示例流程
- `POST /api/flow/rollback/execute` - 执行回退示例流程（触发回退）
- `POST /api/flow/rollback/normal` - 执行正常流程（不触发回退）
- `GET /api/flow/rollback/config` - 获取流程配置

## 完整示例

```java
@Service
public class MyFlowService {

    @Autowired
    private FlowEngine flowEngine;

    public void initFlow() {
        FlowConfig config = FlowConfigBuilder
                .create("my-flow", "我的流程")
                .version("1.0.0")

                // 准备数据
                .addNode("prepare", "准备", "PrepareHandler", 1)

                // 校验数据：失败回退到准备节点
                .addNodeWithRollback(
                    "validate", "校验", "ValidateHandler", 2,
                    true, true, null,
                    FlowNode.RollbackStrategy.SPECIFIC, "prepare", 3
                )

                // 处理数据：失败重试
                .addNodeWithRollback(
                    "process", "处理", "ProcessHandler", 3,
                    true, true, null,
                    FlowNode.RollbackStrategy.RETRY, null, 2
                )

                // 保存数据：失败回退到上一个节点
                .addNodeWithRollback(
                    "save", "保存", "SaveHandler", 4,
                    true, true, null,
                    FlowNode.RollbackStrategy.PREVIOUS, null, 1
                )

                .build();

        flowEngine.registerFlowConfig(config);
    }

    public FlowResult execute() throws FlowException {
        FlowContext context = new FlowContext("my-flow", "我的流程");
        // 设置初始数据
        context.setData("input", "value");

        FlowResult result = flowEngine.execute("my-flow", context);

        // 查看回退信息
        if (result.getTotalRollbackCount() > 0) {
            System.out.println("发生了 " + result.getTotalRollbackCount() + " 次回退");
        }

        return result;
    }
}
```
