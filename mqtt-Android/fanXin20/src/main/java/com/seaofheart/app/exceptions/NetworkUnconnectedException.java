package com.seaofheart.app.exceptions;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class NetworkUnconnectedException extends EaseMobException {
    private static final long serialVersionUID = 1L;

    public NetworkUnconnectedException() {
    }

    public NetworkUnconnectedException(String var1) {
        super(var1);
    }

    public NetworkUnconnectedException(String var1, Throwable var2) {
        super(var1);
        super.initCause(var2);
    }

    public NetworkUnconnectedException(int var1, String var2) {
        super(var2);
        this.errorCode = var1;
    }
}

