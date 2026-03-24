# 条件分支和跳转功能指南

## 概述

流程控制服务现已支持**条件分支和动态跳转**功能，允许根据节点返回的数据动态决定下一个执行的节点。这使得流程引擎更加灵活，可以处理复杂的业务逻辑。

## 核心功能

### 1. 处理器返回结果控制

**FlowHandlerResult** 提供了多种跳转指令：

| 跳转类型 | 说明 | 使用场景 |
|---------|------|---------|
| NONE | 不跳转，按顺序执行下一个节点 | 默认行为 |
| JUMP_TO | 跳转到指定节点 | 条件判断后的分支 |
| SKIP_NEXT | 跳过下一个节点 | 条件满足时跳过某步骤 |
| END | 结束流程 | 提前终止 |
| ROLLBACK | 回退到指定节点 | 错误处理 |

### 2. 条件分支规则

**BranchRule** 支持多种条件类型：

| 条件类型 | 表达式示例 | 说明 |
|---------|-----------|------|
| EQUALS | `status=success` | 等于 |
| NOT_EQUALS | `status!=failed` | 不等于 |
| GREATER_THAN | `amount>1000` | 大于 |
| GREATER_EQUAL | `age>=18` | 大于等于 |
| LESS_THAN | `count<5` | 小于 |
| LESS_EQUAL | `count<=10` | 小于等于 |
| CONTAINS | `name=admin` | 包含 |
| NOT_CONTAINS | `error!=timeout` | 不包含 |
| IS_NULL | `email` | 为空 |
| IS_NOT_NULL | `userId` | 不为空 |
| BOOLEAN | `verified=true` | 布尔判断 |

## 使用方式

### 方式1：处理器返回跳转指令

实现 `ExtendedFlowHandler` 接口，返回 `FlowHandlerResult`：

```java
@Component
public class MyBranchHandler extends AbstractFlowHandler
        implements ExtendedFlowHandler {

    @Override
    public FlowHandlerResult handleWithResult(FlowContext context) {
        String userType = context.getData("userType", String.class);

        // 根据用户类型决定跳转
        if ("VIP".equals(userType)) {
            // VIP用户跳过审批，直接到完成
            return FlowHandlerResult.success("complete-order")
                    .message("VIP用户，跳过审批");
        } else {
            // 普通用户进入审批流程
            return FlowHandlerResult.success("approval");
        }
    }
}
```

### 方式2：配置分支规则

在流程配置中添加分支规则：

```java
// 创建节点
FlowNode node = new FlowNode();
node.setNodeId("risk-assessment");
node.setEnableBranch(true);

// 添加分支规则
BranchRule lowRiskRule = new BranchRule();
lowRiskRule.setBranchName("低风险分支");
lowRiskRule.setTargetNodeId("fast-process");
lowRiskRule.setCondition("riskLevel=low");
lowRiskRule.setConditionType(BranchRule.ConditionType.EQUALS);
lowRiskRule.setPriority(1);
node.addBranchRule(lowRiskRule);

// 添加默认分支
BranchRule defaultRule = new BranchRule();
defaultRule.setBranchName("默认分支");
defaultRule.setTargetNodeId("manual-review");
defaultRule.setDefaultBranch(true);
node.addBranchRule(defaultRule);
```

## 实际应用场景

### 场景1：用户审批流程

```java
FlowConfig config = FlowConfigBuilder
    .create("approval-flow", "审批流程")
    .addNode("check-user-type", "检查用户类型", "CheckUserTypeHandler", 1)
    .addNode("approval", "审批", "ApprovalHandler", 2)
    .addNode("complete", "完成", "CompleteHandler", 3)
    .build();
```

**处理器实现：**
```java
public FlowHandlerResult handleWithResult(FlowContext context) {
    String userType = context.getData("userType", String.class);

    if ("VIP".equals(userType)) {
        return FlowHandlerResult.success("complete")
                .message("VIP用户跳过审批");
    }

    return FlowHandlerResult.success("approval");
}
```

### 场景2：风险评估分支

