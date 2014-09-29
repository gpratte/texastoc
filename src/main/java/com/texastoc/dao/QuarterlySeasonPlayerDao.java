package com.texastoc.dao;

import java.sql.SQLException;
import java.util.List;

import com.texastoc.domain.QuarterlySeasonPlayer;

public interface QuarterlySeasonPlayerDao {

    List<QuarterlySeasonPlayer> selectByQuarterlySeasonId(int id);
    int insert(QuarterlySeasonPlayer qSeasonPlayer) throws SQLException;
    void deleteAllByQuarterlySeasonId(int id) throws SQLException;

}
