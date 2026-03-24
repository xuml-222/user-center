package xuml.study.com.service.flow.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xuml.study.com.common.flow.AbstractFlowHandler;
import xuml.study.com.common.flow.FlowContext;
import xuml.study.com.common.flow.FlowException;

/**
 * 用户数据保存处理器
 *
 * @author xuml
 */
@Slf4j
@Component
public class UserDataSaveHandler extends AbstractFlowHandler {

    @Override
    public String getName() {
        return "UserDataSaveHandler";
    }

    @Override
    protected boolean doHandle(FlowContext context) throws FlowException {
        String username = context.getData("username", String.class);
        String email = context.getData("email", String.class);
        String phone = context.getData("phone", String.class);

        log.info("保存用户数据: username={}, email={}, phone={}", username, email, phone);

        try {
            // 模拟保存到数据库
            Long userId = saveToDatabase(username, email, phone);

            // 将生成的用户ID保存到上下文
            context.setData("userId", userId);
            context.setExtData("saveSuccess", true);

            log.info("用户数据保存成功, userId={}", userId);
            return true;

        } catch (Exception e) {
            log.error("保存用户数据失败", e);
            throw new FlowException("SAVE_FAILED", context.getCurrentNode(), "保存用户数据失败: " + e.getMessage());
        }
    }

    /**
     * 模拟保存到数据库
     */
    private Long saveToDatabase(String username, String email, String phone) {
        // TODO: 实际项目中这里应该调用DAO层保存数据
        // 模拟生成用户ID
        return System.currentTimeMillis();
    }
}
