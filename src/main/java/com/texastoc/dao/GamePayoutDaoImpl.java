package com.texastoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.GamePayout;
import com.texastoc.service.GameServiceImpl;

@Repository
public class GamePayoutDaoImpl extends BaseJDBCTemplateDao implements GamePayoutDao {

    static final Logger logger = Logger.getLogger(GamePayoutDaoImpl.class);

    @Autowired
    private PlayerDao playerDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public List<GamePayout> selectByGameId(int id) {
        List<GamePayout> payouts = this.getJdbcTemplate()
                .query("select * from gamepayout "
                        + " where gameId = " + id + " order by amount desc",
                        new GamePayoutMapper());
        return payouts;
    }


    private static final String INSERT_SQL = 
            "INSERT INTO gamepayout "
            + " (gameId, amount, place, chopAmount, chopPercent) "
            + " VALUES "
            + " (:gameId, :amount, :place, :chopAmount, :chopPercent)";
    @Override
    public void insert(final GamePayout payout) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("gameId", payout.getGameId());
        params.addValue("amount", payout.getAmount());
        params.addValue("place", payout.getPlace());
        params.addValue("chopAmount", payout.getChopAmount());
        params.addValue("chopPercent", payout.getChopPercent());

        getTemplate().update(INSERT_SQL, params);
    }
    
    private static final String UPDATE_SQL = "UPDATE gamepayout set "
            + " amount=:amount, chopAmount=:chopAmount, chopPercent=:chopPercent "
            + " where gameId=:gameId and place=:place";
    @Override
    public void update(final GamePayout payout) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("amount", payout.getAmount());
        params.addValue("chopAmount", payout.getChopAmount());
        params.addValue("chopPercent", payout.getChopPercent());
        params.addValue("place", payout.getPlace());
        params.addValue("gameId", payout.getGameId());

        getTemplate().update(UPDATE_SQL, params);
    }
    
    private static final String DELETE_SQL = "delete from gamepayout "
            + " where gameId=:gameId and place=:place";
    @Override
    public void delete(int gameId, int place) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("gameId", gameId);
        params.addValue("place", place);
        this.getTemplate().update(DELETE_SQL, params);
    }

    private static final String DELETE_ALL_SQL = "delete from gamepayout where gameId=:gameId";
    @Override
    public void deleteByGameId(int gameId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("gameId", gameId);
        this.getTemplate().update(DELETE_ALL_SQL, params);
    }

    private static final class GamePayoutMapper implements RowMapper<GamePayout> {
        public GamePayout mapRow(ResultSet rs, int rowNum) {
            GamePayout payout = new GamePayout();
            try {
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
            } catch (SQLException e) {
                logger.error(e);
            }

            return payout;
        }
    }

}
