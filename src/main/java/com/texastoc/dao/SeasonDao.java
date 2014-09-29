package com.texastoc.dao;

import java.sql.SQLException;
import java.util.List;

import com.texastoc.domain.Season;

public interface SeasonDao {

    List<Season> selectAll();
    Season selectById(int id);
    int insert(Season season) throws SQLException;
    void update(Season season) throws SQLException;
}
