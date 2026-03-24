package xuml.study.com.common.flow;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 流程上下文
 * 用于在流程处理器之间传递数据
 *
 * @author xuml
 */
@Getter
@Setter
public class FlowContext {

    /**
     * 流程唯一标识
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
     * 当前执行的节点
     */
    private String currentNode;

    /**
     * 业务数据
     */
    private Map<String, Object> data;

    /**
     * 扩展数据
     */
    private Map<String, Object> extData;

    /**
     * 是否中断流程
     * -- GETTER --
     *  是否已中断

     */
    @Getter
    private boolean interrupted;

    /**
     * 中断原因
     */
    private String interruptReason;

    /**
     * 是否正在回退
     */
    private boolean rollingBack;

    /**
     * 回退目标节点ID
     * -- GETTER --
     *  获取回退目标节点

     */
    @Getter
    private String rollbackTargetNodeId;

    /**
     * 当前节点的执行次数（用于限制重试次数）
     * -- GETTER --
     *  获取当前节点的执行次数

     */
    @Getter
    private int nodeExecutionCount;

    /**
     * 回退历史记录（节点ID -> 执行次数）
     */
    private Map<String, Integer> nodeExecutionHistory;

    public FlowContext() {
        this.data = new HashMap<>();
        this.extData = new HashMap<>();
        this.nodeExecutionHistory = new HashMap<>();
        this.nodeExecutionCount = 0;
    }

    public FlowContext(String flowId, String flowName) {
        this();
        this.flowId = flowId;
        this.flowName = flowName;
    }

    /**
     * 设置业务数据
     */
    public FlowContext setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 获取业务数据
     */
    public Object getData(String key) {
        return this.data.get(key);
    }

    /**
     * 获取业务数据（带类型）
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, Class<T> clazz) {
        Object value = this.data.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * 设置扩展数据
     */
    public FlowContext setExtData(String key, Object value) {
        this.extData.put(key, value);
        return this;
    }

    /**
     * 获取扩展数据
     */
    public Object getExtData(String key) {
        return this.extData.get(key);
    }

    /**
     * 中断流程
     */
    public void interrupt(String reason) {
        this.interrupted = true;
        this.interruptReason = reason;
    }

    /**
     * 请求回退到指定节点
     */
    public void requestRollback(String targetNodeId) {
        this.rollingBack = true;
        this.rollbackTargetNodeId = targetNodeId;
        this.interruptReason = "请求回退到节点: " + targetNodeId;
    }

    /**
     * 重置回退状态
     */
    public void resetRollback() {
        this.rollingBack = false;
        this.rollbackTargetNodeId = null;
    }

    /**
     * 是否正在回退
     */
    public boolean caisRollingBack() {
        return this.rollingBack;
    }

    /**
     * 增加当前节点的执行次数
     */
    public void incrementNodeExecutionCount() {
        this.nodeExecutionCount++;
        String currentNode = this.currentNode;
        if (currentNode != null) {
            this.nodeExecutionHistory.put(currentNode, this.nodeExecutionHistory.getOrDefault(currentNode, 0) + 1);
        }
    }

    /**
     * 获取指定节点的执行次数
     */
    public int getNodeExecutionCount(String nodeId) {
        return this.nodeExecutionHistory.getOrDefault(nodeId, 0);
    }

    /**
     * 重置当前节点的执行次数
     */
    public void resetNodeExecutionCount() {
        this.nodeExecutionCount = 0;
        String currentNode = this.currentNode;
        if (currentNode != null) {
            this.nodeExecutionHistory.put(currentNode, 0);
        }
    }
}
