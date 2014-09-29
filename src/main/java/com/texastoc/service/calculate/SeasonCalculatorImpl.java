package com.texastoc.service.calculate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.GameDao;
import com.texastoc.dao.PlayerDao;
import com.texastoc.dao.SeasonDao;
import com.texastoc.dao.SeasonPlayerDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Season;
import com.texastoc.domain.SeasonPlayer;
import com.texastoc.exception.CannotCalculateException;

@Service
public class SeasonCalculatorImpl implements SeasonCalculator {

    @Autowired
    SeasonDao seasonDao;
    @Autowired
    SeasonPlayerDao seasonPlayerDao;
    @Autowired
    GameDao gameDao;
    @Autowired
    PlayerDao playerDao;

    @Override
    public void calculate(int id) throws Exception {
        Season season = seasonDao.selectById(id);
        
        if (season.isUseHistoricalData()) {
            throw new CannotCalculateException("Season is using historical data");
        }
        
        if (season.isFinalized()) {
            throw new CannotCalculateException("Season is finalized");
        }

        // Remove current season players
        seasonPlayerDao.deleteAllBySeasonId(id);

        ArrayList<SeasonPlayer> seasonPlayers = calculate(season, null, null);

        seasonDao.update(season);

        for (SeasonPlayer player : seasonPlayers) {
            seasonPlayerDao.insert(player);
        }
    }

    @Override
    public Season calcluateUpToGame(int gameId) {
        Game upToGame = gameDao.selectById(gameId);
        Season season = seasonDao.selectById(upToGame.getSeasonId());
        Season upToSeason = new Season();
        upToSeason.setEndDate(season.getEndDate());
        upToSeason.setFinalized(season.isFinalized());
        upToSeason.setId(season.getId());
        upToSeason.setLastCalculated(season.getLastCalculated());
        upToSeason.setNote(season.getNote());
        upToSeason.setStartDate(season.getStartDate());
        upToSeason.setUseHistoricalData(season.isUseHistoricalData());

        ArrayList<SeasonPlayer> seasonPlayers = calculate(season, upToSeason, upToGame);
        for (SeasonPlayer seasonPlayer : seasonPlayers) {
            seasonPlayer.setPlayer(playerDao.selectById(seasonPlayer.getPlayerId()));
        }
        
        upToSeason.setSeasonPlayers(seasonPlayers);
        
        return upToSeason;
    }

    private ArrayList<SeasonPlayer> calculate(Season season, Season upToSeason, Game upToGame) {
        
        int totalBuyIn = 0;
        int totalReBuy = 0;
        int totalAnnualToc = 0;
        
        HashMap<Integer, SeasonPlayer> playerMap = new HashMap<Integer, SeasonPlayer>();
        for (Game game : season.getGames()) {
            
            if (! game.isFinalized()) {
                continue;
            }
            
            if (upToGame != null && game.getGameDate().isAfter(upToGame.getGameDate())) {
                continue;
            }
            
            totalBuyIn += game.getTotalBuyIn();
            totalReBuy += game.getTotalReBuy();
            totalAnnualToc += game.getTotalAnnualToc();
            
            for (GamePlayer gp : game.getPlayers()) {
                if (!gp.isAnnualTocPlayer()) {
                    continue;
                }

                SeasonPlayer seasonPlayer = playerMap.get(gp.getPlayerId());
                if (seasonPlayer == null) {
                    seasonPlayer = new SeasonPlayer();
                    seasonPlayer.setSeasonId(season.getId());
                    seasonPlayer.setPlayerId(gp.getPlayerId());
                    playerMap.put(gp.getPlayerId(), seasonPlayer);
                }
                
                seasonPlayer.setNumEntries(seasonPlayer.getNumEntries() + 1);
                
                if (gp.getPoints() != null) {
                    int points = seasonPlayer.getPoints() + gp.getPoints();
                    seasonPlayer.setPoints(points);
                }
            }
            
        }

        if (upToSeason != null) {
            upToSeason.setTotalAnnualToc(totalAnnualToc);
            upToSeason.setTotalBuyIn(totalBuyIn);
            upToSeason.setTotalReBuy(totalReBuy);
        } else {
            season.setTotalAnnualToc(totalAnnualToc);
            season.setTotalBuyIn(totalBuyIn);
            season.setTotalReBuy(totalReBuy);
        }
        
        ArrayList<SeasonPlayer> seasonPlayers = 
                new ArrayList<SeasonPlayer>(playerMap.values());
        
        Collections.sort(seasonPlayers);
        
        int count = 1;
        for (SeasonPlayer player : seasonPlayers) {
            if (player.getPoints() > 0) {
                player.setPlace(count++);
            }
        }
        
        return seasonPlayers;
    }

}
