package xuml.study.com.common.flow;

import lombok.Data;

/**
 * 流程节点配置
 *
 * @author xuml
 */
@Data
public class FlowNode {

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 处理器Bean名称
     */
    private String handlerName;

    /**
     * 节点顺序
     */
    private int order;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 是否必须执行（false表示即使失败也继续执行下一个节点）
     */
    private boolean required;

    /**
     * 条件表达式（可选）
     */
    private String condition;

    /**
     * 节点描述
     */
    private String description;

    /**
     * 回退策略
     * NONE - 不回退
     * PREVIOUS - 回退到上一个节点
     * SPECIFIC - 回退到指定节点
     */
    private RollbackStrategy rollbackStrategy = RollbackStrategy.NONE;

    /**
     * 回退目标节点ID（当rollbackStrategy为SPECIFIC时使用）
     */
    private String rollbackTargetNodeId;

    /**
     * 最大回退次数
     */
    private int maxRollbackTimes = 0;

    /**
     * 回退条件表达式（可选）
     */
    private String rollbackCondition;

    /**
     * 回退策略枚举
     */
    public enum RollbackStrategy {
        NONE,       // 不回退
        PREVIOUS,   // 回退到上一个节点
        SPECIFIC,   // 回退到指定节点
        RETRY       // 重试当前节点
    }
}
