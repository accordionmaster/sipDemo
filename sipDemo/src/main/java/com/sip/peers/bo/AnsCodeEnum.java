package com.sip.peers.bo;

import java.util.HashMap;
import java.util.Map;

public enum AnsCodeEnum {

    Ring_Failed("RING_FAILED","0"),
    ALARM("ALARM","1"),
    PICK_UP("PICK_UP","2"),
    TALKING("TALKIING","3"),
    NOT_IN_SERVICE("NOT_IN_SERVICE","5"),
    ARREARS("ARREARS","6"),
    CALLED_REFUSE("CALLED_REFUSE","7"),
    CALLED_OFF("CALLED_OFF","8"),
    CALLED_EMPTY_NUMBER("CALLED_EMPTY_NUMBER","9"),
    LINE_ERROR("LINE_ERROR","11"),
    CALL_TIMEOUT("CALL_TIMEOUT","12"),
    OVERRUN("OVERRUN","13"),
    LINE_TIMEOUT_WITHOUT_RETURN("LINE_TIMEOUT_WITHOUT_RETURN","14"),
    CALLER_OVERRUN("CALLER_OVERRUN","15"),
    BUSY_LINE("BUSY_LINE","16"),
    OTHER("OTHER","99");




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

    AnsCodeEnum(String msg, String code) {
        this.code = code;
        this.msg = msg;
    }

    AnsCodeEnum() {
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
        for (AnsCodeEnum temp : AnsCodeEnum.values()) {
            map.put(temp.getMsg(), temp.getCode());
        }
    }
}
