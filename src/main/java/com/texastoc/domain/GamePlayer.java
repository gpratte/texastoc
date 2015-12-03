package com.texastoc.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.DateTime;

public class GamePlayer  implements Comparable<GamePlayer> {

    private int id;
    private int playerId;
    private int gameId;
    private Integer buyIn;
    private Integer reBuyIn;
    private Integer finish;
    private Integer chop;
    private Integer points;
    private Integer nonTocPoints;
    private String note;
    private boolean annualTocPlayer;
    private boolean quarterlyTocPlayer;
    private boolean knockedOut;
    private boolean optIn;
    private boolean emailOptIn;
    private DateTime lastCalculated;
    private Player player;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public Integer getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(Integer buyIn) {
        this.buyIn = buyIn;
    }

    public Integer getReBuyIn() {
        return reBuyIn;
    }

    public void setReBuyIn(Integer reBuyIn) {
        this.reBuyIn = reBuyIn;
    }

    public Integer getFinish() {
        return finish;
    }

    public void setFinish(Integer finish) {
        this.finish = finish;
    }

    public Integer getChop() {
        return chop;
    }

    public void setChop(Integer chop) {
        this.chop = chop;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getNonTocPoints() {
        return nonTocPoints;
    }

    public void setNonTocPoints(Integer nonTocPoints) {
        this.nonTocPoints = nonTocPoints;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isAnnualTocPlayer() {
        return annualTocPlayer;
    }

    public void setAnnualTocPlayer(boolean annualTocPlayer) {
        this.annualTocPlayer = annualTocPlayer;
    }

    public boolean isQuarterlyTocPlayer() {
        return quarterlyTocPlayer;
    }

    public void setQuarterlyTocPlayer(boolean quarterlyTocPlayer) {
        this.quarterlyTocPlayer = quarterlyTocPlayer;
    }

    public boolean isKnockedOut() {
        return knockedOut;
    }

    public void setKnockedOut(boolean out) {
        this.knockedOut = out;
    }

    public boolean isOptIn() {
        return optIn;
    }

    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }

    public boolean isEmailOptIn() {
        return emailOptIn;
    }

    public void setEmailOptIn(boolean emailOptIn) {
        this.emailOptIn = emailOptIn;
    }

    public DateTime getLastCalculated() {
        return lastCalculated;
    }

    public void setLastCalculated(DateTime lastCalculated) {
        this.lastCalculated = lastCalculated;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isFinalizable() {
        
        if (buyIn == null || buyIn <= 0) {
            return false;
        }
        
        return true;
    }

    /**
     * Sort in reverse order (most chops first)
     */
    @Override
    public int compareTo(GamePlayer o) {
        if (this.chop < o.chop) {
            return 1;
        } else if (this.chop > o.chop) {
            return -1;
        } else {
            return 0;
        }
    }

    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
