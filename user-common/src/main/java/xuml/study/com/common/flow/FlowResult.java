package xuml.study.com.common.flow;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程执行结果
 *
 * @author xuml
 */
@Data
public class FlowResult {

    /**
     * 是否成功
     */
    private boolean success;

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
     * 执行的节点列表
     */
    private List<ExecutedNode> executedNodes;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误节点
     */
    private String errorNode;

    /**
     * 扩展数据
     */
    private Map<String, Object> extData;

    /**
     * 回退历史记录
     */
    private List<RollbackRecord> rollbackHistory;

    /**
     * 总回退次数
     */
    private int totalRollbackCount;

    public FlowResult() {
        this.executedNodes = new ArrayList<>();
        this.extData = new HashMap<>();
        this.rollbackHistory = new ArrayList<>();
        this.totalRollbackCount = 0;
    }

    /**
     * 添加已执行节点
     */
    public void addExecutedNode(String nodeId, String nodeName, boolean success, String message, long executeTime) {
        this.executedNodes.add(new ExecutedNode(nodeId, nodeName, success, message, executeTime));
    }

    /**
     * 标记失败
     */
    public void fail(String errorCode, String errorNode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorNode = errorNode;
        this.errorMessage = errorMessage;
    }

    /**
     * 添加回退记录
     */
    public void addRollbackRecord(String fromNodeId, String toNodeId, String reason) {
        this.rollbackHistory.add(new RollbackRecord(fromNodeId, toNodeId, reason));
        this.totalRollbackCount++;
    }

    /**
     * 已执行节点信息
     */
    @Data
    public static class ExecutedNode {
        private String nodeId;
        private String nodeName;
        private boolean success;
        private String message;
        private long executeTime;

        public ExecutedNode(String nodeId, String nodeName, boolean success, String message, long executeTime) {
            this.nodeId = nodeId;
            this.nodeName = nodeName;
            this.success = success;
            this.message = message;
            this.executeTime = executeTime;
        }
    }

    /**
     * 回退记录
     */
    @Data
    public static class RollbackRecord {
        private String fromNodeId;
        private String toNodeId;
        private String reason;
        private long timestamp;

        public RollbackRecord(String fromNodeId, String toNodeId, String reason) {
            this.fromNodeId = fromNodeId;
            this.toNodeId = toNodeId;
            this.reason = reason;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
