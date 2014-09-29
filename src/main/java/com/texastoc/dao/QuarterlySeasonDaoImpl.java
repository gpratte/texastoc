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
import com.texastoc.domain.Quarter;
import com.texastoc.domain.QuarterlySeason;

@Repository
public class QuarterlySeasonDaoImpl implements QuarterlySeasonDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    GameDao gameDao;
    @Autowired
    GamePlayerDao gamePlayerDao;
    @Autowired
    QuarterlySeasonPlayerDao qSeasonPlayerDao;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final String UPDATE_QUARTERLY_SQL = "UPDATE quarterlyseason set "
            + " startDate=?, endDate=?, note=?, quarter=?, totalQuarterlyToc=?, "
            + " lastCalculated=? where id=?";

    public void update(final QuarterlySeason quarterly) throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(UPDATE_QUARTERLY_SQL);
                    int index = 1;
                    ps.setTimestamp(index++, new Timestamp(quarterly.getStartDate().toDate().getTime()));
                    ps.setTimestamp(index++, new Timestamp(quarterly.getEndDate().toDate().getTime()));
                    ps.setString(index++, quarterly.getNote());
                    ps.setInt(index++, quarterly.getQuarter().getValue());
                    ps.setInt(index++, quarterly.getTotalQuarterlyToc());
                    if (quarterly.getLastCalculated() == null) {
                        ps.setNull(index++, Types.DATE);
                    } else {
                        ps.setTimestamp(index++, new Timestamp(quarterly.getLastCalculated().toDate().getTime()));
                    }
                    ps.setInt(index++, quarterly.getId());
                    return ps;
                }
            });
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
    }
    
    public List<QuarterlySeason> selectBySeasonId(int seasonId) {
        List<QuarterlySeason> quarterlies = this.jdbcTemplate
                .query("select id, seasonId, startDate, endDate, "
                        + " note, totalQuarterlyToc, quarter,"
                        + " lastCalculated from quarterlyseason "
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
        QuarterlySeason quarterly = this.jdbcTemplate
                .queryForObject("select id, seasonId, startDate, endDate, "
                        + " note, totalQuarterlyToc, quarter,"
                        + " lastCalculated from quarterlyseason "
                        + " where id=" + id,
                        new QuarterlySeasonMapper());
        return quarterly;
    }

    private static final String INSERT_QUARTERLY_SQL = "INSERT INTO quarterlyseason "
            + "(seasonId, startDate, endDate, quarter, note) "
            + " VALUES (?,?,?,?,?)";
    public int insert(final QuarterlySeason quarterly) throws SQLException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement(INSERT_QUARTERLY_SQL, Statement.RETURN_GENERATED_KEYS);
                    int index = 1;
                    ps.setInt(index++, quarterly.getSeasonId());
                    ps.setTimestamp(index++, new Timestamp(quarterly.getStartDate().toDate().getTime()));
                    ps.setTimestamp(index++, new Timestamp(quarterly.getEndDate().toDate().getTime()));
                    ps.setInt(index++, quarterly.getQuarter().getValue());
                    ps.setString(index++, quarterly.getNote());
                    return ps;
                }
            },
            keyHolder);
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());

        return keyHolder.getKey().intValue();
    }
    

    private static final class QuarterlySeasonMapper implements RowMapper<QuarterlySeason> {
        public QuarterlySeason mapRow(ResultSet rs, int rowNum) throws SQLException {
            QuarterlySeason quarterly = new QuarterlySeason();
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
            
            return quarterly;
        }
    }
}
