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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.GamePlayer;

@Repository
public class GamePlayerDaoImpl implements GamePlayerDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    PlayerDao playerDao;
    @Autowired
    GameAuditDao gameAuditDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GamePlayer> selectByGameId(int gameId) {
        List<GamePlayer> players = this.jdbcTemplate
                .query("select gp.* from gameplayer gp"
                        + " where gp.gameId = " + gameId 
                        + " and gp.finish is not null "
                        + " order by gp.finish",
                        new GamePlayerMapper());
        
        players.addAll(this.jdbcTemplate
                .query("select gp.* from gameplayer gp, player p "
                        + " where gameId = " + gameId 
                        + " and finish is null "
                        + " and gp.playerId = p.id "
                        + " order by p.firstName",
                        new GamePlayerMapper()));

        // TODO have the mapper get th player from the result set
        for (GamePlayer player : players) {
            player.setPlayer(playerDao.selectById(player.getPlayerId()));
        }

        return players;
    }

    public GamePlayer selectById(int id) {
        GamePlayer player = this.jdbcTemplate
                .queryForObject("select * from gameplayer "
                        + " where id = " + id, 
                        new GamePlayerMapper());

        player.setPlayer(playerDao.selectById(player.getPlayerId()));

        return player;
    }

    private static final String UPDATE_SQL = "UPDATE gameplayer set  playerId=?, "
            + " buyIn=?, note=?, annualTocPlayer=?, "
            + " quarterlyTocPlayer=?, reBuyIn=?, finish=?, chop=?, points=?, "
            + " lastCalculated=?, knockedOut=?, optIn=?  "
            + " where id=?";

    public void update(final GamePlayer player) throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(UPDATE_SQL);
                    int index = 1;
                
                    ps.setInt(index++, player.getPlayerId());
                    
                    if (player.getBuyIn() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getBuyIn());
                    }

                    if (player.getNote() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getNote());
                    }

                    ps.setBoolean(index++, player.isAnnualTocPlayer());
                    ps.setBoolean(index++, player.isQuarterlyTocPlayer());

                    if (player.getReBuyIn() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getReBuyIn());
                    }

                    if (player.getFinish() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getFinish());
                    }

                    if (player.getChop() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getChop());
                    }

                    if (player.getPoints() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getPoints());
                    }

                    if (player.getLastCalculated() == null) {
                        ps.setNull(index++, Types.DATE);
                    } else {
                        ps.setTimestamp(index++, new Timestamp(player.getLastCalculated().toDate().getTime()));
                    }

                    ps.setBoolean(index++, player.isKnockedOut());
                    ps.setBoolean(index++, player.isOptIn());

                    ps.setInt(index++, player.getId());
                    return ps;
                }
            });
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
        
        //gameAuditDao.insert(null, player);
    }

    private static final String INSERT_SQL = "INSERT INTO gameplayer (playerId, gameId, "
            + " buyIn, note, annualTocPlayer, quarterlyTocPlayer, reBuyIn, finish, "
            + " chop, points, lastCalculated, knockedOut, optIn) "
            + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
    @Override
    public int insert(final GamePlayer player) throws SQLException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                    int index = 1;
                    ps.setInt(index++, player.getPlayerId());
                    ps.setInt(index++, player.getGameId());

                    if (player.getBuyIn() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getBuyIn());
                    }

                    if (player.getNote() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getNote());
                    }

                    ps.setBoolean(index++, player.isAnnualTocPlayer());
                    ps.setBoolean(index++, player.isQuarterlyTocPlayer());

                    if (player.getReBuyIn() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getReBuyIn());
                    }

                    if (player.getFinish() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getFinish());
                    }

                    if (player.getChop() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getChop());
                    }

                    if (player.getPoints() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getPoints());
                    }

                    if (player.getLastCalculated() == null) {
                        ps.setNull(index++, Types.DATE);
                    } else {
                        ps.setTimestamp(index++, new Timestamp(player.getLastCalculated().toDate().getTime()));
                    }

                    ps.setBoolean(index++, player.isKnockedOut());
                    ps.setBoolean(index++, player.isOptIn());

                    return ps;
                }
            },
            keyHolder);
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());

        //gameAuditDao.insert(null, player);

        return keyHolder.getKey().intValue();
    }
    
    private static final String DELETE_SQL = "delete from gameplayer where id=";
    @Override
    public void delete(int gamePlayerId) throws SQLException {
        this.jdbcTemplate.execute(DELETE_SQL + gamePlayerId);
    }

    private static final class GamePlayerMapper implements RowMapper<GamePlayer> {
        public GamePlayer mapRow(ResultSet rs, int rowNum) throws SQLException {
            GamePlayer player = new GamePlayer();
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
            
            Date date = rs.getDate("lastCalculated");
            if (date != null) {
                player.setLastCalculated(new DateTime(date));
            }

            return player;
        }
    }

}
