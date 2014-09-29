package com.texastoc.dao;

import java.sql.SQLException;
import java.util.List;

import com.texastoc.domain.GamePayout;

public interface GamePayoutDao {

    List<GamePayout> selectByGameId(int id);
    void insert(GamePayout gamePayout) throws SQLException;
    void deleteAllByGameId(int id) throws SQLException;

}
