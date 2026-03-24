package xuml.study.com.common.flow;

/**
 * 流程处理器接口
 * 所有业务处理器都需要实现此接口
 *
 * @author xuml
 */
public interface FlowHandler {

    /**
     * 处理流程
     *
     * @param context 流程上下文
     * @return 处理结果，true表示继续执行下一个处理器，false表示中断流程
     * @throws FlowException 流程异常
     */
    boolean handle(FlowContext context) throws FlowException;

    /**
     * 获取处理器名称（用于配置）
     *
     * @return 处理器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取处理器优先级（数字越小优先级越高）
     *
     * @return 优先级，默认为100
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 是否启用此处理器
     *
     * @return 默认启用
     */
    default boolean isEnabled() {
        return true;
    }
}
