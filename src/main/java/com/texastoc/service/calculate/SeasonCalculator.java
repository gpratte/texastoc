package com.texastoc.service.calculate;

import com.texastoc.domain.Season;


public interface SeasonCalculator {

    void calculate(int id) throws Exception;
    Season calcluateUpToGame(int gameId);
}
