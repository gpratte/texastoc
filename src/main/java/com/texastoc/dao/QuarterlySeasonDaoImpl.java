package com.texastoc.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.Game;
import com.texastoc.domain.Quarter;
import com.texastoc.domain.QuarterlySeason;

@Repository
public class QuarterlySeasonDaoImpl extends BaseJDBCTemplateDao implements QuarterlySeasonDao {

    static final Logger logger = Logger.getLogger(QuarterlySeasonDaoImpl.class);

    @Autowired
    private GameDao gameDao;
    @Autowired
    private GamePlayerDao gamePlayerDao;
    @Autowired
    private QuarterlySeasonPlayerDao qSeasonPlayerDao;

    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public List<QuarterlySeason> selectBySeasonId(int seasonId) {
        List<QuarterlySeason> quarterlies = this.getJdbcTemplate()
                .query("select * from quarterlyseason "
                        + " where seasonId=" + seasonId + " order by quarter",
                        new QuarterlySeasonMapper());

        for (QuarterlySeason quarterly : quarterlies) {
            quarterly.setGames(gameDao.selectByDate(quarterly.getStartDate(), 
                    quarterly.getEndDate(), false));
            for (Game game : quarterly.getGames()) {
                game.setPlayers(gamePlayerDao.selectByGameId(game.getId()));
            }
            quarterly.setQuarterlySeasonPlayers(qSeasonPlayerDao.selectByQuarterlySeasonId(quarterly.getId()));
        }
        
        return quarterlies;
    }

    public QuarterlySeason selectById(int id) {
        QuarterlySeason quarterly = this.getJdbcTemplate()
                .queryForObject("select * from quarterlyseason "
                        + " where id=" + id,
                        new QuarterlySeasonMapper());
        return quarterly;
    }

    private static final String INSERT_QUARTERLY_SQL = "INSERT INTO quarterlyseason "
            + "(seasonId, startDate, endDate, quarter, note) "
            + " VALUES "
            + " (:seasonId, :startDate, :endDate, :quarter, :note)";
    public int insert(final QuarterlySeason quarterly) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("seasonId", quarterly.getSeasonId());
        params.addValue("startDate", quarterly.getStartDate().toDate());
        params.addValue("endDate", quarterly.getEndDate().toDate());
        params.addValue("quarter", quarterly.getQuarter().getValue());
        params.addValue("note", quarterly.getNote());
        
        String [] keys = {"id"};
        getTemplate().update(INSERT_QUARTERLY_SQL, params, keyHolder, keys);

        return keyHolder.getKey().intValue();
    }
    
    private static final String UPDATE_QUARTERLY_SQL = "UPDATE quarterlyseason set "
            + " startDate=:startDate, endDate=:endDate, note=:note, "
            + " quarter=:quarter, totalQuarterlyToc=:totalQuarterlyToc, "
            + " lastCalculated=:lastCalculated where id=:id";

    public void update(final QuarterlySeason quarterly) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", quarterly.getStartDate().toDate());
        params.addValue("endDate", quarterly.getEndDate().toDate());
        params.addValue("note", quarterly.getNote());
        params.addValue("quarter", quarterly.getQuarter().getValue());
        params.addValue("totalQuarterlyToc", quarterly.getTotalQuarterlyToc());
        params.addValue("lastCalculated", quarterly.getLastCalculated());
        params.addValue("id", quarterly.getId());

        getTemplate().update(UPDATE_QUARTERLY_SQL, params);
    }
    

    private static final class QuarterlySeasonMapper implements RowMapper<QuarterlySeason> {
        public QuarterlySeason mapRow(ResultSet rs, int rowNum) {
            QuarterlySeason quarterly = new QuarterlySeason();
            try {
                quarterly.setId(rs.getInt("id"));
                quarterly.setSeasonId(rs.getInt("seasonId"));
                quarterly.setStartDate(new LocalDate(rs.getDate("startDate")));
                quarterly.setEndDate(new LocalDate(rs.getDate("endDate")));
                quarterly.setNote(rs.getString("note"));
                quarterly.setTotalQuarterlyToc(rs.getInt("totalQuarterlyToc"));

                Date date = rs.getDate("lastCalculated");
                if (date != null) {
                    quarterly.setLastCalculated(new DateTime(date));
                }
                
                int quarterlyValue = rs.getInt("quarter");
                quarterly.setQuarter(Quarter.fromInt(quarterlyValue));
            } catch (SQLException e) {
                logger.error(e);
            }
            
            return quarterly;
        }
    }
}
