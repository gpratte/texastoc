package com.texastoc.service.calculate;

import com.texastoc.domain.QuarterlySeason;

public interface QuarterlySeasonCalculator {

    void calculateByGameId(int gameId) throws Exception;
    QuarterlySeason calculateUpToGame(int gameId) throws Exception;
}
