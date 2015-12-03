package com.texastoc.common;


public enum HomeGame {
	
	TOC(1), CPPL(2);
	
	private int marker;
	
	private HomeGame(int marker) {
		this.marker = marker;
	}

	public int getMarker() {
		return marker;
	}
	
    public static HomeGame fromInt(int value) {
        HomeGame[] homegames = HomeGame.values();
        for(HomeGame homegame : homegames) {
            if (homegame.getMarker() == value) {
                return homegame;
            }
        }
        
        throw new IllegalArgumentException("Unknown HomeGame value " + value);
    }

}
