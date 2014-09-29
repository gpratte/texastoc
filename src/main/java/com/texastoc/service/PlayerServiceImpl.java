package com.texastoc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.PlayerDao;
import com.texastoc.domain.Player;

@Service
public class PlayerServiceImpl implements PlayerService {
    
    @Autowired
    PlayerDao playerDao;
	
    @Override
    public int create(Player player) throws Exception {
        return playerDao.insert(player);
    }   

    @Override
    public List<Player> findAll() {
        return playerDao.selectAll();
    }
    
    @Override
    public Player findById(int id) {
        return playerDao.selectById(id);
    }
    
    @Override
    public void update(Player player) throws Exception {
        playerDao.update(player);
    }   

    @Override
    public void delete(int id) throws Exception {
        playerDao.delete(id);
    }   
}
