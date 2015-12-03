package com.texastoc.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.Errors;


public class QuarterlySeason extends BaseSeason {
//public class QuarterlySeason extends BaseSeason implements Validator {
    
    private int seasonId;
    private Quarter quarter;
    private int totalQuarterlyToc;
    private boolean finalized;
    private List<Game> games = new ArrayList<Game>();
    private List<QuarterlySeasonPlayer> quarterlySeasonPlayers = new ArrayList<QuarterlySeasonPlayer>();
    private List<QuarterlyPayout> payouts = new ArrayList<QuarterlyPayout>();

    public int getSeasonId() {
        return seasonId;
    }
    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }
    public Quarter getQuarter() {
        return quarter;
    }
    public void setQuarter(Quarter quarter) {
        this.quarter = quarter;
    }
    public int getTotalQuarterlyToc() {
        return totalQuarterlyToc;
    }
    public void setTotalQuarterlyToc(int totalQuarterlyToc) {
        this.totalQuarterlyToc = totalQuarterlyToc;
    }
    public boolean isFinalized() {
        return finalized;
    }
    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }
    public List<Game> getGames() {
        return games;
    }
    public void setGames(List<Game> games) {
        this.games = games;
    }
    public List<QuarterlySeasonPlayer> getQuarterlySeasonPlayers() {
        return quarterlySeasonPlayers;
    }
    public void setQuarterlySeasonPlayers(List<QuarterlySeasonPlayer> quarterlySeasonPlayers) {
        this.quarterlySeasonPlayers = quarterlySeasonPlayers;
    }
    public List<QuarterlyPayout> getPayouts() {
        return payouts;
    }
    public void setPayouts(List<QuarterlyPayout> payouts) {
        this.payouts = payouts;
    }
    
    /////////////////////
    // Validation stuff
    /////////////////////
     public boolean supports(Class clazz) {
         // Very odd that this has to return tru for Season or
         // else spring blows up
         return QuarterlySeason.class.equals(clazz) || Season.class.equals(clazz);
     }

     public void validate(Object obj, Errors errors) {
         if (obj instanceof QuarterlySeason) {
             super.validate(obj, errors);
         }
     }
}
