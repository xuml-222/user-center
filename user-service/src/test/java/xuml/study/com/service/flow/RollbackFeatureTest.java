package xuml.study.com.service.flow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xuml.study.com.common.flow.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 回退功能测试
 *
 * @author xuml
 */
class RollbackFeatureTest {

    private FlowEngine flowEngine;

    @BeforeEach
    void setUp() {
        flowEngine = new FlowEngine();

        // 注册测试处理器
        // 注意：实际使用时应该从Spring容器注入，这里简化处理
    }

    @Test
    void testRetryStrategy() throws FlowException {
        // 测试RETRY策略：节点失败时重试当前节点

        FlowConfig config = FlowConfigBuilder
                .create("retry-test", "重试测试")
                .version("1.0.0")
                .addNodeWithRollback(
                        "node1", "重试节点", "RetryTestHandler", 1,
                        true, true, null,
                        FlowNode.RollbackStrategy.RETRY, null, 3 // 重试3次
                )
                .build();

        flowEngine.registerFlowConfig(config);
        flowEngine.registerHandler(new RetryTestHandler());

        FlowContext context = new FlowContext("retry-test", "重试测试");
        FlowResult result = flowEngine.execute("retry-test", context);

        // 应该在第3次重试后成功
        assertTrue(result.isSuccess());
        assertEquals(3, result.getTotalRollbackCount());
    }

    @Test
    void testPreviousStrategy() throws FlowException {
        // 测试PREVIOUS策略：节点失败时回退到上一个节点

        FlowConfig config = FlowConfigBuilder
                .create("previous-test", "回退上一个测试")
                .version("1.0.0")
                .addNode("node1", "节点1", "SimpleHandler", 1)
                .addNodeWithRollback(
                        "node2", "节点2", "FailOnceHandler", 2,
                        true, true, null,
                        FlowNode.RollbackStrategy.PREVIOUS, null, 1
                )
                .addNode("node3", "节点3", "SimpleHandler", 3)
                .build();

        flowEngine.registerFlowConfig(config);
        flowEngine.registerHandler(new SimpleHandler("node1"));
        flowEngine.registerHandler(new FailOnceHandler());
        flowEngine.registerHandler(new SimpleHandler("node3"));

        FlowContext context = new FlowContext("previous-test", "回退上一个测试");
        FlowResult result = flowEngine.execute("previous-test", context);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getTotalRollbackCount());

        // 验证回退历史
        FlowResult.RollbackRecord record = result.getRollbackHistory().get(0);
        assertEquals("node2", record.getFromNodeId());
        assertEquals("node1", record.getToNodeId());
    }

    @Test
    void testSpecificStrategy() throws FlowException {
        // 测试SPECIFIC策略：节点失败时回退到指定节点

        FlowConfig config = FlowConfigBuilder
                .create("specific-test", "指定回退测试")
                .version("1.0.0")
                .addNode("node1", "节点1", "SimpleHandler", 1)
                .addNode("node2", "节点2", "SimpleHandler", 2)
                .addNode("node3", "节点3", "SimpleHandler", 3)
                .addNodeWithRollback(
                        "node4", "节点4", "FailOnceHandler", 4,
                        true, true, null,
                        FlowNode.RollbackStrategy.SPECIFIC, "node1", 1 // 回退到node1
                )
                .build();

        flowEngine.registerFlowConfig(config);
        flowEngine.registerHandler(new SimpleHandler("node1"));
        flowEngine.registerHandler(new SimpleHandler("node2"));
        flowEngine.registerHandler(new SimpleHandler("node3"));
        flowEngine.registerHandler(new FailOnceHandler());

        FlowContext context = new FlowContext("specific-test", "指定回退测试");
        FlowResult result = flowEngine.execute("specific-test", context);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getTotalRollbackCount());

        // 验证回退到指定节点
        FlowResult.RollbackRecord record = result.getRollbackHistory().get(0);
        assertEquals("node4", record.getFromNodeId());
        assertEquals("node1", record.getToNodeId());
    }

    @Test
    void testMaxRollbackTimes() throws FlowException {
        // 测试最大回退次数限制

        FlowConfig config = FlowConfigBuilder
                .create("max-times-test", "最大次数测试")
                .version("1.0.0")
                .addNodeWithRollback(
                        "node1", "总是失败节点", "AlwaysFailHandler", 1,
                        true, true, null,
                        FlowNode.RollbackStrategy.RETRY, null, 2 // 最多重试2次
                )
                .build();

        flowEngine.registerFlowConfig(config);
        flowEngine.registerHandler(new AlwaysFailHandler());

        FlowContext context = new FlowContext("max-times-test", "最大次数测试");
        FlowResult result = flowEngine.execute("max-times-test", context);

        // 应该失败（达到最大重试次数）
        assertFalse(result.isSuccess());
        assertEquals(2, result.getTotalRollbackCount()); // 重试了2次

        // 检查节点执行了3次（初始1次 + 重试2次）
        assertEquals(3, context.getNodeExecutionCount("node1"));
    }

    // ==================== 测试处理器 ====================

    private static class SimpleHandler implements FlowHandler {
        private final String nodeName;

        public SimpleHandler(String nodeName) {
            this.nodeName = nodeName;
        }

        @Override
        public String getName() {
            return nodeName;
        }

        @Override
        public boolean handle(FlowContext context) throws FlowException {
            return true;
        }
    }

    private static class RetryTestHandler implements FlowHandler {
        private int count = 0;

        @Override
        public String getName() {
            return "RetryTestHandler";
        }

        @Override
        public boolean handle(FlowContext context) throws FlowException {
            count++;
            if (count < 3) {
                throw new FlowException("RETRY", context.getCurrentNode(), "需要重试（第" + count + "次）");
            }
            return true; // 第3次成功
        }
    }

    private static class FailOnceHandler implements FlowHandler {
        private boolean failed = false;

        @Override
        public String getName() {
            return "FailOnceHandler";
        }

        @Override
        public boolean handle(FlowContext context) throws FlowException {
            if (!failed) {
                failed = true;
                throw new FlowException("FAIL_ONCE", context.getCurrentNode(), "第一次失败");
            }
            return true;
        }
    }

    private static class AlwaysFailHandler implements FlowHandler {
        @Override
        public String getName() {
            return "AlwaysFailHandler";
        }

        @Override
        public boolean handle(FlowContext context) throws FlowException {
            throw new FlowException("ALWAYS_FAIL", context.getCurrentNode(), "总是失败");
        }
    }

    // 为FlowEngine添加registerHandler方法（需要在实际代码中添加）
    public void registerHandler(FlowHandler handler) {
        // 实现在FlowEngine中添加此方法
    }
}
