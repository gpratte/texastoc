package com.texastoc.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.GamePlayer;

@Repository
public class GamePlayerDaoImpl extends BaseJDBCTemplateDao implements GamePlayerDao {

    static final Logger logger = Logger.getLogger(GamePlayerDaoImpl.class);

    @Autowired
    private PlayerDao playerDao;
    @Autowired
    private GameAuditDao gameAuditDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public List<GamePlayer> selectByGameId(int gameId) {
        List<GamePlayer> players = this.getJdbcTemplate()
                .query("select gp.* from gameplayer gp"
                        + " where gp.gameId = " + gameId 
                        + " and gp.finish is not null "
                        + " order by gp.finish",
                        new GamePlayerMapper());
        
        players.addAll(this.getJdbcTemplate()
                .query("select gp.* from gameplayer gp, player p "
                        + " where gameId = " + gameId 
                        + " and finish is null "
                        + " and gp.playerId = p.id "
                        + " order by p.firstName, p.lastName",
                        new GamePlayerMapper()));

        // TODO have the mapper get th player from the result set
        for (GamePlayer player : players) {
            player.setPlayer(playerDao.selectById(player.getPlayerId()));
        }

        return players;
    }

    public GamePlayer selectById(int id) {
        GamePlayer player = this.getJdbcTemplate()
                .queryForObject("select * from gameplayer "
                        + " where id = " + id, 
                        new GamePlayerMapper());

        player.setPlayer(playerDao.selectById(player.getPlayerId()));

        return player;
    }

    public GamePlayer selectByPlayerId(int playerId, int gameId) {
        
        GamePlayer player = this.getJdbcTemplate()
                .queryForObject("select * from gameplayer "
                        + " where playerId = " + playerId + " and gameId = " + gameId, 
                        new GamePlayerMapper());

        player.setPlayer(playerDao.selectById(playerId));

        return player;
    }

    private static final String INSERT_SQL = "INSERT INTO gameplayer "
            + " (playerId, gameId, buyIn, note, annualTocPlayer, "
            + " quarterlyTocPlayer, reBuyIn, finish, chop, points, "
            + " lastCalculated, knockedOut, optIn, emailOptIn) "
            + " VALUES "
            + " (:playerId, :gameId, :buyIn, :note, :annualTocPlayer, "
            + " :quarterlyTocPlayer, :reBuyIn, :finish, :chop, :points, "
            + " :lastCalculated, :knockedOut, :optIn, :emailOptIn) ";
    @Override
    public int insert(final GamePlayer player) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("playerId", player.getPlayerId());
        params.addValue("gameId", player.getGameId());
        params.addValue("buyIn", player.getBuyIn());
        params.addValue("note", player.getNote());
        params.addValue("annualTocPlayer", player.isAnnualTocPlayer());
        params.addValue("quarterlyTocPlayer", player.isQuarterlyTocPlayer());
        params.addValue("reBuyIn", player.getReBuyIn());
        params.addValue("finish", player.getFinish());
        params.addValue("chop", player.getChop());
        params.addValue("points", player.getPoints());
        params.addValue("lastCalculated", player.getLastCalculated());
        params.addValue("knockedOut", player.isKnockedOut());
        params.addValue("optIn", player.isOptIn());
        params.addValue("emailOptIn", player.isEmailOptIn());

        String [] keys = {"id"};
        getTemplate().update(INSERT_SQL, params, keyHolder, keys);

        //gameAuditDao.insert(null, player);

        return keyHolder.getKey().intValue();
    }
    
    private static final String UPDATE_SQL = "UPDATE gameplayer set "
            + " playerId=:playerId, buyIn=:buyIn, note=:note, "
            + " annualTocPlayer=:annualTocPlayer, quarterlyTocPlayer=:quarterlyTocPlayer, "
            + " reBuyIn=:reBuyIn, finish=:finish, chop=:chop, points=:points, "
            + " lastCalculated=:lastCalculated, knockedOut=:knockedOut, "
            + " optIn=:optIn, emailOptIn=:emailOptIn where id=:id";

    public void update(final GamePlayer player) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("playerId", player.getPlayerId());
        params.addValue("buyIn", player.getBuyIn());
        params.addValue("note", player.getNote());
        params.addValue("annualTocPlayer", player.isAnnualTocPlayer());
        params.addValue("quarterlyTocPlayer", player.isQuarterlyTocPlayer());
        params.addValue("reBuyIn", player.getReBuyIn());
        params.addValue("finish", player.getFinish());
        params.addValue("chop", player.getChop());
        params.addValue("points", player.getPoints());
        params.addValue("lastCalculated", player.getLastCalculated());
        params.addValue("knockedOut", player.isKnockedOut());
        params.addValue("optIn", player.isOptIn());
        params.addValue("emailOptIn", player.isEmailOptIn());
        params.addValue("id", player.getId());

        getTemplate().update(UPDATE_SQL, params);
        
        //gameAuditDao.insert(null, player);
    }


    private static final String DELETE_SQL = "delete from gameplayer where id=";
    @Override
    public void delete(int gamePlayerId) {
        this.getJdbcTemplate().execute(DELETE_SQL + gamePlayerId);
    }

    private static final class GamePlayerMapper implements RowMapper<GamePlayer> {
        public GamePlayer mapRow(ResultSet rs, int rowNum) {
            GamePlayer player = new GamePlayer();
            try {
                player.setId(rs.getInt("id"));
                player.setPlayerId(rs.getInt("playerId"));
                player.setGameId(rs.getInt("gameId"));
                
                String value = rs.getString("buyIn");
                if (value != null) {
                    player.setBuyIn(Integer.parseInt(value));
                }
                
                value = rs.getString("reBuyIn");
                if (value != null) {
                    player.setReBuyIn(Integer.parseInt(value));
                }

                value = rs.getString("finish");
                if (value != null) {
                    player.setFinish(Integer.parseInt(value));
                }

                value = rs.getString("chop");
                if (value != null) {
                    player.setChop(Integer.parseInt(value));
                }

                value = rs.getString("points");
                if (value != null) {
                    player.setPoints(Integer.parseInt(value));
                }

                value = rs.getString("note");
                if (value != null) {
                    player.setNote(value);
                }

                player.setAnnualTocPlayer(rs.getBoolean("annualTocPlayer"));
                player.setQuarterlyTocPlayer(rs.getBoolean("quarterlyTocPlayer"));
                player.setKnockedOut(rs.getBoolean("knockedOut"));
                player.setOptIn(rs.getBoolean("optIn"));
                player.setEmailOptIn(rs.getBoolean("emailOptIn"));
                
                Date date = rs.getDate("lastCalculated");
                if (date != null) {
                    player.setLastCalculated(new DateTime(date));
                }
            } catch (SQLException e) {
                logger.error(e);
            }

            return player;
        }
    }

    private static final String DELETE_ALL_SQL = "delete from gameplayer where gameId=:gameId";
	@Override
	public void deleteByGame(int gameId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("gameId", gameId);
        this.getTemplate().update(DELETE_ALL_SQL, params);
	}

}
