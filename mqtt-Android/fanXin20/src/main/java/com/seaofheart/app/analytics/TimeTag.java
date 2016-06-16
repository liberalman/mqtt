package com.seaofheart.app.analytics;

/**
 * Created by Administrator on 2016/4/14.
 */
public class TimeTag {
    private long oldTime = 0L;
    private long timeSpent = 0L;

    public TimeTag() {
    }

    public void start() {
        this.oldTime = System.currentTimeMillis();
    }

    public long stop() {
        this.timeSpent = System.currentTimeMillis() - this.oldTime;
        return this.timeSpent;
    }

    public String timeStr() {
        return CollectorUtils.timeToString(this.timeSpent);
    }

    public long timeSpent() {
        return this.timeSpent;
    }
}
