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
import com.texastoc.domain.Season;
import com.texastoc.domain.SeasonHistoryEntry;
import com.texastoc.domain.SeasonPayout;

@Repository
public class SeasonDaoImpl extends BaseJDBCTemplateDao implements SeasonDao {

    static final Logger logger = Logger.getLogger(SeasonDaoImpl.class);

    @Autowired
    private GameDao gameDao;
    @Autowired
    private PlayerDao playerDao;
    @Autowired
    private GamePlayerDao gamePlayerDao;
    @Autowired
    private SeasonPlayerDao seasonPlayerDao;
    @Autowired
    private QuarterlySeasonDao quarterlySeasonDao;
    @Autowired
    private SeasonHistoryEntryDao seasonHistoryEntryDao;

    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public List<Season> selectAll() {
        List<Season> seasons = this.getJdbcTemplate()
                .query("select * from season order by startDate desc",
                        new SeasonMapper());
        return seasons;
    }

    public Season selectById(int id) {
        Season season = this.getJdbcTemplate()
                .queryForObject(
                        "select * from season where id = "
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
            season.setPayouts(selectPayoutsBySeasonId(id));
            
            for (Game game : season.getGames()) {
                game.setPlayers(gamePlayerDao.selectByGameId(game.getId()));
            }
        }
        
        return season;
    }

    private static final String INSERT_SQL = "INSERT INTO season "
            + " (startDate, endDate, useHistoricalData, note) "
            + " VALUES "
            + " (:startDate, :endDate, :historical, :note)";
    public int insert(final Season season) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", season.getStartDate().toDate());
        params.addValue("endDate", season.getEndDate().toDate());
        params.addValue("historical", season.isUseHistoricalData());
        params.addValue("note", season.getNote());

        String [] keys = {"id"};
        getTemplate().update(INSERT_SQL, params, keyHolder, keys);
        
        return keyHolder.getKey().intValue();
    }
    
    private static final String UPDATE_SQL = "UPDATE season set startDate=:startDate, endDate=:endDate, useHistoricalData=:historical, "
            + " note=:note, totalBuyIn=:totalBuyIn, totalReBuy=:totalReBuy, totalPot=:totalPot, "
            + " totalAnnualToc=:totalAnnualToc, lastCalculated=:lastCalculated, "
            + " totalAnnualTocSupplies=:totalAnnualTocSupplies, kittyGameDebit=:kittyGameDebit, "
            + " finalized=:finalized, finalTableImage=:finalTableImage, finalTableThumb=:finalTableThumb "
            + " where id=:id";

    public void update(final Season season) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", season.getStartDate().toDate());
        params.addValue("endDate", season.getEndDate().toDate());
        params.addValue("historical", season.isUseHistoricalData());
        params.addValue("note", season.getNote());
        params.addValue("totalBuyIn", season.getTotalBuyIn());
        params.addValue("totalReBuy", season.getTotalReBuy());
        params.addValue("totalPot", season.getTotalPot());
        params.addValue("totalAnnualToc", season.getTotalAnnualToc());
        params.addValue("totalAnnualTocSupplies", season.getTotalAnnualTocSupplies());
        params.addValue("kittyGameDebit", season.getKittyGameDebit());
        params.addValue("lastCalculated", season.getLastCalculated());
        params.addValue("finalized", season.isFinalized());
        params.addValue("finalTableImage", season.getFinalTableImage());
        params.addValue("finalTableThumb", season.getFinalTableThumb());
        params.addValue("id", season.getId());

        getTemplate().update(UPDATE_SQL, params);
    }

    @Override
    public List<SeasonPayout> selectPayoutsBySeasonId(int seasonId) {
        return this.getJdbcTemplate()
                .query("select * from seasonpayout where seasonId = "
                        + seasonId + " order by place", new SeasonPayoutMapper());
    }

    private static final String INSERT_PAYOUT_SQL = "INSERT INTO seasonpayout "
            + " (seasonId, place, amount, description, temp) "
            + " VALUES "
            + " (:seasonId, :place, :amount, :description, :temp)";
    @Override
    public void insertPayout(SeasonPayout payout) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("seasonId", payout.getSeasonId());
        params.addValue("place", payout.getPlace());
        params.addValue("amount", payout.getAmount());
        params.addValue("description", payout.getDescription());
        params.addValue("temp", payout.isTemp());

        getTemplate().update(INSERT_PAYOUT_SQL, params);
    }

    private static final String UPDATE_PAYOUT_SQL = 
            "UPDATE seasonpayout set place=:place, amount=:amount, "
            + " description=:description, temp=:temp "
            + " where id=:id";
    @Override
    public void updatePayout(SeasonPayout payout) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("place", payout.getPlace());
        params.addValue("amount", payout.getAmount());
        params.addValue("description", payout.getDescription());
        params.addValue("temp", payout.isTemp());
        params.addValue("id", payout.getId());

        getTemplate().update(UPDATE_PAYOUT_SQL, params);
    }

    private static final String DELETE_PAYOUT_SQL = "delete from seasonpayout where id=";
    @Override
    public void deletePayout(int id) {
        this.getJdbcTemplate().execute(DELETE_PAYOUT_SQL + id);
    }

    private static final String DELETE_TEMP_PAYOUTS_SQL = 
            "delete from seasonpayout where seasonId=:seasonId and temp=true";
    @Override
    public void deleteTempPayoutsBySeasonId(int seasonId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("seasonId", seasonId);
        getTemplate().update(DELETE_TEMP_PAYOUTS_SQL, params);
    }

    private static final class SeasonMapper implements RowMapper<Season> {
        public Season mapRow(ResultSet rs, int rowNum) {
            Season season = new Season();
            try {
                season.setId(rs.getInt("id"));
                season.setStartDate(new LocalDate(rs.getDate("startDate")));
                season.setEndDate(new LocalDate(rs.getDate("endDate")));
                season.setUseHistoricalData(rs.getBoolean("useHistoricalData"));
                season.setNote(rs.getString("note"));
                season.setTotalBuyIn(rs.getInt("totalBuyIn"));
                season.setTotalReBuy(rs.getInt("totalReBuy"));
                season.setTotalPot(rs.getInt("totalPot"));
                season.setTotalAnnualToc(rs.getInt("totalAnnualToc"));
                season.setTotalAnnualTocSupplies(rs.getInt("totalAnnualTocSupplies"));
                season.setKittyGameDebit(rs.getInt("kittyGameDebit"));
                season.setFinalized(rs.getBoolean("finalized"));
                season.setFinalTableImage(rs.getString("finalTableImage"));
                season.setFinalTableThumb(rs.getString("finalTableThumb"));

                Date date = rs.getDate("lastCalculated");
                if (date != null) {
                    season.setLastCalculated(new DateTime(date));
                }
            } catch (SQLException e) {
                logger.error(e);
            }
            return season;
        }
    }
    
    private static final class SeasonPayoutMapper implements RowMapper<SeasonPayout> {
        public SeasonPayout mapRow(ResultSet rs, int rowNum) {
            SeasonPayout seasonPayout = new SeasonPayout();
            try {
                seasonPayout.setId(rs.getInt("id"));
                seasonPayout.setSeasonId(rs.getInt("seasonId"));
                seasonPayout.setAmount(rs.getInt("amount"));
                seasonPayout.setDescription(rs.getString("description"));
                seasonPayout.setPlace(rs.getString("place"));
                seasonPayout.setTemp(rs.getBoolean("temp"));
            } catch (SQLException e) {
                logger.error(e);
            }
            return seasonPayout;
        }
    }

}
