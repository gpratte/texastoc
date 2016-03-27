package com.texastoc.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import com.texastoc.common.HomeGame;
import com.texastoc.dao.GameDao;
import com.texastoc.dao.GamePayoutDao;
import com.texastoc.dao.GamePlayerDao;
import com.texastoc.dao.PlayerDao;
import com.texastoc.dao.SeasonDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePayout;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Player;
import com.texastoc.domain.QuarterlySeason;
import com.texastoc.domain.Season;
import com.texastoc.domain.Seat;
import com.texastoc.exception.CannotCreateGameException;
import com.texastoc.exception.CannotFinalizeException;
import com.texastoc.exception.CannotRandomizeException;
import com.texastoc.exception.InvalidDateException;
import com.texastoc.exception.InvalidQuarterException;
import com.texastoc.service.calculate.GameCalculator;
import com.texastoc.service.calculate.PayoutCalculator;
import com.texastoc.service.calculate.QuarterlySeasonCalculator;
import com.texastoc.service.calculate.SeasonCalculator;
import com.texastoc.service.integration.GameChangedGateway;
import com.texastoc.service.mail.MailService;

@Service
@EnableCaching 
public class GameServiceImpl implements GameService {

    static final Logger logger = Logger.getLogger(GameServiceImpl.class);

    @Autowired
    GameDao gameDao;
    @Autowired
    SeasonDao seasonDao;
    @Autowired
    PlayerDao playerDao;
    @Autowired
    MailService mailService;
    @Autowired
    GameChangedGateway gateway;
    @Autowired
    GamePlayerDao gamePlayerDao;
    @Autowired
    GamePayoutDao gamePayoutDao;
    @Autowired
    GameCalculator gameCalculator;
    @Autowired
    SeasonCalculator seasonCalculator;
    @Autowired
    PayoutCalculator payoutCalculator;
    @Autowired
    QuarterlySeasonCalculator qSeasonCalculator;
    
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private HashMap<Integer, List<Seat>> gameSeating = new HashMap<Integer, List<Seat>>();
    private HashMap<Integer, List<Integer>> gameTables = new HashMap<Integer, List<Integer>>();

    @Override
    public List<Game> findBySeasonId(int seasonId, boolean includePlayers) {
        return gameDao.selectBySeasonId(seasonId, includePlayers);
    }

    @Override
    public List<Game> findAll() {
        return gameDao.selectAll();
    }

    @Override
    public List<Game> findByDate(LocalDate startDate, LocalDate endDate,
            boolean includePlayers) {
        return gameDao.selectByDate(startDate, endDate, includePlayers);
    }

    @Override
    public Game findById(int id) {
        Game game = gameDao.selectById(id);
        if (gameSeating.containsKey(id)) {
            game.setSeating(gameSeating.get(id));
        }
        return game;
    }

    @Override
    public Game findMostRecent() {
        Game game = gameDao.selectMostRecent();
        if (gameSeating.containsKey(game.getId())) {
            game.setSeating(gameSeating.get(game.getId()));
        }
        return game;
    }

    @Override
    @CacheEvict(value="seasoncache", allEntries=true)
    public int create(Game game) throws Exception {
        Season season = seasonDao.selectById(game.getSeasonId());
        checkGame(season, game, null);
        game.setAnnualIndex(season.getGames().size() + 1);
        QuarterlySeason qSeason = season.getQuarterlies().get(season.getQuarterlies().size() -1);
        game.setQuarterlyIndex(qSeason.getGames().size() + 1);
        
        if (season.getKittyGameDebit() > 0) {
            game.setKittyDebit(season.getKittyGameDebit());
        }
        
        //;;!! hardcoded to 7:30 central
        DateTimeZone timeZone = DateTimeZone.forID("CST6CDT");
        LocalDate gameDate = game.getGameDate();
        DateTime startTime = new DateTime(gameDate.getYear(),
                gameDate.getMonthOfYear(),
                gameDate.getDayOfMonth(),
                19, // 7 pm
                30, // half past
                0, // seconds
                timeZone); // central
        game.setStartTime(startTime);
        logger.debug("GameServiceImpl recording hardcoded startTime " + game.getStartTime() + " tz " + game.getStartTime().getZone());

        int id = gameDao.insert(game);
        game = gameDao.selectById(id);
        
        // Set the host to get the email notifications
        Player host = playerDao.selectById(game.getHostId());
        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setGameId(id);
        gamePlayer.setPlayerId(host.getId());
        gamePlayer.setEmailOptIn(true);
        gamePlayerDao.insert(gamePlayer);

        if (mailService.isEviteEmailSent(game.getGameDate())) {
            List<Player> corePlayers = playerDao.selectCore();
            mailService.sendEviteHasBeenSent(corePlayers);
        }
        
        gateway.notifyGameChanged(game);
        return id;
    }
    
