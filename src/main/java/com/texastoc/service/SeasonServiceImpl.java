package com.texastoc.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.texastoc.dao.GameDao;
import com.texastoc.dao.GamePayoutDao;
import com.texastoc.dao.PlayerDao;
import com.texastoc.dao.SeasonChampionDao;
import com.texastoc.dao.SeasonDao;
import com.texastoc.domain.Game;
import com.texastoc.domain.GamePayout;
import com.texastoc.domain.GamePlayer;
import com.texastoc.domain.Player;
import com.texastoc.domain.PlayerCount;
import com.texastoc.domain.QuarterlySeason;
import com.texastoc.domain.Season;
import com.texastoc.domain.SeasonChampion;
import com.texastoc.domain.SeasonPayout;
import com.texastoc.domain.SeasonTopTen;
import com.texastoc.exception.InvalidDateException;
import com.texastoc.service.calculate.QuarterlySeasonCalculator;
import com.texastoc.service.calculate.SeasonCalculator;
import com.texastoc.service.mail.MailService;

@Transactional
@EnableCaching 
@Service("seasonService")
public class SeasonServiceImpl implements SeasonService {

    static final Logger logger = Logger.getLogger(SeasonServiceImpl.class);

    @Autowired
    private GameDao gameDao;
    @Autowired
    private SeasonDao seasonDao;
    @Autowired
    private PlayerDao playerDao;
    @Autowired
    private MailService mailService;
    @Autowired
    private GamePayoutDao gamePayoutDao;
    @Autowired
    private SeasonCalculator seasonCalculator;
    @Autowired
    private SeasonChampionDao seasonChampionDao;
    @Autowired
    private QuarterlySeasonCalculator qSeasonCalculator;


    @Override
    public List<Season> findAll() {
        return seasonDao.selectAll();
    }

    @Override
	public List<SeasonChampion> findAllChampions() {
    	return seasonChampionDao.selectAll();
    }

	@Override
    public Season getCurrent() {
        List<Season> seasons = seasonDao.selectAll();
        return seasons.get(0);
    }

    @Override
    public Season findById(int id) {
        return seasonDao.selectById(id);
    }

    @Override
    public int create(Season season) throws Exception {
        checkSeasonDates(season, null);
        return seasonDao.insert(season);
    }

    @Override
    @CacheEvict(value="seasoncache", allEntries=true)
    public void update(Season season) throws Exception {
        Season existingSeason = findById(season.getId());
        checkSeasonDates(season, existingSeason);
        if (StringUtils.isBlank(season.getFinalTableImage())) {
            season.setFinalTableImage(null);
        }
        if (StringUtils.isBlank(season.getFinalTableThumb())) {
            season.setFinalTableThumb(null);
        }
        seasonDao.update(season);
    }

    @Override
    @Cacheable(value="seasoncache")
    public Season getUpToGame(int gameId) throws Exception {
        Season season = seasonCalculator.calcluateUpToGame(gameId);
        QuarterlySeason qSeason = qSeasonCalculator.calculateUpToGame(gameId);
        if (qSeason != null) {
            ArrayList<QuarterlySeason> qSeasons = new ArrayList<QuarterlySeason>();
            qSeasons.add(qSeason);
            season.setQuarterlies(qSeasons);
        }
        return season;
    }

    @Override
    public List<PlayerCount> getHosts(Game game, List<Game> games) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();
        Map<Integer,List<GameTime>> times = new HashMap<Integer,List<GameTime>>();

        for (Game gme : games) {
            if (!gme.isFinalized() || gme.getHostId() == null) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }

