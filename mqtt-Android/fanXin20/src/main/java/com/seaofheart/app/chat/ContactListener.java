package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.List;

public interface ContactListener {
    void onContactAdded(List<String> var1);

    void onContactDeleted(List<String> var1);

    void onContactInvited(String var1, String var2);

    void onContactAgreed(String var1);

    void onContactRefused(String var1);
}

