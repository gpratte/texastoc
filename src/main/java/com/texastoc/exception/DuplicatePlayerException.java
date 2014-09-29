package com.texastoc.exception;

import com.texastoc.domain.GamePlayer;

public class DuplicatePlayerException extends Exception {

    private GamePlayer player;
    
    public DuplicatePlayerException(GamePlayer player, String message) {
        super(message);
        this.player = player;
    }
    
    public GamePlayer getGamePlayer() {
        return player;
    }
}
