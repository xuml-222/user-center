package xuml.study.com.service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xuml.study.com.common.dto.Result;
import xuml.study.com.common.flow.FlowConfig;
import xuml.study.com.common.flow.FlowResult;
import xuml.study.com.service.flow.UserFlowService;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户流程控制器
 * 提供流程执行的API接口
 *
 * @author xuml
 */
@Slf4j
@RestController
@RequestMapping("/api/flow")
public class UserFlowController {

    @Autowired
    private UserFlowService userFlowService;

    /**
     * 用户注册流程
     */
    @PostMapping("/register")
    public Result<Map<Object, Object>> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String phone) {

        log.info("收到用户注册请求: username={}, email={}, phone={}", username, email, phone);

        FlowResult result = userFlowService.registerUser(username, email, phone);

        Map<Object, Object> responseData = new HashMap<>();
        responseData.put("success", result.isSuccess());

        if (result.isSuccess()) {
            return Result.success(responseData).message("用户注册成功");
        } else {
            responseData.put("errorCode", result.getErrorCode());
            responseData.put("errorMessage", result.getErrorMessage());
            responseData.put("errorNode", result.getErrorNode());
            responseData.put("executedNodes", result.getExecutedNodes());

            return Result.error(result.getErrorCode(), result.getErrorMessage());
        }
    }

    /**
     * 获取流程配置信息
     */
    @GetMapping("/config")
    public Result<FlowConfig> getConfig() {
        FlowConfig config = userFlowService.getFlowConfig();
        return Result.success(config);
    }

    /**
     * 更新流程配置
     */
    @PostMapping("/config")
    public Result<String> updateConfig(@RequestBody FlowConfig config) {
        try {
            userFlowService.updateFlowConfig(config);
            return Result.successMessage("流程配置更新成功");
        } catch (Exception e) {
            log.error("更新流程配置失败", e);
            return Result.error("更新流程配置失败: " + e.getMessage());
        }
    }

    /**
     * 流程健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        FlowConfig config = userFlowService.getFlowConfig();
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("flowId", config.getFlowId());
        healthData.put("flowName", config.getFlowName());
        healthData.put("flowVersion", config.getFlowVersion());
        healthData.put("nodeCount", config.getNodes().size());
        healthData.put("status", "running");

        return Result.success(healthData);
    }
}
