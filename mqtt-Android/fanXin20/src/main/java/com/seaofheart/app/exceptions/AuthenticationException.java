package com.seaofheart.app.exceptions;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class AuthenticationException extends EaseMobException {
    private static final long serialVersionUID = 1L;

    public AuthenticationException() {
    }

    public AuthenticationException(String var1) {
        super(var1);
    }

    public AuthenticationException(String var1, Throwable var2) {
        super(var1);
        super.initCause(var2);
    }
}
