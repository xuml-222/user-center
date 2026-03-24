package xuml.study.com.common.flow;

import lombok.Data;

/**
 * 流程处理器执行结果
 * 用于处理器返回执行结果和下一步跳转指令
 *
 * @author xuml
 */
@Data
public class FlowHandlerResult {

    /**
     * 是否继续执行
     */
    private boolean continueFlow = true;

    /**
     * 下一个节点ID（可选）
     * 如果设置，流程会跳转到指定节点，而不是按顺序执行下一个节点
     */
    private String nextNodeId;

    /**
     * 跳转类型
     */
    private JumpType jumpType = JumpType.NONE;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 扩展数据
     */
    private Object data;

    /**
     * 跳转类型枚举
     */
    public enum JumpType {
        /**
         * 不跳转，按顺序执行下一个节点
         */
        NONE,

        /**
         * 跳转到指定节点
         */
        JUMP_TO,

        /**
         * 跳过下一个节点（执行下下一个）
         */
        SKIP_NEXT,

        /**
         * 结束流程
         */
        END,

        /**
         * 回退
         */
        ROLLBACK
    }

    /**
     * 创建默认成功结果（继续执行）
     */
    public static FlowHandlerResult success() {
        FlowHandlerResult result = new FlowHandlerResult();
        result.setContinueFlow(true);
        result.setJumpType(JumpType.NONE);
        return result;
    }

    /**
     * 创建成功结果并指定下一个节点
     */
    public static FlowHandlerResult success(String nextNodeId) {
        FlowHandlerResult result = new FlowHandlerResult();
        result.setContinueFlow(true);
        result.setNextNodeId(nextNodeId);
        result.setJumpType(JumpType.JUMP_TO);
        return result;
    }

    /**
     * 创建成功结果并指定跳转类型
     */
    public static FlowHandlerResult success(JumpType jumpType) {
        FlowHandlerResult result = new FlowHandlerResult();
        result.setContinueFlow(true);
        result.setJumpType(jumpType);
        return result;
    }

    /**
     * 创建成功结果并指定下一个节点和跳转类型
     */
    public static FlowHandlerResult success(String nextNodeId, JumpType jumpType) {
        FlowHandlerResult result = new FlowHandlerResult();
        result.setContinueFlow(true);
        result.setNextNodeId(nextNodeId);
        result.setJumpType(jumpType);
        return result;
    }

    /**
     * 创建结束流程结果
     */
    public static FlowHandlerResult end() {
        FlowHandlerResult result = new FlowHandlerResult();
        result.setContinueFlow(false);
        result.setJumpType(JumpType.END);
        return result;
    }

    /**
     * 创建跳过下一个节点结果
     */
    public static FlowHandlerResult skipNext() {
        FlowHandlerResult result = new FlowHandlerResult();
        result.setContinueFlow(true);
        result.setJumpType(JumpType.SKIP_NEXT);
        return result;
    }

    /**
     * 创建回退结果
     */
    public static FlowHandlerResult rollback(String targetNodeId) {
        FlowHandlerResult result = new FlowHandlerResult();
        result.setContinueFlow(false);
        result.setNextNodeId(targetNodeId);
        result.setJumpType(JumpType.ROLLBACK);
        return result;
    }

    /**
     * 设置消息（支持链式调用）
     */
    public FlowHandlerResult message(String message) {
        this.message = message;
        return this;
    }

    /**
     * 设置扩展数据（支持链式调用）
     */
    public FlowHandlerResult data(Object data) {
        this.data = data;
        return this;
    }
}
