package com.texastoc.domain.clock;

import java.util.List;

import com.texastoc.common.HomeGame;

public interface Clock {
    
    public boolean isRunning();
    
    public Level getCurrentLevel();
    
    public Level getNextLevel();

    public List<Level> getLevels();

    public void goToNextLevel();
    
    public void goToPreviousLevel();
    
    public void setRound(String round);

    public String getMaxRound();
    
    public void go();
    
    public void stop();
    
    public void reset();

    public int getRemainingMinutes();
    public int getRemainingSeconds();
    
    public void setHomeGame(HomeGame homeGame);

    public void updateMinute(int value);
    public void updateSecond(int value);
}
