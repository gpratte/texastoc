package com.texastoc.dao;

import java.util.List;

import com.texastoc.domain.QuarterlySeason;

public interface QuarterlySeasonDao {

    List<QuarterlySeason> selectBySeasonId(int seasonId);
    QuarterlySeason selectById(int id);
    int insert(QuarterlySeason quarterly);
    void update(QuarterlySeason quarterly);
}
