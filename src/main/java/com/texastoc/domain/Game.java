package com.texastoc.domain;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.validation.Errors;

import com.texastoc.util.DateConverter;

public class Game implements Comparable<Game> {
    
    private int id;
    private int seasonId;
    private LocalDate gameDate;
    private Integer hostId;
    private String note;
    private int totalBuyIn;
    private int totalReBuy;
    private int adjustPot;
    private int totalAnnualToc;
    private int totalQuarterlyToc;
    private int numPlayers;
    private int payoutDelta;
    private boolean finalized;
    private boolean doubleBuyIn;
    private DateTime lastCalculated;
    private List<GamePlayer> players;
    private List<GamePayout> payouts;
    private List<Seat> seating;
    
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
    public LocalDate getGameDate() {
        return gameDate;
    }
    public void setGameDate(LocalDate gameDate) {
        this.gameDate = gameDate;
    }
    public String getGameDateText() {
        if (gameDate != null) {
            return DateConverter.getDateAsString(gameDate);
        } else {
            return null;
        }
    }
    public void setGameDateText(String text) {
        if (StringUtils.isNotEmpty(text)) {
            this.gameDate = DateConverter.getStringAsDate(text);
        } else {
            this.gameDate = null;
        }
    }
    public Integer getHostId() {
        return hostId;
    }
    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
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
    public int getAdjustPot() {
        return adjustPot;
    }
    public void setAdjustPot(int adjustPot) {
        this.adjustPot = adjustPot;
    }
    public int getTotalAnnualToc() {
        return totalAnnualToc;
    }
    public void setTotalAnnualToc(int totalAnnualToc) {
        this.totalAnnualToc = totalAnnualToc;
    }
    public int getTotalQuarterlyToc() {
        return totalQuarterlyToc;
    }
    public void setTotalQuarterlyToc(int totalQuarterlyToc) {
        this.totalQuarterlyToc = totalQuarterlyToc;
    }
    public int getNumPlayers() {
        return numPlayers;
    }
    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }
    public int getPayoutDelta() {
        return payoutDelta;
    }
    public void setPayoutDelta(int payoutDelta) {
        this.payoutDelta = payoutDelta;
    }
    public boolean isFinalized() {
        return finalized;
    }
    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }
    public boolean isDoubleBuyIn() {
        return doubleBuyIn;
    }
    public void setDoubleBuyIn(boolean doubleBuyIn) {
        this.doubleBuyIn = doubleBuyIn;
    }
    public DateTime getLastCalculated() {
        return lastCalculated;
    }
    public void setLastCalculated(DateTime lastCalculated) {
        this.lastCalculated = lastCalculated;
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }
    public void setPlayers(List<GamePlayer> players) {
        this.players = players;
    }
    public List<GamePayout> getPayouts() {
        return payouts;
    }
    public void setPayouts(List<GamePayout> payouts) {
        this.payouts = payouts;
    }
    public List<Seat> getSeating() {
        return seating;
    }
    public void setSeating(List<Seat> seating) {
        this.seating = seating;
    }
    
    public boolean isFinalizable() {
        if (players == null) {
            return false;
        }
        int count = 0;
        for (GamePlayer player : players) {
            if (player.getFinish() != null && player.getFinish() > 0) {
                ++count;
                if (count == 10 || count == players.size()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    public void validate(Object obj, Errors errors) {

        Game game = (Game) obj;
        if (game.getGameDate() == null) {
            errors.rejectValue("gameDate", "emptyDate",
                    "Game date must be set");
        }
    }
    @Override
    public int compareTo(Game o) {
        if (gameDate.isBefore(o.gameDate)) {
            return -1;
        } else if (gameDate.isAfter(o.gameDate)) {
            return 1;
        } else {
            return 0;
        }
    }
}