            PlayerCount host = map.get(gme.getHostId());
            if (host == null) {
                host = new PlayerCount();
                Player player = playerDao.selectById(gme.getHostId());
                host.setPlayer(player);
                host.setCount(1);
                map.put(gme.getHostId(), host);
                
                List<GameTime> gameTimes = new ArrayList<GameTime>();
                times.put(gme.getHostId(), gameTimes);
                if (gme.getStartTime() != null && gme.getActualStartTime() != null) {
                    GameTime gameTime = new GameTime();
                    gameTime.startTime = gme.getStartTime();
                    gameTime.actualStartTime = gme.getActualStartTime();
                    gameTimes.add(gameTime);
                }
            } else {
                int count = host.getCount();
                host.setCount(++count);
                if (gme.getStartTime() != null && gme.getActualStartTime() != null) {
                    List<GameTime> gameTimes = times.get(gme.getHostId());
                    GameTime gameTime = new GameTime();
                    gameTime.startTime = gme.getStartTime();
                    gameTime.actualStartTime = gme.getActualStartTime();
                    gameTimes.add(gameTime);
                }
            }
        }
        
        // Figure out the average start time for a given host
        for(Map.Entry<Integer, PlayerCount> entry : map.entrySet()) {
            Integer key = entry.getKey();
            PlayerCount playerCount = entry.getValue();
            List<GameTime> gameTimes = times.get(key);
            if (gameTimes.size() > 0) {
                // Get the number of millisseconds past 7:30 and average them
                long totalMillis = 0l;
                for (int i = 0; i < gameTimes.size(); ++i) {
                    GameTime gameTime = gameTimes.get(i);
                    totalMillis += gameTime.actualStartTime.getMillis() - 
                            gameTime.startTime.getMillis();
                }
                long averageMillis = totalMillis / gameTimes.size();
                DateTime averageDateTime = gameTimes.get(0).startTime.plus(averageMillis);
                String hours = "" + (averageDateTime.getHourOfDay() - 12);
                String minutes = "" + averageDateTime.getMinuteOfHour();
                if (minutes.length() == 1) {
                    minutes = StringUtils.rightPad(minutes, 2, '0');
                }
                String averageTimeText = hours + ":" + minutes;
                playerCount.setMisc1(averageTimeText);
            }
        }

        List<PlayerCount> hosts = new ArrayList<PlayerCount>(map.values());
        Collections.sort(hosts);
        return hosts;
    } 

    @Override
    public List<PlayerCount> getBankers(Game game, List<Game> games) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();

        for (Game gme : games) {
            if (!gme.isFinalized()) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }

            for (Player player : gme.getBankers()) {
                PlayerCount banker = map.get(player.getId());
                if (banker == null) {
                    banker = new PlayerCount();
                    banker.setPlayer(player);
                    banker.setCount(1);
                    map.put(player.getId(), banker);
                } else {
                    int count = banker.getCount();
                    banker.setCount(++count);
                }
            }
        }

        List<PlayerCount> hosts = new ArrayList<PlayerCount>(map.values());
        Collections.sort(hosts);
        return hosts;
    } 

    @Override
    public SeasonTopTen getTopTen(Game game, Season season) {
        SeasonTopTen topTen = new SeasonTopTen();
        topTen.setGamesPlayed(getGamesPlayed(game, season));
        topTen.setPointsPerGame(getPointsPerGame(game, season));
        topTen.setFirstPlace(getFirstPlace(game, season));
        topTen.setGrossMoney(getGrossMoney(game, season));
        topTen.setNetMoney(getNetMoney(game, season));
        topTen.setNetMoneyPerGame(getNetMoneyPerGame(game, season));
        topTen.setCashes(getCashes(game, season));
        topTen.setMoneyBubble(getMoneyBubble(game, season));
        topTen.setFinalTable(getFinalTable(game, season));
        
        return topTen;
    }

    private void checkSeasonDates(Season newSeason, Season oldSeason) 
            throws InvalidDateException {
 
        // Check that the dates are not null
        if (newSeason.getStartDate() == null) {
            throw new InvalidDateException(null, "Season start date is missing");
        }
        if (newSeason.getEndDate() == null) {
            throw new InvalidDateException(null, "Season end date is missing");
        }
        
        List<Season> existingSeasons = findAll();
        for (Season existingSeason : existingSeasons) {
            
            if (oldSeason != null && oldSeason.getId() == existingSeason.getId()) {
                // Do not compare to itself
                continue;
            }
            
            // Check that the start and end dates are not already in another season
            LocalDate startDate = newSeason.getStartDate();
            LocalDate endDate = newSeason.getEndDate();
            LocalDate existingStartDate = existingSeason.getStartDate();
            LocalDate existingEndDate = existingSeason.getEndDate();

            if (startDate.equals(existingStartDate)) {
                throw new InvalidDateException(startDate, "Season start date already belongs to another season");
            }
            if (startDate.equals(existingEndDate)) {
                throw new InvalidDateException(startDate, "Season start date already belongs to another season");
            }
            if (endDate.equals(existingStartDate)) {
                throw new InvalidDateException(endDate, "Season end date already belongs to another season");
            }
            if (endDate.equals(existingEndDate)) {
                throw new InvalidDateException(endDate, "Season end date already belongs to another season");
            }
            if (startDate.isAfter(existingStartDate) && startDate.isBefore(existingEndDate)) {
                throw new InvalidDateException(startDate, "Season start date already belongs to another season");
            }
            if (endDate.isAfter(existingStartDate) && endDate.isBefore(existingEndDate)) {
                throw new InvalidDateException(endDate, "Season end date already belongs to another season");
            }
        }
    }

    private List<PlayerCount> getGamesPlayed(Game game, Season season) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();

        for (Game gme : season.getGames()) {
            if (!gme.isFinalized()) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }

            for (GamePlayer gamePlayer : gme.getPlayers()) {
                PlayerCount pc = map.get(gamePlayer.getPlayerId());
                if (pc == null) {
                    pc = new PlayerCount();
                    pc.setPlayer(gamePlayer.getPlayer());
                    pc.setCount(1);
                    map.put(gamePlayer.getPlayerId(), pc);
                } else {
                    int count = pc.getCount();
                    pc.setCount(++count);
                }
            }
        }

        return getSortedTopTen(map);
    } 

    private List<PlayerCount> getFirstPlace(Game game, Season season) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();

        for (Game gme : season.getGames()) {
            if (!gme.isFinalized()) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }

            for (GamePlayer gamePlayer : gme.getPlayers()) {
            	if (gamePlayer.getFinish() != null && gamePlayer.getFinish() == 1) {
                    PlayerCount pc = map.get(gamePlayer.getPlayerId());
                    if (pc == null) {
                        pc = new PlayerCount();
                        pc.setPlayer(gamePlayer.getPlayer());
                        pc.setCount(1);
                        map.put(gamePlayer.getPlayerId(), pc);
                    } else {
                        int count = pc.getCount();
                        pc.setCount(++count);
                    }
            	}
            }
        }

        return getSortedTopTen(map);
    } 

    private List<PlayerCount> getGrossMoney(Game game, Season season) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();

        for (Game gme : season.getGames()) {
            if (!gme.isFinalized()) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }
            
            List<GamePayout> payouts = gamePayoutDao.selectByGameId(gme.getId());

            for (GamePlayer gamePlayer : gme.getPlayers()) {
                PlayerCount pc = map.get(gamePlayer.getPlayerId());
                if (pc == null) {
                    pc = new PlayerCount();
                    pc.setPlayer(gamePlayer.getPlayer());
                    int money = 0;
                    if (gamePlayer.getFinish() != null) {
                        money = getMoneyForFinish(gamePlayer.getFinish(), payouts);
                    }
                    pc.setCount(money);
                    map.put(gamePlayer.getPlayerId(), pc);
                } else {
                    int count = pc.getCount();
                    int money = 0;
                    if (gamePlayer.getFinish() != null) {
                        money = getMoneyForFinish(gamePlayer.getFinish(), payouts);
                    }
                    pc.setCount(count + money);
                }
            }
        }
        
        // Remove the negatives
        List<Integer> removeList = new ArrayList<Integer>();
        for (PlayerCount pc : map.values()) {
            if (pc.getCount() < 0) {
                removeList.add(pc.getPlayer().getId());
            }
        }
        
        for (Integer playerId : removeList) {
            map.remove(playerId);
        }

        return getSortedTopTen(map);
    } 

    private List<PlayerCount> getNetMoney(Game game, Season season) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();

        for (Game gme : season.getGames()) {
            if (!gme.isFinalized()) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }
            
            List<GamePayout> payouts = gamePayoutDao.selectByGameId(gme.getId());

            for (GamePlayer gamePlayer : gme.getPlayers()) {
                PlayerCount pc = map.get(gamePlayer.getPlayerId());
                if (pc == null) {
                    pc = new PlayerCount();
                    pc.setPlayer(gamePlayer.getPlayer());
                    int money = 0;
                    if (gamePlayer.getFinish() != null) {
                        money = getMoneyForFinish(gamePlayer.getFinish(), payouts);
                    }
                    money -= gamePlayer.getBuyIn();
                    if (gamePlayer.getReBuyIn() != null) {
                        money -= gamePlayer.getReBuyIn();
                    }
                    pc.setCount(money);
                    map.put(gamePlayer.getPlayerId(), pc);
                } else {
                    int count = pc.getCount();
                    int money = 0;
                    if (gamePlayer.getFinish() != null) {
                        money = getMoneyForFinish(gamePlayer.getFinish(), payouts);
                    }
                    money -= gamePlayer.getBuyIn();
                    if (gamePlayer.getReBuyIn() != null) {
                        money -= gamePlayer.getReBuyIn();
                    }
                    pc.setCount(count + money);
                }
            }
        }
        
        // Remove the negatives
        List<Integer> removeList = new ArrayList<Integer>();
        for (PlayerCount pc : map.values()) {
            if (pc.getCount() < 0) {
                removeList.add(pc.getPlayer().getId());
            }
        }
        
        for (Integer playerId : removeList) {
            map.remove(playerId);
        }

        return getSortedTopTen(map);
    } 

    private List<PlayerCount> getCashes(Game game, Season season) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();

        for (Game gme : season.getGames()) {
            if (!gme.isFinalized()) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }

            List<GamePayout> payouts = gamePayoutDao.selectByGameId(gme.getId());

            for (GamePlayer gamePlayer : gme.getPlayers()) {
                PlayerCount pc = map.get(gamePlayer.getPlayerId());
                if (pc == null) {
                    pc = new PlayerCount();
                    pc.setPlayer(gamePlayer.getPlayer());
                    int money = 0;
                    if (gamePlayer.getFinish() != null) {
                        money = getMoneyForFinish(gamePlayer.getFinish(), payouts);
                    }
                    if (money > 0) {
                        pc.setCount(1);
                    }
                    map.put(gamePlayer.getPlayerId(), pc);
                } else {
                    int count = pc.getCount();
                    int money = 0;
                    if (gamePlayer.getFinish() != null) {
                        money = getMoneyForFinish(gamePlayer.getFinish(), payouts);
                    }
                    if (money > 0) {
                        pc.setCount(++count);
                    }
                }
            }
        }
        
        return getSortedTopTen(map);
    } 

    private List<PlayerCount> getFinalTable(Game game, Season season) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();

        for (Game gme : season.getGames()) {
            if (!gme.isFinalized()) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }
            
            for (GamePlayer gamePlayer : gme.getPlayers()) {
                if (gamePlayer.getFinish() == null || gamePlayer.getFinish() > 10) {
                    continue;
                }
                PlayerCount pc = map.get(gamePlayer.getPlayerId());
                if (pc == null) {
                    pc = new PlayerCount();
                    pc.setPlayer(gamePlayer.getPlayer());
                    pc.setCount(1);
                    map.put(gamePlayer.getPlayerId(), pc);
                } else {
                    int count = pc.getCount();
                    pc.setCount(++count);
                }
            }
        }
        
        return getSortedTopTen(map);
    } 

    private List<PlayerCount> getMoneyBubble(Game game, Season season) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();

        for (Game gme : season.getGames()) {
            if (!gme.isFinalized()) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }
            
            List<GamePayout> payouts = gamePayoutDao.selectByGameId(gme.getId());

            int bubble = 1;
            for (GamePayout payout : payouts) {
                if (payout.getPlace() > bubble) {
                    bubble = payout.getPlace();
                }
            }
            ++bubble;

            for (GamePlayer gamePlayer : gme.getPlayers()) {
                PlayerCount pc = map.get(gamePlayer.getPlayerId());
                if (pc == null) {
                    pc = new PlayerCount();
                    pc.setPlayer(gamePlayer.getPlayer());
                    if (gamePlayer.getFinish() != null && gamePlayer.getFinish() == bubble) {
                        pc.setCount(1);
                    }
                    map.put(gamePlayer.getPlayerId(), pc);
                } else {
                    if (gamePlayer.getFinish() != null && gamePlayer.getFinish() == bubble) {
                        int count = pc.getCount();
                        pc.setCount(++count);
                    }
                }
            }
        }
        
        // Remove zeros
        List<Integer> removeList = new ArrayList<Integer>();
        for (PlayerCount pc : map.values()) {
            if (pc.getCount()  < 1) {
                removeList.add(pc.getPlayer().getId());
            }
        }
        
        for (Integer playerId : removeList) {
            map.remove(playerId);
        }

        return getSortedTopTen(map);
    } 

    private List<PlayerCount> getPointsPerGame(Game game, Season season) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();

        for (Game gme : season.getGames()) {
            if (!gme.isFinalized()) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }

            for (GamePlayer gamePlayer : gme.getPlayers()) {
                PlayerCount pc = map.get(gamePlayer.getPlayerId());
                if (pc == null) {
                    pc = new PlayerCount();
                    pc.setPlayer(gamePlayer.getPlayer());
                    if (gamePlayer.getPoints() != null) {
                        pc.setCount(gamePlayer.getPoints());
                    }
                    pc.setCount2(1);
                    map.put(gamePlayer.getPlayerId(), pc);
                } else {
                    if (gamePlayer.getPoints() != null) {
                        int count = pc.getCount();
                        pc.setCount(count + gamePlayer.getPoints());
                    }
                    int count2 = pc.getCount2();
                    pc.setCount2(++count2);
                }
            }
        }
        
        int minNumGames = (int)Math.round((double)(season.getGames().size() * 0.333d));
        List<Integer> removeList = new ArrayList<Integer>();
        for (PlayerCount pc : map.values()) {
            if (pc.getCount2() < minNumGames) {
                removeList.add(pc.getPlayer().getId());
            }
        }
        
        for (Integer playerId : removeList) {
            map.remove(playerId);
        }

        for (PlayerCount pc : map.values()) {
            int points = pc.getCount();
            int numGames = pc.getCount2();
            double pointsPerGame = (double)points / numGames;
            pc.setCount((int)Math.round(pointsPerGame));
        }

        return getSortedTopTen(map);
    } 

    private List<PlayerCount> getNetMoneyPerGame(Game game, Season season) {
        Map<Integer,PlayerCount> map = new HashMap<Integer,PlayerCount>();

        for (Game gme : season.getGames()) {
            if (!gme.isFinalized()) {
                continue;
            }
            if (gme.getGameDate().isAfter(game.getGameDate())) {
                continue;
            }

            List<GamePayout> payouts = gamePayoutDao.selectByGameId(gme.getId());

            for (GamePlayer gamePlayer : gme.getPlayers()) {
                PlayerCount pc = map.get(gamePlayer.getPlayerId());
                if (pc == null) {
                    pc = new PlayerCount();
                    pc.setPlayer(gamePlayer.getPlayer());
                    int money = 0;
                    if (gamePlayer.getFinish() != null) {
                        money = getMoneyForFinish(gamePlayer.getFinish(), payouts);
                    }
                    money -= gamePlayer.getBuyIn();
                    if (gamePlayer.getReBuyIn() != null) {
                        money -= gamePlayer.getReBuyIn();
                    }
                    pc.setCount(money);
                    pc.setCount2(1);
                    map.put(gamePlayer.getPlayerId(), pc);
                } else {
                    int count = pc.getCount();
                    int money = 0;
                    if (gamePlayer.getFinish() != null) {
                        money = getMoneyForFinish(gamePlayer.getFinish(), payouts);
                    }
                    money -= gamePlayer.getBuyIn();
                    if (gamePlayer.getReBuyIn() != null) {
                        money -= gamePlayer.getReBuyIn();
                    }
                    pc.setCount(count + money);
                    int count2 = pc.getCount2();
                    pc.setCount2(++count2);
                }
            }
        }

        int minNumGames = (int)Math.round((double)(season.getGames().size() * 0.333d));
        List<Integer> removeList = new ArrayList<Integer>();
        for (PlayerCount pc : map.values()) {
            if (pc.getCount2() < minNumGames || pc.getCount() < 1) {
                removeList.add(pc.getPlayer().getId());
            }
        }
        
        for (Integer playerId : removeList) {
            map.remove(playerId);
        }

        for (PlayerCount pc : map.values()) {
            int money = pc.getCount();
            int numGames = pc.getCount2();
            double moneyPerGame = (double)money / numGames;
            pc.setCount((int)Math.round(moneyPerGame));
        }

        return getSortedTopTen(map);
    } 

    private List<PlayerCount> getSortedTopTen(Map<Integer,PlayerCount> map) {
        List<PlayerCount> pcs = new ArrayList<PlayerCount>(map.values());
        Collections.sort(pcs);
        if (pcs.size() > 0) {
            return pcs.subList(0, Math.min(10, pcs.size()));
        } else {
            return pcs;
        }
    }

    private int getMoneyForFinish(int finish, List<GamePayout> payouts) {
        int money = 0;
        
        for (GamePayout payout : payouts) {
            if (payout.getPlace() == finish) {
                if (payout.getChopAmount() != null) {
                    return payout.getChopAmount();
                } else {
                    return payout.getAmount();
                }
            }
        }
        
        return money;
    }

    @Override
    public void updatePayouts(int seasonId, List<SeasonPayout> payouts,
            Set<Integer> deleteIds) {

        // Delete the payouts that the user wants deleted
        for (Integer id : deleteIds) {
            seasonDao.deletePayout(id);
        }
        
        List<SeasonPayout> currentPayouts = seasonDao.selectPayoutsBySeasonId(seasonId);
        
        for (SeasonPayout payout : payouts) {
            if (isPayoutValid(payout)) {
                // Id of 0 means create since the primary key starts at 1
                if (payout.getId() == 0) {
                    seasonDao.insertPayout(payout);
                    continue;
                }

                for (SeasonPayout currentPayout : currentPayouts) {
                    if (currentPayout.getId() == payout.getId()) {
                        currentPayout.setPlace(payout.getPlace());
                        currentPayout.setAmount(payout.getAmount());
                        currentPayout.setDescription(payout.getDescription());
                        seasonDao.updatePayout(currentPayout);
                        break;
                    }
                }
            }
        }
        
        try {
            seasonCalculator.calculate(seasonId);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void emailSeasonSummary(int gameId, int playerId) {
        Game game = gameDao.selectById(gameId);
        Season season = findById(game.getSeasonId());

        List<Game> games = new ArrayList<Game>();
        if (season.getGames() != null && season.getGames().size() > 0) {
            List<Game> existingGames = season.getGames();
            for (Game existingGame : existingGames) {
                if (existingGame.isFinalized()) {
                    games.add(existingGame);
                }
            }
        }
        
        Season shortenedSeason;
        try {
            shortenedSeason = getUpToGame(gameId);
        } catch (Exception e) {
            logger.error(e);
            return;
        }
        
        QuarterlySeason qSeason = null;
        if (shortenedSeason.getQuarterlies() != null && shortenedSeason.getQuarterlies().size() > 0) {
            qSeason = shortenedSeason.getQuarterlies().get(shortenedSeason.getQuarterlies().size() - 1);
        }
        Player player = playerDao.selectById(playerId);
        List<GamePayout> payouts = gamePayoutDao.selectByGameId(gameId);
        List<PlayerCount> hosts = getHosts(game, games);

        mailService.sendSeasonSummary(player, game, shortenedSeason, qSeason, payouts, hosts);

    }


    // Not only checks of the payout is logically valid
    private boolean isPayoutValid(SeasonPayout payout) {
        if (StringUtils.isEmpty(payout.getPlace())) {
            return false;
        }
        if (payout.getAmount() < 1) {
            return false;
        }

        return true;
    }

    private class GameTime {
        public DateTime startTime;
        public DateTime actualStartTime;
    }
}
