package com.tellmas.android.redditor;

import com.tellmas.android.redditor.GlobalDefines.RedditorTimeUnit;

public class RedditorTime {

    private long timeValue;
    private RedditorTimeUnit timeUnit;

    public RedditorTime() {
        this.timeValue = 0;
        this.timeUnit = RedditorTimeUnit.SECONDS;
    }

    public RedditorTime(final long timeValue, final RedditorTimeUnit timeUnit) {
        this.timeValue = timeValue;
        this.timeUnit = timeUnit;
    }

    public long getTimeValue() {
        return this.timeValue;
    }
    public void setTimeValue(final long timeValue) {
        this.timeValue = timeValue;
    }
    public RedditorTimeUnit getTimeUnit() {
        return this.timeUnit;
    }
    public void setTimeUnit(final RedditorTimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
