package com.texastoc.dao;

import java.util.List;

import com.texastoc.domain.SeasonHistoryEntry;

public interface SeasonHistoryEntryDao {

    List<SeasonHistoryEntry> selectAllBySeasonId(int seasonId);
    SeasonHistoryEntry selectById(int seasonId, int playerId);
    void update(SeasonHistoryEntry entry);
}
