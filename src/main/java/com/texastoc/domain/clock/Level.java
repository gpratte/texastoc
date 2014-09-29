package com.texastoc.domain.clock;


public class Level {
    private String round;
    private LevelType type;
    private int smallBlind;
    private int bigBlind;
    private int ante;
    private long duration;
    
    public Level(String round, LevelType type, int small, int big, int ante, 
            long duration, Clock clock) {
        this.round = round;
        this.type = type;
        this.smallBlind = small;
        this.bigBlind = big;
        this.ante = ante;
        this.duration = duration;
    }
    
    public String getRound() {
        return round;
    }
    
    public LevelType getType() {
        return type;
    }

    public int getSmallBlind() {
        return smallBlind;
        
    }
    public int getBigBlind() {
        return bigBlind;
    }
    
    public int getAnte() {
        return ante;
    }
    
    public long getDuration() {
        return duration;
    }

    public int getDurationMinutes() {
        return (int)(duration / 60000l);
    }

}
