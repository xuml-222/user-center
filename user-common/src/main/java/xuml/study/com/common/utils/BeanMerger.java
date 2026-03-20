
package xuml.study.com.common.utils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * 不使用外部工具，基于 java.beans 的属性复制工具。
 * 规则：
 * - 遍历 source 的属性（跳过 "class"）；
 * - 如果 source 的属性值不为 null，则复制到 target（前提：target 有对应的可写属性且类型兼容）；
 * - 对于原始类型（primitive），其 getter 返回的会被装箱，复制按常规处理（不会被视为 null）；
 * - source 或 target 为 null 时抛出 IllegalArgumentException。
 */
public final class BeanMerger {

    private BeanMerger() {
    }

    public static void copyNonNullProperties(Object source, Object target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("source 和 target 都不能为空");
        }

        try {
            PropertyDescriptor[] sourcePds = Introspector.getBeanInfo(source.getClass(), Object.class)
                    .getPropertyDescriptors();
            PropertyDescriptor[] targetPds = Introspector.getBeanInfo(target.getClass(), Object.class)
                    .getPropertyDescriptors();

            Map<String, PropertyDescriptor> targetMap = new HashMap<>();
            for (PropertyDescriptor pd : targetPds) {
                targetMap.put(pd.getName(), pd);
            }

            for (PropertyDescriptor spd : sourcePds) {
                String name = spd.getName();
                if ("class".equals(name)) {
                    continue;
                }
                PropertyDescriptor tpd = targetMap.get(name);
                if (tpd == null) {
                    continue;
                }
                if (spd.getReadMethod() == null || tpd.getWriteMethod() == null) {
                    continue;
                }

                Object value;
                try {
                    value = spd.getReadMethod().invoke(source);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("读取源属性值失败: " + name, e);
                }

                // 仅当 source 值非 null 时才覆盖；原始类型的 getter 返回被装箱，通常不会为 null
                if (value == null) {
                    continue;
                }

                // 类型兼容检查
                Class<?> writeType = tpd.getWriteMethod().getParameterTypes()[0];
                if (!writeType.isAssignableFrom(value.getClass())) {
                    // 处理基本类型与包装类型的兼容（例如 int 和 Integer）
                    if (!isPrimitiveWrapperCompatible(writeType, value.getClass())) {
                        continue;
                    }
                }

                try {
                    tpd.getWriteMethod().invoke(target, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("写入目标属性值失败: " + name, e);
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException("属性解析失败", e);
        }
    }

    private static boolean isPrimitiveWrapperCompatible(Class<?> writeType, Class<?> valueType) {
        // 映射基本类型到其包装类型
        if (writeType.isPrimitive()) {
            if (writeType == boolean.class && valueType == Boolean.class) return true;
            if (writeType == byte.class && valueType == Byte.class) return true;
            if (writeType == short.class && valueType == Short.class) return true;
            if (writeType == int.class && valueType == Integer.class) return true;
            if (writeType == long.class && valueType == Long.class) return true;
            if (writeType == float.class && valueType == Float.class) return true;
            if (writeType == double.class && valueType == Double.class) return true;
            if (writeType == char.class && valueType == Character.class) return true;
        } else {
            // 目标为包装类型，源为基本类型的装箱类已经是包装类，不需要额外处理
            // 这里可扩展更多兼容规则
            if (writeType.isAssignableFrom(valueType)) return true;
        }
        return false;
    }
}
