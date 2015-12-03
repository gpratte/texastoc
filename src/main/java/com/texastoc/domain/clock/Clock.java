package com.texastoc.domain.clock;

import java.util.List;

import com.texastoc.common.HomeGame;

public interface Clock {
    
    public boolean isRunning();
    
    public Level getCurrentLevel();
    
    public Level getNextLevel();

    public List<Level> getLevels();

    public void goToNextLevel(String round);
    
    public void goToPreviousLevel(String round);
    
    public String getMaxRound();
    
    public void start(String round, Integer minutes, Integer seconds);
    
    public void setRound(String round);

    public void pause();
    
    public void reset();

    public int getRemainingMinutes();
    public int getRemainingSeconds();
    
    public void setHomeGame(HomeGame homeGame);
    
    public void sync();
}
