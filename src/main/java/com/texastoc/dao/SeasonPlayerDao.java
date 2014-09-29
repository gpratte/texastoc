package com.texastoc.dao;

import java.sql.SQLException;
import java.util.List;

import com.texastoc.domain.SeasonPlayer;

public interface SeasonPlayerDao {

    List<SeasonPlayer> selectBySeasonId(int id);
    int insert(SeasonPlayer seasonPlayer) throws SQLException;
    void deleteAllBySeasonId(int seasonId) throws SQLException;

}
