package com.texastoc.dao;

import java.sql.SQLException;
import java.util.List;

import com.texastoc.domain.GamePlayer;

public interface GamePlayerDao {

    List<GamePlayer> selectByGameId(int gameId);
    GamePlayer selectById(int id);
    void update(GamePlayer gamePlayer) throws SQLException;
    int insert(GamePlayer gamePlayer) throws SQLException;
    void delete(int gamePlayerId) throws SQLException;

}
