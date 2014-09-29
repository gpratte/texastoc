package com.texastoc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.texastoc.dao.GameDao;
import com.texastoc.dao.GamePlayerDao;
import com.texastoc.dao.SeasonDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Player;
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
public class GameServiceImpl implements GameService {

    static final Logger logger = Logger.getLogger(GameServiceImpl.class);

    @Autowired
    GameDao gameDao;
    @Autowired
    SeasonDao seasonDao;
    @Autowired
    GamePlayerDao gamePlayerDao;
    @Autowired
    GameCalculator gameCalculator;
    @Autowired
    SeasonCalculator seasonCalculator;
    @Autowired
    QuarterlySeasonCalculator qSeasonCalculator;
    @Autowired
    PayoutCalculator payoutCalculator;
    @Autowired
    MailService mailService;
    @Autowired
    GameChangedGateway gateway;
    
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private HashMap<Integer, List<Seat>> gameSeating = new HashMap<Integer, List<Seat>>();

    @Override
    public List<Game> findBySeasonId(int seasonId, boolean includePlayers) {
        return gameDao.selectBySeasonId(seasonId, includePlayers);
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
    public int create(Game game) throws Exception {
        Season season = seasonDao.selectById(game.getSeasonId());
        checkGame(season, game, null);
        int id = gameDao.insert(game);
        game = gameDao.selectById(id);
        gateway.notifyGameChanged(game);
        return id;
    }

    @Override
    public void update(Game game) throws Exception {
        Game existingGame = gameDao.selectById(game.getId());
        Season season = seasonDao.selectById(game.getSeasonId());
        checkGame(season, game, existingGame);

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

    @Override
    public void randomizeSeats(int gameId, int numTables,
            int numPlayersPerTable, List<String> includePlayersWOBuyInIds, 
            List<String> excludePlayerIds, Player firstPlayer) throws CannotRandomizeException {

        Game game = findById(gameId);
        List<GamePlayer> playersToRandomize = new ArrayList<GamePlayer>();
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

        if (includePlayersWOBuyInIds != null) {
            for (String gamePlayerId : includePlayersWOBuyInIds) {
                GamePlayer gamePlayer = gamePlayerDao.selectById(Integer
                        .parseInt(gamePlayerId));
                playersToRandomize.add(gamePlayer);
            }
        }

        // Make sure there is room
        if (playersToRandomize.size() > (numTables * numPlayersPerTable)) {
            gameSeating.remove(gameId);
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

        HashMap<Integer, List<Seat>> tables = new HashMap<Integer, List<Seat>>();
        for (int i = 1; i <= numTables; ++i) {
            ArrayList<Seat> seats = new ArrayList<Seat>();
            tables.put(i, seats);
        }

        // Put players at tables
        double numberOfPlayersPerTable = (double) playersToRandomize.size() / numTables;
        int playersSeated = 0;
        for (int i = 1; i <= numTables; ++i) {
            for (int j = 0; j <= numberOfPlayersPerTable && playersSeated < playersToRandomize.size(); ++j) {
                GamePlayer gamePlayer = playersToRandomize.get(playersSeated++);
                Seat seat = new Seat();
                seat.setPlayer(gamePlayer.getPlayer());
                seat.setTable(i);
                tables.get(i).add(seat);
            }
        }
        
        // Put the stragglers at a table
        if (playersSeated < playersToRandomize.size()) {
            List<GamePlayer> stragglers = new ArrayList<GamePlayer>();
            for (GamePlayer gamePlayer : playersToRandomize) {
                boolean found = false;
                for (int i = 1; i <= numTables; ++i) {
                    List<Seat> seats = tables.get(i);
                    for (Seat seat : seats) {
                        if (seat.getPlayer().getId() == gamePlayer.getPlayerId()) {
                            found = true;
                        }
                    }
                }
                if (! found) {
                    stragglers.add(gamePlayer);
                }
            }
            
            for (GamePlayer gamePlayer : stragglers) {
                for (int i = 1; i <= numTables; ++i) {
                    Seat seat = new Seat();
                    seat.setPlayer(gamePlayer.getPlayer());
                    seat.setTable(i);
                    tables.get(i).add(seat);
                }
            }
        }

        // Make sure that no table has 2 players more than another
        boolean unbalanced = true;
        while (unbalanced) {
            unbalanced = false;
            outer:
            for (int i = numTables; i >= 2; --i) {
                for (int j = i-1; j >= 1; --j) {
                    if (balance(i, j, tables)) {
                        unbalanced = true;
                        break outer;
                    }
                }
            }
        }
        
        
        // If the player to be at table 1, seat 1 is set then
        // swap that player with whomever is in that position
        if (firstPlayer != null) {
            int tableToSwap = 0;
            int seatToSwap = 0;
            outer:
            for (int i = 1; i <= numTables; ++i) {
                List<Seat> seats = tables.get(i);
                for (int j = 0; j < seats.size(); ++j) {
                    if (seats.get(j).getPlayer().getId() == firstPlayer.getId()) {
                        tableToSwap = i;
                        seatToSwap = j;
                        break outer;
                    }
                }
            }
            
            if (tableToSwap != 0 && (tableToSwap != 1 && seatToSwap != 0)) {
                Seat seat1 = tables.get(tableToSwap).remove(seatToSwap);
                Seat seat2 = tables.get(1).remove(0);
                
                seat1.setTable(1);
                seat2.setTable(tableToSwap);
                
                tables.get(tableToSwap).add(seatToSwap, seat2);
                tables.get(1).add(0, seat1);
            }
        }

        // Now place the players in the seats at each table
        for (int i = 1; i <= numTables; ++i) {
            List<Seat> seats = tables.get(i);

            double seatingFactor = (double) numPlayersPerTable / seats.size();
            seats.get(0).setPosition(1);

            double nextSeat = 1;

            for (int j = 1; j < seats.size(); ++j) {
                nextSeat += seatingFactor;
                seats.get(j).setPosition((int) Math.round(nextSeat));
            }
        }

        List<Seat> allSeats = new ArrayList<Seat>();
        for (int i = 1; i <= numTables; ++i) {
            allSeats.addAll(tables.get(i));
        }

        gameSeating.put(gameId, allSeats);

    }

    @Override
    public void clearSeats(int id) {
        gameSeating.remove(id);
    }

    @Override
    public void changePayout(int id, int change) throws Exception {
        gameDao.changePayout(id, change);
        payoutCalculator.calculate(id);
    }

    
//    public Game startNewGameOrGetGameInProgress() throws SQLException {
//        Game game = findMostRecent();
//        if (!game.isFinalized()) {
//            return game;
//        }
//
//        // If most recent game is today do not create a new game
//        LocalDate today = new LocalDate();
//        if (game.getGameDate().equals(today)) {
//            return game;
//        }
//
//        // If most recent game is yesterday do not create a new game
//        LocalDate yesterday = today.minusDays(1);
//        if (game.getGameDate().equals(yesterday)) {
//            return game;
//        }
//
//        Game newGame = new Game();
//        newGame.setGameDate(new LocalDate());
//
//        Season season = seasonDao.selectById(game.getSeasonId());
//        if (season.getGames().size() < 52) {
//            newGame.setSeasonId(season.getId());
//        } else {
//            // Get the next season
//            List<Season> seasons = seasonDao.selectAll();
//            boolean found = false;
//            for (Season seeson : seasons) {
//                if (seeson.getId() == season.getId()) {
//                    continue;
//                }
//                if (seeson.getStartDate().isAfter(season.getStartDate())) {
//                    newGame.setSeasonId(seeson.getId());
//                    found = true;
//                    break;
//                }
//            }
//
//            if (!found) {
//                throw new RuntimeException(
//                        "Cannot find the next season after the last game");
//            }
//        }
//
//        int id = gameDao.insert(newGame);
//        newGame = gameDao.selectById(id);
//
//        // Check if double buy in
//        season = seasonDao.selectById(newGame.getSeasonId());
//        List<Game> games = season.getGames();
//        Collections.sort(games);
//
//        int count = 0;
//        for (Game gme : games) {
//            ++count;
//            if (gme.getId() == newGame.getId()) {
//                if (count == 13 || count == 26 || count == 39) {
//                    newGame.setDoubleBuyIn(true);
//                }
//                break;
//            }
//        }
//        
//        return newGame;
//    }

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
    
    private boolean balance(int table1, int table2, HashMap<Integer, List<Seat>> tables) {
        List<Seat> seats1 = tables.get(table1);
        List<Seat> seats2 = tables.get(table2);
        if (seats1.size() - seats2.size() > 1) {
            Seat seat1 = tables.get(table1).remove(0);
            seat1.setTable(table2);
            tables.get(table2).add(seat1);
            return true;
        }
        if (seats2.size() - seats1.size() > 1) {
            Seat seat2 = tables.get(table2).remove(0);
            seat2.setTable(table1);
            tables.get(table1).add(seat2);
            return true;
        }
        return false;
    }
}
