package com.texastoc.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.Errors;

public class Season extends BaseSeason {
    
//public class Season extends BaseSeason implements Validator {

    private boolean useHistoricalData;     
    private int totalBuyIn;
    private int totalReBuy;
    private int totalPot;
    private int totalAnnualToc;
    private int totalAnnualTocSupplies;
    private int kittyGameDebit;
    private int annualTocAmount;
    private boolean finalized;
    private String finalTableImage;
    private String finalTableThumb;
    private List<QuarterlySeason> quarterlies = new ArrayList<QuarterlySeason>();
    private List<Game> games = new ArrayList<Game>();
    private List<SeasonPlayer> seasonPlayers = new ArrayList<SeasonPlayer>();
    private List<SeasonHistoryEntry> histories = null;
    private List<SeasonPayout> payouts = new ArrayList<SeasonPayout>();

    public boolean isUseHistoricalData() {
        return useHistoricalData;
    }
    public void setUseHistoricalData(boolean useHistoricalData) {
        this.useHistoricalData = useHistoricalData;
    }
    public int getTotalBuyIn() {
        return totalBuyIn;
    }
    public void setTotalBuyIn(int totalBuyIn) {
        this.totalBuyIn = totalBuyIn;
    }
    public int getTotalReBuy() {
        return totalReBuy;
    }
    public void setTotalReBuy(int totalReBuy) {
        this.totalReBuy = totalReBuy;
    }
    public int getTotalPot() {
        return totalPot;
    }
    public void setTotalPot(int totalPot) {
        this.totalPot = totalPot;
    }
    public int getTotalAnnualToc() {
        return totalAnnualToc;
    }
    public void setTotalAnnualToc(int totalAnnualToc) {
        this.totalAnnualToc = totalAnnualToc;
    }
    public int getTotalAnnualTocSupplies() {
        return totalAnnualTocSupplies;
    }
    public void setTotalAnnualTocSupplies(int totalAnnualTocSupplies) {
        this.totalAnnualTocSupplies = totalAnnualTocSupplies;
    }
    public int getKittyGameDebit() {
        return kittyGameDebit;
    }
    public void setKittyGameDebit(int kittyGameDebit) {
        this.kittyGameDebit = kittyGameDebit;
    }
    public int getAnnualTocAmount() {
        return annualTocAmount;
    }
    public void setAnnualTocAmount(int annualTocAmount) {
        this.annualTocAmount = annualTocAmount;
    }
    public boolean isFinalized() {
        return finalized;
    }
    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }
    public String getFinalTableImage() {
        return finalTableImage;
    }
    public void setFinalTableImage(String finalTableImage) {
        this.finalTableImage = finalTableImage;
    }
    public String getFinalTableThumb() {
        return finalTableThumb;
    }
    public void setFinalTableThumb(String finalTableThumb) {
        this.finalTableThumb = finalTableThumb;
    }
    /**
     * Never null
     * @return non null list
     */
    public List<QuarterlySeason> getQuarterlies() {
        return quarterlies;
    }
    public void setQuarterlies(List<QuarterlySeason> quarterlies) {
        this.quarterlies = quarterlies;
    }
    public List<Game> getGames() {
        return games;
    }
    public void setGames(List<Game> games) {
        this.games = games;
    }
    public List<SeasonPlayer> getSeasonPlayers() {
        return seasonPlayers;
    }
    public void setSeasonPlayers(List<SeasonPlayer> seasonPlayers) {
        this.seasonPlayers = seasonPlayers;
    }
    
    public List<SeasonHistoryEntry> getHistories() {
        return histories;
    }
    public void setHistories(List<SeasonHistoryEntry> histories) {
        this.histories = histories;
    }
    public List<SeasonPayout> getPayouts() {
        return payouts;
    }
    public void setPayouts(List<SeasonPayout> payouts) {
        this.payouts = payouts;
    }
    /////////////////////
    // Validation stuff
    /////////////////////
     public boolean supports(Class clazz) {
         return Season.class.equals(clazz);
     }

     public void validate(Object obj, Errors errors) {
         super.validate(obj, errors);
     }
}
