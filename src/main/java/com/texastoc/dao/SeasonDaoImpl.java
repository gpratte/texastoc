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
import com.texastoc.domain.Season;
import com.texastoc.domain.SeasonHistoryEntry;

@Repository
public class SeasonDaoImpl implements SeasonDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    GameDao gameDao;
    @Autowired
    PlayerDao playerDao;
    @Autowired
    GamePlayerDao gamePlayerDao;
    @Autowired
    SeasonPlayerDao seasonPlayerDao;
    @Autowired
    QuarterlySeasonDao quarterlySeasonDao;
    @Autowired
    SeasonHistoryEntryDao seasonHistoryEntryDao;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Season> selectAll() {
        List<Season> seasons = this.jdbcTemplate
                .query("select id, startDate, endDate, useHistoricalData, "
                        + " note, totalBuyIn, totalReBuy, totalPot, totalAnnualToc, "
                        + " lastCalculated from season order by startDate desc",
                        new SeasonMapper());
        return seasons;
    }

    public Season selectById(int id) {
        Season season = this.jdbcTemplate
                .queryForObject(
                        "select id, startDate, endDate, useHistoricalData, "
                                + " note, totalBuyIn, totalReBuy, totalPot, totalAnnualToc, "
                                + " lastCalculated from season where id = "
                                + id, new SeasonMapper());
        
        if (season.isUseHistoricalData()) {
            season.setHistories(seasonHistoryEntryDao.selectAllBySeasonId(id));
            for (SeasonHistoryEntry entry : season.getHistories()) {
                entry.setPlayer(playerDao.selectById(entry.getPlayerId()));
            }
        } else {
            season.setQuarterlies(quarterlySeasonDao.selectBySeasonId(id));
            season.setGames(gameDao.selectBySeasonId(season.getId(), false));
            season.setSeasonPlayers(seasonPlayerDao.selectBySeasonId(id));
            
            for (Game game : season.getGames()) {
                game.setPlayers(gamePlayerDao.selectByGameId(game.getId()));
            }
        }
        
        return season;
    }

    private static final String INSERT_SQL = "INSERT INTO season (startDate, endDate, useHistoricalData, note) "
            + " VALUES (?,?,?,?)";
    public int insert(final Season season) throws SQLException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                    int index = 1;
                    ps.setTimestamp(index++, new Timestamp(season.getStartDate().toDate().getTime()));
                    ps.setTimestamp(index++, new Timestamp(season.getEndDate().toDate().getTime()));
                    ps.setBoolean(index++, season.isUseHistoricalData());
                    ps.setString(index++, season.getNote());
                    return ps;
                }
            },
            keyHolder);
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());

        return keyHolder.getKey().intValue();
    }
    
    private static final String UPDATE_SQL = "UPDATE season set startDate=?, endDate=?, useHistoricalData=?, "
            + " note=?, totalBuyIn=?, totalReBuy=?, totalPot=?, totalAnnualToc=?, lastCalculated=? "
            + " where id=?";

    public void update(final Season season) throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(UPDATE_SQL);
                    int index = 1;
                    ps.setTimestamp(index++, new Timestamp(season.getStartDate().toDate().getTime()));
                    ps.setTimestamp(index++, new Timestamp(season.getEndDate().toDate().getTime()));
                    ps.setBoolean(index++, season.isUseHistoricalData());
                    ps.setString(index++, season.getNote());
                    ps.setInt(index++, season.getTotalBuyIn());
                    ps.setInt(index++, season.getTotalReBuy());
                    ps.setInt(index++, season.getTotalPot());
                    ps.setInt(index++, season.getTotalAnnualToc());
                    if (season.getLastCalculated() == null) {
                        ps.setNull(index++, Types.DATE);
                    } else {
                        ps.setTimestamp(index++, new Timestamp(season.getLastCalculated().toDate().getTime()));
                    }
                    ps.setInt(index++, season.getId());
                    return ps;
                }
            });
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
    }
    
    private static final class SeasonMapper implements RowMapper<Season> {
        public Season mapRow(ResultSet rs, int rowNum) throws SQLException {
            Season season = new Season();
            season.setId(rs.getInt("id"));
            season.setStartDate(new LocalDate(rs.getDate("startDate")));
            season.setEndDate(new LocalDate(rs.getDate("endDate")));
            season.setUseHistoricalData(rs.getBoolean("useHistoricalData"));
            season.setNote(rs.getString("note"));
            season.setTotalBuyIn(rs.getInt("totalBuyIn"));
            season.setTotalReBuy(rs.getInt("totalReBuy"));
            season.setTotalPot(rs.getInt("totalPot"));
            season.setTotalAnnualToc(rs.getInt("totalAnnualToc"));

            Date date = rs.getDate("lastCalculated");
            if (date != null) {
                season.setLastCalculated(new DateTime(date));
            }
            return season;
        }
    }
}
