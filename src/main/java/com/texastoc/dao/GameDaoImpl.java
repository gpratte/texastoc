package com.texastoc.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.texastoc.common.HomeGame;
import com.texastoc.domain.Game;
import com.texastoc.util.DateConverter;

@Repository
public class GameDaoImpl extends BaseJDBCTemplateDao implements GameDao {

    static final Logger logger = Logger.getLogger(GameDaoImpl.class);

    @Autowired
    private GameAuditDao gameAuditDao;
    @Autowired
    private GamePlayerDao gamePlayerDao;
    @Autowired
    private GamePayoutDao gamePayoutDao;
    @Autowired
    private PlayerDao playerDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    // Never returns null
    @Override
    public List<Game> selectBySeasonId(int seasonId, boolean includePlayers) {
        List<Game> games = this.getJdbcTemplate()
                .query("select * from game " 
                        + " where seasonId=" + seasonId 
                        + " order by gameDate",
                        new GameMapper());
        for (Game game : games) {
            game.setBankers(playerDao.selectBankersByGameId(game.getId()));
        }
        return games;
    }
    
    @Override
    public List<Game> selectAll() {
        return this.getJdbcTemplate()
                .query("select * from game",
                        new GameMapper());
    }
    
    // Never returns null
    @Override
    public List<Game> selectByDate(LocalDate startDate, LocalDate endDate, boolean includePlayers) {
        List<Game> games = this.getJdbcTemplate()
                .query("select * from game " 
                        + " where gameDate >= '" + DateConverter.getDateAsSQLString(startDate) + "'"
                        + " and gameDate <= '" + DateConverter.getDateAsSQLString(endDate) + "'"
                        + " order by gameDate",
                        new GameMapper());
        return games;
    }
    
    @Override
    public Game selectById(int id) {
        Game game = this.getJdbcTemplate()
                .queryForObject(
                        "select * from game " 
                                + " where id = " + id,
                                new GameMapper());
        game.setPlayers(gamePlayerDao.selectByGameId(id));
        game.setPayouts(gamePayoutDao.selectByGameId(id));
        game.setBankers(playerDao.selectBankersByGameId(id));
        return game;
    }
    
    @Override
    public Game selectMostRecent() {
        Game game = this.getJdbcTemplate()
                .queryForObject(
                        "select * from game order by gameDate desc limit 1",
                                new GameMapper());
        game.setPlayers(gamePlayerDao.selectByGameId(game.getId()));
        game.setPayouts(gamePayoutDao.selectByGameId(game.getId()));
        game.setBankers(playerDao.selectBankersByGameId(game.getId()));
        return game;
    }
    
    private static final String INSERT_SQL = 
            "INSERT INTO game "
            + "(seasonId, gameDate, note, hostId, doubleBuyIn, "
            + " transportRequired, annualIndex, "
            + " quarterlyIndex, kittyDebit, startTime) "
            + " VALUES "
            + " (:seasonId, :gameDate, :note, :hostId, :doubleBuyIn, "
            + " :transportRequired, :annualIndex, "
            + ":quarterlyIndex, :kittyDebit, :startTime)";
    @Override
    public int insert(final Game game) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
 
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("seasonId", game.getSeasonId());
        params.addValue("gameDate", game.getGameDate().toDate());
        params.addValue("note", game.getNote());
        params.addValue("hostId", game.getHostId());
        params.addValue("doubleBuyIn", game.isDoubleBuyIn());
        params.addValue("transportRequired", game.isTransportRequired());
        params.addValue("annualIndex", game.getAnnualIndex());
        params.addValue("quarterlyIndex", game.getQuarterlyIndex());
        params.addValue("kittyDebit", game.getKittyDebit());
        logger.debug("GameDaoImpl inserting startTime " + game.getStartTime() + " tz " + game.getStartTime().getZone());
        params.addValue("startTime", new java.sql.Timestamp(game.getStartTime().getMillis()));

        String [] keys = {"id"};
        getTemplate().update(INSERT_SQL, params, keyHolder, keys);

        //gameAuditDao.insert(game, null);
        
