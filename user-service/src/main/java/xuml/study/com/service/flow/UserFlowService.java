package xuml.study.com.service.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xuml.study.com.common.flow.FlowConfig;
import xuml.study.com.common.flow.FlowConfigBuilder;
import xuml.study.com.common.flow.FlowContext;
import xuml.study.com.common.flow.FlowEngine;
import xuml.study.com.common.flow.FlowException;
import xuml.study.com.common.flow.FlowResult;

import javax.annotation.PostConstruct;

/**
 * 用户流程服务
 * 演示如何使用流程引擎
 *
 * @author xuml
 */
@Slf4j
@Service
public class UserFlowService {

    @Autowired
    private FlowEngine flowEngine;

    private static final String USER_REGISTER_FLOW_ID = "user-register-flow";

    /**
     * 初始化流程配置
     * 实际项目中可以从数据库或配置文件中加载
     */
    @PostConstruct
    public void initFlowConfigs() {
        FlowConfig userRegisterFlow = FlowConfigBuilder
                .create(USER_REGISTER_FLOW_ID, "用户注册流程")
                .version("1.0.0")
                .description("用户注册流程，包含数据校验、存在性检查、数据保存、通知发送等环节")
                .addNode("validate", "数据校验", "UserValidationHandler", 1, true, true)
                .addNode("check-exist", "存在性检查", "UserExistenceCheckHandler", 2, true, true)
                .addNode("save-data", "数据保存", "UserDataSaveHandler", 3, true, true)
                .addNode("notification", "通知发送", "UserNotificationHandler", 4, true, false) // 非必需
                .build();

        flowEngine.registerFlowConfig(userRegisterFlow);
        log.info("用户注册流程配置初始化完成");
    }

    /**
     * 执行用户注册流程
     *
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号（可选）
     * @return 执行结果
     */
    public FlowResult registerUser(String username, String email, String phone) {
        log.info("开始执行用户注册流程: username={}, email={}", username, email);

        // 创建流程上下文
        FlowContext context = new FlowContext(USER_REGISTER_FLOW_ID, "用户注册");
        context.setData("username", username);
        context.setData("email", email);
        if (phone != null && !phone.isEmpty()) {
            context.setData("phone", phone);
        }

        // 执行流程
        FlowResult result;
        try {
            result = flowEngine.execute(USER_REGISTER_FLOW_ID, context);

            if (result.isSuccess()) {
                Long userId = context.getData("userId", Long.class);
                log.info("用户注册流程执行成功: userId={}", userId);
            } else {
                log.error("用户注册流程执行失败: errorCode={}, errorMessage={}, errorNode={}",
                        result.getErrorCode(), result.getErrorMessage(), result.getErrorNode());
            }

        } catch (FlowException e) {
            log.error("用户注册流程执行异常", e);
            result = new FlowResult();
            result.setSuccess(false);
            result.setFlowId(USER_REGISTER_FLOW_ID);
            result.fail(e.getCode(), e.getNode(), e.getMessage());
        }

        return result;
    }

    /**
     * 获取流程配置
     */
    public FlowConfig getFlowConfig() {
        return flowEngine.getFlowConfig(USER_REGISTER_FLOW_ID);
    }

    /**
     * 动态更新流程配置
     * 实际项目中可以结合管理后台实现热更新
     */
    public void updateFlowConfig(FlowConfig newConfig) {
        // 清除旧配置
        flowEngine.clearFlowConfig(USER_REGISTER_FLOW_ID);
        // 注册新配置
        flowEngine.registerFlowConfig(newConfig);
        log.info("流程配置已更新: flowId={}", USER_REGISTER_FLOW_ID);
    }
}
