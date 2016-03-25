package com.texastoc.service;

import java.util.List;

import com.texastoc.domain.Player;

public interface PlayerService {
	
    int create(Player player) throws Exception;
    List<Player> findAll();
    List<Player> findPossibleHosts();
    List<Player> findPossibleTransporters();
    List<Player> findPtcg();
    List<Player> findTocBoard();
    List<Player> findActive();
    Player findById(int id);
    Player findByEmail(String email);
    void update(Player player) throws Exception;
    void delete(int id) throws Exception;
    List<Player> findBankersByGameId(int id);
    void addGameBanker(int gameId, int playerId);
    void removeGameBanker(int gameId, int playerId);
    void updateGameBanker(int gameId, Integer includePlayerId, Integer excludePlayerId);
    boolean isPasswordValid(String email, String password);
    void updatePassword(int id, String password);
}
