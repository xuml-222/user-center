package xuml.study.com.common.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程引擎
 * 负责根据流程配置动态组装和执行处理器链
 *
 * @author xuml
 */
@Slf4j
@Component
public class FlowEngine {

    /**
     * 流程配置缓存（可替换为数据库）
     */
    private final Map<String, FlowConfig> flowConfigCache = new ConcurrentHashMap<>();

    /**
     * 处理器缓存（从Spring容器获取）
     */
    private final Map<String, FlowHandler> handlerCache = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private List<FlowHandler> allHandlers;

    /**
     * 注册流程配置
     */
    public void registerFlowConfig(FlowConfig config) {
        flowConfigCache.put(config.getFlowId(), config);
        log.info("注册流程配置: flowId={}, flowName={}, 节点数={}",
                config.getFlowId(), config.getFlowName(), config.getNodes().size());
    }

    /**
     * 执行流程
     *
     * @param flowId  流程ID
     * @param context 流程上下文
     * @return 执行结果
     */
    public FlowResult execute(String flowId, FlowContext context) throws FlowException {
        FlowConfig config = flowConfigCache.get(flowId);
        if (config == null) {
            throw new FlowException("FLOW_NOT_FOUND", flowId, "流程配置不存在: " + flowId);
        }

        return execute(config, context);
    }

    /**
     * 执行流程
     *
     * @param config  流程配置
     * @param context 流程上下文
     * @return 执行结果
     */
    public FlowResult execute(FlowConfig config, FlowContext context) throws FlowException {
        FlowResult result = new FlowResult();
        result.setSuccess(true);
        result.setFlowId(config.getFlowId());
        result.setFlowName(config.getFlowName());
        result.setFlowVersion(config.getFlowVersion());

        if (!config.isEnabled()) {
            result.fail("FLOW_DISABLED", null, "流程已禁用");
            return result;
        }

        // 设置流程信息到上下文
        context.setFlowId(config.getFlowId());
        context.setFlowName(config.getFlowName());
        context.setFlowVersion(config.getFlowVersion());

        // 按顺序执行节点
        List<FlowNode> nodes = config.getNodes();
        nodes.sort(Comparator.comparingInt(FlowNode::getOrder));

        int maxIterations = 100; // 防止无限循环
        int iterationCount = 0;

        while (iterationCount++ < maxIterations) {
            // 如果正在回退，找到回退目标节点
            if (context.isRollingBack()) {
                String targetNodeId = context.getRollbackTargetNodeId();
                FlowNode targetNode = findNodeById(nodes, targetNodeId);
                if (targetNode != null) {
                    log.info("执行回退: flowId={}, 从当前节点回退到: {}",
                            config.getFlowId(), targetNodeId);
                    context.resetRollback();
                    context.setCurrentNode(targetNodeId);

                    // 记录回退历史
                    result.addRollbackRecord(context.getCurrentNode(), targetNodeId, context.getInterruptReason());
                } else {
                    log.warn("回退目标节点不存在: nodeId={}", targetNodeId);
                    context.resetRollback();
                }
            }

            boolean nodeExecuted = false;

            for (FlowNode node : nodes) {
                if (context.isInterrupted()) {
                    log.info("流程已中断: flowId={}, 原因={}", config.getFlowId(), context.getInterruptReason());
                    result.getExtData().put("interruptReason", context.getInterruptReason());
                    return result;
                }

                // 如果正在回退，只执行回退目标节点及之后的节点
                if (context.isRollingBack()) {
                    String targetNodeId = context.getRollbackTargetNodeId();
                    if (node.getNodeId().equals(targetNodeId)) {
                        // 找到回退目标节点，开始执行
                        context.resetRollback();
                    } else if (context.getNodeExecutionCount(node.getNodeId()) > 0) {
                        // 跳过已执行的节点（直到找到回退目标）
                        continue;
                    }
                }

                if (!node.isEnabled()) {
                    log.debug("节点已跳过: nodeId={}, 原因=未启用", node.getNodeId());
                    continue;
                }

                // 检查条件表达式
                if (!checkCondition(node, context)) {
                    log.debug("节点已跳过: nodeId={}, 原因=条件不满足", node.getNodeId());
                    continue;
                }

                // 执行节点
                NodeExecutionResult nodeResult = executeNode(node, context);

                // 记录执行结果
                result.addExecutedNode(
                        node.getNodeId(),
                        node.getNodeName(),
                        nodeResult.success,
                        nodeResult.message,
                        nodeResult.executeTime
                );

                nodeExecuted = true;

                // 检查是否需要回退
                if (!nodeResult.success && shouldRollback(node, context)) {
                    String rollbackTarget = determineRollbackTarget(node, nodes);
                    if (rollbackTarget != null) {
                        log.info("节点执行失败，准备回退: flowId={}, currentNode={}, targetNode={}",
                                config.getFlowId(), node.getNodeId(), rollbackTarget);
                        context.requestRollback(rollbackTarget);
                        break; // 跳出当前循环，开始回退
                    }
                }

                // 如果节点执行失败且为必需节点，则终止流程
                if (!nodeResult.success && node.isRequired()) {
                    result.fail(nodeResult.errorCode, node.getNodeId(), nodeResult.message);
                    log.error("必需节点执行失败，终止流程: nodeId={}, error={}", node.getNodeId(), nodeResult.message);
                    return result;
                }

                // 如果处理器返回终止信号，则终止流程
                if (!nodeResult.continueFlow) {
                    return result;
                }
            }

            // 如果没有节点被执行（比如都在回退后），退出循环
            if (!nodeExecuted && !context.isRollingBack()) {
                break;
            }
        }

        log.warn("流程执行达到最大迭代次数: maxIterations={}", maxIterations);
        return result;
    }

