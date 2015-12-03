package com.texastoc.service.calculate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.GameDao;
import com.texastoc.dao.PlayerDao;
import com.texastoc.dao.QuarterlySeasonDao;
import com.texastoc.dao.QuarterlySeasonPlayerDao;
import com.texastoc.dao.SeasonDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.QuarterlyPayout;
import com.texastoc.domain.QuarterlySeason;
import com.texastoc.domain.QuarterlySeasonPlayer;
import com.texastoc.domain.Season;
import com.texastoc.exception.CannotCalculateException;

@Service
public class QuarterlySeasonCalculatorImpl implements QuarterlySeasonCalculator {

    @Autowired
    GameDao gameDao;
    @Autowired
    SeasonDao seasonDao;
    @Autowired
    PlayerDao playerDao;
    @Autowired
    QuarterlySeasonDao qSeasonDao;
    @Autowired
    QuarterlySeasonPlayerDao qSeasonPlayerDao;

    @Override
    public void calculateByGameId(int gameId) throws Exception {
        Game pastime = gameDao.selectById(gameId);
        Season season = seasonDao.selectById(pastime.getSeasonId());
        
        if (season.isUseHistoricalData()) {
            throw new CannotCalculateException("Season is using historical data");
        }
        
        if (season.isFinalized()) {
            throw new CannotCalculateException("Season is finalized");
        }
        
        // Find the quarterly season the game is in
        QuarterlySeason qSeason = null;
        found:
        for (QuarterlySeason qs : season.getQuarterlies()) {
            for (Game qGame : qs.getGames()) {
                if (pastime.getId() == qGame.getId()) {
                    qSeason = qs;
                    break found;
                }
            }
        }
        
        if (qSeason == null) {
            return;
        }

        ArrayList<QuarterlySeasonPlayer> qSeasonPlayers = calculate(qSeason, null, null);
        
        qSeasonDao.update(qSeason);

        List<QuarterlySeasonPlayer> currentQSPlayers = qSeasonPlayerDao.selectByQuarterlySeasonId(qSeason.getId());
        
        // Add or update existing
        for (QuarterlySeasonPlayer player : qSeasonPlayers) {
            boolean found = false;
            for (QuarterlySeasonPlayer currentPlayer : currentQSPlayers) {
                if (player.getPlayerId() == currentPlayer.getPlayerId()) {
                    // update
                    qSeasonPlayerDao.update(player);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                // add
                qSeasonPlayerDao.insert(player);
            }
        }
        
        // Remove
        for (QuarterlySeasonPlayer currentPlayer : currentQSPlayers) {
            boolean found = false;
            for (QuarterlySeasonPlayer player : qSeasonPlayers) {
                if (player.getPlayerId() == currentPlayer.getPlayerId()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                // remove
                qSeasonPlayerDao.delete(currentPlayer.getId());
            }
        }

        calculatePayouts(qSeason);
    }


    @Override
    public QuarterlySeason calculateUpToGame(int gameId) throws Exception {
        Game upToGame = gameDao.selectById(gameId);
        Season season = seasonDao.selectById(upToGame.getSeasonId());

        // Find the quarterly season the game is in
        QuarterlySeason qSeason = null;
        found:
        for (QuarterlySeason qs : season.getQuarterlies()) {
            for (Game qGame : qs.getGames()) {
                if (upToGame.getId() == qGame.getId()) {
                    qSeason = qs;
                    break found;
                }
            }
        }

        if (qSeason == null) {
            return null;
        }

        QuarterlySeason upToQSeason = new QuarterlySeason();
        upToQSeason.setEndDate(qSeason.getEndDate());
        upToQSeason.setFinalized(qSeason.isFinalized());
        upToQSeason.setId(qSeason.getId());
        upToQSeason.setLastCalculated(qSeason.getLastCalculated());
        upToQSeason.setNote(qSeason.getNote());
        upToQSeason.setQuarter(qSeason.getQuarter());
        upToQSeason.setSeasonId(qSeason.getSeasonId());
        upToQSeason.setStartDate(qSeason.getStartDate());
        
        ArrayList<QuarterlySeasonPlayer> qSeasonPlayers = calculate(qSeason, upToQSeason, upToGame);
        for (QuarterlySeasonPlayer qSeasonPlayer : qSeasonPlayers) {
            qSeasonPlayer.setPlayer(playerDao.selectById(qSeasonPlayer.getPlayerId()));
        }
        upToQSeason.setQuarterlySeasonPlayers(qSeasonPlayers);
        calculatePayouts(upToQSeason);
        
        return upToQSeason;
    }


    private ArrayList<QuarterlySeasonPlayer> calculate(QuarterlySeason qSeason, 
            QuarterlySeason upToQSeason, Game upToGame) {
        
        int totalQuarterlyToc = 0;
        
        HashMap<Integer, QuarterlySeasonPlayer> playerMap = 
                new HashMap<Integer, QuarterlySeasonPlayer>();
        for (Game game : qSeason.getGames()) {
            
            if (! game.isFinalized()) {
                continue;
            }
            
            if (upToGame != null && game.getGameDate().isAfter(upToGame.getGameDate())) {
                continue;
            }
            
            totalQuarterlyToc += game.getTotalQuarterlyToc();
            
            for (GamePlayer gp : game.getPlayers()) {
                if (!gp.isQuarterlyTocPlayer()) {
                    continue;
                }
                
                QuarterlySeasonPlayer qSeasonPlayer = playerMap.get(gp.getPlayerId());
                if (qSeasonPlayer == null) {
                    qSeasonPlayer = new QuarterlySeasonPlayer();
                    qSeasonPlayer.setQuarterlySeasonId(qSeason.getId());
                    qSeasonPlayer.setPlayerId(gp.getPlayerId());
                    playerMap.put(gp.getPlayerId(), qSeasonPlayer);
                }
                
                qSeasonPlayer.setNumEntries(qSeasonPlayer.getNumEntries() + 1);
                
                if (gp.getPoints() != null) {
                    int points = qSeasonPlayer.getPoints() + gp.getPoints();
                    qSeasonPlayer.setPoints(points);
                }
            }
            
        }

        if (upToQSeason != null) {
            upToQSeason.setTotalQuarterlyToc(totalQuarterlyToc);
        } else {
            qSeason.setTotalQuarterlyToc(totalQuarterlyToc);
        }
        
        ArrayList<QuarterlySeasonPlayer> qSeasonPlayers = 
                new ArrayList<QuarterlySeasonPlayer>(playerMap.values());
        
        Collections.sort(qSeasonPlayers);
        
        int count = 1;
        for (QuarterlySeasonPlayer player : qSeasonPlayers) {
            if (player.getPoints() > 0) {
                player.setPlace(count++);
            }
        }
        
        return qSeasonPlayers;
    }
    
    private void calculatePayouts(QuarterlySeason qSeason) {
        long firstPlace = Math.round(qSeason.getTotalQuarterlyToc() * 0.65d);

        QuarterlyPayout first = new QuarterlyPayout();
        first.setQuarterId(qSeason.getId());
        first.setPlace(1);
        first.setAmount((int)firstPlace);
        
        QuarterlyPayout second = new QuarterlyPayout();
        second.setQuarterId(qSeason.getId());
        second.setPlace(2);
        second.setAmount(qSeason.getTotalQuarterlyToc() - first.getAmount());
        
        qSeason.getPayouts().clear();
        qSeason.getPayouts().add(first);
        qSeason.getPayouts().add(second);
    }
}