    @Override
    @CacheEvict(value="seasoncache", allEntries=true)
    public void finalize(int id) throws Exception {
    	Game game = findById(id);
    	game.setFinalized(true);
    	update(game);
    }
    
    //@ Transactional
    @Override
    @CacheEvict(value="seasoncache", allEntries=true)
    public void update(Game game) throws Exception {
        Game existingGame = gameDao.selectById(game.getId());
        Season season = seasonDao.selectById(game.getSeasonId());
        checkGame(season, game, existingGame);
        
        // Check if host changed
        if (game.getHostId() != null && 
                (existingGame.getHostId() == null || 
                existingGame.getHostId().intValue() != game.getHostId().intValue())) {
            // See if the host already belongs to the game
            boolean found = false;
            for (GamePlayer gamePlayer : existingGame.getPlayers()) {
                if (gamePlayer.getPlayerId() == game.getHostId().intValue()) {
                    found = true;
                    gamePlayer.setEmailOptIn(true);
                    gamePlayerDao.update(gamePlayer);
                    break;
                }
            }
            
            if (!found) {
                Player host = playerDao.selectById(game.getHostId());
                GamePlayer gamePlayer = new GamePlayer();
                gamePlayer.setGameId(game.getId());
                gamePlayer.setPlayerId(host.getId());
                gamePlayer.setEmailOptIn(true);
                gamePlayerDao.insert(gamePlayer);
            }
        }

        if (existingGame.isFinalized() != game.isFinalized()) {
            if (game.isFinalized()) {
                checkForInvalidGamePlayers(game);
            }
        }

        gameDao.update(game);

        gameCalculator.calculate(game.getId());
        payoutCalculator.calculate(game.getId());
        if (game.isFinalized()) {
            qSeasonCalculator.calculateByGameId(game.getId());
            seasonCalculator.calculate(game.getSeasonId());
        }
        
        game = gameDao.selectById(game.getId());
        gateway.notifyGameChanged(game);
    }

    //@ Transactional
    @Override
    @CacheEvict(value="seasoncache", allEntries=true)
    public void delete(int id) throws Exception {
    	
    	Game game = gameDao.selectById(id);

    	// Get previous game
    	Season season = seasonDao.selectById(game.getSeasonId());
    	Game previousGame = null;
    	for (Game gme : season.getGames()) {
    		if (previousGame == null) {
    			previousGame = gme;
    			continue;
    		}
    		
    		if (gme.getId() == game.getId()) {
    			break;
    		}
    		
    		previousGame = gme;
    	}

    	// Delete game players
    	gamePlayerDao.deleteByGame(id);
    	
    	// Delete game payouts
    	gamePayoutDao.deleteByGameId(id);
    	
    	// Delete game
    	gameDao.delete(id);
    	
    	// Recalculate season and quarterly
    	if (previousGame != null) {
            qSeasonCalculator.calculateByGameId(previousGame.getId());
            seasonCalculator.calculate(previousGame.getSeasonId());
    	}
    }

