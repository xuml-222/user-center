package xuml.study.com.service.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xuml.study.com.common.flow.*;

/**
 * 回退示例服务
 * 演示流程回退功能的使用
 *
 * @author xuml
 */
@Slf4j
@Service
public class RollbackExampleService {

    @Autowired
    private FlowEngine flowEngine;

    private static final String ROLLBACK_EXAMPLE_FLOW_ID = "rollback-example-flow";

    /**
     * 初始化回退示例流程配置
     */
    public void initRollbackExampleFlow() {
        FlowConfig config = FlowConfigBuilder
                .create(ROLLBACK_EXAMPLE_FLOW_ID, "回退示例流程")
                .version("1.0.0")
                .description("演示流程回退和重试功能")

                // 节点1：准备数据
                .addNode("prepare-data", "准备数据", "RollbackExampleHandler", 1)

                // 节点2：数据校验（如果失败回退到节点1）
                .addNodeWithRollback(
                        "validate-data", "数据校验", "RollbackExampleHandler", 2,
                        true, true, null,
                        FlowNode.RollbackStrategy.SPECIFIC, "prepare-data", 3 // 回退到prepare-data，最多3次
                )

                // 节点3：数据保存（失败时重试）
                .addNodeWithRollback(
                        "save-data", "数据保存", "RollbackExampleHandler", 3,
                        true, true, null,
                        FlowNode.RollbackStrategy.RETRY, null, 2 // 重试当前节点，最多2次
                )

                // 节点4：通知发送（失败时回退到上一个节点）
                .addNodeWithRollback(
                        "notification", "通知发送", "RollbackExampleHandler", 4,
                        true, false, null,
                        FlowNode.RollbackStrategy.PREVIOUS, null, 1 // 回退到上一个节点，最多1次
                )

                .build();

        flowEngine.registerFlowConfig(config);
        log.info("回退示例流程配置初始化完成");
    }

    /**
     * 执行回退示例流程
     */
    public FlowResult executeRollbackExample() {
        log.info("开始执行回退示例流程");

        FlowContext context = new FlowContext(ROLLBACK_EXAMPLE_FLOW_ID, "回退示例");
        // 初始不提供数据，触发校验失败和回退

        try {
            // 先初始化流程
            initRollbackExampleFlow();
            FlowResult result = flowEngine.execute(ROLLBACK_EXAMPLE_FLOW_ID, context);

            log.info("回退示例流程执行完成: success={}", result.isSuccess());
            log.info("总回退次数: {}", result.getTotalRollbackCount());

            if (result.getRollbackHistory() != null && !result.getRollbackHistory().isEmpty()) {
                log.info("回退历史:");
                result.getRollbackHistory().forEach(record ->
                        log.info("  从 {} 回退到 {}，原因: {}",
                                record.getFromNodeId(), record.getToNodeId(), record.getReason())
                );
            }

            return result;

        } catch (Exception e) {
            log.error("回退示例流程执行异常", e);
            FlowResult result = new FlowResult();
            result.setSuccess(false);
            result.fail("SYSTEM_ERROR", null, e.getMessage());
            return result;
        }
    }

    /**
     * 执行正常流程（提供数据，不触发回退）
     */
    public FlowResult executeNormalFlow() {
        log.info("开始执行正常流程（提供数据）");

        FlowContext context = new FlowContext(ROLLBACK_EXAMPLE_FLOW_ID, "正常流程");
        context.setData("data", "test-data"); // 提供数据，避免校验失败

        try {
            // 先初始化流程
            initRollbackExampleFlow();
            FlowResult result = flowEngine.execute(ROLLBACK_EXAMPLE_FLOW_ID, context);
            log.info("正常流程执行完成: success={}", result.isSuccess());
            return result;

        } catch (Exception e) {
            log.error("正常流程执行异常", e);
            FlowResult result = new FlowResult();
            result.setSuccess(false);
            result.fail("SYSTEM_ERROR", null, e.getMessage());
            return result;
        }
    }

    /**
     * 获取流程配置
     */
    public FlowConfig getFlowConfig() {
        return flowEngine.getFlowConfig(ROLLBACK_EXAMPLE_FLOW_ID);
    }
}
