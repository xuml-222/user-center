package xuml.study.com.service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xuml.study.com.common.dto.Result;
import xuml.study.com.common.flow.FlowConfig;
import xuml.study.com.common.flow.FlowResult;
import xuml.study.com.service.flow.RollbackExampleService;

import java.util.HashMap;
import java.util.Map;

/**
 * 回退示例控制器
 *
 * @author xuml
 */
@Slf4j
@RestController
@RequestMapping("/api/flow/rollback")
public class RollbackExampleController {

    @Autowired
    private RollbackExampleService rollbackExampleService;

    /**
     * 初始化回退示例流程
     */
    @PostMapping("/init")
    public Result<String> initFlow() {
        try {
            rollbackExampleService.initRollbackExampleFlow();
            return Result.successMessage("回退示例流程初始化成功");
        } catch (Exception e) {
            log.error("初始化回退示例流程失败", e);
            return Result.error("初始化失败: " + e.getMessage());
        }
    }

    /**
     * 执行回退示例流程
     * 此流程会触发多次回退和重试
     */
    @PostMapping("/execute")
    public Result<Map<String, Object>> executeRollbackExample() {
        log.info("执行回退示例流程");

        FlowResult result = rollbackExampleService.executeRollbackExample();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", result.isSuccess());
        responseData.put("totalRollbackCount", result.getTotalRollbackCount());
        responseData.put("executedNodes", result.getExecutedNodes());
        responseData.put("rollbackHistory", result.getRollbackHistory());

        if (result.isSuccess()) {
            return Result.success(responseData).message("流程执行成功");
        } else {
            responseData.put("errorCode", result.getErrorCode());
            responseData.put("errorMessage", result.getErrorMessage());
            responseData.put("errorNode", result.getErrorNode());
            return Result.error(result.getErrorCode(), result.getErrorMessage());
        }
    }

    /**
     * 执行正常流程（不触发回退）
     */
    @PostMapping("/normal")
    public Result<Map<String, Object>> executeNormalFlow() {
        log.info("执行正常流程");

        FlowResult result = rollbackExampleService.executeNormalFlow();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", result.isSuccess());
        responseData.put("totalRollbackCount", result.getTotalRollbackCount());
        responseData.put("executedNodes", result.getExecutedNodes());

        if (result.isSuccess()) {
            return Result.success(responseData).message("流程执行成功");
        } else {
            responseData.put("errorCode", result.getErrorCode());
            responseData.put("errorMessage", result.getErrorMessage());
            responseData.put("errorNode", result.getErrorNode());
            return Result.error(result.getErrorCode(), result.getErrorMessage());
        }
    }

    /**
     * 获取流程配置
     */
    @GetMapping("/config")
    public Result<FlowConfig> getConfig() {
        FlowConfig config = rollbackExampleService.getFlowConfig();
        return Result.success(config);
    }
}
