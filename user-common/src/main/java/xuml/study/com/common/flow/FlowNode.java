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
}
