package com.texastoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.SeasonHistoryEntry;

@Repository
public class SeasonHistoryEntryDaoImpl implements SeasonHistoryEntryDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<SeasonHistoryEntry> selectAllBySeasonId(int seasonId) {
        List<SeasonHistoryEntry> entries = this.jdbcTemplate
                .query("select seasonId, points, playerId, entries "
                        + " from seasonhistoryentry where seasonId = " + seasonId 
                        + " order by points desc",
                        new SeasonHistoryEntryMapper());
        return entries;
    }

    
    private static final class SeasonHistoryEntryMapper implements RowMapper<SeasonHistoryEntry> {
        public SeasonHistoryEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            SeasonHistoryEntry entry = new SeasonHistoryEntry();
            entry.setSeasonId(rs.getInt("seasonId"));
            entry.setPoints(rs.getInt("points"));
            entry.setPlayerId(rs.getInt("playerId"));
            entry.setEntries(rs.getInt("entries"));
            return entry;
        }
    }
}
