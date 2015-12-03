package com.texastoc.dao;

import java.util.List;

import com.texastoc.domain.Season;
import com.texastoc.domain.SeasonPayout;

public interface SeasonDao {

    List<Season> selectAll();
    Season selectById(int id);
    int insert(Season season);
    void update(Season season);
    List<SeasonPayout> selectPayoutsBySeasonId(int seasonId);
    void insertPayout(SeasonPayout payout);
    void updatePayout(SeasonPayout payout);
    void deletePayout(int id);
    void deleteTempPayoutsBySeasonId(int seasonId);
}
