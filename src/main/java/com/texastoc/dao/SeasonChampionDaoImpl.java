package com.texastoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.SeasonChampion;

@Repository
public class SeasonChampionDaoImpl extends BaseJDBCTemplateDao implements SeasonChampionDao {

    static final Logger logger = Logger.getLogger(SeasonChampionDaoImpl.class);

    @Autowired
    private PlayerDao playerDao;
    @Autowired
    private SeasonDao seasonDao;
    
    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public List<SeasonChampion> selectAll() {
        List<SeasonChampion> champions = this.getJdbcTemplate()
                .query("select * from seasonchampion ",
                        new SeasonChampionMapper());
        
        for (SeasonChampion champ : champions) {
            champ.setPlayer(playerDao.selectById(champ.getPlayerId()));
            champ.setSeason(seasonDao.selectById(champ.getSeasonId()));
        }

        return champions;
    }

    private static final class SeasonChampionMapper implements RowMapper<SeasonChampion> {
        public SeasonChampion mapRow(ResultSet rs, int rowNum) {
            SeasonChampion champ = new SeasonChampion();
            try {
                champ.setSeasonId(rs.getInt("seasonId"));
                champ.setPlayerId(rs.getInt("playerId"));
                champ.setPoints(rs.getInt("points"));
            } catch (SQLException e) {
                logger.error(e);
            }
            
            return champ;
        }
    }

}
