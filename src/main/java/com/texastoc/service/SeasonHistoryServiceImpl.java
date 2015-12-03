package com.texastoc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.SeasonHistoryEntryDao;
import com.texastoc.domain.SeasonHistoryEntry;

@Service
public class SeasonHistoryServiceImpl implements SeasonHistoryService {
    
    @Autowired
    SeasonHistoryEntryDao seasonHistoryEntryDao;
	
    @Override
    public SeasonHistoryEntry findById(int seasonId, int playerId) {
        return seasonHistoryEntryDao.selectById(seasonId, playerId);
    }
    
    @Override
    public void update(SeasonHistoryEntry entry) throws Exception {
        seasonHistoryEntryDao.update(entry);
    }   

}
