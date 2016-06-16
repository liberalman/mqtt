package com.seaofheart.app.exceptions;

/**
 * Created by Administrator on 2016/4/14.
 */
public class PermissionException extends EaseMobException {
    private static final long serialVersionUID = 1L;

    public PermissionException() {
    }

    public PermissionException(String var1) {
        super(var1);
    }

    public PermissionException(String var1, Throwable var2) {
        super(var1);
        super.initCause(var2);
    }

    public PermissionException(int var1, String var2) {
        super(var1, var2);
    }
}
