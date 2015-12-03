package com.texastoc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import com.texastoc.dao.SeasonPlayerDao;
import com.texastoc.domain.SeasonPlayer;

@Service
@EnableCaching 
public class SeasonPlayerServiceImpl implements SeasonPlayerService {
    
    @Autowired
    SeasonPlayerDao seasonPlayerDao;
	
    @Override
    public SeasonPlayer findById(int id) {
        return seasonPlayerDao.selectById(id);
    }
    
    @Override
    @CacheEvict(value="seasoncache", allEntries=true)
    public void update(SeasonPlayer player) throws Exception {
        seasonPlayerDao.update(player);
    }   

}
