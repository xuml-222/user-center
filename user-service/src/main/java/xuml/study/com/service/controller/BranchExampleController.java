package xuml.study.com.service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xuml.study.com.common.dto.Result;
import xuml.study.com.common.flow.FlowConfig;
import xuml.study.com.common.flow.FlowResult;
import xuml.study.com.service.flow.BranchExampleService;

import java.util.HashMap;
import java.util.Map;

/**
 * 条件分支示例控制器
 *
 * @author xuml
 */
@Slf4j
@RestController
@RequestMapping("/api/flow/branch")
public class BranchExampleController {

    @Autowired
    private BranchExampleService branchExampleService;

    /**
     * 执行VIP用户流程
     */
    @PostMapping("/vip")
    public Result<Map<String, Object>> executeVipFlow() {
        log.info("执行VIP用户流程");

        FlowResult result = branchExampleService.executeVipFlow();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", result.isSuccess());
        responseData.put("userType", "VIP");

        if (result.isSuccess()) {
            return Result.success(responseData).message("VIP用户流程执行成功");
        } else {
            responseData.put("errorCode", result.getErrorCode());
            responseData.put("errorMessage", result.getErrorMessage());
            return Result.error(result.getErrorCode(), result.getErrorMessage());
        }
    }

    /**
     * 执行普通用户流程
     */
    @PostMapping("/normal")
    public Result<Map<String, Object>> executeNormalFlow() {
        log.info("执行普通用户流程");

        FlowResult result = branchExampleService.executeNormalFlow();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", result.isSuccess());
        responseData.put("userType", "NORMAL");

        if (result.isSuccess()) {
            return Result.success(responseData).message("普通用户流程执行成功");
        } else {
            responseData.put("errorCode", result.getErrorCode());
            responseData.put("errorMessage", result.getErrorMessage());
            return Result.error(result.getErrorCode(), result.getErrorMessage());
        }
    }

    /**
     * 执行分支规则示例
     */
    @PostMapping("/rule")
    public Result<Map<String, Object>> executeBranchRuleFlow(
            @RequestParam(defaultValue = "low") String riskLevel) {
        log.info("执行分支规则示例: riskLevel={}", riskLevel);

        FlowResult result = branchExampleService.executeBranchRuleFlow(riskLevel);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", result.isSuccess());
        responseData.put("riskLevel", riskLevel);
        responseData.put("executedNodes", result.getExecutedNodes());

        // 获取分支信息
        Object branchName = null;
        if (result.getExecutedNodes() != null && !result.getExecutedNodes().isEmpty()) {
            // 这里可以从上下文中获取分支名称
        }
        responseData.put("branchName", branchName);

        if (result.isSuccess()) {
            return Result.success(responseData).message("分支规则流程执行成功");
        } else {
            responseData.put("errorCode", result.getErrorCode());
            responseData.put("errorMessage", result.getErrorMessage());
            return Result.error(result.getErrorCode(), result.getErrorMessage());
        }
    }

    /**
     * 获取流程配置
     */
    @GetMapping("/config")
    public Result<FlowConfig> getConfig() {
        FlowConfig config = branchExampleService.getFlowConfig();
        return Result.success(config);
    }
}