```java
// 风险评估节点
FlowNode riskNode = new FlowNode();
riskNode.setNodeId("risk-assessment");
riskNode.setEnableBranch(true);

// 低风险 -> 快速处理
riskNode.addBranchRule(createRule("low", "fast-process", 1));

// 中风险 -> 正常处理
riskNode.addBranchRule(createRule("medium", "normal-process", 2));

// 高风险 -> 严格处理
riskNode.addBranchRule(createRule("high", "strict-process", 3));

// 默认 -> 人工审核
BranchRule defaultRule = new BranchRule();
defaultRule.setDefaultBranch(true);
defaultRule.setTargetNodeId("manual-review");
riskNode.addBranchRule(defaultRule);
```

### 场景3：订单金额分支

```java
public FlowHandlerResult handleWithResult(FlowContext context) {
    Double amount = context.getData("amount", Double.class);

    if (amount < 1000) {
        // 小额订单自动处理
        return FlowHandlerResult.success("auto-process");
    } else if (amount < 10000) {
        // 中额订单需要主管审批
        return FlowHandlerResult.success("manager-approval");
    } else {
        // 大额订单需要总经理审批
        return FlowHandlerResult.success("ceo-approval");
    }
}
```

## API接口

### 条件分支示例接口

```bash
# 执行VIP用户流程
curl -X POST http://localhost:8080/api/flow/branch/vip

# 执行普通用户流程
curl -X POST http://localhost:8080/api/flow/branch/normal

# 执行分支规则示例（低风险）
curl -X POST http://localhost:8080/api/flow/branch/rule?riskLevel=low

# 执行分支规则示例（高风险）
curl -X POST http://localhost:8080/api/flow/branch/rule?riskLevel=high

# 获取流程配置
curl http://localhost:8080/api/flow/branch/config
```

## FlowHandlerResult 使用指南

### 常用方法

```java
// 默认成功（继续执行）
FlowHandlerResult.success()

// 跳转到指定节点
FlowHandlerResult.success("target-node-id")

// 跳过下一个节点
FlowHandlerResult.skipNext()

// 结束流程
FlowHandlerResult.end()

// 回退到指定节点
FlowHandlerResult.rollback("target-node-id")
```

### 链式调用

```java
FlowHandlerResult.success("complete-order")
    .message("订单完成")
    .data(orderInfo)
```

## 分支规则配置

### 创建分支规则

```java
BranchRule rule = new BranchRule();
rule.setBranchName("规则名称");
rule.setTargetNodeId("目标节点ID");
rule.setCondition("条件表达式");
rule.setConditionType(BranchRule.ConditionType.EQUALS);
rule.setPriority(1); // 数字越小优先级越高
rule.setDefaultBranch(false); // 是否为默认分支
```

### 条件类型示例

```java
// 等于
.setCondition("status=success")
.setConditionType(BranchRule.ConditionType.EQUALS)

// 不等于
.setCondition("status!=failed")
.setConditionType(BranchRule.ConditionType.NOT_EQUALS)

// 大于
.setCondition("amount>1000")
.setConditionType(BranchRule.ConditionType.GREATER_THAN)

// 包含
.setCondition("name=admin")
.setConditionType(BranchRule.ConditionType.CONTAINS)

// 为空
.setCondition("email")
.setConditionType(BranchRule.ConditionType.IS_NULL)

// 布尔值
.setCondition("verified=true")
.setConditionType(BranchRule.ConditionType.BOOLEAN)
```

## 向后兼容性

✅ **完全向后兼容**

- 现有的 `FlowHandler` 接口继续可用
- 现有流程配置无需修改
- 新功能可选使用

## 最佳实践

### 1. 合理使用优先级
```java
// 高优先级规则先匹配
.addBranchRule(lowRiskRule, 1)
.addBranchRule(mediumRiskRule, 2)
.addBranchRule(highRiskRule, 3)
```

### 2. 设置默认分支
```java
BranchRule defaultRule = new BranchRule();
defaultRule.setDefaultBranch(true);
defaultRule.setTargetNodeId("manual-review");
node.addBranchRule(defaultRule);
```

