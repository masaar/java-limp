package com.masaar.limp;

public class SocketResponse {
    public ResponseArguments args;
    public String msg;
    public Integer status;

    public SocketResponse(Integer status, String msg, ResponseArguments args) {
        this.args = args;
        this.msg = msg;
        this.status = status;
    }

    public SocketResponse(Integer status, String msg) {
        this.args = args;
        this.msg = msg;
    }

    public void setArgs(ResponseArguments args) {
        this.args = args;
    }
}

