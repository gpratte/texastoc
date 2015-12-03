package com.texastoc.domain;

public class SeasonHistoryEntry {

    private int seasonId;
    private int points;
    private int playerId;
    private Player player;
    private int entries;
    private boolean wsop;
    
    public int getSeasonId() {
        return seasonId;
    }
    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }
    public int getPoints() {
        return points;
    }
    public void setPoints(int points) {
        this.points = points;
    }
    public int getPlayerId() {
        return playerId;
    }
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    public int getEntries() {
        return entries;
    }
    public void setEntries(int entries) {
        this.entries = entries;
    }
	public boolean isWsop() {
		return wsop;
	}
	public void setWsop(boolean wsop) {
		this.wsop = wsop;
	}
}
