package com.sip.peers.bo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum RingResponseCodeEnum {

    SUCCESS("success","000000"),
    RESLOVE_FAILED("resolve_failed","108100"),
    ID_NOT_EXIST("id_not_exist","108101"),
    OVER_QUEUE("over_queue","108102"),
    CALL_RESOURCE_NOT_ENOUGH("call_resource_not_enough","108103"),
    OTHER_ERROR("other_error","108104");


    private String code;

    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    RingResponseCodeEnum(String msg, String code) {
        this.code = code;
        this.msg = msg;
    }

    RingResponseCodeEnum() {
    }

    public static final Map<String, String> map = new HashMap<String, String>();

    //通过number获取对应的msg
    public static String getMsg(String code){
        return map.get(code);
    }

    public static String getCode(String msg){
        return map.get(msg);
    }

    static {
        for (RingResponseCodeEnum temp : RingResponseCodeEnum.values()) {
            map.put(temp.getMsg(), temp.getCode());
        }
    }
}
