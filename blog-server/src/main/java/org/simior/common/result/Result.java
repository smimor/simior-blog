package org.simior.common.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果封装类
 *
 * @param <T> 返回数据的类型
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;


    /**
     * 成功响应，返回数据
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应，仅返回消息，无数据
     * <p>
     * 用于仅需返回成功提示文案的场景（如登出、删除等操作）
     *
     * @param msg 响应消息
     * @param <T> 数据类型
     * @return 成功结果（data 为 null）
     */
    public static <T> Result<T> success(String msg) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        return result;
    }

    /**
     * 成功响应，返回消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }


    /**
     * 错误响应，自定义错误消息
     *
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    /**
     * 错误响应，自定义状态码和消息
     *
     * @param code 状态码
     * @param msg  错误消息
     * @param <T>  数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

}
