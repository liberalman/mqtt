package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class ChatRoom extends MultiUserChatRoomModelBase {
    public ChatRoom() {
    }

    public ChatRoom(String var1) {
        super(var1);
    }

    public ChatRoom(String var1, String var2) {
        super(var1);
        this.nick = var2;
    }

    public synchronized void addMember(String var1) {
        if(!this.members.contains(var1)) {
            this.members.add(var1);
            ++this.affiliationsCount;
        }

    }

    public synchronized void removeMember(String var1) {
        if(this.members.contains(var1)) {
            this.members.remove(var1);
            --this.affiliationsCount;
        }

    }
}

