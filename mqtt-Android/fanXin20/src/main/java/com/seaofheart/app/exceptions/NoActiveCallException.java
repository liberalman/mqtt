package com.seaofheart.app.exceptions;

/**
 * Created by Administrator on 2016/4/15.
 */
public class NoActiveCallException extends EaseMobException {
    private static final long serialVersionUID = 1L;

    public NoActiveCallException() {
    }

    public NoActiveCallException(String var1) {
        super(var1);
    }

    public NoActiveCallException(String var1, Throwable var2) {
        super(var1);
        super.initCause(var2);
    }
}