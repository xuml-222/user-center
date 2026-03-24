package xuml.study.com.service.flow.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xuml.study.com.common.flow.AbstractFlowHandler;
import xuml.study.com.common.flow.FlowContext;
import xuml.study.com.common.flow.FlowException;

/**
 * 用户通知处理器
 * （设置为非必需，即使失败也不影响主流程）
 *
 * @author xuml
 */
@Slf4j
@Component
public class UserNotificationHandler extends AbstractFlowHandler {

    @Override
    public String getName() {
        return "UserNotificationHandler";
    }

    @Override
    public boolean isRequired() {
        // 这个处理器是非必需的
        return false;
    }

    @Override
    protected boolean doHandle(FlowContext context) throws FlowException {
        String username = context.getData("username", String.class);
        String email = context.getData("email", String.class);
        Long userId = context.getData("userId", Long.class);

        log.info("发送用户通知: userId={}, username={}, email={}", userId, username, email);

        try {
            // 模拟发送欢迎邮件
            sendWelcomeEmail(username, email);

            // 模拟发送短信
            if (context.getData("phone") != null) {
                sendWelcomeSms(username, context.getData("phone", String.class));
            }

            context.setExtData("notificationSent", true);
            log.info("用户通知发送成功");
            return true;

        } catch (Exception e) {
            log.error("发送用户通知失败", e);
            // 由于是非必需处理器，这里记录错误但不抛出异常
            // 可以选择抛出FlowException，引擎会根据isRequired()判断是否继续
            context.setExtData("notificationError", e.getMessage());
            return true;
        }
    }

    /**
     * 模拟发送欢迎邮件
     */
    private void sendWelcomeEmail(String username, String email) {
        // TODO: 实际项目中这里应该调用邮件服务
        log.info("发送欢迎邮件到: {}", email);
    }

    /**
     * 模拟发送欢迎短信
     */
    private void sendWelcomeSms(String username, String phone) {
        // TODO: 实际项目中这里应该调用短信服务
        log.info("发送欢迎短信到: {}", phone);
    }
}
