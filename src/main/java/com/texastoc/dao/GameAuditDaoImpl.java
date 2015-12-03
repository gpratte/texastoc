package com.texastoc.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.texastoc.domain.Game;
import com.texastoc.domain.GamePlayer;

@Repository
public class GameAuditDaoImpl extends BaseJDBCTemplateDao implements GameAuditDao {

    @Autowired
    public void init(DataSource dataSource) {
        setDataSource(dataSource);
    }

    
    private static final String INSERT_SQL = "INSERT INTO gameaudit "
            + " (gameId, seasonId, gameDate, gameNote, hostId, "
            + " doubleBuyIn, playerId, buyIn, gamePlayerNote, "
            + " annualTocPlayer, quarterlyTocPlayer, reBuyIn, finish, "
            + " chop, points) "
            + " VALUES "
            + " (:gameId, :seasonId, :gameDate, :gameNote, :hostId, "
            + " :doubleBuyIn, :playerId, :buyIn, :gamePlayerNote, "
            + " :annualTocPlayer, :quarterlyTocPlayer, :reBuyIn, :finish, "
            + " :chop, :points) ";

    @Override
    public void insert(final Game game, final GamePlayer player) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("gameId", game.getId());
        params.addValue("seasonId", game.getSeasonId());
        params.addValue("gameDate", game.getGameDate().toDate());
        params.addValue("gameNote", game.getNote());
        params.addValue("hostId", game.getHostId());
        params.addValue("playerId", player.getPlayerId());
        params.addValue("buyIn", player.getBuyIn());
        params.addValue("gamePlayerNote", player.getNote());
        params.addValue("annualTocPlayer", player.isAnnualTocPlayer());
        params.addValue("quarterlyTocPlayer", player.isQuarterlyTocPlayer());
        params.addValue("reBuyIn", player.getReBuyIn());
        params.addValue("finish", player.getFinish());
        params.addValue("chop", player.getChop());
        params.addValue("points", player.getPoints());

        getTemplate().update(INSERT_SQL, params);
    }
}
