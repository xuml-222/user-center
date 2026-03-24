package xuml.study.com.service.flow.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xuml.study.com.common.flow.AbstractFlowHandler;
import xuml.study.com.common.flow.FlowContext;
import xuml.study.com.common.flow.FlowException;

/**
 * 回退示例处理器
 * 演示如何配置和使用回退功能
 *
 * @author xuml
 */
@Slf4j
@Component
public class RollbackExampleHandler extends AbstractFlowHandler {

    @Override
    public String getName() {
        return "RollbackExampleHandler";
    }

    @Override
    protected boolean doHandle(FlowContext context) throws FlowException {
        String nodeId = context.getCurrentNode();
        Integer executionCount = context.getNodeExecutionCount(nodeId);

        log.info("执行回退示例处理器: nodeId={}, 第{}次执行", nodeId, executionCount);

        // 模拟不同的节点行为
        if ("save-data".equals(nodeId)) {
            // 数据保存节点：第一次失败，第二次成功
            if (executionCount == 1) {
                log.warn("第一次保存失败，等待回退后重试");
                throw new FlowException("SAVE_FAILED", nodeId, "数据保存失败（模拟第一次失败）");
            } else if (executionCount == 2) {
                log.info("第二次保存成功");
                context.setData("saved", true);
                return true;
            }
        }

        if ("validate-data".equals(nodeId)) {
            // 数据校验节点
            String data = context.getData("data", String.class);
            if (data == null || data.isEmpty()) {
                log.warn("数据为空，需要回退到准备数据节点");
                context.requestRollback("prepare-data"); // 主动请求回退
                return false;
            }
            log.info("数据校验通过");
            return true;
        }

        if ("prepare-data".equals(nodeId)) {
            // 准备数据节点
            context.setData("data", "prepared-data-" + System.currentTimeMillis());
            log.info("数据准备完成");
            return true;
        }

        return true;
    }

    @Override
    public boolean isRequired() {
        return false;
    }
}
