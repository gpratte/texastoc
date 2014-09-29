package com.texastoc.domain;

public class PointSystem {

    private int numPlayers;
    private double tenthPlaceIncr; 
    private int tenthPlacePoints;
    private double multiplier;
    
    public int getNumPlayers() {
        return numPlayers;
    }
    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }
    public double getTenthPlaceIncr() {
        return tenthPlaceIncr;
    }
    public void setTenthPlaceIncr(double tenthPlaceIncr) {
        this.tenthPlaceIncr = tenthPlaceIncr;
    }
    public int getTenthPlacePoints() {
        return tenthPlacePoints;
    }
    public void setTenthPlacePoints(int tenthPlacePoints) {
        this.tenthPlacePoints = tenthPlacePoints;
    }
    public double getMultiplier() {
        return multiplier;
    }
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }
    
}
