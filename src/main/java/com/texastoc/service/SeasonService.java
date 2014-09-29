package com.texastoc.service;

import java.util.List;

import com.texastoc.domain.Season;

public interface SeasonService {
	
	List<Season> findAll();
    Season findById(int id);
    Season getUpToGame(int gameId) throws Exception;
    int create(Season season) throws Exception;
    void update(Season season) throws Exception;

}
