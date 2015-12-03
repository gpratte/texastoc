package com.texastoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.PointSystem;

@Repository
public class PointSystemDaoImpl extends BaseJDBCTemplateDao implements PointSystemDao {

    static final Logger logger = Logger.getLogger(PointSystemDaoImpl.class);

    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public PointSystem selectPointSystem() {
        PointSystem pointSystem = this.getJdbcTemplate()
                .queryForObject(
                        "select * from pointsystem",
                                new PointSystemMapper());
        return pointSystem;
    }
    
    private static final class PointSystemMapper implements RowMapper<PointSystem> {
        public PointSystem mapRow(ResultSet rs, int rowNum) {
            PointSystem ps = new PointSystem();
            try {
                ps.setNumPlayers(rs.getInt("numPlayers"));
                ps.setTenthPlaceIncr(rs.getDouble("tenthPlaceIncr"));
                ps.setTenthPlacePoints(rs.getInt("tenthPlacePoints"));
                ps.setMultiplier(rs.getFloat("multiplier"));
            } catch (SQLException e) {
                logger.error(e);
            }

            return ps;
        }
    }
}
