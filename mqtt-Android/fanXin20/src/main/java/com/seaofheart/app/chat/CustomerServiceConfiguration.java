package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.List;

public class CustomerServiceConfiguration {
    private List<String> agents = null;
    private long connectionExpiredDuration = 172800000L;

    public CustomerServiceConfiguration() {
    }

    public void setAgents(List<String> var1) {
        this.agents = var1;
    }

    public List<String> getAgents() {
        return this.agents;
    }
}

