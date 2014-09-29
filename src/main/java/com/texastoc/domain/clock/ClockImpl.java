package com.texastoc.domain.clock;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.texastoc.domain.Game;
import com.texastoc.service.GameService;
import com.texastoc.service.mail.MailService;

/**
 * Spring makes this a singleton
 */
@Component
public class ClockImpl implements Clock, TimerListener {
    
    @Autowired 
    private MailService mailService;
    @Autowired 
    private GameService gameService;
    
    static final Logger logger = Logger.getLogger(ClockImpl.class);

    public static final int MINUTES_IN_ROUND = 20;
    private static final long MILLISECONDS_IN_ROUND = 1000 * 60 * MINUTES_IN_ROUND;

    public static final int MINUTES_IN_BREAK_ROUND = 5;
    private static final long MILLISECONDS_IN_BREAK_ROUND = 1000 * 60 * MINUTES_IN_BREAK_ROUND;

    private int currentLevelIndex = 0;
    private List<Level> levels;
    private boolean running;
    private int remainingMinutes;
    private int remainingSeconds;

    private transient Timer timer;

    public ClockImpl() {
        init();
    }
    
    public Level getCurrentLevel() {
        return levels.get(currentLevelIndex);
    }
    
    public Level getNextLevel() {
        if (currentLevelIndex + 1 < levels.size()) {
            return levels.get(currentLevelIndex+1);
        } else {
            return levels.get(currentLevelIndex);
        }
    }
    
    public void goToNextLevel(String round) {
        reset();
        currentLevelIndex = getLevelIndexFromRound(round);
        // This last level is a dummy level not to be used, just shown
        if (currentLevelIndex + 2 < levels.size()) {
            ++currentLevelIndex;
            checkIfNotBreak();
        }
    }
    
    public void goToPreviousLevel(String round) {
        reset();
        currentLevelIndex = getLevelIndexFromRound(round);
        if (currentLevelIndex > 0) {
            --currentLevelIndex;
            checkIfNotBreak();
        }
    }
    
    public String getMaxRound() {
        return levels.get(levels.size() - 1).getRound();
    }
    
    public void start(String round, Integer minutes, Integer seconds) {
        reset();

        if (round == null) {
            round = "Round 1";
        }
        
        if (minutes == null) {
            minutes = 0;
        } else if (minutes > MINUTES_IN_ROUND) {
            minutes = MINUTES_IN_ROUND - 1;
        } else if (minutes == MINUTES_IN_ROUND) {
            seconds = 0;
        }
        
        if (seconds == null) {
            seconds = 0;
        } else if (seconds >= 60) {
            seconds = 0;
        }

        currentLevelIndex = getLevelIndexFromRound(round);
        
        long remainingMinutesAsMillis = minutes * 60 * 1000l;
        long remainingSecondsAsMillis = seconds * 1000l;
        long elapsedMillis = getCurrentLevel().getDuration() - remainingMinutesAsMillis - remainingSecondsAsMillis;

        Level level = getCurrentLevel();
        timer = new Timer(level.getDuration(), elapsedMillis, this);
        timer.start();
    }

    public void reset() {
        currentLevelIndex = 0;
        if (timer != null) {
            timer.end();
        }
        timer = null;
    }

    public void pause() {
        if (timer != null) {
            timer.setPaused(true);
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public int getRemainingMinutes() {
        return remainingMinutes;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void sync() {
        running = timer != null && !timer.isPaused();

        long elasped = 0l;
        if (timer != null) {
            elasped = timer.getElapsed();
        }

        long remaining = getCurrentLevel().getDuration() - elasped;
        if (remaining < 0) {
            remainingMinutes = 0;
            remainingSeconds = 0;
        } else {
            remainingMinutes = (int)(remaining / 60000f);
            
            long remainder = remaining % 60000l;
            remainingSeconds = (int)(remainder / 1000);
        }
    }

    // TimerListener 
    public void finished() {        
        goToNextLevel(getCurrentLevel().getRound());
        Level level = getCurrentLevel();
        start(level.getRound(), level.getDurationMinutes(), 0);
    }

    private void init() {
        levels = new ArrayList<Level>(26);
        Level level = new Level("Round 1", LevelType.NORMAL, 25, 50, 0, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 2", LevelType.NORMAL, 50, 100, 0, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 3", LevelType.NORMAL, 100, 200, 0, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 4", LevelType.NORMAL, 100, 200, 25, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 5", LevelType.NORMAL, 150, 300, 25, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 6", LevelType.NORMAL, 200, 400, 50, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 7", LevelType.NORMAL, 300, 600, 75, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Break 1", LevelType.BREAK, 0, 0, 0, MILLISECONDS_IN_BREAK_ROUND, this);
        levels.add(level);
        level = new Level("Round 8", LevelType.NORMAL, 400, 800, 100, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 9", LevelType.NORMAL, 600, 1200, 100, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 10", LevelType.NORMAL, 800, 1600, 200, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 11", LevelType.NORMAL, 1000, 2000, 300, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 12", LevelType.NORMAL, 1500, 3000, 400, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Break 2", LevelType.BREAK, 0, 0, 0, MILLISECONDS_IN_BREAK_ROUND, this);
        levels.add(level);
        level = new Level("Round 13", LevelType.NORMAL, 2000, 4000, 500, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 14", LevelType.NORMAL, 3000, 6000, 500, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 15", LevelType.NORMAL, 4000, 8000, 1000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 16", LevelType.NORMAL, 5000, 10000, 1000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Break 3", LevelType.BREAK, 0, 0, 0, MILLISECONDS_IN_BREAK_ROUND, this);
        levels.add(level);
        level = new Level("Round 17", LevelType.NORMAL, 6000, 12000, 1000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 18", LevelType.NORMAL, 8000, 16000, 2000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 19", LevelType.NORMAL, 10000, 20000, 3000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 20", LevelType.NORMAL, 12000, 24000, 3000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 21", LevelType.NORMAL, 15000, 30000, 4000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 22", LevelType.NORMAL, 20000, 40000, 5000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 23", LevelType.NORMAL, 25000, 50000, 5000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 24", LevelType.NORMAL, 30000, 60000, 5000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 25", LevelType.NORMAL, 40000, 80000, 10000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Round 26", LevelType.NORMAL, 50000, 100000, 10000, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
        level = new Level("Repeat Round 26", LevelType.NORMAL, 0, 0, 0, MILLISECONDS_IN_ROUND, this);
        levels.add(level);
    }

    private int getLevelIndexFromRound(String round) {
        for (int i = 0; i < levels.size(); ++i) {
            Level level = levels.get(i);
            if (level.getRound().equals(round)) {
                return i;
            }
        }
        return 0;
    }
    
    private void checkIfNotBreak() {
        Level level = getCurrentLevel();
        if (!level.getRound().startsWith("Break")) {
            Game game = gameService.findMostRecent();
            if (game != null && !game.isFinalized()) {
                mailService.sendBlindsUpMail(game, level.getRound(), 
                        level.getSmallBlind(), level.getBigBlind(), level.getAnte());
            }
        }

    }
}
