package com.texastoc.dao;

import java.util.List;

import org.joda.time.LocalDate;

import com.texastoc.domain.Game;

public interface GameDao {

    List<Game> selectAll();
    List<Game> selectBySeasonId(int seasonId, boolean includePlayers);
    List<Game> selectByDate(LocalDate startDate, LocalDate endDate, boolean includePlayers);
    Game selectById(int id);
    Game selectMostRecent();
    int insert(Game game);
    void update(Game game);
    void delete(int id);
    void changePayout(int id, int change);
    void updateHomeGame(int gameId, int homeGameMarker, int kitty);
    void seated(int gameId);
    void recordStartTime(int gameId);
    void updateTransport(int gameId, boolean flag);
    void updateNote(int id, String note);
}
