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
    private boolean finalized;
    private List<QuarterlySeason> quarterlies = new ArrayList<QuarterlySeason>();
    private List<Game> games = new ArrayList<Game>();
    private List<SeasonPlayer> seasonPlayers = new ArrayList<SeasonPlayer>();
    private List<SeasonHistoryEntry> histories = null;

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
    public boolean isFinalized() {
        return finalized;
    }
    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
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
