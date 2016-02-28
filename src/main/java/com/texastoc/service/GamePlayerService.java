package com.texastoc.service;

import com.texastoc.domain.GamePlayer;

public interface GamePlayerService {
	
    int create(GamePlayer gamePlayer) throws Exception;
    GamePlayer findById(int id);
    GamePlayer findByPlayerId(int playerId, int gameId);
    void update(GamePlayer gamePlayer, String updator) throws Exception;
    void delete(int id) throws Exception;

}
