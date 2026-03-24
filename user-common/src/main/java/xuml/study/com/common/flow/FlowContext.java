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
     */
    private boolean interrupted;

    /**
     * 中断原因
     */
    private String interruptReason;

    public FlowContext() {
        this.data = new HashMap<>();
        this.extData = new HashMap<>();
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
     * 是否已中断
     */
    public boolean isInterrupted() {
        return this.interrupted;
    }
}
