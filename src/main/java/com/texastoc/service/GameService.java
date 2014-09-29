package com.texastoc.service;

import java.util.List;

import org.joda.time.LocalDate;

import com.texastoc.domain.Game;
import com.texastoc.domain.Player;
import com.texastoc.exception.CannotRandomizeException;

public interface GameService {
	
    List<Game> findBySeasonId(int seasonId, boolean includePlayers);
    List<Game> findByDate(LocalDate startDate, LocalDate endDate, boolean includePlayers);
    Game findById(int id);
    Game findMostRecent();
    int create(Game game) throws Exception;
    void update(Game game) throws Exception;
    void randomizeSeats(int id, int numTables, int numPlayersPerTable, 
            List<String> includePlayersWOBuyInIds, List<String> excludePlayerIds,
            Player firstPlayer) throws CannotRandomizeException;
    void messageSeats(int id);
    void clearSeats(int id);
//    Game startNewGameOrGetGameInProgress() throws SQLException;
    void changePayout(int id, int change) throws Exception;
}
