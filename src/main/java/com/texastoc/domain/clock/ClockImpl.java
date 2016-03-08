package com.texastoc.domain.clock;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import com.texastoc.common.HomeGame;
import com.texastoc.domain.Game;
import com.texastoc.service.GameService;
import com.texastoc.service.mail.MailService;

/**
 * POJO
 */
public class ClockImpl implements Clock, TimerListener {
    
    private MailService mailService;
    private GameService gameService;
    
    static final Logger logger = Logger.getLogger(ClockImpl.class);

    public static final int MINUTES_IN_ROUND = 20;
    private static final long MILLISECONDS_IN_ROUND = 1000 * 60 * MINUTES_IN_ROUND;

    public static final int MINUTES_IN_BREAK_ROUND = 5;
    private static final long MILLISECONDS_IN_BREAK_ROUND = 1000 * 60 * MINUTES_IN_BREAK_ROUND;

    private int currentLevelIndex = 0;
    private List<Level> levels;
    private List<Level> tocLevels;
    private List<Level> cpplLevels;

    private Timer timer;

    public ClockImpl(GameService gameService, MailService mailService) {
    	this.gameService = gameService;
    	this.mailService = mailService;
        init();
    }
    
    public List<Level> getLevels() {
    	return levels;
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
    
    public void goToNextLevel() {
        // This last level is a dummy level not to be used, just shown
        if (currentLevelIndex + 2 < levels.size()) {
            ++currentLevelIndex;
        }
        if (timer != null) {
            timer.end();
            timer = null;
        }
        sendLevelChange();
    }
    
    public void goToPreviousLevel() {
        if (currentLevelIndex > 0) {
            --currentLevelIndex;
        }
        if (timer != null) {
            timer.end();
            timer = null;
        }
        sendLevelChange();
    }
    
	public void setRound(String round) {
        if (round == null) {
            round = "Round 1";
        }
        currentLevelIndex = getLevelIndexFromRound(round);
        if (timer != null) {
            timer.end();
            timer = null;
        }
        sendLevelChange();
	}

	public String getMaxRound() {
        return levels.get(levels.size() - 1).getRound();
    }
    
    public void updateMinute(int value) {
        if (timer == null) {
            timer = new Timer(getCurrentLevel().getDuration(), this);
            timer.setPaused(true);
            timer.start();
        }
        int currentMinutes = getRemainingMinutes();
        if (value < 0) {
            if (currentMinutes > 0) {
                currentMinutes += value;
                if (currentMinutes < 0) {
                    currentMinutes = 0;
                }
                int currentSeconds = getRemainingSeconds();
                long remainingTime = (long)((currentMinutes * 60 * 1000) + (currentSeconds * 1000));
                timer.setRemaining(remainingTime);
            }
        } else {
            if (currentMinutes < getCurrentLevel().getDurationMinutes()) {
                currentMinutes += value;
                if (currentMinutes > getCurrentLevel().getDurationMinutes()) {
                    currentMinutes = getCurrentLevel().getDurationMinutes();
                }
                int currentSeconds = getRemainingSeconds();
                if (currentMinutes == getCurrentLevel().getDurationMinutes()) {
                    currentSeconds = 0;
                }
                long remainingTime = (long)((currentMinutes * 60 * 1000) + (currentSeconds * 1000));
                timer.setRemaining(remainingTime);
            }
        }
    }
    
    public void updateSecond(int value) {
        if (timer == null) {
            timer = new Timer(getCurrentLevel().getDuration(), this);
            timer.setPaused(true);
            timer.start();
        }
        int currentSeconds = getRemainingSeconds();
        int currentMinutes = getRemainingMinutes();
        if (value < 0) {
            if (currentSeconds > 0) {
                currentSeconds += value;
                if (currentSeconds < 0) {
                    currentSeconds = 0;
                }
                long remainingTime = (long)((currentMinutes * 60 * 1000) + (currentSeconds * 1000));
                timer.setRemaining(remainingTime);
            }
        } else {
            if (currentSeconds < 59 && currentMinutes < getCurrentLevel().getDurationMinutes()) {
                currentSeconds += value;
                if (currentSeconds > 59) {
                    currentSeconds = 59;
                }
                long remainingTime = (long)((currentMinutes * 60 * 1000) + (currentSeconds * 1000));
                timer.setRemaining(remainingTime);
            }
        }
    }
    
	@Override
	public void setHomeGame(HomeGame homeGame) {
		if (homeGame == HomeGame.TOC) {
			levels = tocLevels;
		} else if (homeGame == HomeGame.CPPL) {
			levels = cpplLevels;
		}
	}

	public void go() {
	    if (timer == null) {
	        timer = new Timer(getCurrentLevel().getDuration(), this);
	        timer.start();
	    } else {
	        timer.setPaused(false);
	    }
    }

    public void stop() {
        if (timer != null) {
            timer.setPaused(true);
        }
    }
    
    public boolean isRunning() {
        if (timer == null) {
            return false;
        } else {
            return timer != null && !timer.isPaused();
        }
    }
    
    public int getRemainingMinutes() {
        long millis = 0;
        if (timer != null) {
            millis = timer.getRemaining();
        } else {
            millis = getCurrentLevel().getDuration();
        }
        return (int) ((millis / (1000*60)) % 60);
    }

    public int getRemainingSeconds() {
        long millis = 0;
        if (timer != null) {
            millis = timer.getRemaining();
        } else {
            millis = getCurrentLevel().getDuration();
        }
        return (int) ((millis / 1000) % 60);
    }

    public void reset() {
        currentLevelIndex = 0;
        if (timer != null) {
            timer.end();
        }
        timer = null;
    }

    // TimerListener 
    public void finished() {
        goToNextLevel();
        timer = new Timer(getCurrentLevel().getDuration(), this);
        timer.start();
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private void init() {
        currentLevelIndex = 0;

        tocLevels = new ArrayList<Level>(26);
        levels = tocLevels;
        Level level = new Level("Round 1", LevelType.NORMAL, 25, 50, 0, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 2", LevelType.NORMAL, 50, 100, 0, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 3", LevelType.NORMAL, 100, 200, 0, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 4", LevelType.NORMAL, 100, 200, 25, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 5", LevelType.NORMAL, 150, 300, 25, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 6", LevelType.NORMAL, 200, 400, 50, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 7", LevelType.NORMAL, 300, 600, 75, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Break 1", LevelType.BREAK, 0, 0, 0, MILLISECONDS_IN_BREAK_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 8", LevelType.NORMAL, 400, 800, 100, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 9", LevelType.NORMAL, 600, 1200, 100, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 10", LevelType.NORMAL, 800, 1600, 200, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 11", LevelType.NORMAL, 1000, 2000, 300, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 12", LevelType.NORMAL, 1500, 3000, 400, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Break 2", LevelType.BREAK, 0, 0, 0, MILLISECONDS_IN_BREAK_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 13", LevelType.NORMAL, 2000, 4000, 500, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 14", LevelType.NORMAL, 3000, 6000, 500, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 15", LevelType.NORMAL, 4000, 8000, 1000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 16", LevelType.NORMAL, 5000, 10000, 1000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Break 3", LevelType.BREAK, 0, 0, 0, MILLISECONDS_IN_BREAK_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 17", LevelType.NORMAL, 6000, 12000, 1000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 18", LevelType.NORMAL, 8000, 16000, 2000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 19", LevelType.NORMAL, 10000, 20000, 3000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 20", LevelType.NORMAL, 12000, 24000, 3000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 21", LevelType.NORMAL, 15000, 30000, 4000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 22", LevelType.NORMAL, 20000, 40000, 5000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 23", LevelType.NORMAL, 25000, 50000, 5000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 24", LevelType.NORMAL, 30000, 60000, 5000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 25", LevelType.NORMAL, 40000, 80000, 10000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Round 26", LevelType.NORMAL, 50000, 100000, 10000, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);
        level = new Level("Repeat Round 26", LevelType.NORMAL, 0, 0, 0, MILLISECONDS_IN_ROUND, this);
        tocLevels.add(level);

        cpplLevels = new ArrayList<Level>(26);
        level = new Level("Round 1", LevelType.NORMAL, 25, 50, 0, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 2", LevelType.NORMAL, 50, 100, 0, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 3", LevelType.NORMAL, 75, 150, 0, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 4", LevelType.NORMAL, 100, 200, 0, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 5", LevelType.NORMAL, 150, 300, 0, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 6", LevelType.NORMAL, 200, 400, 0, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 7", LevelType.NORMAL, 300, 600, 0, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Break 1", LevelType.BREAK, 0, 0, 0, MILLISECONDS_IN_BREAK_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 8", LevelType.NORMAL, 400, 800, 100, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 9", LevelType.NORMAL, 600, 1200, 100, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 10", LevelType.NORMAL, 800, 1600, 200, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 11", LevelType.NORMAL, 1000, 2000, 300, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 12", LevelType.NORMAL, 1500, 3000, 400, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Break 2", LevelType.BREAK, 0, 0, 0, MILLISECONDS_IN_BREAK_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 13", LevelType.NORMAL, 2000, 4000, 500, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 14", LevelType.NORMAL, 3000, 6000, 500, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 15", LevelType.NORMAL, 4000, 8000, 1000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 16", LevelType.NORMAL, 5000, 10000, 1000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Break 3", LevelType.BREAK, 0, 0, 0, MILLISECONDS_IN_BREAK_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 17", LevelType.NORMAL, 6000, 12000, 1000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 18", LevelType.NORMAL, 8000, 16000, 2000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 19", LevelType.NORMAL, 10000, 20000, 3000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 20", LevelType.NORMAL, 12000, 24000, 3000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 21", LevelType.NORMAL, 15000, 30000, 4000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 22", LevelType.NORMAL, 20000, 40000, 5000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 23", LevelType.NORMAL, 25000, 50000, 5000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 24", LevelType.NORMAL, 30000, 60000, 5000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 25", LevelType.NORMAL, 40000, 80000, 10000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Round 26", LevelType.NORMAL, 50000, 100000, 10000, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        level = new Level("Repeat Round 26", LevelType.NORMAL, 0, 0, 0, MILLISECONDS_IN_ROUND, this);
        cpplLevels.add(level);
        
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
    
    private void sendLevelChange() {
        Level level = getCurrentLevel();
        Game game = gameService.findMostRecent();
        if (game != null && !game.isFinalized()) {
            mailService.sendBlindsUpMail(game, level.getRound(), 
                    level.getSmallBlind(), level.getBigBlind(), level.getAnte());
        }
    }

}
