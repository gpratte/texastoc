package com.texastoc.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.Game;
import com.texastoc.util.DateConverter;

@Repository
public class GameDaoImpl implements GameDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    GamePlayerDao gamePlayerDao;
    @Autowired
    GamePayoutDao gamePayoutDao;
    @Autowired
    GameAuditDao gameAuditDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // Never returns null
    @Override
    public List<Game> selectBySeasonId(int seasonId, boolean includePlayers) {
        List<Game> games = this.jdbcTemplate
                .query("select * from game " 
                        + " where seasonId=" + seasonId 
                        + " order by gameDate",
                        new GameMapper());
        return games;
    }
    
    // Never returns null
    @Override
    public List<Game> selectByDate(LocalDate startDate, LocalDate endDate, boolean includePlayers) {
        List<Game> games = this.jdbcTemplate
                .query("select * from game " 
                        + " where gameDate >= '" + DateConverter.getDateAsSQLString(startDate) + "'"
                        + " and gameDate <= '" + DateConverter.getDateAsSQLString(endDate) + "'"
                        + " order by gameDate",
                        new GameMapper());
        return games;
    }
    
    @Override
    public Game selectById(int id) {
        Game game = this.jdbcTemplate
                .queryForObject(
                        "select * from game " 
                                + " where id = " + id,
                                new GameMapper());
        game.setPlayers(gamePlayerDao.selectByGameId(id));
        game.setPayouts(gamePayoutDao.selectByGameId(id));
        return game;
    }
    
    @Override
    public Game selectMostRecent() {
        Game game = this.jdbcTemplate
                .queryForObject(
                        "select * from game order by gameDate desc limit 1",
                                new GameMapper());
        game.setPlayers(gamePlayerDao.selectByGameId(game.getId()));
        game.setPayouts(gamePayoutDao.selectByGameId(game.getId()));
        return game;
    }
    
    private static final String INSERT_SQL = "INSERT INTO game (seasonId, gameDate, note, hostId, doubleBuyIn) "
            + " VALUES (?,?,?,?,?)";
    @Override
    public int insert(final Game game) throws SQLException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                    int index = 1;
                    ps.setInt(index++, game.getSeasonId());
                    ps.setTimestamp(index++, new Timestamp(game.getGameDate().toDate().getTime()));
                    ps.setString(index++, game.getNote());
                    
                    if (game.getHostId() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, game.getHostId());
                    }

                    ps.setBoolean(index++, game.isDoubleBuyIn());
                    return ps;
                }
            },
            keyHolder);
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());

        //gameAuditDao.insert(game, null);
        
        return keyHolder.getKey().intValue();
    }
    
    
    private static final String UPDATE_SQL = "UPDATE game set gameDate=?, "
            + " note=?, totalBuyIn=?, totalReBuy=?, adjustPot=?, "
            + " totalAnnualToc=?, totalQuarterlyToc=?, lastCalculated=?, "
            + " seasonId=?, hostId=?, numPlayers=?, "
            + " doubleBuyIn=?, finalized=?, payoutDelta=? "
            + " where id=?";

    @Override
    public void update(final Game game) throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(UPDATE_SQL);
                    int index = 1;
                    ps.setTimestamp(index++, new Timestamp(game.getGameDate().toDate().getTime()));
                    ps.setString(index++, game.getNote());
                    ps.setInt(index++, game.getTotalBuyIn());
                    ps.setInt(index++, game.getTotalReBuy());
                    ps.setInt(index++, game.getAdjustPot());
                    ps.setInt(index++, game.getTotalAnnualToc());
                    ps.setInt(index++, game.getTotalQuarterlyToc());
                    if (game.getLastCalculated() == null) {
                        ps.setNull(index++, Types.DATE);
                    } else {
                        ps.setTimestamp(index++, new Timestamp(game.getLastCalculated().toDate().getTime()));
                    }
                    ps.setInt(index++, game.getSeasonId());

                    if (game.getHostId() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, game.getHostId());
                    }

                    ps.setInt(index++, game.getNumPlayers());
                    ps.setBoolean(index++, game.isDoubleBuyIn());
                    ps.setBoolean(index++, game.isFinalized());
                    ps.setInt(index++, game.getPayoutDelta());
                    ps.setInt(index++, game.getId());
                    return ps;
                }
            });
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());

        //gameAuditDao.insert(game, null);
    }
    
    @Override
    public void changePayout(int id, int change) throws SQLException {
        if (change == 0) {
            return;
        }
        Game game = this.selectById(id);
        int delta = game.getPayoutDelta();
        delta += change;
        game.setPayoutDelta(delta);
        this.update(game);
    }


    private static final class GameMapper implements RowMapper<Game> {
        public Game mapRow(ResultSet rs, int rowNum) throws SQLException {
            Game game = new Game();
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
            game.setPayoutDelta(rs.getInt("payoutDelta"));

            Date date = rs.getDate("lastCalculated");
            if (date != null) {
                game.setLastCalculated(new DateTime(date));
            }
            return game;
        }
    }
}
