package com.texastoc.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

public class QuarterlyPayout {

    private int quarterId;
    private int place;
    private int amount;

    public int getQuarterId() {
        return quarterId;
    }
    public void setQuarterId(int quarterId) {
        this.quarterId = quarterId;
    }
    public int getPlace() {
        return place;
    }
    public void setPlace(int place) {
        this.place = place;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
