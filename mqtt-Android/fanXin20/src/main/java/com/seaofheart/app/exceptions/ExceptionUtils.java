package com.seaofheart.app.exceptions;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class ExceptionUtils {
    public ExceptionUtils() {
    }

    public static int fromExceptionToErrorCode(Exception var0) {
        short var1 = -999;
        if(var0 instanceof UnknownHostException) {
            var1 = -1002;
        } else if(var0 instanceof NoRouteToHostException) {
            var1 = -1003;
        } else if(var0 instanceof ConnectException) {
            var1 = -1003;
        } else if(var0 instanceof SocketException) {
            var1 = -1003;
        } else if(var0 instanceof SocketTimeoutException) {
            var1 = -1004;
        } else if(var0 instanceof AuthenticationException) {
            var1 = -1005;
        } else if(var0 instanceof KeyStoreException) {
            var1 = -1006;
        } else if(var0 instanceof IOException) {
            var1 = -1007;
        } else if(var0 instanceof CertificateException) {
            var1 = -1008;
        } else if(var0 instanceof NoSuchAlgorithmException) {
            var1 = -1009;
        } else if(var0 instanceof UnrecoverableKeyException) {
            var1 = -1009;
        } else if(var0 instanceof KeyManagementException) {
            var1 = -1009;
        } else if(var0.getMessage().contains("User removed")) {
            var1 = -1023;
        } else if(var0.getMessage().contains("conflict")) {
            var1 = -1014;
        } else if(var0 instanceof XMPPException) {
            if(var0.getMessage().contains("Not connected to server")) {
                var1 = -1003;
            }
        } else if(var0 instanceof XMPPException && var0.getMessage().contains("No response")) {
            var1 = -1003;
        }

        return var1;
    }
}