    /**
     * 执行单个节点
     *
     * @return 节点执行结果
     */
    private NodeExecutionResult executeNode(FlowNode node, FlowContext context) {
        long startTime = System.currentTimeMillis();
        context.setCurrentNode(node.getNodeId());
        context.incrementNodeExecutionCount();

        NodeExecutionResult result = new NodeExecutionResult();

        log.info("开始执行节点: flowId={}, nodeId={}, handler={}, 执行次数={}",
                context.getFlowId(), node.getNodeId(), node.getHandlerName(), context.getNodeExecutionCount());

        try {
            FlowHandler handler = getHandler(node.getHandlerName());
            if (handler == null) {
                String errorMsg = "处理器不存在: " + node.getHandlerName();
                log.error(errorMsg);
                result.success = false;
                result.message = errorMsg;
                result.errorCode = "HANDLER_NOT_FOUND";
                result.executeTime = System.currentTimeMillis() - startTime;
                return result;
            }

            if (!handler.isEnabled()) {
                log.debug("处理器已跳过: handler={}, 原因=未启用", handler.getName());
                result.success = true;
                result.continueFlow = true;
                result.message = "处理器未启用";
                result.executeTime = 0L;
                return result;
            }

            // 执行处理器
            boolean continueFlow = handler.handle(context);

            result.executeTime = System.currentTimeMillis() - startTime;

            if (continueFlow) {
                log.info("节点执行成功: flowId={}, nodeId={}, 耗时={}ms",
                        context.getFlowId(), node.getNodeId(), result.executeTime);
                result.success = true;
                result.continueFlow = true;
                result.message = "执行成功";
            } else {
                log.info("节点返回终止信号: flowId={}, nodeId={}, 耗时={}ms",
                        context.getFlowId(), node.getNodeId(), result.executeTime);
                result.success = true;
                result.continueFlow = false;
                result.message = "处理器返回终止信号";
            }

        } catch (FlowException e) {
            result.executeTime = System.currentTimeMillis() - startTime;
            log.error("节点执行异常: flowId={}, nodeId={}, error={}", context.getFlowId(), node.getNodeId(), e.getMessage());
            result.success = false;
            result.continueFlow = false;
            result.message = e.getMessage();
            result.errorCode = e.getCode() != null ? e.getCode() : "FLOW_EXCEPTION";

        } catch (Exception e) {
            result.executeTime = System.currentTimeMillis() - startTime;
            log.error("节点执行异常: flowId={}, nodeId={}, error={}", context.getFlowId(), node.getNodeId(), e.getMessage(), e);
            result.success = false;
            result.continueFlow = false;
            result.message = e.getMessage();
            result.errorCode = "SYSTEM_EXCEPTION";
        }

        return result;
    }

