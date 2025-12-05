package com.hospital.ouc.registrationsystem.web.dto;

import com.hospital.ouc.registrationsystem.domain.enums.ResultCodeEnum;
import lombok.Data;

@Data
public class ResultDTO<T> {
    private Integer code;
    private String msg;
    private T data;

    // 成功返回
    public static <T> ResultDTO<T> success(T data) {
        ResultDTO<T> result = new ResultDTO<>();
        result.setCode(ResultCodeEnum.SUCCESS.getCode());
        result.setMsg(ResultCodeEnum.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    // 失败返回
    public static <T> ResultDTO<T> fail(ResultCodeEnum codeEnum) {
        ResultDTO<T> result = new ResultDTO<>();
        result.setCode(codeEnum.getCode());
        result.setMsg(codeEnum.getMsg());
        result.setData(null);
        return result;
    }

    // 自定义失败返回
    public static <T> ResultDTO<T> fail(Integer code, String msg) {
        ResultDTO<T> result = new ResultDTO<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}
