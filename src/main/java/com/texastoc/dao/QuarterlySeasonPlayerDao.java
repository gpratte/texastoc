package com.texastoc.dao;

import java.util.List;

import com.texastoc.domain.QuarterlySeasonPlayer;

public interface QuarterlySeasonPlayerDao {

    List<QuarterlySeasonPlayer> selectByQuarterlySeasonId(int id);
    int insert(QuarterlySeasonPlayer qSeasonPlayer);
    void update(QuarterlySeasonPlayer qSeasonPlayer);
    void delete(int id);
    void deleteAllByQuarterlySeasonId(int id);

}
