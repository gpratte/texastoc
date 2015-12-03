package com.texastoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.SeasonPlayer;

@Repository
public class SeasonPlayerDaoImpl extends BaseJDBCTemplateDao implements SeasonPlayerDao {

    static final Logger logger = Logger.getLogger(SeasonPlayerDaoImpl.class);

    @Autowired
    private PlayerDao playerDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public List<SeasonPlayer> selectBySeasonId(int id) {
        List<SeasonPlayer> players = this.getJdbcTemplate()
                .query("select * from seasonplayer "
                        + " where seasonId = " + id 
                        + " order by points desc, tie asc",
                        new SeasonPlayerMapper());
        
        for (SeasonPlayer player : players) {
            player.setPlayer(playerDao.selectById(player.getPlayerId()));
        }

        return players;
    }

    @Override
    public SeasonPlayer selectById(int id) {
        SeasonPlayer player = this.getJdbcTemplate()
                .queryForObject(
                        "select * from seasonplayer "
                        + " where id = " + id,
                        new SeasonPlayerMapper());
        player.setPlayer(playerDao.selectById(player.getPlayerId()));
        return player;
    }

    private static final String INSERT_SQL = "INSERT INTO seasonplayer "
            + " (playerId, seasonId, place, points, numEntries) "
            + " VALUES (:playerId, :seasonId, :place, :points, :numEntries)";
    @Override
    public int insert(final SeasonPlayer player) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("playerId", player.getPlayerId());
        params.addValue("seasonId", player.getSeasonId());
        params.addValue("place", player.getPlace());
        params.addValue("points", player.getPoints());
        params.addValue("numEntries", player.getNumEntries());

        String [] keys = {"id"};
        getTemplate().update(INSERT_SQL, params, keyHolder, keys);
        
        return keyHolder.getKey().intValue();
    }
    
    private static final String UPDATE_SQL = "UPDATE seasonplayer set "
            + " place=:place, points=:points, numEntries=:numEntries, "
            + " forfeit=:forfeit, wsop=:wsop, tie=:tie "
            + " where id=:id";
    @Override
    public void update(final SeasonPlayer player) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("place", player.getPlace());
        params.addValue("points", player.getPoints());
        params.addValue("numEntries", player.getNumEntries());
        params.addValue("id", player.getId());
        params.addValue("forfeit", player.isForfeit());
        params.addValue("wsop", player.isWsop());
        params.addValue("tie", player.getTie());

        getTemplate().update(UPDATE_SQL, params);
    }
    
    private static final String DELETE_SQL = "delete from seasonplayer where id=:id";
    @Override
    public void delete(int id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        this.getTemplate().update(DELETE_SQL, params);
    }

    private static final String DELETE_ALL_SQL = "delete from seasonplayer where seasonId=";
    @Override
    public void deleteAllBySeasonId(int seasonId) {
        this.getJdbcTemplate().execute(DELETE_ALL_SQL + seasonId);
    }
    
    private static final class SeasonPlayerMapper implements RowMapper<SeasonPlayer> {
        public SeasonPlayer mapRow(ResultSet rs, int rowNum) {
            SeasonPlayer player = new SeasonPlayer();
            try {
                player.setId(rs.getInt("id"));
                player.setPlayerId(rs.getInt("playerId"));
                player.setSeasonId(rs.getInt("seasonId"));
                player.setPlace(rs.getInt("place"));
                player.setPoints(rs.getInt("points"));
                player.setNumEntries(rs.getInt("numEntries"));
                player.setForfeit(rs.getBoolean("forfeit"));
                player.setWsop(rs.getBoolean("wsop"));
                
                String temp = rs.getString("tie");
                if (temp != null) {
                    player.setTie(Integer.valueOf(temp));
                }
            } catch (SQLException e) {
                logger.error(e);
            }
            
            return player;
        }
    }

}
