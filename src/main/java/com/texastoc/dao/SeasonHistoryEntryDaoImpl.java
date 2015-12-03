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

import com.texastoc.domain.SeasonHistoryEntry;

@Repository
public class SeasonHistoryEntryDaoImpl extends BaseJDBCTemplateDao implements SeasonHistoryEntryDao {

    static final Logger logger = Logger.getLogger(SeasonHistoryEntryDaoImpl.class);

    @Autowired
    private PlayerDao playerDao;

    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public List<SeasonHistoryEntry> selectAllBySeasonId(int seasonId) {
        List<SeasonHistoryEntry> entries = this.getJdbcTemplate()
                .query("select * from seasonhistoryentry where seasonId = " 
                        + seasonId + " order by points desc",
                        new SeasonHistoryEntryMapper());
        return entries;
    }

    @Override
    public SeasonHistoryEntry selectById(int seasonId, int playerId) {
        SeasonHistoryEntry entry = this.getJdbcTemplate()
                .queryForObject(
                        "select * from seasonhistoryentry "
                        + " where seasonId = " + seasonId
                        + " and playerId = " + playerId,
                        new SeasonHistoryEntryMapper());
        entry.setPlayer(playerDao.selectById(playerId));
        return entry;
    }

    private static final String UPDATE_SQL = "UPDATE seasonhistoryentry set "
            + " points=:points, entries=:entries, wsop=:wsop "
            + " where seasonId=:seasonId and playerId=:playerId";
    @Override
    public void update(SeasonHistoryEntry entry) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("points", entry.getPoints());
        params.addValue("entries", entry.getEntries());
        params.addValue("wsop", entry.isWsop());
        params.addValue("seasonId", entry.getSeasonId());
        params.addValue("playerId", entry.getPlayerId());

        getTemplate().update(UPDATE_SQL, params);
    }
    
    private static final class SeasonHistoryEntryMapper implements RowMapper<SeasonHistoryEntry> {
        public SeasonHistoryEntry mapRow(ResultSet rs, int rowNum) {
            SeasonHistoryEntry entry = new SeasonHistoryEntry();
            try {
                entry.setSeasonId(rs.getInt("seasonId"));
                entry.setPoints(rs.getInt("points"));
                entry.setPlayerId(rs.getInt("playerId"));
                entry.setEntries(rs.getInt("entries"));
                entry.setWsop(rs.getBoolean("wsop"));
            } catch (SQLException e) {
                logger.error(e);
            }
            return entry;
        }
    }

}
