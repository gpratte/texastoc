package com.texastoc.domain;

import java.util.HashMap;
import java.util.Map;

public class TopTenPoints {

    private int numPlayers;
    private Map<Integer, Integer> placePoints = new HashMap<Integer, Integer>();
    
    public int getNumPlayers() {
        return numPlayers;
    }
    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }
    
    public int getPointsForPlace(int place) {
        return placePoints.get(place);
    }
    public void setPointsForPlace(int place, int points) {
        if (place < 1 || place > 10) {
            throw new IllegalArgumentException("Place must be 1 through 10");
        }
        placePoints.put(place, points);
    }
}
