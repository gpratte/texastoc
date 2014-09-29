package com.texastoc.exception;

import java.util.List;

import com.texastoc.domain.GamePlayer;

public class CannotFinalizeException extends Exception {

    private List<GamePlayer> invalidPlayers;
    
    public CannotFinalizeException(List<GamePlayer> players, String message) {
        super(message);
        this.invalidPlayers = players;
    }
    
    public List<GamePlayer> getInvalidPlayers() {
        return invalidPlayers;
    }
}