### 3. 结合跳转指令
```java
public FlowHandlerResult handleWithResult(FlowContext context) {
    // 使用分支规则
    BranchRule matchedRule = getMatchedBranch(context);
    if (matchedRule != null) {
        return FlowHandlerResult.success(matchedRule.getTargetNodeId());
    }

    // 或直接根据业务逻辑判断
    if (shouldSkip(context)) {
        return FlowHandlerResult.skipNext();
    }

    return FlowHandlerResult.success();
}
```

## 注意事项

1. **循环检测**：系统有最大迭代次数限制（100次）
2. **优先级**：分支规则按优先级从高到低匹配
3. **默认分支**：建议始终设置默认分支
4. **条件评估**：条件表达式在每次节点执行时评估

## 完整示例

```java
@Service
public class OrderFlowService {

    public void initOrderFlow() {
        FlowConfig config = new FlowConfig("order-flow", "订单处理流程");

        // 1. 订单创建
        FlowNode createNode = new FlowNode();
        createNode.setNodeId("create-order");
        createNode.setNodeName("订单创建");
        createNode.setHandlerName("CreateOrderHandler");
        createNode.setOrder(1);
        config.addNode(createNode);

        // 2. 金额判断（条件分支）
        FlowNode amountCheckNode = new FlowNode();
        amountCheckNode.setNodeId("check-amount");
        amountCheckNode.setNodeName("金额检查");
        amountCheckNode.setHandlerName("AmountCheckHandler");
        amountCheckNode.setOrder(2);
        amountCheckNode.setEnableBranch(true);

        // 小额订单 -> 快速处理
        BranchRule smallAmount = new BranchRule();
        smallAmount.setCondition("amount<1000");
        smallAmount.setConditionType(BranchRule.ConditionType.LESS_THAN);
        smallAmount.setTargetNodeId("auto-approve");
        smallAmount.setPriority(1);
        amountCheckNode.addBranchRule(smallAmount);

        // 中额订单 -> 需要审批
        BranchRule mediumAmount = new BranchRule();
        mediumAmount.setCondition("amount>=1000");
        mediumAmount.setCondition("amount<10000");
        mediumAmount.setConditionType(BranchRule.ConditionType.GREATER_EQUAL);
        mediumAmount.setTargetNodeId("manager-approval");
        mediumAmount.setPriority(2);
        amountCheckNode.addBranchRule(mediumAmount);

        // 大额订单 -> 总经理审批
        BranchRule largeAmount = new BranchRule();
        largeAmount.setCondition("amount>=10000");
        largeAmount.setConditionType(BranchRule.ConditionType.GREATER_EQUAL);
        largeAmount.setTargetNodeId("ceo-approval");
        largeAmount.setPriority(3);
        amountCheckNode.addBranchRule(largeAmount);

        config.addNode(amountCheckNode);

        // 3. 自动审批
        FlowNode autoApproveNode = new FlowNode();
        autoApproveNode.setNodeId("auto-approve");
        autoApproveNode.setNodeName("自动审批");
        autoApproveNode.setHandlerName("AutoApproveHandler");
        autoApproveNode.setOrder(3);
        config.addNode(autoApproveNode);

        // 4. 经理审批
        FlowNode managerApprovalNode = new FlowNode();
        managerApprovalNode.setNodeId("manager-approval");
        managerApprovalNode.setNodeName("经理审批");
        managerApprovalNode.setHandlerName("ManagerApprovalHandler");
        managerApprovalNode.setOrder(3);
        config.addNode(managerApprovalNode);

        // 5. 总经理审批
        FlowNode ceoApprovalNode = new FlowNode();
        ceoApprovalNode.setNodeId("ceo-approval");
        ceoApprovalNode.setNodeName("总经理审批");
        ceoApprovalNode.setHandlerName("CeoApprovalHandler");
        ceoApprovalNode.setOrder(3);
        config.addNode(ceoApprovalNode);

        // 6. 订单完成
        FlowNode completeNode = new FlowNode();
        completeNode.setNodeId("complete-order");
        completeNode.setNodeName("订单完成");
        completeNode.setHandlerName("CompleteOrderHandler");
        completeNode.setOrder(4);
        config.addNode(completeNode);

        flowEngine.registerFlowConfig(config);
    }
}
```

条件分支功能让流程引擎真正具备了工作流的核心能力！
