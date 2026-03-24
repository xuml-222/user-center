package xuml.study.com.common.flow;

import lombok.extern.slf4j.Slf4j;

/**
 * 流程处理器抽象基类
 * 提供通用的处理逻辑，方便快速创建自定义处理器
 *
 * @author xuml
 */
@Slf4j
public abstract class AbstractFlowHandler implements FlowHandler {

    @Override
    public boolean handle(FlowContext context) throws FlowException {
        long startTime = System.currentTimeMillis();

        try {
            log.info("执行处理器: handler={}, flowId={}", getName(), context.getFlowId());

            // 前置校验
            if (!validate(context)) {
                log.warn("处理器校验失败: handler={}", getName());
                throw new FlowException("VALIDATION_FAILED", context.getCurrentNode(), "处理器校验失败");
            }

            // 执行具体业务逻辑
            boolean continueFlow = doHandle(context);

            long executeTime = System.currentTimeMillis() - startTime;
            log.info("处理器执行完成: handler={}, 耗时={}ms", getName(), executeTime);

            return continueFlow;

        } catch (FlowException e) {
            long executeTime = System.currentTimeMillis() - startTime;
            log.error("处理器执行异常: handler={}, 耗时={}ms, error={}", getName(), executeTime, e.getMessage());
            throw e;
        } catch (Exception e) {
            long executeTime = System.currentTimeMillis() - startTime;
            log.error("处理器执行系统异常: handler={}, 耗时={}ms", getName(), executeTime, e);
            throw new FlowException("SYSTEM_EXCEPTION", context.getCurrentNode(), "系统异常: " + e.getMessage(), e);
        }
    }

    /**
     * 前置校验
     * 子类可重写此方法实现自定义校验逻辑
     *
     * @param context 流程上下文
     * @return 校验是否通过
     */
    protected boolean validate(FlowContext context) {
        return true;
    }

    public boolean isRequired() {
        return true;
    }

    /**
     * 具体的业务处理逻辑
     * 子类必须实现此方法
     *
     * @param context 流程上下文
     * @return 是否继续执行下一个处理器
     * @throws FlowException 流程异常
     */
    protected abstract boolean doHandle(FlowContext context) throws FlowException;
}
