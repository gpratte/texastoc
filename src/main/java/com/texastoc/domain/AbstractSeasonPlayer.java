package com.texastoc.domain;


public class AbstractSeasonPlayer {

    protected int id;
    protected int playerId;
    protected int place;
    protected int points;
    protected int numEntries;
    protected Integer tie;
    protected Player player;

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

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getNumEntries() {
        return numEntries;
    }

    public void setNumEntries(int numEntries) {
        this.numEntries = numEntries;
    }

    public Integer getTie() {
        return tie;
    }

    public void setTie(Integer tie) {
        this.tie = tie;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * Sort in reverse order (most points first)
     */
    protected int compareTo(AbstractSeasonPlayer o) {
        if (this.points < o.points) {
            return 1;
        } else if (this.points > o.points) {
            return -1;
        } else {
            return 0;
        }
    }
}
