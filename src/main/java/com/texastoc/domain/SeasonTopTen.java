package com.texastoc.domain;

import java.util.Collections;
import java.util.List;

public class SeasonTopTen {
    
    private static final List<PlayerCount> EMPTY_LIST = Collections.emptyList();

    private List<PlayerCount> gamesPlayed;
    private List<PlayerCount> pointsPerGame;
    private List<PlayerCount> firstPlace;
    private List<PlayerCount> grossMoney;
    private List<PlayerCount> netMoney;
    private List<PlayerCount> netMoneyPerGame;
    private List<PlayerCount> cashes;
    private List<PlayerCount> moneyBubble;
    private List<PlayerCount> finalTable;

    public List<PlayerCount> getGamesPlayed() {
        if (gamesPlayed == null) {
            return EMPTY_LIST;
        } else {
            return gamesPlayed;
        }
    }
    public void setGamesPlayed(List<PlayerCount> gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
    public List<PlayerCount> getPointsPerGame() {
        if (pointsPerGame == null) {
            return EMPTY_LIST;
        } else {
            return pointsPerGame;
        }
    }
    public void setPointsPerGame(List<PlayerCount> pointsPerGame) {
        this.pointsPerGame = pointsPerGame;
    }
    public List<PlayerCount> getFirstPlace() {
		return firstPlace;
	}
	public void setFirstPlace(List<PlayerCount> firstPlace) {
		this.firstPlace = firstPlace;
	}
	public List<PlayerCount> getGrossMoney() {
        return grossMoney;
    }
    public void setGrossMoney(List<PlayerCount> grossMoney) {
        this.grossMoney = grossMoney;
    }
    public List<PlayerCount> getNetMoney() {
        return netMoney;
    }
    public void setNetMoney(List<PlayerCount> netMoney) {
        this.netMoney = netMoney;
    }
    public List<PlayerCount> getNetMoneyPerGame() {
        return netMoneyPerGame;
    }
    public void setNetMoneyPerGame(List<PlayerCount> netMoneyPerGame) {
        this.netMoneyPerGame = netMoneyPerGame;
    }
    public List<PlayerCount> getCashes() {
        if (cashes == null) {
            return EMPTY_LIST;
        } else {
            return cashes;
        }
    }
    public void setCashes(List<PlayerCount> cashes) {
        this.cashes = cashes;
    }
    public List<PlayerCount> getMoneyBubble() {
        if (moneyBubble == null) {
            return EMPTY_LIST;
        } else {
            return moneyBubble;
        }
    }
    public void setMoneyBubble(List<PlayerCount> moneyBubble) {
        this.moneyBubble = moneyBubble;
    }
    public List<PlayerCount> getFinalTable() {
        return finalTable;
    }
    public void setFinalTable(List<PlayerCount> finalTable) {
        this.finalTable = finalTable;
    }
    
}
