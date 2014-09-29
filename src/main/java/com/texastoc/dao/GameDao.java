package com.texastoc.dao;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;

import com.texastoc.domain.Game;

public interface GameDao {

    List<Game> selectBySeasonId(int seasonId, boolean includePlayers);
    List<Game> selectByDate(LocalDate startDate, LocalDate endDate, boolean includePlayers);
    Game selectById(int id);
    Game selectMostRecent();
    int insert(Game game) throws SQLException;
    void update(Game game) throws SQLException;
    void changePayout(int id, int change) throws SQLException;
}
