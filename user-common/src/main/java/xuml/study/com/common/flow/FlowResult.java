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

    public FlowResult() {
        this.executedNodes = new ArrayList<>();
        this.extData = new HashMap<>();
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
}