    @Override
    public void randomizeSeats(int gameId, List<String> includePlayersWOBuyInIds, 
            List<String> excludePlayerIds, Player firstPlayer, 
            List<Integer> playersPerTable) throws CannotRandomizeException {

        Game game = findById(gameId);
        List<GamePlayer> playersToRandomize = new ArrayList<GamePlayer>();
        
        // Add players that are in the game and have a buy in are are not excluded
        loop1:
        for (GamePlayer gamePlayer : game.getPlayers()) {
            if (gamePlayer.getBuyIn() != null) {
                if (excludePlayerIds != null) {
                    for (String playerId : excludePlayerIds) {
                        if (StringUtils.equals(playerId, Integer.toString(gamePlayer.getId()))) {
                            continue loop1;
                        }
                    }
                }
                playersToRandomize.add(gamePlayer);
            }
        }

        // Add players that are in the game and do not a buy in
        if (includePlayersWOBuyInIds != null) {
            for (String gamePlayerId : includePlayersWOBuyInIds) {
                GamePlayer gamePlayer = gamePlayerDao.selectById(Integer
                        .parseInt(gamePlayerId));
                playersToRandomize.add(gamePlayer);
            }
        }

        // Make sure there is room
        int totalNumSeatsNeeded = 0;
        for (int seats : playersPerTable) {
            totalNumSeatsNeeded += seats;
        }
        if (playersToRandomize.size() > totalNumSeatsNeeded) {
            gameSeating.remove(gameId);
            gameTables.remove(gameId);
            throw new CannotRandomizeException("Not enough seats for the number of players");
        }
        
        // Randomize the list quit a bit
        for (int loop = 0; loop < 100; ++loop) {
            // Swap two players
            int index1 = RANDOM.nextInt(playersToRandomize.size());
            int index2 = RANDOM.nextInt(playersToRandomize.size());
            GamePlayer gamePlayer = playersToRandomize.get(index1);
            playersToRandomize.set(index1, playersToRandomize.get(index2));
            playersToRandomize.set(index2, gamePlayer);
        }

        // Create the tables (1's based)
        HashMap<Integer, List<Seat>> tables = new HashMap<Integer, List<Seat>>();
        int numTables = 0;
        for (int count : playersPerTable) {
            if (count == 0) {
                continue;
            }
            
            // Allocate the seats for the table
            ArrayList<Seat> seats = new ArrayList<Seat>(count);
            for (int i = 0; i < count; ++i) {
                Seat seat = new Seat();
                seats.add(seat);
            }
            
            // Put the table in the map
            tables.put(++numTables, seats);
        }

        // Put players in seats
        int playersSeated = 0;
        while (playersSeated < playersToRandomize.size()) {
            for (int i = 1; i <= numTables; ++i) {
                List<Seat> seats = tables.get(i);
                Seat seat = getAvailableSeat(seats);
                if (seat != null) {
                    GamePlayer gamePlayer = playersToRandomize.get(playersSeated++);
                    seat.setPlayer(gamePlayer.getPlayer());
                    seat.setTable(i);
                    if (playersSeated == playersToRandomize.size()) {
                        break;
                    }
                }
            }
        }        
        
        // If the player to be at table 1 and that player is at another
        // table then swap that player with someone at table 1
        if (firstPlayer != null) {
            int tableToSwap = 0;
            int seatToSwap = 0;
            outer:
            for (int i = 2; i <= numTables; ++i) {
                List<Seat> seats = tables.get(i);
                for (int j = 0; j < seats.size(); ++j) {
                    if (seats.get(j).getPlayer() != null &&
                    		seats.get(j).getPlayer().getId() == firstPlayer.getId()) {
                        tableToSwap = i;
                        seatToSwap = j;
                        break outer;
                    }
                }
            }
            
            if (tableToSwap > 1) {
            	// Get the seat where the player is that will be moved
            	// to table 1
                Seat originalSeat = tables.get(tableToSwap).remove(seatToSwap);
                
                // Randomly pick a seat at table 1
                int table1SeatIndex = -1;
                List<Seat> table1Seats = tables.get(1);
                do {
                    table1SeatIndex = RANDOM.nextInt(tables.get(1).size());
                } while (table1Seats.get(table1SeatIndex).getPlayer() == null);

                Seat seatToMoveFromTable1 = tables.get(1).remove(table1SeatIndex);
                
                originalSeat.setTable(1);
                tables.get(1).add(table1SeatIndex, originalSeat);

                seatToMoveFromTable1.setTable(tableToSwap);
                tables.get(tableToSwap).add(seatToSwap, seatToMoveFromTable1);
            }
        }

        // Now place the players in the seats at each table
        for (int i = 1; i <= numTables; ++i) {
            List<Seat> seats = tables.get(i);

            int numPlayersSeated = 0;
            for (Seat seat : seats) {
                if (seat.getPlayer() != null) {
                    ++numPlayersSeated;
                }
            }

            double seatingFactor = seats.size() / (double)numPlayersSeated;
            seats.get(0).setPosition(1);

            double nextSeat = 1;

            for (int j = 1; j < numPlayersSeated; ++j) {
                nextSeat += seatingFactor;
                seats.get(j).setPosition((int) Math.round(nextSeat));
            }
        }

        List<Seat> allSeats = new ArrayList<Seat>();
        for (int i = 1; i <= numTables; ++i) {
            List<Seat> seats = tables.get(i);
            for (Seat seat : seats) {
                if (seat.getPlayer() == null) {
                    continue;
                }
                allSeats.add(seat);
            }
        }

        gameSeating.put(gameId, allSeats);
        gameTables.put(gameId, playersPerTable);

    }

