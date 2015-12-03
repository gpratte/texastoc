package com.texastoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.Supply;
import com.texastoc.domain.SupplyType;

@Repository
public class SupplyDaoImpl extends BaseJDBCTemplateDao implements SupplyDao {

    static final Logger logger = Logger.getLogger(SupplyDaoImpl.class);

    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    private static final String INSERT_SQL = "INSERT INTO supply "
            + " (gameId, seasonId, typeText, prizePotAmount, "
            + " annualTocAmount, kittyAmount, description, createDate) "
            + " VALUES "
            + " (:gameId, :seasonId, :typeText, :prizePotAmount, "
            + " :annualTocAmount, :kittyAmount, :description, :createDate) ";
    @Override
    public void create(Supply supply) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("gameId", supply.getGameId());
        params.addValue("seasonId", supply.getSeasonId());
        params.addValue("typeText", supply.getType().getText());
        params.addValue("prizePotAmount", supply.getPrizePotAmount());
        params.addValue("annualTocAmount", supply.getAnnualTocAmount());
        params.addValue("kittyAmount", supply.getKittyAmount());
        params.addValue("description", supply.getDescription());
        params.addValue("createDate", supply.getCreateDate().toDate());

        getTemplate().update(INSERT_SQL, params);
    }

    private static final String UPDATE_SQL = "UPDATE supply set "
            + " typeText=:typeText, prizePotAmount=:prizePotAmount, "
            + " annualTocAmount=:annualTocAmount, description=:description,  "
            + " kittyAmount=:kittyAmount  "
            + " where id=:id ";
    @Override
    public void update(Supply supply) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("typeText", supply.getType().getText());
        params.addValue("prizePotAmount", supply.getPrizePotAmount());
        params.addValue("annualTocAmount", supply.getAnnualTocAmount());
        params.addValue("kittyAmount", supply.getKittyAmount());
        params.addValue("description", supply.getDescription());
        params.addValue("id", supply.getId());

        getTemplate().update(UPDATE_SQL, params);
    }

    @Override
    public List<Supply> selectAll() {
        List<Supply> supplies = this.getJdbcTemplate()
                .query("select * from supply order by createDate desc",
                        new SupplyMapper());
        List<Integer> supplyIds = this.getJdbcTemplate()
                .queryForList("select id from supply where invoice is not null",
                        null, Integer.class);
        for (Integer supplyId : supplyIds) {
            for (Supply supply : supplies) {
                if (supplyId == supply.getId()) {
                    supply.setInvoice(true);
                    break;
                }
            }
        }
        return supplies;
    }

    @Override
    public List<Supply> selectSuppliesForGame(int gameId) {
        List<Supply> supplies = this.getJdbcTemplate()
                .query("select * from supply where gameId = " + gameId,
                        new SupplyMapper());
        List<Integer> supplyIds = this.getJdbcTemplate()
                .queryForList("select id from supply where gameId = " + gameId + " and invoice is not null",
                        null, Integer.class);
        for (Integer supplyId : supplyIds) {
            for (Supply supply : supplies) {
                if (supplyId == supply.getId()) {
                    supply.setInvoice(true);
                    break;
                }
            }
        }
        return supplies;
    }

    @Override
    public List<Supply> selectSuppliesForSeason(int seasonId) {
        List<Supply> supplies = this.getJdbcTemplate()
                .query("select * from supply where seasonId = " + seasonId,
                        new SupplyMapper());
        List<Integer> supplyIds = this.getJdbcTemplate()
                .queryForList("select id from supply where seasonId = " + seasonId + " and invoice is not null",
                        null, Integer.class);
        for (Integer supplyId : supplyIds) {
            for (Supply supply : supplies) {
                if (supplyId == supply.getId()) {
                    supply.setInvoice(true);
                    break;
                }
            }
        }
        return supplies;
    }

    @Override
    public Supply selectById(int id) {
        Supply supply = this.getJdbcTemplate()
                .queryForObject("select * from supply where id = " + id,
                        new SupplyMapper());
        try {
            Integer supplyId = this.getJdbcTemplate()
                    .queryForObject("select id from supply where id = " + id + " and invoice is not null",
                            null, Integer.class);
            if (supplyId != null) {
                supply.setInvoice(true);
            }
        } catch(EmptyResultDataAccessException e) {
            // No invoice
        }
        return supply;
    }

    private static final String DELETE_SQL = "delete from supply where id=";
    @Override
    public void delete(int id) {
        this.getJdbcTemplate().execute(DELETE_SQL + id);
    }

    @Override            

    public int getKitty() {
        List<Integer> kittyEntries = this.getJdbcTemplate().queryForList("select kittyDebit from game where finalized = true", Integer.class);
        int kitty = 0;
        for (Integer kittyEntry : kittyEntries) {
            kitty += kittyEntry;
        }
        return kitty;
    }

    @Override
    public int getKittySpent() {
        List<Integer> entries = this.getJdbcTemplate().queryForList("select kittyAmount from supply", Integer.class);
        int spent = 0;
        for (Integer entry : entries) {
            if (entry != null) {
                spent += entry;
            }
        }
        return spent;
    }

    private static final String ADD_INVOICE_SQL = "UPDATE supply set "
            + " invoice=:invoice "
            + " where id=:id ";
    @Override
    public void addInvoice(int supplyId, byte[] invoice) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("invoice", invoice);
        params.addValue("id", supplyId);

        getTemplate().update(ADD_INVOICE_SQL, params);
    }

    private static final String DELETE_INVOICE_SQL = "UPDATE supply set "
            + " invoice = NULL "
            + " where id= ";
    @Override
    public void deleteInvoice(int id) {
        this.getJdbcTemplate().execute(DELETE_INVOICE_SQL + id);
    }

    @Override
    public byte[] selectInvoice(int id) {
        SqlRowSet rowSet = this.getJdbcTemplate().queryForRowSet("select invoice from supply where id=" + id);
        if (rowSet.first()) {
            Object obj = rowSet.getObject("invoice");
            if (obj != null && obj instanceof byte[]) {
                return (byte[])obj;
            }
        }
        return null;
    }

    private static final class SupplyMapper implements RowMapper<Supply> {
        public Supply mapRow(ResultSet rs, int rowNum) {
            Supply supply = new Supply();
            try {
                supply.setId(rs.getInt("id"));

                String temp = rs.getString("gameId");
                if (temp != null) {
                    supply.setGameId(Integer.valueOf(temp));
                }

                temp = rs.getString("seasonId");
                if (temp != null) {
                    supply.setSeasonId(Integer.valueOf(temp));
                }

                supply.setType(SupplyType.fromString(rs.getString("typeText")));
                supply.setPrizePotAmount(rs.getInt("prizePotAmount"));
                
                temp = rs.getString("annualTocAmount");
                if (temp != null) {
                    supply.setAnnualTocAmount(Integer.valueOf(temp));
                }
                temp = rs.getString("kittyAmount");
                if (temp != null) {
                    supply.setKittyAmount(Integer.valueOf(temp));
                }
                
                supply.setDescription(rs.getString("description"));
                supply.setCreateDate(new LocalDate(rs.getDate("createDate")));
            } catch (SQLException e) {
                logger.error(e);
            }
            return supply;
        }
    }

}
