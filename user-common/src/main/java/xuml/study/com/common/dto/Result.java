package xuml.study.com.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 统一返回结果类
 *
 * @author xuml
 */
@Getter
@Setter
@SuppressWarnings("unused")
public final class Result<E> {
    public static final String DEFAULT_CODE = "000000";
    private String code = DEFAULT_CODE;
    private long total;
    private String message;
    private String level;
    private E data;
    private Map<Object, Object> extData;

    private Result() {
    }

    private Result(String code, long total, String message, Level level, E data) {
        this.code = code;
        this.total = total;
        this.message = message;
        this.level = Objects.nonNull(level) ? level.name() : null;
        this.data = data;
    }

    public static <U> Result<U> success() {
        Result<U> r = new Result<>();
        return r.level(Result.Level.INFO);
    }

    public static <U> Result<U> success(U data) {
        Result<U> r = new Result<>();
        r.data(data);
        return r.level(Result.Level.INFO);
    }

    public static <U> Result<U> successMessage(String message) {
        return new Result<>(DEFAULT_CODE, 0L, message, Result.Level.INFO, null);
    }

    public static <U> Result<U> error(String code, String message) {
        return new Result<>(code, 0L, message, Result.Level.ERROR, null);
    }

    public static <U> Result<U> error(String message) {
        return new Result<>("-1", 0L, message, Result.Level.ERROR, null);
    }

    public static <U> Result<U> warn(String code, String message) {
        return new Result<>(code, 0L, message, Result.Level.WARN, null);
    }

    public Result<E> total(long total) {
        this.total = total;
        return this;
    }

    public Result<E> message(String message) {
        this.message = message;
        return this;
    }

    public Result<E> code(String code) {
        this.code = code;
        return this;
    }

    public Result<E> extData(Map<?, ?> params) {
        if (this.extData == null) {
            this.extData = new HashMap<>();
        }

        this.extData.putAll(params);
        return this;
    }

    public Result<E> extData(Object key, Object value) {
        if (this.extData == null) {
            this.extData = new HashMap<>();
        }

        this.extData.put(key, value);
        return this;
    }

    public Result<E> level(Level level) {
        this.level = level.name();
        return this;
    }

    public Result<E> data(E data) {
        this.data = data;
        return this;
    }

    public static <U> Result<U> create() {
        return new Result<>();
    }

    public String toString() {
        return "Result [code=" + this.code + ", total=" + this.total + ", message=" + this.message + ", level=" + this.level + ", data=" + this.data + ", extData=" + this.extData + "]";
    }

    public enum Level {
        INFO,
        DEBUG,
        WARN,
        ERROR
    }
}
