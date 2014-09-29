package com.texastoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.PointSystem;

@Repository
public class PointSystemDaoImpl implements PointSystemDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public PointSystem selectPointSystem() {
        PointSystem pointSystem = this.jdbcTemplate
                .queryForObject(
                        "select numPlayers, tenthPlaceIncr, tenthPlacePoints, "
                                + " multiplier from pointsystem",
                                new PointSystemMapper());
        return pointSystem;
    }
    
    private static final class PointSystemMapper implements RowMapper<PointSystem> {
        public PointSystem mapRow(ResultSet rs, int rowNum) throws SQLException {
            PointSystem ps = new PointSystem();
            ps.setNumPlayers(rs.getInt("numPlayers"));
            ps.setTenthPlaceIncr(rs.getDouble("tenthPlaceIncr"));
            ps.setTenthPlacePoints(rs.getInt("tenthPlacePoints"));
            ps.setMultiplier(rs.getFloat("multiplier"));

            return ps;
        }
    }
}
