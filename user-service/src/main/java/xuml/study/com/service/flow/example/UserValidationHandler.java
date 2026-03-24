package xuml.study.com.service.flow.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xuml.study.com.common.flow.AbstractFlowHandler;
import xuml.study.com.common.flow.FlowContext;
import xuml.study.com.common.flow.FlowException;

/**
 * 用户数据校验处理器
 *
 * @author xuml
 */
@Slf4j
@Component
public class UserValidationHandler extends AbstractFlowHandler {

    @Override
    public String getName() {
        return "UserValidationHandler";
    }

    @Override
    protected boolean validate(FlowContext context) {
        // 检查必要的数据是否存在
        String username = context.getData("username", String.class);
        if (username == null || username.isEmpty()) {
            log.warn("用户名为空");
            return false;
        }

        String email = context.getData("email", String.class);
        if (email == null || email.isEmpty()) {
            log.warn("邮箱为空");
            return false;
        }

        return true;
    }



    @Override
    protected boolean doHandle(FlowContext context) throws FlowException {
        String username = context.getData("username", String.class);
        String email = context.getData("email", String.class);

        log.info("校验用户数据: username={}, email={}", username, email);

        // 校验用户名长度
        if (username.length() < 3 || username.length() > 20) {
            throw new FlowException("USERNAME_INVALID", context.getCurrentNode(), "用户名长度必须在3-20个字符之间");
        }

        // 校验邮箱格式（简单校验）
        if (!email.matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new FlowException("EMAIL_INVALID", context.getCurrentNode(), "邮箱格式不正确");
        }

        // 校验通过，保存校验结果到上下文
        context.setExtData("validationPassed", true);

        log.info("用户数据校验通过");
        return true;
    }
}
