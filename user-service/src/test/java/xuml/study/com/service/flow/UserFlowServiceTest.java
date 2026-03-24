package xuml.study.com.service.flow;

import xuml.study.com.common.flow.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;


import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程引擎测试
 *
 * @author xuml
 */
class FlowEngineTest {

    @InjectMocks
    private FlowEngine flowEngine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 注册测试流程配置
        FlowConfig config = FlowConfigBuilder
                .create("test-flow", "测试流程")
                .version("1.0.0")
                .addNode("validate", "数据校验", "TestValidationHandler", 1)
                .addNode("process", "数据处理", "TestProcessHandler", 2)
                .addNode("save", "数据保存", "TestSaveHandler", 3, true, true)
                .addNode("notify", "通知发送", "TestNotifyHandler", 4, true, false) // 非必需
                .build();

        flowEngine.registerFlowConfig(config);

        // 注册测试处理器
//        flowEngine.registerHandler(new TestValidationHandler());
//        flowEngine.registerHandler(new TestProcessHandler());
//        flowEngine.registerHandler(new TestSaveHandler());
//        flowEngine.registerHandler(new TestNotifyHandler());
    }

    @Test
    void testExecuteFlowSuccess() throws FlowException {
        FlowContext context = new FlowContext("test-flow", "测试流程");
        context.setData("name", "test");
        context.setData("age", 25);

        FlowResult result = flowEngine.execute("test-flow", context);

        assertTrue(result.isSuccess());
        assertEquals("test-flow", result.getFlowId());
        assertEquals(4, result.getExecutedNodes().size());
    }

    @Test
    void testExecuteFlowWithValidationFailure() throws FlowException {
        FlowContext context = new FlowContext("test-flow", "测试流程");
        context.setData("name", ""); // 空名字会导致校验失败

        FlowResult result = flowEngine.execute("test-flow", context);

        assertFalse(result.isSuccess());
        assertEquals("validate", result.getErrorNode());
    }

    @Test
    void testExecuteFlowWithOptionalNodeFailure() throws FlowException {
        FlowContext context = new FlowContext("test-flow", "测试流程");
        context.setData("name", "test");
        context.setData("age", 25);
        context.setData("failNotify", true); // 触发通知节点失败

        FlowResult result = flowEngine.execute("test-flow", context);

        // 由于通知节点是非必需的，流程应该成功
        assertTrue(result.isSuccess());

        // 检查通知节点是否执行失败
        result.getExecutedNodes().forEach(node -> {
            if ("notify".equals(node.getNodeId())) {
                assertFalse(node.isSuccess());
            }
        });
    }

    @Test
    void testContextDataTransfer() throws FlowException {
        FlowContext context = new FlowContext("test-flow", "测试流程");
        context.setData("name", "test");
        context.setData("age", 25);

        flowEngine.execute("test-flow", context);

        // 验证处理器之间的数据传递
        assertNotNull(context.getData("validated"));
        assertNotNull(context.getData("processed"));
        assertNotNull(context.getData("saved"));
        assertNotNull(context.getData("userId"));
    }

    // ==================== 测试处理器 ====================

    private static class TestValidationHandler extends AbstractFlowHandler {
        @Override
        public String getName() {
            return "TestValidationHandler";
        }

        @Override
        protected boolean validate(FlowContext context) {
            String name = context.getData("name", String.class);
            return name != null && !name.isEmpty();
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        protected boolean doHandle(FlowContext context) throws FlowException {
            context.setExtData("validated", true);
            return true;
        }
    }

    private static class TestProcessHandler extends AbstractFlowHandler {
        @Override
        public String getName() {
            return "TestProcessHandler";
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        protected boolean doHandle(FlowContext context) throws FlowException {
            String name = context.getData("name", String.class);
            Integer age = context.getData("age", Integer.class);

            // 模拟数据处理
            context.setExtData("processed", true);
            context.setData("processedName", name.toUpperCase());
            context.setData("doubledAge", age * 2);

            return true;
        }
    }

    private static class TestSaveHandler extends AbstractFlowHandler {
        @Override
        public String getName() {
            return "TestSaveHandler";
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        protected boolean doHandle(FlowContext context) throws FlowException {
            // 模拟保存数据
            Long userId = System.currentTimeMillis();
            context.setData("userId", userId);
            context.setExtData("saved", true);

            return true;
        }
    }

    private static class TestNotifyHandler extends AbstractFlowHandler {
        @Override
        public String getName() {
            return "TestNotifyHandler";
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        protected boolean doHandle(FlowContext context) throws FlowException {
            Boolean failNotify = context.getData("failNotify", Boolean.class);
            if (Boolean.TRUE.equals(failNotify)) {
                throw new FlowException("NOTIFY_FAILED", context.getCurrentNode(), "通知发送失败");
            }

            context.setExtData("notified", true);
            return true;
        }
    }
}

// 为FlowEngine添加registerHandler方法（需要在实际代码中添加）
class FlowEngineExtension {
    public void registerHandler(FlowHandler handler) {
        // 实现在FlowEngine中添加此方法
    }
}
