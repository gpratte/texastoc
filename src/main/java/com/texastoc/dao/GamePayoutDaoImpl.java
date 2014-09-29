package com.texastoc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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

import com.texastoc.domain.GamePayout;

@Repository
public class GamePayoutDaoImpl implements GamePayoutDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    PlayerDao playerDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GamePayout> selectByGameId(int id) {
        List<GamePayout> payouts = this.jdbcTemplate
                .query("select * from gamepayout "
                        + " where gameId = " + id + " order by amount desc",
                        new GamePayoutMapper());
        return payouts;
    }


    private static final String INSERT_SQL = 
            "INSERT INTO gamepayout (gameId, amount, place, chopAmount, chopPercent) "
            + " VALUES (?,?,?,?,?)";
    @Override
    public void insert(final GamePayout payout) throws SQLException {
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement(INSERT_SQL);
                    int index = 1;
                    ps.setInt(index++, payout.getGameId());
                    ps.setInt(index++, payout.getAmount());
                    ps.setInt(index++, payout.getPlace());
                    if (payout.getChopAmount() == null) {
                        ps.setNull(index++, Types.INTEGER);
                    } else {
                        ps.setInt(index++, payout.getChopAmount());
                    }
                    if (payout.getChopPercent() == null) {
                        ps.setNull(index++, Types.FLOAT);
                    } else {
                        ps.setFloat(index++, payout.getChopPercent());
                    }

                    return ps;
                }
            });
    }
    
    private static final String DELETE_ALL_SQL = "delete from gamepayout where gameId=";
    @Override
    public void deleteAllByGameId(int id) throws SQLException {
        this.jdbcTemplate.execute(DELETE_ALL_SQL + id);
    }

    private static final class GamePayoutMapper implements RowMapper<GamePayout> {
        public GamePayout mapRow(ResultSet rs, int rowNum) throws SQLException {
            GamePayout payout = new GamePayout();
            payout.setGameId(rs.getInt("gameId"));
            payout.setAmount(rs.getInt("amount"));
            payout.setPlace(rs.getInt("place"));
            
            String temp = rs.getString("chopAmount");
            if (temp != null) {
                payout.setChopAmount(Integer.valueOf(temp));
            }
            
            temp = rs.getString("chopPercent");
            if (temp != null) {
                payout.setChopPercent(Float.valueOf(temp));
            }

            return payout;
        }
    }

}