    @Override
    public void assignSeats(int gameId, List<String> playersWBuyInList, 
            List<String> playersWOBuyInList) throws CannotRandomizeException {

        Game game = findById(gameId);
        
        if (! gameSeating.containsKey(gameId)) {
            throw new CannotRandomizeException("Could not find seats for the game");
        }
        List<Seat> seats = gameSeating.get(gameId);
        
        if (! gameTables.containsKey(gameId)) {
            throw new CannotRandomizeException("Could not find tables for the game");
        }
        List<Integer> tables = gameTables.get(gameId);

        // Make sure there is room
        int totalSeats = 0;
        for (Integer table : tables) {
            totalSeats += table;
        }
        
        int newPlayersCount = 0;
        if (playersWBuyInList != null) {
            newPlayersCount += playersWBuyInList.size();
        }
        if (playersWOBuyInList != null) {
            newPlayersCount += playersWOBuyInList.size();
        }
        if (totalSeats < seats.size() + newPlayersCount) {
            throw new CannotRandomizeException("Not enough seats");
        }
        
        if (playersWBuyInList != null) {
            for (String gamePlayerId : playersWBuyInList) {
                if (! assignSeat(Integer.valueOf(gamePlayerId), gameId)) {
                    throw new CannotRandomizeException("Could not seat one of the players");
                }
            }
        }
        if (playersWOBuyInList != null) {
            for (String gamePlayerId : playersWOBuyInList) {
                if (! assignSeat(Integer.valueOf(gamePlayerId), gameId)) {
                    throw new CannotRandomizeException("Could not seat one of the players");
                }
            }
        }
    }

    @Override
    public void updateNote(int id, String note) {
        gameDao.updateNote(id, note);
    }

    @Override
    public void clearSeats(int id) {
        gameSeating.remove(id);
        gameTables.remove(id);
    }

    @Override
    @CacheEvict(value="seasoncache", allEntries=true)
    public void changePayout(int id, int change) throws Exception {
        gameDao.changePayout(id, change);
        payoutCalculator.calculate(id);
    }

    @Override
    public List<GamePayout> findPayoutsByGameId(int gameId) {
        return gamePayoutDao.selectByGameId(gameId);
    }

    @Override
    @CacheEvict(value="seasoncache", allEntries=true)
    public void updateHomeGame(int gameId, int homeGameMarker) {
    	HomeGame homeGame = HomeGame.fromInt(homeGameMarker);
    	int kittyDebit = 0;
    	if (homeGame == HomeGame.TOC) {
    		Game game = gameDao.selectById(gameId);
    		Season season = seasonDao.selectById(game.getSeasonId());
            if (season.getKittyGameDebit() > 0) {
                kittyDebit = season.getKittyGameDebit();
            }
    	}
    	gameDao.updateHomeGame(gameId, homeGameMarker, kittyDebit);
    }
    
    @Override
    public void updateTransport(int gameId, boolean flag) {
        gameDao.updateTransport(gameId, flag);
    }


