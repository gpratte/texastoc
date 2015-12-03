package com.texastoc.service;

import java.util.List;
import java.util.Set;

import com.texastoc.domain.Game;
import com.texastoc.domain.PlayerCount;
import com.texastoc.domain.Season;
import com.texastoc.domain.SeasonChampion;
import com.texastoc.domain.SeasonPayout;
import com.texastoc.domain.SeasonTopTen;

public interface SeasonService {
	
	List<Season> findAll();
	List<SeasonChampion> findAllChampions();
    Season findById(int id);
    Season getCurrent();
    Season getUpToGame(int gameId) throws Exception;
    int create(Season season) throws Exception;
    void update(Season season) throws Exception;
    List<PlayerCount> getHosts(Game game, List<Game> games);
    List<PlayerCount> getBankers(Game game, List<Game> games);
    SeasonTopTen getTopTen(Game game, Season season);
    void updatePayouts(int seasonId, List<SeasonPayout> payouts, 
            Set<Integer> deleteIds);
    void emailSeasonSummary(int gameId, int playerId);
}
