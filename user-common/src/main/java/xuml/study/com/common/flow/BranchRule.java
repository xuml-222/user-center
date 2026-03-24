package xuml.study.com.common.flow;

import lombok.Data;

/**
 * 流程分支规则
 * 用于定义条件分支的规则
 *
 * @author xuml
 */
@Data
public class BranchRule {

    /**
     * 分支名称
     */
    private String branchName;

    /**
     * 目标节点ID
     * 当条件满足时跳转到此节点
     */
    private String targetNodeId;

    /**
     * 条件表达式
     * 支持简单的表达式，如：
     * - "status=success"  等于
     * - "amount>1000"     大于
     * - "age>=18"         大于等于
     * - "name!=null"      不等于
     * - "flag=true"       布尔值
     */
    private String condition;

    /**
     * 条件类型
     */
    private ConditionType conditionType = ConditionType.EQUALS;

    /**
     * 优先级（数字越小优先级越高）
     */
    private int priority = 0;

    /**
     * 是否为默认分支
     * 当其他分支条件都不满足时使用
     */
    private boolean defaultBranch = false;

    /**
     * 条件类型枚举
     */
    public enum ConditionType {
        EQUALS,         // 等于
        NOT_EQUALS,     // 不等于
        GREATER_THAN,   // 大于
        GREATER_EQUAL,  // 大于等于
        LESS_THAN,      // 小于
        LESS_EQUAL,     // 小于等于
        CONTAINS,       // 包含
        NOT_CONTAINS,   // 不包含
        IS_NULL,       // 为空
        IS_NOT_NULL,    // 不为空
        BOOLEAN        // 布尔值判断
    }

    /**
     * 评估条件是否满足
     *
     * @param context 流程上下文
     * @return 是否满足条件
     */
    public boolean evaluate(FlowContext context) {
        if (defaultBranch) {
            return true;
        }

        if (condition == null || condition.isEmpty()) {
            return false;
        }

        // 解析表达式
        ExpressionParser parser = new ExpressionParser(condition, conditionType);
        return parser.evaluate(context);
    }

    /**
     * 简单的表达式解析器
     */
    private static class ExpressionParser {
        private final String expression;
        private final ConditionType conditionType;

        public ExpressionParser(String expression, ConditionType conditionType) {
            this.expression = expression;
            this.conditionType = conditionType;
        }

        public boolean evaluate(FlowContext context) {
            try {
                switch (conditionType) {
                    case EQUALS:
                        return evaluateEquals(context);
                    case NOT_EQUALS:
                        return !evaluateEquals(context);
                    case GREATER_THAN:
                        return evaluateCompare(context) > 0;
                    case GREATER_EQUAL:
                        return evaluateCompare(context) >= 0;
                    case LESS_THAN:
                        return evaluateCompare(context) < 0;
                    case LESS_EQUAL:
                        return evaluateCompare(context) <= 0;
                    case CONTAINS:
                        return evaluateContains(context);
                    case NOT_CONTAINS:
                        return !evaluateContains(context);
                    case IS_NULL:
                        return evaluateIsNull(context);
                    case IS_NOT_NULL:
                        return !evaluateIsNull(context);
                    case BOOLEAN:
                        return evaluateBoolean(context);
                    default:
                        return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        private boolean evaluateEquals(FlowContext context) {
            String[] parts = expression.split("=");
            if (parts.length != 2) return false;

            String key = parts[0].trim();
            String expectedValue = parts[1].trim();

            Object actualValue = context.getData(key);
            if (actualValue == null) {
                return "null".equalsIgnoreCase(expectedValue);
            }

            return actualValue.toString().equals(expectedValue);
        }

        private int evaluateCompare(FlowContext context) {
            String[] parts = expression.split("[><]");
            if (parts.length < 2) return 0;

            String key = parts[0].trim();
            Object actualValue = context.getData(key);

            if (actualValue == null) return 0;
            if (!(actualValue instanceof Number)) return 0;

            double actual = ((Number) actualValue).doubleValue();
            double expected = Double.parseDouble(parts[1].trim());

            return Double.compare(actual, expected);
        }

        private boolean evaluateContains(FlowContext context) {
            String[] parts = expression.split("=");
            if (parts.length != 2) return false;

            String key = parts[0].trim();
            String value = parts[1].trim();

            Object actualValue = context.getData(key);
            if (actualValue == null) return false;

            return actualValue.toString().contains(value);
        }

        private boolean evaluateIsNull(FlowContext context) {
            Object value = context.getData(expression);
            return value == null || value.toString().isEmpty();
        }

        private boolean evaluateBoolean(FlowContext context) {
            Object value = context.getData(expression);
            if (value == null) return false;

            if (value instanceof Boolean) {
                return (Boolean) value;
            }

            return "true".equalsIgnoreCase(value.toString());
        }
    }
}
