package com.texastoc.dao;

import java.util.List;

import com.texastoc.domain.Player;

public interface PlayerDao {

    int insert(Player player);
    Player selectById(int id);
    Player selectByName(String firstName, String lastName);
    Player selectByEmail(String email);
    List<Player> selectAll();
    List<Player> selectPossibleHosts();
    List<Player> selectPossibleTransporters();
    List<Player> selectPtcg();
    List<Player> selectActive();
    void update(Player player);
    void delete(int id);
    List<Player> selectBankersByGameId(int gameId);
    void insertGameBanker(int gameId, int playerId);
    void deleteGameBanker(int gameId, int playerId);
    void updatePassword(int id, String password);
}
