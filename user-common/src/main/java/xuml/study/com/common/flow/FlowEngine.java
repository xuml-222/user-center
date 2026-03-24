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

        for (FlowNode node : nodes) {
            if (context.isInterrupted()) {
                log.info("流程已中断: flowId={}, 原因={}", config.getFlowId(), context.getInterruptReason());
                result.getExtData().put("interruptReason", context.getInterruptReason());
                break;
            }

            if (!node.isEnabled()) {
                log.debug("节点已跳过: nodeId={}, 原因=未启用", node.getNodeId());
                continue;
            }

            // 检查条件表达式（可选实现）
            if (!checkCondition(node, context)) {
                log.debug("节点已跳过: nodeId={}, 原因=条件不满足", node.getNodeId());
                continue;
            }

            // 执行节点
            executeNode(node, context, result);

            // 如果节点执行失败且为必需节点，则终止流程
            if (!result.isSuccess() && node.isRequired()) {
                log.error("必需节点执行失败，终止流程: nodeId={}, error={}", node.getNodeId(), result.getErrorMessage());
                break;
            }
        }

        return result;
    }

    /**
     * 执行单个节点
     */
    private void executeNode(FlowNode node, FlowContext context, FlowResult result) {
        long startTime = System.currentTimeMillis();
        context.setCurrentNode(node.getNodeId());

        log.info("开始执行节点: flowId={}, nodeId={}, handler={}",
                context.getFlowId(), node.getNodeId(), node.getHandlerName());

        try {
            FlowHandler handler = getHandler(node.getHandlerName());
            if (handler == null) {
                String errorMsg = "处理器不存在: " + node.getHandlerName();
                log.error(errorMsg);
                result.fail("HANDLER_NOT_FOUND", node.getNodeId(), errorMsg);
                return;
            }

            if (!handler.isEnabled()) {
                log.debug("处理器已跳过: handler={}, 原因=未启用", handler.getName());
                result.addExecutedNode(node.getNodeId(), node.getNodeName(), true, "处理器未启用", 0L);
                return;
            }

            // 执行处理器
            boolean continueFlow = handler.handle(context);

            long executeTime = System.currentTimeMillis() - startTime;

            if (continueFlow) {
                log.info("节点执行成功: flowId={}, nodeId={}, 耗时={}ms",
                        context.getFlowId(), node.getNodeId(), executeTime);
                result.addExecutedNode(node.getNodeId(), node.getNodeName(), true, "执行成功", executeTime);
            } else {
                log.info("节点返回终止信号: flowId={}, nodeId={}, 耗时={}ms",
                        context.getFlowId(), node.getNodeId(), executeTime);
                result.addExecutedNode(node.getNodeId(), node.getNodeName(), true, "处理器返回终止信号", executeTime);
                result.setSuccess(true);
            }

        } catch (FlowException e) {
            long executeTime = System.currentTimeMillis() - startTime;
            log.error("节点执行异常: flowId={}, nodeId={}, error={}", context.getFlowId(), node.getNodeId(), e.getMessage());
            result.addExecutedNode(node.getNodeId(), node.getNodeName(), false, e.getMessage(), executeTime);
            result.fail(e.getCode() != null ? e.getCode() : "FLOW_EXCEPTION", node.getNodeId(), e.getMessage());

            if (!node.isRequired()) {
                // 非必需节点失败，重置成功状态，继续执行
                result.setSuccess(true);
            }

        } catch (Exception e) {
            long executeTime = System.currentTimeMillis() - startTime;
            log.error("节点执行异常: flowId={}, nodeId={}, error={}", context.getFlowId(), node.getNodeId(), e.getMessage(), e);
            result.addExecutedNode(node.getNodeId(), node.getNodeName(), false, e.getMessage(), executeTime);
            result.fail("SYSTEM_EXCEPTION", node.getNodeId(), e.getMessage());

            if (!node.isRequired()) {
                // 非必需节点失败，重置成功状态，继续执行
                result.setSuccess(true);
            }
        }
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
