package com.texastoc.dao;

import java.sql.SQLException;
import java.util.List;

import com.texastoc.domain.QuarterlySeason;

public interface QuarterlySeasonDao {

    List<QuarterlySeason> selectBySeasonId(int seasonId);
    QuarterlySeason selectById(int id);
    int insert(QuarterlySeason quarterly) throws SQLException;
    void update(QuarterlySeason quarterly) throws SQLException;
}
