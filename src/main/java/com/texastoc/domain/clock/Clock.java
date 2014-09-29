package com.texastoc.domain.clock;


public interface Clock {
    
    public boolean isRunning();
    
    public Level getCurrentLevel();
    
    public Level getNextLevel();
    
    public void goToNextLevel(String round);
    
    public void goToPreviousLevel(String round);
    
    public String getMaxRound();
    
    public void start(String round, Integer minutes, Integer seconds);
    
    public void pause();
    
    public void reset();

    public int getRemainingMinutes();
    public int getRemainingSeconds();
    
    public void sync();
}
