package com.texastoc.dao;

import java.util.List;

import com.texastoc.domain.GamePayout;

public interface GamePayoutDao {

    List<GamePayout> selectByGameId(int gameId);
    void insert(GamePayout gamePayout);
    void update(GamePayout gamePayout);
    void delete(int gameId, int place);
    void deleteByGameId(int gamePayoutId);

}
