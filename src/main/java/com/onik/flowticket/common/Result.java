package com.onik.flowticket.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果。
 *
 * @param <T> data 字段的数据类型
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    /**
     * 返回一个不携带数据的成功结果。
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 1;
        return result;
    }

    /**
     * 返回一个携带业务数据的成功结果。
     *
     * @param object 需要放入 data 字段的数据
     */
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    /**
     * 返回一个失败结果。
     *
     * @param msg 失败原因，会放入 msg 字段
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<T>();
        result.msg = msg;
        result.code = 0;
        return result;
    }
}
