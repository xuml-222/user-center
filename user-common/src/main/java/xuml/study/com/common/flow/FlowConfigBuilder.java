package xuml.study.com.common.flow;

/**
 * 流程配置构建器
 * 方便快速构建流程配置
 *
 * @author xuml
 */
public class FlowConfigBuilder {

    private final FlowConfig config;

    private FlowConfigBuilder(String flowId, String flowName) {
        this.config = new FlowConfig(flowId, flowName);
    }

    public static FlowConfigBuilder create(String flowId, String flowName) {
        return new FlowConfigBuilder(flowId, flowName);
    }

    public FlowConfigBuilder version(String version) {
        this.config.setFlowVersion(version);
        return this;
    }

    public FlowConfigBuilder enabled(boolean enabled) {
        this.config.setEnabled(enabled);
        return this;
    }

    public FlowConfigBuilder description(String description) {
        this.config.setDescription(description);
        return this;
    }

    public FlowConfigBuilder addNode(String nodeId, String nodeName, String handlerName, int order) {
        FlowNode node = new FlowNode();
        node.setNodeId(nodeId);
        node.setNodeName(nodeName);
        node.setHandlerName(handlerName);
        node.setOrder(order);
        node.setEnabled(true);
        node.setRequired(true);
        config.addNode(node);
        return this;
    }

    public FlowConfigBuilder addNode(String nodeId, String nodeName, String handlerName, int order, boolean enabled, boolean required) {
        FlowNode node = new FlowNode();
        node.setNodeId(nodeId);
        node.setNodeName(nodeName);
        node.setHandlerName(handlerName);
        node.setOrder(order);
        node.setEnabled(enabled);
        node.setRequired(required);
        config.addNode(node);
        return this;
    }

    public FlowConfigBuilder addNode(String nodeId, String nodeName, String handlerName, int order, boolean enabled, boolean required, String condition) {
        FlowNode node = new FlowNode();
        node.setNodeId(nodeId);
        node.setNodeName(nodeName);
        node.setHandlerName(handlerName);
        node.setOrder(order);
        node.setEnabled(enabled);
        node.setRequired(required);
        node.setCondition(condition);
        config.addNode(node);
        return this;
    }

    /**
     * 添加带回退配置的节点（指定回退目标）
     */
    public FlowConfigBuilder addNodeWithRollback(
            String nodeId, String nodeName, String handlerName, int order,
            boolean enabled, boolean required, String condition,
            FlowNode.RollbackStrategy rollbackStrategy, String rollbackTargetNodeId, int maxRollbackTimes) {
        FlowNode node = new FlowNode();
        node.setNodeId(nodeId);
        node.setNodeName(nodeName);
        node.setHandlerName(handlerName);
        node.setOrder(order);
        node.setEnabled(enabled);
        node.setRequired(required);
        node.setCondition(condition);
        node.setRollbackStrategy(rollbackStrategy);
        node.setRollbackTargetNodeId(rollbackTargetNodeId);
        node.setMaxRollbackTimes(maxRollbackTimes);
        config.addNode(node);
        return this;
    }

    /**
     * 添加带回退配置的节点（使用RETRY或PREVIOUS策略）
     */
    public FlowConfigBuilder addNodeWithRollback(
            String nodeId, String nodeName, String handlerName, int order,
            boolean enabled, boolean required, String condition,
            FlowNode.RollbackStrategy rollbackStrategy, int maxRollbackTimes) {
        return addNodeWithRollback(
                nodeId, nodeName, handlerName, order,
                enabled, required, condition,
                rollbackStrategy, null, maxRollbackTimes
        );
    }

    public FlowConfig build() {
        return this.config;
    }
}