    public void messageSeats(int id) {
        logger.debug("messageSeats entered");
        Game game = findById(id);
        if (game.getSeating() == null) {
            logger.debug("No seats to seat");
            return;
        }
        if (game.isFinalized()) {
            logger.debug("Game is finalized, no seating");
            return;
        }

        for (Seat seat : game.getSeating()) {
            Player player = seat.getPlayer();
            if (player.getCellCarrier() != null
                    && StringUtils.isNotEmpty(player.getCellCarrier())
                    && player.getPhone() != null
                    && StringUtils.isNotEmpty(player.getPhone())) {
                mailService.sendSeatingMail(player.getPhone() + player.getCellCarrier(), seat, game);
            }
        }
    }

    @Override
    public void seated(int gameId) {
        gameDao.seated(gameId);
    }

    @Override
    public void recordStartTime(int gameId) {
        gameDao.recordStartTime(gameId);
    }

    private void checkGame(Season season, Game newGame, Game oldGame)
            throws InvalidDateException, InvalidQuarterException, 
            CannotCreateGameException {

        // Check that the date is not null
        if (newGame.getGameDate() == null) {
            throw new InvalidDateException(null, "Game date is missing");
        }

        // Make sure the game is in the bounds of the
        // containing season
        if (newGame.getGameDate().isBefore(season.getStartDate())) {
            throw new InvalidDateException(newGame.getGameDate(),
                    "Game date cannot be before the season start date");
        }
        if (newGame.getGameDate().isAfter(season.getEndDate())) {
            throw new InvalidDateException(newGame.getGameDate(),
                    "Game date cannot be after the season end date");
        }

        List<Game> existingGames = findBySeasonId(newGame.getSeasonId(), false);
        
        if (oldGame == null && existingGames.size() == 52) {
            throw new CannotCreateGameException("There are already 52 games in the season");
        }
        
        for (Game existingGame : existingGames) {

            if (oldGame != null && oldGame.getId() == existingGame.getId()) {
                // Do not compare to itself
                continue;
            }

            // Check that the game date is not already taken
            LocalDate gameDate = newGame.getGameDate();
            LocalDate existingGameDate = existingGame.getGameDate();

            if (gameDate.equals(existingGameDate)) {
                throw new InvalidDateException(gameDate,
                        "Game date already belongs to another game");
            }
        }
    }

    private void checkForInvalidGamePlayers(Game game)
            throws CannotFinalizeException {
        List<GamePlayer> invalidPlayers = null;
        List<GamePlayer> players = gamePlayerDao.selectByGameId(game.getId());
        for (GamePlayer player : players) {
            if (!player.isFinalizable()) {
                if (invalidPlayers == null) {
                    invalidPlayers = new ArrayList<GamePlayer>();
                }
                invalidPlayers.add(player);
            }
        }

        if (invalidPlayers != null) {
            throw new CannotFinalizeException(invalidPlayers,
                    "The game has players that have not bought in");
        }

        int numberOfPlaces = Math.min(players.size(), 10);
        for (int i = 1; i <= numberOfPlaces; ++i) {
            boolean found = false;
            for (GamePlayer player : players) {
                if (player.getFinish() != null && player.getFinish() == i) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new CannotFinalizeException(invalidPlayers,
                        "No players was marked for finishing " + i);
            }
        }
    }
    
    private Seat getAvailableSeat(List<Seat> seats) {
        for (Seat seat : seats) {
            if (seat.getPlayer() == null) {
                return seat;
            }
        }
        return null;
    }
 
