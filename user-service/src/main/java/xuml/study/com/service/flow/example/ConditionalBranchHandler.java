package xuml.study.com.service.flow.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xuml.study.com.common.flow.ExtendedFlowHandler;
import xuml.study.com.common.flow.FlowContext;
import xuml.study.com.common.flow.FlowException;
import xuml.study.com.common.flow.FlowHandlerResult;

/**
 * 条件分支处理器
 * 演示如何根据业务数据返回不同的跳转指令
 *
 * @author xuml
 */
@Slf4j
@Component
public class ConditionalBranchHandler implements ExtendedFlowHandler {

    @Override
    public String getName() {
        return "ConditionalBranchHandler";
    }

    @Override
    public FlowHandlerResult handleWithResult(FlowContext context) throws FlowException {
        String userType = context.getData("userType", String.class);
        String nodeId = context.getCurrentNode();

        log.info("条件分支处理: nodeId={}, userType={}", nodeId, userType);

        // 根据用户类型决定跳转到哪个节点
        if ("check-approval".equals(nodeId)) {
            if ("VIP".equals(userType)) {
                log.info("VIP用户，跳过审批流程");
                return FlowHandlerResult.success("complete-order")
                        .message("VIP用户，跳过审批");
            } else {
                log.info("普通用户，进入审批流程");
                return FlowHandlerResult.success("approval");
            }
        }

        if ("approval".equals(nodeId)) {
            String approvalStatus = context.getData("approvalStatus", String.class);
            if ("approved".equals(approvalStatus)) {
                log.info("审批通过，进入订单完成流程");
                return FlowHandlerResult.success("complete-order")
                        .message("审批通过");
            } else {
                log.info("审批拒绝，结束流程");
                return FlowHandlerResult.end()
                        .message("审批拒绝");
            }
        }

        return FlowHandlerResult.success();
    }

    public boolean isRequired() {
        return false;
    }
}
