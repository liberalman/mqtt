package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.Random;

class RandomDelay {
    int randomBase = -1;

    RandomDelay() {
    }

    public void reset() {
        this.randomBase = -1;
    }

    public int timeDelay(int var1) {
        if(this.randomBase <= -1) {
            this.randomBase = (new Random()).nextInt(1) + 5;
            return this.randomBase;
        } else {
            return var1 > 3 && var1 <= 9?this.randomBase + (new Random()).nextInt(5):(var1 > 9?this.randomBase * 3 + (new Random()).nextInt(5):this.randomBase);
        }
    }
}

