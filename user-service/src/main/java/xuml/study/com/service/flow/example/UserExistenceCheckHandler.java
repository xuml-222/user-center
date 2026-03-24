package xuml.study.com.service.flow.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xuml.study.com.common.flow.AbstractFlowHandler;
import xuml.study.com.common.flow.FlowContext;
import xuml.study.com.common.flow.FlowException;

/**
 * 用户存在性检查处理器
 *
 * @author xuml
 */
@Slf4j
@Component
public class UserExistenceCheckHandler extends AbstractFlowHandler {

    @Override
    public String getName() {
        return "UserExistenceCheckHandler";
    }

    @Override
    protected boolean doHandle(FlowContext context) throws FlowException {
        String username = context.getData("username", String.class);
        String email = context.getData("email", String.class);

        log.info("检查用户是否存在: username={}, email={}", username, email);

        // 模拟数据库查询
        boolean userExists = checkUserFromDatabase(username, email);

        if (userExists) {
            throw new FlowException("USER_ALREADY_EXISTS", context.getCurrentNode(), "用户已存在");
        }

        log.info("用户不存在，可以注册");
        context.setExtData("userExists", false);
        return true;
    }

    /**
     * 模拟数据库查询
     */
    private boolean checkUserFromDatabase(String username, String email) {
        // TODO: 实际项目中这里应该查询数据库
        // 模拟：如果用户名是"admin"则已存在
        return "admin".equals(username);
    }
}
