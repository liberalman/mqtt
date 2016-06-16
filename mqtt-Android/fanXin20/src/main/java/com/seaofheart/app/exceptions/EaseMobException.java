package com.seaofheart.app.exceptions;

/**
 * Created by Administrator on 2016/4/14.
 */
public class EaseMobException extends Exception {
    protected int errorCode = -1;
    private static final long serialVersionUID = 1L;

    public EaseMobException() {
    }

    public EaseMobException(String var1) {
        super(var1);
    }

    public EaseMobException(String var1, Throwable var2) {
        super(var1);
        super.initCause(var2);
    }

    public EaseMobException(int var1, String var2) {
        super(var2);
        this.errorCode = var1;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int var1) {
        this.errorCode = var1;
    }
}
