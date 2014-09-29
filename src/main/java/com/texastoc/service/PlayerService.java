package com.texastoc.service;

import java.util.List;

import com.texastoc.domain.Player;

public interface PlayerService {
	
    int create(Player player) throws Exception;
    List<Player> findAll();
    Player findById(int id);
    void update(Player player) throws Exception;
    void delete(int id) throws Exception;

}
