package com.texastoc.dao;

import java.util.List;

import com.texastoc.domain.GamePlayer;

public interface GamePlayerDao {

    List<GamePlayer> selectByGameId(int gameId);
    GamePlayer selectById(int id);
    GamePlayer selectByPlayerId(int playerId, int gameId);
    void update(GamePlayer gamePlayer);
    int insert(GamePlayer gamePlayer);
    void delete(int gamePlayerId);
    void deleteByGame(int gameId);

}
