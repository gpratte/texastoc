package com.texastoc.service;

import java.util.List;

import org.joda.time.LocalDate;

import com.texastoc.domain.Game;
import com.texastoc.domain.GamePayout;
import com.texastoc.domain.Player;
import com.texastoc.exception.CannotRandomizeException;

public interface GameService {
	
    List<Game> findBySeasonId(int seasonId, boolean includePlayers);
    List<Game> findByDate(LocalDate startDate, LocalDate endDate, boolean includePlayers);
    List<Game> findAll();
    Game findById(int id);
    Game findMostRecent();
    int create(Game game) throws Exception;
    void update(Game game) throws Exception;
    void delete(int id) throws Exception;
    void finalize(int id) throws Exception;
    void randomizeSeats(int id, List<String> includePlayersWOBuyInIds, List<String> excludePlayerIds,
            Player firstPlayer, List<Integer> playersPerTable) throws CannotRandomizeException;
    void messageSeats(int id);
    void clearSeats(int id);
//    Game startNewGameOrGetGameInProgress() throws SQLException;
    void changePayout(int id, int change) throws Exception;
    List<GamePayout> findPayoutsByGameId(int gameId);
    void updateHomeGame(int gameId, int homeGameMarker);
    void seated(int gameId);
    void assignSeats(int gameId, List<String> playersWBuyInList, 
            List<String> playersWOBuyInList) throws CannotRandomizeException;
    void recordStartTime(int gameId);
    void updateTransport(int gameId, boolean flag);
    void updateNote(int gameId, String note);

}
