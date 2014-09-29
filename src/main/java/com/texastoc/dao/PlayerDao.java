package com.texastoc.dao;

import java.sql.SQLException;
import java.util.List;

import com.texastoc.domain.Player;

public interface PlayerDao {

    int insert(Player player) throws SQLException;
    Player selectById(int id);
    Player selectByName(String firstName, String lastName);
    List<Player> selectAll();
    void update(Player player) throws SQLException;
    void delete(int id) throws SQLException;

}
