package com.texastoc.service;

import java.util.List;

import com.texastoc.domain.clock.Clock;
import com.texastoc.domain.clock.Level;

public interface ClockService {

    List<Level> getLevels();
    Clock getClock(int gameId);
    void endClock(int gameId);
}
