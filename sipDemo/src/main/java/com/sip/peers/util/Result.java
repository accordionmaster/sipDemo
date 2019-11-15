package com.sip.peers.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @program: sipDemo
 * @description:
 * @author: wangxp
 * @create: 2019-11-15 09:58
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 返回码，默认是失败=-1，成功=0
     */
    private int code = -1;

    /**
     * 描述信息
     */
    private String msg = "";

    /**
     * 返回结果实体
     */
    private T data;


    public T getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 成功时候的调用
     *
     * @return
     */
    public static <T> Result<T> success(T data) {
        Result<T> dataResult = new Result<>();
        dataResult.setCode(0);
        dataResult.setMsg("success");
        dataResult.setData(data);
        return dataResult;
    }

    /**
     * 成功，不需要传入参数
     *
     * @return
     */
    public static <T> Result<T> success() {
        return (Result<T>) success("");
    }

    /**
     * 失败时候的调用
     *
     * @return
     */
    public static <T> Result<T> error(IErrorCode cm) {
        Result<T> data = new Result<>();
        data.setCode(cm.getErrorCode());
        data.setMsg(cm.getErrorMessage());
        return data;
    }

    @Override
    public String toString() {
        return "Result [ code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
