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

import com.texastoc.domain.QuarterlySeasonPlayer;

@Repository
public class QuarterlySeasonPlayerDaoImpl implements QuarterlySeasonPlayerDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    PlayerDao playerDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<QuarterlySeasonPlayer> selectByQuarterlySeasonId(int id) {
        List<QuarterlySeasonPlayer> players = this.jdbcTemplate
                .query("select id, playerId, qSeasonId, place, points, "
                        + " numEntries from qseasonplayer "
                        + " where qSeasonId = " + id + " order by points desc",
                        new QuarterlySeasonPlayerMapper());
        
        for (QuarterlySeasonPlayer player : players) {
            player.setPlayer(playerDao.selectById(player.getPlayerId()));
        }

        return players;
    }


    private static final String INSERT_SQL = "INSERT INTO qseasonplayer (playerId, qSeasonId, "
            + " place, points, numEntries) "
            + " VALUES (?,?,?,?,?)";
    @Override
    public int insert(final QuarterlySeasonPlayer player) throws SQLException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                    int index = 1;
                    ps.setInt(index++, player.getPlayerId());
                    ps.setInt(index++, player.getQuarterlySeasonId());
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
    
    private static final String DELETE_ALL_SQL = "delete from qseasonplayer where qSeasonId=";
    @Override
    public void deleteAllByQuarterlySeasonId(int id) throws SQLException {
        this.jdbcTemplate.execute(DELETE_ALL_SQL + id);
    }

    private static final class QuarterlySeasonPlayerMapper implements RowMapper<QuarterlySeasonPlayer> {
        public QuarterlySeasonPlayer mapRow(ResultSet rs, int rowNum) throws SQLException {
            QuarterlySeasonPlayer player = new QuarterlySeasonPlayer();
            player.setId(rs.getInt("id"));
            player.setPlayerId(rs.getInt("playerId"));
            player.setQuarterlySeasonId(rs.getInt("qSeasonId"));
            player.setPlace(rs.getInt("place"));
            player.setPoints(rs.getInt("points"));
            player.setNumEntries(rs.getInt("numEntries"));
            
            return player;
        }
    }

}
