package com.seaofheart.app.chat.core;

import org.jivesoftware.smack.ConnectionListener;

public interface ChatConnectionListener extends ConnectionListener { // class q
    void onConnectionSuccessful();

    void onConnecting();
}