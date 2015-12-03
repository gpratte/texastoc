package com.texastoc.service;

import com.texastoc.domain.SeasonPlayer;

public interface SeasonPlayerService {
	
    SeasonPlayer findById(int id);
    void update(SeasonPlayer player) throws Exception;

}
