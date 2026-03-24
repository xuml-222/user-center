package xuml.study.com.common.flow;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程配置
 *
 * @author xuml
 */
@Data
public class FlowConfig {

    /**
     * 流程ID
     */
    private String flowId;

    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 流程版本
     */
    private String flowVersion;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 流程节点列表
     */
    private List<FlowNode> nodes;

    /**
     * 流程描述
     */
    private String description;

    public FlowConfig() {
        this.nodes = new ArrayList<>();
        this.enabled = true;
    }

    public FlowConfig(String flowId, String flowName) {
        this();
        this.flowId = flowId;
        this.flowName = flowName;
    }

    /**
     * 添加节点
     */
    public FlowConfig addNode(FlowNode node) {
        this.nodes.add(node);
        return this;
    }

    /**
     * 根据节点ID获取节点
     */
    public FlowNode getNode(String nodeId) {
        return this.nodes.stream()
                .filter(node -> nodeId.equals(node.getNodeId()))
                .findFirst()
                .orElse(null);
    }
}
