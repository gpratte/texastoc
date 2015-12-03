package com.texastoc.dao;

import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;

public interface GameAuditDao {

    void insert(Game game, GamePlayer gamePlayer);

}
