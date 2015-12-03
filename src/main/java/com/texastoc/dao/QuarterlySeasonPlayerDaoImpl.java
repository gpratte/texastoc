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

import com.texastoc.domain.QuarterlySeasonPlayer;

@Repository
public class QuarterlySeasonPlayerDaoImpl extends BaseJDBCTemplateDao implements QuarterlySeasonPlayerDao {

    static final Logger logger = Logger.getLogger(QuarterlySeasonPlayerDaoImpl.class);

    @Autowired
    private PlayerDao playerDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public List<QuarterlySeasonPlayer> selectByQuarterlySeasonId(int id) {
        List<QuarterlySeasonPlayer> players = this.getJdbcTemplate()
                .query("select * from qseasonplayer "
                        + " where qSeasonId = " + id + " order by points desc",
                        new QuarterlySeasonPlayerMapper());
        
        for (QuarterlySeasonPlayer player : players) {
            player.setPlayer(playerDao.selectById(player.getPlayerId()));
        }

        return players;
    }


    private static final String INSERT_SQL = "INSERT INTO qseasonplayer "
            + " (playerId, qSeasonId, place, points, numEntries) "
            + " VALUES "
            + " (:playerId, :qSeasonId, :place, :points, :numEntries)";
    @Override
    public int insert(final QuarterlySeasonPlayer player) {
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("playerId", player.getPlayerId());
        params.addValue("qSeasonId", player.getQuarterlySeasonId());
        params.addValue("place", player.getPlace());
        params.addValue("points", player.getPoints());
        params.addValue("numEntries", player.getNumEntries());

        String [] keys = {"id"};
        getTemplate().update(INSERT_SQL, params, keyHolder, keys);

        return keyHolder.getKey().intValue();
    }
    
    private static final String UPDATE_SQL = "UPDATE qseasonplayer set "
            + " place=:place, points=:points, numEntries=:numEntries "
            + " where id=:id";
    @Override
    public void update(final QuarterlySeasonPlayer player) {
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("place", player.getPlace());
        params.addValue("points", player.getPoints());
        params.addValue("numEntries", player.getNumEntries());
        params.addValue("id", player.getId());

        getTemplate().update(UPDATE_SQL, params);
    }
    
    private static final String DELETE_SQL = "delete from qseasonplayer where id=:id";
    @Override
    public void delete(int id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        this.getTemplate().update(DELETE_SQL, params);
    }

    private static final String DELETE_ALL_SQL = "delete from qseasonplayer where qSeasonId=";
    @Override
    public void deleteAllByQuarterlySeasonId(int id) {
        this.getJdbcTemplate().execute(DELETE_ALL_SQL + id);
    }

    private static final class QuarterlySeasonPlayerMapper implements RowMapper<QuarterlySeasonPlayer> {
        public QuarterlySeasonPlayer mapRow(ResultSet rs, int rowNum) {
            QuarterlySeasonPlayer player = new QuarterlySeasonPlayer();
            try {
                player.setId(rs.getInt("id"));
                player.setPlayerId(rs.getInt("playerId"));
                player.setQuarterlySeasonId(rs.getInt("qSeasonId"));
                player.setPlace(rs.getInt("place"));
                player.setPoints(rs.getInt("points"));
                player.setNumEntries(rs.getInt("numEntries"));
            } catch (SQLException e) {
                logger.error(e);
            }
            
            return player;
        }
    }

}
