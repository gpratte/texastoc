package com.texastoc.service.calculate;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Chop {
    
    private int orgAmount;
    private int chopAmount;
    private float percent;

    public int getOrgAmount() {
        return orgAmount;
    }
    public void setOrgAmount(int orgAmount) {
        this.orgAmount = orgAmount;
    }
    public int getChopAmount() {
        return chopAmount;
    }
    public void setChopAmount(int chopAmount) {
        this.chopAmount = chopAmount;
    }
    public float getPercent() {
        return percent;
    }
    public void setPercent(float percent) {
        this.percent = percent;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