        return keyHolder.getKey().intValue();
    }
    
    
    private static final String UPDATE_SQL = "UPDATE game set gameDate=:gameDate, "
            + " note=:note, totalBuyIn=:totalBuyIn, totalReBuy=:totalReBuy, "
            + " adjustPot=:adjustPot, totalAnnualToc=:totalAnnualToc, "
            + " totalQuarterlyToc=:totalQuarterlyToc, lastCalculated=:lastCalculated, "
            + " seasonId=:seasonId, hostId=:hostId, numPlayers=:numPlayers, "
            + " doubleBuyIn=:doubleBuyIn, transportRequired=:transportRequired, "
            + " finalized=:finalized, payoutDelta=:payoutDelta, "
            + " annualIndex=:annualIndex, quarterlyIndex=:quarterlyIndex, "
            + " totalPotSupplies=:totalPotSupplies, totalAnnualTocSupplies=:totalAnnualTocSupplies, "
            + " kittyDebit=:kittyDebit "
            + " where id=:id";

    @Override
    public void update(final Game game) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("gameDate", game.getGameDate().toDate());
        params.addValue("note", game.getNote());
        params.addValue("totalBuyIn", game.getTotalBuyIn());
        params.addValue("totalReBuy", game.getTotalReBuy());
        params.addValue("adjustPot", game.getAdjustPot());
        params.addValue("totalAnnualToc", game.getTotalAnnualToc());
        params.addValue("totalQuarterlyToc", game.getTotalQuarterlyToc());
        params.addValue("lastCalculated", game.getLastCalculated());
        params.addValue("seasonId", game.getSeasonId());
        params.addValue("hostId", game.getHostId());
        params.addValue("numPlayers", game.getNumPlayers());
        params.addValue("doubleBuyIn", game.isDoubleBuyIn());
        params.addValue("transportRequired", game.isTransportRequired());
        params.addValue("finalized", game.isFinalized());
        params.addValue("payoutDelta", game.getPayoutDelta());
        params.addValue("annualIndex", game.getAnnualIndex());
        params.addValue("quarterlyIndex", game.getQuarterlyIndex());
        params.addValue("totalPotSupplies", game.getTotalPotSupplies());
        params.addValue("totalAnnualTocSupplies", game.getTotalAnnualTocSupplies());
        params.addValue("kittyDebit", game.getKittyDebit());
        params.addValue("id", game.getId());

        getTemplate().update(UPDATE_SQL, params);

        //gameAuditDao.insert(game, null);
    }
    
    private static final String DELETE_SQL = "delete from game where id=:id";
	@Override
	public void delete(int id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        this.getTemplate().update(DELETE_SQL, params);
	}

	@Override
    public void changePayout(int id, int change) {
        if (change == 0) {
            return;
        }
        Game game = this.selectById(id);
        int delta = game.getPayoutDelta();
        delta += change;
        game.setPayoutDelta(delta);
        this.update(game);
    }
	
    private static final String UPDATE_HOMEGAME_SQL = "UPDATE game set "
            + " homegame=:homegame, kittyDebit=:kittyDebit "
            + " where id=:id";
	@Override
    public void updateHomeGame(int gameId, int homeGameMarker, int kitty) {

		MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("homegame", homeGameMarker);
        params.addValue("kittyDebit", kitty);
        params.addValue("id", gameId);

        getTemplate().update(UPDATE_HOMEGAME_SQL, params);
	}

    private static final String UPDATE_TRANSPORT_SQL = "UPDATE game set "
            + " transportRequired=:required  where id=:id";
    @Override
    public void updateTransport(int gameId, boolean required) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("required", required);
        params.addValue("id", gameId);

        getTemplate().update(UPDATE_TRANSPORT_SQL, params);
    }

    private static final String UPDATE_SEATED_SQL = "UPDATE game set "
            + " seated=1 where id=:id";
    @Override
    public void seated(int gameId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", gameId);
        getTemplate().update(UPDATE_SEATED_SQL, params);
    }

    private static final String UPDATE_ACTUAL_START_TIME_SQL = "UPDATE game set "
            + " actualStartTime = now() where id=:id";
    @Override
    public void recordStartTime(int gameId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", gameId);
        getTemplate().update(UPDATE_ACTUAL_START_TIME_SQL, params);
        logger.debug("GameDaoImpl setting actualStartTime to now()");
    }

    private static final class GameMapper implements RowMapper<Game> {
        public Game mapRow(ResultSet rs, int rowNum) {
            Game game = new Game();
            try {
                game.setId(rs.getInt("id"));
                game.setSeasonId(rs.getInt("seasonId"));
                game.setGameDate(new LocalDate(rs.getDate("gameDate")));
                
                String hostId = rs.getString("hostId");
                if (hostId != null) {
                    game.setHostId(rs.getInt("hostId"));
                }
                
                game.setNote(rs.getString("note"));
                game.setTotalBuyIn(rs.getInt("totalBuyIn"));
                game.setTotalReBuy(rs.getInt("totalReBuy"));
                game.setAdjustPot(rs.getInt("adjustPot"));
                game.setTotalAnnualToc(rs.getInt("totalAnnualToc"));
                game.setTotalQuarterlyToc(rs.getInt("totalQuarterlyToc"));
                game.setNumPlayers(rs.getInt("numPlayers"));
                game.setFinalized(rs.getBoolean("finalized"));
                game.setDoubleBuyIn(rs.getBoolean("doubleBuyIn"));
                game.setTransportRequired(rs.getBoolean("transportRequired"));
                game.setPayoutDelta(rs.getInt("payoutDelta"));
                game.setTotalAnnualTocSupplies(rs.getInt("totalAnnualTocSupplies"));
                game.setTotalPotSupplies(rs.getInt("totalPotSupplies"));
                game.setKittyDebit(rs.getInt("kittyDebit"));
                game.setSeated(rs.getBoolean("seated"));

                Date date = rs.getDate("lastCalculated");
                if (date != null) {
                    game.setLastCalculated(new DateTime(date));
                }
                
                java.sql.Timestamp startTime = rs.getTimestamp("startTime");
                if (startTime != null) {
                    DateTimeZone timeZone = DateTimeZone.forID("CST6CDT");
                    game.setStartTime(new DateTime(startTime, timeZone));
                }

                java.sql.Timestamp actualStartTime = rs.getTimestamp("actualStartTime");
                if (actualStartTime != null) {
                    DateTimeZone timeZone = DateTimeZone.forID("CST6CDT");
                    game.setActualStartTime(new DateTime(actualStartTime, timeZone));
                }

                game.setAnnualIndex(rs.getInt("annualIndex"));
                game.setQuarterlyIndex(rs.getInt("quarterlyIndex"));
                
                int homeGameMarker = rs.getInt("homegame");
                game.setHomeGame(HomeGame.fromInt(homeGameMarker));
                
            } catch (SQLException e) {
                logger.error(e);
            }
            
            return game;
        }
    }

}
