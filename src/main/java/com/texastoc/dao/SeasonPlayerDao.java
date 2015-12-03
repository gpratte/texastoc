package com.texastoc.dao;

import java.util.List;

import com.texastoc.domain.Player;
import com.texastoc.domain.SeasonPlayer;

public interface SeasonPlayerDao {

    List<SeasonPlayer> selectBySeasonId(int id);
    SeasonPlayer selectById(int id);
    int insert(SeasonPlayer seasonPlayer);
    void update(SeasonPlayer seasonPlayer);
    void delete(int id);
    void deleteAllBySeasonId(int seasonId);

}
