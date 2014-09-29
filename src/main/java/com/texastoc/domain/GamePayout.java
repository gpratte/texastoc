package com.texastoc.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

public class GamePayout {

    private int gameId;
    private int place;
    private int amount;
    private Integer chopAmount;
    private Float chopPercent;

    public int getGameId() {
        return gameId;
    }
    public void setGameId(int gameId) {
        this.gameId = gameId;
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
    
    public Integer getChopAmount() {
        return chopAmount;
    }
    public void setChopAmount(Integer chopAmount) {
        this.chopAmount = chopAmount;
    }
    public Float getChopPercent() {
        return chopPercent;
    }
    public void setChopPercent(Float chopPercent) {
        this.chopPercent = chopPercent;
    }
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