    // Remember that tables are 1's based
    private boolean assignSeat(int gamePlayerId, int gameId) {
        List<Seat> seats = gameSeating.get(gameId);
        List<Integer> maxPlayersPerTable = gameTables.get(gameId);
        if (seats == null || maxPlayersPerTable == null) {
            return false;
        }

        int tableToAssign = 0;
        int maxPlayersAllowedAtTableToAssign = 0;
        int mostFreeSeats = 0;

        // Tables are 1's based
        for (int i = 0; i < maxPlayersPerTable.size(); ++i) {
            // Count how many players are at the table
            int maxPlayers = maxPlayersPerTable.get(i);
            int numPlayers = 0;
            for (Seat seat : seats) {
                if (seat.getTable() == (i+1)) {
                    ++numPlayers;
                }
            }
            
            if ( (maxPlayers - numPlayers) > mostFreeSeats ) {
                mostFreeSeats = maxPlayers - numPlayers;
                tableToAssign = i+1;
                maxPlayersAllowedAtTableToAssign = maxPlayers;
            }
        }

        if (tableToAssign == 0) {
            return false;
        }
        
        // Now find the max block(s) of empty seats
        int sizeOfBlock = 0;
        List<Integer> beginningOfBlock = new ArrayList<Integer>();
        
        Seat previousSeat = null;
        for (Seat seat : seats) {
            if (seat.getTable() == (tableToAssign)) {
                if (previousSeat != null) {
                    if ( (seat.getPosition() - previousSeat.getPosition() -1) > sizeOfBlock) {
                        sizeOfBlock = seat.getPosition() - previousSeat.getPosition() -1;
                        beginningOfBlock.clear();
                        beginningOfBlock.add(previousSeat.getPosition() + 1);
                    } else if ((seat.getPosition() - previousSeat.getPosition() -1) == sizeOfBlock) {
                        beginningOfBlock.add(previousSeat.getPosition() + 1);
                    }
                }
                previousSeat = seat;
            }
        }
        
        // Now check the seat that is one bigger than the table
        int oneMoreChairPosition = maxPlayersAllowedAtTableToAssign + 1;
        if ( (oneMoreChairPosition - previousSeat.getPosition() -1) > sizeOfBlock) {
            sizeOfBlock = oneMoreChairPosition - previousSeat.getPosition() -1;
            beginningOfBlock.clear();
            beginningOfBlock.add(previousSeat.getPosition() + 1);
        } else if ((oneMoreChairPosition - previousSeat.getPosition() -1) == sizeOfBlock) {
            beginningOfBlock.add(previousSeat.getPosition() + 1);
        }
        
        if (sizeOfBlock == 0 || beginningOfBlock.size() < 1) {
            return false;
        }
        
        int beginningOfBlockSeat = 0;
        if (beginningOfBlock.size() == 1) {
            beginningOfBlockSeat = beginningOfBlock.get(0);
        } else {
            // If there is more than one block randomly choose a block
            int index = 0;
            for (int i = 1; i < 100; ++i) {
                index = RANDOM.nextInt(beginningOfBlock.size());
            }
            beginningOfBlockSeat = 
                    beginningOfBlock.get(index);
        }
        
        // Pick a seat in the block
        int seatToAssign = 0;
        seatToAssign = beginningOfBlockSeat;
        if (sizeOfBlock == 1) {
            // Nothing else to do
        } else if (sizeOfBlock == 2) {
            if (RANDOM.nextBoolean()) {
                ++seatToAssign;
            }
        } else if ((sizeOfBlock % 2) == 0) {
            // Even so pick one of the two middle seats.
            // Divide by 2.
            int middleOfBlock = sizeOfBlock / 2;
            // Move to the seat
            seatToAssign += middleOfBlock;
            if (RANDOM.nextBoolean()) {
                // Move one more seat
                --seatToAssign;
            }
        } else {
            // Odd so pick the middle seat.
            // Decrease the size of the block by 1
            --sizeOfBlock;
            // Divide by 2
            int middleOfBlock = sizeOfBlock / 2;
            // Move to the seat
            seatToAssign += middleOfBlock;
        }

        Seat seat = new Seat();
        seat.setPosition(seatToAssign);
        seat.setTable(tableToAssign);
        GamePlayer gamePlayer = gamePlayerDao.selectById(gamePlayerId);
        seat.setPlayer(gamePlayer.getPlayer());
        seats.add(seat);
        Collections.sort(seats);
        
        // Text the player
        Game game = findById(gameId);
        Player player = seat.getPlayer();
        if (player.getCellCarrier() != null
                && StringUtils.isNotEmpty(player.getCellCarrier())
                && player.getPhone() != null
                && StringUtils.isNotEmpty(player.getPhone())) {
            mailService.sendSeatingMail(player.getPhone() + player.getCellCarrier(), seat, game);
        }
        
        return true;
    }
}
