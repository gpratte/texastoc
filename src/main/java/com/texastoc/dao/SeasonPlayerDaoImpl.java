package com.texastoc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.SeasonPlayer;

@Repository
public class SeasonPlayerDaoImpl implements SeasonPlayerDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    PlayerDao playerDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<SeasonPlayer> selectBySeasonId(int id) {
        List<SeasonPlayer> players = this.jdbcTemplate
                .query("select id, playerId, seasonId, place, points, "
                        + " numEntries from seasonplayer "
                        + " where seasonId = " + id + " order by points desc",
                        new SeasonPlayerMapper());
        
        for (SeasonPlayer player : players) {
            player.setPlayer(playerDao.selectById(player.getPlayerId()));
        }

        return players;
    }


    private static final String INSERT_SQL = "INSERT INTO seasonplayer (playerId, seasonId, "
            + " place, points, numEntries) "
            + " VALUES (?,?,?,?,?)";
    @Override
    public int insert(final SeasonPlayer player) throws SQLException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                    int index = 1;
                    ps.setInt(index++, player.getPlayerId());
                    ps.setInt(index++, player.getSeasonId());
                    ps.setInt(index++, player.getPlace());
                    ps.setInt(index++, player.getPoints());
                    ps.setInt(index++, player.getNumEntries());

                    return ps;
                }
            },
            keyHolder);
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());

        return keyHolder.getKey().intValue();
    }
    
    private static final String DELETE_ALL_SQL = "delete from seasonplayer where seasonId=";
    @Override
    public void deleteAllBySeasonId(int seasonId) throws SQLException {
        this.jdbcTemplate.execute(DELETE_ALL_SQL + seasonId);
    }

    private static final class SeasonPlayerMapper implements RowMapper<SeasonPlayer> {
        public SeasonPlayer mapRow(ResultSet rs, int rowNum) throws SQLException {
            SeasonPlayer player = new SeasonPlayer();
            player.setId(rs.getInt("id"));
            player.setPlayerId(rs.getInt("playerId"));
            player.setSeasonId(rs.getInt("seasonId"));
            player.setPlace(rs.getInt("place"));
            player.setPoints(rs.getInt("points"));
            player.setNumEntries(rs.getInt("numEntries"));
            
            return player;
        }
    }

}
