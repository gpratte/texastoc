package com.texastoc.service.integration;

import com.texastoc.domain.Game;

public interface GameChangedGateway {

    void notifyGameChanged(Game game);
}
