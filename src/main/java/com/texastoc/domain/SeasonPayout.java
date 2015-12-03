package com.texastoc.domain;

import org.apache.commons.lang.builder.ToStringBuilder;


public class SeasonPayout {

    private int id;
    private int seasonId;
    private String place;
    private int amount;
    private String description;
    private boolean temp;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getSeasonId() {
        return seasonId;
    }
    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }
    public String getPlace() {
        return place;
    }
    public void setPlace(String place) {
        this.place = place;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
	public boolean isTemp() {
        return temp;
    }
    public void setTemp(boolean temp) {
        this.temp = temp;
    }
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