    /**
     * 检查是否需要回退
     */
    private boolean shouldRollback(FlowNode node, FlowContext context) {
        // 检查回退策略
        if (node.getRollbackStrategy() == FlowNode.RollbackStrategy.NONE) {
            return false;
        }

        // 检查回退条件（如果配置了）
        if (node.getRollbackCondition() != null && !node.getRollbackCondition().isEmpty()) {
            Object conditionValue = context.getData(node.getRollbackCondition());
            if (conditionValue instanceof Boolean && !(Boolean) conditionValue) {
                return false;
            }
        }

        // 检查回退次数限制
        if (node.getMaxRollbackTimes() > 0) {
            int executionCount = context.getNodeExecutionCount(node.getNodeId());
            if (executionCount >= node.getMaxRollbackTimes()) {
                log.warn("节点已达到最大回退次数限制: nodeId={}, count={}, max={}",
                        node.getNodeId(), executionCount, node.getMaxRollbackTimes());
                return false;
            }
        }

        return true;
    }

    /**
     * 确定回退目标节点
     */
    private String determineRollbackTarget(FlowNode node, List<FlowNode> nodes) {
        FlowNode.RollbackStrategy strategy = node.getRollbackStrategy();

        switch (strategy) {
            case PREVIOUS:
                // 回退到上一个节点
                int currentIndex = findNodeIndex(nodes, node.getNodeId());
                if (currentIndex > 0) {
                    return nodes.get(currentIndex - 1).getNodeId();
                }
                break;

            case SPECIFIC:
                // 回退到指定节点
                if (node.getRollbackTargetNodeId() != null) {
                    return node.getRollbackTargetNodeId();
                }
                break;

            case RETRY:
                // 重试当前节点
                return node.getNodeId();

            case NONE:
            default:
                return null;
        }

        return null;
    }

    /**
     * 根据节点ID查找节点
     */
    private FlowNode findNodeById(List<FlowNode> nodes, String nodeId) {
        for (FlowNode node : nodes) {
            if (nodeId.equals(node.getNodeId())) {
                return node;
            }
        }
        return null;
    }

    /**
     * 查找节点索引
     */
    private int findNodeIndex(List<FlowNode> nodes, String nodeId) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodeId.equals(nodes.get(i).getNodeId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 节点执行结果
     */
    private static class NodeExecutionResult {
        boolean success;
        boolean continueFlow;
        String message;
        String errorCode;
        long executeTime;
    }

    /**
     * 获取处理器
     */
    private FlowHandler getHandler(String handlerName) {
        FlowHandler handler = handlerCache.get(handlerName);
        if (handler != null) {
            return handler;
        }

        // 从Spring容器中查找
        if (allHandlers != null) {
            for (FlowHandler h : allHandlers) {
                if (handlerName.equals(h.getName())) {
                    handlerCache.put(handlerName, h);
                    return h;
                }
            }
        }

        return null;
    }

    /**
     * 检查条件表达式
     * （可根据需要集成SpEL或其他表达式引擎）
     */
    private boolean checkCondition(FlowNode node, FlowContext context) {
        if (node.getCondition() == null || node.getCondition().isEmpty()) {
            return true;
        }

        // TODO: 可集成SpEL表达式引擎
        // 目前简单实现：检查上下文中是否有对应的条件数据
        Object conditionValue = context.getData(node.getCondition());
        if (conditionValue instanceof Boolean) {
            return (Boolean) conditionValue;
        }

        return true;
    }

    /**
     * 获取流程配置
     */
    public FlowConfig getFlowConfig(String flowId) {
        return flowConfigCache.get(flowId);
    }

    /**
     * 清除流程配置缓存
     */
    public void clearFlowConfig(String flowId) {
        flowConfigCache.remove(flowId);
    }

    /**
     * 清除所有流程配置缓存
     */
    public void clearAllFlowConfigs() {
        flowConfigCache.clear();
    }
}
