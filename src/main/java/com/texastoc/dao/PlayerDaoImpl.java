package com.texastoc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.Player;

@Repository
public class PlayerDaoImpl implements PlayerDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void init(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public List<Player> selectAll() {
        List<Player> players = this.jdbcTemplate
                .query("select * from player order by firstName",
                        new PlayerMapper());
        return players;
    }

    @Override
    public Player selectById(int id) {
        Player player = this.jdbcTemplate
                .queryForObject(
                        "select * from player "
                        + " where id = " + id,
                        new PlayerMapper());
        return player;
    }

    @Override
    public Player selectByName(String firstName, String lastName) {
        Player player = this.jdbcTemplate
                .queryForObject(
                        "select * from player "
                        + " where firstName = '" + firstName + "'"
                        + " and lastName = '" + lastName + "'",
                        new PlayerMapper());
        return player;
    }

    private static final String UPDATE_SQL = "UPDATE player set firstName=?, "
            + " lastName=?, phone=?, email=?, cellCarrier=?, address=?, "
            + " active=?, note=? where id=?";

    public void update(final Player player) throws SQLException {
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(UPDATE_SQL);
                    int index = 1;
                
                    if (player.getFirstName() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getFirstName());
                    }

                    if (player.getLastName() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getLastName());
                    }

                    if (player.getPhone() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getPhone());
                    }

                    if (player.getEmail() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getEmail());
                    }

                    if (player.getCellCarrier() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getCellCarrier());
                    }

                    if (player.getAddress() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getAddress());
                    }

                    ps.setBoolean(index++, player.isActive());

                    if (player.getNote() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getNote());
                    }

                    ps.setInt(index++, player.getId());
                    return ps;
                }
            });
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
    }

    private static final String INSERT_SQL = "INSERT INTO player (id, firstName, "
            + " lastName, phone, email, cellCarrier, address, active, note) "
            + " VALUES (?,?,?,?,?,?,?,?,?)";
    @Override
    public int insert(final Player player) throws SQLException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Connection connection = jdbcTemplate.getDataSource().getConnection();
        jdbcTemplate.update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps =
                        connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                    int index = 1;
                    ps.setInt(index++, player.getId());

                    if (player.getFirstName() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getFirstName());
                    }

                    if (player.getLastName() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getLastName());
                    }

                    if (player.getPhone() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getPhone());
                    }

                    if (player.getEmail() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getEmail());
                    }

                    if (player.getCellCarrier() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getCellCarrier());
                    }

                    if (player.getAddress() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getAddress());
                    }

                    ps.setBoolean(index++, player.isActive());

                    if (player.getNote() == null) {
                        ps.setNull(index++, Types.VARCHAR);
                    } else {
                        ps.setString(index++, player.getNote());
                    }

                    return ps;
                }
            },
            keyHolder);
        
        // ;;!! Not sure if I need to do this
        DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());

        return keyHolder.getKey().intValue();
    }
    
    private static final String DELETE_SQL = "delete from player where id=";
    @Override
    public void delete(int id) throws SQLException {
        this.jdbcTemplate.execute(DELETE_SQL + id);
    }

    
    private static final class PlayerMapper implements RowMapper<Player> {
        public Player mapRow(ResultSet rs, int rowNum) throws SQLException {
            Player player = new Player();
            player.setId(rs.getInt("id"));
            player.setFirstName(rs.getString("firstName"));
            player.setLastName(rs.getString("lastName"));
            player.setPhone(rs.getString("phone"));
            player.setEmail(rs.getString("email"));
            player.setCellCarrier(rs.getString("cellCarrier"));
            player.setAddress(rs.getString("address"));
            player.setNote(rs.getString("note"));
            player.setActive(rs.getBoolean("active"));
            return player;
        }
    }

}
