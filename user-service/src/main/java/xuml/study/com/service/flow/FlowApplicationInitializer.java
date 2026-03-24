package xuml.study.com.service.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 流程应用初始化器
 * 在应用启动时自动初始化示例流程
 *
 * @author xuml
 */
@Slf4j
@Component
public class FlowApplicationInitializer implements ApplicationRunner {

    @Autowired
    private UserFlowService userFlowService;

    @Autowired
    private RollbackExampleService rollbackExampleService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("========================================");
        log.info("开始初始化流程控制服务");
        log.info("========================================");

        try {
            // 初始化用户注册流程
            userFlowService.initFlowConfigs();
            log.info("✓ 用户注册流程初始化完成");

            // 初始化回退示例流程
            rollbackExampleService.initRollbackExampleFlow();
            log.info("✓ 回退示例流程初始化完成");

            log.info("========================================");
            log.info("流程控制服务初始化完成");
            log.info("可用API:");
            log.info("  - POST /api/flow/register           # 用户注册流程");
            log.info("  - GET  /api/flow/config           # 获取流程配置");
            log.info("  - GET  /api/flow/health           # 流程健康检查");
            log.info("  - POST /api/flow/rollback/execute  # 执行回退示例");
            log.info("  - POST /api/flow/rollback/normal   # 执行正常流程");
            log.info("========================================");

        } catch (Exception e) {
            log.error("流程初始化失败", e);
        }
    }
}
