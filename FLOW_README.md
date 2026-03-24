# 流程控制服务使用说明

## 概述

本流程控制服务实现了流程配置与业务逻辑的完全解耦，使用**责任链模式 + 配置化**的设计思路。

## 核心组件

### 1. FlowContext（流程上下文）
用于在流程处理器之间传递数据。

```java
FlowContext context = new FlowContext("user-register-flow", "用户注册");
context.setData("username", "testuser");
context.setData("email", "test@example.com");
String username = context.getData("username", String.class);
```

### 2. FlowHandler（处理器接口）
所有业务处理器都需要实现此接口。

```java
public interface FlowHandler {
    boolean handle(FlowContext context) throws FlowException;
    String getName();
    int getOrder();
    boolean isEnabled();
}
```

### 3. FlowConfig（流程配置）
定义流程的结构、节点顺序、条件等。

```java
FlowConfig config = FlowConfigBuilder
    .create("flow-id", "流程名称")
    .version("1.0.0")
    .addNode("node1", "节点1", "Handler1", 1)
    .addNode("node2", "节点2", "Handler2", 2)
    .build();
```

### 4. FlowEngine（流程引擎）
负责根据配置动态组装和执行处理器链。

```java
@Autowired
private FlowEngine flowEngine;

// 注册配置
flowEngine.registerFlowConfig(config);

// 执行流程
FlowResult result = flowEngine.execute("flow-id", context);
```

## 快速开始

### 1. 创建业务处理器

继承 `AbstractFlowHandler` 基类：

```java
@Component
public class MyHandler extends AbstractFlowHandler {

    @Override
    public String getName() {
        return "MyHandler";  // 用于配置中的handlerName
    }

    @Override
    protected boolean validate(FlowContext context) {
        // 前置校验逻辑
        return true;
    }

    @Override
    protected boolean doHandle(FlowContext context) throws FlowException {
        // 具体业务逻辑
        String data = context.getData("key", String.class);
        // 处理业务...
        return true;  // 返回true继续执行，false终止流程
    }
}
```

### 2. 创建流程配置

```java
@PostConstruct
public void initFlowConfigs() {
    FlowConfig config = FlowConfigBuilder
        .create("my-flow", "我的流程")
        .version("1.0.0")
        .addNode("step1", "步骤1", "MyHandler1", 1)
        .addNode("step2", "步骤2", "MyHandler2", 2, true, true)
        .addNode("step3", "步骤3", "MyHandler3", 3, true, false) // 非必需节点
        .build();

    flowEngine.registerFlowConfig(config);
}
```

### 3. 执行流程

```java
@Autowired
private FlowEngine flowEngine;

public void executeMyFlow() {
    FlowContext context = new FlowContext("my-flow", "我的流程");
    context.setData("key1", "value1");
    context.setData("key2", "value2");

    FlowResult result = flowEngine.execute("my-flow", context);

    if (result.isSuccess()) {
        log.info("流程执行成功");
    } else {
        log.error("流程执行失败: {}", result.getErrorMessage());
    }
}
```

## 高级特性

### 1. 条件执行

节点可以设置条件表达式，满足条件才执行：

```java
.addNode("conditional-node", "条件节点", "MyHandler", 1, true, true, "someCondition")
```

### 2. 非必需节点

非必需节点执行失败不会中断流程：

```java
// 设置required=false
.addNode("optional-node", "可选节点", "MyHandler", 1, true, false)
```

### 3. 流程中断

处理器可以主动中断流程：

```java
if (shouldStop) {
    context.interrupt("中断原因");
    return false;
}
```

### 4. 扩展数据

处理器可以设置扩展数据供后续节点使用：

```java
context.setExtData("validated", true);
boolean validated = context.getExtData("validated");
```

## API接口

项目提供了完整的REST API：

- `POST /api/flow/register` - 执行用户注册流程
- `GET /api/flow/config` - 获取流程配置
- `POST /api/flow/config` - 更新流程配置
- `GET /api/flow/health` - 流程健康检查

## 项目结构

```
user-common/src/main/java/xuml/study/com/common/flow/
├── FlowContext.java           # 流程上下文
├── FlowHandler.java           # 处理器接口
├── AbstractFlowHandler.java   # 处理器抽象基类
├── FlowException.java         # 流程异常
├── FlowNode.java              # 流程节点配置
├── FlowConfig.java            # 流程配置
├── FlowConfigBuilder.java     # 流程配置构建器
├── FlowResult.java            # 流程执行结果
└── FlowEngine.java            # 流程引擎

user-service/src/main/java/xuml/study/com/service/flow/
├── UserFlowService.java       # 用户流程服务
├── example/
│   ├── UserValidationHandler.java        # 校验处理器示例
│   ├── UserExistenceCheckHandler.java    # 存在性检查处理器示例
│   ├── UserDataSaveHandler.java          # 数据保存处理器示例
│   └── UserNotificationHandler.java      # 通知处理器示例
└── controller/
    └── UserFlowController.java            # 流程控制器
```

## 优势

1. **完全解耦**：流程配置与业务逻辑分离
2. **灵活配置**：通过配置动态调整流程
3. **易于扩展**：新增处理器无需修改核心代码
4. **可视化友好**：配置易于转化为流程图
5. **支持热更新**：可动态更新流程配置
6. **容错性强**：支持非必需节点和错误处理

## 使用场景

- 用户注册流程
- 订单处理流程
- 审批流程
- 数据同步流程
- 任何需要多步骤处理的业务场景
