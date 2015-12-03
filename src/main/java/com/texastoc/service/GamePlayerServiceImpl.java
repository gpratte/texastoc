package com.texastoc.service;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.GameDao;
import com.texastoc.dao.GamePlayerDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;
import com.texastoc.exception.CannotAddGamePlayerException;
import com.texastoc.exception.CannotSetFinishException;
import com.texastoc.exception.DuplicatePlayerException;
import com.texastoc.service.calculate.GameCalculator;
import com.texastoc.service.calculate.PayoutCalculator;
import com.texastoc.service.integration.GameChangedGateway;
import com.texastoc.service.mail.MailService;

@Service
public class GamePlayerServiceImpl implements GamePlayerService {
    
    static final Logger logger = Logger.getLogger(GamePlayerServiceImpl.class);

    @Autowired
    GameDao gameDao;
    @Autowired
    GameService gameService;
    @Autowired
    MailService mailService;
    @Autowired
    GamePlayerDao gamePlayerDao;
    @Autowired
    GameCalculator gameCalculator;
    @Autowired
    PayoutCalculator payoutCalculator;
    @Autowired
    GameChangedGateway gateway;
	
    @Override
    public int create(GamePlayer gamePlayer) throws Exception {
        checkGame(gamePlayer);
        checkPlayer(gamePlayer, null);
        int id = gamePlayerDao.insert(gamePlayer);
        gameCalculator.calculate(gamePlayer.getGameId());
        payoutCalculator.calculate(gamePlayer.getGameId());

        Game game = gameDao.selectById(gamePlayer.getGameId());
        gateway.notifyGameChanged(game);
        return id;
    }   

    @Override
    public void update(GamePlayer gamePlayer) throws Exception {
        checkGame(gamePlayer);
        GamePlayer existingPlayer = findById(gamePlayer.getId());
        checkPlayer(gamePlayer, existingPlayer);
        if (gamePlayer.getFinish() != null) {
            gamePlayer.setKnockedOut(true);
        }
        
        gamePlayerDao.update(gamePlayer);
        gameCalculator.calculate(gamePlayer.getGameId());
        payoutCalculator.calculate(gamePlayer.getGameId());
        
        if (!existingPlayer.isOptIn() && gamePlayer.isOptIn()) {
            mailService.sendOptIn(gamePlayer);
        }

        Game game = gameDao.selectById(gamePlayer.getGameId());
        gateway.notifyGameChanged(game);
    }   

    @Override
    public GamePlayer findById(int id) {
        return gamePlayerDao.selectById(id);
    }
    
    @Override
    public GamePlayer findByPlayerId(int playerId, int gameId) {
        return gamePlayerDao.selectByPlayerId(playerId, gameId);
    }
    
    @Override
    public void delete(int id) {
        GamePlayer gamePlayer = gamePlayerDao.selectById(id);
        gamePlayerDao.delete(id);

        Game game = gameDao.selectById(gamePlayer.getGameId());
        gateway.notifyGameChanged(game);
    }

    // If the game is finalized then no players can be added
    private void checkGame(GamePlayer player) throws CannotAddGamePlayerException {
        Game game = gameDao.selectById(player.getGameId());
        if (game.isFinalized()) {
            throw new CannotAddGamePlayerException("Game is finalized so no players can be added");
        }
    }

    // Check if player already belongs to the game
    private void checkPlayer(GamePlayer newPlayer, GamePlayer oldPlayer) throws DuplicatePlayerException, CannotSetFinishException {
        Game game = gameDao.selectById(newPlayer.getGameId());
        for (GamePlayer existingPlayer : game.getPlayers()) {
            if (oldPlayer != null && (oldPlayer.getId() == existingPlayer.getId())) {
                continue;
            }
            if (existingPlayer.getPlayerId() == newPlayer.getPlayerId()) {
                throw new DuplicatePlayerException(newPlayer, existingPlayer.getPlayer().getFirstName() + " "
                        + existingPlayer.getPlayer().getLastName() + " already joined the game");
            }
            if (existingPlayer.getFinish() != null && 
                    existingPlayer.getFinish().equals(newPlayer.getFinish())) {
                throw new CannotSetFinishException("Another players finished in " + newPlayer.getFinish() + " place");
            }
        }
    }

    private boolean checkIfFinished(int gameId) {
        Game game = gameDao.selectById(gameId);
        
        int numFinishes = Math.min(game.getNumPlayers(), 10);
        
        HashSet<Integer> places = new HashSet<Integer>();
        for (int i = 1; i <= numFinishes; ++i) {
            places.add(new Integer(i));
        }
        
        for (GamePlayer player : game.getPlayers()) {
            if (player.getFinish() != null) {
                places.remove(player.getFinish());
            }
        }
        
        return places.isEmpty();
    }
}
