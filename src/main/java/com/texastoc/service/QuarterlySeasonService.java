package com.texastoc.service;

import java.util.List;

import com.texastoc.domain.QuarterlySeason;

public interface QuarterlySeasonService {
	
    List<QuarterlySeason> findBySeasonId(int seasonId);
    QuarterlySeason findById(int id);
    int create(QuarterlySeason quarterly) throws Exception;
    void update(QuarterlySeason quarterly) throws Exception;
}
