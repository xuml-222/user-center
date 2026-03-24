package xuml.study.com.service.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xuml.study.com.common.flow.*;

/**
 * 条件分支示例服务
 * 演示条件分支和跳转功能
 *
 * @author xuml
 */
@Slf4j
@Service
public class BranchExampleService {

    @Autowired
    private FlowEngine flowEngine;

    private static final String BRANCH_EXAMPLE_FLOW_ID = "branch-example-flow";

    /**
     * 初始化条件分支示例流程
     */
    public void initBranchExampleFlow() {
        FlowConfig config = FlowConfigBuilder
                .create(BRANCH_EXAMPLE_FLOW_ID, "条件分支示例")
                .version("1.0.0")
                .description("演示条件分支和跳转功能")

                // 节点1：数据准备
                .addNode("prepare-data", "准备数据", "RollbackExampleHandler", 1)

                // 节点2：条件检查（根据用户类型决定走向）
                .addNode("check-approval", "检查审批需求", "ConditionalBranchHandler", 2)

                // 节点3：审批（普通用户需要审批）
                .addNode("approval", "审批处理", "RollbackExampleHandler", 3)

                // 节点4：订单完成（所有用户都需要）
                .addNode("complete-order", "订单完成", "RollbackExampleHandler", 4)

                .build();

        flowEngine.registerFlowConfig(config);
        log.info("条件分支示例流程初始化完成");
    }

    /**
     * 初始化带分支规则的流程
     */
    public void initBranchRuleFlow() {
        FlowConfig config = new FlowConfig("branch-rule-flow", "分支规则示例");
        config.setFlowVersion("1.0.0");

        // 节点1：风险评估
        FlowNode riskAssessment = new FlowNode();
        riskAssessment.setNodeId("risk-assessment");
        riskAssessment.setNodeName("风险评估");
        riskAssessment.setHandlerName("RiskAssessmentHandler");
        riskAssessment.setOrder(1);
        riskAssessment.setEnabled(true);
        riskAssessment.setRequired(true);
        riskAssessment.setEnableBranch(true);

        // 添加分支规则
        BranchRule lowRiskRule = new BranchRule();
        lowRiskRule.setBranchName("低风险分支");
        lowRiskRule.setTargetNodeId("fast-process");
        lowRiskRule.setCondition("riskLevel=low");
        lowRiskRule.setConditionType(BranchRule.ConditionType.EQUALS);
        lowRiskRule.setPriority(1);
        riskAssessment.addBranchRule(lowRiskRule);

        BranchRule mediumRiskRule = new BranchRule();
        mediumRiskRule.setBranchName("中风险分支");
        mediumRiskRule.setTargetNodeId("normal-process");
        mediumRiskRule.setCondition("riskLevel=medium");
        mediumRiskRule.setConditionType(BranchRule.ConditionType.EQUALS);
        mediumRiskRule.setPriority(2);
        riskAssessment.addBranchRule(mediumRiskRule);

        BranchRule highRiskRule = new BranchRule();
        highRiskRule.setBranchName("高风险分支");
        highRiskRule.setTargetNodeId("strict-process");
        highRiskRule.setCondition("riskLevel=high");
        highRiskRule.setConditionType(BranchRule.ConditionType.EQUALS);
        highRiskRule.setPriority(3);
        riskAssessment.addBranchRule(highRiskRule);

        BranchRule defaultRule = new BranchRule();
        defaultRule.setBranchName("默认分支");
        defaultRule.setTargetNodeId("manual-review");
        defaultRule.setDefaultBranch(true);
        riskAssessment.addBranchRule(defaultRule);

        config.addNode(riskAssessment);

        // 节点2：快速处理（低风险）
        FlowNode fastProcess = new FlowNode();
        fastProcess.setNodeId("fast-process");
        fastProcess.setNodeName("快速处理");
        fastProcess.setHandlerName("RollbackExampleHandler");
        fastProcess.setOrder(2);
        fastProcess.setEnabled(true);
        fastProcess.setRequired(true);
        config.addNode(fastProcess);

        // 节点3：正常处理（中风险）
        FlowNode normalProcess = new FlowNode();
        normalProcess.setNodeId("normal-process");
        normalProcess.setNodeName("正常处理");
        normalProcess.setHandlerName("RollbackExampleHandler");
        normalProcess.setOrder(3);
        normalProcess.setEnabled(true);
        normalProcess.setRequired(true);
        config.addNode(normalProcess);

        // 节点4：严格处理（高风险）
        FlowNode strictProcess = new FlowNode();
        strictProcess.setNodeId("strict-process");
        strictProcess.setNodeName("严格处理");
        strictProcess.setHandlerName("RollbackExampleHandler");
        strictProcess.setOrder(4);
        strictProcess.setEnabled(true);
        strictProcess.setRequired(true);
        config.addNode(strictProcess);

        // 节点5：人工审核（默认）
        FlowNode manualReview = new FlowNode();
        manualReview.setNodeId("manual-review");
        manualReview.setNodeName("人工审核");
        manualReview.setHandlerName("RollbackExampleHandler");
        manualReview.setOrder(5);
        manualReview.setEnabled(true);
        manualReview.setRequired(true);
        config.addNode(manualReview);

        // 节点6：完成
        FlowNode complete = new FlowNode();
        complete.setNodeId("complete");
        complete.setNodeName("完成");
        complete.setHandlerName("RollbackExampleHandler");
        complete.setOrder(6);
        complete.setEnabled(true);
        complete.setRequired(true);
        config.addNode(complete);

        flowEngine.registerFlowConfig(config);
        log.info("分支规则示例流程初始化完成");
    }

    /**
     * 执行条件分支示例（VIP用户）
     */
    public FlowResult executeVipFlow() {
        log.info("执行VIP用户流程");

        initBranchExampleFlow();
        FlowContext context = new FlowContext(BRANCH_EXAMPLE_FLOW_ID, "VIP用户流程");
        context.setData("userType", "VIP");

        try {
            return flowEngine.execute(BRANCH_EXAMPLE_FLOW_ID, context);
        } catch (Exception e) {
            log.error("流程执行异常", e);
            FlowResult result = new FlowResult();
            result.setSuccess(false);
            result.fail("SYSTEM_ERROR", null, e.getMessage());
            return result;
        }
    }

    /**
     * 执行条件分支示例（普通用户）
     */
    public FlowResult executeNormalFlow() {
        log.info("执行普通用户流程");

        initBranchExampleFlow();
        FlowContext context = new FlowContext(BRANCH_EXAMPLE_FLOW_ID, "普通用户流程");
        context.setData("userType", "NORMAL");

        try {
            return flowEngine.execute(BRANCH_EXAMPLE_FLOW_ID, context);
        } catch (Exception e) {
            log.error("流程执行异常", e);
            FlowResult result = new FlowResult();
            result.setSuccess(false);
            result.fail("SYSTEM_ERROR", null, e.getMessage());
            return result;
        }
    }

    /**
     * 执行分支规则示例
     */
    public FlowResult executeBranchRuleFlow(String riskLevel) {
        log.info("执行分支规则示例: riskLevel={}", riskLevel);

        initBranchRuleFlow();
        FlowContext context = new FlowContext("branch-rule-flow", "分支规则流程");
        context.setData("riskLevel", riskLevel);

        try {
            return flowEngine.execute("branch-rule-flow", context);
        } catch (Exception e) {
            log.error("流程执行异常", e);
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
        return flowEngine.getFlowConfig(BRANCH_EXAMPLE_FLOW_ID);
    }
}
