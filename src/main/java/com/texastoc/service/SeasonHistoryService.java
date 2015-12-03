package com.texastoc.service;

import com.texastoc.domain.SeasonHistoryEntry;

public interface SeasonHistoryService {
	
    SeasonHistoryEntry findById(int seasonId, int playerId);
    void update(SeasonHistoryEntry entry) throws Exception;

}
