package com.texastoc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;

@Repository
public class GameAuditDaoImpl implements GameAuditDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    
    private static final String INSERT_SQL = "INSERT INTO gameaudit (gameId, seasonId, "
            + " gameDate, gameNote, hostId, doubleBuyIn, playerId, buyIn, " 
            + " gamePlayerNote, annualTocPlayer, quarterlyTocPlayer, reBuyIn, finish, "
            + " chop, points) "
            + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    @Override
    public void insert(final Game game, final GamePlayer player) throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement(INSERT_SQL);
                    int index = 1;
                    if (game == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, game.getId());
                    }
                    if (game == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, game.getSeasonId());
                    }
                    if (game == null) {
                        ps.setNull(index++, Types.TIMESTAMP);
                    } else {
                        ps.setTimestamp(index++, new Timestamp(game.getGameDate().toDate().getTime()));
                    }
                    if (game == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, game.getNote());
                    }
                    
                    if (game == null || game.getHostId() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, game.getHostId());
                    }

                    if (game == null) {
                        ps.setNull(index++, Types.BOOLEAN);
                    } else {
                        ps.setBoolean(index++, game.isDoubleBuyIn());
                    }

                    if (player == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getPlayerId());
                    }

                    if (player == null || player.getBuyIn() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getBuyIn());
                    }

                    if (player == null || player.getNote() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getNote());
                    }

                    if (player == null) {
                        ps.setNull(index++, Types.BOOLEAN);
                    } else {
                        ps.setBoolean(index++, player.isAnnualTocPlayer());
                    }
                    if (player == null) {
                        ps.setNull(index++, Types.BOOLEAN);
                    } else {
                        ps.setBoolean(index++, player.isQuarterlyTocPlayer());
                    }

                    if (player == null || player.getReBuyIn() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getReBuyIn());
                    }

                    if (player == null || player.getFinish() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getFinish());
                    }

                    if (player == null || player.getChop() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getChop());
                    }

                    if (player == null || player.getPoints() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, player.getPoints());
                    }

                    return ps;
                }
            });
    }
}
