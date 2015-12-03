package com.texastoc.domain;

import org.joda.time.LocalDate;


public class Supply {
    
    private int id;
    private LocalDate createDate;
    private Integer gameId;
    private Integer seasonId;
    private SupplyType type;
    private Integer prizePotAmount;
    private Integer annualTocAmount;
    private Integer kittyAmount;
    private String description;
    private boolean invoice;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public LocalDate getCreateDate() {
        return createDate;
    }
    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }
    public Integer getGameId() {
        return gameId;
    }
    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }
    public Integer getSeasonId() {
        return seasonId;
    }
    public void setSeasonId(Integer seasonId) {
        this.seasonId = seasonId;
    }
    public SupplyType getType() {
        return type;
    }
    public void setType(SupplyType type) {
        this.type = type;
    }
    public Integer getPrizePotAmount() {
        return prizePotAmount;
    }
    public void setPrizePotAmount(Integer prizePotAmount) {
        this.prizePotAmount = prizePotAmount;
    }
    public Integer getAnnualTocAmount() {
        return annualTocAmount;
    }
    public void setAnnualTocAmount(Integer annualTocAmount) {
        this.annualTocAmount = annualTocAmount;
    }
    public Integer getKittyAmount() {
        return kittyAmount;
    }
    public void setKittyAmount(Integer kittyAmount) {
        this.kittyAmount = kittyAmount;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isInvoice() {
        return invoice;
    }
    public void setInvoice(boolean invoice) {
        this.invoice = invoice;
    }

}
