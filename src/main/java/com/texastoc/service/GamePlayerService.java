package com.texastoc.service;

import com.texastoc.domain.GamePlayer;

public interface GamePlayerService {
	
    int create(GamePlayer gamePlayer) throws Exception;
    GamePlayer findById(int id);
    void update(GamePlayer gamePlayer) throws Exception;
    void delete(int id) throws Exception;

}
