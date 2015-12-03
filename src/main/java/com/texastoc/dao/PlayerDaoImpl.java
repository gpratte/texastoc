package com.texastoc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.Player;

@Repository
public class PlayerDaoImpl extends BaseJDBCTemplateDao implements PlayerDao {

    static final Logger logger = Logger.getLogger(PlayerDaoImpl.class);

    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }


    public List<Player> selectAll() {
        List<Player> players = this.getJdbcTemplate()
                .query("select * from player order by firstName, lastName",
                        new PlayerMapper());
        return players;
    }

    public List<Player> selectPossibleHosts() {
        List<Player> players = this.getJdbcTemplate()
                .query("select * from player where possibleHost = true order by firstName, lastName",
                        new PlayerMapper());
        return players;
    }

    public List<Player> selectPossibleTransporters() {
        List<Player> players = this.getJdbcTemplate()
                .query("select * from player where transporter = true order by firstName, lastName",
                        new PlayerMapper());
        return players;
    }

    public List<Player> selectActive() {
        List<Player> players = this.getJdbcTemplate()
                .query("select * from player where active = true order by firstName, lastName",
                        new PlayerMapper());
        return players;
    }

    public List<Player> selectPtcg() {
        List<Player> players = this.getJdbcTemplate()
                .query("select * from player where ptcg = true order by firstName, lastName",
                        new PlayerMapper());
        return players;
    }

    @Override
    public Player selectById(int id) {
        Player player = this.getJdbcTemplate()
                .queryForObject(
                        "select * from player "
                        + " where id = " + id,
                        new PlayerMapper());
        return player;
    }

    @Override
    public Player selectByName(String firstName, String lastName) {
        Player player = this.getJdbcTemplate()
                .queryForObject(
                        "select * from player "
                        + " where firstName = '" + firstName + "'"
                        + " and lastName = '" + lastName + "'",
                        new PlayerMapper());
        return player;
    }

    private static final String INSERT_SQL = "INSERT INTO player "
            + " (id, firstName, lastName, phone, email, cellCarrier, "
            + " address, active, note, possibleHost, transporter, ptcg) "
            + " VALUES "
            + " (:id, :firstName, :lastName, :phone, :email, :cellCarrier, "
            + " :address, :active, :note, :possibleHost, :transporter, :ptcg) ";
    @Override
    public int insert(final Player player) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", player.getId());
        params.addValue("firstName", player.getFirstName());
        params.addValue("lastName", player.getLastName());
        params.addValue("phone", player.getPhone());
        params.addValue("email", player.getEmail());
        params.addValue("cellCarrier", player.getCellCarrier());
        params.addValue("address", player.getAddress());
        params.addValue("active", player.isActive());
        params.addValue("possibleHost", player.isPossibleHost());
        params.addValue("transporter", player.isTransporter());
        params.addValue("ptcg", player.isPtcg());
        params.addValue("note", player.getNote());

        String [] keys = {"id"};
        getTemplate().update(INSERT_SQL, params, keyHolder, keys);

        return keyHolder.getKey().intValue();
    }
    
    private static final String UPDATE_SQL = "UPDATE player set firstName=:firstName, "
            + " lastName=:lastName, phone=:phone, email=:email, "
            + " cellCarrier=:cellCarrier, address=:address, active=:active, "
            + " note=:note, possibleHost=:possibleHost, "
            + " transporter=:transporter, ptcg=:ptcg where id=:id";

    public void update(final Player player) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("firstName", player.getFirstName());
        params.addValue("lastName", player.getLastName());
        params.addValue("phone", player.getPhone());
        params.addValue("email", player.getEmail());
        params.addValue("cellCarrier", player.getCellCarrier());
        params.addValue("address", player.getAddress());
        params.addValue("active", player.isActive());
        params.addValue("possibleHost", player.isPossibleHost());
        params.addValue("transporter", player.isTransporter());
        params.addValue("ptcg", player.isPtcg());
        params.addValue("note", player.getNote());
        params.addValue("id", player.getId());
        
        getTemplate().update(UPDATE_SQL, params);
    }

    private static final String DELETE_SQL = "delete from player where id=";
    @Override
    public void delete(int id) {
        this.getJdbcTemplate().execute(DELETE_SQL + id);
    }

    @Override
    public List<Player> selectBankersByGameId(int gameId) {
        return this.getJdbcTemplate()
                .query("select p.* from player p, gamebanker gb"
                        + " where p.id = gb.playerId  "
                        + " and gb.gameId = " + gameId,
                        new PlayerMapper());
    }
    
    private static final String INSERT_BANKER_SQL = "INSERT INTO gamebanker "
            + " (gameId, playerId) "
            + " VALUES "
            + " (:gameId, :playerId) ";
    @Override
    public void insertGameBanker(int gameId, int playerId) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("gameId", gameId);
        params.addValue("playerId", playerId);

        getTemplate().update(INSERT_BANKER_SQL, params);
    }
    
    private static final String DELETE_BANKER_SQL = "delete from gamebanker " +
                "where gameId=:gameId and playerId=:playerId";
    @Override
    public void deleteGameBanker(int gameId, int playerId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("gameId", gameId);
        params.addValue("playerId", playerId);

        getTemplate().update(DELETE_BANKER_SQL, params);
    }


    private static final class PlayerMapper implements RowMapper<Player> {
        public Player mapRow(ResultSet rs, int rowNum) {
            Player player = new Player();
            try {
                player.setId(rs.getInt("id"));
                player.setFirstName(rs.getString("firstName"));
                player.setLastName(rs.getString("lastName"));
                player.setPhone(rs.getString("phone"));
                player.setEmail(rs.getString("email"));
                player.setCellCarrier(rs.getString("cellCarrier"));
                player.setAddress(rs.getString("address"));
                player.setNote(rs.getString("note"));
                player.setPossibleHost(rs.getBoolean("possibleHost"));
                player.setTransporter(rs.getBoolean("transporter"));
                player.setPtcg(rs.getBoolean("ptcg"));
                player.setActive(rs.getBoolean("active"));
            } catch (SQLException e) {
                logger.error(e);
            }
            return player;
        }
    }

}
