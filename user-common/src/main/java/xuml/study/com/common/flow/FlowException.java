package xuml.study.com.common.flow;

import lombok.Getter;

/**
 * 流程异常
 *
 * @author xuml
 */
@Getter
public class FlowException extends Exception {

    private String code;
    private String node;

    public FlowException(String message) {
        super(message);
    }

    public FlowException(String code, String message) {
        super(message);
        this.code = code;
    }

    public FlowException(String code, String node, String message) {
        super(message);
        this.code = code;
        this.node = node;
    }

    public FlowException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public FlowException(String code, String node, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.node = node;
    }

}
