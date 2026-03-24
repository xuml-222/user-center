package xuml.study.com.common.flow;

/**
 * 扩展流程处理器接口
 * 支持返回更复杂的执行结果（跳转、分支等）
 *
 * @author xuml
 */
public interface ExtendedFlowHandler extends FlowHandler {

    /**
     * 执行流程（扩展版）
     * 返回执行结果，可以控制流程走向
     *
     * @param context 流程上下文
     * @return 执行结果
     * @throws FlowException 流程异常
     */
    FlowHandlerResult handleWithResult(FlowContext context) throws FlowException;

    /**
     * 默认实现：调用基础接口的方法
     * 向后兼容
     */
    @Override
    default boolean handle(FlowContext context) throws FlowException {
        FlowHandlerResult result = handleWithResult(context);
        return result.isContinueFlow();
    }

    /**
     * 判断是否使用扩展接口
     * 默认使用扩展接口
     */
    default boolean useExtendedInterface() {
        return true;
    }
}
