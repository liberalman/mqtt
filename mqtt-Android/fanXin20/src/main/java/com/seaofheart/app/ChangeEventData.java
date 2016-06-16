package com.seaofheart.app;

/**
 * Created by Administrator on 2016/4/14.
 */
public class ChangeEventData<T> {
    protected T oldValue;
    protected T newValue;

    public ChangeEventData() {
    }

    public void setOldValue(T var1) {
        this.oldValue = var1;
    }

    public void setNewValue(T var1) {
        this.newValue = var1;
    }

    public T getOldValue() {
        return this.oldValue;
    }

    public T getNewValue() {
        return this.newValue;
    }
}
