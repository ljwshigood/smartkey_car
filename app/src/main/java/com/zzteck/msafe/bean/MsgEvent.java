package com.zzteck.msafe.bean;

/**
 * Created by Administrator on 2018/8/3 0003.
 */

public class MsgEvent {

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type ;

    private String msg;

    public MsgEvent(String msg, int type) {
        super();
        this.msg = msg;
        this.type = type ;
    }

    public String getMsg() {
        return msg;
    }
}
